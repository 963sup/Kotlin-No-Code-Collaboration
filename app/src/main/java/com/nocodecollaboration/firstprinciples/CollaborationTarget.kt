package com.nocodecollaboration.firstprinciples

/**
 * One canonical collaboration destination shared by Inbox, Explore, favorites,
 * activity feeds, and notification/deep-link handling.
 */
sealed interface CollaborationTarget {
    val stableId: String

    data class Enterprise(override val stableId: String) : CollaborationTarget
    data class Organization(override val stableId: String) : CollaborationTarget
    data class Team(override val stableId: String) : CollaborationTarget
    data class User(override val stableId: String) : CollaborationTarget
    data class Repository(override val stableId: String) : CollaborationTarget
    data class Issue(
        override val stableId: String,
        val repositoryId: String,
    ) : CollaborationTarget
    data class Artifact(
        override val stableId: String,
        val repositoryId: String,
    ) : CollaborationTarget
    data class Discussion(
        override val stableId: String,
        val repositoryId: String,
    ) : CollaborationTarget
}

sealed interface TargetResolution {
    data class Allowed(val target: CollaborationTarget) : TargetResolution
    data class Missing(val requested: CollaborationTarget) : TargetResolution
    data class Denied(val requested: CollaborationTarget) : TargetResolution
}

fun interface TargetExistence {
    fun exists(target: CollaborationTarget): Boolean
}

fun interface TargetAuthorization {
    fun mayOpen(actorUserId: String, target: CollaborationTarget): Boolean
}

/**
 * Resolves only the exact requested object. A denied or missing Issue/Artifact/
 * Discussion is never widened to its Repository, because that would hide an
 * authorization failure and can disclose broader tenant context.
 */
class SafeTargetResolver(
    private val existence: TargetExistence,
    private val authorization: TargetAuthorization,
) {
    fun resolve(actorUserId: String, target: CollaborationTarget): TargetResolution {
        if (!existence.exists(target)) return TargetResolution.Missing(target)
        if (!authorization.mayOpen(actorUserId, target)) return TargetResolution.Denied(target)
        return TargetResolution.Allowed(target)
    }
}

/** UI-independent navigation contract. */
fun interface CollaborationNavigator {
    fun open(allowed: TargetResolution.Allowed)
}
