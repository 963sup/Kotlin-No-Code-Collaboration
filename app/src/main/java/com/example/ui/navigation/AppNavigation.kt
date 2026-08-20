package com.example.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainNavigationTab
import com.example.PrimaryBottomNavigationTabs
import com.example.bottomNavigationLabel
import com.example.bottomNavigationTestTag
import com.example.data.model.*
import com.example.navigation.CollaborationTarget
import com.example.ui.components.PersonaSwitcherDialog
import com.example.ui.components.WorkspaceScopeKind
import com.example.ui.components.WorkspaceScopeSelection
import com.example.ui.components.WorkspaceScopeSwitcherSheet
import com.example.ui.explore.ExploreScreen
import com.example.ui.home.HomeScreen
import com.example.ui.inbox.InboxScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.repository.RepositoryDetailScreen
import com.example.ui.screens.ArtifactDetailScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.MeSubTab
import com.example.ui.screens.PersonalCenterSwitchScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.screens.WorkItemDetailScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.CollaborationExperienceViewModel
import com.example.ui.viewmodel.GovernanceViewModel
import com.example.ui.work.WorkScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationHost(
    viewModel: GovernanceViewModel,
    experienceViewModel: CollaborationExperienceViewModel,
    modifier: Modifier = Modifier,
) {
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
    val selectedIssueEvidence by viewModel.selectedIssueEvidence.collectAsState()
    val selectedIssueChecklist by viewModel.selectedIssueChecklist.collectAsState()
    val selectedEvidenceVerifications by viewModel.selectedEvidenceVerifications.collectAsState()

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
    var showWorkspaceScopeSwitcher by remember { mutableStateOf(false) }
    var selectedIssueDetail by remember { mutableStateOf<RepoIssue?>(null) }
    var verificationIssueId by remember { mutableStateOf<String?>(null) }
    var selectedWorkspaceScope by remember { mutableStateOf<WorkspaceScopeSelection?>(null) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    LaunchedEffect(activeUser?.id) {
        if (selectedWorkspaceScope == null && activeUser != null) {
            selectedWorkspaceScope = WorkspaceScopeSelection(
                kind = WorkspaceScopeKind.USER,
                id = activeUser!!.id,
                name = activeUser!!.displayName,
                subtitle = "@${activeUser!!.username} • ${activeUser!!.title}",
            )
        }
    }

    val isDetailScreenOpen = selectedRepo != null || selectedArtifact != null || selectedIssueDetail != null || verificationIssueId != null || isSettingsOpen

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 600.dp

        Scaffold(
            topBar = {},
            bottomBar = {
                if (!isDetailScreenOpen && !isExpanded) {
                    Surface(
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
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
                                            MainNavigationTab.WORK -> Icons.Default.Dashboard
                                            MainNavigationTab.EXPLORE -> Icons.Default.Search
                                            MainNavigationTab.PROFILE -> Icons.Default.AccountCircle
                                        }
                                        if (tab == MainNavigationTab.INBOX && unreadNotificationCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary) {
                                                        Text("$unreadNotificationCount")
                                                    }
                                                },
                                            ) {
                                                Icon(icon, contentDescription = tab.bottomNavigationLabel())
                                            }
                                        } else {
                                            Icon(icon, contentDescription = tab.bottomNavigationLabel())
                                        }
                                    },
                                    label = {
                                        Text(text = tab.bottomNavigationLabel(), style = MaterialTheme.typography.labelSmall)
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = LavenderOnPrimary,
                                        selectedTextColor = LavenderPrimary,
                                        unselectedIconColor = TextMediumEmphasis,
                                        unselectedTextColor = TextMediumEmphasis,
                                        indicatorColor = LavenderPrimary,
                                    ),
                                    modifier = Modifier.testTag(tab.bottomNavigationTestTag()),
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                // Adaptive Navigation Rail for Expanded/Tablet screens
                if (isExpanded && !isDetailScreenOpen) {
                    NavigationRail(
                        containerColor = SophisticatedSurfaceDark,
                        contentColor = TextHighEmphasis,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        PrimaryBottomNavigationTabs.forEach { tab ->
                            val icon = when (tab) {
                                MainNavigationTab.HOME -> Icons.Default.Home
                                MainNavigationTab.INBOX -> Icons.Default.Notifications
                                MainNavigationTab.WORK -> Icons.Default.Dashboard
                                MainNavigationTab.EXPLORE -> Icons.Default.Search
                                MainNavigationTab.PROFILE -> Icons.Default.AccountCircle
                            }
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = {
                                    viewModel.selectArtifact(null)
                                    viewModel.selectRepository(null)
                                    currentTab = tab
                                },
                                icon = {
                                    if (tab == MainNavigationTab.INBOX && unreadNotificationCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary) {
                                                    Text("$unreadNotificationCount")
                                                }
                                            },
                                        ) {
                                            Icon(icon, contentDescription = tab.bottomNavigationLabel())
                                        }
                                    } else {
                                        Icon(icon, contentDescription = tab.bottomNavigationLabel())
                                    }
                                },
                                label = { Text(tab.bottomNavigationLabel()) },
                                modifier = Modifier.testTag(tab.bottomNavigationTestTag()),
                            )
                        }
                    }
                }

                // Main Display Content
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when {
                        isSettingsOpen -> {
                            SettingsScreen(
                                onNavigateBack = { isSettingsOpen = false },
                            )
                        }

                        verificationIssueId != null -> {
                            val verIssue = allIssues.firstOrNull { it.id == verificationIssueId }
                            if (verIssue != null) {
                                VerificationScreen(
                                    evidenceList = selectedIssueEvidence,
                                    verifications = selectedEvidenceVerifications,
                                    onVerifySubmit = { isAccepted, comment ->
                                        val evidenceId = selectedIssueEvidence.firstOrNull()?.id ?: "evd_${verIssue.id}"
                                        viewModel.submitVerification(evidenceId, verIssue.id, isAccepted, comment, activeUser)
                                        verificationIssueId = null
                                        selectedIssueDetail = null
                                    },
                                    issue = verIssue,
                                    onBack = { verificationIssueId = null },
                                )
                            }
                        }

                        selectedIssueDetail != null -> {
                            WorkItemDetailScreen(
                                evidenceList = selectedIssueEvidence,
                                checklist = selectedIssueChecklist,
                                onToggleChecklist = { id, done -> viewModel.toggleChecklistItem(id, done, activeUser) },
                                onAddChecklistItem = { title -> viewModel.addChecklistItem(selectedIssueDetail!!.id, title, activeUser) },
                                onAddEvidence = { desc -> viewModel.addWorkEvidence(selectedIssueDetail!!.id, desc, activeUser) },
                                issue = selectedIssueDetail!!,
                                activeUser = activeUser,
                                onBack = { selectedIssueDetail = null },
                                onNavigateToVerification = { issueId -> verificationIssueId = issueId },
                            )
                        }

                        selectedArtifact != null && selectedRepo != null -> {
                            ArtifactDetailScreen(
                                artifact = selectedArtifact!!,
                                repo = selectedRepo!!,
                                reviews = selectedArtifactReviews,
                                approvals = selectedArtifactApprovals,
                                activeUser = activeUser,
                                onBack = { viewModel.selectArtifact(null) },
                                onSubmitForReview = { viewModel.submitForReview(selectedArtifact!!.id) },
                                onSubmitReview = { decision, feedback ->
                                    viewModel.submitReview(selectedArtifact!!.id, decision, feedback)
                                },
                                onSubmitApproverSignOff = { viewModel.submitApproverSignOff(selectedArtifact!!.id) },
                                onPublishAndLock = { viewModel.publishAndLock(selectedArtifact!!.id) },
                                onInspectPolicy = { action ->
                                    if (activeUser != null) {
                                        viewModel.runPolicySimulation(
                                            actor = activeUser!!,
                                            repo = selectedRepo!!,
                                            artifact = selectedArtifact,
                                            action = action,
                                        )
                                    }
                                },
                                simulationResult = simulationResult,
                                onClearSimulation = { viewModel.clearSimulationResult() },
                            )
                        }

                        selectedRepo != null -> {
                            RepositoryDetailScreen(
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
                                onNavigateToIssue = { selectedIssueDetail = it },
                                onBack = { viewModel.selectRepository(null) },
                                onSelectArtifact = { art -> viewModel.selectArtifact(art) },
                                onCreateArtifact = { title, type, summary, content, callback ->
                                    viewModel.createNoCodeArtifact(
                                        repoId = selectedRepo!!.id,
                                        title = title,
                                        type = type,
                                        summary = summary,
                                        content = content,
                                        onComplete = callback,
                                    )
                                },
                                onAddAccessRule = { granteeType, granteeId, granteeName, role ->
                                    viewModel.addRepoAccessRule(selectedRepo!!.id, granteeType, granteeId, granteeName, role)
                                },
                                onRemoveAccessRule = { rule -> viewModel.removeRepoAccessRule(rule) },
                                onCreateIssue = {
                                    title, desc, priority, assigneeType, assigneeId, assigneeName,
                                    linkedArtifactId, linkedArtifactTitle, parentIssueId, labels, callback ->
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
                                        onSuccess = callback,
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
                                onLoadIssueComments = { issueId -> viewModel.loadIssueComments(issueId) },
                                onCreateDiscussion = { title, category, body, callback ->
                                    viewModel.createDiscussion(
                                        repoId = selectedRepo!!.id,
                                        title = title,
                                        category = category,
                                        body = body,
                                        onSuccess = callback,
                                    )
                                },
                                onAddDiscussionComment = { discussionId, content, callback ->
                                    viewModel.addDiscussionComment(discussionId, content, callback)
                                },
                                onToggleLockDiscussion = { discussionId -> viewModel.toggleLockDiscussion(discussionId) },
                                onMarkAcceptedAnswer = { discussionId, commentId -> viewModel.markAcceptedAnswer(discussionId, commentId) },
                                onUpvoteDiscussion = { discussionId -> viewModel.upvoteDiscussion(discussionId) },
                                onUpvoteDiscussionComment = { commentId, discussionId -> viewModel.upvoteDiscussionComment(commentId, discussionId) },
                                onLoadDiscussionComments = { discussionId -> viewModel.loadDiscussionComments(discussionId) },
                            )
                        }

                        else -> {
                            when (currentTab) {
                                MainNavigationTab.HOME -> {
                                    HomeScreen(
                                        repositories = repositories,
                                        activeUser = activeUser,
                                        onSelectRepository = { repo -> viewModel.selectRepository(repo) },
                                        onNavigateToTab = { tab -> currentTab = tab },
                                        onOpenPersonaSwitcher = { showPersonaSwitcher = true },
                                        onOpenProfile = { currentTab = MainNavigationTab.PROFILE },
                                        onSyncRefresh = { experienceViewModel.syncNow() },
                                    )
                                }

                                MainNavigationTab.WORK -> {
                                    WorkScreen(
                                        issues = allIssues,
                                        onUpdateIssueStatus = { issueId, status -> viewModel.updateIssueStatus(issueId, status) },
                                    )
                                }

                                MainNavigationTab.EXPLORE -> {
                                    ExploreScreen(
                                        repositories = repositories,
                                        onSelectRepository = { repo -> viewModel.selectRepository(repo) },
                                    )
                                }

                                MainNavigationTab.INBOX -> {
                                    InboxScreen(
                                        notifications = userNotifications,
                                        onNotificationClick = { notification ->
                                            viewModel.markNotificationAsRead(notification.id)
                                            notification.repoId?.let { repoId ->
                                                repositories.firstOrNull { it.id == repoId }?.let { repo ->
                                                    viewModel.selectRepository(repo)
                                                    notification.artifactId?.let { artId ->
                                                        allArtifacts.firstOrNull { it.id == artId }?.let { art ->
                                                            viewModel.selectArtifact(art)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                                        onConfigureNotifications = { isSettingsOpen = true },
                                    )
                                }

                                MainNavigationTab.PROFILE -> {
                                    val currentActiveUser = activeUser
                                    val profile = inspectedProfileUser ?: currentActiveUser
                                    if (profile != null && currentActiveUser != null) {
                                        PersonalCenterSwitchScreen(
                                            profileUser = profile,
                                            activeUser = currentActiveUser,
                                            auditLogs = auditLogs,
                                            visibleRepositoryIds = repositories.map { it.id }.toSet(),
                                            follows = userFollows,
                                            savedTargets = savedTargets,
                                            syncStatus = syncStatus,
                                            onToggleFollow = { experienceViewModel.toggleFollow(currentActiveUser.id, it) },
                                            onSyncNow = experienceViewModel::syncNow,
                                            onNavigateToSettings = { isSettingsOpen = true },
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
                                                    onSwitchActivePersona = { u -> viewModel.switchActiveUser(u) },
                                                    onSelectRepository = { repo -> viewModel.selectRepository(repo) },
                                                    onSelectArtifact = { art ->
                                                        repositories.firstOrNull { it.id == art.repoId }?.let { repo ->
                                                            viewModel.selectRepository(repo)
                                                            viewModel.selectArtifact(art)
                                                        }
                                                    },
                                                    onUpdateProfile = { target, displayName, title, bio, loc, pronouns, avatarColor, notifPrefs ->
                                                        viewModel.updateUserProfile(target, displayName, title, bio, loc, pronouns, avatarColor, notifPrefs)
                                                    },
                                                    onCreateEnterprise = { name, slug, desc, dualApp, allowUserRepos, revGate, segDuties ->
                                                        viewModel.createEnterprise(name, slug, desc, dualApp, allowUserRepos, revGate, segDuties)
                                                    },
                                                    onUpdateEnterprisePolicies = { updated -> viewModel.updateEnterpriseSecurityPolicies(updated) },
                                                    onCreateEnterpriseUser = { entId, username, displayName, email, title, isAdmin, avatarColor ->
                                                        viewModel.createEnterpriseUser(entId, username, displayName, email, title, isAdmin, avatarColor)
                                                    },
                                                    onCreateOrganization = { entId, name, slug, desc, colorHex, defaultRole, ownerId ->
                                                        viewModel.createOrganization(entId, name, slug, desc, colorHex, defaultRole, ownerId)
                                                    },
                                                    onUpdateOrganization = { updated -> viewModel.updateOrganization(updated) },
                                                    onAddOrgMember = { orgId, userId, role -> viewModel.addOrgMember(orgId, userId, role) },
                                                    onRemoveOrgMember = { orgId, userId -> viewModel.removeOrgMember(orgId, userId) },
                                                    onCreateTeam = { orgId, name, slug, desc, parentTeamId ->
                                                        viewModel.createTeam(orgId, name, slug, desc, parentTeamId)
                                                    },
                                                    onAddTeamMember = { teamId, userId, role -> viewModel.addTeamMember(teamId, userId, role) },
                                                    onRemoveTeamMember = { teamId, userId -> viewModel.removeTeamMember(teamId, userId) },
                                                    onRunPolicySimulation = { actor, repo, artifact, action ->
                                                        viewModel.runPolicySimulation(actor, repo, artifact, action)
                                                    },
                                                    onClearPolicySimulation = { viewModel.clearSimulationResult() },
                                                    onUpdatePolicySettings = { dualApp, allowUserRepos, revGate, segDuties ->
                                                        viewModel.updateEnterprisePolicies(dualApp, allowUserRepos, revGate, segDuties)
                                                    },
                                                    onNavigateToSettings = { isSettingsOpen = true },
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedIssueDetail?.id) {
        if (selectedIssueDetail != null) {
            viewModel.loadIssueDetailData(selectedIssueDetail!!.id)
        }
    }

    LaunchedEffect(verificationIssueId) {
        if (verificationIssueId != null) {
            viewModel.loadIssueDetailData(verificationIssueId!!)
            val evidence = selectedIssueEvidence.firstOrNull()
            if (evidence != null) {
                viewModel.loadEvidenceVerifications(evidence.id)
            }
        }
    }

    if (showPersonaSwitcher) {
        PersonaSwitcherDialog(
            currentUser = activeUser,
            allUsers = users,
            onSelectUser = { user -> viewModel.switchActiveUser(user) },
            onDismiss = { showPersonaSwitcher = false },
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
            onDismiss = { showWorkspaceScopeSwitcher = false },
        )
    }
}

@Composable
fun ActivePersonaPill(user: User?, onClick: () -> Unit) {
    if (user == null) return

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = SophisticatedContainer,
        border = BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier
            .padding(end = 8.dp)
            .testTag("active_persona_trigger"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(LavenderPrimary.copy(alpha = 0.2f))
                    .border(1.dp, LavenderPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user.displayName.take(1),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LavenderPrimary,
                )
            }

            Column {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextHighEmphasis,
                )
                Text(
                    text = user.title.take(18),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = LavenderSubtle,
                )
            }

            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "切換身分",
                tint = LavenderPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
