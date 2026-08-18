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
    fun stats(
        user: User,
        auditLogs: List<AuditLog>,
        visibleRepositoryIds: Set<String>
    ): SocialStats {
        val public = auditLogs.filter { log ->
            log.actorUserId == user.id &&
                PublicActivityPolicy.isPublic(log) &&
                log.repoId in visibleRepositoryIds
        }
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
