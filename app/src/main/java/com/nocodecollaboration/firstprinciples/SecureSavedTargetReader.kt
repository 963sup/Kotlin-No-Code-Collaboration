package com.nocodecollaboration.firstprinciples

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Re-authorizes persisted favorites every time they are observed. Permission loss
 * therefore removes the row from UI projection without disclosing target metadata.
 */
class SecureSavedTargetReader(
    private val sessionProvider: SessionProvider,
    private val resolver: SafeTargetResolver,
    private val dao: FirstPrinciplesDao,
) {
    fun observeCurrentUser(): Flow<List<SavedTargetEntity>> {
        val session = sessionProvider.requireSession()
        return dao.observeSavedTargets(session.userId).map { rows ->
            rows.filter { row ->
                val target = row.toTargetOrNull() ?: return@filter false
                resolver.resolve(session.userId, target) is TargetResolution.Allowed
            }
        }
    }

    private fun SavedTargetEntity.toTargetOrNull(): CollaborationTarget? = when (targetType) {
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
