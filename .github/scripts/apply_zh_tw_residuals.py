from pathlib import Path
import re

ROOT = Path('.')
UI_FILES = list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt']


def literal_replace(text: str, mapping: dict[str, str]) -> str:
    for src, dst in sorted(mapping.items(), key=lambda item: len(item[0]), reverse=True):
        text = text.replace(f'"{src}"', f'"{dst}"')
    return text


mapping = {
    # Artifact detail
    'Approvals & Cryptographic Signatures ($validApprovalsCount of $requiredApprovers)':'核准與簽章（$validApprovalsCount / $requiredApprovers）',
    'Artifact is officially Published & Locked under Enterprise cryptographic policy.':'成果已依企業簽核政策正式發布並鎖定。',
    'Author: ${artifact.authorDisplayName} • Version ${artifact.version}':'作者：${artifact.authorDisplayName} • 版本 ${artifact.version}',
    'Back':'返回','Evaluating: ${artifact.title}':'評估中：${artifact.title}',
    'Governance Sign-Off & Lifecycle Gates':'治理簽核與生命週期關卡',
    'Hierarchical Governance Lifecycle Pipeline':'階層治理生命週期流程',
    'Inspect Access Control Policy for This Action':'檢視此動作的存取控制政策',
    'Multi-Signature Approver Gate: $collectedApprovals of $requiredApprovers required signatures recorded.':'多重簽核關卡：已取得 $collectedApprovals / $requiredApprovers 個必要簽核。',
    'No approvals recorded yet.':'尚無核准紀錄。','No peer reviews logged yet.':'尚無同儕審查紀錄。',
    'No-Code Blueprint Specification':'無程式碼藍圖規格','Peer Review Decisions (${reviews.size})':'同儕審查決定（${reviews.size}）',
    'Publish & Lock Blueprint':'發布並鎖定藍圖','Reviewer Notes & Verification Feedback':'審查備註與驗證回饋',
    'Submit Blueprint for Peer Review':'送出藍圖進行同儕審查','Submit Decision':'送出決定',
    'Submit Reviewer Decision & Feedback':'送出審查決定與回饋','Submit Reviewer Sign-Off':'送出審查者簽核',
    'This blueprint has been archived.':'此藍圖已封存。',
    'This blueprint is currently in DRAFT. A Collaborator or Maintainer can submit it to begin formal peer review.':'此藍圖目前為草稿；協作者或維護者可送出以開始正式同儕審查。',
    'This blueprint is undergoing Reviewer inspection. Designated Reviewers can evaluate and approve or request changes.':'此藍圖正在審查中；指定審查者可核准或要求修改。',

    # Audit
    'Enterprise Audit Trail & Telemetry':'企業稽核軌跡與監測','Filter audit trail by action, actor, repository...':'依動作、執行者或儲存庫篩選稽核軌跡…',
    'IMMUTABLE REAL-TIME GOVERNANCE LOG':'不可任意竄改的即時治理紀錄',

    # Home
    'Containers':'協作容器','Open Repository':'開啟儲存庫','Quick Navigation':'快速導覽','Recently Accessed Repositories':'最近存取的儲存庫','You':'你',
    '${repo.ownerDisplayName} • $artifactCount Blueprints • $openIssueCount Issues • $discussionCount Discussions':'${repo.ownerDisplayName} • $artifactCount 個藍圖 • $openIssueCount 個任務 • $discussionCount 個討論',
    'View All (${repositories.size})':'查看全部（${repositories.size}）',

    # Inbox
    'All Categories (${notifications.count { if (selectedTab == InboxFilterTab.ARCHIVED) it.status == NotificationStatus.ARCHIVED else it.status != NotificationStatus.ARCHIVED }})':'所有分類（${notifications.count { if (selectedTab == InboxFilterTab.ARCHIVED) it.status == NotificationStatus.ARCHIVED else it.status != NotificationStatus.ARCHIVED }}）',
    'Architecture Info':'架構說明','Distinct from System Audit Log':'與系統稽核紀錄分離','Governance Links':'治理關聯',
    'Governance Preserved: Triggering an action from any notification runs the exact same Hierarchical Policy Engine validation as manual execution.':'治理規則不變：從通知執行動作時，仍會經過與手動操作完全相同的階層政策引擎驗證。',
    'Mark as Read':'標為已讀','Notification Relational Context':'通知關聯脈絡','Open Target Entity':'開啟目標物件',
    'The Unified Inbox establishes a user-centric collaboration layer across the entire enterprise governance hierarchy. It is architecturally separate from the immutable compliance Audit Log:':'統一收件匣在企業治理階層上建立以使用者為中心的協作層，並在架構上與不可任意竄改的守規稽核紀錄分離：',
    'Unified Inbox vs. Audit Log Architecture':'統一收件匣與稽核紀錄架構',
    'While this notification routes an interactive prompt to ${notification.recipientUserId}, the corresponding action record in the Audit Log remains an immutable forensic entry accessible to enterprise compliance officers.':'此通知將互動工作導向 ${notification.recipientUserId}，而相對應的動作仍會在稽核紀錄中保留不可任意竄改的追溯證據，供企業守規人員查核。',
    '• Actionable: Direct 1-click approvals & reviews':'• 可執行：直接處理核准與審查',
    '• Append-only, non-actionable history':'• 僅追加、不可直接操作的歷史',
    '• Enterprise-wide immutable ledger':'• 全企業不可任意竄改的紀錄帳',
    '• Forensic compliance & security stream':'• 守規與安全追溯事件流',
    '• Interactive states: Unread / Read / Archived':'• 互動狀態：未讀／已讀／已封存',
    '• Private to active User identity':'• 僅屬目前使用者身分',
    '• Records all policy engine verdicts':'• 記錄所有政策引擎判定',
    '• Scoped to user\'s assigned responsibilities':'• 依使用者被指派的責任範圍顯示',

    # Organization / Team
    '+ Add User':'+ 新增使用者','+ Enterprise':'+ 企業','+ Member':'+ 成員','+ Team':'+ 團隊','Add Action':'新增操作','Add Member':'新增成員',
    'All enterprise users are already members of this organization.':'所有企業使用者都已是此組織成員。',
    'All users are already assigned to this team.':'所有使用者都已加入此團隊。',
    'Allow User-Owned Repositories':'允許使用者擁有儲存庫','Allow non-organization workspaces':'允許非組織工作區',
    'Assign Organization Role':'指派組織角色','CLOSED-LOOP MULTI-LAYER ACCESS CONTROL DELEGATION':'閉環多層級存取控制授權',
    'Changes apply immediately across all organizations and repositories under ${enterprise.name}.':'變更會立即套用至 ${enterprise.name} 下所有組織與儲存庫。',
    'Configure':'設定','Configure Governance Policies':'設定治理政策','Create New Enterprise':'建立新企業','Create Organization':'建立組織','Create Team':'建立團隊',
    'Create your first Organization to group teams and own collaboration containers.':'建立第一個組織，用來管理團隊並擁有協作容器。',
    'Default Member Repository Role':'成員預設儲存庫角色','Display Name (e.g. Maya Lin)':'顯示名稱（例如：Maya Lin）',
    'Dual Approval Gate':'雙重核准關卡','Dual Approver Gate':'雙核准人關卡','ENTERPRISE ADMIN':'企業管理員','Enterprise Admin Privilege':'企業管理員權限',
    'Enterprise Email':'企業電子郵件','Enterprise Governance Policies':'企業治理政策','Enterprise Identity Roster (${users.size})':'企業身分名冊（${users.size}）',
    'Enterprise Name':'企業名稱','Establish Enterprise':'建立企業','Establish a dedicated operating entity capable of grouping teams and owning workspaces.':'建立可管理團隊並擁有工作區的專屬營運實體。',
    'Establish a root organizational boundary with customized governance baseline.':'建立具自訂治理基準的根組織邊界。','Governance Purpose':'治理目的','Grant top-level administrative authority':'授予最高層管理權限',
    'Hierarchical Model & Permission Matrix':'階層模型與權限矩陣','Initial Organization Owner':'初始組織擁有者',
    'LEVEL 2: ORGANIZATIONS (${organizations.size})':'第 2 層：組織（${organizations.size}）','LEVEL 3: TEAMS (Child of Org)':'第 3 層：團隊（隸屬組織）','LEVEL 4: WORKSPACES (Owned by Org)':'第 4 層：工作區（由組織擁有）',
    'Nested Under Parent Team (Optional)':'隸屬上層團隊（選填）','New Org':'新增組織','No members registered.':'尚無成員。','No organizations established yet':'尚未建立組織',
    'No repositories owned by this organization.':'此組織尚未擁有任何儲存庫。','No team members assigned yet':'尚未指派團隊成員','No teams established under this organization.':'此組織下尚未建立團隊。',
    'ORGANIZATION MEMBERS & GOVERNANCE ROLES':'組織成員與治理角色','OWNED NO-CODE REPOSITORIES':'擁有的無程式碼儲存庫','Operating entities containing teams, members, and workspaces':'包含團隊、成員與工作區的營運實體',
    'Organization Mission & Scope':'組織使命與範圍','Organization Name (e.g. Fintech & Payments)':'組織名稱（例如：Fintech & Payments）','Peer Review Gate':'同儕審查關卡',
    'Prohibit authors from reviewing or approving own work':'禁止作者審查或核准自己的成果','Provision Enterprise User':'建立企業使用者','Provision Identity':'建立身分',
    'ROOT COMPLIANCE GATES & ACCESS CONTROL INHERITANCE':'根層守規關卡與存取控制繼承','Require 2 sign-offs for release promotions':'發布提升需取得 2 個簽核','Require review before approver sign-off':'核准人簽核前必須先通過審查',
    'Reviewer Gate Before Sign-Off':'簽核前審查者關卡','Save Policies':'儲存政策','Security Governance Policies':'安全治理政策','Segregation of Duties (SoD)':'職責分離（SoD）','Select User':'選擇使用者',
    'Slug Identifier':'Slug 識別碼','Slug Identifier (@core-infra)':'Slug 識別碼（@core-infra）','Slug Identifier (@fintech-payments)':'Slug 識別碼（@fintech-payments）','TEAMS ROSTER':'團隊名冊',
    'Team Name (e.g. Core Infrastructure)':'團隊名稱（例如：Core Infrastructure）','Team Purpose & Mission':'團隊目的與使命','Team Role':'團隊角色','Teams are organization-scoped groups that receive repository permissions.':'團隊是組織範圍內的群組，可取得儲存庫權限。',
    'Title / Professional Role':'職稱／專業角色','Top-level team (No parent)':'最上層團隊（無上層）','User-Owned Workspaces':'使用者擁有的工作區','Username handle':'使用者帳號',

    # Persona / policy simulator
    'ENT ADMIN':'企業管理員','Selected':'已選取','Simulate how different roles and hierarchical access control policies behave across enterprise no-code repositories.':'模擬不同角色與階層存取控制政策在企業無程式碼儲存庫中的行為。','Switch Active Persona':'切換目前身分',
    'Access Policy Engine & Simulator':'存取政策引擎與模擬器','Actor (User Persona)':'執行者（使用者身分）','Enterprise Compliance Guardrails':'企業守規護欄','Evaluate Access Policy':'評估存取政策',
    'Governance Action to Test':'要測試的治理動作','Hierarchy & Entity Schema Mapping':'階層與實體結構映射','Live Access Evaluation Inspector':'即時存取評估檢視器','None (Repository Scope)':'無（儲存庫範圍）',
    'Reverse-Engineered GitHub Enterprise Access Control Semantics':'逆向 GitHub Enterprise 的存取控制語意','Simulate how enterprise policies, team inheritances, and segregation of duties resolve for any combination of Actor, Repository, and Action.':'模擬企業政策、團隊繼承與職責分離如何針對任意執行者、儲存庫與動作產生最終判定。',
    'Strict structural constraints reverse-engineered from GitHub enterprise governance:':'由 GitHub 企業治理逆向整理出的嚴格結構限制：','Target No-Code Artifact (Optional)':'目標無程式碼成果（選填）','Target Repository Container':'目標儲存庫容器','These global policies cascade strictly down to all Organizations, Teams, and Repositories:':'這些全域政策會嚴格向下套用至所有組織、團隊與儲存庫：',
    '1. Enterprise Guardrail Evaluations':'1. 企業護欄評估','2. Repository Role & Lifecycle Quorum Checks':'2. 儲存庫角色與生命週期門檻檢查','Acknowledge & Close':'確認並關閉','HIERARCHICAL GOVERNANCE ENGINE':'階層治理引擎','Policy Evaluation Trace':'政策評估軌跡',

    # Repository detail
    'ACTION REQUIRED':'需處理','ANSWERED':'已回答','Active Discussions & RFCs':'進行中的討論與 RFC','Add No-Code Artifact':'新增無程式碼成果','All Blueprints Up to Date':'所有藍圖皆為最新',
    'Artifact Schema Type':'成果結構類型','Artifact Title':'成果標題','Assign Access Role':'指派存取角色','Assign Hierarchical Role':'指派階層角色','Assign Role':'指派角色','BLOCKED':'受阻',
    'Back to Repository Workspace':'返回儲存庫工作區','Collaborators & Team Access Mappings':'協作者與團隊存取映射','Container Resource Summary':'容器資源摘要','Create Blueprint':'建立藍圖','Create Draft':'建立草稿',
    'Executive Summary':'摘要','General Container Information':'一般容器資訊','Grant Role':'授予角色','Hierarchical permissions mapped to Users and Teams':'映射至使用者與團隊的階層權限','Important Blueprints & Specs':'重要藍圖與規格','Locked':'已鎖定','Manage Access':'管理存取',
    'New Blueprint':'新增藍圖','New No-Code Artifact / Blueprint':'新增無程式碼成果／藍圖','No audit log events recorded for this repository yet.':'此儲存庫尚無稽核事件紀錄。','No blueprints created yet. Switch to Artifacts tab to create specifications, workflows, or schemas.':'尚未建立藍圖；請切換至成果分頁建立規格、工作流程或資料結構。',
    'No blueprints or documents created yet in this container.':'此容器尚未建立藍圖或文件。','No discussions started yet. Start an RFC or proposal in the Discussions tab.':'尚未開始討論；請在討論分頁建立 RFC 或提案。','No explicit access rules configured for this container.':'此容器尚未設定明確的存取規則。',
    'No open action items. Everything is on track.':'目前沒有未完成的行動項目，進度正常。','No pending peer reviews or approval quorum sign-offs required.':'目前沒有待處理的同儕審查或核准門檻簽核。','No recent activity recorded yet.':'尚無近期活動紀錄。','Open Action Items':'未完成行動項目','Pending Reviews & Approvals':'待審查與核准',
    'Remove Role':'移除角色','Repository Hierarchical Governance Policies':'儲存庫階層治理政策','Repository Purpose & Scope':'儲存庫目的與範圍','Structured No-Code Blueprint (JSON / Schema)':'結構化無程式碼藍圖（JSON／Schema）','Team Entity':'團隊實體','User Entity':'使用者實體','View Full Trail':'查看完整軌跡','Your Effective Access':'你的有效存取權限',

    # Discussions
    'Answered':'已回答','Discussion Content & Specification *':'討論內容與規格 *','Discussion Title *':'討論標題 *','No replies yet. Contribute to this RFC or answer the question below.':'尚無回覆；請針對此 RFC 提供意見或回答下方問題。','Outline the proposal, questions, tradeoffs, or governance rules...':'說明提案、問題、權衡或治理規則…','Publish Discussion':'發布討論','Replies & Answers (${comments.size})':'回覆與回答（${comments.size}）','Start a Discussion':'開始討論','This conversation has been locked by repository maintainers. Replies are disabled.':'此討論已被儲存庫維護者鎖定，無法再回覆。','Upvote':'贊成','Write a reply or RFC feedback...':'撰寫回覆或 RFC 意見…','e.g. RFC: Unified Declarative State Machine Model':'例如：RFC－統一宣告式狀態機模型',

    # Issues
    'Activity & Comments (${comments.size})':'活動與留言（${comments.size}）','All (${issues.size})':'全部（${issues.size}）','BLOCKING (Blocks Downstream):':'阻擋下游任務：','Close Issue':'關閉任務','Labels (comma-separated)':'標籤（以逗號分隔）','No blocking dependencies attached to this issue.':'此任務尚無阻擋相依。','No sub-issues linked yet. Break this task down into tracked sub-components.':'尚未連結子任務；可將此工作拆解成可追蹤的子項目。','Reopen Issue':'重新開啟任務','Resolve prerequisite blocking tasks before executing or closing this issue.':'請先完成前置阻擋任務，再執行或關閉此任務。','Start Work':'開始處理','Submit Issue':'送出任務','e.g. compliance, security, bug, sla':'例如：守規、安全、問題、SLA','e.g., Update KYC schema for Tier-3 approvals':'例如：更新第 3 級核准的 KYC 結構',

    # Repository catalog
    'Category (e.g. Process Automation, RFCs)':'分類（例如：流程自動化、RFC）','Create Workspace':'建立工作區','Display Name (e.g. Core API Blueprints)':'顯示名稱（例如：Core API 藍圖）','New No-Code Repository Container':'新增無程式碼儲存庫容器','Only an Organization or User can Owner a Repository. Teams cannot own repositories; they inherit collaboration roles.':'只有組織或使用者可以擁有儲存庫；團隊不能擁有儲存庫，只能繼承協作角色。','Open Repo':'開啟儲存庫','Purpose & Governance Scope':'目的與治理範圍','Repository Owner Entity':'儲存庫擁有者實體','Repository Slug Identifier':'儲存庫 Slug 識別碼',

    # User profile
    'Account Display & Identity Customization':'帳號顯示與身分自訂','Act as Persona':'切換為此身分','Architectural Separation of Concerns':'架構責任分離','Authored No-Code Artifacts (${artifacts.size})':'建立的無程式碼成果（${artifacts.size}）','Avatar Badge Color':'頭像識別色','Bio / Focus Area':'簡介／專注領域','COLLABORATOR & ORGANIZATIONAL REPOSITORIES':'協作者與組織儲存庫','ENTERPRISE FOOTPRINT & RESPONSIBILITIES':'企業範圍與責任','Edit Profile':'編輯個人檔案','Edit User Profile':'編輯使用者檔案','Effective Permissions Matrix':'有效權限矩陣','Enterprise Policy & Authority Scope':'企業政策與權限範圍','Evaluated capabilities determined by Enterprise, Organization, Team memberships, and Repo Access Rules:':'實際能力由企業、組織、團隊成員關係與儲存庫存取規則共同決定：','Federated Authentication & Single Sign-On':'聯邦驗證與單一登入','Formal Approver Sign-offs (${approvals.size})':'正式核准簽核（${approvals.size}）','Formal Reviews Submitted (${reviews.size})':'已提交正式審查（${reviews.size}）','Hierarchy & Relationship Architecture':'階層與關係架構','ISSUES AUTHORED / ASSIGNED (${issues.size})':'建立／被指派的任務（${issues.size}）','Identity & Role Profile':'身分與角色檔案','Illustrating the direct relational mapping from User Profile down through Enterprise, Organizations, Teams, and Repositories:':'顯示使用者檔案向下連結企業、組織、團隊與儲存庫的關係：','Inspect User':'檢視使用者','Issues & RFC Discussions':'任務與 RFC 討論','Location':'位置','Modify Profile Information':'修改個人檔案資訊','Multi-Factor & Cryptographic Tokens':'多因素驗證與安全權杖','No accessible repositories assigned to this user.':'此使用者目前沒有可存取的儲存庫。','No formal artifact reviews recorded for this user.':'此使用者尚無正式成果審查紀錄。','No formal sign-offs executed by this user.':'此使用者尚無正式簽核紀錄。','No issues or RFC discussions recorded for this user.':'此使用者尚無任務或 RFC 討論紀錄。','Notification Routing & Policy Subscriptions':'通知路由與政策訂閱','Notification Subscriptions':'通知訂閱','Organization Memberships (${memberships.size})':'組織成員關係（${memberships.size}）','Pronouns (e.g. they/them)':'代名詞（例如：they/them）','Recent Attributed Activity':'近期歸屬活動','Repositories & Workspaces':'儲存庫與工作區','SIGNED OFF':'已簽核','Save Profile':'儲存個人檔案','Team Memberships & Maintainer Roles (${teamMemberships.size})':'團隊成員與維護者角色（${teamMemberships.size}）','This user does not currently hold membership in any Organization.':'此使用者目前不屬於任何組織。','This user is not assigned to any collaborative Teams.':'此使用者目前未加入任何協作團隊。','User Profile serves as the centralized identity representation across the Enterprise while strictly decoupling Authentication (SAML/OIDC SSO, FIDO2 tokens), Authorization (Role & Permission Hierarchy), and Account Settings.':'使用者檔案是企業中的集中身分表示，同時嚴格分離驗證（SAML／OIDC SSO、FIDO2 權杖）、授權（角色與權限階層）以及帳號設定。','YOU':'你'
}

fragment_mapping = {
    'Active: ${activeUser?.displayName ?: ':'目前身分：${activeUser?.displayName ?: ',
    'Actor: ':'執行者：',' • Repo: ':' • 儲存庫：','Container: ':'容器：','Linked: ':'連結：','Status: ':'狀態：',
    'From ${notification.actorDisplayName}':'來自 ${notification.actorDisplayName}','Inbox Scoped To: ':'收件匣範圍：','ID: ${notification.id}':'通知 ID：${notification.id}',
    ' Members • ':' 個成員 • ',' Teams':' 個團隊',' Members':' 個成員',' Sign-Offs Required':' 個簽核為必要','Default: ':'預設：',
    "Add Member to '":"新增成員至『","Create Team under '":"在『",
    'All provisioned accounts within ':'已建立的所有帳號皆位於 ','ROOT GOVERNANCE ENTITY • ':'根治理實體 • ','Default Repo Role: ':'預設儲存庫角色：',
    'Min: ':'最低：','Actor: ${log.actorDisplayName} • ':'執行者：${log.actorDisplayName} • ','Author: ':'作者：',' • Type: ':' • 類型：',' upvotes':' 票贊成',
    'Approver Quorum: ':'核准門檻：',' Sign-off':' 個簽核','By ':'由 ','Resolved via: ':'解析來源：','Role via: ':'角色來源：','Scope: ':'範圍：','Select ${selectedGranteeType.name}':'選擇 ${selectedGranteeType.name}',
    ' ACTION REQUIRED':' 需處理',' OWNED':' 擁有','View All (':'查看全部（','Posted on ':'發布於 ','Created on ':'建立於 ','Work Blocked by ':'工作受到 ',' Prerequisite Issue(s)':' 個前置任務阻擋',
    ' Artifacts':' 個成果',' Approvers Gate':' 個核准人關卡',' Events':' 個事件',' Upvotes':' 票贊成','Reviewed on ':'審查於 ','Signed on ':'簽核於 ','Signature Proof: ':'簽章證明：',
    'No artifacts authored yet by ':'尚未由此使用者建立成果：','No recent audit events attributed to ':'尚無歸屬於此使用者的近期稽核事件：','The User Profile is rooted within Enterprise ':'使用者檔案隸屬企業 '
}

for path in UI_FILES:
    text = path.read_text(encoding='utf-8')
    text = literal_replace(text, mapping)
    for src, dst in sorted(fragment_mapping.items(), key=lambda item: len(item[0]), reverse=True):
        text = text.replace(src, dst)
    path.write_text(text, encoding='utf-8')

# Visible labels carried by enums/model display names.
model_path = ROOT / 'app/src/main/java/com/example/data/model/GovernanceModels.kt'
model = model_path.read_text(encoding='utf-8')
model_fragments = {
    'GENERAL("General"':'GENERAL("一般"','RFC_PROPOSALS("RFC Proposals"':'RFC_PROPOSALS("RFC 提案"','ANNOUNCEMENTS("Announcements"':'ANNOUNCEMENTS("公告"',
    'IDEAS_AND_BRAINSTORM("Ideas & Brainstorm"':'IDEAS_AND_BRAINSTORM("想法與腦力激盪"','Q_AND_A("Q & A"':'Q_AND_A("問答"','GOVERNANCE_DEBATE("Governance & Policy"':'GOVERNANCE_DEBATE("治理與政策"'
}
for src, dst in model_fragments.items():
    model = model.replace(src, dst)
model_path.write_text(model, encoding='utf-8')

# Audit fixed UI English after second pass. Dynamic values and technical acronyms are allowed.
patterns = [
    re.compile(r'Text\(\s*(?:text\s*=\s*)?"([^"\\]*(?:\\.[^"\\]*)*)"'),
    re.compile(r'contentDescription\s*=\s*"([^"\\]*(?:\\.[^"\\]*)*)"')
]
residual = []
for path in UI_FILES:
    source = path.read_text(encoding='utf-8')
    for pattern in patterns:
        for match in pattern.finditer(source):
            value = match.group(1)
            if not re.search(r'[A-Za-z]{3,}', value):
                continue
            if value.startswith(('http','SIG_','RBAC','ABAC','RFC','SSO','OIDC','JSON','FIDO2')):
                continue
            residual.append(f'{path.name}: {value}')

report = ROOT / '.github/zh-tw-residual-audit.txt'
report.write_text('\n'.join(sorted(set(residual))) + f'\nResidual count: {len(set(residual))}\n', encoding='utf-8')
print(report.read_text(encoding='utf-8'))
