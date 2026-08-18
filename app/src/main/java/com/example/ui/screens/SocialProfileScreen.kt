package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AuditLog
import com.example.data.model.SavedTarget
import com.example.data.model.SyncStatusSummary
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget
import com.example.navigation.CollaborationTargetAccess
import com.example.ui.model.AchievementProjection
import com.example.ui.model.FollowingActivityProjection
import com.example.ui.model.SavedProjection
import com.example.ui.model.SocialProjection
import com.example.ui.viewmodel.GovernanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SocialProfileTab(val label: String) {
    OVERVIEW("總覽"),
    FOLLOWERS("追隨者"),
    FOLLOWING("追蹤中"),
    ACTIVITY("追蹤動態"),
    SAVED("收藏"),
    ACHIEVEMENTS("成就")
}

@Composable
fun SocialProfileScreen(
    profileUser: User,
    activeUser: User,
    auditLogs: List<AuditLog>,
    visibleRepositoryIds: Set<String>,
    follows: List<UserFollow>,
    savedTargets: List<SavedTarget>,
    syncStatus: SyncStatusSummary,
    onToggleFollow: (String) -> Unit,
    onSyncNow: () -> Unit
) {
    val governanceViewModel: GovernanceViewModel = viewModel()
    val users by governanceViewModel.users.collectAsState()
    val repositories by governanceViewModel.repositories.collectAsState()
    val organizations by governanceViewModel.organizations.collectAsState()
    val teams by governanceViewModel.teams.collectAsState()
    val artifacts by governanceViewModel.allArtifacts.collectAsState()
    val issues by governanceViewModel.allIssues.collectAsState()
    val discussions by governanceViewModel.allDiscussions.collectAsState()
    val reviews by governanceViewModel.allReviews.collectAsState()
    val allAccessRules by governanceViewModel.allAccessRules.collectAsState()
    val allOrgMemberships by governanceViewModel.allOrgMemberships.collectAsState()
    val allTeamMemberships by governanceViewModel.allTeamMemberships.collectAsState()

    var tabName by rememberSaveable(profileUser.id) { mutableStateOf(SocialProfileTab.OVERVIEW.name) }
    val selectedTab = SocialProfileTab.valueOf(tabName)
    val stats = remember(profileUser, auditLogs, visibleRepositoryIds) {
        SocialProjection.stats(profileUser, auditLogs, visibleRepositoryIds)
    }
    val usersById = remember(users, activeUser.enterpriseId) {
        users.filter { it.enterpriseId == activeUser.enterpriseId }.associateBy { it.id }
    }
    val followerUsers = remember(profileUser.id, follows, usersById) {
        follows.filter { it.followedUserId == profileUser.id }
            .mapNotNull { usersById[it.followerUserId] }
            .distinctBy { it.id }
            .sortedBy { it.displayName }
    }
    val followingUsers = remember(profileUser.id, follows, usersById) {
        follows.filter { it.followerUserId == profileUser.id }
            .mapNotNull { usersById[it.followedUserId] }
            .distinctBy { it.id }
            .sortedBy { it.displayName }
    }
    val isFollowing = follows.any { it.followerUserId == activeUser.id && it.followedUserId == profileUser.id }
    val activity = remember(activeUser.id, follows, auditLogs, visibleRepositoryIds) {
        FollowingActivityProjection.build(activeUser.id, follows, auditLogs, visibleRepositoryIds, limit = 40)
    }
    val savedGroups = remember(activeUser.id, savedTargets) {
        SavedProjection.build(activeUser.id, savedTargets)
    }
    val achievements = remember(profileUser.id, issues, reviews, artifacts, auditLogs, visibleRepositoryIds) {
        AchievementProjection.build(profileUser, issues, reviews, artifacts, auditLogs, visibleRepositoryIds)
    }
    val repositoriesById = remember(
        repositories,
        activeUser,
        allOrgMemberships,
        allTeamMemberships,
        allAccessRules
    ) {
        repositories.filter { repository ->
            CollaborationTargetAccess.canOpenRepository(
                user = activeUser,
                repository = repository,
                orgMemberships = allOrgMemberships,
                teamMemberships = allTeamMemberships,
                accessRules = allAccessRules
            )
        }.associateBy { it.id }
    }
    val accessibleRepositoryIds = remember(repositoriesById) { repositoriesById.keys }
    val organizationsById = remember(organizations, activeUser.enterpriseId) {
        organizations.filter { it.enterpriseId == activeUser.enterpriseId }.associateBy { it.id }
    }
    val teamsById = remember(teams, organizationsById) {
        teams.filter { it.orgId in organizationsById }.associateBy { it.id }
    }
    val artifactsById = remember(artifacts, accessibleRepositoryIds) {
        artifacts.filter { it.repoId in accessibleRepositoryIds }.associateBy { it.id }
    }
    val issuesById = remember(issues, accessibleRepositoryIds) {
        issues.filter { it.repoId in accessibleRepositoryIds }.associateBy { it.id }
    }
    val discussionsById = remember(discussions, accessibleRepositoryIds) {
        discussions.filter { it.repoId in accessibleRepositoryIds }.associateBy { it.id }
    }

    fun openTarget(target: CollaborationTarget) {
        when (target) {
            is CollaborationTarget.Repository -> repositoriesById[target.repositoryId]
                ?.let(governanceViewModel::selectRepository)
            is CollaborationTarget.Artifact -> {
                val repository = repositoriesById[target.repositoryId]
                val artifact = artifactsById[target.artifactId]
                if (repository != null && artifact?.repoId == repository.id) {
                    governanceViewModel.selectRepository(repository)
                    governanceViewModel.selectArtifact(artifact)
                }
            }
            is CollaborationTarget.Issue -> repositoriesById[target.repositoryId]
                ?.let(governanceViewModel::selectRepository)
            is CollaborationTarget.Discussion -> repositoriesById[target.repositoryId]
                ?.let(governanceViewModel::selectRepository)
            is CollaborationTarget.UserProfile -> usersById[target.userId]
                ?.let(governanceViewModel::selectProfileUser)
            is CollaborationTarget.Organization,
            is CollaborationTarget.Team -> Unit
        }
    }

    Column(Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("social_profile_header")
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(profileUser.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("@${profileUser.username} · ${profileUser.title}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Lv.${stats.level} · XP ${stats.xp} · 公開協作 ${stats.publicActions}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("追隨者 ${followerUsers.size} · 追蹤中 ${followingUsers.size} · 已解鎖 ${achievements.count { it.unlocked }} 個成就")
                if (profileUser.id != activeUser.id) {
                    OutlinedButton(
                        onClick = { onToggleFollow(profileUser.id) },
                        modifier = Modifier.testTag("profile_follow")
                    ) {
                        Text(if (isFollowing) "取消追蹤" else "追蹤")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(onClick = onSyncNow, modifier = Modifier.testTag("profile_sync_now")) {
                            Text("立即同步")
                        }
                        Text(
                            "待同步 ${syncStatus.pending} · 衝突 ${syncStatus.conflicts} · 失敗 ${syncStatus.failed}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).testTag("social_profile_tabs"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SocialProfileTab.entries, key = { it.name }) { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { tabName = tab.name },
                    label = { Text(tab.label) },
                    modifier = Modifier.testTag("social_tab_${tab.name.lowercase()}")
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedTab) {
                SocialProfileTab.OVERVIEW -> {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("身份", fontWeight = FontWeight.Bold)
                                Text(profileUser.bio)
                                Text(
                                    "${profileUser.location} · ${profileUser.pronouns}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("成就摘要", fontWeight = FontWeight.Bold)
                                achievements.filter { it.unlocked }.take(3).forEach { achievement ->
                                    Text("✓ ${achievement.badge.title} · ${achievement.badge.description}")
                                }
                                if (achievements.none { it.unlocked }) {
                                    Text("尚未解鎖成就；完成可驗證協作行為後會自動產生。")
                                }
                            }
                        }
                    }
                }

                SocialProfileTab.FOLLOWERS -> {
                    if (followerUsers.isEmpty()) {
                        item { CollaborationEmptyStateCard("尚無追隨者", "其他用戶追蹤此身份後會顯示在這裡。") }
                    } else {
                        items(followerUsers, key = { it.id }) { user ->
                            SocialPersonCard(user) { governanceViewModel.selectProfileUser(user) }
                        }
                    }
                }

                SocialProfileTab.FOLLOWING -> {
                    if (followingUsers.isEmpty()) {
                        item { CollaborationEmptyStateCard("尚未追蹤任何人", "從探索或用戶資料頁開始追蹤協作者。") }
                    } else {
                        items(followingUsers, key = { it.id }) { user ->
                            SocialPersonCard(user) { governanceViewModel.selectProfileUser(user) }
                        }
                    }
                }

                SocialProfileTab.ACTIVITY -> {
                    if (activity.isEmpty()) {
                        item { CollaborationEmptyStateCard("沒有可公開的追蹤動態", "只會顯示 PublicActivityPolicy 允許且你有權查看的事件。") }
                    } else {
                        items(activity, key = { "${it.actorUserId}:${it.timestamp}:${it.actionName}" }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    usersById[item.actorUserId]?.let(governanceViewModel::selectProfileUser)
                                }
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(item.actorDisplayName, fontWeight = FontWeight.SemiBold)
                                    Text(item.actionName.replace('_', ' '), color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "${item.repositoryName} · ${formatSocialTime(item.timestamp)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                SocialProfileTab.SAVED -> {
                    if (profileUser.id != activeUser.id) {
                        item { CollaborationEmptyStateCard("收藏為私人資料", "只能查看目前有效身份自己的收藏。") }
                    } else if (savedGroups.isEmpty()) {
                        item { CollaborationEmptyStateCard("尚無收藏", "在探索中使用星號收藏原始協作目標。") }
                    } else {
                        savedGroups.forEach { group ->
                            item(key = "saved_group_${group.group.name}") {
                                Text("${group.group.label}（${group.targets.size}）", fontWeight = FontWeight.Bold)
                            }
                            items(group.targets, key = { it.toString() }) { target ->
                                val label = resolveSavedLabel(
                                    target,
                                    repositoriesById,
                                    artifactsById,
                                    issuesById,
                                    discussionsById,
                                    organizationsById,
                                    teamsById,
                                    usersById
                                )
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { openTarget(target) }
                                        .testTag("saved_target_${target.toString().hashCode()}")
                                ) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(
                                            label.first,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            label.second,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SocialProfileTab.ACHIEVEMENTS -> {
                    items(achievements, key = { it.badge.name }) { achievement ->
                        Card(Modifier.fillMaxWidth().testTag("achievement_${achievement.badge.name.lowercase()}")) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    if (achievement.unlocked) "✓ ${achievement.badge.title}" else achievement.badge.title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (achievement.unlocked) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(achievement.badge.description, style = MaterialTheme.typography.bodySmall)
                                LinearProgressIndicator(
                                    progress = {
                                        (achievement.evidenceCount.toFloat() / achievement.badge.requiredEvidence)
                                            .coerceIn(0f, 1f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "${achievement.evidenceCount}/${achievement.badge.requiredEvidence}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun SocialPersonCard(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .testTag("social_user_${user.id}")
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(user.displayName, fontWeight = FontWeight.SemiBold)
            Text(
                "@${user.username} · ${user.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun resolveSavedLabel(
    target: CollaborationTarget,
    repositories: Map<String, com.example.data.model.Repository>,
    artifacts: Map<String, com.example.data.model.NoCodeArtifact>,
    issues: Map<String, com.example.data.model.RepoIssue>,
    discussions: Map<String, com.example.data.model.RepoDiscussion>,
    organizations: Map<String, com.example.data.model.Organization>,
    teams: Map<String, com.example.data.model.Team>,
    users: Map<String, User>
): Pair<String, String> = when (target) {
    is CollaborationTarget.Repository -> repositories[target.repositoryId]
        ?.let { it.displayName to "儲存庫" } ?: "無法開啟" to "儲存庫已不存在或權限已撤銷"
    is CollaborationTarget.Artifact -> artifacts[target.artifactId]
        ?.let { it.title to (repositories[target.repositoryId]?.displayName ?: "成果") }
        ?: "無法開啟" to "成果已不存在或權限已撤銷"
    is CollaborationTarget.Issue -> issues[target.issueId]
        ?.let { "#${it.issueNumber} ${it.title}" to (repositories[target.repositoryId]?.displayName ?: "工作") }
        ?: "無法開啟" to "工作已不存在或權限已撤銷"
    is CollaborationTarget.Discussion -> discussions[target.discussionId]
        ?.let { it.title to (repositories[target.repositoryId]?.displayName ?: "討論") }
        ?: "無法開啟" to "討論已不存在或權限已撤銷"
    is CollaborationTarget.Organization -> organizations[target.organizationId]
        ?.let { it.name to "組織" } ?: "無法開啟" to "組織已不存在或不在目前企業"
    is CollaborationTarget.Team -> teams[target.teamId]
        ?.let { it.name to "團隊" } ?: "無法開啟" to "團隊已不存在或不在目前企業"
    is CollaborationTarget.UserProfile -> users[target.userId]
        ?.let { it.displayName to "@${it.username}" } ?: "無法開啟" to "用戶已不存在或不在目前企業"
}

private fun formatSocialTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.TAIWAN).format(Date(timestamp))
