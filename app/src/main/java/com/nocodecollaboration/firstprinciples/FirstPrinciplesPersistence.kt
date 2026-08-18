package com.nocodecollaboration.firstprinciples

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "saved_targets",
    primaryKeys = ["userId", "targetType", "targetId"],
    indices = [Index("userId"), Index("targetType", "targetId")],
)
data class SavedTargetEntity(
    val userId: String,
    val targetType: String,
    val targetId: String,
    val repositoryId: String?,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "user_follows",
    primaryKeys = ["followerUserId", "followedUserId"],
    indices = [Index("followerUserId"), Index("followedUserId")],
)
data class UserFollowEntity(
    val followerUserId: String,
    val followedUserId: String,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "sync_outbox",
    indices = [
        Index(value = ["enterpriseId", "state", "nextAttemptAtEpochMillis"]),
        Index(value = ["idempotencyKey"], unique = true),
    ],
)
data class SyncOutboxEntity(
    @androidx.room.PrimaryKey val id: String,
    val enterpriseId: String,
    val actorUserId: String,
    val aggregateType: String,
    val aggregateId: String,
    val operation: String,
    val payloadJson: String,
    val expectedServerVersion: Long?,
    val idempotencyKey: String,
    val state: String,
    val attempts: Int,
    val nextAttemptAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
)

@Entity(
    tableName = "sync_cursors",
    primaryKeys = ["enterpriseId", "stream"],
)
data class SyncCursorEntity(
    val enterpriseId: String,
    val stream: String,
    val cursor: String?,
    val updatedAtEpochMillis: Long,
)

@Dao
interface FirstPrinciplesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTarget(entity: SavedTargetEntity)

    @Query("DELETE FROM saved_targets WHERE userId = :userId AND targetType = :targetType AND targetId = :targetId")
    suspend fun removeSavedTarget(userId: String, targetType: String, targetId: String)

    @Query("SELECT * FROM saved_targets WHERE userId = :userId ORDER BY createdAtEpochMillis DESC")
    fun observeSavedTargets(userId: String): Flow<List<SavedTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun follow(entity: UserFollowEntity): Long

    @Query("DELETE FROM user_follows WHERE followerUserId = :followerUserId AND followedUserId = :followedUserId")
    suspend fun unfollow(followerUserId: String, followedUserId: String)

    @Query("SELECT * FROM user_follows WHERE followerUserId = :userId ORDER BY createdAtEpochMillis DESC")
    fun observeFollowing(userId: String): Flow<List<UserFollowEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun enqueueOutbox(entity: SyncOutboxEntity)

    @Query(
        """
        SELECT * FROM sync_outbox
        WHERE enterpriseId = :enterpriseId
          AND state IN ('PENDING', 'RETRY')
          AND nextAttemptAtEpochMillis <= :nowEpochMillis
        ORDER BY createdAtEpochMillis ASC
        LIMIT :limit
        """,
    )
    suspend fun nextOutboxBatch(
        enterpriseId: String,
        nowEpochMillis: Long,
        limit: Int,
    ): List<SyncOutboxEntity>

    @Query("UPDATE sync_outbox SET state = :state, attempts = :attempts, nextAttemptAtEpochMillis = :nextAttemptAtEpochMillis WHERE id = :id")
    suspend fun updateOutboxState(
        id: String,
        state: String,
        attempts: Int,
        nextAttemptAtEpochMillis: Long,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCursor(entity: SyncCursorEntity)

    @Query("SELECT * FROM sync_cursors WHERE enterpriseId = :enterpriseId AND stream = :stream LIMIT 1")
    suspend fun getCursor(enterpriseId: String, stream: String): SyncCursorEntity?
}

/**
 * Explicit additive migration factory. The application database must register
 * this migration for its actual current version and include these entities in
 * its @Database entity list. Destructive migration is not an acceptable fallback.
 */
object FirstPrinciplesSchema {
    fun migrationFrom(currentVersion: Int): Migration = object : Migration(
        currentVersion,
        currentVersion + 1,
    ) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS saved_targets (
                    userId TEXT NOT NULL,
                    targetType TEXT NOT NULL,
                    targetId TEXT NOT NULL,
                    repositoryId TEXT,
                    createdAtEpochMillis INTEGER NOT NULL,
                    PRIMARY KEY(userId, targetType, targetId)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_targets_userId ON saved_targets(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_targets_targetType_targetId ON saved_targets(targetType, targetId)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_follows (
                    followerUserId TEXT NOT NULL,
                    followedUserId TEXT NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL,
                    PRIMARY KEY(followerUserId, followedUserId)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_follows_followerUserId ON user_follows(followerUserId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_follows_followedUserId ON user_follows(followedUserId)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS sync_outbox (
                    id TEXT NOT NULL PRIMARY KEY,
                    enterpriseId TEXT NOT NULL,
                    actorUserId TEXT NOT NULL,
                    aggregateType TEXT NOT NULL,
                    aggregateId TEXT NOT NULL,
                    operation TEXT NOT NULL,
                    payloadJson TEXT NOT NULL,
                    expectedServerVersion INTEGER,
                    idempotencyKey TEXT NOT NULL,
                    state TEXT NOT NULL,
                    attempts INTEGER NOT NULL,
                    nextAttemptAtEpochMillis INTEGER NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_enterpriseId_state_nextAttemptAtEpochMillis ON sync_outbox(enterpriseId, state, nextAttemptAtEpochMillis)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sync_outbox_idempotencyKey ON sync_outbox(idempotencyKey)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS sync_cursors (
                    enterpriseId TEXT NOT NULL,
                    stream TEXT NOT NULL,
                    cursor TEXT,
                    updatedAtEpochMillis INTEGER NOT NULL,
                    PRIMARY KEY(enterpriseId, stream)
                )
                """.trimIndent(),
            )
        }
    }
}
