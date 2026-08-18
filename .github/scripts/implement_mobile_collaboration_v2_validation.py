from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def write(rel: str, content: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8")


write(
    "app/src/test/java/com/example/MobileCollaborationV2RulesTest.kt",
    r'''
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
        assertEquals(5, AppMigrations.CURRENT_VERSION)
        assertTrue(sql.contains("ALTER TABLE repo_issues ADD COLUMN sortOrder"))
        assertTrue(sql.contains("ALTER TABLE repo_issues ADD COLUMN progressPercent"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS saved_targets"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS sync_outbox"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS sync_conflicts"))
    }
}
'''
)

write(
    "docs/decisions/mobile-collaboration-v2.md",
    r'''
# Mobile Collaboration v2 — First-Principles Decision

## Invariants

- Repository remains a no-code collaboration container owned only by User or Organization.
- Team access remains an access rule, never ownership.
- RepoIssue remains the only persisted work truth; WBS and Kanban are projections.
- Scope-aware Home uses existing scoped records; no dashboard persistence is added.
- SavedTarget is the single generic favorite relation.
- UserFollow is the only follow graph; XP and public activity are projections from allow-listed audit events.
- Room remains the local UI source of truth.
- Remote sync uses an outbox, explicit versions/cursors/conflicts, Firebase-authenticated HTTPS requests, and FCM only as an untrusted sync hint.
- Schema 4 to 5 uses an explicit migration and must preserve existing issue data.

## 80/20 scope

The highest-leverage field loop is: select scope → find work → update one Issue → roll up WBS → notify/sync → retrieve through Inbox or Explore. Existing cross-repository My Work from PR #20 is reused rather than replaced.
'''
)

write(
    "docs/contracts/collaboration-sync-v1.md",
    r'''
# Collaboration Sync API v1

Every request uses `Authorization: Bearer <Firebase ID token>` over HTTPS. The server must verify token authenticity and re-evaluate enterprise membership, Repository visibility, ownership, role, and action policy; client payloads are never authorization evidence.

`POST /v1/sync/push` accepts idempotent outbox events with entity type/id, operation, local version, and bounded payload. `409` represents version conflict. `401/403` represents authentication or authorization failure.

`GET /v1/sync/pull?cursor=<opaque>` returns only mutations currently visible to the authenticated user. The client does not overwrite an entity that has a pending local change; it records a conflict instead. Generic remote envelopes cannot hard-delete governance records.

`POST /v1/push-registrations` binds an FCM token to the authenticated user/device. Push content is only a sync hint; domain state is fetched through authenticated pull.

Tokens and secrets must not be written to logs. Endpoint configuration must be HTTPS and must not contain embedded credentials.
'''
)

print("current-main compatible mobile v2 validation artifacts written")
