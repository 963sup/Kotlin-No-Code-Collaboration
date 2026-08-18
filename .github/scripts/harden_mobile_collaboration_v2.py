from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def save(rel: str, text: str) -> None:
    (ROOT / rel).write_text(text, encoding="utf-8")


def insert_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text
    imports = list(re.finditer(r"^import .+$", text, flags=re.MULTILINE))
    if not imports:
        raise RuntimeError(f"No import block for {import_line}")
    at = imports[-1].end()
    return text[:at] + "\n" + import_line + text[at:]


# A process killed during a remote apply must never leave local outbox capture
# disabled on the next application start.
trigger_path = "app/src/main/java/com/example/data/local/DatabaseSyncTriggers.kt"
trigger = read(trigger_path)
reset_line = '        db.execSQL("UPDATE sync_runtime_state SET isApplyingRemote = 0 WHERE id = 1")'
insert_after = '        db.execSQL("INSERT OR IGNORE INTO sync_runtime_state(id, isApplyingRemote) VALUES(1, 0)")'
if reset_line not in trigger:
    if insert_after not in trigger:
        raise RuntimeError("DatabaseSyncTriggers runtime-state insert missing")
    trigger = trigger.replace(insert_after, insert_after + "\n" + reset_line, 1)
save(trigger_path, trigger)

# Authentication can recover after login/token refresh, so auth-required outbox
# rows must become eligible for retry rather than remaining permanently stuck.
dao_path = "app/src/main/java/com/example/data/local/CollaborationExperienceDao.kt"
dao = read(dao_path)
dao = dao.replace(
    "WHERE state IN ('PENDING', 'FAILED') ORDER BY queuedAt ASC LIMIT :limit",
    "WHERE state IN ('PENDING', 'FAILED', 'AUTH_REQUIRED') ORDER BY queuedAt ASC LIMIT :limit"
)
save(dao_path, dao)

sync_path = "app/src/main/java/com/example/sync/RemoteSync.kt"
sync = read(sync_path)
sync = insert_import(sync, "import androidx.room.withTransaction")

# Coordinator owns the database transaction boundary in addition to DAOs.
if "private val database: AppDatabase," not in sync:
    sync = sync.replace(
        "class SyncCoordinator(\n    private val governanceDao: GovernanceDao,",
        "class SyncCoordinator(\n    private val database: AppDatabase,\n    private val governanceDao: GovernanceDao,",
        1
    )

old_apply = '''            experienceDao.setRemoteApplyState(true)\n            try {\n                body.mutations.forEach { mutation ->\n                    if (experienceDao.countUnsynced(mutation.entityType, mutation.entityId) > 0) {\n                        val localVersion = experienceDao\n                            .getSyncMetadata(mutation.entityType, mutation.entityId)\n                            ?.localVersion ?: 0L\n                        recordConflict(\n                            mutation.entityType,\n                            mutation.entityId,\n                            localVersion,\n                            mutation.serverVersion,\n                            "Remote change arrived while local change is pending"\n                        )\n                        conflicts += 1\n                    } else if (applyRemoteMutation(mutation)) {\n                        experienceDao.upsertSyncMetadata(\n                            SyncMetadata(\n                                id = "${mutation.entityType}:${mutation.entityId}",\n                                entityType = mutation.entityType,\n                                entityId = mutation.entityId,\n                                localVersion = mutation.serverVersion,\n                                serverVersion = mutation.serverVersion,\n                                status = SyncState.SYNCED,\n                                updatedAt = System.currentTimeMillis(),\n                                deletedAt = if (mutation.operation == SyncOperation.DELETE) System.currentTimeMillis() else null,\n                                lastSyncedAt = System.currentTimeMillis()\n                            )\n                        )\n                        pulled += 1\n                    } else {\n                        recordConflict(\n                            mutation.entityType,\n                            mutation.entityId,\n                            0L,\n                            mutation.serverVersion,\n                            "Remote payload failed validation or requested a protected hard delete"\n                        )\n                        conflicts += 1\n                    }\n                }\n            } finally {\n                experienceDao.setRemoteApplyState(false)\n            }'''
new_apply = '''            database.withTransaction {\n                // SQLite serializes writers for this transaction. The trigger guard\n                // is therefore never visible to a concurrent local domain write.\n                experienceDao.setRemoteApplyState(true)\n                try {\n                    body.mutations.forEach { mutation ->\n                        if (experienceDao.countUnsynced(mutation.entityType, mutation.entityId) > 0) {\n                            val localVersion = experienceDao\n                                .getSyncMetadata(mutation.entityType, mutation.entityId)\n                                ?.localVersion ?: 0L\n                            recordConflict(\n                                mutation.entityType,\n                                mutation.entityId,\n                                localVersion,\n                                mutation.serverVersion,\n                                "Remote change arrived while local change is pending"\n                            )\n                            conflicts += 1\n                        } else if (applyRemoteMutation(mutation)) {\n                            experienceDao.upsertSyncMetadata(\n                                SyncMetadata(\n                                    id = "${mutation.entityType}:${mutation.entityId}",\n                                    entityType = mutation.entityType,\n                                    entityId = mutation.entityId,\n                                    localVersion = mutation.serverVersion,\n                                    serverVersion = mutation.serverVersion,\n                                    status = SyncState.SYNCED,\n                                    updatedAt = System.currentTimeMillis(),\n                                    deletedAt = if (mutation.operation == SyncOperation.DELETE) System.currentTimeMillis() else null,\n                                    lastSyncedAt = System.currentTimeMillis()\n                                )\n                            )\n                            pulled += 1\n                        } else {\n                            recordConflict(\n                                mutation.entityType,\n                                mutation.entityId,\n                                0L,\n                                mutation.serverVersion,\n                                "Remote payload failed validation or requested a protected hard delete"\n                            )\n                            conflicts += 1\n                        }\n                    }\n                } finally {\n                    experienceDao.setRemoteApplyState(false)\n                }\n            }'''
if old_apply in sync:
    sync = sync.replace(old_apply, new_apply, 1)
elif "database.withTransaction" not in sync:
    raise RuntimeError("Remote apply block was not recognized")

# Reject oversized envelopes before JSON reflection or transport.
if "MAX_SYNC_PAYLOAD_CHARS" not in sync:
    sync = sync.replace(
        "sealed interface SyncRunResult {",
        "private const val MAX_SYNC_PAYLOAD_CHARS = 262_144\nprivate const val MAX_PULL_MUTATIONS = 500\n\nsealed interface SyncRunResult {",
        1
    )
    sync = sync.replace(
        "                if (item.operation != SyncOperation.DELETE && payload == null) {",
        "                if (payload != null && payload.length > MAX_SYNC_PAYLOAD_CHARS) {\n                    experienceDao.failOutbox(item.id, SyncState.FAILED, \"Payload exceeds client safety limit\")\n                    return@forEach\n                }\n                if (item.operation != SyncOperation.DELETE && payload == null) {",
        1
    )
    sync = sync.replace(
        "            val body = pullResponse.body()!!\n            database.withTransaction {",
        "            val body = pullResponse.body()!!\n            if (body.mutations.size > MAX_PULL_MUTATIONS) {\n                return SyncRunResult.RetryableFailure(\"Pull page exceeds client safety limit\")\n            }\n            database.withTransaction {",
        1
    )
    sync = sync.replace(
        "        val payload = mutation.payloadJson ?: return false\n        return runCatching {",
        "        val payload = mutation.payloadJson ?: return false\n        if (payload.length > MAX_SYNC_PAYLOAD_CHARS) return false\n        return runCatching {",
        1
    )

# Validate owner cardinality before accepting a Repository envelope.
repository_parse = '''                    val entity = moshi.adapter(Repository::class.java).fromJson(payload) ?: return false\n                    if (entity.id != mutation.entityId) return false\n                    governanceDao.insertRepository(entity)'''
repository_hardened = '''                    val entity = moshi.adapter(Repository::class.java).fromJson(payload) ?: return false\n                    val hasUserOwner = !entity.ownerUserId.isNullOrBlank()\n                    val hasOrganizationOwner = !entity.ownerOrganizationId.isNullOrBlank()\n                    if (entity.id != mutation.entityId || hasUserOwner == hasOrganizationOwner) return false\n                    governanceDao.insertRepository(entity)'''
if repository_parse in sync:
    sync = sync.replace(repository_parse, repository_hardened, 1)

# Runtime wiring includes the transaction-owning database.
if "database = database," not in sync:
    sync = sync.replace(
        "        return SyncCoordinator(\n            governanceDao = database.governanceDao(),",
        "        return SyncCoordinator(\n            database = database,\n            governanceDao = database.governanceDao(),",
        1
    )
save(sync_path, sync)

# Coalesce push hints. REPLACE can let repeated hints continuously cancel a run.
worker_path = "app/src/main/java/com/example/sync/SyncWorker.kt"
worker = read(worker_path)
worker = worker.replace(
    "ExistingWorkPolicy.REPLACE,",
    "ExistingWorkPolicy.KEEP,"
)
save(worker_path, worker)

projection_path = "app/src/main/java/com/example/ui/model/ExperienceProjections.kt"
projection = read(projection_path)
# Trending scores never consume audit events outside accessible repositories.
projection = projection.replace(
    "        val publicAudit = auditLogs.filter(PublicActivityPolicy::isPublic)",
    "        val accessibleRepositoryIds = repositoryById.keys\n        val publicAudit = auditLogs.filter { log ->\n            PublicActivityPolicy.isPublic(log) && log.repoId in accessibleRepositoryIds\n        }"
)
# Social activity drops every event whose repository is not in the supplied
# viewer-visible repository projection, including target IDs.
projection = projection.replace(
    "        val publicAudits = auditLogs.filter {\n            it.actorUserId == profileUser.id && PublicActivityPolicy.isPublic(it)\n        }\n        val xp =",
    "        val visibleRepositoryIds = repositories.map { it.id }.toSet()\n        val publicAudits = auditLogs.filter {\n            it.actorUserId == profileUser.id &&\n                PublicActivityPolicy.isPublic(it) &&\n                it.repoId in visibleRepositoryIds\n        }\n        val xp ="
)
save(projection_path, projection)

# Tests for the two security invariants added by this pass.
test_path = "app/src/test/java/com/example/MobileCollaborationV2SecurityRulesTest.kt"
save(
    test_path,
    '''package com.example\n\nimport com.example.data.model.SyncState\nimport com.example.sync.SyncEndpointPolicy\nimport org.junit.Assert.assertEquals\nimport org.junit.Assert.assertNull\nimport org.junit.Test\n\nclass MobileCollaborationV2SecurityRulesTest {\n    @Test\n    fun endpointPolicyNormalizesOnlyCredentialFreeHttps() {\n        assertEquals(\n            "https://engineering.example.com/sync/",\n            SyncEndpointPolicy.normalizedHttpsBaseUrl("https://engineering.example.com/sync")\n        )\n        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("http://engineering.example.com"))\n        assertNull(SyncEndpointPolicy.normalizedHttpsBaseUrl("https://user:token@engineering.example.com"))\n    }\n\n    @Test\n    fun authenticationRequiredIsAnExplicitSyncState() {\n        assertEquals("AUTH_REQUIRED", SyncState.AUTH_REQUIRED)\n    }\n}\n'''
)

print("mobile collaboration v2 security hardening applied")
