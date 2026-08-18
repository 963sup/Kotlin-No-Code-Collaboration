package com.example

import com.example.data.local.AppMigrations
import com.example.data.model.CollaborationTargetType
import com.example.data.model.SavedTarget
import com.example.navigation.CollaborationTarget
import com.example.navigation.storageKey
import com.example.navigation.toCollaborationTarget
import com.example.sync.SyncEndpointPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileCollaborationV2RulesTest {
    @Test
    fun savedTargetRoundTripsThroughCanonicalTarget() {
        val saved = SavedTarget(
            id = "saved-1",
            userId = "user-1",
            targetKey = "ISSUE:repo-1:issue-1",
            targetType = CollaborationTargetType.ISSUE,
            targetId = "issue-1",
            repositoryId = "repo-1"
        )
        val target = saved.toCollaborationTarget()
        assertEquals(CollaborationTarget.Issue("repo-1", "issue-1"), target)
        assertEquals("ISSUE:repo-1:issue-1", target?.storageKey())
    }

    @Test
    fun syncEndpointRequiresHttpsAndNoEmbeddedCredentials() {
        assertEquals("https://sync.example.com/", SyncEndpointPolicy.normalizedHttpsBaseUrl("https://sync.example.com"))
        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("http://sync.example.com"))
        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("https://user:secret@sync.example.com"))
        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("https://sync.invalid/"))
    }

    @Test
    fun explicitMigrationContainsWbsAndSyncTables() {
        val sql = AppMigrations.MIGRATION_4_5_STATEMENTS.joinToString("\n")
        assertEquals(6, AppMigrations.CURRENT_VERSION)
        assertTrue(sql.contains("ALTER TABLE repo_issues ADD COLUMN sortOrder"))
        assertTrue(sql.contains("ALTER TABLE repo_issues ADD COLUMN progressPercent"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS saved_targets"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS sync_outbox"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS sync_conflicts"))
    }
}
