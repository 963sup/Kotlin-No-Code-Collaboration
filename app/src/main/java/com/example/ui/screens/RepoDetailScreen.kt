package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ArtifactType
import com.example.data.model.AuditLog
import com.example.data.model.DiscussionCategory
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.GovernanceAction
import com.example.data.model.GranteeType
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.PolicyVerdict
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
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.RoseError
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onSelectArtifact: (NoCodeArtifact) -> Unit,
    onCreateArtifact: (String, ArtifactType, String, String, (Boolean) -> Unit) -> Unit,
    onAddAccessRule: (GranteeType, String, String, RepoRole) -> Unit,
    onRemoveAccessRule: (RepoAccessRule) -> Unit,
    onCreateIssue: (title: String, desc: String, priority: IssuePriority, assigneeType: GranteeType?, assigneeId: String?, assigneeName: String?, linkedArtifactId: String?, linkedArtifactTitle: String?, parentIssueId: String?, labels: String, () -> Unit) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onLinkParentIssue: (issueId: String, parentIssueId: String?, () -> Unit) -> Unit = { _, _, _ -> },
    onAddDependency: (repoId: String, blockedIssueId: String, blockingIssueId: String, () -> Unit) -> Unit = { _, _, _, _ -> },
    onRemoveDependency: (dependencyId: String, () -> Unit) -> Unit = { _, _ -> },
    onAddIssueComment: (issueId: String, content: String, () -> Unit) -> Unit = { _, _, _ -> },
    onUpdateIssueStatus: (issueId: String, newStatus: IssueStatus) -> Unit = { _, _ -> },
    onAssignIssue: (issueId: String, assigneeType: GranteeType?, assigneeId: String?, assigneeName: String?) -> Unit = { _, _, _, _ -> },
    onLoadIssueComments: (issueId: String) -> Unit = {},
    onCreateDiscussion: (title: String, category: DiscussionCategory, body: String, () -> Unit) -> Unit = { _, _, _, _ -> },
    onAddDiscussionComment: (discussionId: String, content: String, () -> Unit) -> Unit = { _, _, _ -> },
    onToggleLockDiscussion: (discussionId: String) -> Unit = {},
    onMarkAcceptedAnswer: (discussionId: String, commentId: String) -> Unit = { _, _ -> },
    onUpvoteDiscussion: (discussionId: String) -> Unit = {},
    onUpvoteDiscussionComment: (commentId: String, discussionId: String) -> Unit = { _, _ -> },
    onLoadDiscussionComments: (discussionId: String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateArtifactDialog by remember { mutableStateOf(false) }
    var showAddAccessRuleDialog by remember { mutableStateOf(false) }

    val effectiveRolePair = if (activeUser != null) {
        HierarchicalPolicyEngine.resolveEffectiveRole(
            actor = activeUser,
            repo = repo,
            orgMemberships = allOrgMemberships,
            teamMemberships = allTeamMemberships,
            teams = allTeams,
            accessRules = accessRules
        )
    } else Pair(RepoRole.VIEWER, "Default")

    val effectiveRole = effectiveRolePair.first
    val roleSource = effectiveRolePair.second
    val canManageAccess = effectiveRole.canPerform(RepoRole.MAINTAINER)
    val canCreateArtifact = effectiveRole.canPerform(RepoRole.COLLABORATOR)
    val canCreateIssue = effectiveRole.canPerform(RepoRole.COLLABORATOR)
    val canCreateDiscussion = effectiveRole.canPerform(RepoRole.COLLABORATOR)

    val tabs = listOf(
        "Blueprints (${artifacts.size})",
        "Issues (${issues.size})",
        "Discussions (${discussions.size})",
        "Access Hierarchy (${accessRules.size})",
        "Policies & Gates",
        "Audit Logs"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Repo Header
            RepoDetailHeader(
                repo = repo,
                enterprise = enterprise,
                effectiveRole = effectiveRole,
                roleSource = roleSource,
                onBack = onBack
            )

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SlateDark900,
                contentColor = IndigoLight,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = IndigoLight,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selectedTab == index) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> ArtifactsTabContent(
                    artifacts = artifacts,
                    onSelectArtifact = onSelectArtifact
                )
                1 -> RepoIssuesSection(
                    repo = repo,
                    issues = issues,
                    dependencies = dependencies,
                    selectedIssueComments = issueComments,
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
                    onSelectArtifact = onSelectArtifact
                )
                2 -> RepoDiscussionsSection(
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
                    onLoadComments = onLoadDiscussionComments
                )
                3 -> AccessHierarchyTabContent(
                    accessRules = accessRules,
                    canManageAccess = canManageAccess,
                    onAddRule = { showAddAccessRuleDialog = true },
                    onRemoveRule = onRemoveAccessRule
                )
                4 -> PoliciesTabContent(repo = repo, enterprise = enterprise)
                5 -> RepoAuditTabContent(repo = repo, auditLogs = allAuditLogs)
            }
        }

        // Floating Action Button (Only if on tab 0 and user is Collaborator+)
        if (selectedTab == 0 && canCreateArtifact) {
            FloatingActionButton(
                onClick = { showCreateArtifactDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_artifact_fab"),
                containerColor = CyanAccent,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add No-Code Artifact")
                    Text("New Blueprint", fontWeight = FontWeight.Bold)
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
            }
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
            }
        )
    }
}

@Composable
fun RepoDetailHeader(
    repo: Repository,
    enterprise: Enterprise?,
    effectiveRole: RepoRole,
    roleSource: String,
    onBack: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateDark900),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Navigation Bar / Breadcrumbs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp).testTag("back_to_repos_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = IndigoLight)
                }

                Text(
                    text = "${enterprise?.name ?: "Enterprise"} > ${repo.ownerDisplayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repo.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanAccent
                    )
                }

                RoleBadge(role = effectiveRole)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Owner & Role Source Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OwnerTypeTag(ownerType = repo.ownerType, ownerDisplayName = repo.ownerDisplayName)

                Text(
                    text = "Role via: $roleSource",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ArtifactsTabContent(
    artifacts: List<NoCodeArtifact>,
    onSelectArtifact: (NoCodeArtifact) -> Unit
) {
    if (artifacts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No blueprints or documents created yet in this container.\nClick '+ New Blueprint' to create a specification or workflow.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(artifacts) { artifact ->
                ArtifactCardItem(
                    artifact = artifact,
                    onClick = { onSelectArtifact(artifact) }
                )
            }
        }
    }
}

@Composable
fun ArtifactCardItem(
    artifact: NoCodeArtifact,
    onClick: () -> Unit
) {
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
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = artifact.type.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CyanAccent
                    )
                }

                LifecycleBadge(state = artifact.lifecycleState)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = artifact.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Text(
                text = artifact.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                maxLines = 2,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Author: ${artifact.authorDisplayName} • ${artifact.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )

                if (artifact.lockedByPolicy) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                        Text("Locked", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                    }
                }
            }
        }
    }
}

@Composable
fun AccessHierarchyTabContent(
    accessRules: List<RepoAccessRule>,
    canManageAccess: Boolean,
    onAddRule: () -> Unit,
    onRemoveRule: (RepoAccessRule) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Collaborators & Team Access Mappings",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Hierarchical permissions mapped to Users and Teams",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (canManageAccess) {
                    Button(
                        onClick = onAddRule,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_access_rule_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Role", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (accessRules.isEmpty()) {
            item {
                EmptyStateCard(message = "No explicit access rules configured.")
            }
        } else {
            items(accessRules) { rule ->
                AccessRuleCardItem(
                    rule = rule,
                    canDelete = canManageAccess,
                    onDelete = { onRemoveRule(rule) }
                )
            }
        }
    }
}

@Composable
fun AccessRuleCardItem(
    rule: RepoAccessRule,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val isTeam = rule.granteeType == GranteeType.TEAM
    val icon = if (isTeam) Icons.Default.Groups else Icons.Default.Person
    val tint = if (isTeam) IndigoLight else CyanAccent

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = rule.granteeName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = if (isTeam) "Team Group Grant" else "Individual User Grant",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleBadge(role = rule.role)
                if (canDelete && rule.role != RepoRole.OWNER) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Role",
                            tint = RoseError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PoliciesTabContent(repo: Repository, enterprise: Enterprise?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Repository Hierarchical Governance Policies",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        PolicySettingCard(
            title = "Multi-Signature Approver Quorum",
            description = "Requires ${repo.requiredApproverCount} distinct Approver sign-offs before any artifact can transition from Pending Sign-Off to Approved / Published.",
            isActive = true,
            icon = Icons.Default.Gavel,
            accentColor = EmeraldSuccess
        )

        PolicySettingCard(
            title = "Mandatory Peer Review Gate",
            description = "Artifacts must first pass Reviewer inspection (Decision = APPROVED) before Approvers are permitted to sign off.",
            isActive = repo.requireReviewerPass,
            icon = Icons.Default.Policy,
            accentColor = AmberWarning
        )

        PolicySettingCard(
            title = "Segregation of Duties (Anti-Self-Approval)",
            description = "The author who created or updated the draft is strictly barred from approving or reviewing their own proposal.",
            isActive = repo.preventSelfApproval,
            icon = Icons.Default.Security,
            accentColor = PurpleGlow
        )

        PolicySettingCard(
            title = "Enterprise Owner Constraint",
            description = "Strictly enforces that only an Organization or User can be assigned as the Owner of this container. Teams cannot own repositories.",
            isActive = true,
            icon = Icons.Default.Lock,
            accentColor = CyanAccent
        )
    }
}

@Composable
fun PolicySettingCard(
    title: String,
    description: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) Color(0xFF064E3B) else Color(0xFF334155))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isActive) "ENFORCED" else "DISABLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isActive) EmeraldSuccess else Color(0xFF94A3B8)
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun RepoAuditTabContent(repo: Repository, auditLogs: List<AuditLog>) {
    val repoLogs = auditLogs.filter { it.repoId == repo.id }
    if (repoLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No audit log events recorded for this repository yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(repoLogs) { log ->
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                val dateStr = dateFormat.format(Date(log.timestamp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = log.actionName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = IndigoLight
                            )
                            PolicyVerdictBadge(verdict = log.verdict)
                        }

                        Text(
                            text = log.reasoning,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 16.sp
                        )

                        Text(
                            text = "Actor: ${log.actorDisplayName} • $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateArtifactDialog(
    onDismiss: () -> Unit,
    onCreate: (String, ArtifactType, String, String) -> Unit
) {
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
            """.trimIndent()
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = SlateDark900,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "New No-Code Artifact / Blueprint",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Artifact Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("artifact_title_input")
                )

                Text(
                    text = "Artifact Schema Type",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )

                // Types chooser
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ArtifactType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A))
                                .border(1.dp, if (isSelected) IndigoLight else Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .clickable { selectedType = type }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = IndigoLight)
                            )
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Executive Summary") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("artifact_summary_input")
                )

                OutlinedTextField(
                    value = structuredContent,
                    onValueChange = { structuredContent = it },
                    label = { Text("Structured No-Code Blueprint (JSON / Schema)") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("artifact_content_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title, selectedType, summary, structuredContent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("submit_create_artifact_button")
                    ) {
                        Text("Create Draft", fontWeight = FontWeight.Bold)
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
    onAddRule: (GranteeType, String, String, RepoRole) -> Unit
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
            color = SlateDark900,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Assign Access Role",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Grantee Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedGranteeType = GranteeType.USER
                            selectedGranteeId = allUsers.firstOrNull()?.id ?: ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedGranteeType == GranteeType.USER) IndigoPrimary else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("User Entity")
                    }

                    Button(
                        onClick = {
                            selectedGranteeType = GranteeType.TEAM
                            selectedGranteeId = allTeams.firstOrNull()?.id ?: ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedGranteeType == GranteeType.TEAM) IndigoPrimary else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Team Entity")
                    }
                }

                // Grantee Selection List
                Text("Select ${selectedGranteeType.name}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (selectedGranteeType == GranteeType.USER) {
                        allUsers.forEach { u ->
                            val isSelected = u.id == selectedGranteeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A))
                                    .clickable { selectedGranteeId = u.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedGranteeId = u.id })
                                Text(u.displayName, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    } else {
                        allTeams.forEach { t ->
                            val isSelected = t.id == selectedGranteeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A))
                                    .clickable { selectedGranteeId = t.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedGranteeId = t.id })
                                Text(t.name, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    }
                }

                // Role Selection
                Text("Assign Hierarchical Role", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        RepoRole.MAINTAINER,
                        RepoRole.APPROVER,
                        RepoRole.REVIEWER,
                        RepoRole.COLLABORATOR,
                        RepoRole.VIEWER
                    ).forEach { r ->
                        val isSelected = selectedRole == r
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A))
                                .clickable { selectedRole = r }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { selectedRole = r })
                            Column {
                                Text(r.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text(r.description, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF94A3B8)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedGranteeId.isNotBlank()) {
                                onAddRule(selectedGranteeType, selectedGranteeId, selectedGranteeName, selectedRole)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Grant Role")
                    }
                }
            }
        }
    }
}
