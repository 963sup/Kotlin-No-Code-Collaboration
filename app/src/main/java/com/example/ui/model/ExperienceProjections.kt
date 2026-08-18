package com.example.ui.model

import com.example.data.model.AppNotification
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.GranteeType
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationStatus
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.ReviewDecision
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget
import com.example.navigation.storageKey
import com.example.navigation.toCollaborationTarget
import com.example.ui.components.WorkspaceScopeKind

data class ExploreResult(
    val target: CollaborationTarget,
    val typeLabel: String,
    val title: String,
    val subtitle: String,
    val searchableText: String,
    val score: Int,
    val isSaved: Boolean,
)

enum class ExploreCategory(val label: String) {
    ALL("全部"),
    REPOSITORIES("儲存庫"),
    WORK("工作"),
    DOCUMENTS("成果"),
    DISCUSSIONS("討論"),
    PEOPLE("人員與團隊"),
}

fun ExploreResult.matches(category: ExploreCategory): Boolean = when (category) {
    ExploreCategory.ALL -> true

    ExploreCategory.REPOSITORIES -> target is CollaborationTarget.Repository

    ExploreCategory.WORK -> target is CollaborationTarget.Issue

    ExploreCategory.DOCUMENTS -> target is CollaborationTarget.Artifact

    ExploreCategory.DISCUSSIONS -> target is CollaborationTarget.Discussion

    ExploreCategory.PEOPLE ->
        target is CollaborationTarget.Organization ||
            target is CollaborationTarget.Team ||
            target is CollaborationTarget.UserProfile
}

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
        now: Long = System.currentTimeMillis(),
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
            updatedAt: Long,
        ) = ExploreResult(
            target = target,
            typeLabel = type,
            title = title,
            subtitle = subtitle,
            searchableText = search,
            score = recency(updatedAt),
            isSaved = target.storageKey() in savedKeys,
        )

        val repoItems = repositories.map { repo ->
            item(
                CollaborationTarget.Repository(repo.id),
                "儲存庫",
                repo.displayName,
                repo.description.ifBlank { "無程式碼協作容器" },
                "${repo.name} ${repo.displayName} ${repo.description} ${repo.category}",
                repo.updatedAt,
            )
        }
        val issueItems = issues.filter { it.repoId in repoById }.map { issue ->
            val repo = repoById.getValue(issue.repoId)
            item(
                CollaborationTarget.Issue(issue.repoId, issue.id),
                "Issue",
                "#${issue.issueNumber} ${issue.title}",
                "${repo.displayName} · ${issue.status.label}",
                "${issue.title} ${issue.description} ${issue.labels} ${repo.displayName}",
                issue.updatedAt,
            )
        }
        val artifactItems = artifacts.filter { it.repoId in repoById }.map { artifact ->
            val repo = repoById.getValue(artifact.repoId)
            item(
                CollaborationTarget.Artifact(artifact.repoId, artifact.id),
                "成果",
                artifact.title,
                "${repo.displayName} · ${artifact.lifecycleState.label}",
                "${artifact.title} ${artifact.summary} ${artifact.type.label} ${repo.displayName}",
                artifact.updatedAt,
            )
        }
        val discussionItems = discussions.filter { it.repoId in repoById }.map { discussion ->
            val repo = repoById.getValue(discussion.repoId)
            item(
                CollaborationTarget.Discussion(discussion.repoId, discussion.id),
                "討論",
                discussion.title,
                "${repo.displayName} · ${discussion.category.label}",
                "${discussion.title} ${discussion.body} ${discussion.category.label} ${repo.displayName}",
                discussion.updatedAt,
            )
        }
        val orgItems = organizations.map { org ->
            item(
                CollaborationTarget.Organization(org.id),
                "組織",
                org.name,
                org.description.ifBlank { "企業組織" },
                "${org.name} ${org.slug} ${org.description}",
                org.createdAt,
            )
        }
        val teamItems = teams.map { team ->
            item(
                CollaborationTarget.Team(team.id),
                "團隊",
                team.name,
                team.description.ifBlank { "協作團隊" },
                "${team.name} ${team.slug} ${team.description}",
                team.createdAt,
            )
        }
        val userItems = users.filter { it.enterpriseId == user.enterpriseId }.map { profile ->
            item(
                CollaborationTarget.UserProfile(profile.id),
                "用戶",
                profile.displayName,
                "@${profile.username} · ${profile.title}",
                "${profile.displayName} ${profile.username} ${profile.email} ${profile.title}",
                profile.createdAt,
            )
        }
        return (repoItems + issueItems + artifactItems + discussionItems + orgItems + teamItems + userItems)
            .sortedWith(compareByDescending<ExploreResult> { it.score }.thenBy { it.title })
    }
}

data class ScopeOperationalSummary(val title: String, val healthScore: Int, val detail: String)

object ScopeOperationalProjection {
    fun build(
        scopeKind: WorkspaceScopeKind?,
        scopeName: String?,
        repositories: List<Repository>,
        issues: List<RepoIssue>,
        artifacts: List<NoCodeArtifact>,
        reviews: List<ArtifactReview>,
        approvals: List<ArtifactApproval>,
        notifications: List<AppNotification>,
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
            detail = "${repositories.size} 儲存庫 · ${issues.size} Issue · ${artifacts.size} 成果 · ${reviews.size} 審查 · ${approvals.size} 核准",
        )
    }
}

data class TeamMemberSummary(val user: User, val role: TeamRole)

data class TeamRepositorySummary(
    val repository: Repository,
    val role: RepoRole,
    val issueCount: Int,
    val openIssueCount: Int,
    val overallProgress: Float,
)

data class TeamSpaceSummary(
    val team: Team,
    val members: List<TeamMemberSummary>,
    val repositories: List<TeamRepositorySummary>,
)

object TeamSpaceProjection {
    fun build(
        team: Team,
        users: List<User>,
        memberships: List<TeamMembership>,
        repositories: List<Repository>,
        accessRules: List<RepoAccessRule>,
        issues: List<RepoIssue>,
    ): TeamSpaceSummary {
        val usersById = users.associateBy { it.id }
        val members = memberships.asSequence()
            .filter { it.teamId == team.id }
            .mapNotNull { membership ->
                usersById[membership.userId]?.let { TeamMemberSummary(it, membership.role) }
            }
            .sortedWith(
                compareByDescending<TeamMemberSummary> {
                    it.role == TeamRole.MAINTAINER
                }.thenBy { it.user.displayName },
            )
            .toList()

        val strongestRuleByRepo = accessRules.asSequence()
            .filter { it.granteeType == GranteeType.TEAM && it.granteeId == team.id }
            .groupBy { it.repoId }
            .mapValues { (_, rules) -> rules.maxBy { it.role.rank } }
        val issuesByRepo = issues.groupBy { it.repoId }
        val repoSummaries = repositories.asSequence()
            .filter { it.id in strongestRuleByRepo }
            .map { repository ->
                val repoIssues = issuesByRepo[repository.id].orEmpty()
                TeamRepositorySummary(
                    repository = repository,
                    role = strongestRuleByRepo.getValue(repository.id).role,
                    issueCount = repoIssues.size,
                    openIssueCount = repoIssues.count { it.status != IssueStatus.CLOSED },
                    overallProgress = IssueHierarchyRules.overallProgress(repoIssues),
                )
            }
            .sortedBy { it.repository.displayName }
            .toList()
        return TeamSpaceSummary(team = team, members = members, repositories = repoSummaries)
    }
}

data class RepositoryOverviewSummary(
    val repository: Repository,
    val wbsCount: Int,
    val rootWbsCount: Int,
    val issueCount: Int,
    val openIssueCount: Int,
    val memberCount: Int,
    val overallProgress: Float,
    val recentActivity: List<AuditLog>,
)

object RepositoryOverviewProjection {
    fun build(
        repository: Repository,
        issues: List<RepoIssue>,
        accessRules: List<RepoAccessRule>,
        orgMemberships: List<OrgMembership>,
        teamMemberships: List<TeamMembership>,
        auditLogs: List<AuditLog>,
    ): RepositoryOverviewSummary {
        val repoIssues = issues.filter { it.repoId == repository.id }
        val rows = IssueHierarchyRules.wbsProjection(repoIssues)
        val rules = accessRules.filter { it.repoId == repository.id }
        val participantUserIds = linkedSetOf<String>()
        when (repository.ownerType) {
            OwnerType.USER -> participantUserIds += repository.ownerId

            OwnerType.ORGANIZATION ->
                participantUserIds += orgMemberships
                    .filter { it.orgId == repository.ownerId }
                    .map { it.userId }
        }
        participantUserIds += rules.filter { it.granteeType == GranteeType.USER }.map { it.granteeId }
        val grantedTeamIds = rules.filter { it.granteeType == GranteeType.TEAM }.map { it.granteeId }.toSet()
        participantUserIds += teamMemberships.filter { it.teamId in grantedTeamIds }.map { it.userId }
        return RepositoryOverviewSummary(
            repository = repository,
            wbsCount = rows.size,
            rootWbsCount = rows.count { it.depth == 0 },
            issueCount = repoIssues.size,
            openIssueCount = repoIssues.count { it.status != IssueStatus.CLOSED },
            memberCount = participantUserIds.size,
            overallProgress = IssueHierarchyRules.overallProgress(repoIssues),
            recentActivity = auditLogs.filter { it.repoId == repository.id }
                .sortedByDescending { it.timestamp }
                .take(5),
        )
    }
}

enum class InboxPrimaryView(val label: String) {
    ALL("全部"),
    MENTIONS_OR_ACTION("提及／需處理"),
    SYSTEM("系統"),
    UNREAD("未讀"),
}

private val systemNotificationCategories = setOf(
    NotificationCategory.ACCESS_CHANGE,
    NotificationCategory.MEMBERSHIP_CHANGE,
    NotificationCategory.PUBLICATION,
    NotificationCategory.GOVERNANCE_EVENT,
)

fun AppNotification.matches(view: InboxPrimaryView): Boolean = when (view) {
    InboxPrimaryView.ALL -> status != NotificationStatus.ARCHIVED

    InboxPrimaryView.MENTIONS_OR_ACTION ->
        status != NotificationStatus.ARCHIVED &&
            (category == NotificationCategory.MENTION_AND_REPLY || isActionable)

    InboxPrimaryView.SYSTEM -> status != NotificationStatus.ARCHIVED && category in systemNotificationCategories

    InboxPrimaryView.UNREAD -> status == NotificationStatus.UNREAD
}

object PublicActivityPolicy {
    private val allowedPrefixes = listOf(
        "CREATE_", "SUBMIT_", "PUBLISH", "CLOSE_ISSUE", "REOPEN_ISSUE",
        "ASSIGN_ISSUE", "LINK_PARENT_ISSUE", "ADD_ISSUE_DEPENDENCY", "UPDATE_ISSUE_PLAN",
    )
    fun isPublic(log: AuditLog): Boolean = log.verdict == PolicyVerdict.ALLOWED &&
        log.repoId != null &&
        allowedPrefixes.any { prefix -> log.actionName.startsWith(prefix) }
}

data class FollowingActivityItem(
    val actorUserId: String,
    val actorDisplayName: String,
    val actionName: String,
    val repositoryId: String,
    val repositoryName: String,
    val timestamp: Long,
)

object FollowingActivityProjection {
    fun build(
        activeUserId: String,
        follows: List<UserFollow>,
        auditLogs: List<AuditLog>,
        visibleRepositoryIds: Set<String>,
        limit: Int = 50,
    ): List<FollowingActivityItem> {
        val followedIds = follows.asSequence()
            .filter { it.followerUserId == activeUserId }
            .map { it.followedUserId }
            .toSet()
        return auditLogs.asSequence()
            .filter { it.actorUserId in followedIds }
            .filter(PublicActivityPolicy::isPublic)
            .filter { it.repoId in visibleRepositoryIds }
            .sortedByDescending { it.timestamp }
            .take(limit.coerceAtLeast(0))
            .map { log ->
                FollowingActivityItem(
                    actorUserId = log.actorUserId,
                    actorDisplayName = log.actorDisplayName,
                    actionName = log.actionName,
                    repositoryId = requireNotNull(log.repoId),
                    repositoryName = log.repoName ?: "儲存庫",
                    timestamp = log.timestamp,
                )
            }
            .toList()
    }
}

enum class SavedGroup(val label: String) {
    REPOSITORIES("儲存庫"),
    WORK("工作"),
    DOCUMENTS("成果／文件"),
    DISCUSSIONS("討論"),
    PEOPLE("人員與團隊"),
}

data class SavedGroupItems(val group: SavedGroup, val targets: List<CollaborationTarget>)

object SavedProjection {
    fun build(userId: String, savedTargets: List<SavedTarget>): List<SavedGroupItems> {
        val grouped = savedTargets.asSequence()
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }
            .mapNotNull { it.toCollaborationTarget() }
            .groupBy(::groupFor)
        return SavedGroup.entries.mapNotNull { group ->
            grouped[group]?.takeIf { it.isNotEmpty() }?.let { SavedGroupItems(group, it) }
        }
    }

    private fun groupFor(target: CollaborationTarget): SavedGroup = when (target) {
        is CollaborationTarget.Repository -> SavedGroup.REPOSITORIES

        is CollaborationTarget.Issue -> SavedGroup.WORK

        is CollaborationTarget.Artifact -> SavedGroup.DOCUMENTS

        is CollaborationTarget.Discussion -> SavedGroup.DISCUSSIONS

        is CollaborationTarget.Organization,
        is CollaborationTarget.Team,
        is CollaborationTarget.UserProfile,
        -> SavedGroup.PEOPLE
    }
}

enum class AchievementBadge(val title: String, val description: String, val requiredEvidence: Int) {
    FIRST_COMPLETION("首次交付", "完成第一個可驗證的 Issue", 1),
    RELIABLE_DELIVERY("穩定交付", "完成 10 個可驗證的 Issue", 10),
    FIRST_REVIEW("首次審查", "完成第一個正式審查", 1),
    REVIEW_PRACTITIONER("審查實踐者", "完成 10 個正式審查", 10),
    FIRST_PUBLICATION("首次發布", "發布第一個正式成果", 1),
    PUBLIC_COLLABORATOR("公開協作者", "完成 20 次可公開協作行為", 20),
}

data class AchievementState(val badge: AchievementBadge, val evidenceCount: Int, val unlocked: Boolean)

object AchievementProjection {
    fun build(
        user: User,
        issues: List<RepoIssue>,
        reviews: List<ArtifactReview>,
        artifacts: List<NoCodeArtifact>,
        auditLogs: List<AuditLog>,
        visibleRepositoryIds: Set<String>,
    ): List<AchievementState> {
        val completedIssues = issues.count { issue ->
            issue.repoId in visibleRepositoryIds &&
                issue.status == IssueStatus.CLOSED &&
                (
                    issue.closedByUserId == user.id ||
                        (issue.assigneeType == GranteeType.USER && issue.assigneeId == user.id)
                    )
        }
        val completedReviews = reviews.count { review ->
            review.reviewerUserId == user.id && review.decision != ReviewDecision.COMMENTED
        }
        val publishedArtifacts = artifacts.count { artifact ->
            artifact.repoId in visibleRepositoryIds &&
                artifact.authorUserId == user.id &&
                artifact.lifecycleState == LifecycleState.PUBLISHED
        }
        val publicActions = auditLogs.count { log ->
            log.actorUserId == user.id &&
                log.repoId in visibleRepositoryIds &&
                PublicActivityPolicy.isPublic(log)
        }
        fun state(badge: AchievementBadge, evidenceCount: Int) = AchievementState(
            badge = badge,
            evidenceCount = evidenceCount,
            unlocked = evidenceCount >= badge.requiredEvidence,
        )
        return listOf(
            state(AchievementBadge.FIRST_COMPLETION, completedIssues),
            state(AchievementBadge.RELIABLE_DELIVERY, completedIssues),
            state(AchievementBadge.FIRST_REVIEW, completedReviews),
            state(AchievementBadge.REVIEW_PRACTITIONER, completedReviews),
            state(AchievementBadge.FIRST_PUBLICATION, publishedArtifacts),
            state(AchievementBadge.PUBLIC_COLLABORATOR, publicActions),
        )
    }
}

data class SocialStats(val xp: Int, val level: Int, val publicActions: Int)

object SocialProjection {
    fun stats(user: User, auditLogs: List<AuditLog>, visibleRepositoryIds: Set<String>): SocialStats {
        val public = auditLogs.filter { log ->
            log.actorUserId == user.id &&
                PublicActivityPolicy.isPublic(log) &&
                log.repoId in visibleRepositoryIds
        }
        val xp = public.fold(0) { total, log ->
            total + when {
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
