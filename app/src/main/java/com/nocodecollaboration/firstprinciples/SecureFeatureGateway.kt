package com.nocodecollaboration.firstprinciples

/** Session identity is supplied by the authenticated application layer, never by UI text fields. */
data class ActorSession(
    val enterpriseId: String,
    val userId: String,
    val activeTeamIds: Set<String>,
)

fun interface SessionProvider {
    fun requireSession(): ActorSession
}

/**
 * Central gateway that binds user/enterprise identity to target operations.
 * UI callers never choose another user's ID when listing or mutating favorites/follows.
 */
class SecureFeatureGateway(
    private val sessionProvider: SessionProvider,
    private val targetResolver: SafeTargetResolver,
    private val dao: FirstPrinciplesDao,
) {
    fun resolveForCurrentSession(target: CollaborationTarget): TargetResolution {
        val session = sessionProvider.requireSession()
        return targetResolver.resolve(session.userId, target)
    }

    suspend fun saveForCurrentUser(
        targetType: String,
        targetId: String,
        repositoryId: String?,
        createdAtEpochMillis: Long,
    ): TargetResolution {
        val target = targetFromStorage(targetType, targetId, repositoryId)
            ?: return TargetResolution.Missing(CollaborationTarget.Repository(targetId))
        val resolution = resolveForCurrentSession(target)
        if (resolution !is TargetResolution.Allowed) return resolution

        val session = sessionProvider.requireSession()
        dao.saveTarget(
            SavedTargetEntity(
                userId = session.userId,
                targetType = targetType,
                targetId = targetId,
                repositoryId = repositoryId,
                createdAtEpochMillis = createdAtEpochMillis,
            ),
        )
        return resolution
    }

    suspend fun removeForCurrentUser(targetType: String, targetId: String) {
        val session = sessionProvider.requireSession()
        dao.removeSavedTarget(session.userId, targetType, targetId)
    }

    suspend fun followCurrentUserTarget(
        followedUserId: String,
        createdAtEpochMillis: Long,
    ) {
        val session = sessionProvider.requireSession()
        require(followedUserId != session.userId) { "A user cannot follow themselves" }
        val resolution = targetResolver.resolve(
            session.userId,
            CollaborationTarget.User(followedUserId),
        )
        check(resolution is TargetResolution.Allowed) { "Follow target is missing or unauthorized" }
        dao.follow(
            UserFollowEntity(
                followerUserId = session.userId,
                followedUserId = followedUserId,
                createdAtEpochMillis = createdAtEpochMillis,
            ),
        )
    }

    private fun targetFromStorage(
        targetType: String,
        targetId: String,
        repositoryId: String?,
    ): CollaborationTarget? = when (targetType) {
        "ENTERPRISE" -> CollaborationTarget.Enterprise(targetId)
        "ORGANIZATION" -> CollaborationTarget.Organization(targetId)
        "TEAM" -> CollaborationTarget.Team(targetId)
        "USER" -> CollaborationTarget.User(targetId)
        "REPOSITORY" -> CollaborationTarget.Repository(targetId)
        "ISSUE" -> repositoryId?.let { CollaborationTarget.Issue(targetId, it) }
        "ARTIFACT" -> repositoryId?.let { CollaborationTarget.Artifact(targetId, it) }
        "DISCUSSION" -> repositoryId?.let { CollaborationTarget.Discussion(targetId, it) }
        else -> null
    }
}
