from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def save(rel: str, text: str) -> None:
    path = ROOT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def write(rel: str, content: str) -> None:
    save(rel, content.strip() + "\n")


def insert_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text
    imports = list(re.finditer(r"^import .+$", text, flags=re.MULTILINE))
    if not imports:
        raise RuntimeError(f"No import block found for {import_line}")
    at = imports[-1].end()
    return text[:at] + "\n" + import_line + text[at:]


# Integration script adds the second Activity ViewModel first; make the app
# function signature independently idempotent so property text cannot mask it.
main_path = "app/src/main/java/com/example/MainActivity.kt"
main = read(main_path)
main = re.sub(
    r"private fun GovernanceApp\(\s*viewModel:\s*GovernanceViewModel\s*\)\s*\{",
    "private fun GovernanceApp(\n    viewModel: GovernanceViewModel,\n    experienceViewModel: com.example.ui.viewmodel.CollaborationExperienceViewModel\n) {",
    main,
    count=1
)
if "private fun GovernanceApp(\n    viewModel: GovernanceViewModel,\n    experienceViewModel:" not in main:
    raise RuntimeError("GovernanceApp two-ViewModel signature was not established")
save(main_path, main)

repo_path = "app/src/main/java/com/example/ui/screens/RepoDetailScreen.kt"
repo = read(repo_path)
repo = insert_import(repo, "import androidx.compose.material.icons.filled.List")
# Cover formatting variants of the content-padding when expression.
repo = re.sub(
    r"RepoDetailTab\.DISCUSSIONS\s*,\s*RepoDetailTab\.ARTIFACTS\s*->\s*0\.dp",
    "RepoDetailTab.WBS, RepoDetailTab.DISCUSSIONS, RepoDetailTab.ARTIFACTS -> 0.dp",
    repo
)
save(repo_path, repo)

# Endpoint credentials in URL authority are rejected; bearer credentials must
# only come from Firebase Auth and the Authorization header.
sync_path = "app/src/main/java/com/example/sync/RemoteSync.kt"
sync = read(sync_path)
sync = sync.replace(
    'if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) return null',
    'if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank() || uri.userInfo != null) return null'
)
save(sync_path, sync)

write(
    "app/src/test/java/com/example/MobileCollaborationV2RulesTest.kt",
    r'''
package com.example

import com.example.data.local.AppMigrations
import com.example.data.model.CollaborationTargetType
import com.example.data.model.SavedTarget
import com.example.navigation.CollaborationTarget
import com.example.navigation.CollaborationTargetResolver
import com.example.navigation.storageKey
import com.example.navigation.toCollaborationTarget
import com.example.sync.SyncEndpointPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileCollaborationV2RulesTest {
    @Test
    fun notificationResolverPrefersOneMostSpecificTarget() {
        val resolution = CollaborationTargetResolver.resolveNotification(
            repositoryId = "repo-1",
            artifactId = null,
            issueId = "issue-7",
            discussionId = null
        )

        assertTrue(resolution.isSuccess)
        assertEquals(
            CollaborationTarget.Issue("repo-1", "issue-7"),
            resolution.target
        )
    }

    @Test
    fun notificationResolverFailsClosedForConflictingOrUnscopedTargets() {
        val conflicting = CollaborationTargetResolver.resolveNotification(
            repositoryId = "repo-1",
            artifactId = "artifact-1",
            issueId = "issue-1",
            discussionId = null
        )
        val unscoped = CollaborationTargetResolver.resolveNotification(
            repositoryId = null,
            artifactId = null,
            issueId = "issue-1",
            discussionId = null
        )

        assertFalse(conflicting.isSuccess)
        assertFalse(unscoped.isSuccess)
        assertTrue(conflicting.failureMessage?.isNotBlank() == true)
        assertTrue(unscoped.failureMessage?.isNotBlank() == true)
    }

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
    fun syncEndpointRequiresHttpsHostAndNoEmbeddedCredentials() {
        assertEquals(
            "https://sync.example.com/",
            SyncEndpointPolicy.normalizedHttpsBaseUrl("https://sync.example.com")
        )
        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("http://sync.example.com"))
        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("https://user:secret@sync.example.com"))
        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("https://sync.invalid/"))
    }

    @Test
    fun explicitMigrationContainsWbsAndSyncTables() {
        val statements = AppMigrations.MIGRATION_4_5_STATEMENTS.joinToString("\n")

        assertEquals(5, AppMigrations.CURRENT_VERSION)
        assertTrue(statements.contains("ALTER TABLE repo_issues ADD COLUMN sortOrder"))
        assertTrue(statements.contains("ALTER TABLE repo_issues ADD COLUMN progressPercent"))
        assertTrue(statements.contains("CREATE TABLE IF NOT EXISTS saved_targets"))
        assertTrue(statements.contains("CREATE TABLE IF NOT EXISTS sync_outbox"))
        assertTrue(statements.contains("CREATE TABLE IF NOT EXISTS sync_conflicts"))
    }
}
''',
)

write(
    "docs/decisions/mobile-collaboration-v2.md",
    r'''
# Mobile Collaboration v2 — First-Principles Decision Record

## Decision status

Accepted for the `feature/mobile-collaboration-v2` branch.

## Problem reduced to first principles

A field collaboration product must let an authorized person find the right work, understand its dependencies, update one authoritative record, leave evidence, notify affected people, and roll the result upward without creating contradictory copies.

The smallest stable product vocabulary is:

- workspace scope: Enterprise, Organization, Team, User;
- Repository: a no-code collaboration container;
- RepoIssue: the only persisted work record;
- NoCodeArtifact and RepoDiscussion: evidence and deliberation;
- CollaborationTarget: the only navigation/search/favorite destination type;
- verified AuditLog/domain events: the source for public activity and achievements;
- Room plus an outbox: the local-first consistency boundary.

## Occam and 80/20 decisions

1. **No dashboard entities.** Home summaries are projections of existing scoped entities.
2. **No `WbsTask`.** WBS and Kanban are views of the recursive `RepoIssue` tree.
3. **One generic favorite relation.** `SavedTarget` stores any canonical collaboration target.
4. **One follow graph.** `UserFollow` is sufficient; follower counts and feeds are projections.
5. **No mutable XP counters.** XP, levels, achievements, trending, and public activity derive from allow-listed verified events.
6. **Room remains the UI source of truth.** Network work records IDs in an outbox and reconciles through authenticated incremental synchronization.
7. **Push is only a hint.** FCM payloads never mutate domain data; they request authenticated pull synchronization.
8. **Explicit migration before schema growth.** Version 4 moves to version 5 without destructive fallback.

## Data and permission boundaries

Repository ownership remains User or Organization. Teams only receive access through existing rules. Explore filters Repository-backed targets through the hierarchical access model before rendering. Inbox and favorites resolve through `CollaborationTarget`; invalid, conflicting, missing, or unauthorized targets fail visibly rather than falling back to a broader object.

Social activity excludes membership, role, access-rule, policy, denial, and other private governance events. A viewer receives only activity from Repository data already present in the viewer's scoped projection.

## WBS semantics

- sibling ordering: `sortOrder`, then issue number;
- optional plan dates: `plannedStartAt`, `plannedEndAt`;
- positive weighting: `wbsWeight`;
- leaf progress: `progressPercent`;
- closed leaf progress: 100%;
- parent progress: weighted roll-up of direct children at read time;
- cycles: rejected by existing hierarchy rules and guarded during projection.

## Synchronization boundary

The Android client implements the local half of multi-user collaboration:

- Firebase-authenticated bearer token;
- HTTPS-only configured endpoint;
- coalesced Room outbox;
- idempotency event ID;
- stable local/server versions;
- incremental cursor;
- retry and explicit conflict records;
- remote-apply guard to prevent echo;
- FCM token registration and push-triggered pull.

A production deployment still requires a server conforming to `docs/contracts/collaboration-sync-v1.md`. Until a valid endpoint and authenticated Firebase user exist, synchronization is intentionally disabled rather than silently using an insecure fallback.

## Verification

The branch must pass JVM unit tests and `assembleDebug`. Security review covers schema migration, authorization boundaries, target resolution, event privacy, network endpoint policy, untrusted push handling, remote payload validation, and workflow self-removal.
''',
)

write(
    "docs/contracts/collaboration-sync-v1.md",
    r'''
# Collaboration Sync API v1

## Trust model

The client authenticates every request with a Firebase ID token in `Authorization: Bearer <token>`. The server must validate issuer, audience, expiry, revocation policy, and enterprise/user membership before reading or writing any entity. Transport must use HTTPS. The Android client rejects endpoints containing embedded URL credentials and never accepts domain mutations directly from push payloads.

## Push local mutation

`POST /v1/sync/push`

Request fields:

- `eventId`: globally unique outbox ID; server must use it as an idempotency key;
- `entityType`: allow-listed entity type;
- `entityId`: stable entity ID;
- `operation`: `UPSERT` or `DELETE`;
- `localVersion`: monotonic local change timestamp/version;
- `payloadJson`: full entity for `UPSERT`, absent for `DELETE`.

Response fields:

- `serverVersion`: authoritative monotonic version;
- `cursor`: optional stream cursor advanced by the accepted mutation.

The server must re-evaluate ownership, membership, Repository access, action policy, foreign-key scope, entity ID equality, payload size, and allowed state transitions. A client payload is never authorization evidence.

Use `409 Conflict` when the supplied version cannot be safely applied. Use `401` or `403` for authentication/authorization failure. Duplicate `eventId` requests must return the original successful result without applying a second mutation.

## Pull incremental mutations

`GET /v1/sync/pull?cursor=<opaque>`

Response fields:

- `cursor`: next opaque cursor;
- `mutations`: ordered authorized mutations with `entityType`, `entityId`, `operation`, `serverVersion`, and optional `payloadJson`.

The server must filter every mutation to the authenticated user's current visibility. Revoked access must not leak historical private payloads. Cursors are opaque, user/enterprise scoped, and non-authoritative for permission checks.

## Push registration

`POST /v1/push-registrations`

Body: `{ "token": "<FCM registration token>" }`.

The server binds tokens to the authenticated user and device, supports token rotation/revocation, and sends only minimal sync hints. Notification text and domain payload are fetched through the authenticated sync API.

## Conflict policy

The client does not auto-overwrite a pending local mutation with a remote mutation. It creates a `SyncConflict` record and retains both version identifiers. Product-specific resolution must be explicit. Governance records are not remotely hard-deleted by the generic client envelope; a server should model auditable lifecycle transitions instead.

## Limits

Recommended server limits:

- maximum 50 mutations per pull page;
- maximum 256 KiB serialized payload per mutation;
- bounded retry and rate limits by user, device, and enterprise;
- audit record for accepted and denied mutations;
- no secrets or access tokens in logs.
''',
)

print("mobile collaboration v2 verification fixes, tests and decision records applied")
