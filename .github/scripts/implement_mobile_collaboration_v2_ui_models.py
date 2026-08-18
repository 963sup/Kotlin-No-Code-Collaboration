from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def write(rel: str, content: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")


write(
    "app/src/main/java/com/example/navigation/CollaborationTarget.kt",
    r'''
package com.example.navigation

import com.example.data.model.CollaborationTargetType
import com.example.data.model.OrgMembership
import com.example.data.model.RepoAccessRule
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.TeamMembership
import com.example.data.model.User

sealed interface CollaborationTarget {
    data class Repository(val repositoryId: String) : CollaborationTarget
    data class Artifact(val repositoryId: String, val artifactId: String) : CollaborationTarget
    data class Issue(val repositoryId: String, val issueId: String) : CollaborationTarget
    data class Discussion(val repositoryId: String, val discussionId: String) : CollaborationTarget
    data class Organization(val organizationId: String) : CollaborationTarget
    data class Team(val teamId: String) : CollaborationTarget
    data class UserProfile(val userId: String) : CollaborationTarget
}

data class CollaborationTargetResolution(
    val target: CollaborationTarget?,
    val failureMessage: String? = null
) {
    val isSuccess: Boolean get() = target != null
}

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
    CollaborationTargetType.ARTIFACT -> repositoryId.takeIf { it.isNotBlank() }
        ?.let { CollaborationTarget.Artifact(it, targetId) }
    CollaborationTargetType.ISSUE -> repositoryId.takeIf { it.isNotBlank() }
        ?.let { CollaborationTarget.Issue(it, targetId) }
    CollaborationTargetType.DISCUSSION -> repositoryId.takeIf { it.isNotBlank() }
        ?.let { CollaborationTarget.Discussion(it, targetId) }
    CollaborationTargetType.ORGANIZATION -> CollaborationTarget.Organization(targetId)
    CollaborationTargetType.TEAM -> CollaborationTarget.Team(targetId)
    CollaborationTargetType.USER -> CollaborationTarget.UserProfile(targetId)
    else -> null
}

object CollaborationTargetResolver {
    fun resolveNotification(
        repositoryId: String?,
        artifactId: String?,
        issueId: String?,
        discussionId: String?
    ): CollaborationTargetResolution {
        val specificTargetCount = listOf(artifactId, issueId, discussionId).count { !it.isNullOrBlank() }
        if (specificTargetCount > 1) {
            return CollaborationTargetResolution(
                target = null,
                failureMessage = "通知目標互相衝突，無法安全開啟"
            )
        }
        return when {
            !artifactId.isNullOrBlank() && !repositoryId.isNullOrBlank() ->
                CollaborationTargetResolution(CollaborationTarget.Artifact(repositoryId, artifactId))
            !issueId.isNullOrBlank() && !repositoryId.isNullOrBlank() ->
                CollaborationTargetResolution(CollaborationTarget.Issue(repositoryId, issueId))
            !discussionId.isNullOrBlank() && !repositoryId.isNullOrBlank() ->
                CollaborationTargetResolution(CollaborationTarget.Discussion(repositoryId, discussionId))
            !artifactId.isNullOrBlank() || !issueId.isNullOrBlank() || !discussionId.isNullOrBlank() ->
                CollaborationTargetResolution(
                    target = null,
                    failureMessage = "通知缺少儲存庫範圍，無法安全開啟"
                )
            !repositoryId.isNullOrBlank() ->
                CollaborationTargetResolution(CollaborationTarget.Repository(repositoryId))
            else -> CollaborationTargetResolution(
                target = null,
                failureMessage = "通知沒有可開啟的協作目標"
            )
        }
    }
}

object CollaborationTargetAccess {
    fun canOpenRepository(
        user: User,
        repository: Repository,
        orgMemberships: List<OrgMembership>,
        teamMemberships: List<TeamMembership>,
        accessRules: List<RepoAccessRule>
    ): Boolean {
        if (repository.enterpriseId != user.enterpriseId) return false
        if (user.isEnterpriseAdmin) return true
        if (repository.ownerUserId == user.id) return true
        if (repository.ownerOrganizationId != null && orgMemberships.any {
                it.userId == user.id && it.organizationId == repository.ownerOrganizationId
            }
        ) return true

        val userTeamIds = teamMemberships.filter { it.userId == user.id }.map { it.teamId }.toSet()
        return accessRules.any { rule ->
            rule.repoId == repository.id && (
                rule.userId == user.id ||
                    (rule.teamId != null && rule.teamId in userTeamIds)
                )
        }
    }
}
''',
)

write(
    "app/src/main/java/com/example/ui/model/ExperienceProjections.kt",
    r'''
package com.example.ui.model

import com.example.data.model.AppNotification
import com.example.data.model.ApprovalDecision
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactLifecycleState
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.Enterprise
import com.example.data.model.IssueAssigneeType
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.ReviewDecision
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget
import com.example.navigation.CollaborationTargetAccess
import com.example.navigation.storageKey
import com.example.ui.components.WorkspaceScopeKind
import kotlin.math.max


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
        orgMemberships: List<OrgMembership>,
        teamMemberships: List<TeamMembership>,
        accessRules: List<RepoAccessRule>,
        auditLogs: List<AuditLog>,
        savedTargets: List<SavedTarget>,
        now: Long = System.currentTimeMillis()
    ): List<ExploreResult> {
        val user = activeUser ?: return emptyList()
        val accessibleRepositories = repositories.filter { repository ->
            CollaborationTargetAccess.canOpenRepository(
                user = user,
                repository = repository,
                orgMemberships = orgMemberships,
                teamMemberships = teamMemberships,
                accessRules = accessRules
            )
        }
        val repositoryById = accessibleRepositories.associateBy { it.id }
        val savedKeys = savedTargets.filter { it.userId == user.id }.map { it.targetKey }.toSet()
        val publicAudit = auditLogs.filter(PublicActivityPolicy::isPublic)

        fun score(updatedAt: Long, repositoryId: String?, targetId: String): Int {
            val ageDays = ((now - updatedAt).coerceAtLeast(0L) / 86_400_000L).toInt()
            val recency = (30 - ageDays).coerceIn(0, 30)
            val activity = publicAudit.count { log ->
                (repositoryId != null && log.repoId == repositoryId) || log.targetId == targetId
            }.coerceAtMost(20) * 3
            return recency + activity
        }

        fun result(
            target: CollaborationTarget,
            typeLabel: String,
            title: String,
            subtitle: String,
            searchable: String,
            updatedAt: Long,
            repositoryId: String? = null
        ) = ExploreResult(
            target = target,
            typeLabel = typeLabel,
            title = title,
            subtitle = subtitle,
            searchableText = searchable,
            score = score(updatedAt, repositoryId, target.storageKey()),
            isSaved = target.storageKey() in savedKeys
        )

        val repositoryResults = accessibleRepositories.map { repo ->
            result(
                target = CollaborationTarget.Repository(repo.id),
                typeLabel = "儲存庫",
                title = repo.displayName,
                subtitle = repo.description.ifBlank { "無程式碼協作容器" },
                searchable = "${repo.name} ${repo.displayName} ${repo.description} ${repo.category}",
                updatedAt = repo.updatedAt,
                repositoryId = repo.id
            )
        }
        val issueResults = issues.filter { it.repoId in repositoryById }.map { issue ->
            val repo = repositoryById.getValue(issue.repoId)
            result(
                target = CollaborationTarget.Issue(issue.repoId, issue.id),
                typeLabel = "Issue",
                title = "#${issue.issueNumber} ${issue.title}",
                subtitle = "${repo.displayName} · ${issue.status.displayName}",
                searchable = "${issue.title} ${issue.body} ${issue.labels} ${repo.displayName}",
                updatedAt = issue.updatedAt,
                repositoryId = issue.repoId
            )
        }
        val artifactResults = artifacts.filter { it.repoId in repositoryById }.map { artifact ->
            val repo = repositoryById.getValue(artifact.repoId)
            result(
                target = CollaborationTarget.Artifact(artifact.repoId, artifact.id),
                typeLabel = "成果",
                title = artifact.displayName,
                subtitle = "${repo.displayName} · ${artifact.lifecycleState.displayName}",
                searchable = "${artifact.name} ${artifact.displayName} ${artifact.description} ${artifact.tags}",
                updatedAt = artifact.updatedAt,
                repositoryId = artifact.repoId
            )
        }
        val discussionResults = discussions.filter { it.repoId in repositoryById }.map { discussion ->
            val repo = repositoryById.getValue(discussion.repoId)
            result(
                target = CollaborationTarget.Discussion(discussion.repoId, discussion.id),
                typeLabel = "討論",
                title = discussion.title,
                subtitle = "${repo.displayName} · ${discussion.status.displayName}",
                searchable = "${discussion.title} ${discussion.body} ${discussion.category} ${discussion.labels}",
                updatedAt = discussion.updatedAt,
                repositoryId = discussion.repoId
            )
        }

        val accessibleOrganizationIds = if (user.isEnterpriseAdmin) {
            organizations.filter { it.enterpriseId == user.enterpriseId }.map { it.id }.toSet()
        } else {
            orgMemberships.filter { it.userId == user.id }.map { it.organizationId }.toSet()
        }
        val organizationResults = organizations.filter { it.id in accessibleOrganizationIds }.map { org ->
            result(
                target = CollaborationTarget.Organization(org.id),
                typeLabel = "組織",
                title = org.displayName,
                subtitle = org.description.ifBlank { "企業組織" },
                searchable = "${org.name} ${org.displayName} ${org.description}",
                updatedAt = org.createdAt
            )
        }
        val directTeamIds = teamMemberships.filter { it.userId == user.id }.map { it.teamId }.toSet()
        val teamResults = teams.filter {
            it.enterpriseId == user.enterpriseId && (user.isEnterpriseAdmin || it.organizationId in accessibleOrganizationIds || it.id in directTeamIds)
        }.map { team ->
            result(
                target = CollaborationTarget.Team(team.id),
                typeLabel = "團隊",
                title = team.name,
                subtitle = team.description.ifBlank { "協作團隊" },
                searchable = "${team.name} ${team.description}",
                updatedAt = team.createdAt
            )
        }
        val userResults = users.filter { it.enterpriseId == user.enterpriseId }.map { profile ->
            result(
                target = CollaborationTarget.UserProfile(profile.id),
                typeLabel = "用戶",
                title = profile.displayName,
                subtitle = profile.email,
                searchable = "${profile.displayName} ${profile.email}",
                updatedAt = profile.createdAt
            )
        }

        return (repositoryResults + issueResults + artifactResults + discussionResults +
            organizationResults + teamResults + userResults)
            .sortedWith(compareByDescending<ExploreResult> { it.score }.thenBy { it.title })
    }
}


data class ScopeMetric(val label: String, val value: String, val detail: String)

data class ScopeOperationalSummary(
    val title: String,
    val subtitle: String,
    val healthScore: Int,
    val metrics: List<ScopeMetric>
)

object ScopeOperationalProjection {
    fun build(
        scopeKind: WorkspaceScopeKind?,
        scopeName: String?,
        activeUserId: String?,
        repositories: List<Repository>,
        issues: List<RepoIssue>,
        artifacts: List<NoCodeArtifact>,
        reviews: List<ArtifactReview>,
        approvals: List<ArtifactApproval>,
        notifications: List<AppNotification>,
        organizations: List<Organization>,
        teams: List<Team>,
        users: List<User>
    ): ScopeOperationalSummary {
        val closed = issues.count { it.status == IssueStatus.CLOSED }
        val delivery = if (issues.isEmpty()) 100 else closed * 100 / issues.size
        val highRisk = issues.count {
            it.status != IssueStatus.CLOSED && (it.priority == IssuePriority.CRITICAL || it.status == IssueStatus.BLOCKED)
        }
        val pendingGovernance = reviews.count { it.decision == ReviewDecision.PENDING } +
            approvals.count { it.decision == ApprovalDecision.PENDING }
        val health = (delivery - highRisk * 7 - pendingGovernance * 2).coerceIn(0, 100)
        val unread = notifications.count { !it.isRead }
        val name = scopeName?.takeIf { it.isNotBlank() } ?: "目前範圍"

        return when (scopeKind) {
            WorkspaceScopeKind.ENTERPRISE, null -> ScopeOperationalSummary(
                title = "企業作業態勢",
                subtitle = "$name · 跨組織交付、風險與治理",
                healthScore = health,
                metrics = listOf(
                    ScopeMetric("組織", organizations.size.toString(), "可運作單元"),
                    ScopeMetric("儲存庫", repositories.size.toString(), "協作容器"),
                    ScopeMetric("高風險", highRisk.toString(), "關鍵或阻塞工作"),
                    ScopeMetric("待治理", pendingGovernance.toString(), "審查與核准")
                )
            )
            WorkspaceScopeKind.ORGANIZATION -> ScopeOperationalSummary(
                title = "組織交付態勢",
                subtitle = "$name · 團隊產能與交付負荷",
                healthScore = health,
                metrics = listOf(
                    ScopeMetric("團隊", teams.size.toString(), "組織內團隊"),
                    ScopeMetric("成員", users.size.toString(), "可協作人員"),
                    ScopeMetric("交付", "$delivery%", "Issue 完成率"),
                    ScopeMetric("未讀", unread.toString(), "需注意訊息")
                )
            )
            WorkspaceScopeKind.TEAM -> {
                val assigned = issues.count { it.assigneeType == IssueAssigneeType.TEAM }
                ScopeOperationalSummary(
                    title = "團隊執行態勢",
                    subtitle = "$name · 現在要完成的工作",
                    healthScore = health,
                    metrics = listOf(
                        ScopeMetric("成員", users.size.toString(), "團隊可用能力"),
                        ScopeMetric("指派", assigned.toString(), "團隊承擔工作"),
                        ScopeMetric("交付", "$delivery%", "目前完成率"),
                        ScopeMetric("成果", artifacts.size.toString(), "文件與交付物")
                    )
                )
            }
            WorkspaceScopeKind.USER -> {
                val mine = issues.count {
                    it.assigneeType == IssueAssigneeType.USER && it.assigneeId == activeUserId && it.status != IssueStatus.CLOSED
                }
                ScopeOperationalSummary(
                    title = "我的作業焦點",
                    subtitle = "$name · 待辦、審查與通知",
                    healthScore = health,
                    metrics = listOf(
                        ScopeMetric("待辦", mine.toString(), "直接指派工作"),
                        ScopeMetric("待審", pendingGovernance.toString(), "審查與核准"),
                        ScopeMetric("未讀", unread.toString(), "收件匣訊息"),
                        ScopeMetric("成果", artifacts.count { it.authorUserId == activeUserId }.toString(), "個人貢獻")
                    )
                )
            }
        }
    }
}

object MyWorkProjection {
    fun assignedIssues(
        issues: List<RepoIssue>,
        activeUserId: String?,
        activeTeamIds: Set<String>
    ): List<RepoIssue> {
        if (activeUserId == null) return emptyList()
        return issues.filter { issue ->
            (issue.assigneeType == IssueAssigneeType.USER && issue.assigneeId == activeUserId) ||
                (issue.assigneeType == IssueAssigneeType.TEAM && issue.assigneeId in activeTeamIds)
        }.sortedWith(
            compareBy<RepoIssue> { it.status.ordinal }
                .thenByDescending { it.priority.ordinal }
                .thenBy { it.sortOrder }
                .thenBy { it.issueNumber }
        )
    }
}


data class DerivedAchievement(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int,
    val earned: Boolean
)

data class PublicActivityItem(
    val id: String,
    val title: String,
    val detail: String,
    val createdAt: Long
)

data class SocialProfileProjection(
    val xp: Int,
    val level: Int,
    val currentLevelProgress: Float,
    val nextLevelXp: Int,
    val followerCount: Int,
    val followingCount: Int,
    val achievements: List<DerivedAchievement>,
    val activity: List<PublicActivityItem>
)

object PublicActivityPolicy {
    private val allowList = setOf(
        "CREATE_ISSUE",
        "UPDATE_ISSUE_STATUS",
        "UPDATE_ISSUE_PLAN",
        "CREATE_DISCUSSION",
        "CREATE_ARTIFACT",
        "UPDATE_ARTIFACT_CONTENT",
        "SUBMIT_REVIEW",
        "PUBLISH_AND_LOCK"
    )

    fun isPublic(log: AuditLog): Boolean =
        log.verdict == PolicyVerdict.ALLOWED && log.actionName in allowList
}

object SocialProjection {
    fun derive(
        profileUser: User,
        issues: List<RepoIssue>,
        artifacts: List<NoCodeArtifact>,
        reviews: List<ArtifactReview>,
        approvals: List<ArtifactApproval>,
        auditLogs: List<AuditLog>,
        userFollows: List<UserFollow>,
        repositories: List<Repository>
    ): SocialProfileProjection {
        val completedIssues = issues.count {
            it.status == IssueStatus.CLOSED &&
                (it.closedByUserId == profileUser.id ||
                    (it.assigneeType == IssueAssigneeType.USER && it.assigneeId == profileUser.id))
        }
        val approvedReviews = reviews.count {
            it.reviewerUserId == profileUser.id && it.decision == ReviewDecision.APPROVED
        }
        val approvalsGranted = approvals.count {
            it.approverUserId == profileUser.id && it.decision == ApprovalDecision.APPROVED
        }
        val publishedArtifacts = artifacts.count {
            it.authorUserId == profileUser.id && it.lifecycleState == ArtifactLifecycleState.PUBLISHED
        }
        val publicAudits = auditLogs.filter {
            it.actorUserId == profileUser.id && PublicActivityPolicy.isPublic(it)
        }
        val xp = completedIssues * 20 + approvedReviews * 15 + approvalsGranted * 20 +
            publishedArtifacts * 30 + publicAudits.size * 5
        val level = 1 + xp / 250
        val levelFloor = (level - 1) * 250
        val nextLevelXp = level * 250
        val progress = ((xp - levelFloor).toFloat() / 250f).coerceIn(0f, 1f)
        val repoById = repositories.associateBy { it.id }

        val achievements = listOf(
            DerivedAchievement("first_delivery", "首次交付", "完成第一個 Issue", completedIssues, 1, completedIssues >= 1),
            DerivedAchievement("delivery_10", "穩定交付", "完成 10 個 Issue", completedIssues, 10, completedIssues >= 10),
            DerivedAchievement("reviewer_5", "品質守門", "核准 5 次審查", approvedReviews, 5, approvedReviews >= 5),
            DerivedAchievement("publisher_3", "成果發布者", "發布 3 項成果", publishedArtifacts, 3, publishedArtifacts >= 3)
        )
        val activity = publicAudits.sortedByDescending { it.createdAt }.take(20).map { log ->
            PublicActivityItem(
                id = log.id,
                title = when (log.actionName) {
                    "CREATE_ISSUE" -> "建立工作"
                    "UPDATE_ISSUE_STATUS" -> "更新工作狀態"
                    "UPDATE_ISSUE_PLAN" -> "更新 WBS 計畫"
                    "CREATE_DISCUSSION" -> "發起討論"
                    "CREATE_ARTIFACT" -> "建立成果"
                    "UPDATE_ARTIFACT_CONTENT" -> "更新成果"
                    "SUBMIT_REVIEW" -> "完成審查"
                    "PUBLISH_AND_LOCK" -> "發布成果"
                    else -> "協作活動"
                },
                detail = listOfNotNull(repoById[log.repoId]?.displayName, log.targetType, log.targetId)
                    .joinToString(" · "),
                createdAt = log.createdAt
            )
        }
        return SocialProfileProjection(
            xp = xp,
            level = level,
            currentLevelProgress = progress,
            nextLevelXp = nextLevelXp,
            followerCount = userFollows.count { it.followedUserId == profileUser.id },
            followingCount = userFollows.count { it.followerUserId == profileUser.id },
            achievements = achievements,
            activity = activity
        )
    }
}
''',
)

write(
    "app/src/main/java/com/example/ui/screens/ScopeOperationalSummaryCard.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.model.ScopeOperationalSummary

@Composable
fun ScopeOperationalSummaryCard(summary: ScopeOperationalSummary) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("home_scope_operational_summary"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(summary.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        summary.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    )
                }
                Text(
                    "${summary.healthScore}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { summary.healthScore / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            summary.metrics.chunked(2).forEach { rowMetrics ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowMetrics.forEach { metric ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(metric.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(metric.label, style = MaterialTheme.typography.labelMedium)
                            Text(
                                metric.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (rowMetrics.size == 1) Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}
''',
)

write(
    "app/src/main/java/com/example/ui/screens/RepositoryWbsSection.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.RepoIssue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RepositoryWbsSection(
    issues: List<RepoIssue>,
    onOpenIssue: (String) -> Unit,
    onUpdatePlan: (String, Int, Long?, Long?, Double, Int) -> Unit
) {
    val rows = remember(issues) { IssueHierarchyRules.wbsProjection(issues) }
    var editingIssue by remember { mutableStateOf<RepoIssue?>(null) }

    if (rows.isEmpty()) {
        EmptyStateCard(
            title = "尚無 WBS 工作",
            body = "建立 Issue 後會直接投影為工作分解結構，不會產生第二套任務資料。"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().testTag("repository_wbs_tree"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rows, key = { it.issue.id }) { row ->
                val hasChildren = rows.any { it.issue.parentIssueId == row.issue.id }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (row.depth * 14).dp)
                        .clickable { onOpenIssue(row.issue.id) }
                        .testTag("wbs_issue_${row.issue.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                row.code,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                row.issue.title,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { editingIssue = row.issue },
                                modifier = Modifier.testTag("wbs_edit_${row.issue.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "編輯 WBS 計畫")
                            }
                        }
                        LinearProgressIndicator(progress = { row.progress }, modifier = Modifier.fillMaxWidth())
                        Text(
                            "${(row.progress * 100).toInt()}% · ${row.completedCount}/${row.totalCount} 完成 · 權重 ${row.issue.wbsWeight}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val planText = formatPlan(row.issue)
                        if (planText.isNotBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(planText, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (hasChildren) {
                            Text(
                                "父層進度由直接子工作依權重自動彙總",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    editingIssue?.let { issue ->
        WbsPlanDialog(
            issue = issue,
            hasChildren = issues.any { it.parentIssueId == issue.id },
            onDismiss = { editingIssue = null },
            onConfirm = { order, start, end, weight, progress ->
                onUpdatePlan(issue.id, order, start, end, weight, progress)
                editingIssue = null
            }
        )
    }
}

@Composable
private fun WbsPlanDialog(
    issue: RepoIssue,
    hasChildren: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Long?, Long?, Double, Int) -> Unit
) {
    var orderText by remember(issue.id) { mutableStateOf(issue.sortOrder.toString()) }
    var startText by remember(issue.id) { mutableStateOf(formatDate(issue.plannedStartAt)) }
    var endText by remember(issue.id) { mutableStateOf(formatDate(issue.plannedEndAt)) }
    var weightText by remember(issue.id) { mutableStateOf(issue.wbsWeight.toString()) }
    var progressText by remember(issue.id) { mutableStateOf(issue.progressPercent.toString()) }

    val order = orderText.toIntOrNull()
    val start = parseDate(startText)
    val end = parseDate(endText)
    val weight = weightText.toDoubleOrNull()
    val progress = progressText.toIntOrNull()
    val datesValid = (startText.isBlank() || start != null) && (endText.isBlank() || end != null) &&
        (start == null || end == null || end >= start)
    val valid = order != null && order >= 0 && weight != null && weight > 0.0 &&
        progress != null && progress in 0..100 && datesValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("WBS 計畫 · #${issue.issueNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = orderText,
                    onValueChange = { orderText = it },
                    label = { Text("同層排序") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = startText,
                    onValueChange = { startText = it },
                    label = { Text("計畫開始日 YYYY-MM-DD") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = endText,
                    onValueChange = { endText = it },
                    label = { Text("計畫結束日 YYYY-MM-DD") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("WBS 權重") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = progressText,
                    onValueChange = { progressText = it },
                    label = { Text(if (hasChildren) "葉節點進度（父層由子工作彙總）" else "進度 0–100") },
                    enabled = !hasChildren,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (!datesValid) {
                    Text("日期格式錯誤，或結束日早於開始日", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(order!!, start, end, weight!!, progress!!) },
                modifier = Modifier.testTag("wbs_plan_confirm")
            ) { Text("儲存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun formatDate(epochMillis: Long?): String = epochMillis?.let {
    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
}.orEmpty()

private fun parseDate(value: String): Long? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDate.parse(value.trim(), dateFormatter)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun formatPlan(issue: RepoIssue): String {
    val start = formatDate(issue.plannedStartAt)
    val end = formatDate(issue.plannedEndAt)
    return when {
        start.isNotBlank() && end.isNotBlank() -> "$start → $end"
        start.isNotBlank() -> "$start 起"
        end.isNotBlank() -> "$end 前完成"
        else -> ""
    }
}
''',
)

write(
    "app/src/main/java/com/example/ui/screens/UnifiedExploreScreen.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.Enterprise
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.navigation.CollaborationTarget
import com.example.ui.model.ExploreProjection

@Composable
fun UnifiedExploreScreen(
    enterprise: Enterprise?,
    activeUser: User?,
    repositories: List<Repository>,
    artifacts: List<NoCodeArtifact>,
    issues: List<RepoIssue>,
    discussions: List<RepoDiscussion>,
    organizations: List<Organization>,
    teams: List<Team>,
    users: List<User>,
    orgMemberships: List<OrgMembership>,
    teamMemberships: List<TeamMembership>,
    accessRules: List<RepoAccessRule>,
    auditLogs: List<AuditLog>,
    savedTargets: List<SavedTarget>,
    onOpenTarget: (CollaborationTarget) -> Unit,
    onToggleSaved: (CollaborationTarget) -> Unit,
    onCreateRepository: (String, String, OwnerType, String, String, String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf("全部") }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    val results = remember(
        activeUser, repositories, artifacts, issues, discussions, organizations, teams,
        users, orgMemberships, teamMemberships, accessRules, auditLogs, savedTargets
    ) {
        ExploreProjection.build(
            activeUser = activeUser,
            repositories = repositories,
            artifacts = artifacts,
            issues = issues,
            discussions = discussions,
            organizations = organizations,
            teams = teams,
            users = users,
            orgMemberships = orgMemberships,
            teamMemberships = teamMemberships,
            accessRules = accessRules,
            auditLogs = auditLogs,
            savedTargets = savedTargets
        )
    }
    val filtered = remember(results, query, typeFilter) {
        val normalized = query.trim().lowercase()
        results.filter { result ->
            (typeFilter == "全部" || result.typeLabel == typeFilter) &&
                (normalized.isBlank() || result.title.lowercase().contains(normalized) ||
                    result.subtitle.lowercase().contains(normalized) ||
                    result.searchableText.lowercase().contains(normalized))
        }
    }
    val ownerCandidates = remember(activeUser, enterprise, organizations) {
        buildList {
            if (activeUser != null && enterprise?.allowUserOwnedRepos == true) {
                add(OwnerChoice(OwnerType.USER, activeUser.id, "個人 · ${activeUser.displayName}"))
            }
            organizations.forEach { add(OwnerChoice(OwnerType.ORGANIZATION, it.id, "組織 · ${it.displayName}")) }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text("探索", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("搜尋你有權存取的協作目標", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (ownerCandidates.isNotEmpty()) {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("explore_create_repository")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("建立")
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().testTag("explore_search_input"),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("儲存庫、Issue、成果、討論、團隊或用戶") },
            singleLine = true
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("全部", "儲存庫", "Issue", "成果", "討論", "組織", "團隊", "用戶")) { type ->
                FilterChip(
                    selected = typeFilter == type,
                    onClick = { typeFilter = type },
                    label = { Text(type) },
                    modifier = Modifier.testTag("explore_filter_$type")
                )
            }
        }
        if (filtered.isEmpty()) {
            EmptyStateCard(
                title = if (query.isBlank()) "沒有可探索的項目" else "找不到符合項目",
                body = "搜尋結果會先套用現有階層權限，再依活動與更新時間排序。"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.target.toString() }) { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTarget(result.target) }
                            .testTag("explore_result_${result.target.hashCode()}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(result.typeLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(result.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(result.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("熱度 ${result.score}", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(
                                onClick = { onToggleSaved(result.target) },
                                modifier = Modifier.testTag("explore_save_${result.target.hashCode()}")
                            ) {
                                Icon(
                                    imageVector = if (result.isSaved) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (result.isSaved) "取消收藏" else "收藏",
                                    tint = if (result.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showCreateDialog) {
        RepositoryCreateDialog(
            owners = ownerCandidates,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, displayName, owner, description, category ->
                onCreateRepository(name, displayName, owner.type, owner.id, description, category)
                showCreateDialog = false
            }
        )
    }
}

private data class OwnerChoice(val type: OwnerType, val id: String, val label: String)

@Composable
private fun RepositoryCreateDialog(
    owners: List<OwnerChoice>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, OwnerChoice, String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("工程協作") }
    var selectedOwner by remember { mutableStateOf(owners.first()) }
    val valid = name.matches(Regex("[A-Za-z0-9_-]{2,60}")) && displayName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("建立無程式碼協作容器") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("識別名稱（英數、-、_）") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("顯示名稱") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("說明") }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分類") },
                    singleLine = true
                )
                Text("擁有者", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(owners) { owner ->
                        FilterChip(
                            selected = owner == selectedOwner,
                            onClick = { selectedOwner = owner },
                            label = { Text(owner.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(name, displayName.trim(), selectedOwner, description.trim(), category.trim()) },
                modifier = Modifier.testTag("explore_create_repository_confirm")
            ) { Text("建立") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
''',
)

write(
    "app/src/main/java/com/example/ui/screens/SocialProfileScreen.kt",
    r'''
package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.NoCodeArtifact
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.SyncStatusSummary
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget
import com.example.navigation.toCollaborationTarget
import com.example.ui.model.SocialProjection
import java.text.DateFormat
import java.util.Date

@Composable
fun SocialProfileScreen(
    profileUser: User,
    activeUser: User,
    users: List<User>,
    repositories: List<Repository>,
    issues: List<RepoIssue>,
    artifacts: List<NoCodeArtifact>,
    reviews: List<ArtifactReview>,
    approvals: List<ArtifactApproval>,
    auditLogs: List<AuditLog>,
    userFollows: List<UserFollow>,
    savedTargets: List<SavedTarget>,
    syncStatus: SyncStatusSummary,
    onToggleFollow: (String) -> Unit,
    onOpenTarget: (CollaborationTarget) -> Unit,
    onSyncNow: () -> Unit
) {
    val projection = remember(
        profileUser, repositories, issues, artifacts, reviews, approvals, auditLogs, userFollows
    ) {
        SocialProjection.derive(
            profileUser = profileUser,
            issues = issues,
            artifacts = artifacts,
            reviews = reviews,
            approvals = approvals,
            auditLogs = auditLogs,
            userFollows = userFollows,
            repositories = repositories
        )
    }
    val isSelf = profileUser.id == activeUser.id
    val isFollowing = userFollows.any {
        it.followerUserId == activeUser.id && it.followedUserId == profileUser.id
    }
    val followingUsers = users.filter { candidate ->
        userFollows.any { it.followerUserId == profileUser.id && it.followedUserId == candidate.id }
    }
    val mySaved = savedTargets.filter { it.userId == profileUser.id }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp).testTag("social_profile"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Lv.${projection.level}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("${projection.xp} XP / ${projection.nextLevelXp} XP")
                        }
                        if (!isSelf) {
                            Button(
                                onClick = { onToggleFollow(profileUser.id) },
                                modifier = Modifier.testTag("profile_follow_toggle")
                            ) { Text(if (isFollowing) "取消追蹤" else "追蹤") }
                        }
                    }
                    LinearProgressIndicator(progress = { projection.currentLevelProgress }, modifier = Modifier.fillMaxWidth())
                    Text("追隨者 ${projection.followerCount} · 追蹤中 ${projection.followingCount}")
                }
            }
        }

        if (isSelf) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("同步", fontWeight = FontWeight.SemiBold)
                            Text(
                                "待送 ${syncStatus.pending} · 失敗 ${syncStatus.failed} · 衝突 ${syncStatus.conflicts}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            syncStatus.lastSyncedAt?.let {
                                Text("最近同步 ${DateFormat.getDateTimeInstance().format(Date(it))}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        OutlinedButton(onClick = onSyncNow, modifier = Modifier.testTag("sync_now")) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Text("同步")
                        }
                    }
                }
            }
        }

        item { SectionTitle("成就") }
        items(projection.achievements, key = { it.id }) { achievement ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (achievement.earned) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(achievement.title, fontWeight = FontWeight.SemiBold)
                    Text(achievement.description, style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(
                        progress = { (achievement.progress.toFloat() / achievement.target.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${achievement.progress.coerceAtMost(achievement.target)}/${achievement.target}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (followingUsers.isNotEmpty()) {
            item { SectionTitle("追蹤中") }
            items(followingUsers, key = { it.id }) { followed ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        onOpenTarget(CollaborationTarget.UserProfile(followed.id))
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(followed.displayName, fontWeight = FontWeight.SemiBold)
                        Text(followed.email, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (isSelf && mySaved.isNotEmpty()) {
            item { SectionTitle("我的收藏") }
            items(mySaved, key = { it.id }) { saved ->
                val target = saved.toCollaborationTarget()
                Card(
                    modifier = Modifier.fillMaxWidth().then(
                        if (target != null) Modifier.clickable { onOpenTarget(target) } else Modifier
                    )
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(saved.targetType, fontWeight = FontWeight.SemiBold)
                            Text(saved.targetId, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item { SectionTitle("公開協作動態") }
        if (projection.activity.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "尚無公開動態",
                    body = "權限、成員、政策與拒絕事件不會出現在社交動態。"
                )
            }
        } else {
            items(projection.activity, key = { it.id }) { activity ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(activity.title, fontWeight = FontWeight.SemiBold)
                        Text(activity.detail, style = MaterialTheme.typography.bodySmall)
                        Text(
                            DateFormat.getDateTimeInstance().format(Date(activity.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
''',
)

print("mobile collaboration v2 navigation, projections and new screens written")
