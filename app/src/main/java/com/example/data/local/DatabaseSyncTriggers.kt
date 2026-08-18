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
        "user_follows" to "user_follow",
    )

    val callback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            install(db)
        }
    }

    fun install(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO sync_runtime_state(id, isApplyingRemote) VALUES(1, 0)")
        db.execSQL("UPDATE sync_runtime_state SET isApplyingRemote = 0 WHERE id = 1")
        db.execSQL(
            "UPDATE sync_outbox SET state = 'PENDING', lastError = 'Recovered interrupted sync' WHERE state = 'IN_FLIGHT'",
        )
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
                  AND state IN ('PENDING', 'FAILED', 'AUTH_REQUIRED');
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
              AND state IN ('PENDING', 'FAILED', 'AUTH_REQUIRED');
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
