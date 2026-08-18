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
