from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def save(rel: str, text: str) -> None:
    (ROOT / rel).write_text(text, encoding="utf-8")


def insert_import(text: str, line: str) -> str:
    if line in text:
        return text
    imports = list(re.finditer(r"^import .+$", text, flags=re.MULTILINE))
    if not imports:
        raise RuntimeError(f"No import block for {line}")
    at = imports[-1].end()
    return text[:at] + "\n" + line + text[at:]


def find_branch(text: str, label: str):
    marker = f"MainNavigationTab.{label} ->"
    navigation_root = text.find("when (currentTab)")
    if navigation_root < 0:
        raise RuntimeError("Main navigation when(currentTab) missing")
    start = text.find(marker, navigation_root)
    if start < 0:
        raise RuntimeError(f"Missing {marker} in currentTab navigation")
    cursor = start + len(marker)
    while cursor < len(text) and text[cursor].isspace():
        cursor += 1
    if cursor >= len(text) or text[cursor] != "{":
        next_match = re.search(r"\n\s*MainNavigationTab\.[A-Z_]+\s*->", text[cursor:])
        if not next_match:
            raise RuntimeError(f"Cannot bound {label}")
        end = cursor + next_match.start()
        return start, end, text[cursor:end].strip()
    depth = 0
    in_string = False
    escaped = False
    for i in range(cursor, len(text)):
        c = text[i]
        if in_string:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif c == '"':
                in_string = False
            continue
        if c == '"':
            in_string = True
        elif c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return start, i + 1, text[cursor + 1:i]
    raise RuntimeError(f"Unclosed {label} branch")


# Scope-aware Home: existing MainActivity already supplies scope-filtered data.
home_path = "app/src/main/java/com/example/ui/screens/HomeScreen.kt"
home = read(home_path)
home = insert_import(home, "import com.example.ui.components.WorkspaceScopeKind")
home = insert_import(home, "import com.example.ui.model.ScopeOperationalProjection")
if "scopeKind: WorkspaceScopeKind?" not in home:
    home = home.replace(
        "fun HomeScreen(\n    activeUser: User?,",
        "fun HomeScreen(\n    scopeKind: WorkspaceScopeKind? = null,\n    scopeName: String? = null,\n    activeUser: User?,",
        1,
    )
if "val scopeOperationalSummary =" not in home:
    marker = "    var selectedWorkFilter by remember { mutableStateOf(HomeWorkFilter.ASSIGNED_ISSUES) }"
    if marker not in home:
        raise RuntimeError("Home selectedWorkFilter marker missing")
    summary = marker + '''\n    val scopeOperationalSummary = remember(\n        scopeKind, scopeName, repositories, allIssues, allArtifacts, allReviews, allApprovals, notifications\n    ) {\n        ScopeOperationalProjection.build(\n            scopeKind = scopeKind,\n            scopeName = scopeName,\n            repositories = repositories,\n            issues = allIssues,\n            artifacts = allArtifacts,\n            reviews = allReviews,\n            approvals = allApprovals,\n            notifications = notifications\n        )\n    }'''
    home = home.replace(marker, summary, 1)
    lazy = home.find("    LazyColumn(")
    if lazy < 0:
        raise RuntimeError("Home LazyColumn missing")
    brace = home.find("{", lazy)
    home = home[:brace + 1] + "\n        item { ScopeOperationalSummaryCard(scopeOperationalSummary) }" + home[brace + 1:]
save(home_path, home)


# Repository WBS tab over the existing RepoIssue tree.
repo_path = "app/src/main/java/com/example/ui/screens/RepoDetailScreen.kt"
repo = read(repo_path)
if 'WBS("WBS", Icons.Default.AccountTree)' not in repo:
    repo = repo.replace(
        '    OVERVIEW("總覽", Icons.Default.Dashboard),\n    ISSUES',
        '    OVERVIEW("總覽", Icons.Default.Dashboard),\n    WBS("WBS", Icons.Default.AccountTree),\n    ISSUES',
        1,
    )
if "onUpdateIssuePlan:" not in repo:
    marker = "    onUpdateIssueStatus:(issueId: String, newStatus:IssueStatus) -> Unit = { _, _ -> },"
    if marker not in repo:
        marker = "    onUpdateIssueStatus: (issueId: String, newStatus: IssueStatus) -> Unit = { _, _ -> },"
    if marker not in repo:
        raise RuntimeError("RepoDetail update status callback marker missing")
    repo = repo.replace(
        marker,
        marker + "\n    onUpdateIssuePlan: (String, Int, Long?, Long?, Double, Int) -> Unit = { _, _, _, _, _, _ -> },",
        1,
    )
# Add WBS to tab count when.
repo = repo.replace(
    "RepoWorkspaceTab.OVERVIEW -> null\n                            RepoWorkspaceTab.ISSUES ->",
    "RepoWorkspaceTab.OVERVIEW -> null\n                            RepoWorkspaceTab.WBS -> issues.size\n                            RepoWorkspaceTab.ISSUES ->",
)
# Add WBS content branch once.
if "RepoWorkspaceTab.WBS -> RepositoryWbsSection" not in repo:
    marker = "                    RepoWorkspaceTab.ISSUES -> RepoIssuesSection("
    if marker not in repo:
        raise RuntimeError("RepoDetail ISSUES content marker missing")
    branch = '''                    RepoWorkspaceTab.WBS -> RepositoryWbsSection(\n                        issues = issues,\n                        onUpdatePlan = onUpdateIssuePlan\n                    )\n\n'''
    repo = repo.replace(marker, branch + marker, 1)
save(repo_path, repo)


# Main application wiring.
main_path = "app/src/main/java/com/example/MainActivity.kt"
main = read(main_path)
for line in [
    "import com.example.navigation.CollaborationTarget",
    "import com.example.ui.screens.UnifiedExploreScreen",
    "import com.example.ui.screens.PersonalCenterSwitchScreen",
    "import com.example.ui.viewmodel.CollaborationExperienceViewModel",
]:
    main = insert_import(main, line)

if "private val experienceViewModel:" not in main:
    main = main.replace(
        "    private val viewModel: GovernanceViewModel by viewModels()",
        "    private val viewModel: GovernanceViewModel by viewModels()\n    private val experienceViewModel: CollaborationExperienceViewModel by viewModels()",
        1,
    )
main = main.replace(
    "GovernanceApp(viewModel = viewModel)",
    "GovernanceApp(viewModel = viewModel, experienceViewModel = experienceViewModel)",
    1,
)
if "fun GovernanceApp(viewModel: GovernanceViewModel, experienceViewModel:" not in main:
    main = main.replace(
        "fun GovernanceApp(viewModel: GovernanceViewModel) {",
        "fun GovernanceApp(viewModel: GovernanceViewModel, experienceViewModel: CollaborationExperienceViewModel) {",
        1,
    )

if "val savedTargets by experienceViewModel.savedTargets" not in main:
    marker = "    val userNotifications by viewModel.userNotifications.collectAsState()"
    if marker not in main:
        raise RuntimeError("Main notification state marker missing")
    main = main.replace(
        marker,
        marker + "\n    val savedTargets by experienceViewModel.savedTargets.collectAsState()"
        + "\n    val userFollows by experienceViewModel.userFollows.collectAsState()"
        + "\n    val syncStatus by experienceViewModel.syncStatus.collectAsState()",
        1,
    )

# Home scope parameters.
home_call = main.find("HomeScreen(")
if home_call < 0:
    raise RuntimeError("Main HomeScreen call missing")
call_window = main[home_call:home_call + 700]
if "scopeKind =" not in call_window:
    paren = main.find("(", home_call)
    main = main[:paren + 1] + "\n                                scopeKind = selectedWorkspaceScope?.kind,\n                                scopeName = selectedWorkspaceScope?.name," + main[paren + 1:]

# Repository plan update callback.
repo_call = main.find("RepoDetailScreen(")
if repo_call < 0:
    raise RuntimeError("Main RepoDetailScreen call missing")
repo_window = main[repo_call:repo_call + 9000]
if "onUpdateIssuePlan =" not in repo_window:
    marker = "                        onUpdateIssueStatus = { issueId, newStatus ->\n                            viewModel.updateIssueStatus(issueId, newStatus)\n                        },"
    if marker not in main:
        raise RuntimeError("Main issue status callback marker missing")
    main = main.replace(
        marker,
        marker + "\n                        onUpdateIssuePlan = { id, order, start, end, weight, progress ->\n                            viewModel.updateIssuePlan(id, order, start, end, weight, progress)\n                        },",
        1,
    )

# Unified Explore replaces repository-only Explore, but only receives already-scoped records.
explore_start, explore_end, _ = find_branch(main, "EXPLORE")
explore = '''MainNavigationTab.EXPLORE -> {\n                            UnifiedExploreScreen(\n                                activeUser = activeUser,\n                                repositories = scopedRepositories,\n                                artifacts = scopedArtifacts,\n                                issues = scopedIssues,\n                                discussions = scopedDiscussions,\n                                organizations = scopedOrganizations,\n                                teams = scopedTeams,\n                                users = scopedUsers,\n                                savedTargets = savedTargets,\n                                onOpenTarget = { target ->\n                                    when (target) {\n                                        is CollaborationTarget.Repository -> repositories.firstOrNull { it.id == target.repositoryId }?.let(viewModel::selectRepository)\n                                        is CollaborationTarget.Artifact -> {\n                                            val repo = repositories.firstOrNull { it.id == target.repositoryId }\n                                            val artifact = allArtifacts.firstOrNull { it.id == target.artifactId && it.repoId == target.repositoryId }\n                                            if (repo != null && artifact != null) {\n                                                viewModel.selectRepository(repo)\n                                                viewModel.selectArtifact(artifact)\n                                            }\n                                        }\n                                        is CollaborationTarget.Issue -> repositories.firstOrNull { it.id == target.repositoryId }?.let(viewModel::selectRepository)\n                                        is CollaborationTarget.Discussion -> repositories.firstOrNull { it.id == target.repositoryId }?.let(viewModel::selectRepository)\n                                        is CollaborationTarget.Organization -> {\n                                            organizations.firstOrNull { it.id == target.organizationId }?.let { org ->\n                                                selectedWorkspaceScope = WorkspaceScopeSelection(WorkspaceScopeKind.ORGANIZATION, org.id, org.name, org.description)\n                                                currentTab = MainNavigationTab.HOME\n                                            }\n                                        }\n                                        is CollaborationTarget.Team -> {\n                                            teams.firstOrNull { it.id == target.teamId }?.let { team ->\n                                                selectedWorkspaceScope = WorkspaceScopeSelection(WorkspaceScopeKind.TEAM, team.id, team.name, team.description)\n                                                currentTab = MainNavigationTab.HOME\n                                            }\n                                        }\n                                        is CollaborationTarget.UserProfile -> {\n                                            users.firstOrNull { it.id == target.userId }?.let { profile ->\n                                                viewModel.selectProfileUser(profile)\n                                                meSubTab = MeSubTab.PROFILE\n                                                currentTab = MainNavigationTab.ME\n                                            }\n                                        }\n                                    }\n                                },\n                                onToggleSaved = { target -> activeUser?.let { experienceViewModel.toggleSaved(it.id, target) } }\n                            )\n                        }'''
main = main[:explore_start] + explore + main[explore_end:]

# Social-first personal center wraps the existing governance screen without deleting it.
me_start, me_end, me_inner = find_branch(main, "ME")
me = '''MainNavigationTab.ME -> {\n                            val profile = inspectedProfileUser ?: activeUser\n                            if (profile != null && activeUser != null) {\n                                PersonalCenterSwitchScreen(\n                                    profileUser = profile,\n                                    activeUser = activeUser,\n                                    auditLogs = scopedAuditLogs,\n                                    follows = userFollows,\n                                    savedTargets = savedTargets,\n                                    syncStatus = syncStatus,\n                                    onToggleFollow = { experienceViewModel.toggleFollow(activeUser.id, it) },\n                                    onSyncNow = experienceViewModel::syncNow,\n                                    governanceContent = {\n''' + me_inner.rstrip() + '''\n                                    }\n                                )\n                            }\n                        }'''
main = main[:me_start] + me + main[me_end:]
save(main_path, main)

print("current-main compatible mobile v2 integration applied")
