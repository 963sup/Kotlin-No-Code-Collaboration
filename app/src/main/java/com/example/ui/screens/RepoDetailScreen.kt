package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ArtifactType
import com.example.data.model.AuditLog
import com.example.data.model.DiscussionCategory
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.GranteeType
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.engine.HierarchicalPolicyEngine
import com.example.ui.components.LifecycleBadge
import com.example.ui.components.OwnerTypeTag
import com.example.ui.components.PolicyVerdictBadge
import com.example.ui.components.RepoDiscussionsSection
import com.example.ui.components.RepoIssuesSection
import com.example.ui.components.RoleBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedBorderSubtle
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Primary Repository Workspace Navigation Tabs:
 * Centered around active collaboration: Overview, Issues, Discussions, 個成果.
 */
enum class RepoWorkspaceTab(val label: String, val icon: ImageVector) {
    OVERVIEW("總覽", Icons.Default.Dashboard),
    WBS("WBS", Icons.Default.AccountTree),
    KANBAN("看板", Icons.Default.ViewWeek),
    ISSUE("Issue", Icons.Default.TaskAlt),
    MEMBERS("成員", Icons.Default.Groups),
    FILES("文件", Icons.Default.Description),
}

/**
 * Repository Governance Settings Subsections:
 * General, Access & 個成員, Policies, Audit.
 */
enum class RepoSettingsSection(val label: String, val icon: ImageVector) {
    GENERAL("一般", Icons.Default.Info),
    ACCESS("存取與成員", Icons.Default.Groups),
    POLICIES("政策", Icons.Default.Policy),
    AUDIT("稽核", Icons.Default.History),
}

@Composable
fun RepoDetailScreen(
    repo: Repository,
    enterprise: Enterprise?,
    artifacts: List<NoCodeArtifact>,
    issues: List<RepoIssue> = emptyList(),
    dependencies: List<IssueDependency> = emptyList(),
    discussions: List<RepoDiscussion> = emptyList(),
    issueComments: List<IssueComment> = emptyList(),
    discussionComments: List<DiscussionComment> = emptyList(),
    accessRules: List<RepoAccessRule>,
    allUsers: List<User>,
    allTeams: List<Team>,
    allOrgMemberships: List<OrgMembership>,
    allTeamMemberships: List<TeamMembership>,
    allAuditLogs: List<AuditLog>,
    activeUser: User?,
    onBack: () -> Unit,
    onNavigateToIssue: (RepoIssue) -> Unit,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
    onCreateArtifact: (String, ArtifactType, String, String, (Boolean) -> Unit) -> Unit,
    onAddAccessRule: (GranteeType, String, String, RepoRole) -> Unit,
    onRemoveAccessRule: (RepoAccessRule) -> Unit,
    onCreateIssue: (
        title: String,
        desc: String,
        priority: IssuePriority,
        assigneeType: GranteeType?,
        assigneeId: String?,
        assigneeName: String?,
        linkedArtifactId: String?,
        linkedArtifactTitle: String?,
        parentIssueId: String?,
        labels: String,
        () -> Unit,
    ) -> Unit = {
            _,
            _,
            _,
            _,
            _,
            _,
            _,
            _,
            _,
            _,
            _,
        ->
    },
    onLinkParentIssue: (issueId: String, parentIssueId: String?, () -> Unit) -> Unit = { _, _, _ -> },
    onAddDependency: (repoId: String, blockedIssueId: String, blockingIssueId: String, () -> Unit) -> Unit = {
            _,
            _,
            _,
            _,
        ->
    },
    onRemoveDependency: (dependencyId: String, () -> Unit) -> Unit = { _, _ -> },
    onAddIssueComment: (issueId: String, content: String, () -> Unit) -> Unit = { _, _, _ -> },
    onUpdateIssueStatus: (issueId: String, newStatus: IssueStatus) -> Unit = { _, _ -> },
    onUpdateIssuePlan: (String, Int, Long?, Long?, Double, Int) -> Unit = { _, _, _, _, _, _ -> },
    onAssignIssue: (issueId: String, assigneeType: GranteeType?, assigneeId: String?, assigneeName: String?) -> Unit = {
            _,
            _,
            _,
            _,
        ->
    },
    onLoadIssueComments: (issueId: String) -> Unit = {},
    onCreateDiscussion: (title: String, category: DiscussionCategory, body: String, () -> Unit) -> Unit = {
            _,
            _,
            _,
            _,
        ->
    },
    onAddDiscussionComment: (discussionId: String, content: String, () -> Unit) -> Unit = { _, _, _ -> },
    onToggleLockDiscussion: (discussionId: String) -> Unit = {},
    onMarkAcceptedAnswer: (discussionId: String, commentId: String) -> Unit = { _, _ -> },
    onUpvoteDiscussion: (discussionId: String) -> Unit = {},
    onUpvoteDiscussionComment: (commentId: String, discussionId: String) -> Unit = { _, _ -> },
    onLoadDiscussionComments: (discussionId: String) -> Unit = {},
) {
    // Navigation mode: Workspace vs Contextual Settings
    var inSettingsMode by remember { mutableStateOf(false) }
    var selectedWorkspaceTab by remember { mutableStateOf(RepoWorkspaceTab.OVERVIEW) }
    var selectedSettingsSection by remember { mutableStateOf(RepoSettingsSection.GENERAL) }

    var showCreateArtifactDialog by remember { mutableStateOf(false) }
    var showAddAccessRuleDialog by remember { mutableStateOf(false) }

    val effectiveRolePair = if (activeUser != null) {
        HierarchicalPolicyEngine.resolveEffectiveRole(
            actor = activeUser,
            repo = repo,
            orgMemberships = allOrgMemberships,
            teamMemberships = allTeamMemberships,
            teams = allTeams,
            accessRules = accessRules,
        )
    } else {
        Pair(RepoRole.VIEWER, "Default")
    }

    val effectiveRole = effectiveRolePair.first
    val roleSource = effectiveRolePair.second
    val canManageAccess = effectiveRole.canPerform(RepoRole.MAINTAINER)
    val canCreateArtifact = effectiveRole.canPerform(RepoRole.COLLABORATOR)
    val canCreateIssue = effectiveRole.canPerform(RepoRole.COLLABORATOR)
    val canCreateDiscussion = effectiveRole.canPerform(RepoRole.COLLABORATOR)

    Box(modifier = Modifier.fillMaxSize().background(SophisticatedBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!inSettingsMode) {
                // =========================================================================
                // 1. WORKSPACE HEADER & PRIMARY TABS (Overview, Issues, Discussions, 個成果)
                // =========================================================================
                RepoWorkspaceHeader(
                    repo = repo,
                    enterprise = enterprise,
                    effectiveRole = effectiveRole,
                    roleSource = roleSource,
                    onBack = onBack,
                    onOpenSettings = { inSettingsMode = true },
                )

                // Primary Workspace Tab Row
                TabRow(
                    selectedTabIndex = selectedWorkspaceTab.ordinal,
                    containerColor = SophisticatedSurfaceDark,
                    contentColor = LavenderPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedWorkspaceTab.ordinal]),
                            color = LavenderPrimary,
                            height = 3.dp,
                        )
                    },
                    divider = {
                        HorizontalDivider(color = SophisticatedBorder, thickness = 1.dp)
                    },
                ) {
                    RepoWorkspaceTab.values().forEach { tab ->
                        val count = when (tab) {
                            RepoWorkspaceTab.OVERVIEW -> null
                            RepoWorkspaceTab.WBS -> issues.size
                            RepoWorkspaceTab.KANBAN -> issues.count { it.status != IssueStatus.CLOSED }
                            RepoWorkspaceTab.ISSUE -> issues.count { it.status != IssueStatus.CLOSED }
                            RepoWorkspaceTab.MEMBERS -> discussions.size
                            RepoWorkspaceTab.FILES -> artifacts.size
                        }
                        val isSelected = selectedWorkspaceTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedWorkspaceTab = tab },
                            modifier = Modifier.testTag("repo_tab_${tab.name.lowercase()}"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) LavenderPrimary else TextMediumEmphasis,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = if (count != null) "${tab.label} ($count)" else tab.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = if (isSelected) TextHighEmphasis else TextMediumEmphasis,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            },
                        )
                    }
                }

                // Workspace Content View
                when (selectedWorkspaceTab) {
                    RepoWorkspaceTab.OVERVIEW -> RepoOverviewSection(
                        repo = repo,
                        enterprise = enterprise,
                        artifacts = artifacts,
                        issues = issues,
                        dependencies = dependencies,
                        discussions = discussions,
                        accessRules = accessRules,
                        allAuditLogs = allAuditLogs,
                        effectiveRole = effectiveRole,
                        roleSource = roleSource,
                        onNavigateToTab = { tab -> selectedWorkspaceTab = tab },
                        onNavigateToSettings = { section ->
                            selectedSettingsSection = section
                            inSettingsMode = true
                        },
                        onSelectArtifact = onSelectArtifact,
                    )

                    RepoWorkspaceTab.WBS -> Column(modifier = Modifier.fillMaxSize()) {
                        HomologousThreeViewSwitcher(
                            selectedTab = selectedWorkspaceTab,
                            onTabSelect = { selectedWorkspaceTab = it },
                        )
                        RepositoryWbsSection(
                            issues = issues,
                            onUpdatePlan = onUpdateIssuePlan,
                        )
                    }

                    RepoWorkspaceTab.KANBAN -> Column(modifier = Modifier.fillMaxSize()) {
                        HomologousThreeViewSwitcher(
                            selectedTab = selectedWorkspaceTab,
                            onTabSelect = { selectedWorkspaceTab = it },
                        )
                        RepoKanbanView(
                            issues = issues,
                            onIssueClick = onNavigateToIssue,
                            onUpdateStatus = onUpdateIssueStatus,
                        )
                    }

                    RepoWorkspaceTab.ISSUE -> Column(modifier = Modifier.fillMaxSize()) {
                        HomologousThreeViewSwitcher(
                            selectedTab = selectedWorkspaceTab,
                            onTabSelect = { selectedWorkspaceTab = it },
                        )
                        RepoIssuesSection(
                            repo = repo,
                            issues = issues,
                            dependencies = dependencies,
                            selectedIssueComments = issueComments,
                            onIssueClick = onNavigateToIssue,
                            allUsers = allUsers,
                            allTeams = allTeams,
                            repoArtifacts = artifacts,
                            activeUser = activeUser,
                            canCreateIssue = canCreateIssue,
                            onCreateIssue = onCreateIssue,
                            onLinkParentIssue = onLinkParentIssue,
                            onAddDependency = onAddDependency,
                            onRemoveDependency = onRemoveDependency,
                            onAddComment = onAddIssueComment,
                            onUpdateStatus = onUpdateIssueStatus,
                            onAssignIssue = onAssignIssue,
                            onLoadComments = onLoadIssueComments,
                            onSelectArtifact = onSelectArtifact,
                        )
                    }

                    RepoWorkspaceTab.MEMBERS -> RepoDiscussionsSection(
                        repo = repo,
                        discussions = discussions,
                        selectedDiscussionComments = discussionComments,
                        activeUser = activeUser,
                        effectiveRole = effectiveRole,
                        canCreateDiscussion = canCreateDiscussion,
                        onCreateDiscussion = onCreateDiscussion,
                        onAddComment = onAddDiscussionComment,
                        onToggleLock = onToggleLockDiscussion,
                        onMarkAcceptedAnswer = onMarkAcceptedAnswer,
                        onUpvoteDiscussion = onUpvoteDiscussion,
                        onUpvoteComment = onUpvoteDiscussionComment,
                        onLoadComments = onLoadDiscussionComments,
                    )

                    RepoWorkspaceTab.FILES -> 個成果TabContent(
                        artifacts = artifacts,
                        onSelectArtifact = onSelectArtifact,
                        canCreateArtifact = canCreateArtifact,
                        onCreateArtifactClick = { showCreateArtifactDialog = true },
                    )
                }
            } else {
                // =========================================================================
                // 2. CONTEXTUAL REPOSITORY GOVERNANCE SETTINGS (General, Access & 個成員, Policies, Audit)
                // =========================================================================
                RepoSettingsHeader(
                    repo = repo,
                    onBackToWorkspace = { inSettingsMode = false },
                )

                // Settings Sub-navigation Bar
                ScrollableTabRow(
                    selectedTabIndex = selectedSettingsSection.ordinal,
                    containerColor = SophisticatedSurfaceDark,
                    contentColor = LavenderPrimary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedSettingsSection.ordinal]),
                            color = LavenderPrimary,
                            height = 3.dp,
                        )
                    },
                    divider = {
                        HorizontalDivider(color = SophisticatedBorder, thickness = 1.dp)
                    },
                ) {
                    RepoSettingsSection.values().forEach { section ->
                        val isSelected = selectedSettingsSection == section
                        val count = when (section) {
                            RepoSettingsSection.ACCESS -> accessRules.size
                            RepoSettingsSection.AUDIT -> allAuditLogs.count { it.repoId == repo.id }
                            else -> null
                        }

                        Tab(
                            selected = isSelected,
                            onClick = { selectedSettingsSection = section },
                            modifier = Modifier.testTag("repo_settings_tab_${section.name.lowercase()}"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = section.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) LavenderPrimary else TextMediumEmphasis,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = if (count != null) "${section.label} ($count)" else section.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = if (isSelected) TextHighEmphasis else TextMediumEmphasis,
                                    )
                                }
                            },
                        )
                    }
                }

                // Settings Section Content
                when (selectedSettingsSection) {
                    RepoSettingsSection.GENERAL -> RepoGeneralSettingsContent(
                        repo = repo,
                        effectiveRole = effectiveRole,
                        roleSource = roleSource,
                        artifactCount = artifacts.size,
                        issueCount = issues.size,
                        discussionCount = discussions.size,
                        accessCount = accessRules.size,
                    )

                    RepoSettingsSection.ACCESS -> AccessHierarchyTabContent(
                        accessRules = accessRules,
                        canManageAccess = canManageAccess,
                        onAddRule = { showAddAccessRuleDialog = true },
                        onRemoveRule = onRemoveAccessRule,
                    )

                    RepoSettingsSection.POLICIES -> PoliciesTabContent(
                        repo = repo,
                        enterprise = enterprise,
                    )

                    RepoSettingsSection.AUDIT -> RepoAuditTabContent(
                        repo = repo,
                        auditLogs = allAuditLogs,
                    )
                }
            }
        }

        // Floating Action Button (Only on 個成果 tab in workspace mode when Collaborator+)
        if (!inSettingsMode && selectedWorkspaceTab == RepoWorkspaceTab.FILES && canCreateArtifact) {
            FloatingActionButton(
                onClick = { showCreateArtifactDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_artifact_fab"),
                containerColor = LavenderPrimary,
                contentColor = LavenderOnPrimary,
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新增無程式碼成果")
                    Text("新增藍圖", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCreateArtifactDialog) {
        CreateArtifactDialog(
            onDismiss = { showCreateArtifactDialog = false },
            onCreate = { title, type, summary, content ->
                onCreateArtifact(title, type, summary, content) { success ->
                    if (success) showCreateArtifactDialog = false
                }
            },
        )
    }

    if (showAddAccessRuleDialog) {
        AddAccessRuleDialog(
            allUsers = allUsers,
            allTeams = allTeams,
            existingRules = accessRules,
            onDismiss = { showAddAccessRuleDialog = false },
            onAddRule = { granteeType, granteeId, granteeName, role ->
                onAddAccessRule(granteeType, granteeId, granteeName, role)
                showAddAccessRuleDialog = false
            },
        )
    }
}

// =========================================================================
// WORKSPACE HEADER
// =========================================================================
@Composable
fun RepoWorkspaceHeader(
    repo: Repository,
    enterprise: Enterprise?,
    effectiveRole: RepoRole,
    roleSource: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Navigation Bar / Breadcrumbs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(32.dp).testTag("back_to_repos_button"),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = LavenderPrimary)
                    }

                    Text(
                        text = "${enterprise?.name ?: "企業"} > ${repo.ownerDisplayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Settings Shortcut Icon
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(32.dp).testTag("repo_settings_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "儲存庫設定",
                        tint = LavenderPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repo.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = LavenderPrimary,
                    )
                }

                RoleBadge(role = effectiveRole)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Owner & Role Source Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SophisticatedContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OwnerTypeTag(ownerType = repo.ownerType, ownerDisplayName = repo.ownerDisplayName)

                Text(
                    text = "角色來源：$roleSource",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMediumEmphasis,
                    maxLines = 1,
                )
            }
        }
    }
}

// =========================================================================
// CONTEXTUAL SETTINGS HEADER
// =========================================================================
@Composable
fun RepoSettingsHeader(repo: Repository, onBackToWorkspace: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onBackToWorkspace,
                    modifier = Modifier.size(32.dp).testTag("repo_settings_back_btn"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回儲存庫工作區",
                        tint = LavenderPrimary,
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = LavenderPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "儲存庫設定",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                        )
                    }
                    Text(
                        text = "${repo.displayName} (${repo.name})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                    )
                }
            }
        }
    }
}

// =========================================================================
// REPOSITORY OVERVIEW SECTION (Collaboration State & Health Hub)
// =========================================================================
@Composable
fun RepoOverviewSection(
    repo: Repository,
    enterprise: Enterprise?,
    artifacts: List<NoCodeArtifact>,
    issues: List<RepoIssue>,
    dependencies: List<IssueDependency>,
    discussions: List<RepoDiscussion>,
    accessRules: List<RepoAccessRule>,
    allAuditLogs: List<AuditLog>,
    effectiveRole: RepoRole,
    roleSource: String,
    onNavigateToTab: (RepoWorkspaceTab) -> Unit,
    onNavigateToSettings: (RepoSettingsSection) -> Unit,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
) {
    val openIssues = remember(issues) { issues.filter { it.status != IssueStatus.CLOSED } }
    val criticalIssuesCount = remember(openIssues) {
        openIssues.count { it.priority == IssuePriority.CRITICAL || it.priority == IssuePriority.HIGH }
    }
    val pendingArtifacts = remember(artifacts) {
        artifacts.filter {
            it.lifecycleState == LifecycleState.IN_REVIEW || it.lifecycleState == LifecycleState.PENDING_APPROVAL
        }
    }
    val publishedArtifacts = remember(artifacts) {
        artifacts.filter { it.lifecycleState == LifecycleState.PUBLISHED }
    }
    val repoAuditLogs: List<AuditLog> = remember(allAuditLogs, repo.id) {
        allAuditLogs.filter { it.repoId == repo.id }.sortedByDescending { it.timestamp }.take(5)
    }
    val wbsProgress: Float = remember(issues) { IssueHierarchyRules.overallProgress(issues) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 0. WBS Overall Progress & Homologous Quick Projection Cards (Screen 3 in Blueprint)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SophisticatedBorder),
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_wbs_card"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "WBS 總體推進度",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextHighEmphasis,
                            )
                            Text(
                                text = "${issues.count { it.status == IssueStatus.CLOSED }} / ${issues.size} 個工作節點已完成",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMediumEmphasis,
                            )
                        }
                        Text(
                            text = "${(wbsProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                            ),
                            color = LavenderPrimary,
                        )
                    }

                    LinearProgressIndicator(
                        progress = { wbsProgress.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = LavenderPrimary,
                        trackColor = SophisticatedContainer,
                    )

                    HorizontalDivider(color = SophisticatedBorderSubtle, thickness = 1.dp)

                    // 4 Homologous Fast Navigation Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            QuickNavCard(
                                title = "WBS 工作樹",
                                subtitle = "分析結構與進度",
                                icon = Icons.Default.AccountTree,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(RepoWorkspaceTab.WBS) },
                            )
                            QuickNavCard(
                                title = "Kanban 看板",
                                subtitle = "狀態與進度",
                                icon = Icons.Default.ViewWeek,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(RepoWorkspaceTab.KANBAN) },
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            QuickNavCard(
                                title = "Issue 清單",
                                subtitle = "問題與待辦",
                                icon = Icons.Default.TaskAlt,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(RepoWorkspaceTab.ISSUE) },
                            )
                            QuickNavCard(
                                title = "文件 / 成果物",
                                subtitle = "知識與成果",
                                icon = Icons.Default.Description,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTab(RepoWorkspaceTab.FILES) },
                            )
                        }
                    }
                }
            }
        }
        // 1. Repository Purpose & Ownership Context Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SophisticatedBorder),
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_summary_card"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "儲存庫目的與範圍",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = LavenderPrimary,
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SophisticatedContainer,
                        ) {
                            Text(
                                text = "${repo.ownerType.name} 擁有",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = TextMediumEmphasis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    Text(
                        text = repo.description.ifEmpty {
                            "Enterprise no-code collaboration container for specifications, workflows, and governance schemas."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHighEmphasis,
                        lineHeight = 20.sp,
                    )

                    HorizontalDivider(color = SophisticatedBorderSubtle, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "核准門檻：${repo.requiredApproverCount} 個簽核${if (repo.requiredApproverCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = EmeraldSuccess,
                            )
                        }

                        Text(
                            text = "範圍：${enterprise?.name ?: "企業"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLowEmphasis,
                        )
                    }
                }
            }
        }

        // 2. Effective User Role & Permissions Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SophisticatedBorder),
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_role_card"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "你的有效存取權限",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextHighEmphasis,
                            )
                        }

                        RoleBadge(role = effectiveRole)
                    }

                    Text(
                        text = when (effectiveRole) {
                            RepoRole.OWNER -> "Full container sovereignty: access management, policy enforcement, and destructive controls."
                            RepoRole.MAINTAINER -> "Administrative governance: manage team access rules, policy definitions, and container settings."
                            RepoRole.APPROVER -> "Executive authority: conduct formal reviews, grant binding sign-offs, and publish blueprints."
                            RepoRole.REVIEWER -> "Review authority: evaluate blueprint proposals, recommend changes, and participate in RFCs."
                            RepoRole.COLLABORATOR -> "Active contributor: author blueprints, open action items, and create discussion threads."
                            RepoRole.VIEWER -> "Read-only access: view published blueprints, active issues, discussions, and audit records."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                        lineHeight = 18.sp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "解析來源：$roleSource",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextLowEmphasis,
                        )

                        if (effectiveRole.canPerform(RepoRole.MAINTAINER)) {
                            TextButton(
                                onClick = { onNavigateToSettings(RepoSettingsSection.ACCESS) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                            ) {
                                Text("管理存取", color = LavenderPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Live Collaboration Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OverviewMetricBox(
                    label = "成果",
                    count = artifacts.size,
                    icon = Icons.Default.Description,
                    accentColor = LavenderPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(RepoWorkspaceTab.FILES) },
                )
                OverviewMetricBox(
                    label = "Open Issues",
                    count = openIssues.size,
                    icon = Icons.Default.TaskAlt,
                    accentColor = if (criticalIssuesCount > 0) AmberWarning else LavenderPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(RepoWorkspaceTab.ISSUE) },
                )
                OverviewMetricBox(
                    label = "討論",
                    count = discussions.size,
                    icon = Icons.Default.Forum,
                    accentColor = LavenderPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(RepoWorkspaceTab.MEMBERS) },
                )
                OverviewMetricBox(
                    label = "Pending",
                    count = pendingArtifacts.size,
                    icon = Icons.Default.Info,
                    accentColor = if (pendingArtifacts.isNotEmpty()) RoseError else EmeraldSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(RepoWorkspaceTab.FILES) },
                )
            }
        }

        // 4. Pending Reviews or Approvals Section (Crucial Collaboration & Governance workflow)
        item {
            Column(
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_pending_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (pendingArtifacts.isNotEmpty()) AmberWarning else EmeraldSuccess,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "待審查與核准",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                        )
                    }

                    if (pendingArtifacts.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AmberWarning.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "${pendingArtifacts.size} 需處理",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = AmberWarning,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                if (pendingArtifacts.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp),
                            )
                            Column {
                                Text(
                                    text = "所有藍圖皆為最新",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextHighEmphasis,
                                )
                                Text(
                                    text = "目前沒有待處理的同儕審查或核准門檻簽核。",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMediumEmphasis,
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pendingArtifacts.forEach { artifact ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectArtifact(artifact) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = artifact.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = TextHighEmphasis,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "v${artifact.version}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                ),
                                                color = LavenderPrimary,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "由 ${artifact.authorDisplayName} • 類型：${artifact.type.label}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMediumEmphasis,
                                        )
                                    }

                                    LifecycleBadge(state = artifact.lifecycleState)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Important Blueprints & 個成果 Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_artifacts_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "重要藍圖與規格",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    TextButton(onClick = { onNavigateToTab(RepoWorkspaceTab.FILES) }) {
                        Text("查看全部（${artifacts.size})", color = LavenderPrimary, fontSize = 12.sp)
                    }
                }

                if (artifacts.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "尚未建立藍圖；請切換至成果分頁建立規格、工作流程或資料結構。",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    val displayArtifacts = if (publishedArtifacts.isNotEmpty()) {
                        publishedArtifacts.take(
                        3,
                    )
                    } else {
                        artifacts.take(3)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayArtifacts.forEach { artifact ->
                            ArtifactCardItem(
                                artifact = artifact,
                                onClick = { onSelectArtifact(artifact) },
                            )
                        }
                    }
                }
            }
        }

        // 6. Open Issues & Action Items Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_issues_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "未完成行動項目",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    TextButton(onClick = { onNavigateToTab(RepoWorkspaceTab.ISSUE) }) {
                        Text("查看全部（${issues.size})", color = LavenderPrimary, fontSize = 12.sp)
                    }
                }

                if (openIssues.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "目前沒有未完成的行動項目，進度正常。",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        openIssues.take(3).forEach { issue ->
                            val isBlocked = dependencies.any { it.blockedIssueId == issue.id }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isBlocked) AmberWarning.copy(alpha = 0.6f) else SophisticatedBorder,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToTab(RepoWorkspaceTab.ISSUE) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = "#${issue.issueNumber} • ${issue.title}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = TextHighEmphasis,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (isBlocked) {
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = AmberWarning.copy(alpha = 0.2f),
                                                ) {
                                                    Text(
                                                        text = "受阻",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                        ),
                                                        color = AmberWarning,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = if (issue.assigneeName !=
                                                null
                                            ) {
                                                    "Assigned: ${issue.assigneeName}"
                                                } else {
                                                    "未指派"
                                                },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMediumEmphasis,
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (issue.priority) {
                                            IssuePriority.CRITICAL -> RoseError.copy(alpha = 0.2f)
                                            IssuePriority.HIGH -> AmberWarning.copy(alpha = 0.2f)
                                            IssuePriority.MEDIUM -> LavenderContainer
                                            IssuePriority.LOW -> SophisticatedContainer
                                        },
                                    ) {
                                        Text(
                                            text = issue.priority.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = when (issue.priority) {
                                                IssuePriority.CRITICAL -> RoseError
                                                IssuePriority.HIGH -> AmberWarning
                                                IssuePriority.MEDIUM -> LavenderPrimary
                                                IssuePriority.LOW -> TextMediumEmphasis
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Active Discussions & RFCs Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_discussions_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "進行中的討論與 RFC",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    TextButton(onClick = { onNavigateToTab(RepoWorkspaceTab.MEMBERS) }) {
                        Text("查看全部（${discussions.size})", color = LavenderPrimary, fontSize = 12.sp)
                    }
                }

                if (discussions.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "尚未開始討論；請在討論分頁建立 RFC 或提案。",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        discussions.take(3).forEach { discussion ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SophisticatedBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToTab(RepoWorkspaceTab.MEMBERS) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = SophisticatedContainer,
                                            ) {
                                                Text(
                                                    text = discussion.category.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                    color = LavenderPrimary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                )
                                            }

                                            Text(
                                                text = discussion.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = TextHighEmphasis,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "由 ${discussion.authorDisplayName} • ${discussion.upvoteCount} 票贊成",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMediumEmphasis,
                                        )
                                    }

                                    if (discussion.isAnswered) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldSuccess.copy(alpha = 0.15f),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = EmeraldSuccess,
                                                    modifier = Modifier.size(12.dp),
                                                )
                                                Text(
                                                    text = "已回答",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                    color = EmeraldSuccess,
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
        }

        // 8. Recent Collaboration & Governance Activity Trail Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth().testTag("repo_overview_activity_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = LavenderPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "最近活動",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                        )
                    }

                    TextButton(onClick = { onNavigateToSettings(RepoSettingsSection.AUDIT) }) {
                        Text("查看完整軌跡", color = LavenderPrimary, fontSize = 12.sp)
                    }
                }

                if (repoAuditLogs.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "尚無近期活動紀錄。",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            repoAuditLogs.forEachIndexed { index, log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SophisticatedContainer,
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when {
                                                    log.actionName.contains(
                                                        "CREATE",
                                                        ignoreCase = true,
                                                    ) -> Icons.Default.Add

                                                    log.actionName.contains(
                                                        "REVIEW",
                                                        ignoreCase = true,
                                                    ) -> Icons.Default.Gavel

                                                    log.actionName.contains(
                                                        "APPROVE",
                                                        ignoreCase = true,
                                                    ) -> Icons.Default.Check

                                                    log.actionName.contains(
                                                        "PUBLISH",
                                                        ignoreCase = true,
                                                    ) -> Icons.Default.TaskAlt

                                                    log.actionName.contains(
                                                        "ACCESS",
                                                        ignoreCase = true,
                                                    ) -> Icons.Default.Groups

                                                    else -> Icons.Default.History
                                                },
                                                contentDescription = null,
                                                tint = LavenderPrimary,
                                                modifier = Modifier.size(12.dp),
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${log.actorDisplayName}: ${log.actionName}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium,
                                            ),
                                            color = TextHighEmphasis,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = log.reasoning.ifEmpty { log.verdict.name },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMediumEmphasis,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    Text(
                                        text = SimpleDateFormat(
                                            "MMM dd, HH:mm",
                                            Locale.getDefault(),
                                        ).format(Date(log.timestamp)),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextLowEmphasis,
                                    )
                                }

                                if (index < repoAuditLogs.size - 1) {
                                    HorizontalDivider(color = SophisticatedBorderSubtle, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewMetricBox(
    label: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("overview_metric_${label.lowercase().replace(" ", "_")}"),
        color = SophisticatedSurfaceDark,
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMediumEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// =========================================================================
// GENERAL SETTINGS CONTENT
// =========================================================================
@Composable
fun RepoGeneralSettingsContent(
    repo: Repository,
    effectiveRole: RepoRole,
    roleSource: String,
    artifactCount: Int,
    issueCount: Int,
    discussionCount: Int,
    accessCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "一般容器資訊",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextHighEmphasis,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SettingsInfoRow("顯示名稱", repo.displayName)
                SettingsInfoRow("Identifier", repo.name)
                SettingsInfoRow("Owner Type", repo.ownerType.displayName())
                SettingsInfoRow("Owner Name", repo.ownerDisplayName)
                SettingsInfoRow("有效角色", effectiveRole.name)
                SettingsInfoRow("角色來源", roleSource)
                SettingsInfoRow(
                    "Created On",
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(repo.createdAt)),
                )
                SettingsInfoRow(
                    "Last Modified",
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(repo.updatedAt)),
                )
            }
        }

        Text(
            text = "容器資源摘要",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextHighEmphasis,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SettingsInfoRow("Blueprints & Documents", "$artifactCount registered")
                SettingsInfoRow("Action Items & Issues", "$issueCount total")
                SettingsInfoRow("Collaboration Discussions", "$discussionCount threads")
                SettingsInfoRow("Explicit Access Mappings", "$accessCount rules")
            }
        }
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextHighEmphasis,
        )
    }
}

// =========================================================================
// ARTIFACTS TAB CONTENT
// =========================================================================
@Composable
fun 個成果TabContent(
    artifacts: List<NoCodeArtifact>,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
    canCreateArtifact: Boolean = false,
    onCreateArtifactClick: () -> Unit = {},
) {
    if (artifacts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = TextMediumEmphasis,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    text = "此容器尚未建立藍圖或文件。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMediumEmphasis,
                    textAlign = TextAlign.Center,
                )
                if (canCreateArtifact) {
                    Button(
                        onClick = onCreateArtifactClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("建立藍圖", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(artifacts) { artifact ->
                ArtifactCardItem(
                    artifact = artifact,
                    onClick = { onSelectArtifact(artifact) },
                )
            }
        }
    }
}

@Composable
fun ArtifactCardItem(artifact: NoCodeArtifact, onClick: () -> Unit) {
    val icon = when (artifact.type) {
        ArtifactType.SPECIFICATION_DOC -> Icons.Default.Description
        ArtifactType.PROCESS_WORKFLOW -> Icons.Default.AccountTree
        ArtifactType.DECISION_RECORD -> Icons.Default.Gavel
        ArtifactType.FORM_SCHEMA -> Icons.Default.DynamicForm
        ArtifactType.CANVAS_BOARD -> Icons.Default.Security
        ArtifactType.MILESTONE_RELEASE -> Icons.Default.Flag
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("artifact_item_${artifact.title.take(15).replace(" ", "_")}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = artifact.type.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LavenderPrimary,
                    )
                }

                LifecycleBadge(state = artifact.lifecycleState)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = artifact.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis,
            )

            Text(
                text = artifact.summary,
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
                maxLines = 2,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "作者：${artifact.authorDisplayName} • ${artifact.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextLowEmphasis,
                )

                if (artifact.lockedByPolicy) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "已鎖定",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp),
                        )
                        Text("已鎖定", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                    }
                }
            }
        }
    }
}

// =========================================================================
// ACCESS HIERARCHY TAB CONTENT (Moved to Repository Settings)
// =========================================================================
@Composable
fun AccessHierarchyTabContent(
    accessRules: List<RepoAccessRule>,
    canManageAccess: Boolean,
    onAddRule: () -> Unit,
    onRemoveRule: (RepoAccessRule) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "協作者與團隊存取映射",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Text(
                        text = "映射至使用者與團隊的階層權限",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                    )
                }

                if (canManageAccess) {
                    Button(
                        onClick = onAddRule,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_access_rule_button"),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("指派角色", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (accessRules.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "此容器尚未設定明確的存取規則。",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            items(accessRules) { rule ->
                AccessRuleCardItem(
                    rule = rule,
                    canDelete = canManageAccess,
                    onDelete = { onRemoveRule(rule) },
                )
            }
        }
    }
}

@Composable
fun AccessRuleCardItem(rule: RepoAccessRule, canDelete: Boolean, onDelete: () -> Unit) {
    val isTeam = rule.granteeType == GranteeType.TEAM
    val icon = if (isTeam) Icons.Default.Groups else Icons.Default.Person
    val tint = if (isTeam) LavenderPrimary else CyanAccent

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column {
                    Text(
                        text = rule.granteeName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Text(
                        text = if (isTeam) "Team Group Grant" else "Individual User Grant",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoleBadge(role = rule.role)
                if (canDelete && rule.role != RepoRole.OWNER) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "移除角色",
                            tint = RoseError,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// POLICIES TAB CONTENT (Moved to Repository Settings)
// =========================================================================
@Composable
fun PoliciesTabContent(repo: Repository, enterprise: Enterprise?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "儲存庫階層治理政策",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextHighEmphasis,
        )

        PolicySettingCard(
            title = "Multi-Signature Approver Quorum",
            description = "Requires ${repo.requiredApproverCount} distinct Approver sign-offs before any artifact can transition from Pending Sign-Off to Approved / Published.",
            isActive = true,
            icon = Icons.Default.Gavel,
            accentColor = EmeraldSuccess,
        )

        PolicySettingCard(
            title = "Mandatory Peer Review Gate",
            description = "Artifacts must first pass Reviewer inspection (Decision = APPROVED) before Approvers are permitted to sign off.",
            isActive = repo.requireReviewerPass,
            icon = Icons.Default.Policy,
            accentColor = AmberWarning,
        )

        PolicySettingCard(
            title = "Segregation of Duties (Anti-Self-Approval)",
            description = "The author who created or updated the draft is strictly barred from approving or reviewing their own proposal.",
            isActive = repo.preventSelfApproval,
            icon = Icons.Default.Security,
            accentColor = LavenderPrimary,
        )

        PolicySettingCard(
            title = "Enterprise Owner Constraint",
            description = "Strictly enforces that only an Organization or User can be assigned as the Owner of this container. 個團隊 cannot own repositories.",
            isActive = true,
            icon = Icons.Default.Lock,
            accentColor = CyanAccent,
        )
    }
}

@Composable
fun PolicySettingCard(title: String, description: String, isActive: Boolean, icon: ImageVector, accentColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) Color(0xFF064E3B) else Color(0xFF334155))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = if (isActive) "ENFORCED" else "DISABLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = if (isActive) EmeraldSuccess else TextMediumEmphasis,
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

// =========================================================================
// REPO AUDIT LOGS (Moved to Repository Settings)
// =========================================================================
@Composable
fun RepoAuditTabContent(repo: Repository, auditLogs: List<AuditLog>) {
    val repoLogs = auditLogs.filter { it.repoId == repo.id }
    if (repoLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "此儲存庫尚無稽核事件紀錄。",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMediumEmphasis,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(repoLogs) { log ->
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                val dateStr = dateFormat.format(Date(log.timestamp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = log.actionName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = LavenderPrimary,
                            )
                            PolicyVerdictBadge(verdict = log.verdict)
                        }

                        Text(
                            text = log.reasoning,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHighEmphasis,
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 16.sp,
                        )

                        Text(
                            text = "執行者：${log.actorDisplayName} • $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLowEmphasis,
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// DIALOGS (Create Artifact & Add Access Rule)
// =========================================================================
@Composable
fun CreateArtifactDialog(onDismiss: () -> Unit, onCreate: (String, ArtifactType, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ArtifactType.PROCESS_WORKFLOW) }
    var summary by remember { mutableStateOf("") }
    var structuredContent by remember {
        mutableStateOf(
            """
            {
              "process_name": "Sample Automated Blueprint",
              "trigger": "Incoming Customer Request Event",
              "stages": [
                {"step": 1, "action": "Validate Request Payload"},
                {"step": 2, "action": "Route to Department Reviewer"},
                {"step": 3, "action": "Execute No-Code Transaction"}
              ]
            }
            """.trimIndent(),
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = SophisticatedSurfaceDark,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, SophisticatedBorder),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "新增無程式碼成果／藍圖",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("成果標題") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("artifact_title_input"),
                )

                Text(
                    text = "成果結構類型",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                )

                // Types chooser
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ArtifactType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SophisticatedContainer else SophisticatedSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) LavenderPrimary else SophisticatedBorder,
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable { selectedType = type }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary),
                            )
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = TextHighEmphasis,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("摘要") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("artifact_summary_input"),
                )

                OutlinedTextField(
                    value = structuredContent,
                    onValueChange = { structuredContent = it },
                    label = { Text("結構化無程式碼藍圖（JSON／Schema）") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("artifact_content_input"),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = TextMediumEmphasis)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title, selectedType, summary, structuredContent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("submit_create_artifact_button"),
                    ) {
                        Text("建立草稿", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccessRuleDialog(
    allUsers: List<User>,
    allTeams: List<Team>,
    existingRules: List<RepoAccessRule>,
    onDismiss: () -> Unit,
    onAddRule: (GranteeType, String, String, RepoRole) -> Unit,
) {
    var selectedGranteeType by remember { mutableStateOf(GranteeType.USER) }
    var selectedGranteeId by remember {
        mutableStateOf(allUsers.firstOrNull()?.id ?: "")
    }
    var selectedRole by remember { mutableStateOf(RepoRole.COLLABORATOR) }

    val selectedGranteeName = when (selectedGranteeType) {
        GranteeType.USER -> allUsers.firstOrNull { it.id == selectedGranteeId }?.displayName ?: ""
        GranteeType.TEAM -> allTeams.firstOrNull { it.id == selectedGranteeId }?.name ?: ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = SophisticatedSurfaceDark,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, SophisticatedBorder),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "指派存取角色",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )

                // Grantee Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            selectedGranteeType = GranteeType.USER
                            selectedGranteeId = allUsers.firstOrNull()?.id ?: ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedGranteeType ==
                                GranteeType.USER
                            ) {
                                    LavenderPrimary
                                } else {
                                    SophisticatedContainer
                                },
                            contentColor = if (selectedGranteeType ==
                                GranteeType.USER
                            ) {
                                    LavenderOnPrimary
                                } else {
                                    TextHighEmphasis
                                },
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("使用者實體")
                    }

                    Button(
                        onClick = {
                            selectedGranteeType = GranteeType.TEAM
                            selectedGranteeId = allTeams.firstOrNull()?.id ?: ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedGranteeType ==
                                GranteeType.TEAM
                            ) {
                                    LavenderPrimary
                                } else {
                                    SophisticatedContainer
                                },
                            contentColor = if (selectedGranteeType ==
                                GranteeType.TEAM
                            ) {
                                    LavenderOnPrimary
                                } else {
                                    TextHighEmphasis
                                },
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("團隊實體")
                    }
                }

                // Grantee Selection List
                Text(
                    "選擇 ${selectedGranteeType.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (selectedGranteeType == GranteeType.USER) {
                        allUsers.forEach { u ->
                            val isSelected = u.id == selectedGranteeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) SophisticatedContainer else SophisticatedSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) LavenderPrimary else SophisticatedBorder,
                                        RoundedCornerShape(6.dp),
                                    )
                                    .clickable { selectedGranteeId = u.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedGranteeId = u.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary),
                                )
                                Text(
                                    u.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextHighEmphasis,
                                )
                            }
                        }
                    } else {
                        allTeams.forEach { t ->
                            val isSelected = t.id == selectedGranteeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) SophisticatedContainer else SophisticatedSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) LavenderPrimary else SophisticatedBorder,
                                        RoundedCornerShape(6.dp),
                                    )
                                    .clickable { selectedGranteeId = t.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedGranteeId = t.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary),
                                )
                                Text(t.name, style = MaterialTheme.typography.bodySmall, color = TextHighEmphasis)
                            }
                        }
                    }
                }

                // Role Selection
                Text("指派階層角色", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        RepoRole.MAINTAINER,
                        RepoRole.APPROVER,
                        RepoRole.REVIEWER,
                        RepoRole.COLLABORATOR,
                        RepoRole.VIEWER,
                    ).forEach { r ->
                        val isSelected = selectedRole == r
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SophisticatedContainer else SophisticatedSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) LavenderPrimary else SophisticatedBorder,
                                    RoundedCornerShape(6.dp),
                                )
                                .clickable { selectedRole = r }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedRole = r },
                                colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary),
                            )
                            Column {
                                Text(
                                    r.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextHighEmphasis,
                                )
                                Text(
                                    r.description,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMediumEmphasis,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedGranteeId.isNotBlank()) {
                                onAddRule(selectedGranteeType, selectedGranteeId, selectedGranteeName, selectedRole)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("授予角色", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = SophisticatedContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMediumEmphasis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextLowEmphasis,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
fun HomologousThreeViewSwitcher(
    selectedTab: RepoWorkspaceTab,
    onTabSelect: (RepoWorkspaceTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = SophisticatedSurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SophisticatedBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                Triple(RepoWorkspaceTab.WBS, "WBS 樹狀", Icons.Default.AccountTree),
                Triple(RepoWorkspaceTab.KANBAN, "Kanban 看板", Icons.Default.ViewWeek),
                Triple(RepoWorkspaceTab.ISSUE, "Issue 清單", Icons.Default.TaskAlt),
            ).forEach { (tab, label, icon) ->
                val isSelected = selectedTab == tab
                Surface(
                    onClick = { onTabSelect(tab) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) LavenderPrimary else Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) LavenderOnPrimary else TextMediumEmphasis,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                            ),
                            color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RepoKanbanView(
    issues: List<RepoIssue>,
    onIssueClick: (RepoIssue) -> Unit,
    onUpdateStatus: (String, IssueStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openIssues = remember(issues) { issues.filter { it.status == IssueStatus.OPEN } }
    val inProgressIssues = remember(issues) { issues.filter { it.status == IssueStatus.IN_PROGRESS } }
    val closedIssues = remember(issues) { issues.filter { it.status == IssueStatus.CLOSED } }

    val columns = listOf(
        Triple("待處理", openIssues, IssueStatus.OPEN),
        Triple("進行中", inProgressIssues, IssueStatus.IN_PROGRESS),
        Triple("已完成", closedIssues, IssueStatus.CLOSED),
    )

    LazyRow(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
    ) {
        items(columns) { (title, columnIssues, status) ->
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                border = BorderStroke(1.dp, SophisticatedBorder),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (status) {
                                            IssueStatus.OPEN -> TextMediumEmphasis
                                            IssueStatus.IN_PROGRESS -> LavenderPrimary
                                            IssueStatus.CLOSED -> EmeraldSuccess
                                        },
                                    ),
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextHighEmphasis,
                            )
                        }
                        Surface(
                            color = SophisticatedContainer,
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                text = "${columnIssues.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LavenderGlow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    if (columnIssues.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "無 $title 項目",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLowEmphasis,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(columnIssues, key = { it.id }) { issue ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onIssueClick(issue) },
                                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                    border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "#${issue.issueNumber}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = LavenderPrimary,
                                            )
                                            Surface(
                                                color = when (issue.priority) {
                                                    IssuePriority.CRITICAL -> RoseError.copy(alpha = 0.2f)
                                                    IssuePriority.HIGH -> AmberWarning.copy(alpha = 0.2f)
                                                    IssuePriority.MEDIUM -> LavenderContainer
                                                    IssuePriority.LOW -> SophisticatedContainer
                                                },
                                                shape = RoundedCornerShape(4.dp),
                                            ) {
                                                Text(
                                                    text = issue.priority.label,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                    color = when (issue.priority) {
                                                        IssuePriority.CRITICAL -> RoseError
                                                        IssuePriority.HIGH -> AmberWarning
                                                        IssuePriority.MEDIUM -> LavenderPrimary
                                                        IssuePriority.LOW -> TextMediumEmphasis
                                                    },
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                )
                                            }
                                        }

                                        Text(
                                            text = issue.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = TextHighEmphasis,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )

                                        if (issue.progressPercent > 0) {
                                            LinearProgressIndicator(
                                                progress = { issue.progressPercent / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = LavenderPrimary,
                                                trackColor = SophisticatedContainer,
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = issue.assigneeName ?: "未指派",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = TextMediumEmphasis,
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (status != IssueStatus.OPEN) {
                                                    IconButton(
                                                        onClick = {
                                                            val prev = if (status ==
                                                                IssueStatus.CLOSED
                                                            ) {
                                                                    IssueStatus.IN_PROGRESS
                                                                } else {
                                                                    IssueStatus.OPEN
                                                                }
                                                            onUpdateStatus(issue.id, prev)
                                                        },
                                                        modifier = Modifier.size(24.dp),
                                                    ) {
                                                        Icon(
                                                            Icons.AutoMirrored.Filled.ArrowBack,
                                                            contentDescription = "上一步",
                                                            tint = TextMediumEmphasis,
                                                            modifier = Modifier.size(14.dp),
                                                        )
                                                    }
                                                }
                                                if (status != IssueStatus.CLOSED) {
                                                    IconButton(
                                                        onClick = {
                                                            val next = if (status ==
                                                                IssueStatus.OPEN
                                                            ) {
                                                                    IssueStatus.IN_PROGRESS
                                                                } else {
                                                                    IssueStatus.CLOSED
                                                                }
                                                            onUpdateStatus(issue.id, next)
                                                        },
                                                        modifier = Modifier.size(24.dp),
                                                    ) {
                                                        Icon(
                                                            Icons.AutoMirrored.Filled.ArrowForward,
                                                            contentDescription = "下一步",
                                                            tint = LavenderPrimary,
                                                            modifier = Modifier.size(14.dp),
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
                }
            }
        }
    }
}
