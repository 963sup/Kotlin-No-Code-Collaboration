from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def save(rel: str, text: str) -> None:
    (ROOT / rel).write_text(text, encoding="utf-8")


def insert_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text
    imports = list(re.finditer(r"^import .+$", text, flags=re.MULTILINE))
    if not imports:
        raise RuntimeError(f"No import block for {import_line}")
    at = imports[-1].end()
    return text[:at] + "\n" + import_line + text[at:]


def balanced_block(text: str, open_brace: int) -> tuple[int, int]:
    depth = 0
    in_string = False
    escaped = False
    for i in range(open_brace, len(text)):
        char = text[i]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == '"':
            in_string = True
        elif char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return open_brace, i + 1
    raise RuntimeError("Unclosed Kotlin block")


# Explicit migration lambda avoids overload ambiguity.
migration_path = "app/src/main/java/com/example/data/local/AppMigrations.kt"
migration = read(migration_path)
migration = migration.replace(
    "MIGRATION_4_5_STATEMENTS.forEach(database::execSQL)",
    "MIGRATION_4_5_STATEMENTS.forEach { statement -> database.execSQL(statement) }"
)
save(migration_path, migration)

# Nullable assignee labels must not invoke String.ifBlank directly.
my_work_path = "app/src/main/java/com/example/ui/screens/MobileMyWorkScreen.kt"
my_work = read(my_work_path)
my_work = my_work.replace(
    '${issue.assigneeDisplayName.ifBlank { "未標示指派者" }}',
    '${issue.assigneeDisplayName?.takeIf { it.isNotBlank() } ?: "未標示指派者"}'
)
save(my_work_path, my_work)

# Give the selected personal-center body the remaining height below the mode chips.
personal_path = "app/src/main/java/com/example/ui/screens/PersonalCenterSwitchScreen.kt"
personal = read(personal_path)
personal = insert_import(personal, "import androidx.compose.foundation.layout.Box")
personal = insert_import(personal, "import androidx.compose.foundation.layout.weight")
old_body = '''        if (mode == "SOCIAL") {\n            SocialProfileScreen(\n                profileUser = profileUser,\n                activeUser = activeUser,\n                users = users,\n                repositories = repositories,\n                issues = issues,\n                artifacts = artifacts,\n                reviews = reviews,\n                approvals = approvals,\n                auditLogs = auditLogs,\n                userFollows = userFollows,\n                savedTargets = savedTargets,\n                syncStatus = syncStatus,\n                onToggleFollow = onToggleFollow,\n                onOpenTarget = onOpenTarget,\n                onSyncNow = onSyncNow\n            )\n        } else {\n            governanceContent()\n        }'''
new_body = '''        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {\n            if (mode == "SOCIAL") {\n                SocialProfileScreen(\n                    profileUser = profileUser,\n                    activeUser = activeUser,\n                    users = users,\n                    repositories = repositories,\n                    issues = issues,\n                    artifacts = artifacts,\n                    reviews = reviews,\n                    approvals = approvals,\n                    auditLogs = auditLogs,\n                    userFollows = userFollows,\n                    savedTargets = savedTargets,\n                    syncStatus = syncStatus,\n                    onToggleFollow = onToggleFollow,\n                    onOpenTarget = onOpenTarget,\n                    onSyncNow = onSyncNow\n                )\n            } else {\n                governanceContent()\n            }\n        }'''
if old_body in personal:
    personal = personal.replace(old_body, new_body, 1)
save(personal_path, personal)

# Every exhaustive RepoDetailTab when must include WBS. The first implementation
# patches known formats; this pass inspects actual balanced when blocks.
repo_path = "app/src/main/java/com/example/ui/screens/RepoDetailScreen.kt"
repo = read(repo_path)
search_at = 0
while True:
    match = re.search(r"when\s*\((?:tab|selectedTab)\)\s*\{", repo[search_at:])
    if not match:
        break
    start = search_at + match.start()
    brace = repo.find("{", start)
    block_start, block_end = balanced_block(repo, brace)
    block = repo[block_start:block_end]
    if "RepoDetailTab.OVERVIEW" in block and "RepoDetailTab.WBS" not in block:
        overview_line = re.search(r"(?m)^(\s*)RepoDetailTab\.OVERVIEW\s*->[^\n]+$", block)
        if not overview_line:
            raise RuntimeError("Could not patch exhaustive RepoDetailTab block")
        indent = overview_line.group(1)
        if "Icons.Default" in block:
            insertion = f"\n{indent}RepoDetailTab.WBS -> Icons.Default.List"
        elif "RepoOverviewSection" in block:
            insertion = (
                f"\n{indent}RepoDetailTab.WBS -> RepositoryWbsSection(\n"
                f"{indent}    issues = issues,\n"
                f"{indent}    onOpenIssue = onOpenIssue,\n"
                f"{indent}    onUpdatePlan = onUpdateIssuePlan\n"
                f"{indent})"
            )
        elif ".dp" in block:
            insertion = f"\n{indent}RepoDetailTab.WBS -> 0.dp"
        else:
            raise RuntimeError("Unknown RepoDetailTab when context")
        local_at = overview_line.end()
        block = block[:local_at] + insertion + block[local_at:]
        repo = repo[:block_start] + block + repo[block_end:]
        search_at = block_start + len(block)
    else:
        search_at = block_end
save(repo_path, repo)

# MainActivity uses explicit screen imports in this project; add new screens.
main_path = "app/src/main/java/com/example/MainActivity.kt"
main = read(main_path)
for import_line in [
    "import com.example.ui.screens.CollaborationEmptyStateCard",
    "import com.example.ui.screens.MobileMyWorkScreen",
    "import com.example.ui.screens.PersonalCenterSwitchScreen",
    "import com.example.ui.screens.UnifiedExploreScreen",
]:
    main = insert_import(main, import_line)

# Explore spans entity types globally within the active authorized scope. Feeding
# it scoped lists reuses the existing permission projection rather than creating
# a parallel, weaker authorization system.
explore_start = main.find("UnifiedExploreScreen(")
if explore_start < 0:
    raise RuntimeError("UnifiedExploreScreen integration missing")
explore_end = main.find("\n                            )", explore_start)
if explore_end < 0:
    raise RuntimeError("UnifiedExploreScreen call end missing")
explore_call = main[explore_start:explore_end]
replacements = {
    r"repositories\s*=\s*\w+": "repositories = scopedRepositories",
    r"artifacts\s*=\s*\w+": "artifacts = scopedArtifacts",
    r"issues\s*=\s*\w+": "issues = scopedIssues",
    r"discussions\s*=\s*\w+": "discussions = scopedDiscussions",
    r"organizations\s*=\s*\w+": "organizations = scopedOrganizations",
    r"teams\s*=\s*\w+": "teams = scopedTeams",
    r"users\s*=\s*\w+": "users = scopedUsers",
    r"auditLogs\s*=\s*\w+": "auditLogs = scopedAudits",
}
for pattern, replacement in replacements.items():
    explore_call = re.sub(pattern, replacement, explore_call, count=1)
main = main[:explore_start] + explore_call + main[explore_end:]
save(main_path, main)

print("mobile collaboration v2 repair pass applied")
