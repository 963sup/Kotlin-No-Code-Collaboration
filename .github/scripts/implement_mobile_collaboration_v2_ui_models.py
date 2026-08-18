from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def write(rel: str, content: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")


write(
    "app/src/main/java/com/example/navigation/CollaborationTargetStorage.kt",
    r'''
package com.example.navigation

import com.example.data.model.CollaborationTargetType
import com.example.data.model.SavedTarget

fun CollaborationTarget.storageType(): String = when (this) {
    is CollaborationTarget.Repository -> CollaborationTargetType.REPOSITORY
    is CollaborationTarget.Artifact -> CollaborationTargetType.ARTIFACT
    is CollaborationTarget.Issue -> CollaborationTargetType.ISSUE
    is CollaborationTarget.Discussion -> CollaborationTargetType.DISCUSSION
    is CollaborationTarget.Organization -> CollaborationTargetType.ORGANIZATION
    is CollaborationTarget.Team -> CollaborationTargetType.TEAM
    is CollaborationTarget.UserProfile -> CollaborationTargetType.USER
}

fun CollaborationTarget.storageId(): String = when (this) {
    is CollaborationTarget.Repository -> repositoryId
    is CollaborationTarget.Artifact -> artifactId
    is CollaborationTarget.Issue -> issueId
    is CollaborationTarget.Discussion -> discussionId
    is CollaborationTarget.Organization -> organizationId
    is CollaborationTarget.Team -> teamId
    is CollaborationTarget.UserProfile -> userId
}

fun CollaborationTarget.storageRepositoryId(): String = when (this) {
    is CollaborationTarget.Repository -> repositoryId
    is CollaborationTarget.Artifact -> repositoryId
    is CollaborationTarget.Issue -> repositoryId
    is CollaborationTarget.Discussion -> repositoryId
    else -> ""
}

fun CollaborationTarget.storageKey(): String =
    "${storageType()}:${storageRepositoryId()}:${storageId()}"

fun CollaborationTarget.toSavedTarget(userId: String): SavedTarget = SavedTarget(
    userId = userId,
    targetKey = storageKey(),
    targetType = storageType(),
    targetId = storageId(),
    repositoryId = storageRepositoryId()
)

fun SavedTarget.toCollaborationTarget(): CollaborationTarget? = when (targetType) {
    CollaborationTargetType.REPOSITORY -> CollaborationTarget.Repository(targetId)
    CollaborationTargetType.ARTIFACT -> repositoryId.takeIf(String::isNotBlank)
        ?.let { CollaborationTarget.Artifact(it, targetId) }
    CollaborationTargetType.ISSUE -> repositoryId.takeIf(String::isNotBlank)
        ?.let { CollaborationTarget.Issue(it, targetId) }
    CollaborationTargetType.DISCUSSION -> repositoryId.takeIf(String::isNotBlank)
        ?.let { CollaborationTarget.Discussion(it, targetId) }
    CollaborationTargetType.ORGANIZATION -> CollaborationTarget.Organization(targetId)
    CollaborationTargetType.TEAM -> CollaborationTarget.Team(targetId)
    CollaborationTargetType.USER -> CollaborationTarget.UserProfile(targetId)
    else -> null
}
'''
)

write(
    "app/src/main/java/com/example/ui/model/ExperienceProjections.kt",
    r'''
package com.example.ui.model

import com.example.data.model.AppNotification
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.NoCodeArtifact
import com.example.data.model.NotificationStatus
import com.example.data.model.Organization
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.User
import com.example.navigation.CollaborationTarget
import com.example.navigation.storageKey
import com.example.ui.components.WorkspaceScopeKind


data class ExploreResult(
    val target: CollaborationTarget,
    val typeLabel: String,
    val title: String,
    val subtitle: String,
    val searchableText: String,
    val score: Int,
    val isSaved: Boolean
)

object ExploreProjection {
    fun build(
        activeUser: User?,
        repositories: List<Repository>,
        artifacts: List<NoCodeArtifact>,
        issues: List<RepoIssue>,
        discussions: List<RepoDiscussion>,
        organizations: List<Organization>,
        teams: List<Team>,
        users: List<User>,
        savedTargets: List<SavedTarget>,
        now: Long = System.currentTimeMillis()
    ): List<ExploreResult> {
        val user = activeUser ?: return emptyList()
        val repoById = repositories.associateBy { it.id }
        val savedKeys = savedTargets.filter { it.userId == user.id }.map { it.targetKey }.toSet()
        fun recency(updatedAt: Long): Int =
            (30 - ((now - updatedAt).coerceAtLeast(0L) / 86_400_000L).toInt()).coerceIn(0, 30)
        fun item(
            target: CollaborationTarget,
            type: String,
            title: String,
            subtitle: String,
            search: String,
            updatedAt: Long
        ) = ExploreResult(
            target = target,
            typeLabel = type,
            title = title,
            subtitle = subtitle,
            searchableText = search,
            score = recency(updatedAt),
            isSaved = target.storageKey() in savedKeys
        )

        val repoItems = repositories.map { repo ->
            item(
                CollaborationTarget.Repository(repo.id), "儲存庫", repo.displayName,
                repo.description.ifBlank { "無程式碼協作容器" },
                "${repo.name} ${repo.displayName} ${repo.description} ${repo.category}", repo.updatedAt
            )
        }
        val issueItems = issues.filter { it.repoId in repoById }.map { issue ->
            val repo = repoById.getValue(issue.repoId)
            item(
                CollaborationTarget.Issue(issue.repoId, issue.id), "Issue",
                "#${issue.issueNumber} ${issue.title}", "${repo.displayName} · ${issue.status.label}",
                "${issue.title} ${issue.description} ${issue.labels} ${repo.displayName}", issue.updatedAt
            )
        }
        val artifactItems = artifacts.filter { it.repoId in repoById }.map { artifact ->
            val repo = repoById.getValue(artifact.repoId)
            item(
                CollaborationTarget.Artifact(artifact.repoId, artifact.id), "成果", artifact.title,
                "${repo.displayName} · ${artifact.lifecycleState.label}",
                "${artifact.title} ${artifact.summary} ${artifact.type.label} ${repo.displayName}", artifact.updatedAt
            )
        }
        val discussionItems = discussions.filter { it.repoId in repoById }.map { discussion ->
            val repo = repoById.getValue(discussion.repoId)
            item(
                CollaborationTarget.Discussion(discussion.repoId, discussion.id), "討論", discussion.title,
                "${repo.displayName} · ${discussion.category.label}",
                "${discussion.title} ${discussion.body} ${discussion.category.label} ${repo.displayName}", discussion.updatedAt
            )
        }
        val orgItems = organizations.map { org ->
            item(
                CollaborationTarget.Organization(org.id), "組織", org.name,
                org.description.ifBlank { "企業組織" }, "${org.name} ${org.slug} ${org.description}", org.createdAt
            )
        }
        val teamItems = teams.map { team ->
            item(
                CollaborationTarget.Team(team.id), "團隊", team.name,
                team.description.ifBlank { "協作團隊" }, "${team.name} ${team.slug} ${team.description}", team.createdAt
            )
        }
        val userItems = users.filter { it.enterpriseId == user.enterpriseId }.map { profile ->
            item(
                CollaborationTarget.UserProfile(profile.id), "用戶", profile.displayName,
                "@${profile.username} · ${profile.title}",
                "${profile.displayName} ${profile.username} ${profile.email} ${profile.title}", profile.createdAt
            )
        }
        return (repoItems + issueItems + artifactItems + discussionItems + orgItems + teamItems + userItems)
            .sortedWith(compareByDescending<ExploreResult> { it.score }.thenBy { it.title })
    }
}


data class ScopeOperationalSummary(
    val title: String,
    val healthScore: Int,
    val detail: String
)

object ScopeOperationalProjection {
    fun build(
        scopeKind: WorkspaceScopeKind?,
        scopeName: String?,
        repositories: List<Repository>,
        issues: List<RepoIssue>,
        artifacts: List<NoCodeArtifact>,
        reviews: List<ArtifactReview>,
        approvals: List<ArtifactApproval>,
        notifications: List<AppNotification>
    ): ScopeOperationalSummary {
        val completed = issues.count { it.status == IssueStatus.CLOSED }
        val delivery = if (issues.isEmpty()) 100 else completed * 100 / issues.size
        val highRisk = issues.count { it.status != IssueStatus.CLOSED && it.priority == IssuePriority.CRITICAL }
        val pending = notifications.count { it.status == NotificationStatus.UNREAD || it.isActionable }
        val health = (delivery - highRisk * 8 - pending.coerceAtMost(10)).coerceIn(0, 100)
        val prefix = when (scopeKind) {
            WorkspaceScopeKind.ENTERPRISE -> "企業"
            WorkspaceScopeKind.ORGANIZATION -> "組織"
            WorkspaceScopeKind.TEAM -> "團隊"
            WorkspaceScopeKind.USER -> "個人"
            null -> "目前"
        }
        return ScopeOperationalSummary(
            title = "${prefix}態勢｜${scopeName ?: "目前範圍"}",
            healthScore = health,
            detail = "${repositories.size} 儲存庫 · ${issues.size} Issue · ${artifacts.size} 成果 · ${reviews.size} 審查 · ${approvals.size} 核准"
        )
    }
}

object PublicActivityPolicy {
    private val allowedPrefixes = listOf(
        "CREATE_", "SUBMIT_", "PUBLISH", "CLOSE_ISSUE", "REOPEN_ISSUE",
        "ASSIGN_ISSUE", "LINK_PARENT_ISSUE", "ADD_ISSUE_DEPENDENCY", "UPDATE_ISSUE_PLAN"
    )
    fun isPublic(log: AuditLog): Boolean =
        log.verdict == PolicyVerdict.ALLOWED &&
            log.repoId != null &&
            allowedPrefixes.any { prefix -> log.actionName.startsWith(prefix) }
}

data class SocialStats(
    val xp: Int,
    val level: Int,
    val publicActions: Int
)

object SocialProjection {
    fun stats(user: User, auditLogs: List<AuditLog>): SocialStats {
        val public = auditLogs.filter { it.actorUserId == user.id && PublicActivityPolicy.isPublic(it) }
        val xp = public.sumOf { log ->
            when {
                log.actionName.startsWith("PUBLISH") -> 80
                log.actionName.startsWith("SUBMIT_REVIEW") -> 40
                log.actionName == "CLOSE_ISSUE" -> 30
                log.actionName.startsWith("CREATE_") -> 20
                else -> 10
            }
        }
        return SocialStats(xp = xp, level = 1 + xp / 500, publicActions = public.size)
    }
}
'''
)

write(
    "app/src/main/java/com/example/ui/screens/ExperienceUiPrimitives.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.model.ScopeOperationalSummary

@Composable
fun CollaborationEmptyStateCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ScopeOperationalSummaryCard(summary: ScopeOperationalSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(summary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("健康度 ${summary.healthScore}%", color = MaterialTheme.colorScheme.primary)
            Text(summary.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
'''
)

write(
    "app/src/main/java/com/example/ui/screens/UnifiedExploreScreen.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NoCodeArtifact
import com.example.data.model.Organization
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.User
import com.example.navigation.CollaborationTarget
import com.example.ui.model.ExploreProjection

@Composable
fun UnifiedExploreScreen(
    activeUser: User?,
    repositories: List<Repository>,
    artifacts: List<NoCodeArtifact>,
    issues: List<RepoIssue>,
    discussions: List<RepoDiscussion>,
    organizations: List<Organization>,
    teams: List<Team>,
    users: List<User>,
    savedTargets: List<SavedTarget>,
    onOpenTarget: (CollaborationTarget) -> Unit,
    onToggleSaved: (CollaborationTarget) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(activeUser, repositories, artifacts, issues, discussions, organizations, teams, users, savedTargets) {
        ExploreProjection.build(activeUser, repositories, artifacts, issues, discussions, organizations, teams, users, savedTargets)
    }
    val visible = remember(results, query) {
        val q = query.trim()
        if (q.isBlank()) results else results.filter {
            it.title.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true) || it.searchableText.contains(q, ignoreCase = true)
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("探索", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().testTag("explore_search"),
            label = { Text("搜尋儲存庫、Issue、成果、討論、團隊與用戶") },
            singleLine = true
        )
        if (visible.isEmpty()) {
            CollaborationEmptyStateCard("沒有結果", "目前授權範圍內沒有符合條件的協作目標。")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.target.toString() }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenTarget(item.target) }
                            .testTag("explore_result_${item.typeLabel}_${item.title.hashCode()}")
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("${item.typeLabel} · ${item.title}", fontWeight = FontWeight.SemiBold)
                            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            IconButton(onClick = { onToggleSaved(item.target) }) {
                                Text(if (item.isSaved) "★" else "☆", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
'''
)

write(
    "app/src/main/java/com/example/ui/screens/RepositoryWbsSection.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.RepoIssue
import kotlin.math.min

@Composable
fun RepositoryWbsSection(
    issues: List<RepoIssue>,
    onUpdatePlan: (String, Int, Long?, Long?, Double, Int) -> Unit
) {
    val rows = remember(issues) { IssueHierarchyRules.wbsProjection(issues) }
    val parentIds = remember(issues) { issues.mapNotNull { it.parentIssueId }.toSet() }
    if (rows.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            CollaborationEmptyStateCard("WBS 尚無工作", "Repository Issue 建立後會自動形成工作分解樹。")
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(rows, key = { it.issue.id }) { row ->
            val issue = row.issue
            Card(Modifier.fillMaxWidth().padding(start = (row.depth * 12).dp).testTag("repo_wbs_${issue.id}")) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${row.code} ${issue.title}", fontWeight = FontWeight.SemiBold)
                        Text("${(row.progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(progress = { row.progress }, modifier = Modifier.fillMaxWidth())
                    Text(
                        "排序 ${issue.sortOrder} · 權重 ${issue.wbsWeight} · ${issue.plannedStartAt ?: "未排開始"} → ${issue.plannedEndAt ?: "未排結束"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (issue.id !in parentIds && issue.status != com.example.data.model.IssueStatus.CLOSED) {
                        Button(
                            onClick = {
                                onUpdatePlan(
                                    issue.id,
                                    issue.sortOrder,
                                    issue.plannedStartAt,
                                    issue.plannedEndAt,
                                    issue.wbsWeight,
                                    min(100, issue.progressPercent + 10)
                                )
                            },
                            modifier = Modifier.testTag("repo_wbs_progress_${issue.id}")
                        ) { Text("進度 +10%") }
                    }
                }
            }
        }
    }
}
'''
)

write(
    "app/src/main/java/com/example/ui/screens/SocialProfileScreen.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.SavedTarget
import com.example.data.model.SyncStatusSummary
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.ui.model.SocialProjection

@Composable
fun SocialProfileScreen(
    profileUser: User,
    activeUser: User,
    auditLogs: List<AuditLog>,
    follows: List<UserFollow>,
    savedTargets: List<SavedTarget>,
    syncStatus: SyncStatusSummary,
    onToggleFollow: (String) -> Unit,
    onSyncNow: () -> Unit
) {
    val stats = SocialProjection.stats(profileUser, auditLogs)
    val followers = follows.count { it.followedUserId == profileUser.id }
    val following = follows.count { it.followerUserId == profileUser.id }
    val isFollowing = follows.any { it.followerUserId == activeUser.id && it.followedUserId == profileUser.id }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(profileUser.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("@${profileUser.username} · ${profileUser.title}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Lv.${stats.level} · XP ${stats.xp}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("公開協作成果 ${stats.publicActions} · 追隨者 $followers · 追蹤中 $following")
                Text("收藏 ${savedTargets.count { it.userId == profileUser.id }}")
            }
        }
        if (profileUser.id != activeUser.id) {
            OutlinedButton(onClick = { onToggleFollow(profileUser.id) }, modifier = Modifier.testTag("profile_follow")) {
                Text(if (isFollowing) "取消追蹤" else "追蹤")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSyncNow, modifier = Modifier.testTag("profile_sync_now")) { Text("立即同步") }
                Text("待同步 ${syncStatus.pending} · 衝突 ${syncStatus.conflicts} · 失敗 ${syncStatus.failed}")
            }
        }
    }
}
'''
)

write(
    "app/src/main/java/com/example/ui/screens/PersonalCenterSwitchScreen.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.SavedTarget
import com.example.data.model.SyncStatusSummary
import com.example.data.model.User
import com.example.data.model.UserFollow

@Composable
fun PersonalCenterSwitchScreen(
    profileUser: User,
    activeUser: User,
    auditLogs: List<AuditLog>,
    follows: List<UserFollow>,
    savedTargets: List<SavedTarget>,
    syncStatus: SyncStatusSummary,
    onToggleFollow: (String) -> Unit,
    onSyncNow: () -> Unit,
    governanceContent: @Composable () -> Unit
) {
    var social by rememberSaveable(profileUser.id) { mutableStateOf(true) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            FilterChip(selected = social, onClick = { social = true }, label = { Text("成就與動態") })
            FilterChip(selected = !social, onClick = { social = false }, label = { Text("身份與治理") }, modifier = Modifier.padding(start = 8.dp))
        }
        Column(Modifier.fillMaxWidth()) {
            if (social) {
                SocialProfileScreen(profileUser, activeUser, auditLogs, follows, savedTargets, syncStatus, onToggleFollow, onSyncNow)
            } else governanceContent()
        }
    }
}
'''
)

print("current-main compatible mobile v2 UI models written")
