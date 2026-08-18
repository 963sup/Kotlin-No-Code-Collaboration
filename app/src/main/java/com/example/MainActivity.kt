package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoCodeArtifact
import com.example.data.model.Repository
import com.example.data.model.User
import com.example.ui.components.PersonaSwitcherDialog
import com.example.ui.components.RepositoryWorkBoardDialog
import com.example.ui.screens.ArtifactDetailScreen
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.KanbanBoardScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.MeSubTab
import com.example.ui.screens.OrgTeamScreen
import com.example.ui.screens.PolicySimulatorScreen
import com.example.ui.screens.RepoDetailScreen
import com.example.ui.screens.RepositoriesScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.ui.viewmodel.GovernanceViewModel

enum class MainNavigationTab {
    HOME,
    INBOX,
    KANBAN,
    REPOSITORIES,
    ME
}

class MainActivity : ComponentActivity() {
    private val viewModel: GovernanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GovernanceApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernanceApp(viewModel: GovernanceViewModel) {
    val enterprise by viewModel.enterprise.collectAsState()
    val enterprises by viewModel.enterprises.collectAsState()
    val organizations by viewModel.organizations.collectAsState()
    val users by viewModel.users.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val repositories by viewModel.repositories.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val allAccessRules by viewModel.allAccessRules.collectAsState()
    val allOrgMemberships by viewModel.allOrgMemberships.collectAsState()
    val allTeamMemberships by viewModel.allTeamMemberships.collectAsState()
    val allArtifacts by viewModel.allArtifacts.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val selectedRepo by viewModel.selectedRepo.collectAsState()
    val selectedArtifact by viewModel.selectedArtifact.collectAsState()
    val selectedArtifactReviews by viewModel.selectedArtifactReviews.collectAsState()
    val selectedArtifactApprovals by viewModel.selectedArtifactApprovals.collectAsState()
    val selectedRepoArtifacts by viewModel.selectedRepoArtifacts.collectAsState()
    val selectedRepoAccessRules by viewModel.selectedRepoAccessRules.collectAsState()
    val selectedRepoIssues by viewModel.selectedRepoIssues.collectAsState()
    val selectedRepoDependencies by viewModel.selectedRepoDependencies.collectAsState()
    val selectedRepoDiscussions by viewModel.selectedRepoDiscussions.collectAsState()
    val selectedIssueComments by viewModel.selectedIssueComments.collectAsState()
    val selectedDiscussionComments by viewModel.selectedDiscussionComments.collectAsState()
    val simulationResult by viewModel.simulationResult.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val userNotifications by viewModel.userNotifications.collectAsState()

    val allIssues by viewModel.allIssues.collectAsState()
    val allDiscussions by viewModel.allDiscussions.collectAsState()
    val allDependencies by viewModel.allDependencies.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val allApprovals by viewModel.allApprovals.collectAsState()

    val inspectedProfileUser by viewModel.inspectedProfileUser.collectAsState()
    val profileUserArtifacts by viewModel.profileUserArtifacts.collectAsState()
    val profileUserReviews by viewModel.profileUserReviews.collectAsState()
    val profileUserApprovals by viewModel.profileUserApprovals.collectAsState()
    val profileUserAuditLogs by viewModel.profileUserAuditLogs.collectAsState()
    val profileUserIssues by viewModel.profileUserIssues.collectAsState()
    val profileUserDiscussions by viewModel.profileUserDiscussions.collectAsState()

    var currentTab by remember { mutableStateOf(MainNavigationTab.HOME) }
    var meSubTab by remember { mutableStateOf(MeSubTab.PROFILE) }
    var showPersonaSwitcher by remember { mutableStateOf(false) }
    var showWorkBoard by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg.text)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SophisticatedBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LavenderPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = LavenderOnPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "存取治理",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = LavenderPrimary
                            )
                            Text(
                                text = (enterprise?.name ?: "企業").uppercase() + " • 無程式碼協作平台",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextMediumEmphasis
                            )
                        }
                    }
                },
                actions = {
                    // Repository-scoped work view: a projection of Issues, not a new Project owner.
                    if (selectedRepo != null && selectedArtifact == null) {
                        IconButton(
                            onClick = { showWorkBoard = true },
                            modifier = Modifier.testTag("topbar_repo_work_board_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "儲存庫工作看板與巢狀任務",
                                tint = LavenderPrimary
                            )
                        }
                    }

                    // Inbox Quick Access Button with Badge
                    IconButton(
                        onClick = { currentTab = MainNavigationTab.INBOX },
                        modifier = Modifier.testTag("topbar_inbox_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = LavenderPrimary,
                                        contentColor = LavenderOnPrimary
                                    ) {
                                        Text("$unreadNotificationCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "統一收件匣",
                                tint = if (currentTab == MainNavigationTab.INBOX) LavenderPrimary else TextHighEmphasis
                            )
                        }
                    }

                    // Persona Switcher Trigger Pill
                    ActivePersonaPill(
                        user = activeUser,
                        onClick = { showPersonaSwitcher = true }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SophisticatedSurfaceDark,
                    titleContentColor = TextHighEmphasis
                )
            )
        },
        bottomBar = {
            if (selectedRepo == null && selectedArtifact == null) {
                Surface(
                    color = SophisticatedSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder)
                ) {
                    NavigationBar(containerColor = SophisticatedSurfaceDark, tonalElevation = 0.dp) {
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.HOME,
                            onClick = { currentTab = MainNavigationTab.HOME },
                            icon = { Icon(Icons.Default.Home, contentDescription = "首頁") },
                            label = { Text("首頁", style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,
                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary
                            ), modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.INBOX,
                            onClick = { currentTab = MainNavigationTab.INBOX },
                            icon = {
                                BadgedBox(badge = {
                                    if (unreadNotificationCount > 0) Badge(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary) { Text("$unreadNotificationCount") }
                                }) { Icon(Icons.Default.Notifications, contentDescription = "收件匣") }
                            },
                            label = { Text("收件匣", style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,
                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary
                            ), modifier = Modifier.testTag("nav_tab_inbox")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.KANBAN,
                            onClick = { currentTab = MainNavigationTab.KANBAN },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "工作看板") },
                            label = { Text("看板", style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,
                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary
                            ), modifier = Modifier.testTag("nav_tab_kanban")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.REPOSITORIES,
                            onClick = { currentTab = MainNavigationTab.REPOSITORIES },
                            icon = { Icon(Icons.Default.Folder, contentDescription = "儲存庫") },
                            label = { Text("儲存庫", style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,
                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary
                            ), modifier = Modifier.testTag("nav_tab_repos")
                        )
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.ME,
                            onClick = {
                                if (inspectedProfileUser == null && activeUser != null) viewModel.selectProfileUser(activeUser)
                                currentTab = MainNavigationTab.ME
                            },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "我的") },
                            label = { Text("我的", style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,
                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary
                            ), modifier = Modifier.testTag("nav_tab_me")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedArtifact != null && selectedRepo != null -> {
                    ArtifactDetailScreen(
                        artifact = selectedArtifact!!,
                        repo = selectedRepo!!,
                        reviews = selectedArtifactReviews,
                        approvals = selectedArtifactApprovals,
                        activeUser = activeUser,
                        onBack = { viewModel.selectArtifact(null) },
                        onSubmitForReview = {
                            viewModel.submitForReview(selectedArtifact!!.id)
                        },
                        onSubmitReview = { decision, feedback ->
                            viewModel.submitReview(selectedArtifact!!.id, decision, feedback)
                        },
                        onSubmitApproverSignOff = {
                            viewModel.submitApproverSignOff(selectedArtifact!!.id)
                        },
                        onPublishAndLock = {
                            viewModel.publishAndLock(selectedArtifact!!.id)
                        },
                        onInspectPolicy = { action ->
                            if (activeUser != null) {
                                viewModel.runPolicySimulation(
                                    actor = activeUser!!,
                                    repo = selectedRepo!!,
                                    artifact = selectedArtifact,
                                    action = action
                                )
                            }
                        },
                        simulationResult = simulationResult,
                        onClearSimulation = { viewModel.clearSimulationResult() }
                    )
                }

                selectedRepo != null -> {
                    RepoDetailScreen(
                        repo = selectedRepo!!,
                        enterprise = enterprise,
                        artifacts = selectedRepoArtifacts,
                        issues = selectedRepoIssues,
                        dependencies = selectedRepoDependencies,
                        discussions = selectedRepoDiscussions,
                        issueComments = selectedIssueComments,
                        discussionComments = selectedDiscussionComments,
                        accessRules = selectedRepoAccessRules,
                        allUsers = users,
                        allTeams = teams,
                        allOrgMemberships = allOrgMemberships,
                        allTeamMemberships = allTeamMemberships,
                        allAuditLogs = auditLogs,
                        activeUser = activeUser,
                        onBack = {
                            showWorkBoard = false
                            viewModel.selectRepository(null)
                        },
                        onSelectArtifact = { art ->
                            showWorkBoard = false
                            viewModel.selectArtifact(art)
                        },
                        onCreateArtifact = { title, type, summary, content, callback ->
                            viewModel.createNoCodeArtifact(
                                repoId = selectedRepo!!.id,
                                title = title,
                                type = type,
                                summary = summary,
                                content = content,
                                onComplete = callback
                            )
                        },
                        onAddAccessRule = { granteeType, granteeId, granteeName, role ->
                            viewModel.addRepoAccessRule(selectedRepo!!.id, granteeType, granteeId, granteeName, role)
                        },
                        onRemoveAccessRule = { rule ->
                            viewModel.removeRepoAccessRule(rule)
                        },
                        onCreateIssue = { title, desc, priority, assigneeType, assigneeId, assigneeName, linkedArtifactId, linkedArtifactTitle, parentIssueId, labels, callback ->
                            viewModel.createIssue(
                                repoId = selectedRepo!!.id,
                                title = title,
                                description = desc,
                                priority = priority,
                                assigneeType = assigneeType,
                                assigneeId = assigneeId,
                                assigneeName = assigneeName,
                                linkedArtifactId = linkedArtifactId,
                                linkedArtifactTitle = linkedArtifactTitle,
                                parentIssueId = parentIssueId,
                                labels = labels,
                                onSuccess = callback
                            )
                        },
                        onLinkParentIssue = { issueId, parentIssueId, callback ->
                            viewModel.linkParentIssue(issueId, parentIssueId, callback)
                        },
                        onAddDependency = { repoId, blockedIssueId, blockingIssueId, callback ->
                            viewModel.addIssueDependency(repoId, blockedIssueId, blockingIssueId, callback)
                        },
                        onRemoveDependency = { dependencyId, callback ->
                            viewModel.removeIssueDependency(dependencyId, callback)
                        },
                        onAddIssueComment = { issueId, content, callback ->
                            viewModel.addIssueComment(issueId, content, callback)
                        },
                        onUpdateIssueStatus = { issueId, newStatus ->
                            viewModel.updateIssueStatus(issueId, newStatus)
                        },
                        onAssignIssue = { issueId, assigneeType, assigneeId, assigneeName ->
                            viewModel.assignIssue(issueId, assigneeType, assigneeId, assigneeName)
                        },
                        onLoadIssueComments = { issueId ->
                            viewModel.loadIssueComments(issueId)
                        },
                        onCreateDiscussion = { title, category, body, callback ->
                            viewModel.createDiscussion(
                                repoId = selectedRepo!!.id,
                                title = title,
                                category = category,
                                body = body,
                                onSuccess = callback
                            )
                        },
                        onAddDiscussionComment = { discussionId, content, callback ->
                            viewModel.addDiscussionComment(discussionId, content, callback)
                        },
                        onToggleLockDiscussion = { discussionId ->
                            viewModel.toggleLockDiscussion(discussionId)
                        },
                        onMarkAcceptedAnswer = { discussionId, commentId ->
                            viewModel.markAcceptedAnswer(discussionId, commentId)
                        },
                        onUpvoteDiscussion = { discussionId ->
                            viewModel.upvoteDiscussion(discussionId)
                        },
                        onUpvoteDiscussionComment = { commentId, discussionId ->
                            viewModel.upvoteDiscussionComment(commentId, discussionId)
                        },
                        onLoadDiscussionComments = { discussionId ->
                            viewModel.loadDiscussionComments(discussionId)
                        }
                    )
                }

                else -> {
                    when (currentTab) {
                        MainNavigationTab.HOME -> {
                            HomeScreen(
                                activeUser = activeUser,
                                enterprise = enterprise,
                                organizations = organizations,
                                teams = teams,
                                repositories = repositories,
                                allArtifacts = allArtifacts,
                                allIssues = allIssues,
                                allDiscussions = allDiscussions,
                                allDependencies = allDependencies,
                                allReviews = allReviews,
                                allApprovals = allApprovals,
                                allAccessRules = allAccessRules,
                                allOrgMemberships = allOrgMemberships,
                                allTeamMemberships = allTeamMemberships,
                                notifications = userNotifications,
                                auditLogs = auditLogs,
                                unreadNotificationCount = unreadNotificationCount,
                                onNavigateToRepository = { repo ->
                                    viewModel.selectRepository(repo)
                                },
                                onNavigateToArtifact = { repo, artifact ->
                                    viewModel.selectRepository(repo)
                                    viewModel.selectArtifact(artifact)
                                },
                                onNavigateToInbox = {
                                    currentTab = MainNavigationTab.INBOX
                                },
                                onNavigateToRepositoriesCatalog = {
                                    currentTab = MainNavigationTab.REPOSITORIES
                                },
                                onNavigateToMe = {
                                    currentTab = MainNavigationTab.ME
                                },
                                onSwitchPersonaClick = {
                                    showPersonaSwitcher = true
                                }
                            )
                        }

                        MainNavigationTab.KANBAN -> {
                            KanbanBoardScreen(
                                repositories = repositories,
                                allIssues = allIssues,
                                onUpdateIssueStatus = { issueId, status -> viewModel.updateIssueStatus(issueId, status) },
                                onOpenRepository = { repo -> viewModel.selectRepository(repo) }
                            )
                        }

                        MainNavigationTab.REPOSITORIES -> {
                            RepositoriesScreen(
                                repositories = repositories,
                                organizations = organizations,
                                users = users,
                                teams = teams,
                                allAccessRules = allAccessRules,
                                allOrgMemberships = allOrgMemberships,
                                allTeamMemberships = allTeamMemberships,
                                allArtifacts = allArtifacts,
                                activeUser = activeUser,
                                onSelectRepo = { repo -> viewModel.selectRepository(repo) },
                                onCreateRepo = { name, displayName, ownerType, ownerId, ownerDisplayName, desc, category, callback ->
                                    viewModel.createRepository(
                                        name = name,
                                        displayName = displayName,
                                        ownerType = ownerType,
                                        ownerId = ownerId,
                                        ownerDisplayName = ownerDisplayName,
                                        description = desc,
                                        category = category,
                                        onComplete = callback
                                    )
                                }
                            )
                        }

                        MainNavigationTab.INBOX -> {
                            InboxScreen(
                                viewModel = viewModel,
                                onNavigateToRepository = { repoId ->
                                    val repo = repositories.firstOrNull { it.id == repoId }
                                    if (repo != null) {
                                        viewModel.selectRepository(repo)
                                    }
                                },
                                onNavigateToArtifact = { repoId, artifactId ->
                                    val repo = repositories.firstOrNull { it.id == repoId }
                                    val art = allArtifacts.firstOrNull { it.id == artifactId }
                                    if (repo != null && art != null) {
                                        viewModel.selectRepository(repo)
                                        viewModel.selectArtifact(art)
                                    }
                                },
                                onNavigateToOrg = { orgId ->
                                    meSubTab = MeSubTab.ORGS_AND_TEAMS
                                    currentTab = MainNavigationTab.ME
                                },
                                onNavigateToUserProfile = { user ->
                                    viewModel.selectProfileUser(user)
                                    meSubTab = MeSubTab.PROFILE
                                    currentTab = MainNavigationTab.ME
                                }
                            )
                        }

                        MainNavigationTab.ME -> {
                            MeScreen(
                                currentSubTab = meSubTab,
                                onSubTabChange = { meSubTab = it },
                                activeUser = activeUser,
                                inspectedProfileUser = inspectedProfileUser,
                                allUsers = users,
                                enterprise = enterprise,
                                enterprises = enterprises,
                                organizations = organizations,
                                teams = teams,
                                repositories = repositories,
                                allAccessRules = allAccessRules,
                                allOrgMemberships = allOrgMemberships,
                                allTeamMemberships = allTeamMemberships,
                                allArtifacts = allArtifacts,
                                profileUserArtifacts = profileUserArtifacts,
                                profileUserReviews = profileUserReviews,
                                profileUserApprovals = profileUserApprovals,
                                profileUserAuditLogs = profileUserAuditLogs,
                                profileUserIssues = profileUserIssues,
                                profileUserDiscussions = profileUserDiscussions,
                                auditLogs = auditLogs,
                                simulationResult = simulationResult,
                                onSelectUserToInspect = { u ->
                                    viewModel.selectProfileUser(u)
                                    meSubTab = MeSubTab.PROFILE
                                },
                                onSwitchActivePersona = { u ->
                                    viewModel.switchActiveUser(u)
                                },
                                onSelectRepository = { repo ->
                                    viewModel.selectRepository(repo)
                                },
                                onSelectArtifact = { art ->
                                    val repo = repositories.firstOrNull { it.id == art.repoId }
                                    if (repo != null) {
                                        viewModel.selectRepository(repo)
                                        viewModel.selectArtifact(art)
                                    }
                                },
                                onUpdateProfile = { target, displayName, title, bio, loc, pronouns, avatarColor, notifPrefs ->
                                    viewModel.updateUserProfile(
                                        targetUser = target,
                                        displayName = displayName,
                                        title = title,
                                        bio = bio,
                                        location = loc,
                                        pronouns = pronouns,
                                        avatarColorHex = avatarColor,
                                        notificationPreferences = notifPrefs
                                    )
                                },
                                onCreateEnterprise = { name, slug, desc, dualApp, allowUserRepos, revGate, segDuties ->
                                    viewModel.createEnterprise(name, slug, desc, dualApp, allowUserRepos, revGate, segDuties)
                                },
                                onUpdateEnterprisePolicies = { updated ->
                                    viewModel.updateEnterpriseSecurityPolicies(updated)
                                },
                                onCreateEnterpriseUser = { entId, username, displayName, email, title, isAdmin, avatarColor ->
                                    viewModel.createEnterpriseUser(entId, username, displayName, email, title, isAdmin, avatarColor)
                                },
                                onCreateOrganization = { entId, name, slug, desc, colorHex, defaultRole, ownerId ->
                                    viewModel.createOrganization(entId, name, slug, desc, colorHex, defaultRole, ownerId)
                                },
                                onUpdateOrganization = { updated ->
                                    viewModel.updateOrganization(updated)
                                },
                                onAddOrgMember = { orgId, userId, role ->
                                    viewModel.addOrgMember(orgId, userId, role)
                                },
                                onRemoveOrgMember = { orgId, userId ->
                                    viewModel.removeOrgMember(orgId, userId)
                                },
                                onCreateTeam = { orgId, name, slug, desc, parentTeamId ->
                                    viewModel.createTeam(orgId, name, slug, desc, parentTeamId)
                                },
                                onAddTeamMember = { teamId, userId, role ->
                                    viewModel.addTeamMember(teamId, userId, role)
                                },
                                onRemoveTeamMember = { teamId, userId ->
                                    viewModel.removeTeamMember(teamId, userId)
                                },
                                onRunPolicySimulation = { actor, repo, artifact, action ->
                                    viewModel.runPolicySimulation(actor, repo, artifact, action)
                                },
                                onClearPolicySimulation = { viewModel.clearSimulationResult() },
                                onUpdatePolicySettings = { dualApp, allowUserRepos, revGate, segDuties ->
                                    viewModel.updateEnterprisePolicies(dualApp, allowUserRepos, revGate, segDuties)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWorkBoard && selectedRepo != null && selectedArtifact == null) {
        RepositoryWorkBoardDialog(
            repo = selectedRepo!!,
            issues = selectedRepoIssues,
            onUpdateIssueStatus = { issueId, newStatus ->
                viewModel.updateIssueStatus(issueId, newStatus)
            },
            onDismiss = { showWorkBoard = false }
        )
    }

    if (showPersonaSwitcher) {
        PersonaSwitcherDialog(
            currentUser = activeUser,
            allUsers = users,
            onSelectUser = { user ->
                viewModel.switchActiveUser(user)
            },
            onDismiss = { showPersonaSwitcher = false }
        )
    }
}

@Composable
fun ActivePersonaPill(
    user: User?,
    onClick: () -> Unit
) {
    if (user == null) return

    val avatarColor = try {
        Color(android.graphics.Color.parseColor(user.avatarColorHex))
    } catch (e: Exception) {
        LavenderPrimary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = SophisticatedContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier
            .padding(end = 8.dp)
            .testTag("active_persona_trigger")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(LavenderPrimary.copy(alpha = 0.2f))
                    .border(1.dp, LavenderPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.take(1),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LavenderPrimary
                )
            }

            Column {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextHighEmphasis
                )
                Text(
                    text = user.title.take(18),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = LavenderSubtle
                )
            }

            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "切換身分",
                tint = LavenderPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
