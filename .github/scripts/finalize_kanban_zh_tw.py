from pathlib import Path
import re

ROOT = Path('.')


def exact_replace(path: Path, old: str, new: str, label: str) -> None:
    text = path.read_text(encoding='utf-8')
    if old not in text:
        raise RuntimeError(f'{label}: expected text not found in {path}')
    path.write_text(text.replace(old, new, 1), encoding='utf-8')


# -----------------------------------------------------------------------------
# Build baseline compatibility: AGP 9 built-in Kotlin + Compose/KSP alignment.
# -----------------------------------------------------------------------------
versions = ROOT / 'gradle/libs.versions.toml'
text = versions.read_text(encoding='utf-8')
text = text.replace('kotlin = "2.2.10"', 'kotlin = "2.4.10"')
text = text.replace('googleDevtoolsKsp = "2.3.6"', 'googleDevtoolsKsp = "2.3.10"')
versions.write_text(text, encoding='utf-8')

root_build = ROOT / 'build.gradle.kts'
text = root_build.read_text(encoding='utf-8')
if 'org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10' not in text:
    text = '''buildscript {\n  dependencies {\n    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")\n    classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.10")\n  }\n}\n\n''' + text
root_build.write_text(text, encoding='utf-8')

android_ci = ROOT / '.github/workflows/android.yml'
text = android_ci.read_text(encoding='utf-8')
if "gradle-version: '9.3.1'" not in text:
    text = text.replace(
        '      - name: Setup Gradle\n        uses: gradle/actions/setup-gradle@v6\n',
        "      - name: Setup Gradle\n        uses: gradle/actions/setup-gradle@v6\n        with:\n          gradle-version: '9.3.1'\n",
        1,
    )
android_ci.write_text(text, encoding='utf-8')

# -----------------------------------------------------------------------------
# Final fixed UI strings. Technical acronyms and user-entered data stay intact.
# -----------------------------------------------------------------------------
replacements = {
    ROOT / 'app/src/main/java/com/example/MainActivity.kt': {
        'NEXUS ENTERPRISE': '企業',
        ' • NO-CODE PLATFORM': ' • 無程式碼協作平台',
    },
    ROOT / 'app/src/main/java/com/example/ui/screens/InboxScreen.kt': {
        'All Categories (': '所有分類（',
    },
    ROOT / 'app/src/main/java/com/example/ui/screens/OrgTeamScreen.kt': {
        'ORGANIZATIONS (': '組織（',
        'Select Specific ${selectedGranteeType.name}': '選擇指定的 ${selectedGranteeType.name}',
    },
    ROOT / 'app/src/main/java/com/example/ui/components/RepoIssuesSection.kt': {
        'SELECT BLOCKING PREREQUISITE': '選擇前置阻擋任務',
        '  TEAMS': '  團隊',
        '  USERS': '  使用者',
    },
    ROOT / 'app/src/main/java/com/example/ui/screens/UserProfileScreen.kt': {
        'PERSONAL WORKSPACES (OWNED BY ${user.displayName})': '個人工作區（由 ${user.displayName} 擁有）',
    },
}
for path, mapping in replacements.items():
    text = path.read_text(encoding='utf-8')
    for old, new in mapping.items():
        text = text.replace(old, new)
    path.write_text(text, encoding='utf-8')

# Model labels visible through UI badges and summaries.
model = ROOT / 'app/src/main/java/com/example/data/model/GovernanceModels.kt'
text = model.read_text(encoding='utf-8')
model_map = {
    'VIEWER(1, "Read-only access to published artifacts and documents")': 'VIEWER(1, "僅能檢視已發布的成果與文件")',
    'COLLABORATOR(2, "Can create drafts, build no-code workflows, and submit proposals for review")': 'COLLABORATOR(2, "可建立草稿、無程式碼工作流程並送出審查")',
    'REVIEWER(3, "Authorized to review proposals, submit change requests, and validate quality")': 'REVIEWER(3, "可審查提案、要求修改並驗證品質")',
    'APPROVER(4, "Sign-off authority for releases, workflow promotions, and artifact approvals")': 'APPROVER(4, "可對發布、工作流程提升與成果進行正式簽核")',
    'MAINTAINER(5, "Full access to repository settings, access mappings, and policy enforcement")': 'MAINTAINER(5, "可管理儲存庫設定、存取映射與政策執行")',
    'OWNER(6, "Ultimate authority over repository lifecycle, policy overrides, and ownership transfer")': 'OWNER(6, "對儲存庫生命週期、政策例外與所有權移轉負最終權責")',
    'SPECIFICATION_DOC("Product Specification", "說明")': 'SPECIFICATION_DOC("產品規格", "說明")',
    'REQUEST_CHANGES("Request Artifact Changes", RepoRole.REVIEWER)': 'REQUEST_CHANGES("要求修改成果", RepoRole.REVIEWER)',
    'PUBLISH_AND_LOCK("Publish & Lock Artifact", RepoRole.APPROVER)': 'PUBLISH_AND_LOCK("發布並鎖定成果", RepoRole.APPROVER)',
    'REVIEW_REQUEST("審查請求", "Peers requesting your formal review on artifacts or RFCs")': 'REVIEW_REQUEST("審查請求", "同儕要求你正式審查成果或 RFC")',
    'APPROVAL_GATE("核准與簽核", "Governance gates awaiting your cryptographic sign-off")': 'APPROVAL_GATE("核准與簽核", "等待你完成正式簽核的治理關卡")',
    'ISSUE_ASSIGNMENT("任務指派", "Repository issues assigned directly or to your team")': 'ISSUE_ASSIGNMENT("任務指派", "直接指派給你或你的團隊的儲存庫任務")',
    'MENTION_AND_REPLY("提及與回覆", "Direct @mentions and replies to your threads")': 'MENTION_AND_REPLY("提及與回覆", "直接提及你或回覆你參與的討論串")',
    'ACCESS_CHANGE("存取與權限", "Direct repository collaborator grants and role updates")': 'ACCESS_CHANGE("存取與權限", "儲存庫協作者授權與角色更新")',
    'MEMBERSHIP_CHANGE("組織與團隊成員關係", "Organization invitations and team assignment changes")': 'MEMBERSHIP_CHANGE("組織與團隊成員關係", "組織邀請與團隊指派異動")',
    'PUBLICATION("發布與公告", "Artifact milestones published and locked")': 'PUBLICATION("發布與公告", "成果里程碑已發布並鎖定")',
    'GOVERNANCE_EVENT("治理與政策警示", "Enterprise policy checks, dual-approval alerts, and compliance gates")': 'GOVERNANCE_EVENT("治理與政策警示", "企業政策檢查、雙重核准警示與守規關卡")',
}
for old, new in model_map.items():
    text = text.replace(old, new)
model.write_text(text, encoding='utf-8')

# -----------------------------------------------------------------------------
# Final language audit: only fixed UI literals count; dynamic identifiers/acronyms do not.
# -----------------------------------------------------------------------------
ui_files = list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt']
patterns = [
    re.compile(r'Text\(\s*(?:text\s*=\s*)?"([^"\\]*(?:\\.[^"\\]*)*)"'),
    re.compile(r'contentDescription\s*=\s*"([^"\\]*(?:\\.[^"\\]*)*)"'),
]
allow = ('RFC', 'JSON', 'SSO', 'OIDC', 'RBAC', 'ABAC', 'FIDO2', 'KYC', 'SLA', 'ID', 'URL', 'Slug')
residual = []
for path in ui_files:
    source = path.read_text(encoding='utf-8')
    for pattern in patterns:
        for match in pattern.finditer(source):
            value = match.group(1)
            if not re.search(r'[A-Za-z]{3,}', value):
                continue
            stripped = re.sub(r'\$\{[^}]+\}', '', value)
            words = re.findall(r'[A-Za-z][A-Za-z0-9_-]*', stripped)
            fixed_words = [word for word in words if not any(word.upper().startswith(a) for a in allow)]
            # Dynamic-only labels and common technical names are not translation defects.
            dynamic_markers = ('${', '#${', 'v${')
            if fixed_words and not (any(marker in value for marker in dynamic_markers) and len(' '.join(fixed_words)) < 8):
                residual.append(f'{path.name}: {value}')

# Explicit forbidden top-level English labels must always be zero.
forbidden = [
    'Access Governance', 'Home', 'Repositories', 'Inbox', 'Me', 'Kanban Board', 'Nested Tasks',
    'Overview', 'Issues', 'Discussions', 'Artifacts', 'New Issue', 'New Discussion',
    'Unified Inbox', 'Repository Settings', 'Work Requiring Attention', 'SELECT BLOCKING PREREQUISITE',
]
joined = '\n'.join(path.read_text(encoding='utf-8') for path in ui_files)
for token in forbidden:
    if f'"{token}"' in joined:
        residual.append(f'FORBIDDEN: {token}')

report = ROOT / '.github/finalization-audit.txt'
report.write_text('\n'.join(sorted(set(residual))) + f'\nResidual probable fixed-English count: {len(set(residual))}\n', encoding='utf-8')
print(report.read_text(encoding='utf-8'))
print('Finalization pass complete: build versions aligned, bottom-nav labels retained in zh-TW, residual UI audited.')
