package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.WorkspaceScopeKind
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    scopeKind: WorkspaceScopeKind? = null,
    scopeName: String? = null,
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
    notifications: List<AppNotification> = emptyList(),
    auditLogs: List<AuditLog>,
    unreadNotificationCount: Int,
    onNavigateToRepository: (Repository) -> Unit,
    onNavigateToArtifact: (Repository, NoCodeArtifact) -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToRepositoriesCatalog: () -> Unit,
    onNavigateToMe: () -> Unit,
    onSwitchPersonaClick: () -> Unit,
    onMarkNotificationAsRead: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Derived states
    val title = scopeName ?: enterprise?.name ?: "企業總覽"
    val inProgressCount = remember(allIssues) {
        val count = allIssues.count { it.status == IssueStatus.IN_PROGRESS }
        if (count == 0) 24 else count
    }
    val pendingCount = remember(allIssues) {
        val count = allIssues.count { it.status == IssueStatus.OPEN }
        if (count == 0) 18 else count
    }
    val toVerifyCount = 6

    val activeRepos = remember(repositories) { repositories.sortedByDescending { it.updatedAt }.take(4) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Text(
                        text = "無代碼協同作業管理平台 v3",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                    )
                }
                IconButton(
                    onClick = onSwitchPersonaClick,
                    modifier = Modifier.testTag("home_persona_switch_btn"),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LavenderPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "切換人員角色",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        // Enterprise Health & Progress Card
        item {
            Text(
                text = "企業健康度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                border = BorderStroke(1.dp, SophisticatedBorder),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Circle Progress & trend badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                            CircularProgressIndicator(
                                progress = { 0.68f },
                                modifier = Modifier.fillMaxSize(),
                                color = LavenderPrimary,
                                trackColor = SophisticatedBorder,
                                strokeWidth = 8.dp,
                            )
                            Text(
                                "68%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldDark,
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                        ) {
                            Text(
                                "較上週 ↑12%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    // Stats in column
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        HealthStatItem("進行中", inProgressCount.toString(), LavenderPrimary)
                        HealthStatItem("待處理", pendingCount.toString(), AmberWarning)
                        HealthStatItem("待驗證", toVerifyCount.toString(), RoseError)
                    }
                }
            }
        }

        // Active Repositories
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "活躍倉庫 ${if (repositories.isEmpty()) 12 else repositories.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldDark,
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Text(
                            "7",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberGlow,
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Text(
                            "3",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RoseDark,
                    ) {
                        Text(
                            "2",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseError,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    "查看全部",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = LavenderPrimary,
                    modifier = Modifier.clickable { onNavigateToRepositoriesCatalog() },
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (activeRepos.isEmpty()) {
                    DefaultRepoCardItem(
                        "製造邊優化專案",
                        "WBS 60%",
                        "Issue 18",
                        0.6f,
                        onClick = onNavigateToRepositoriesCatalog,
                    )
                    DefaultRepoCardItem(
                        "設備檢修管理系統",
                        "WBS 75%",
                        "Issue 12",
                        0.75f,
                        onClick = onNavigateToRepositoriesCatalog,
                    )
                    DefaultRepoCardItem(
                        "客服流程優化專案",
                        "WBS 45%",
                        "Issue 9",
                        0.45f,
                        onClick = onNavigateToRepositoriesCatalog,
                    )
                } else {
                    activeRepos.forEach { repo ->
                        RepoListItem(repo = repo, onClick = { onNavigateToRepository(repo) })
                    }
                }
            }
        }

        // Recent Activity
        item {
            Text(
                "最近活動",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ActivityItem(
                        "Sarah Chen",
                        "製造邊優化專案",
                        "指派了 #128 Issue 給王小明",
                        "2 小時前",
                        Icons.Default.AssignmentInd,
                        LavenderPrimary,
                    )
                    HorizontalDivider(color = SophisticatedBorderSubtle)
                    ActivityItem(
                        "Marcus Wong",
                        "設備檢修管理系統",
                        "WBS 從 30% -> 45%",
                        "3 小時前",
                        Icons.Default.TrendingUp,
                        EmeraldSuccess,
                    )
                    HorizontalDivider(color = SophisticatedBorderSubtle)
                    ActivityItem(
                        "Elena Rostova",
                        "客服流程優化專案",
                        "完成驗證 Issue #56",
                        "5 小時前",
                        Icons.Default.Verified,
                        CyanAccent,
                    )
                }
            }
        }
    }
}

@Composable
fun HealthStatItem(label: String, value: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accentColor)
    }
}

@Composable
fun RepoListItem(repo: Repository, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("repo_card_${repo.id}"),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LavenderContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    repo.displayName.ifEmpty { repo.name },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "WBS 60%",
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Issue 18", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                }
            }
            // Member Avatar pile
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                Box(
                    modifier = Modifier.size(
                        24.dp,
                    ).background(Color(0xFFEF4444), CircleShape).border(1.5.dp, SophisticatedSurfaceDark, CircleShape),
                )
                Box(
                    modifier = Modifier.size(
                        24.dp,
                    ).background(Color(0xFF3B82F6), CircleShape).border(1.5.dp, SophisticatedSurfaceDark, CircleShape),
                )
                Box(
                    modifier = Modifier.size(
                        24.dp,
                    ).background(Color(0xFF10B981), CircleShape).border(1.5.dp, SophisticatedSurfaceDark, CircleShape),
                )
            }
        }
    }
}

@Composable
fun DefaultRepoCardItem(title: String, wbsText: String, issueText: String, progress: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LavenderContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        wbsText,
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(issueText, style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                }
            }
            // Member Avatar pile
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                Box(
                    modifier = Modifier.size(
                        24.dp,
                    ).background(Color(0xFFEF4444), CircleShape).border(1.5.dp, SophisticatedSurfaceDark, CircleShape),
                )
                Box(
                    modifier = Modifier.size(
                        24.dp,
                    ).background(Color(0xFF3B82F6), CircleShape).border(1.5.dp, SophisticatedSurfaceDark, CircleShape),
                )
                Box(
                    modifier = Modifier.size(
                        24.dp,
                    ).background(Color(0xFF10B981), CircleShape).border(1.5.dp, SophisticatedSurfaceDark, CircleShape),
                )
            }
        }
    }
}

@Composable
fun ActivityItem(
    userName: String,
    repoName: String,
    actionText: String,
    timeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    repoName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                )
                Text(timeText, style = MaterialTheme.typography.labelSmall, color = TextLowEmphasis)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "$userName $actionText",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
            )
        }
    }
}
