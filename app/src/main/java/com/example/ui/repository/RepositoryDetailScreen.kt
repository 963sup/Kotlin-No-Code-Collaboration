package com.example.ui.repository

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.RepoDiscussionsSection
import com.example.ui.components.RepoIssuesSection
import com.example.ui.screens.RepositoryWbsSection
import com.example.ui.theme.*
import com.example.ui.work.KanbanBoardView

enum class RepoWorkspaceTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("總覽", Icons.Default.Dashboard),
    WBS("WBS", Icons.Default.AccountTree),
    KANBAN("看板", Icons.Default.ViewWeek),
    ISSUE("Issue", Icons.Default.TaskAlt),
    ARTIFACTS("文件/成果", Icons.Default.Description),
    DISCUSSIONS("討論", Icons.Default.Forum),
    ACCESS("存取權限", Icons.Default.Security),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryDetailScreen(
    repo: Repository,
    enterprise: Enterprise?,
    artifacts: List<NoCodeArtifact>,
    issues: List<RepoIssue> = emptyList(),
    dependencies: List<IssueDependency> = emptyList(),
    discussions: List<RepoDiscussion> = emptyList(),
    issueComments: List<IssueComment> = emptyList(),
    discussionComments: List<DiscussionComment> = emptyList(),
    accessRules: List<RepoAccessRule> = emptyList(),
    allUsers: List<User> = emptyList(),
    allTeams: List<Team> = emptyList(),
    allOrgMemberships: List<OrgMembership> = emptyList(),
    allTeamMemberships: List<TeamMembership> = emptyList(),
    allAuditLogs: List<AuditLog> = emptyList(),
    activeUser: User?,
    onBack: () -> Unit,
    onNavigateToIssue: (RepoIssue) -> Unit,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
    onCreateArtifact: (String, ArtifactType, String, String, (Boolean) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onAddAccessRule: (GranteeType, String, String, RepoRole) -> Unit = { _, _, _, _ -> },
    onRemoveAccessRule: (RepoAccessRule) -> Unit = {},
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
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onLinkParentIssue: (issueId: String, parentIssueId: String?, () -> Unit) -> Unit = { _, _, _ -> },
    onAddDependency: (repoId: String, blockedIssueId: String, blockingIssueId: String, () -> Unit) -> Unit = { _, _, _, _ -> },
    onRemoveDependency: (dependencyId: String, () -> Unit) -> Unit = { _, _ -> },
    onAddIssueComment: (issueId: String, content: String, () -> Unit) -> Unit = { _, _, _ -> },
    onUpdateIssueStatus: (issueId: String, newStatus: IssueStatus) -> Unit = { _, _ -> },
    onUpdateIssuePlan: (String, Int, Long?, Long?, Double, Int) -> Unit = { _, _, _, _, _, _ -> },
    onAssignIssue: (issueId: String, assigneeType: GranteeType?, assigneeId: String?, assigneeName: String?) -> Unit = { _, _, _, _ -> },
    onLoadIssueComments: (issueId: String) -> Unit = {},
    onCreateDiscussion: (title: String, category: DiscussionCategory, body: String, () -> Unit) -> Unit = { _, _, _, _ -> },
    onAddDiscussionComment: (discussionId: String, content: String, () -> Unit) -> Unit = { _, _, _ -> },
    onToggleLockDiscussion: (discussionId: String) -> Unit = {},
    onMarkAcceptedAnswer: (discussionId: String, commentId: String) -> Unit = { _, _ -> },
    onUpvoteDiscussion: (discussionId: String) -> Unit = {},
    onUpvoteDiscussionComment: (commentId: String, discussionId: String) -> Unit = { _, _ -> },
    onLoadDiscussionComments: (discussionId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(RepoWorkspaceTab.OVERVIEW) }
    var showCreateArtifactDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = repo.displayName.ifBlank { repo.name },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = repo.description.ifBlank { "無程式碼協作專案容器" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("repo_back_btn")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = TextHighEmphasis,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SophisticatedSurfaceDark),
            )
        },
        containerColor = SophisticatedBg,
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // Scrollable Tab Row for workspace sections
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = SophisticatedSurfaceDark,
                contentColor = LavenderPrimary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (selectedTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = LavenderPrimary,
                        )
                    }
                },
            ) {
                RepoWorkspaceTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = tab.label,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                )
                            }
                        },
                        selectedContentColor = LavenderPrimary,
                        unselectedContentColor = TextMediumEmphasis,
                        modifier = Modifier.testTag("repo_tab_${tab.name.lowercase()}"),
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                when (selectedTab) {
                    RepoWorkspaceTab.OVERVIEW -> {
                        RepositoryOverviewTab(
                            repository = repo,
                            onNavigateTab = { tabIndex ->
                                if (tabIndex in 0 until RepoWorkspaceTab.values().size) {
                                    selectedTab = RepoWorkspaceTab.values()[tabIndex]
                                }
                            },
                        )
                    }

                    RepoWorkspaceTab.WBS -> {
                        RepositoryWbsSection(
                            issues = issues,
                            onUpdatePlan = onUpdateIssuePlan,
                        )
                    }

                    RepoWorkspaceTab.KANBAN -> {
                        KanbanBoardView(
                            issues = issues,
                            onSelectIssue = onNavigateToIssue,
                        )
                    }

                    RepoWorkspaceTab.ISSUE -> {
                        RepoIssuesSection(
                            repo = repo,
                            issues = issues,
                            dependencies = dependencies,
                            selectedIssueComments = issueComments,
                            allUsers = allUsers,
                            allTeams = allTeams,
                            repoArtifacts = artifacts,
                            activeUser = activeUser,
                            canCreateIssue = true,
                            onIssueClick = onNavigateToIssue,
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

                    RepoWorkspaceTab.ARTIFACTS -> {
                        RepoArtifactsSection(
                            artifacts = artifacts,
                            onSelectArtifact = onSelectArtifact,
                            onCreateArtifactClick = { showCreateArtifactDialog = true },
                        )
                    }

                    RepoWorkspaceTab.DISCUSSIONS -> {
                        RepoDiscussionsSection(
                            repo = repo,
                            discussions = discussions,
                            selectedDiscussionComments = discussionComments,
                            activeUser = activeUser,
                            effectiveRole = RepoRole.MAINTAINER,
                            canCreateDiscussion = true,
                            onCreateDiscussion = onCreateDiscussion,
                            onAddComment = onAddDiscussionComment,
                            onToggleLock = onToggleLockDiscussion,
                            onMarkAcceptedAnswer = onMarkAcceptedAnswer,
                            onUpvoteDiscussion = onUpvoteDiscussion,
                            onUpvoteComment = onUpvoteDiscussionComment,
                            onLoadComments = onLoadDiscussionComments,
                        )
                    }

                    RepoWorkspaceTab.ACCESS -> {
                        RepoAccessSection(
                            accessRules = accessRules,
                            allUsers = allUsers,
                            allTeams = allTeams,
                            onAddRule = onAddAccessRule,
                            onRemoveRule = onRemoveAccessRule,
                        )
                    }
                }
            }
        }
    }

    if (showCreateArtifactDialog) {
        CreateArtifactDialog(
            onDismiss = { showCreateArtifactDialog = false },
            onCreate = { title, type, summary, content ->
                onCreateArtifact(title, type, summary, content) {
                    showCreateArtifactDialog = false
                }
            },
        )
    }
}

@Composable
private fun RepoArtifactsSection(
    artifacts: List<NoCodeArtifact>,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
    onCreateArtifactClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "成果與文件 (${artifacts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
            )
            Button(
                onClick = onCreateArtifactClick,
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("create_artifact_btn"),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("新增成果物", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (artifacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "目前尚無任何文件或成果物",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMediumEmphasis,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(artifacts) { artifact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectArtifact(artifact) },
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        shape = RoundedCornerShape(12.dp),
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Surface(
                                    color = LavenderContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = LavenderPrimary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = artifact.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHighEmphasis,
                                    )
                                    Text(
                                        text = artifact.summary.ifBlank { "類型: ${artifact.type.name}" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMediumEmphasis,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Surface(
                                color = when (artifact.lifecycleState) {
                                    LifecycleState.PUBLISHED -> EmeraldDark
                                    LifecycleState.APPROVED -> LavenderContainer
                                    LifecycleState.IN_REVIEW -> AmberGlow
                                    else -> SophisticatedContainer
                                },
                                shape = RoundedCornerShape(6.dp),
                            ) {
                                Text(
                                    text = artifact.lifecycleState.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = when (artifact.lifecycleState) {
                                        LifecycleState.PUBLISHED -> EmeraldSuccess
                                        LifecycleState.APPROVED -> LavenderPrimary
                                        LifecycleState.IN_REVIEW -> AmberWarning
                                        else -> TextMediumEmphasis
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoAccessSection(
    accessRules: List<RepoAccessRule>,
    allUsers: List<User>,
    allTeams: List<Team>,
    onAddRule: (GranteeType, String, String, RepoRole) -> Unit,
    onRemoveRule: (RepoAccessRule) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "協作成員與權限 (${accessRules.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_access_rule_btn"),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("新增授權", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(accessRules) { rule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    shape = RoundedCornerShape(12.dp),
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (rule.granteeType == GranteeType.USER) LavenderContainer else AmberGlow,
                                        CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (rule.granteeType == GranteeType.USER) Icons.Default.Person else Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = if (rule.granteeType == GranteeType.USER) LavenderPrimary else AmberWarning,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = rule.granteeName.ifBlank { rule.granteeId },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighEmphasis,
                                )
                                Text(
                                    text = "${if (rule.granteeType == GranteeType.USER) "個人" else "團隊"} • 角色: ${rule.role.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumEmphasis,
                                )
                            }
                        }

                        IconButton(
                            onClick = { onRemoveRule(rule) },
                            modifier = Modifier.testTag("remove_rule_${rule.id}"),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "移除",
                                tint = RoseError,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAccessRuleDialog(
            allUsers = allUsers,
            allTeams = allTeams,
            onDismiss = { showAddDialog = false },
            onAdd = { type, id, name, role ->
                onAddRule(type, id, name, role)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun CreateArtifactDialog(
    onDismiss: () -> Unit,
    onCreate: (String, ArtifactType, String, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ArtifactType.SPECIFICATION_DOC) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增無程式碼成果物 / 文件", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("成果物名稱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("成果物摘要") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("內容 / 描述") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, selectedType, summary, content)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
            ) {
                Text("建立")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun AddAccessRuleDialog(
    allUsers: List<User>,
    allTeams: List<Team>,
    onDismiss: () -> Unit,
    onAdd: (GranteeType, String, String, RepoRole) -> Unit,
) {
    var isUser by remember { mutableStateOf(true) }
    var selectedUserId by remember { mutableStateOf(allUsers.firstOrNull()?.id ?: "") }
    var selectedTeamId by remember { mutableStateOf(allTeams.firstOrNull()?.id ?: "") }
    var selectedRole by remember { mutableStateOf(RepoRole.COLLABORATOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增倉庫授權", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isUser = true },
                    ) {
                        RadioButton(selected = isUser, onClick = { isUser = true })
                        Text("指定個人")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isUser = false },
                    ) {
                        RadioButton(selected = !isUser, onClick = { isUser = false })
                        Text("指定團隊")
                    }
                }

                if (isUser) {
                    Text("選擇使用者:", style = MaterialTheme.typography.labelMedium)
                    allUsers.take(5).forEach { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUserId = user.id },
                        ) {
                            RadioButton(
                                selected = selectedUserId == user.id,
                                onClick = { selectedUserId = user.id },
                            )
                            Text(user.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text("選擇團隊:", style = MaterialTheme.typography.labelMedium)
                    allTeams.take(5).forEach { team ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTeamId = team.id },
                        ) {
                            RadioButton(
                                selected = selectedTeamId == team.id,
                                onClick = { selectedTeamId = team.id },
                            )
                            Text(team.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                HorizontalDivider()

                Text("選擇角色權限:", style = MaterialTheme.typography.labelMedium)
                listOf(RepoRole.VIEWER, RepoRole.COLLABORATOR, RepoRole.REVIEWER, RepoRole.APPROVER, RepoRole.MAINTAINER, RepoRole.OWNER).forEach { role ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = role },
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                        )
                        Text(role.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isUser) {
                        val user = allUsers.firstOrNull { it.id == selectedUserId }
                        if (user != null) {
                            onAdd(GranteeType.USER, user.id, user.displayName, selectedRole)
                        }
                    } else {
                        val team = allTeams.firstOrNull { it.id == selectedTeamId }
                        if (team != null) {
                            onAdd(GranteeType.TEAM, team.id, team.name, selectedRole)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
            ) {
                Text("確認授權")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
