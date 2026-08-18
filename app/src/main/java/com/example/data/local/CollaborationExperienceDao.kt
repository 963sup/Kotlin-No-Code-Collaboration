package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PushRegistration
import com.example.data.model.SavedTarget
import com.example.data.model.SyncConflict
import com.example.data.model.SyncCursor
import com.example.data.model.SyncMetadata
import com.example.data.model.SyncOutbox
import com.example.data.model.SyncRuntimeState
import com.example.data.model.UserFollow
import kotlinx.coroutines.flow.Flow

@Dao
interface CollaborationExperienceDao {
    @Query("SELECT * FROM saved_targets ORDER BY createdAt DESC")
    fun observeSavedTargets(): Flow<List<SavedTarget>>

    @Query("SELECT * FROM saved_targets WHERE userId = :userId AND targetKey = :targetKey LIMIT 1")
    suspend fun getSavedTarget(userId: String, targetKey: String): SavedTarget?

    @Query("SELECT * FROM saved_targets WHERE id = :id LIMIT 1")
    suspend fun getSavedTargetById(id: String): SavedTarget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedTarget(target: SavedTarget)

    @Query("DELETE FROM saved_targets WHERE userId = :userId AND targetKey = :targetKey")
    suspend fun deleteSavedTarget(userId: String, targetKey: String)

    @Query("DELETE FROM saved_targets WHERE id = :id")
    suspend fun deleteSavedTargetById(id: String)

    @Query("SELECT * FROM user_follows ORDER BY createdAt DESC")
    fun observeUserFollows(): Flow<List<UserFollow>>

    @Query(
        "SELECT * FROM user_follows " +
            "WHERE followerUserId = :followerUserId AND followedUserId = :followedUserId LIMIT 1",
    )
    suspend fun getUserFollow(followerUserId: String, followedUserId: String): UserFollow?

    @Query("SELECT * FROM user_follows WHERE id = :id LIMIT 1")
    suspend fun getUserFollowById(id: String): UserFollow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserFollow(follow: UserFollow)

    @Query(
        "DELETE FROM user_follows " +
            "WHERE followerUserId = :followerUserId AND followedUserId = :followedUserId",
    )
    suspend fun deleteUserFollow(followerUserId: String, followedUserId: String)

    @Query("DELETE FROM user_follows WHERE id = :id")
    suspend fun deleteUserFollowById(id: String)

    @Query("SELECT * FROM sync_outbox ORDER BY queuedAt DESC")
    fun observeOutbox(): Flow<List<SyncOutbox>>

    @Query(
        "SELECT * FROM sync_outbox " +
            "WHERE state IN ('PENDING', 'FAILED', 'AUTH_REQUIRED') ORDER BY queuedAt ASC LIMIT :limit",
    )
    suspend fun getPendingOutbox(limit: Int = 50): List<SyncOutbox>

    @Query(
        "SELECT COUNT(*) FROM sync_outbox " +
            "WHERE entityType = :entityType AND entityId = :entityId " +
            "AND state != 'SYNCED'",
    )
    suspend fun countUnsynced(entityType: String, entityId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOutbox(item: SyncOutbox)

    @Query(
        "UPDATE sync_outbox SET state = :state, updatedAt = :updatedAt, " +
            "lastError = :lastError WHERE id = :id",
    )
    suspend fun markOutboxState(
        id: String,
        state: String,
        updatedAt: Long = System.currentTimeMillis(),
        lastError: String? = null,
    )

    @Query(
        "UPDATE sync_outbox SET attemptCount = attemptCount + 1, state = :state, " +
            "updatedAt = :updatedAt, lastError = :lastError WHERE id = :id",
    )
    suspend fun failOutbox(id: String, state: String, lastError: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM sync_outbox WHERE id = :id")
    suspend fun deleteOutbox(id: String)

    @Query("DELETE FROM sync_outbox WHERE state = 'SYNCED' AND updatedAt < :before")
    suspend fun deleteOldSyncedOutbox(before: Long)

    @Query("SELECT * FROM sync_metadata WHERE entityType = :entityType AND entityId = :entityId LIMIT 1")
    suspend fun getSyncMetadata(entityType: String, entityId: String): SyncMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncMetadata(metadata: SyncMetadata)

    @Query("SELECT * FROM sync_cursors WHERE streamId = :streamId LIMIT 1")
    suspend fun getSyncCursor(streamId: String = "enterprise"): SyncCursor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncCursor(cursor: SyncCursor)

    @Query("SELECT * FROM sync_conflicts ORDER BY detectedAt DESC")
    fun observeConflicts(): Flow<List<SyncConflict>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConflict(conflict: SyncConflict)

    @Query("UPDATE sync_conflicts SET resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolveConflict(id: String, resolvedAt: Long = System.currentTimeMillis())

    @Query("SELECT MAX(lastSyncedAt) FROM sync_metadata")
    fun observeLastSyncedAt(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPushRegistration(registration: PushRegistration)

    @Query("SELECT * FROM push_registrations WHERE id = 'device' LIMIT 1")
    suspend fun getPushRegistration(): PushRegistration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRuntimeState(state: SyncRuntimeState)

    @Query("UPDATE sync_runtime_state SET isApplyingRemote = :isApplyingRemote WHERE id = 1")
    suspend fun setRemoteApplyState(isApplyingRemote: Boolean)
}
