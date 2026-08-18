from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def write(rel: str, content: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")


def replace_once(rel: str, old: str, new: str) -> None:
    path = ROOT / rel
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{rel}: expected one marker, found {count}: {old[:120]!r}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")


write(
    "app/src/main/java/com/example/data/model/CollaborationExperienceModels.kt",
    r'''
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
''',
)

write(
    "app/src/main/java/com/example/data/local/CollaborationExperienceDao.kt",
    r'''
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
            "WHERE followerUserId = :followerUserId AND followedUserId = :followedUserId LIMIT 1"
    )
    suspend fun getUserFollow(followerUserId: String, followedUserId: String): UserFollow?

    @Query("SELECT * FROM user_follows WHERE id = :id LIMIT 1")
    suspend fun getUserFollowById(id: String): UserFollow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserFollow(follow: UserFollow)

    @Query(
        "DELETE FROM user_follows " +
            "WHERE followerUserId = :followerUserId AND followedUserId = :followedUserId"
    )
    suspend fun deleteUserFollow(followerUserId: String, followedUserId: String)

    @Query("DELETE FROM user_follows WHERE id = :id")
    suspend fun deleteUserFollowById(id: String)

    @Query("SELECT * FROM sync_outbox ORDER BY queuedAt DESC")
    fun observeOutbox(): Flow<List<SyncOutbox>>

    @Query(
        "SELECT * FROM sync_outbox " +
            "WHERE state IN ('PENDING', 'FAILED') ORDER BY queuedAt ASC LIMIT :limit"
    )
    suspend fun getPendingOutbox(limit: Int = 50): List<SyncOutbox>

    @Query(
        "SELECT COUNT(*) FROM sync_outbox " +
            "WHERE entityType = :entityType AND entityId = :entityId " +
            "AND state IN ('PENDING', 'FAILED', 'IN_FLIGHT')"
    )
    suspend fun countUnsynced(entityType: String, entityId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOutbox(item: SyncOutbox)

    @Query(
        "UPDATE sync_outbox SET state = :state, updatedAt = :updatedAt, " +
            "lastError = :lastError WHERE id = :id"
    )
    suspend fun markOutboxState(
        id: String,
        state: String,
        updatedAt: Long = System.currentTimeMillis(),
        lastError: String? = null
    )

    @Query(
        "UPDATE sync_outbox SET attemptCount = attemptCount + 1, state = :state, " +
            "updatedAt = :updatedAt, lastError = :lastError WHERE id = :id"
    )
    suspend fun failOutbox(
        id: String,
        state: String,
        lastError: String,
        updatedAt: Long = System.currentTimeMillis()
    )

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
''',
)

write(
    "app/src/main/java/com/example/data/local/AppMigrations.kt",
    r'''
package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppMigrations {
    const val CURRENT_VERSION = 5

    val MIGRATION_4_5_STATEMENTS: List<String> = listOf(
        "ALTER TABLE repo_issues ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE repo_issues ADD COLUMN plannedStartAt INTEGER",
        "ALTER TABLE repo_issues ADD COLUMN plannedEndAt INTEGER",
        "ALTER TABLE repo_issues ADD COLUMN wbsWeight REAL NOT NULL DEFAULT 1.0",
        "ALTER TABLE repo_issues ADD COLUMN progressPercent INTEGER NOT NULL DEFAULT 0",
        "UPDATE repo_issues SET sortOrder = issueNumber",
        "UPDATE repo_issues SET progressPercent = CASE WHEN status = 'CLOSED' THEN 100 ELSE 0 END",
        """
        CREATE TABLE IF NOT EXISTS saved_targets (
            id TEXT NOT NULL PRIMARY KEY,
            userId TEXT NOT NULL,
            targetKey TEXT NOT NULL,
            targetType TEXT NOT NULL,
            targetId TEXT NOT NULL,
            repositoryId TEXT NOT NULL,
            createdAt INTEGER NOT NULL
        )
        """.trimIndent(),
        "CREATE UNIQUE INDEX IF NOT EXISTS index_saved_targets_userId_targetKey ON saved_targets(userId, targetKey)",
        "CREATE INDEX IF NOT EXISTS index_saved_targets_userId ON saved_targets(userId)",
        "CREATE INDEX IF NOT EXISTS index_saved_targets_repositoryId ON saved_targets(repositoryId)",
        """
        CREATE TABLE IF NOT EXISTS user_follows (
            id TEXT NOT NULL PRIMARY KEY,
            followerUserId TEXT NOT NULL,
            followedUserId TEXT NOT NULL,
            createdAt INTEGER NOT NULL
        )
        """.trimIndent(),
        "CREATE UNIQUE INDEX IF NOT EXISTS index_user_follows_followerUserId_followedUserId ON user_follows(followerUserId, followedUserId)",
        "CREATE INDEX IF NOT EXISTS index_user_follows_followerUserId ON user_follows(followerUserId)",
        "CREATE INDEX IF NOT EXISTS index_user_follows_followedUserId ON user_follows(followedUserId)",
        """
        CREATE TABLE IF NOT EXISTS sync_outbox (
            id TEXT NOT NULL PRIMARY KEY,
            entityType TEXT NOT NULL,
            entityId TEXT NOT NULL,
            operation TEXT NOT NULL,
            localVersion INTEGER NOT NULL,
            state TEXT NOT NULL,
            attemptCount INTEGER NOT NULL,
            queuedAt INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL,
            lastError TEXT
        )
        """.trimIndent(),
        "CREATE INDEX IF NOT EXISTS index_sync_outbox_state_queuedAt ON sync_outbox(state, queuedAt)",
        "CREATE INDEX IF NOT EXISTS index_sync_outbox_entityType_entityId ON sync_outbox(entityType, entityId)",
        """
        CREATE TABLE IF NOT EXISTS sync_metadata (
            id TEXT NOT NULL PRIMARY KEY,
            entityType TEXT NOT NULL,
            entityId TEXT NOT NULL,
            localVersion INTEGER NOT NULL,
            serverVersion INTEGER,
            status TEXT NOT NULL,
            updatedAt INTEGER NOT NULL,
            deletedAt INTEGER,
            lastSyncedAt INTEGER
        )
        """.trimIndent(),
        "CREATE UNIQUE INDEX IF NOT EXISTS index_sync_metadata_entityType_entityId ON sync_metadata(entityType, entityId)",
        """
        CREATE TABLE IF NOT EXISTS sync_cursors (
            streamId TEXT NOT NULL PRIMARY KEY,
            cursor TEXT,
            updatedAt INTEGER NOT NULL
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS sync_conflicts (
            id TEXT NOT NULL PRIMARY KEY,
            entityType TEXT NOT NULL,
            entityId TEXT NOT NULL,
            localVersion INTEGER NOT NULL,
            remoteVersion INTEGER NOT NULL,
            reason TEXT NOT NULL,
            detectedAt INTEGER NOT NULL,
            resolvedAt INTEGER
        )
        """.trimIndent(),
        "CREATE INDEX IF NOT EXISTS index_sync_conflicts_entityType_entityId ON sync_conflicts(entityType, entityId)",
        "CREATE INDEX IF NOT EXISTS index_sync_conflicts_resolvedAt ON sync_conflicts(resolvedAt)",
        """
        CREATE TABLE IF NOT EXISTS push_registrations (
            id TEXT NOT NULL PRIMARY KEY,
            token TEXT NOT NULL,
            updatedAt INTEGER NOT NULL
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS sync_runtime_state (
            id INTEGER NOT NULL PRIMARY KEY,
            isApplyingRemote INTEGER NOT NULL
        )
        """.trimIndent(),
        "INSERT OR IGNORE INTO sync_runtime_state(id, isApplyingRemote) VALUES(1, 0)"
    )

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            MIGRATION_4_5_STATEMENTS.forEach(database::execSQL)
            DatabaseSyncTriggers.install(database)
        }
    }
}
''',
)

write(
    "app/src/main/java/com/example/data/local/DatabaseSyncTriggers.kt",
    r'''
package com.example.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Local-first sync uses SQLite triggers as the smallest reliable write boundary.
 * Business mutations remain in repositories; triggers only record changed IDs.
 * Remote application sets a guard row so pulled changes do not echo back out.
 */
object DatabaseSyncTriggers {
    private const val NOW_MS = "CAST((julianday('now') - 2440587.5) * 86400000 AS INTEGER)"

    private val trackedTables = linkedMapOf(
        "repositories" to "repository",
        "repo_issues" to "repo_issue",
        "no_code_artifacts" to "artifact",
        "repo_discussions" to "discussion",
        "saved_targets" to "saved_target",
        "user_follows" to "user_follow"
    )

    val callback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            install(db)
        }
    }

    fun install(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO sync_runtime_state(id, isApplyingRemote) VALUES(1, 0)")
        trackedTables.forEach { (table, entityType) ->
            db.execSQL(upsertTrigger(table, entityType, "INSERT"))
            db.execSQL(upsertTrigger(table, entityType, "UPDATE"))
            db.execSQL(deleteTrigger(table, entityType))
        }
    }

    private fun upsertTrigger(table: String, entityType: String, event: String): String {
        val suffix = event.lowercase()
        return """
            CREATE TRIGGER IF NOT EXISTS sync_${table}_$suffix
            AFTER $event ON $table
            WHEN COALESCE((SELECT isApplyingRemote FROM sync_runtime_state WHERE id = 1), 0) = 0
            BEGIN
                DELETE FROM sync_outbox
                WHERE entityType = '$entityType' AND entityId = NEW.id
                  AND state IN ('PENDING', 'FAILED');
                INSERT INTO sync_outbox(
                    id, entityType, entityId, operation, localVersion,
                    state, attemptCount, queuedAt, updatedAt, lastError
                ) VALUES(
                    'outbox_' || lower(hex(randomblob(8))), '$entityType', NEW.id, 'UPSERT',
                    $NOW_MS, 'PENDING', 0, $NOW_MS, $NOW_MS, NULL
                );
                INSERT OR IGNORE INTO sync_metadata(
                    id, entityType, entityId, localVersion, serverVersion,
                    status, updatedAt, deletedAt, lastSyncedAt
                ) VALUES(
                    '$entityType:' || NEW.id, '$entityType', NEW.id, $NOW_MS, NULL,
                    'PENDING', $NOW_MS, NULL, NULL
                );
                UPDATE sync_metadata
                SET localVersion = $NOW_MS, status = 'PENDING', updatedAt = $NOW_MS, deletedAt = NULL
                WHERE id = '$entityType:' || NEW.id;
            END
        """.trimIndent()
    }

    private fun deleteTrigger(table: String, entityType: String): String = """
        CREATE TRIGGER IF NOT EXISTS sync_${table}_delete
        AFTER DELETE ON $table
        WHEN COALESCE((SELECT isApplyingRemote FROM sync_runtime_state WHERE id = 1), 0) = 0
        BEGIN
            DELETE FROM sync_outbox
            WHERE entityType = '$entityType' AND entityId = OLD.id
              AND state IN ('PENDING', 'FAILED');
            INSERT INTO sync_outbox(
                id, entityType, entityId, operation, localVersion,
                state, attemptCount, queuedAt, updatedAt, lastError
            ) VALUES(
                'outbox_' || lower(hex(randomblob(8))), '$entityType', OLD.id, 'DELETE',
                $NOW_MS, 'PENDING', 0, $NOW_MS, $NOW_MS, NULL
            );
            INSERT OR IGNORE INTO sync_metadata(
                id, entityType, entityId, localVersion, serverVersion,
                status, updatedAt, deletedAt, lastSyncedAt
            ) VALUES(
                '$entityType:' || OLD.id, '$entityType', OLD.id, $NOW_MS, NULL,
                'PENDING', $NOW_MS, $NOW_MS, NULL
            );
            UPDATE sync_metadata
            SET localVersion = $NOW_MS, status = 'PENDING', updatedAt = $NOW_MS, deletedAt = $NOW_MS
            WHERE id = '$entityType:' || OLD.id;
        END
    """.trimIndent()
}
''',
)

write(
    "app/src/main/java/com/example/data/local/AppDatabase.kt",
    r'''
package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AppNotification
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.DiscussionComment
import com.example.data.model.Enterprise
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.PushRegistration
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.SyncConflict
import com.example.data.model.SyncCursor
import com.example.data.model.SyncMetadata
import com.example.data.model.SyncOutbox
import com.example.data.model.SyncRuntimeState
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.data.model.UserFollow

@Database(
    entities = [
        Enterprise::class,
        Organization::class,
        User::class,
        Team::class,
        TeamMembership::class,
        OrgMembership::class,
        Repository::class,
        RepoAccessRule::class,
        NoCodeArtifact::class,
        ArtifactReview::class,
        ArtifactApproval::class,
        AuditLog::class,
        RepoIssue::class,
        IssueDependency::class,
        IssueComment::class,
        RepoDiscussion::class,
        DiscussionComment::class,
        AppNotification::class,
        SavedTarget::class,
        UserFollow::class,
        SyncOutbox::class,
        SyncMetadata::class,
        SyncCursor::class,
        SyncConflict::class,
        PushRegistration::class,
        SyncRuntimeState::class
    ],
    version = AppMigrations.CURRENT_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun governanceDao(): GovernanceDao
    abstract fun collaborationExperienceDao(): CollaborationExperienceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "repo_governance_db"
                )
                    .addMigrations(AppMigrations.MIGRATION_4_5)
                    .addCallback(DatabaseSyncTriggers.callback)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
''',
)

replace_once(
    "app/src/main/java/com/example/data/model/GovernanceModels.kt",
    '''    val parentIssueTitle: String? = null,\n    val labels: String = "governance",     // Comma-separated labels\n    val createdAt: Long = System.currentTimeMillis(),''',
    '''    val parentIssueTitle: String? = null,\n    val labels: String = "governance",     // Comma-separated labels\n    @ColumnInfo(defaultValue = "0")\n    val sortOrder: Int = issueNumber,\n    val plannedStartAt: Long? = null,\n    val plannedEndAt: Long? = null,\n    @ColumnInfo(defaultValue = "1.0")\n    val wbsWeight: Double = 1.0,\n    @ColumnInfo(defaultValue = "0")\n    val progressPercent: Int = if (status == IssueStatus.CLOSED) 100 else 0,\n    val createdAt: Long = System.currentTimeMillis(),'''
)

write(
    "app/src/main/java/com/example/data/model/IssueHierarchyRules.kt",
    r'''
package com.example.data.model

import kotlin.math.max

/**
 * Pure recursive hierarchy rules for RepoIssue.
 * Kanban and WBS are projections of Issues, never a second Task entity.
 */
data class WbsProjectionRow(
    val issue: RepoIssue,
    val depth: Int,
    val code: String,
    val completedCount: Int,
    val totalCount: Int,
    val progress: Float
)

object IssueHierarchyRules {
    private fun ordered(issues: List<RepoIssue>): List<RepoIssue> =
        issues.sortedWith(compareBy<RepoIssue> { it.sortOrder }.thenBy { it.issueNumber }.thenBy { it.id })

    fun descendantIds(rootIssueId: String, issues: List<RepoIssue>): Set<String> {
        val childrenByParent = issues.groupBy { it.parentIssueId }
        val descendants = linkedSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.addLast(rootIssueId)
        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            childrenByParent[currentId].orEmpty().forEach { child ->
                if (child.id != rootIssueId && descendants.add(child.id)) queue.addLast(child.id)
            }
        }
        return descendants
    }

    fun canAssignParent(issueId: String, candidateParentId: String?, issues: List<RepoIssue>): Boolean {
        if (candidateParentId == null) return true
        if (candidateParentId == issueId) return false
        val issue = issues.firstOrNull { it.id == issueId } ?: return false
        val candidate = issues.firstOrNull { it.id == candidateParentId } ?: return false
        if (issue.repoId != candidate.repoId) return false
        return candidateParentId !in descendantIds(issueId, issues)
    }

    fun depthOf(issueId: String, issues: List<RepoIssue>): Int {
        val byId = issues.associateBy { it.id }
        val visited = mutableSetOf<String>()
        var depth = 0
        var current = byId[issueId]
        while (current?.parentIssueId != null) {
            val parentId = current.parentIssueId ?: break
            if (!visited.add(parentId)) break
            current = byId[parentId] ?: break
            depth += 1
        }
        return depth
    }

    fun orderedForDisplay(issues: List<RepoIssue>): List<Pair<RepoIssue, Int>> {
        if (issues.isEmpty()) return emptyList()
        val byId = issues.associateBy { it.id }
        val childrenByParent = issues.groupBy { it.parentIssueId }.mapValues { ordered(it.value) }
        val result = mutableListOf<Pair<RepoIssue, Int>>()
        val visited = mutableSetOf<String>()

        fun visit(issue: RepoIssue, depth: Int) {
            if (!visited.add(issue.id)) return
            result += issue to depth
            childrenByParent[issue.id].orEmpty().forEach { visit(it, depth + 1) }
        }

        ordered(issues.filter { it.parentIssueId == null || it.parentIssueId !in byId })
            .forEach { visit(it, 0) }
        ordered(issues).forEach { if (it.id !in visited) visit(it, depthOf(it.id, issues)) }
        return result
    }

    fun wbsProjection(issues: List<RepoIssue>): List<WbsProjectionRow> {
        if (issues.isEmpty()) return emptyList()
        val byId = issues.associateBy { it.id }
        val childrenByParent = issues.groupBy { it.parentIssueId }.mapValues { ordered(it.value) }
        val progressMemo = mutableMapOf<String, Float>()

        fun progressOf(issue: RepoIssue, visiting: MutableSet<String>): Float {
            progressMemo[issue.id]?.let { return it }
            if (!visiting.add(issue.id)) return leafProgress(issue)
            val children = childrenByParent[issue.id].orEmpty().filter { it.id !in visiting }
            val value = if (children.isEmpty()) {
                leafProgress(issue)
            } else {
                val totalWeight = children.sumOf { max(it.wbsWeight, 0.01) }
                if (totalWeight <= 0.0) 0f else {
                    (children.sumOf { child ->
                        progressOf(child, visiting) * max(child.wbsWeight, 0.01)
                    } / totalWeight).toFloat()
                }
            }
            visiting.remove(issue.id)
            return value.coerceIn(0f, 1f).also { progressMemo[issue.id] = it }
        }

        val rows = mutableListOf<WbsProjectionRow>()
        val visited = mutableSetOf<String>()

        fun visit(issue: RepoIssue, depth: Int, code: String) {
            if (!visited.add(issue.id)) return
            val subtreeIds = descendantIds(issue.id, issues) + issue.id
            val subtree = issues.filter { it.id in subtreeIds }
            rows += WbsProjectionRow(
                issue = issue,
                depth = depth,
                code = code,
                completedCount = subtree.count { it.status == IssueStatus.CLOSED },
                totalCount = subtree.size.coerceAtLeast(1),
                progress = progressOf(issue, mutableSetOf())
            )
            childrenByParent[issue.id].orEmpty().forEachIndexed { index, child ->
                visit(child, depth + 1, "$code.${index + 1}")
            }
        }

        var rootOrdinal = 1
        ordered(issues.filter { it.parentIssueId == null || it.parentIssueId !in byId }).forEach { root ->
            visit(root, 0, rootOrdinal.toString())
            rootOrdinal += 1
        }
        ordered(issues).forEach { issue ->
            if (issue.id !in visited) {
                visit(issue, 0, rootOrdinal.toString())
                rootOrdinal += 1
            }
        }
        return rows
    }

    fun overallProgress(issues: List<RepoIssue>): Float {
        if (issues.isEmpty()) return 0f
        val rows = wbsProjection(issues)
        val rootRows = rows.filter { it.depth == 0 }
        if (rootRows.isEmpty()) return 0f
        val totalWeight = rootRows.sumOf { max(it.issue.wbsWeight, 0.01) }
        return if (totalWeight <= 0.0) 0f else {
            (rootRows.sumOf { it.progress * max(it.issue.wbsWeight, 0.01) } / totalWeight)
                .toFloat()
                .coerceIn(0f, 1f)
        }
    }

    fun validatePlan(
        sortOrder: Int,
        plannedStartAt: Long?,
        plannedEndAt: Long?,
        wbsWeight: Double,
        progressPercent: Int
    ): String? = when {
        sortOrder < 0 -> "WBS 排序不得小於 0"
        plannedStartAt != null && plannedEndAt != null && plannedEndAt < plannedStartAt ->
            "計畫結束日不得早於開始日"
        !wbsWeight.isFinite() || wbsWeight <= 0.0 || wbsWeight > 1000.0 ->
            "WBS 權重必須介於 0 與 1000 之間"
        progressPercent !in 0..100 -> "進度必須介於 0% 與 100% 之間"
        else -> null
    }

    private fun leafProgress(issue: RepoIssue): Float = when (issue.status) {
        IssueStatus.CLOSED -> 1f
        else -> issue.progressPercent.coerceIn(0, 100) / 100f
    }
}
''',
)


# Compatibility with the current governance API on main.
repo_path = ROOT / "app/src/main/java/com/example/data/repository/GovernanceRepository.kt"
repo_text = repo_path.read_text(encoding="utf-8")
status_needle = "        val updated = issue.copy(\n            status = newStatus,\n"
if "status = newStatus,\n            progressPercent = when" not in repo_text:
    if status_needle not in repo_text:
        raise RuntimeError("Current updateIssueStatus shape was not recognized")
    repo_text = repo_text.replace(
        status_needle,
        "        val updated = issue.copy(\n"
        "            status = newStatus,\n"
        "            progressPercent = when {\n"
        "                newStatus == IssueStatus.CLOSED -> 100\n"
        "                issue.status == IssueStatus.CLOSED -> 0\n"
        "                else -> issue.progressPercent\n"
        "            },\n",
        1,
    )

if "suspend fun updateIssuePlan(" not in repo_text:
    marker = "    // --- REPO DISCUSSIONS METHODS ---"
    if marker not in repo_text:
        raise RuntimeError("Repository discussion section marker missing")
    method = r"""
    suspend fun updateIssuePlan(
        issueId: String,
        sortOrder: Int,
        plannedStartAt: Long?,
        plannedEndAt: Long?,
        wbsWeight: Double,
        progressPercent: Int,
        actor: User
    ): Pair<Boolean, String> {
        val issue = dao.getIssueByIdOnce(issueId) ?: return Pair(false, "Issue not found")
        val repo = dao.getRepositoryByIdOnce(issue.repoId) ?: return Pair(false, "Repository not found")
        val enterprise = dao.getEnterpriseOnce() ?: return Pair(false, "Enterprise not found")
        val evaluation = evaluateAction(actor, repo, null, GovernanceAction.ASSIGN_ISSUE)
        if (evaluation.verdict != PolicyVerdict.ALLOWED && issue.authorUserId != actor.id) {
            return Pair(false, evaluation.finalExplanation)
        }
        val validationError = IssueHierarchyRules.validatePlan(
            sortOrder = sortOrder,
            plannedStartAt = plannedStartAt,
            plannedEndAt = plannedEndAt,
            wbsWeight = wbsWeight,
            progressPercent = progressPercent
        )
        if (validationError != null) return Pair(false, validationError)

        val hasChildren = dao.getSubIssuesOnce(issue.id).isNotEmpty()
        val effectiveProgress = when {
            issue.status == IssueStatus.CLOSED -> 100
            hasChildren -> issue.progressPercent
            else -> progressPercent
        }
        val now = System.currentTimeMillis()
        dao.updateIssue(
            issue.copy(
                sortOrder = sortOrder,
                plannedStartAt = plannedStartAt,
                plannedEndAt = plannedEndAt,
                wbsWeight = wbsWeight,
                progressPercent = effectiveProgress,
                updatedAt = now
            )
        )
        dao.insertAuditLog(
            AuditLog(
                enterpriseId = enterprise.id,
                orgId = if (repo.ownerType == OwnerType.ORGANIZATION) repo.ownerId else null,
                repoId = repo.id,
                repoName = repo.name,
                actorUserId = actor.id,
                actorDisplayName = actor.displayName,
                actionName = "UPDATE_ISSUE_PLAN",
                verdict = PolicyVerdict.ALLOWED,
                reasoning = "Updated WBS plan for Issue #${issue.issueNumber}."
            )
        )
        return Pair(true, "Issue #${issue.issueNumber} WBS plan updated.")
    }

"""
    repo_text = repo_text.replace(marker, method + marker, 1)
repo_path.write_text(repo_text, encoding="utf-8")

vm_path = ROOT / "app/src/main/java/com/example/ui/viewmodel/GovernanceViewModel.kt"
vm_text = vm_path.read_text(encoding="utf-8")
if "fun updateIssuePlan(" not in vm_text:
    marker = "    // --- DISCUSSION VM ACTIONS ---"
    if marker not in vm_text:
        raise RuntimeError("ViewModel discussion section marker missing")
    method = r"""
    fun updateIssuePlan(
        issueId: String,
        sortOrder: Int,
        plannedStartAt: Long?,
        plannedEndAt: Long?,
        wbsWeight: Double,
        progressPercent: Int
    ) {
        val user = _activeUser.value ?: return
        viewModelScope.launch {
            val (success, msg) = repository.updateIssuePlan(
                issueId = issueId,
                sortOrder = sortOrder,
                plannedStartAt = plannedStartAt,
                plannedEndAt = plannedEndAt,
                wbsWeight = wbsWeight,
                progressPercent = progressPercent,
                actor = user
            )
            _uiMessages.emit(UiMessage(msg, isError = !success))
        }
    }

"""
    vm_text = vm_text.replace(marker, method + marker, 1)
vm_path.write_text(vm_text, encoding="utf-8")


write(
    "app/src/main/java/com/example/sync/RemoteSync.kt",
    r'''
package com.example.sync

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.CollaborationExperienceDao
import com.example.data.local.GovernanceDao
import com.example.data.model.NoCodeArtifact
import com.example.data.model.PushRegistration
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.SyncConflict
import com.example.data.model.SyncCursor
import com.example.data.model.SyncEntityType
import com.example.data.model.SyncMetadata
import com.example.data.model.SyncOperation
import com.example.data.model.SyncOutbox
import com.example.data.model.SyncState
import com.example.data.model.UserFollow
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.net.URI
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class SyncPushRequest(
    val eventId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val localVersion: Long,
    val payloadJson: String?
)

@JsonClass(generateAdapter = true)
data class SyncPushResponse(val serverVersion: Long, val cursor: String? = null)

@JsonClass(generateAdapter = true)
data class RemoteMutation(
    val entityType: String,
    val entityId: String,
    val operation: String,
    val serverVersion: Long,
    val payloadJson: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncPullResponse(val cursor: String?, val mutations: List<RemoteMutation>)

@JsonClass(generateAdapter = true)
data class PushRegistrationRequest(val token: String)

interface CollaborationSyncApi {
    @POST("v1/sync/push")
    suspend fun push(
        @Header("Authorization") authorization: String,
        @Body request: SyncPushRequest
    ): Response<SyncPushResponse>

    @GET("v1/sync/pull")
    suspend fun pull(
        @Header("Authorization") authorization: String,
        @Query("cursor") cursor: String?
    ): Response<SyncPullResponse>

    @POST("v1/push-registrations")
    suspend fun registerPushToken(
        @Header("Authorization") authorization: String,
        @Body request: PushRegistrationRequest
    ): Response<Unit>
}

interface AuthTokenProvider {
    suspend fun accessToken(): String?
}

class FirebaseAuthTokenProvider(private val context: Context) : AuthTokenProvider {
    override suspend fun accessToken(): String? {
        if (FirebaseApp.getApps(context).isEmpty()) return null
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return suspendCancellableCoroutine { continuation ->
            user.getIdToken(false)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result.token)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }
}

object SyncEndpointPolicy {
    fun normalizedHttpsBaseUrl(raw: String): String? {
        val value = raw.trim()
        if (value.isBlank() || value.contains(".invalid", ignoreCase = true)) return null
        return runCatching {
            val uri = URI(value)
            if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) return null
            if (value.endsWith('/')) value else "$value/"
        }.getOrNull()
    }
}

sealed interface SyncRunResult {
    data object Disabled : SyncRunResult
    data object AuthRequired : SyncRunResult
    data class Completed(val pushed: Int, val pulled: Int, val conflicts: Int) : SyncRunResult
    data class RetryableFailure(val reason: String) : SyncRunResult
}

class SyncPayloadAssembler(
    private val governanceDao: GovernanceDao,
    private val experienceDao: CollaborationExperienceDao,
    private val moshi: Moshi
) {
    suspend fun payload(item: SyncOutbox): String? = when (item.entityType) {
        SyncEntityType.REPOSITORY -> governanceDao.getRepositoryByIdOnce(item.entityId)
            ?.let { moshi.adapter(Repository::class.java).toJson(it) }
        SyncEntityType.ISSUE -> governanceDao.getIssueByIdOnce(item.entityId)
            ?.let { moshi.adapter(RepoIssue::class.java).toJson(it) }
        SyncEntityType.ARTIFACT -> governanceDao.getArtifactByIdOnce(item.entityId)
            ?.let { moshi.adapter(NoCodeArtifact::class.java).toJson(it) }
        SyncEntityType.DISCUSSION -> governanceDao.getDiscussionByIdOnce(item.entityId)
            ?.let { moshi.adapter(RepoDiscussion::class.java).toJson(it) }
        SyncEntityType.SAVED_TARGET -> experienceDao.getSavedTargetById(item.entityId)
            ?.let { moshi.adapter(SavedTarget::class.java).toJson(it) }
        SyncEntityType.USER_FOLLOW -> experienceDao.getUserFollowById(item.entityId)
            ?.let { moshi.adapter(UserFollow::class.java).toJson(it) }
        else -> null
    }
}

class SyncCoordinator(
    private val governanceDao: GovernanceDao,
    private val experienceDao: CollaborationExperienceDao,
    private val api: CollaborationSyncApi,
    private val tokenProvider: AuthTokenProvider,
    private val moshi: Moshi
) {
    suspend fun runOnce(): SyncRunResult {
        val token = tokenProvider.accessToken() ?: return SyncRunResult.AuthRequired
        val authorization = "Bearer $token"
        val assembler = SyncPayloadAssembler(governanceDao, experienceDao, moshi)
        var pushed = 0
        var pulled = 0
        var conflicts = 0

        return try {
            experienceDao.getPendingOutbox().forEach { item ->
                experienceDao.markOutboxState(item.id, SyncState.IN_FLIGHT)
                val payload = if (item.operation == SyncOperation.DELETE) null else assembler.payload(item)
                if (item.operation != SyncOperation.DELETE && payload == null) {
                    experienceDao.failOutbox(item.id, SyncState.FAILED, "Local entity is missing")
                    return@forEach
                }
                val response = api.push(
                    authorization,
                    SyncPushRequest(
                        eventId = item.id,
                        entityType = item.entityType,
                        entityId = item.entityId,
                        operation = item.operation,
                        localVersion = item.localVersion,
                        payloadJson = payload
                    )
                )
                when {
                    response.isSuccessful && response.body() != null -> {
                        val body = response.body()!!
                        experienceDao.markOutboxState(item.id, SyncState.SYNCED)
                        experienceDao.upsertSyncMetadata(
                            SyncMetadata(
                                id = "${item.entityType}:${item.entityId}",
                                entityType = item.entityType,
                                entityId = item.entityId,
                                localVersion = item.localVersion,
                                serverVersion = body.serverVersion,
                                status = SyncState.SYNCED,
                                updatedAt = System.currentTimeMillis(),
                                deletedAt = if (item.operation == SyncOperation.DELETE) System.currentTimeMillis() else null,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        )
                        body.cursor?.let {
                            experienceDao.upsertSyncCursor(SyncCursor(cursor = it))
                        }
                        pushed += 1
                    }
                    response.code() == 401 || response.code() == 403 -> {
                        experienceDao.failOutbox(item.id, SyncState.AUTH_REQUIRED, "Authentication required")
                        return SyncRunResult.AuthRequired
                    }
                    response.code() == 409 -> {
                        experienceDao.failOutbox(item.id, SyncState.CONFLICT, "Server version conflict")
                        recordConflict(item.entityType, item.entityId, item.localVersion, item.localVersion + 1, "Push conflict")
                        conflicts += 1
                    }
                    else -> {
                        experienceDao.failOutbox(item.id, SyncState.FAILED, "Push failed: HTTP ${response.code()}")
                        return SyncRunResult.RetryableFailure("Push failed: HTTP ${response.code()}")
                    }
                }
            }

            val cursor = experienceDao.getSyncCursor()?.cursor
            val pullResponse = api.pull(authorization, cursor)
            if (pullResponse.code() == 401 || pullResponse.code() == 403) return SyncRunResult.AuthRequired
            if (!pullResponse.isSuccessful || pullResponse.body() == null) {
                return SyncRunResult.RetryableFailure("Pull failed: HTTP ${pullResponse.code()}")
            }
            val body = pullResponse.body()!!
            experienceDao.setRemoteApplyState(true)
            try {
                body.mutations.forEach { mutation ->
                    if (experienceDao.countUnsynced(mutation.entityType, mutation.entityId) > 0) {
                        val localVersion = experienceDao
                            .getSyncMetadata(mutation.entityType, mutation.entityId)
                            ?.localVersion ?: 0L
                        recordConflict(
                            mutation.entityType,
                            mutation.entityId,
                            localVersion,
                            mutation.serverVersion,
                            "Remote change arrived while local change is pending"
                        )
                        conflicts += 1
                    } else if (applyRemoteMutation(mutation)) {
                        experienceDao.upsertSyncMetadata(
                            SyncMetadata(
                                id = "${mutation.entityType}:${mutation.entityId}",
                                entityType = mutation.entityType,
                                entityId = mutation.entityId,
                                localVersion = mutation.serverVersion,
                                serverVersion = mutation.serverVersion,
                                status = SyncState.SYNCED,
                                updatedAt = System.currentTimeMillis(),
                                deletedAt = if (mutation.operation == SyncOperation.DELETE) System.currentTimeMillis() else null,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        )
                        pulled += 1
                    } else {
                        recordConflict(
                            mutation.entityType,
                            mutation.entityId,
                            0L,
                            mutation.serverVersion,
                            "Remote payload failed validation or requested a protected hard delete"
                        )
                        conflicts += 1
                    }
                }
            } finally {
                experienceDao.setRemoteApplyState(false)
            }
            experienceDao.upsertSyncCursor(SyncCursor(cursor = body.cursor))
            experienceDao.getPushRegistration()?.let { registration ->
                api.registerPushToken(authorization, PushRegistrationRequest(registration.token))
            }
            experienceDao.deleteOldSyncedOutbox(System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L)
            SyncRunResult.Completed(pushed, pulled, conflicts)
        } catch (error: Exception) {
            experienceDao.setRemoteApplyState(false)
            SyncRunResult.RetryableFailure(error.message ?: error::class.java.simpleName)
        }
    }

    private suspend fun applyRemoteMutation(mutation: RemoteMutation): Boolean {
        if (mutation.operation == SyncOperation.DELETE) {
            return when (mutation.entityType) {
                SyncEntityType.SAVED_TARGET -> {
                    experienceDao.deleteSavedTargetById(mutation.entityId)
                    true
                }
                SyncEntityType.USER_FOLLOW -> {
                    experienceDao.deleteUserFollowById(mutation.entityId)
                    true
                }
                // Governance records are not hard-deleted from an unverified remote envelope.
                else -> false
            }
        }
        val payload = mutation.payloadJson ?: return false
        return runCatching {
            when (mutation.entityType) {
                SyncEntityType.REPOSITORY -> {
                    val entity = moshi.adapter(Repository::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId) return false
                    governanceDao.insertRepository(entity)
                }
                SyncEntityType.ISSUE -> {
                    val entity = moshi.adapter(RepoIssue::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || governanceDao.getRepositoryByIdOnce(entity.repoId) == null) return false
                    governanceDao.insertIssue(entity)
                }
                SyncEntityType.ARTIFACT -> {
                    val entity = moshi.adapter(NoCodeArtifact::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || governanceDao.getRepositoryByIdOnce(entity.repoId) == null) return false
                    governanceDao.insertArtifact(entity)
                }
                SyncEntityType.DISCUSSION -> {
                    val entity = moshi.adapter(RepoDiscussion::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || governanceDao.getRepositoryByIdOnce(entity.repoId) == null) return false
                    governanceDao.insertDiscussion(entity)
                }
                SyncEntityType.SAVED_TARGET -> {
                    val entity = moshi.adapter(SavedTarget::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId) return false
                    experienceDao.upsertSavedTarget(entity)
                }
                SyncEntityType.USER_FOLLOW -> {
                    val entity = moshi.adapter(UserFollow::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || entity.followerUserId == entity.followedUserId) return false
                    experienceDao.upsertUserFollow(entity)
                }
                else -> return false
            }
            true
        }.getOrDefault(false)
    }

    private suspend fun recordConflict(
        entityType: String,
        entityId: String,
        localVersion: Long,
        remoteVersion: Long,
        reason: String
    ) {
        experienceDao.upsertConflict(
            SyncConflict(
                entityType = entityType,
                entityId = entityId,
                localVersion = localVersion,
                remoteVersion = remoteVersion,
                reason = reason
            )
        )
    }
}

object SyncRuntime {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun createCoordinator(context: Context): SyncCoordinator? {
        val baseUrl = SyncEndpointPolicy.normalizedHttpsBaseUrl(BuildConfig.SYNC_BASE_URL) ?: return null
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CollaborationSyncApi::class.java)
        val database = AppDatabase.getInstance(context)
        return SyncCoordinator(
            governanceDao = database.governanceDao(),
            experienceDao = database.collaborationExperienceDao(),
            api = api,
            tokenProvider = FirebaseAuthTokenProvider(context.applicationContext),
            moshi = moshi
        )
    }
}
''',
)

write(
    "app/src/main/java/com/example/sync/SyncWorker.kt",
    r'''
package com.example.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class CollaborationSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val coordinator = SyncRuntime.createCoordinator(applicationContext) ?: return Result.success()
        return when (val result = coordinator.runOnce()) {
            SyncRunResult.Disabled -> Result.success()
            SyncRunResult.AuthRequired -> Result.success()
            is SyncRunResult.Completed -> Result.success()
            is SyncRunResult.RetryableFailure -> if (runAttemptCount < 5) Result.retry() else Result.failure()
        }
    }
}

object SyncScheduler {
    private const val PERIODIC_WORK = "collaboration_periodic_sync"
    private const val IMMEDIATE_WORK = "collaboration_immediate_sync"

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun ensurePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<CollaborationSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun requestNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<CollaborationSyncWorker>()
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
''',
)

write(
    "app/src/main/java/com/example/sync/CollaborationMessagingService.kt",
    r'''
package com.example.sync

import com.example.data.local.AppDatabase
import com.example.data.model.PushRegistration
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Push payloads are treated only as an untrusted sync hint. Domain mutations are
 * fetched through the authenticated sync API and validated before Room writes.
 */
class CollaborationMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            AppDatabase.getInstance(applicationContext)
                .collaborationExperienceDao()
                .upsertPushRegistration(PushRegistration(token = token))
            SyncScheduler.requestNow(applicationContext)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        SyncScheduler.requestNow(applicationContext)
    }
}
''',
)

write(
    "app/src/main/java/com/example/ui/viewmodel/CollaborationExperienceViewModel.kt",
    r'''
package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.SavedTarget
import com.example.data.model.SyncState
import com.example.data.model.SyncStatusSummary
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget
import com.example.navigation.storageKey
import com.example.navigation.toSavedTarget
import com.example.sync.SyncScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollaborationExperienceViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).collaborationExperienceDao()

    val savedTargets = dao.observeSavedTargets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val userFollows = dao.observeUserFollows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val outbox = dao.observeOutbox()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val conflicts = dao.observeConflicts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val lastSyncedAt = dao.observeLastSyncedAt()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val syncStatus = combine(outbox, conflicts, lastSyncedAt) { queue, conflictRows, lastSync ->
        SyncStatusSummary(
            pending = queue.count { it.state == SyncState.PENDING || it.state == SyncState.IN_FLIGHT },
            failed = queue.count { it.state == SyncState.FAILED },
            conflicts = conflictRows.count { it.resolvedAt == null },
            authRequired = queue.count { it.state == SyncState.AUTH_REQUIRED },
            lastSyncedAt = lastSync
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncStatusSummary())

    init {
        SyncScheduler.ensurePeriodic(application)
    }

    fun toggleSaved(userId: String, target: CollaborationTarget) {
        viewModelScope.launch {
            val key = target.storageKey()
            val existing = dao.getSavedTarget(userId, key)
            if (existing == null) dao.upsertSavedTarget(target.toSavedTarget(userId))
            else dao.deleteSavedTarget(userId, key)
        }
    }

    fun toggleFollow(followerUserId: String, followedUserId: String) {
        if (followerUserId == followedUserId) return
        viewModelScope.launch {
            val existing = dao.getUserFollow(followerUserId, followedUserId)
            if (existing == null) {
                dao.upsertUserFollow(
                    UserFollow(
                        followerUserId = followerUserId,
                        followedUserId = followedUserId
                    )
                )
            } else {
                dao.deleteUserFollow(followerUserId, followedUserId)
            }
        }
    }

    fun syncNow() {
        SyncScheduler.requestNow(getApplication())
    }
}
''',
)

# Dependencies: WorkManager, Firebase Auth and Firebase Messaging.
replace_once(
    "gradle/libs.versions.toml",
    'credentials = "1.5.0"\ngoogleid = "1.1.1"',
    'credentials = "1.5.0"\ngoogleid = "1.1.1"\nworkRuntime = "2.10.1"'
)
replace_once(
    "gradle/libs.versions.toml",
    'androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }',
    'androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }\nandroidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workRuntime" }'
)
replace_once(
    "gradle/libs.versions.toml",
    'firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }',
    'firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }\nfirebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }'
)
replace_once(
    "app/build.gradle.kts",
    '  implementation(libs.androidx.lifecycle.viewmodel.compose)\n  // implementation(libs.androidx.navigation.compose)',
    '  implementation(libs.androidx.lifecycle.viewmodel.compose)\n  implementation(libs.androidx.work.runtime.ktx)\n  // implementation(libs.androidx.navigation.compose)'
)
replace_once(
    "app/build.gradle.kts",
    '  implementation(libs.firebase.ai)\n  // Uncomment to use Firestore:',
    '  implementation(libs.firebase.ai)\n  implementation(libs.firebase.messaging)\n  // Uncomment to use Firestore:'
)
replace_once(
    "app/build.gradle.kts",
    '  // implementation(libs.firebase.auth)\n  // implementation(libs.androidx.credentials)',
    '  implementation(libs.firebase.auth)\n  // implementation(libs.androidx.credentials)'
)


env_path = ROOT / ".env.example"
env_text = env_path.read_text(encoding="utf-8")
if "SYNC_BASE_URL=" not in env_text:
    if env_text and not env_text.endswith("\n"):
        env_text += "\n"
    env_text += (
        "\n# Authenticated collaboration sync endpoint. "
        "The invalid default keeps sync disabled.\n"
        "SYNC_BASE_URL=https://sync.invalid/\n"
    )
    env_path.write_text(env_text, encoding="utf-8")


replace_once(
    "app/src/main/AndroidManifest.xml",
    '<manifest xmlns:android="http://schemas.android.com/apk/res/android">',
    '<manifest xmlns:android="http://schemas.android.com/apk/res/android">\n\n    <uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />'
)
replace_once(
    "app/src/main/AndroidManifest.xml",
    '''        <activity\n            android:name=".MainActivity"''',
    '''        <service\n            android:name=".sync.CollaborationMessagingService"\n            android:exported="false">\n            <intent-filter>\n                <action android:name="com.google.firebase.MESSAGING_EVENT" />\n            </intent-filter>\n        </service>\n\n        <activity\n            android:name=".MainActivity"'''
)

print("mobile collaboration v2 data, migration, sync and WBS model changes applied")
