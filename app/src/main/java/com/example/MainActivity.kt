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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GranteeType
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OwnerType
import com.example.data.model.Repository
import com.example.data.model.User
import com.example.ui.components.PersonaSwitcherDialog
import com.example.ui.components.RepositoryWorkBoardDialog
import com.example.ui.components.WorkspaceScopeKind
import com.example.ui.components.WorkspaceScopeSelection
import com.example.ui.components.WorkspaceScopeSwitcherSheet
import com.example.ui.screens.ArtifactDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.KanbanBoardScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.MeSubTab
import com.example.ui.screens.RepoDetailScreen
import com.example.ui.screens.RepositoriesScreen
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
import com.example.navigation.CollaborationTarget
import com.example.ui.screens.UnifiedExploreScreen
import com.example.ui.screens.PersonalCenterSwitchScreen
import com.example.ui.viewmodel.CollaborationExperienceViewModel

enum class MainNavigationTab {
    HOME,
    INBOX,
    KANBAN,
    EXPLORE,
    ME
}

internal val PrimaryBottomNavigationTabs = listOf(
    MainNavigationTab.HOME,
    MainNavigationTab.INBOX,
    MainNavigationTab.KANBAN,
    MainNavigationTab.EXPLORE
)

internal fun MainNavigationTab.bottomNavigationLabel(): String = when (this) {
    MainNavigationTab.HOME -> "首頁"
    MainNavigationTab.INBOX -> "收件匣"
    MainNavigationTab.KANBAN -> "工作"
    MainNavigationTab.EXPLORE -> "探索"
    MainNavigationTab.ME -> "個人"
}

internal fun MainNavigationTab.bottomNavigationTestTag(): String = when (this) {
    MainNavigationTab.HOME -> "nav_tab_home"
    MainNavigationTab.INBOX -> "nav_tab_inbox"
    MainNavigationTab.KANBAN -> "nav_tab_kanban"
    MainNavigationTab.EXPLORE -> "nav_tab_explore"
    MainNavigationTab.ME -> "nav_tab_me"
}

class MainActivity : ComponentActivity() {
    private val viewModel: GovernanceViewModel by viewModels()
    private val experienceViewModel: CollaborationExperienceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GovernanceApp(viewModel = viewModel, experienceViewModel = experienceViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernanceApp(viewModel: GovernanceViewModel, experienceViewModel: CollaborationExperienceViewModel) {
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
    val savedTargets by experienceViewModel.savedTargets.collectAsState()
    val userFollows by experienceViewModel.userFollows.collectAsState()
    val syncStatus by experienceViewModel.syncStatus.collectAsState()

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
    var showWorkspaceScopeSwitcher by remember { mutableStateOf(false) }
    var selectedWorkspaceScope by remember { mutableStateOf<WorkspaceScopeSelection?>(null) }

    LaunchedEffect(activeUser?.id) {
        if (selectedWorkspaceScope == null && activeUser != null) {
            selectedWorkspaceScope = WorkspaceScopeSelection(
                kind = WorkspaceScopeKind.USER,
                id = activeUser!!.id,
                name = activeUser!!.displayName,
                subtitle = "@${activeUser!!.username} • ${activeUser!!.title}"
            )
        }
    }

    val selectedScopeEnterpriseId = when (selectedWorkspaceScope?.kind) {
        WorkspaceScopeKind.ENTERPRISE -> selectedWorkspaceScope?.id
        WorkspaceScopeKind.ORGANIZATION -> organizations.firstOrNull { it.id == selectedWorkspaceScope?.id }?.enterpriseId
        WorkspaceScopeKind.TEAM -> {
            val team = teams.firstOrNull { it.id == selectedWorkspaceScope?.id }
            organizations.firstOrNull { it.id == team?.orgId }?.enterpriseId
        }
        WorkspaceScopeKind.USER -> users.firstOrNull { it.id == selectedWorkspaceScope?.id }?.enterpriseId
        null -> activeUser?.enterpriseId ?: enterprise?.id
    }

    val scopedEnterprise = enterprises.firstOrNull { it.id == selectedScopeEnterpriseId } ?: enterprise

    val scopedOrganizations = when (selectedWorkspaceScope?.kind) {
        WorkspaceScopeKind.ENTERPRISE -> organizations.filter { it.enterpriseId == selectedWorkspaceScope?.id }
        WorkspaceScopeKind.ORGANIZATION -> organizations.filter { it.id == selectedWorkspaceScope?.id }
        WorkspaceScopeKind.TEAM -> {
            val orgId = teams.firstOrNull { it.id == selectedWorkspaceScope?.id }?.orgId
            organizations.filter { it.id == orgId }
        }
        WorkspaceScopeKind.USER -> {
            val userId = selectedWorkspaceScope?.id
            val orgIds = allOrgMemberships.filter { it.userId == userId }.map { it.orgId }.toSet()
            organizations.filter { it.id in orgIds }
        }
        null -> organizations
    }

    val scopedTeams = when (selectedWorkspaceScope?.kind) {
        WorkspaceScopeKind.ENTERPRISE -> {
            val orgIds = organizations.filter { it.enterpriseId == selectedWorkspaceScope?.id }.map { it.id }.toSet()
            teams.filter { it.orgId in orgIds }
        }
        WorkspaceScopeKind.ORGANIZATION -> teams.filter { it.orgId == selectedWorkspaceScope?.id }
        WorkspaceScopeKind.TEAM -> teams.filter { it.id == selectedWorkspaceScope?.id }
        WorkspaceScopeKind.USER -> {
            val userId = selectedWorkspaceScope?.id
            val teamIds = allTeamMemberships.filter { it.userId == userId }.map { it.teamId }.toSet()
            teams.filter { it.id in teamIds }
        }
        null -> teams
    }

    val scopedUsers = when (selectedWorkspaceScope?.kind) {
        WorkspaceScopeKind.ENTERPRISE -> users.filter { it.enterpriseId == selectedWorkspaceScope?.id }
        WorkspaceScopeKind.ORGANIZATION -> {
            val userIds = allOrgMemberships.filter { it.orgId == selectedWorkspaceScope?.id }.map { it.userId }.toSet()
            users.filter { it.id in userIds }
        }
        WorkspaceScopeKind.TEAM -> {
            val userIds = allTeamMemberships.filter { it.teamId == selectedWorkspaceScope?.id }.map { it.userId }.toSet()
            users.filter { it.id in userIds }
        }
        WorkspaceScopeKind.USER -> users.filter { it.id == selectedWorkspaceScope?.id }
        null -> users
    }

    val scopedRepositories = when (selectedWorkspaceScope?.kind) {
        WorkspaceScopeKind.ENTERPRISE -> repositories.filter { it.enterpriseId == selectedWorkspaceScope?.id }
        WorkspaceScopeKind.ORGANIZATION -> repositories.filter {
            it.ownerType == OwnerType.ORGANIZATION && it.ownerId == selectedWorkspaceScope?.id
        }
        WorkspaceScopeKind.TEAM -> {
            val teamId = selectedWorkspaceScope?.id
            val repoIds = allAccessRules
                .filter { it.granteeType == GranteeType.TEAM && it.granteeId == teamId }
                .map { it.repoId }
                .toSet()
            repositories.filter { it.id in repoIds }
        }
        WorkspaceScopeKind.USER -> {
            val userId = selectedWorkspaceScope?.id
            val orgIds = allOrgMemberships.filter { it.userId == userId }.map { it.orgId }.toSet()
            val teamIds = allTeamMemberships.filter { it.userId == userId }.map { it.teamId }.toSet()
            val grantedRepoIds = allAccessRules.filter {
                (it.granteeType == GranteeType.USER && it.granteeId == userId) ||
                    (it.granteeType == GranteeType.TEAM && it.granteeId in teamIds)
            }.map { it.repoId }.toSet()

            repositories.filter {
                (it.ownerType == OwnerType.USER && it.ownerId == userId) ||
                    (it.ownerType == OwnerType.ORGANIZATION && it.ownerId in orgIds) ||
                    it.id in grantedRepoIds
            }
        }
        null -> repositories
    }

    val scopedRepoIds = scopedRepositories.map { it.id }.toSet()
    val scopedArtifacts = allArtifacts.filter { it.repoId in scopedRepoIds }
    val scopedArtifactIds = scopedArtifacts.map { it.id }.toSet()
    val scopedIssues = allIssues.filter { it.repoId in scopedRepoIds }
    val scopedDiscussions = allDiscussions.filter { it.repoId in scopedRepoIds }
    val scopedDependencies = allDependencies.filter { it.repoId in scopedRepoIds }
    val scopedReviews = allReviews.filter { it.artifactId in scopedArtifactIds }
    val scopedApprovals = allApprovals.filter { it.artifactId in scopedArtifactIds }
    val scopedAccessRules = allAccessRules.filter { it.repoId in scopedRepoIds }
    val scopedAuditLogs = auditLogs.filter {
        it.repoId == null && it.enterpriseId == scopedEnterprise?.id || it.repoId in scopedRepoIds
    }

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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(LavenderPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = LavenderOnPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = selectedWorkspaceScope?.name ?: scopedEnterprise?.name ?: "存取治理",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = TextHighEmphasis,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedWorkspaceScope?.kind?.label ?: "企業",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextMediumEmphasis,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.selectArtifact(null)
                            viewModel.selectRepository(null)
                            currentTab = MainNavigationTab.EXPLORE
                        },
                        modifier = Modifier.testTag("topbar_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜尋",
                            tint = TextHighEmphasis
                        )
                    }

                    IconButton(
                        onClick = {
                            if (activeUser != null) viewModel.selectProfileUser(activeUser)
                            meSubTab = MeSubTab.PROFILE
                            viewModel.selectArtifact(null)
                            viewModel.selectRepository(null)
                            currentTab = MainNavigationTab.ME
                        },
                        modifier = Modifier.testTag("topbar_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "我的帳號",
                            tint = if (currentTab == MainNavigationTab.ME) LavenderPrimary else TextHighEmphasis
                        )
                    }

                    IconButton(
                        onClick = { showWorkspaceScopeSwitcher = true },
                        modifier = Modifier.testTag("topbar_scope_switcher_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "切換企業、組織、團隊或用戶範圍",
                            tint = LavenderPrimary
                        )
                    }
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
    PrimaryBottomNavigationTabs.forEach { tab ->
        NavigationBarItem(
            selected = currentTab == tab,
            onClick = {
                viewModel.selectArtifact(null)
                viewModel.selectRepository(null)
                currentTab = tab
            },
            icon = {
                val icon = when (tab) {
                    MainNavigationTab.HOME -> Icons.Default.Home
                    MainNavigationTab.INBOX -> Icons.Default.Notifications
                    MainNavigationTab.KANBAN -> Icons.Default.Dashboard
                    MainNavigationTab.EXPLORE -> Icons.Default.Search
                    MainNavigationTab.ME -> Icons.Default.AccountCircle
                }
                if (tab == MainNavigationTab.INBOX) {
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
                            imageVector = icon,
                            contentDescription = tab.bottomNavigationLabel()
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.bottomNavigationLabel()
                    )
                }
            },
            label = {
                Text(
                    text = tab.bottomNavigationLabel(),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LavenderOnPrimary,
                selectedTextColor = LavenderPrimary,
                unselectedIconColor = TextMediumEmphasis,
                unselectedTextColor = TextMediumEmphasis,
                indicatorColor = LavenderPrimary
            ),
            modifier = Modifier.testTag(tab.bottomNavigationTestTag())
        )
    }
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
                        onUpdateIssuePlan = { id, order, start, end, weight, progress ->
                            viewModel.updateIssuePlan(id, order, start, end, weight, progress)
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
                                scopeKind = selectedWorkspaceScope?.kind,
                                scopeName = selectedWorkspaceScope?.name,
                                activeUser = activeUser,
                                enterprise = scopedEnterprise,
                                organizations = scopedOrganizations,
                                teams = scopedTeams,
                                repositories = scopedRepositories,
                                allArtifacts = scopedArtifacts,
                                allIssues = scopedIssues,
                                allDiscussions = scopedDiscussions,
                                allDependencies = scopedDependencies,
                                allReviews = scopedReviews,
                                allApprovals = scopedApprovals,
                                allAccessRules = scopedAccessRules,
                                allOrgMemberships = allOrgMemberships,
                                allTeamMemberships = allTeamMemberships,
                                notifications = userNotifications,
                                auditLogs = scopedAuditLogs,
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
                                    currentTab = MainNavigationTab.EXPLORE
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
                                repositories = scopedRepositories,
                                allIssues = scopedIssues,
                                onUpdateIssueStatus = { issueId, status -> viewModel.updateIssueStatus(issueId, status) },
                                onOpenRepository = { repo -> viewModel.selectRepository(repo) }
                            )
                        }

                        MainNavigationTab.EXPLORE -> {
                            UnifiedExploreScreen(
                                activeUser = activeUser,
                                repositories = scopedRepositories,
                                artifacts = scopedArtifacts,
                                issues = scopedIssues,
                                discussions = scopedDiscussions,
                                organizations = scopedOrganizations,
                                teams = scopedTeams,
                                users = scopedUsers,
                                savedTargets = savedTargets,
                                onOpenTarget = { target ->
                                    when (target) {
                                        is CollaborationTarget.Repository -> repositories.firstOrNull { it.id == target.repositoryId }?.let(viewModel::selectRepository)
                                        is CollaborationTarget.Artifact -> {
                                            val repo = repositories.firstOrNull { it.id == target.repositoryId }
                                            val artifact = allArtifacts.firstOrNull { it.id == target.artifactId && it.repoId == target.repositoryId }
                                            if (repo != null && artifact != null) {
                                                viewModel.selectRepository(repo)
                                                viewModel.selectArtifact(artifact)
                                            }
                                        }
                                        is CollaborationTarget.Issue -> repositories.firstOrNull { it.id == target.repositoryId }?.let(viewModel::selectRepository)
                                        is CollaborationTarget.Discussion -> repositories.firstOrNull { it.id == target.repositoryId }?.let(viewModel::selectRepository)
                                        is CollaborationTarget.Organization -> {
                                            organizations.firstOrNull { it.id == target.organizationId }?.let { org ->
                                                selectedWorkspaceScope = WorkspaceScopeSelection(WorkspaceScopeKind.ORGANIZATION, org.id, org.name, org.description)
                                                currentTab = MainNavigationTab.HOME
                                            }
                                        }
                                        is CollaborationTarget.Team -> {
                                            teams.firstOrNull { it.id == target.teamId }?.let { team ->
                                                selectedWorkspaceScope = WorkspaceScopeSelection(WorkspaceScopeKind.TEAM, team.id, team.name, team.description)
                                                currentTab = MainNavigationTab.HOME
                                            }
                                        }
                                        is CollaborationTarget.UserProfile -> {
                                            users.firstOrNull { it.id == target.userId }?.let { profile ->
                                                viewModel.selectProfileUser(profile)
                                                meSubTab = MeSubTab.PROFILE
                                                currentTab = MainNavigationTab.ME
                                            }
                                        }
                                    }
                                },
                                onToggleSaved = { target -> activeUser?.let { experienceViewModel.toggleSaved(it.id, target) } }
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
                            val currentActiveUser = activeUser
                            val profile = inspectedProfileUser ?: currentActiveUser
                            if (profile != null && currentActiveUser != null) {
                                PersonalCenterSwitchScreen(
                                    profileUser = profile,
                                    activeUser = currentActiveUser,
                                    auditLogs = scopedAuditLogs,
                                    visibleRepositoryIds = scopedRepoIds,
                                    follows = userFollows,
                                    savedTargets = savedTargets,
                                    syncStatus = syncStatus,
                                    onToggleFollow = { experienceViewModel.toggleFollow(currentActiveUser.id, it) },
                                    onSyncNow = experienceViewModel::syncNow,
                                    governanceContent = {

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
                                )
                            }
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

    if (showWorkspaceScopeSwitcher) {
        WorkspaceScopeSwitcherSheet(
            enterprises = enterprises,
            organizations = organizations,
            teams = teams,
            users = users,
            selectedScope = selectedWorkspaceScope,
            onSelectScope = { scope ->
                selectedWorkspaceScope = scope
                viewModel.selectArtifact(null)
                viewModel.selectRepository(null)
                currentTab = MainNavigationTab.HOME
            },
            onDismiss = { showWorkspaceScopeSwitcher = false }
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
