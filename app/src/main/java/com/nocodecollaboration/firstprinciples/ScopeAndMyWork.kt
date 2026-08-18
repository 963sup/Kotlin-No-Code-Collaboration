package com.nocodecollaboration.firstprinciples

enum class WorkspaceScopeKind {
    ENTERPRISE,
    ORGANIZATION,
    TEAM,
    USER,
}

data class WorkspaceScope(
    val kind: WorkspaceScopeKind,
    val id: String,
)

data class ScopedOperationalRecord(
    val enterpriseId: String?,
    val organizationId: String?,
    val teamIds: Set<String>,
    val userIds: Set<String>,
    val repositoryId: String?,
    val openIssues: Int = 0,
    val blockedIssues: Int = 0,
    val unreadNotifications: Int = 0,
    val recentArtifacts: Int = 0,
    val recentAuditEvents: Int = 0,
)

data class OperationalSummary(
    val accessibleRepositoryCount: Int,
    val openIssueCount: Int,
    val blockedIssueCount: Int,
    val pendingNotificationCount: Int,
    val recentArtifactCount: Int,
    val recentAuditEventCount: Int,
)

/**
 * Produces Home summaries from existing operational records. It intentionally
 * has no persisted dashboard entity, so every scope remains a projection of
 * current Organization/Team/Repository/Issue/Artifact/Notification/Audit data.
 */
object ScopeDashboardProjector {
    fun project(
        scope: WorkspaceScope,
        rows: List<ScopedOperationalRecord>,
    ): OperationalSummary {
        val visible = rows.filter { row ->
            when (scope.kind) {
                WorkspaceScopeKind.ENTERPRISE -> row.enterpriseId == scope.id
                WorkspaceScopeKind.ORGANIZATION -> row.organizationId == scope.id
                WorkspaceScopeKind.TEAM -> scope.id in row.teamIds
                WorkspaceScopeKind.USER -> scope.id in row.userIds
            }
        }

        return OperationalSummary(
            accessibleRepositoryCount = visible.mapNotNull { it.repositoryId }.distinct().size,
            openIssueCount = visible.sumOf { it.openIssues },
            blockedIssueCount = visible.sumOf { it.blockedIssues },
            pendingNotificationCount = visible.sumOf { it.unreadNotifications },
            recentArtifactCount = visible.sumOf { it.recentArtifacts },
            recentAuditEventCount = visible.sumOf { it.recentAuditEvents },
        )
    }
}

enum class WorkStatus {
    TODO,
    IN_PROGRESS,
    DONE,
}

data class AccessibleIssue(
    val issueId: String,
    val repositoryId: String,
    val assigneeUserId: String?,
    val assigneeTeamId: String?,
    val status: WorkStatus,
    val title: String,
)

data class MyWorkQuery(
    val activeUserId: String,
    val activeTeamIds: Set<String>,
    val accessibleRepositoryIds: Set<String>,
    val repositoryFilter: String? = null,
)

/** Mobile My Work is another Issue view; it does not persist independent cards. */
object MyWorkProjector {
    fun group(
        query: MyWorkQuery,
        issues: List<AccessibleIssue>,
    ): Map<WorkStatus, List<AccessibleIssue>> = issues.asSequence()
        .filter { it.repositoryId in query.accessibleRepositoryIds }
        .filter { query.repositoryFilter == null || it.repositoryId == query.repositoryFilter }
        .filter {
            it.assigneeUserId == query.activeUserId ||
                it.assigneeTeamId != null && it.assigneeTeamId in query.activeTeamIds
        }
        .sortedWith(compareBy<AccessibleIssue> { it.repositoryId }.thenBy { it.title })
        .groupBy { it.status }
}
