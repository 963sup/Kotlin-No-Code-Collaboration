package com.nocodecollaboration.firstprinciples

data class SearchableCollaborationItem(
    val target: CollaborationTarget,
    val title: String,
    val normalizedText: String,
    val verifiedActivityScore: Double = 0.0,
)

data class SavedTarget(
    val userId: String,
    val target: CollaborationTarget,
    val createdAtEpochMillis: Long,
)

/** Authorization is applied before a result can be displayed. */
class ExploreService(
    private val authorization: TargetAuthorization,
) {
    fun search(
        actorUserId: String,
        query: String,
        candidates: List<SearchableCollaborationItem>,
    ): List<SearchableCollaborationItem> {
        val needle = query.trim().lowercase()
        return candidates.asSequence()
            .filter { needle.isBlank() || it.normalizedText.lowercase().contains(needle) }
            .filter { authorization.mayOpen(actorUserId, it.target) }
            .sortedWith(
                compareByDescending<SearchableCollaborationItem> { it.verifiedActivityScore }
                    .thenBy { it.title },
            )
            .toList()
    }
}

data class UserFollow(
    val followerUserId: String,
    val followedUserId: String,
    val createdAtEpochMillis: Long,
)

enum class CollaborationEventKind {
    ISSUE_COMPLETED,
    REVIEW_APPROVED,
    ARTIFACT_PUBLISHED,
    BLOCKER_RESOLVED,
    GOVERNANCE_AUDIT,
}

data class CollaborationEvent(
    val id: String,
    val actorUserId: String,
    val target: CollaborationTarget,
    val kind: CollaborationEventKind,
    val occurredAtEpochMillis: Long,
    /** True only after the source event has passed repository/scope visibility checks. */
    val visibleWithinAuthorizedScope: Boolean,
)

data class AchievementDefinition(
    val id: String,
    val qualifyingKind: CollaborationEventKind,
    val threshold: Int,
    val xp: Int,
)

data class AchievementAward(
    val userId: String,
    val definitionId: String,
    val earnedAtEpochMillis: Long,
    val xp: Int,
)

data class AchievementProjection(
    val xp: Int,
    val level: Int,
    val awards: List<AchievementAward>,
)

/**
 * Feed, trending and achievements are projections of verified events. Audit-only
 * governance events are categorically excluded and no mutable display counter is trusted.
 */
object SocialProjection {
    fun visibleFeed(events: List<CollaborationEvent>): List<CollaborationEvent> = events
        .asSequence()
        .filter { it.kind != CollaborationEventKind.GOVERNANCE_AUDIT }
        .filter { it.visibleWithinAuthorizedScope }
        .sortedByDescending { it.occurredAtEpochMillis }
        .toList()

    fun trending(events: List<CollaborationEvent>): List<Pair<CollaborationTarget, Int>> =
        visibleFeed(events)
            .groupingBy { it.target }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<CollaborationTarget, Int>> { it.value })
            .map { it.key to it.value }

    fun achievements(
        userId: String,
        events: List<CollaborationEvent>,
        definitions: List<AchievementDefinition>,
    ): AchievementProjection {
        val verified = visibleFeed(events).filter { it.actorUserId == userId }
        val awards = definitions.mapNotNull { definition ->
            require(definition.threshold > 0) { "Achievement threshold must be positive" }
            require(definition.xp >= 0) { "Achievement XP cannot be negative" }

            val matching = verified
                .filter { it.kind == definition.qualifyingKind }
                .sortedBy { it.occurredAtEpochMillis }
            if (matching.size < definition.threshold) {
                null
            } else {
                AchievementAward(
                    userId = userId,
                    definitionId = definition.id,
                    earnedAtEpochMillis = matching[definition.threshold - 1].occurredAtEpochMillis,
                    xp = definition.xp,
                )
            }
        }
        val xp = awards.sumOf { it.xp }
        return AchievementProjection(
            xp = xp,
            level = 1 + xp / 1_000,
            awards = awards,
        )
    }
}
