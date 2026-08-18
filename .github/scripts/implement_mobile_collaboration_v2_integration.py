from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]


def write(rel: str, content: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def save(rel: str, text: str) -> None:
    (ROOT / rel).write_text(text, encoding="utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected one marker, found {count}: {old[:120]!r}")
    return text.replace(old, new, 1)


def insert_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text
    imports = list(re.finditer(r"^import .+$", text, flags=re.MULTILINE))
    if not imports:
        raise RuntimeError(f"No import block found for {import_line}")
    at = imports[-1].end()
    return text[:at] + "\n" + import_line + text[at:]


def find_branch(text: str, label: str) -> tuple[int, int, str]:
    marker = f"MainNavigationTab.{label} ->"
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f"MainActivity branch not found: {marker}")
    content_start = start + len(marker)
    cursor = content_start
    while cursor < len(text) and text[cursor].isspace():
        cursor += 1
    if cursor < len(text) and text[cursor] == "{":
        depth = 0
        in_string = False
        escaped = False
        for index in range(cursor, len(text)):
            char = text[index]
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
                    return start, index + 1, text[cursor + 1:index]
        raise RuntimeError(f"Unclosed branch for {label}")
    next_branch = re.search(r"\n\s*MainNavigationTab\.[A-Z_]+\s*->", text[cursor:])
    if not next_branch:
        raise RuntimeError(f"Next branch not found after {label}")
    end = cursor + next_branch.start()
    return start, end, text[cursor:end].strip()


def choose_var(text: str, *candidates: str) -> str:
    for candidate in candidates:
        if re.search(rf"\b(?:val|var)\s+{re.escape(candidate)}\b", text):
            return candidate
    raise RuntimeError(f"None of the expected variables exist: {candidates}")


write(
    "app/src/main/java/com/example/ui/screens/ExperienceUiPrimitives.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CollaborationEmptyStateCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ExperienceSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}
''',
)

# Generated screens deliberately use uniquely named shared primitives so they do
# not depend on file-private helpers in older screens.
for rel in [
    "app/src/main/java/com/example/ui/screens/RepositoryWbsSection.kt",
    "app/src/main/java/com/example/ui/screens/UnifiedExploreScreen.kt",
    "app/src/main/java/com/example/ui/screens/SocialProfileScreen.kt",
]:
    text = read(rel)
    text = text.replace("EmptyStateCard(", "CollaborationEmptyStateCard(")
    if rel.endswith("SocialProfileScreen.kt"):
        text = text.replace("SectionTitle(", "ExperienceSectionTitle(")
    save(rel, text)

write(
    "app/src/main/java/com/example/ui/screens/MobileMyWorkScreen.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.ui.model.MyWorkProjection

@Composable
fun MobileMyWorkScreen(
    repositories: List<Repository>,
    issues: List<RepoIssue>,
    activeUserId: String?,
    activeTeamIds: Set<String>,
    onOpenIssue: (RepoIssue) -> Unit,
    onUpdateStatus: (RepoIssue, IssueStatus) -> Unit
) {
    var repositoryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    val repositoryById = remember(repositories) { repositories.associateBy { it.id } }
    val assigned = remember(issues, activeUserId, activeTeamIds) {
        MyWorkProjection.assignedIssues(issues, activeUserId, activeTeamIds)
    }
    val visible = remember(assigned, repositoryFilter) {
        repositoryFilter?.let { repoId -> assigned.filter { it.repoId == repoId } } ?: assigned
    }
    val grouped = remember(visible) { visible.groupBy { it.status } }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("我的工作", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "直接指派給你或所屬團隊的 Issue；Kanban 只是同一批工作資料的行動視圖。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("儲存庫篩選", style = MaterialTheme.typography.labelLarge)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = repositoryFilter == null,
                    onClick = { repositoryFilter = null },
                    label = { Text("全部 ${assigned.size}") },
                    modifier = Modifier.testTag("my_work_filter_all")
                )
            }
            items(repositories.filter { repo -> assigned.any { it.repoId == repo.id } }, key = { it.id }) { repo ->
                val count = assigned.count { it.repoId == repo.id }
                FilterChip(
                    selected = repositoryFilter == repo.id,
                    onClick = { repositoryFilter = repo.id },
                    label = { Text("${repo.displayName} $count") },
                    modifier = Modifier.testTag("my_work_filter_${repo.id}")
                )
            }
        }

        if (visible.isEmpty()) {
            CollaborationEmptyStateCard(
                title = "目前沒有指派工作",
                body = if (repositoryFilter == null) "指派給個人或團隊的 Issue 會集中出現在這裡。" else "此儲存庫沒有符合的指派工作。"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("my_work_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IssueStatus.entries.forEach { status ->
                    val statusIssues = grouped[status].orEmpty()
                    if (statusIssues.isNotEmpty()) {
                        item(key = "heading_${status.name}") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(status.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(statusIssues.size.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        items(statusIssues, key = { it.id }) { issue ->
                            MyWorkIssueCard(
                                issue = issue,
                                repositoryName = repositoryById[issue.repoId]?.displayName ?: "未知儲存庫",
                                onOpen = { onOpenIssue(issue) },
                                onUpdateStatus = { onUpdateStatus(issue, it) }
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun MyWorkIssueCard(
    issue: RepoIssue,
    repositoryName: String,
    onOpen: () -> Unit,
    onUpdateStatus: (IssueStatus) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).testTag("my_work_issue_${issue.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(repositoryName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("#${issue.issueNumber} ${issue.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更新狀態")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    IssueStatus.entries.filter { it != issue.status }.forEach { targetStatus ->
                        DropdownMenuItem(
                            text = { Text("移至 ${targetStatus.displayName}") },
                            onClick = {
                                menuExpanded = false
                                onUpdateStatus(targetStatus)
                            }
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { if (issue.status == IssueStatus.CLOSED) 1f else issue.progressPercent.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${issue.priority.displayName} · ${issue.assigneeDisplayName.ifBlank { "未標示指派者" }} · ${issue.progressPercent.coerceIn(0, 100)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
''',
)

write(
    "app/src/main/java/com/example/ui/screens/PersonalCenterSwitchScreen.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.NoCodeArtifact
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.SyncStatusSummary
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget

@Composable
fun PersonalCenterSwitchScreen(
    profileUser: User,
    activeUser: User,
    users: List<User>,
    repositories: List<Repository>,
    issues: List<RepoIssue>,
    artifacts: List<NoCodeArtifact>,
    reviews: List<ArtifactReview>,
    approvals: List<ArtifactApproval>,
    auditLogs: List<AuditLog>,
    userFollows: List<UserFollow>,
    savedTargets: List<SavedTarget>,
    syncStatus: SyncStatusSummary,
    onToggleFollow: (String) -> Unit,
    onOpenTarget: (CollaborationTarget) -> Unit,
    onSyncNow: () -> Unit,
    governanceContent: @Composable () -> Unit
) {
    var mode by rememberSaveable(profileUser.id) { mutableStateOf("SOCIAL") }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = mode == "SOCIAL",
                onClick = { mode = "SOCIAL" },
                label = { Text("成就與動態") },
                modifier = Modifier.testTag("personal_center_social")
            )
            FilterChip(
                selected = mode == "GOVERNANCE",
                onClick = { mode = "GOVERNANCE" },
                label = { Text("身份與治理") },
                modifier = Modifier.testTag("personal_center_governance")
            )
        }
        if (mode == "SOCIAL") {
            SocialProfileScreen(
                profileUser = profileUser,
                activeUser = activeUser,
                users = users,
                repositories = repositories,
                issues = issues,
                artifacts = artifacts,
                reviews = reviews,
                approvals = approvals,
                auditLogs = auditLogs,
                userFollows = userFollows,
                savedTargets = savedTargets,
                syncStatus = syncStatus,
                onToggleFollow = onToggleFollow,
                onOpenTarget = onOpenTarget,
                onSyncNow = onSyncNow
            )
        } else {
            governanceContent()
        }
    }
}
''',
)

# Ensure new Room column annotations compile.
model_path = "app/src/main/java/com/example/data/model/GovernanceModels.kt"
model_text = read(model_path)
model_text = insert_import(model_text, "import androidx.room.ColumnInfo")
save(model_path, model_text)

# BuildConfig endpoint: disabled by default, configurable only at build time, and
# runtime policy still rejects non-HTTPS or invalid endpoints.
build_path = "app/build.gradle.kts"
build_text = read(build_path)
if 'buildConfigField("String", "SYNC_BASE_URL"' not in build_text:
    marker = "defaultConfig {"
    if marker not in build_text:
        raise RuntimeError("app/build.gradle.kts: defaultConfig marker missing")
    build_text = build_text.replace(
        marker,
        marker + '''\n    val syncBaseUrl = (System.getenv("SYNC_BASE_URL") ?: "https://sync.invalid/")\n      .replace("\\\\", "\\\\\\\\")\n      .replace("\\\"", "\\\\\\\"")\n    buildConfigField("String", "SYNC_BASE_URL", "\\\"$syncBaseUrl\\\"")''',
        1
    )
save(build_path, build_text)

# Scope-aware Home is a projection of existing scoped data; no dashboard tables.
home_path = "app/src/main/java/com/example/ui/screens/HomeScreen.kt"
home = read(home_path)
home = insert_import(home, "import com.example.ui.components.WorkspaceScopeKind")
home = insert_import(home, "import com.example.ui.model.ScopeOperationalProjection")
if "scopeKind: WorkspaceScopeKind?" not in home:
    home = replace_once(
        home,
        "fun HomeScreen(\n",
        "fun HomeScreen(\n    scopeKind: WorkspaceScopeKind? = null,\n",
        "HomeScreen signature"
    )
if "val scopeOperationalSummary =" not in home:
    marker = "    val currentUser = activeUser ?: return"
    summary = '''    val currentUser = activeUser ?: return\n    val effectiveScopeName = when (scopeKind) {\n        WorkspaceScopeKind.ENTERPRISE, null -> enterprise?.displayName\n        WorkspaceScopeKind.ORGANIZATION -> organizations.singleOrNull()?.displayName\n        WorkspaceScopeKind.TEAM -> teams.singleOrNull()?.name\n        WorkspaceScopeKind.USER -> users.singleOrNull()?.displayName ?: currentUser.displayName\n    }\n    val scopeOperationalSummary = remember(\n        scopeKind, effectiveScopeName, currentUser.id, repositories, issues, artifacts,\n        reviews, approvals, notifications, organizations, teams, users\n    ) {\n        ScopeOperationalProjection.build(\n            scopeKind = scopeKind,\n            scopeName = effectiveScopeName,\n            activeUserId = currentUser.id,\n            repositories = repositories,\n            issues = issues,\n            artifacts = artifacts,\n            reviews = reviews,\n            approvals = approvals,\n            notifications = notifications,\n            organizations = organizations,\n            teams = teams,\n            users = users\n        )\n    }'''
    home = replace_once(home, marker, summary, "HomeScreen summary insertion")
    operational_marker = "        item {\n            OperationalSummaryRow("
    if operational_marker in home:
        home = home.replace(
            operational_marker,
            "        item { ScopeOperationalSummaryCard(scopeOperationalSummary) }\n\n" + operational_marker,
            1
        )
    else:
        lazy_marker = "    LazyColumn("
        index = home.find(lazy_marker)
        if index < 0:
            raise RuntimeError("HomeScreen LazyColumn marker missing")
        brace = home.find("{", index)
        home = home[:brace + 1] + "\n        item { ScopeOperationalSummaryCard(scopeOperationalSummary) }" + home[brace + 1:]
save(home_path, home)

# Add a dedicated Repository WBS tab over RepoIssue hierarchy.
repo_path = "app/src/main/java/com/example/ui/screens/RepoDetailScreen.kt"
repo = read(repo_path)
if "WBS(\"WBS\"" not in repo:
    repo = repo.replace(
        'OVERVIEW("總覽", "repo_secondary_tab_overview"),',
        'OVERVIEW("總覽", "repo_secondary_tab_overview"),\n    WBS("WBS", "repo_secondary_tab_wbs"),',
        1
    )
if "onUpdateIssuePlan:" not in repo:
    repo = repo.replace(
        "    onIssueStatusChange: (RepoIssue, IssueStatus) -> Unit,",
        "    onIssueStatusChange: (RepoIssue, IssueStatus) -> Unit,\n    onUpdateIssuePlan: (String, Int, Long?, Long?, Double, Int) -> Unit,",
        1
    )
# Patch every exhaustive tab when where the neighbouring OVERVIEW branch identifies context.
repo = repo.replace(
    "RepoDetailTab.OVERVIEW -> Icons.Default.Home\n",
    "RepoDetailTab.OVERVIEW -> Icons.Default.Home\n                                RepoDetailTab.WBS -> Icons.Default.List\n",
)
repo = repo.replace(
    "RepoDetailTab.OVERVIEW -> Icons.Default.Info\n",
    "RepoDetailTab.OVERVIEW -> Icons.Default.Info\n                                RepoDetailTab.WBS -> Icons.Default.List\n",
)
if "RepoDetailTab.WBS -> RepositoryWbsSection" not in repo:
    overview_branch = "            RepoDetailTab.OVERVIEW -> RepoOverviewSection("
    branch_index = repo.find(overview_branch)
    if branch_index < 0:
        raise RuntimeError("RepoDetailScreen overview content branch missing")
    next_branch = repo.find("            RepoDetailTab.ISSUES ->", branch_index)
    if next_branch < 0:
        raise RuntimeError("RepoDetailScreen issues branch missing")
    wbs_branch = '''            RepoDetailTab.WBS -> RepositoryWbsSection(\n                issues = issues,\n                onOpenIssue = onOpenIssue,\n                onUpdatePlan = onUpdateIssuePlan\n            )\n'''
    repo = repo[:next_branch] + wbs_branch + repo[next_branch:]
# Horizontal padding branch must be exhaustive.
repo = repo.replace(
    "RepoDetailTab.DISCUSSIONS, RepoDetailTab.ARTIFACTS -> 0.dp",
    "RepoDetailTab.WBS, RepoDetailTab.DISCUSSIONS, RepoDetailTab.ARTIFACTS -> 0.dp"
)
save(repo_path, repo)

# Main shell integration.
main_path = "app/src/main/java/com/example/MainActivity.kt"
main = read(main_path)
if "CollaborationExperienceViewModel by viewModels" not in main:
    main = main.replace(
        "    private val viewModel: GovernanceViewModel by viewModels()",
        "    private val viewModel: GovernanceViewModel by viewModels()\n    private val experienceViewModel: com.example.ui.viewmodel.CollaborationExperienceViewModel by viewModels()",
        1
    )
main = main.replace("GovernanceApp(viewModel)", "GovernanceApp(viewModel, experienceViewModel)", 1)
if "experienceViewModel: com.example.ui.viewmodel.CollaborationExperienceViewModel" not in main:
    main = main.replace(
        "private fun GovernanceApp(viewModel: GovernanceViewModel)",
        "private fun GovernanceApp(\n    viewModel: GovernanceViewModel,\n    experienceViewModel: com.example.ui.viewmodel.CollaborationExperienceViewModel\n)",
        1
    )

if "val savedTargets by experienceViewModel.savedTargets" not in main:
    active_match = re.search(
        r"(?m)^(\s*)val\s+activeUser\s+by\s+viewModel\.activeUser\.collectAsStateWithLifecycle\([^\n]*\)\s*$",
        main
    )
    if not active_match:
        raise RuntimeError("MainActivity activeUser StateFlow collection not found")
    indent = active_match.group(1)
    insertion = (
        active_match.group(0) + "\n" +
        f"{indent}val savedTargets by experienceViewModel.savedTargets.collectAsStateWithLifecycle()\n" +
        f"{indent}val userFollows by experienceViewModel.userFollows.collectAsStateWithLifecycle()\n" +
        f"{indent}val syncStatus by experienceViewModel.syncStatus.collectAsStateWithLifecycle()"
    )
    main = main[:active_match.start()] + insertion + main[active_match.end():]

# Resolve current source variable names rather than duplicating state.
users_var = choose_var(main, "users", "allUsers")
repositories_var = choose_var(main, "repositories", "allRepositories")
artifacts_var = choose_var(main, "artifacts", "allArtifacts")
issues_var = choose_var(main, "issues", "allIssues")
discussions_var = choose_var(main, "discussions", "allDiscussions")
organizations_var = choose_var(main, "organizations", "allOrganizations")
teams_var = choose_var(main, "teams", "allTeams")
org_memberships_var = choose_var(main, "orgMemberships", "organizationMemberships")
team_memberships_var = choose_var(main, "teamMemberships")
access_rules_var = choose_var(main, "accessRules", "repoAccessRules")
audits_var = choose_var(main, "audits", "auditLogs")
reviews_var = choose_var(main, "reviews", "artifactReviews")
approvals_var = choose_var(main, "approvals", "artifactApprovals")
enterprise_var = choose_var(main, "enterprise")
profile_var = choose_var(main, "profileUser", "selectedProfileUser")

# Find the selected workspace kind from the actual switcher call/declaration.
scope_match = re.search(r"\b(?:var|val)\s+(\w*[Ss]cope\w*[Kk]ind\w*|selectedScopeKind)\b", main)
if scope_match:
    scope_kind_expr = scope_match.group(1)
else:
    kind_arg = re.search(r"(?:selectedKind|scopeKind)\s*=\s*([A-Za-z_][A-Za-z0-9_.]*)", main)
    if not kind_arg:
        raise RuntimeError("MainActivity selected WorkspaceScopeKind expression not found")
    scope_kind_expr = kind_arg.group(1)

# Pass scope kind to Home.
home_call = main.find("HomeScreen(")
if home_call < 0:
    raise RuntimeError("MainActivity HomeScreen call missing")
if "scopeKind =" not in main[home_call:home_call + 500]:
    paren = main.find("(", home_call)
    main = main[:paren + 1] + f"\n                            scopeKind = {scope_kind_expr}," + main[paren + 1:]

# Repository detail receives WBS plan mutations.
repo_call = main.find("RepoDetailScreen(")
if repo_call < 0:
    raise RuntimeError("MainActivity RepoDetailScreen call missing")
repo_slice = main[repo_call:repo_call + 5000]
if "onUpdateIssuePlan =" not in repo_slice:
    status_match = re.search(r"(?m)^(\s*)onIssueStatusChange\s*=.*,$", repo_slice)
    if not status_match:
        raise RuntimeError("MainActivity RepoDetailScreen status callback missing")
    absolute_end = repo_call + status_match.end()
    indent = status_match.group(1)
    main = main[:absolute_end] + f"\n{indent}onUpdateIssuePlan = viewModel::updateIssuePlan," + main[absolute_end:]

# Replace global Kanban with mobile vertical My Work.
kanban_start, kanban_end, _ = find_branch(main, "KANBAN")
kanban_branch = f'''MainNavigationTab.KANBAN -> {{\n                            MobileMyWorkScreen(\n                                repositories = scopedRepositories,\n                                issues = scopedIssues,\n                                activeUserId = activeUser?.id,\n                                activeTeamIds = {team_memberships_var}\n                                    .filter {{ it.userId == activeUser?.id }}\n                                    .map {{ it.teamId }}\n                                    .toSet(),\n                                onOpenIssue = {{ issue ->\n                                    openCollaborationTarget(\n                                        CollaborationTarget.Issue(issue.repoId, issue.id)\n                                    )\n                                }},\n                                onUpdateStatus = {{ issue, status ->\n                                    viewModel.updateIssueStatus(issue.id, status)\n                                }}\n                            )\n                        }}'''
main = main[:kanban_start] + kanban_branch + main[kanban_end:]

# Replace repository-only Explore with permission-filtered unified discovery.
explore_start, explore_end, _ = find_branch(main, "EXPLORE")
explore_branch = f'''MainNavigationTab.EXPLORE -> {{\n                            UnifiedExploreScreen(\n                                enterprise = {enterprise_var},\n                                activeUser = activeUser,\n                                repositories = {repositories_var},\n                                artifacts = {artifacts_var},\n                                issues = {issues_var},\n                                discussions = {discussions_var},\n                                organizations = {organizations_var},\n                                teams = {teams_var},\n                                users = {users_var},\n                                orgMemberships = {org_memberships_var},\n                                teamMemberships = {team_memberships_var},\n                                accessRules = {access_rules_var},\n                                auditLogs = {audits_var},\n                                savedTargets = savedTargets,\n                                onOpenTarget = {{ openCollaborationTarget(it) }},\n                                onToggleSaved = {{ target ->\n                                    activeUser?.let {{ user ->\n                                        experienceViewModel.toggleSaved(user.id, target)\n                                    }}\n                                }},\n                                onCreateRepository = viewModel::createRepository\n                            )\n                        }}'''
main = main[:explore_start] + explore_branch + main[explore_end:]

# Wrap existing identity/governance profile in a social-first personal center.
me_start, me_end, me_inner = find_branch(main, "ME")
me_branch = f'''MainNavigationTab.ME -> {{\n                            val resolvedProfileUser = {profile_var} ?: activeUser\n                            if (resolvedProfileUser == null || activeUser == null) {{\n                                CollaborationEmptyStateCard(\n                                    title = "找不到個人資料",\n                                    body = "請先選擇有效的企業用戶。"\n                                )\n                            }} else {{\n                                PersonalCenterSwitchScreen(\n                                    profileUser = resolvedProfileUser,\n                                    activeUser = activeUser,\n                                    users = scopedUsers,\n                                    repositories = scopedRepositories,\n                                    issues = scopedIssues,\n                                    artifacts = scopedArtifacts,\n                                    reviews = scopedReviews,\n                                    approvals = scopedApprovals,\n                                    auditLogs = scopedAudits,\n                                    userFollows = userFollows,\n                                    savedTargets = savedTargets,\n                                    syncStatus = syncStatus,\n                                    onToggleFollow = {{ followedUserId ->\n                                        experienceViewModel.toggleFollow(activeUser.id, followedUserId)\n                                    }},\n                                    onOpenTarget = {{ openCollaborationTarget(it) }},\n                                    onSyncNow = experienceViewModel::syncNow,\n                                    governanceContent = {{\n{me_inner.rstrip()}\n                                    }}\n                                )\n                            }}\n                        }}'''
main = main[:me_start] + me_branch + main[me_end:]
save(main_path, main)

print("mobile collaboration v2 screen integration applied")
