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
