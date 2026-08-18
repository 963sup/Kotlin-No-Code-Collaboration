package com.example.navigation

import com.example.data.model.AppNotification
import com.example.data.model.GranteeType
import com.example.data.model.OrgMembership
import com.example.data.model.OwnerType
import com.example.data.model.RepoAccessRule
import com.example.data.model.Repository
import com.example.data.model.TeamMembership
import com.example.data.model.User
import java.util.Locale

/**
 * Canonical, in-app destination for collaboration objects.
 *
 * Repository-scoped resources always carry the Repository identifier so callers
 * can reject cross-container or stale references before navigation.
 */
sealed interface CollaborationTarget {
    data class Repository(val repositoryId: String) : CollaborationTarget
    data class Artifact(val repositoryId: String, val artifactId: String) : CollaborationTarget
    data class Issue(val repositoryId: String, val issueId: String) : CollaborationTarget
    data class Discussion(val repositoryId: String, val discussionId: String) : CollaborationTarget
    data class Organization(val organizationId: String) : CollaborationTarget
    data class Team(val teamId: String) : CollaborationTarget
    data class UserProfile(val userId: String) : CollaborationTarget
}

/**
 * Resolves an AppNotification into one most-specific destination without
 * silently degrading an explicit target to a broader Repository destination.
 */
object CollaborationTargetResolver {
    fun resolve(notification: AppNotification): CollaborationTarget? {
        val action = notification.actionType.identifier()?.uppercase(Locale.ROOT)
        val repositoryId = notification.repoId.identifier()
        val artifactId = notification.artifactId.identifier()
        val issueId = notification.issueId.identifier()
        val discussionId = notification.discussionId.identifier()
        val organizationId = notification.orgId.identifier()
        val teamId = notification.teamId.identifier()

        return when (action) {
            "REVIEW", "APPROVE", "VIEW_ARTIFACT" ->
                repositoryId?.let { repo -> artifactId?.let { CollaborationTarget.Artifact(repo, it) } }

            "VIEW_ISSUE" ->
                repositoryId?.let { repo -> issueId?.let { CollaborationTarget.Issue(repo, it) } }

            "VIEW_DISCUSSION" ->
                repositoryId?.let { repo -> discussionId?.let { CollaborationTarget.Discussion(repo, it) } }

            "VIEW_REPO" -> repositoryId?.let(CollaborationTarget::Repository)

            "VIEW_ORG" -> organizationId?.let(CollaborationTarget::Organization)

            "VIEW_TEAM" -> teamId?.let(CollaborationTarget::Team)

            "VIEW_PROFILE" -> notification.actorUserId.identifier()?.let(CollaborationTarget::UserProfile)

            else -> when {
                repositoryId != null && issueId != null -> CollaborationTarget.Issue(repositoryId, issueId)

                repositoryId != null && discussionId != null -> CollaborationTarget.Discussion(
                    repositoryId,
                    discussionId,
                )

                repositoryId != null && artifactId != null -> CollaborationTarget.Artifact(repositoryId, artifactId)

                repositoryId != null -> CollaborationTarget.Repository(repositoryId)

                teamId != null -> CollaborationTarget.Team(teamId)

                organizationId != null -> CollaborationTarget.Organization(organizationId)

                else -> null
            }
        }
    }

    private fun String?.identifier(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * Read-access boundary used before resolving a notification into a UI surface.
 * This mirrors the existing ownership and grant hierarchy without creating a
 * second policy model.
 */
object CollaborationTargetAccess {
    fun canOpenRepository(
        user: User,
        repository: Repository,
        orgMemberships: List<OrgMembership>,
        teamMemberships: List<TeamMembership>,
        accessRules: List<RepoAccessRule>,
    ): Boolean {
        if (user.enterpriseId != repository.enterpriseId) return false
        if (user.isEnterpriseAdmin) return true
        if (repository.ownerType == OwnerType.USER && repository.ownerId == user.id) return true

        val organizationIds = orgMemberships
            .asSequence()
            .filter { it.userId == user.id }
            .map { it.orgId }
            .toSet()

        if (repository.ownerType == OwnerType.ORGANIZATION && repository.ownerId in organizationIds) {
            return true
        }

        val teamIds = teamMemberships
            .asSequence()
            .filter { it.userId == user.id }
            .map { it.teamId }
            .toSet()

        return accessRules.any { rule ->
            rule.repoId == repository.id && when (rule.granteeType) {
                GranteeType.USER -> rule.granteeId == user.id
                GranteeType.TEAM -> rule.granteeId in teamIds
            }
        }
    }
}
