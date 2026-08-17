package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.Enterprise
import com.example.data.model.GranteeType
import com.example.data.model.IssueDependency
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDark
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

enum class HomeWorkFilter(val label: String) {
    ASSIGNED_ISSUES("Assigned Issues"),
    PENDING_REVIEWS("Review Requests"),
    PENDING_APPROVALS("Approval Requests"),
    RECENT_ACTIVITY("Activity Trail")
}

@Composable
fun HomeScreen(
    activeUser: User?,
    enterprise: Enterprise?,
    organizations: List<Organization>,
    teams: List<Team>,
    repositories: List<Repository>,
    allArtifacts: List<NoCodeArtifact>,
    allIssues: List<RepoIssue>,
    allDiscussions: List<RepoDiscussion>,
    allDependencies: List<IssueDependency>,
    allReviews: List<ArtifactReview>,
    allApprovals: List<ArtifactApproval>,
    allAccessRules: List<RepoAccessRule>,
    allOrgMemberships: List<OrgMembership>,
    allTeamMemberships: List<TeamMembership>,
    auditLogs: List<AuditLog>,
    unreadNotificationCount: Int,
    onNavigateToRepository: (Repository) -> Unit,
    onNavigateToArtifact: (Repository, NoCodeArtifact) -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToRepositoriesCatalog: () -> Unit,
    onNavigateToMe: () -> Unit,
    onSwitchPersonaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWorkFilter by remember { mutableStateOf(HomeWorkFilter.ASSIGNED_ISSUES) }

    // Calculate user's team IDs
    val userTeamIds = remember(activeUser, allTeamMemberships) {
        if (activeUser == null) emptySet()
        else allTeamMemberships.filter { it.userId == activeUser.id }.map { it.teamId }.toSet()
    }

    // Issues assigned to active user or user's teams
    val assignedIssues = remember(activeUser, allIssues, userTeamIds) {
        if (activeUser == null) emptyList()
        else allIssues.filter { issue ->
            (issue.assigneeType == GranteeType.USER && issue.assigneeId == activeUser.id) ||
                    (issue.assigneeType == GranteeType.TEAM && userTeamIds.contains(issue.assigneeId)) ||
                    (issue.authorUserId == activeUser.id && issue.status != IssueStatus.CLOSED)
        }.sortedWith(compareByDescending<RepoIssue> { it.status != IssueStatus.CLOSED }.thenByDescending { it.updatedAt })
    }

    // Artifacts waiting for review where user is reviewer/collaborator
    val pendingReviewArtifacts = remember(activeUser, allArtifacts, allReviews, repositories) {
        if (activeUser == null) emptyList()
        else {
            allArtifacts.filter { art ->
                art.lifecycleState == LifecycleState.IN_REVIEW
            }
        }
    }

    // Artifacts waiting for approval
    val pendingApprovalArtifacts = remember(activeUser, allArtifacts, allApprovals, repositories) {
        if (activeUser == null) emptyList()
        else {
            allArtifacts.filter { art ->
                art.lifecycleState == LifecycleState.PENDING_APPROVAL
            }
        }
    }

    // Recent accessible repositories
    val accessibleRepositories = remember(activeUser, repositories, allAccessRules, allOrgMemberships, userTeamIds) {
        if (activeUser == null) emptyList()
        else {
            repositories.take(6)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =========================================================================
        // 1. HOME HERO BANNER: ATTENTION & IDENTITY SUMMARY
        // =========================================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                border = BorderStroke(1.dp, SophisticatedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(activeUser?.avatarColorHex ?: "#8B5CF6"))
                                        } catch (e: Exception) {
                                            LavenderPrimary
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (activeUser?.displayName?.take(1) ?: "U").uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = activeUser?.displayName ?: "Guest User",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = TextHighEmphasis
                                )
                                Text(
                                    text = "${activeUser?.title ?: "Collaborator"} • ${enterprise?.name ?: "Enterprise"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumEmphasis
                                )
                            }
                        }

                        // Persona quick-switch chip
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSwitchPersonaClick() }
                                .testTag("home_switch_persona_btn"),
                            color = SophisticatedContainer,
                            border = BorderStroke(1.dp, SophisticatedBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Switch Persona",
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Switch",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = LavenderPrimary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SophisticatedBorderSubtle, thickness = 1.dp)

                    // Work routing attention metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttentionMetricPill(
                            icon = Icons.Default.TaskAlt,
                            label = "Assigned",
                            count = assignedIssues.count { it.status != IssueStatus.CLOSED },
                            accentColor = if (assignedIssues.any { it.priority == IssuePriority.CRITICAL }) RoseError else LavenderPrimary,
                            isSelected = selectedWorkFilter == HomeWorkFilter.ASSIGNED_ISSUES,
                            onClick = { selectedWorkFilter = HomeWorkFilter.ASSIGNED_ISSUES },
                            modifier = Modifier.weight(1f)
                        )
                        AttentionMetricPill(
                            icon = Icons.Default.RateReview,
                            label = "Reviews",
                            count = pendingReviewArtifacts.size,
                            accentColor = AmberWarning,
                            isSelected = selectedWorkFilter == HomeWorkFilter.PENDING_REVIEWS,
                            onClick = { selectedWorkFilter = HomeWorkFilter.PENDING_REVIEWS },
                            modifier = Modifier.weight(1f)
                        )
                        AttentionMetricPill(
                            icon = Icons.Default.Approval,
                            label = "Approvals",
                            count = pendingApprovalApprovalCount(pendingApprovalArtifacts),
                            accentColor = EmeraldSuccess,
                            isSelected = selectedWorkFilter == HomeWorkFilter.PENDING_APPROVALS,
                            onClick = { selectedWorkFilter = HomeWorkFilter.PENDING_APPROVALS },
                            modifier = Modifier.weight(1f)
                        )
                        AttentionMetricPill(
                            icon = Icons.Default.Notifications,
                            label = "Inbox",
                            count = unreadNotificationCount,
                            accentColor = LavenderPrimary,
                            isSelected = false,
                            onClick = onNavigateToInbox,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 2. WORK ROUTING SECTION: ASSIGNED WORK & REVIEWS
        // =========================================================================
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Work & Attention",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextHighEmphasis
                    )
                    Text(
                        text = "${assignedIssues.count { it.status != IssueStatus.CLOSED }} active items",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis
                    )
                }

                // Filter row for attention list
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(HomeWorkFilter.values()) { filter ->
                        val count = when (filter) {
                            HomeWorkFilter.ASSIGNED_ISSUES -> assignedIssues.count { it.status != IssueStatus.CLOSED }
                            HomeWorkFilter.PENDING_REVIEWS -> pendingReviewArtifacts.size
                            HomeWorkFilter.PENDING_APPROVALS -> pendingApprovalArtifacts.size
                            HomeWorkFilter.RECENT_ACTIVITY -> auditLogs.take(5).size
                        }
                        FilterChip(
                            selected = selectedWorkFilter == filter,
                            onClick = { selectedWorkFilter = filter },
                            label = {
                                Text(
                                    text = "${filter.label} ($count)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LavenderPrimary,
                                selectedLabelColor = LavenderOnPrimary,
                                containerColor = SophisticatedSurfaceDark,
                                labelColor = TextMediumEmphasis
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedWorkFilter == filter,
                                borderColor = if (selectedWorkFilter == filter) LavenderPrimary else SophisticatedBorder
                            ),
                            modifier = Modifier.testTag("home_filter_${filter.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Selected filter content
        when (selectedWorkFilter) {
            HomeWorkFilter.ASSIGNED_ISSUES -> {
                if (assignedIssues.isEmpty()) {
                    item {
                        EmptyHomeStateCard(
                            icon = Icons.Default.TaskAlt,
                            title = "No Assigned Issues",
                            subtitle = "You have no pending or blocked action items assigned at this time."
                        )
                    }
                } else {
                    items(assignedIssues.take(6)) { issue ->
                        val repo = repositories.firstOrNull { it.id == issue.repoId }
                        val blockers = allDependencies.filter { it.blockedIssueId == issue.id }
                        HomeIssueCard(
                            issue = issue,
                            repo = repo,
                            blockers = blockers,
                            allIssues = allIssues,
                            onClick = {
                                if (repo != null) {
                                    onNavigateToRepository(repo)
                                }
                            }
                        )
                    }
                }
            }

            HomeWorkFilter.PENDING_REVIEWS -> {
                if (pendingReviewArtifacts.isEmpty()) {
                    item {
                        EmptyHomeStateCard(
                            icon = Icons.Default.RateReview,
                            title = "No Pending Reviews",
                            subtitle = "All blueprints and schemas in your scope have completed review."
                        )
                    }
                } else {
                    items(pendingReviewArtifacts.take(6)) { artifact ->
                        val repo = repositories.firstOrNull { it.id == artifact.repoId }
                        HomeArtifactReviewCard(
                            artifact = artifact,
                            repo = repo,
                            onClick = {
                                if (repo != null) {
                                    onNavigateToArtifact(repo, artifact)
                                }
                            }
                        )
                    }
                }
            }

            HomeWorkFilter.PENDING_APPROVALS -> {
                if (pendingApprovalArtifacts.isEmpty()) {
                    item {
                        EmptyHomeStateCard(
                            icon = Icons.Default.Approval,
                            title = "No Pending Approvals",
                            subtitle = "There are currently no artifacts awaiting multi-signatory approval."
                        )
                    }
                } else {
                    items(pendingApprovalArtifacts.take(6)) { artifact ->
                        val repo = repositories.firstOrNull { it.id == artifact.repoId }
                        HomeArtifactReviewCard(
                            artifact = artifact,
                            repo = repo,
                            isApproval = true,
                            onClick = {
                                if (repo != null) {
                                    onNavigateToArtifact(repo, artifact)
                                }
                            }
                        )
                    }
                }
            }

            HomeWorkFilter.RECENT_ACTIVITY -> {
                if (auditLogs.isEmpty()) {
                    item {
                        EmptyHomeStateCard(
                            icon = Icons.Default.History,
                            title = "No Activity Logs",
                            subtitle = "Recent governance events and collaboration activity will appear here."
                        )
                    }
                } else {
                    items(auditLogs.take(6)) { log ->
                        val repo = repositories.firstOrNull { it.id == log.repoId }
                        HomeActivityCard(log = log, repo = repo)
                    }
                }
            }
        }

        // =========================================================================
        // 3. RECENT REPOSITORIES SECTION
        // =========================================================================
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collaboration Repositories",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextHighEmphasis
                    )
                    TextButton(
                        onClick = onNavigateToRepositoriesCatalog,
                        modifier = Modifier.testTag("home_view_all_repos_btn")
                    ) {
                        Text(
                            text = "View All (${repositories.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LavenderPrimary
                        )
                    }
                }

                if (accessibleRepositories.isEmpty()) {
                    EmptyHomeStateCard(
                        icon = Icons.Default.Folder,
                        title = "No Repositories Available",
                        subtitle = "Create your first No-Code schema or blueprint container to get started."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        accessibleRepositories.forEach { repo ->
                            val repoArtifactCount = allArtifacts.count { it.repoId == repo.id }
                            val repoIssueCount = allIssues.count { it.repoId == repo.id }
                            HomeRepositoryRowCard(
                                repo = repo,
                                artifactCount = repoArtifactCount,
                                issueCount = repoIssueCount,
                                onClick = { onNavigateToRepository(repo) }
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 4. QUICK ROUTING & SHORTCUTS
        // =========================================================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                border = BorderStroke(1.dp, SophisticatedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Navigation",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextMediumEmphasis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToRepositoriesCatalog,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_quick_repos_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SophisticatedBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Schemas", color = TextHighEmphasis)
                        }

                        OutlinedButton(
                            onClick = onNavigateToMe,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_quick_me_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SophisticatedBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("My Space", color = TextHighEmphasis)
                        }
                    }
                }
            }
        }
    }
}

private fun pendingApprovalApprovalCount(list: List<NoCodeArtifact>): Int = list.size

@Composable
fun AttentionMetricPill(
    icon: ImageVector,
    label: String,
    count: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("metric_pill_${label.lowercase()}"),
        color = if (isSelected) SophisticatedContainer else SophisticatedSurface,
        border = BorderStroke(
            1.dp,
            if (isSelected) accentColor else SophisticatedBorderSubtle
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (count > 0) TextHighEmphasis else TextLowEmphasis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMediumEmphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeIssueCard(
    issue: RepoIssue,
    repo: Repository?,
    blockers: List<IssueDependency>,
    allIssues: List<RepoIssue>,
    onClick: () -> Unit
) {
    val isBlocked = blockers.isNotEmpty()
    val isClosed = issue.status == IssueStatus.CLOSED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_issue_${issue.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, if (isBlocked) AmberWarning.copy(alpha = 0.5f) else SophisticatedBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${issue.issueNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = LavenderPrimary
                    )
                    if (repo != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SophisticatedContainer
                        ) {
                            Text(
                                text = repo.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextMediumEmphasis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Priority Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (issue.priority) {
                        IssuePriority.CRITICAL -> RoseError.copy(alpha = 0.2f)
                        IssuePriority.HIGH -> AmberWarning.copy(alpha = 0.2f)
                        IssuePriority.MEDIUM -> LavenderContainer
                        IssuePriority.LOW -> SophisticatedContainer
                    }
                ) {
                    Text(
                        text = issue.priority.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = when (issue.priority) {
                            IssuePriority.CRITICAL -> RoseError
                            IssuePriority.HIGH -> AmberWarning
                            IssuePriority.MEDIUM -> LavenderPrimary
                            IssuePriority.LOW -> TextMediumEmphasis
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = issue.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isClosed) TextLowEmphasis else TextHighEmphasis,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Blocker indicator if present
            if (isBlocked) {
                val blockingNumbers = blockers.mapNotNull { dep ->
                    allIssues.firstOrNull { it.id == dep.blockingIssueId }?.issueNumber
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberWarning.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Blocked",
                            tint = AmberWarning,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (blockingNumbers.isNotEmpty()) "Blocked by #${blockingNumbers.joinToString(", #")}" else "Blocked by upstream dependency",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            color = AmberWarning
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${issue.status.name.replace("_", " ")}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = when (issue.status) {
                        IssueStatus.OPEN -> LavenderPrimary
                        IssueStatus.IN_PROGRESS -> AmberWarning
                        IssueStatus.CLOSED -> TextLowEmphasis
                    }
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(issue.updatedAt)),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextLowEmphasis
                )
            }
        }
    }
}

@Composable
fun HomeArtifactReviewCard(
    artifact: NoCodeArtifact,
    repo: Repository?,
    isApproval: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_review_${artifact.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, if (isApproval) EmeraldSuccess.copy(alpha = 0.4f) else AmberWarning.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isApproval) Icons.Default.Approval else Icons.Default.RateReview,
                        contentDescription = null,
                        tint = if (isApproval) EmeraldSuccess else AmberWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isApproval) "Approval Request" else "Review Request",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isApproval) EmeraldSuccess else AmberWarning
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SophisticatedContainer
                ) {
                    Text(
                        text = artifact.type.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = LavenderPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = artifact.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextHighEmphasis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Container: ${repo?.displayName ?: "Repository"}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMediumEmphasis
                )
                Text(
                    text = "Version: v${artifact.version}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = TextLowEmphasis
                )
            }
        }
    }
}

@Composable
fun HomeActivityCard(
    log: AuditLog,
    repo: Repository?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SophisticatedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${log.actorDisplayName} • ${log.actionName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextHighEmphasis
                )
                Text(
                    text = log.reasoning.ifEmpty { "Governance event recorded" },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMediumEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextLowEmphasis
            )
        }
    }
}

@Composable
fun HomeRepositoryRowCard(
    repo: Repository,
    artifactCount: Int,
    issueCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("home_repo_${repo.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LavenderContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextHighEmphasis
                )
                Text(
                    text = "${repo.ownerDisplayName} • $artifactCount Blueprints • $issueCount Issues",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMediumEmphasis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open Repository",
                tint = TextMediumEmphasis,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyHomeStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMediumEmphasis,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextHighEmphasis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis
            )
        }
    }
}
