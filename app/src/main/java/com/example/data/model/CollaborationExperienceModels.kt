package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

object CollaborationTargetType {
    const val REPOSITORY = "REPOSITORY"
    const val ARTIFACT = "ARTIFACT"
    const val ISSUE = "ISSUE"
    const val DISCUSSION = "DISCUSSION"
    const val ORGANIZATION = "ORGANIZATION"
    const val TEAM = "TEAM"
    const val USER = "USER"
}

object SyncEntityType {
    const val REPOSITORY = "repository"
    const val ISSUE = "repo_issue"
    const val ARTIFACT = "artifact"
    const val DISCUSSION = "discussion"
    const val SAVED_TARGET = "saved_target"
    const val USER_FOLLOW = "user_follow"
}

object SyncOperation {
    const val UPSERT = "UPSERT"
    const val DELETE = "DELETE"
}

object SyncState {
    const val PENDING = "PENDING"
    const val IN_FLIGHT = "IN_FLIGHT"
    const val SYNCED = "SYNCED"
    const val FAILED = "FAILED"
    const val CONFLICT = "CONFLICT"
    const val AUTH_REQUIRED = "AUTH_REQUIRED"
}

@Entity(
    tableName = "saved_targets",
    indices = [
        Index(value = ["userId", "targetKey"], unique = true),
        Index(value = ["userId"]),
        Index(value = ["repositoryId"])
    ]
)
data class SavedTarget(
    @PrimaryKey val id: String = "saved_${UUID.randomUUID().toString().take(12)}",
    val userId: String,
    val targetKey: String,
    val targetType: String,
    val targetId: String,
    val repositoryId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_follows",
    indices = [
        Index(value = ["followerUserId", "followedUserId"], unique = true),
        Index(value = ["followerUserId"]),
        Index(value = ["followedUserId"])
    ]
)
data class UserFollow(
    @PrimaryKey val id: String = "follow_${UUID.randomUUID().toString().take(12)}",
    val followerUserId: String,
    val followedUserId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sync_outbox",
    indices = [
        Index(value = ["state", "queuedAt"]),
        Index(value = ["entityType", "entityId"])
    ]
)
data class SyncOutbox(
    @PrimaryKey val id: String = "outbox_${UUID.randomUUID().toString().take(12)}",
    val entityType: String,
    val entityId: String,
    val operation: String = SyncOperation.UPSERT,
    val localVersion: Long = System.currentTimeMillis(),
    val state: String = SyncState.PENDING,
    val attemptCount: Int = 0,
    val queuedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastError: String? = null
)

@Entity(
    tableName = "sync_metadata",
    indices = [Index(value = ["entityType", "entityId"], unique = true)]
)
data class SyncMetadata(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val localVersion: Long,
    val serverVersion: Long? = null,
    val status: String = SyncState.PENDING,
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val lastSyncedAt: Long? = null
)

@Entity(tableName = "sync_cursors")
data class SyncCursor(
    @PrimaryKey val streamId: String = "enterprise",
    val cursor: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sync_conflicts",
    indices = [Index(value = ["entityType", "entityId"]), Index(value = ["resolvedAt"])]
)
data class SyncConflict(
    @PrimaryKey val id: String = "conflict_${UUID.randomUUID().toString().take(12)}",
    val entityType: String,
    val entityId: String,
    val localVersion: Long,
    val remoteVersion: Long,
    val reason: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Entity(tableName = "push_registrations")
data class PushRegistration(
    @PrimaryKey val id: String = "device",
    val token: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_runtime_state")
data class SyncRuntimeState(
    @PrimaryKey val id: Int = 1,
    val isApplyingRemote: Boolean = false
)

data class SyncStatusSummary(
    val pending: Int = 0,
    val failed: Int = 0,
    val conflicts: Int = 0,
    val authRequired: Int = 0,
    val lastSyncedAt: Long? = null
)
