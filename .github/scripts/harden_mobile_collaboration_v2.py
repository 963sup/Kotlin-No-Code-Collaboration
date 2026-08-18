from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(rel: str) -> str:
    return (ROOT / rel).read_text(encoding="utf-8")


def save(rel: str, text: str) -> None:
    (ROOT / rel).write_text(text, encoding="utf-8")


def add_import(text: str, line: str) -> str:
    if line in text:
        return text
    imports = [i for i in range(len(text)) if text.startswith("import ", i)]
    if not imports:
        raise RuntimeError(f"No imports for {line}")
    last = text.rfind("\nimport ")
    end = text.find("\n", last + 1)
    return text[:end] + "\n" + line + text[end:]


# Auth-required work must become retryable after login/token refresh.
dao_path = "app/src/main/java/com/example/data/local/CollaborationExperienceDao.kt"
dao = read(dao_path)
dao = dao.replace(
    "WHERE state IN ('PENDING', 'FAILED') ORDER BY queuedAt ASC LIMIT :limit",
    "WHERE state IN ('PENDING', 'FAILED', 'AUTH_REQUIRED') ORDER BY queuedAt ASC LIMIT :limit"
)
save(dao_path, dao)

# A crashed remote apply must not leave local outbox capture disabled.
trigger_path = "app/src/main/java/com/example/data/local/DatabaseSyncTriggers.kt"
trigger = read(trigger_path)
insert = '        db.execSQL("INSERT OR IGNORE INTO sync_runtime_state(id, isApplyingRemote) VALUES(1, 0)")'
reset = '        db.execSQL("UPDATE sync_runtime_state SET isApplyingRemote = 0 WHERE id = 1")'
if reset not in trigger and insert in trigger:
    trigger = trigger.replace(insert, insert + "\n" + reset, 1)
save(trigger_path, trigger)

sync_path = "app/src/main/java/com/example/sync/RemoteSync.kt"
sync = read(sync_path)
sync = add_import(sync, "import androidx.room.withTransaction")

# Reject URL credentials; bearer auth comes only from Firebase Auth.
sync = sync.replace(
    'if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) return null',
    'if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank() || uri.userInfo != null) return null'
)

# Bound reflection/deserialization and transport work.
if "MAX_SYNC_PAYLOAD_CHARS" not in sync:
    sync = sync.replace(
        "sealed interface SyncRunResult {",
        "private const val MAX_SYNC_PAYLOAD_CHARS = 262_144\nprivate const val MAX_PULL_MUTATIONS = 500\n\nsealed interface SyncRunResult {",
        1,
    )
    sync = sync.replace(
        "                if (item.operation != SyncOperation.DELETE && payload == null) {",
        "                if (payload != null && payload.length > MAX_SYNC_PAYLOAD_CHARS) {\n"
        "                    experienceDao.failOutbox(item.id, SyncState.FAILED, \"Payload exceeds client safety limit\")\n"
        "                    return@forEach\n"
        "                }\n"
        "                if (item.operation != SyncOperation.DELETE && payload == null) {",
        1,
    )
    sync = sync.replace(
        "            val body = pullResponse.body()!!\n            experienceDao.setRemoteApplyState(true)",
        "            val body = pullResponse.body()!!\n"
        "            if (body.mutations.size > MAX_PULL_MUTATIONS) {\n"
        "                return SyncRunResult.RetryableFailure(\"Pull page exceeds client safety limit\")\n"
        "            }\n"
        "            experienceDao.setRemoteApplyState(true)",
        1,
    )
    sync = sync.replace(
        "        val payload = mutation.payloadJson ?: return false\n        return runCatching {",
        "        val payload = mutation.payloadJson ?: return false\n"
        "        if (payload.length > MAX_SYNC_PAYLOAD_CHARS) return false\n"
        "        return runCatching {",
        1,
    )

# Remote apply is one transaction so the echo-suppression guard cannot leak to a concurrent writer.
if "database.withTransaction" not in sync:
    sync = sync.replace(
        "class SyncCoordinator(\n    private val governanceDao: GovernanceDao,",
        "class SyncCoordinator(\n    private val database: AppDatabase,\n    private val governanceDao: GovernanceDao,",
        1,
    )
    old = '''            experienceDao.setRemoteApplyState(true)\n            try {\n                body.mutations.forEach { mutation ->'''
    new = '''            database.withTransaction {\n                experienceDao.setRemoteApplyState(true)\n                try {\n                    body.mutations.forEach { mutation ->'''
    if old not in sync:
        raise RuntimeError("Remote apply start marker not found")
    sync = sync.replace(old, new, 1)
    old_end = '''                }\n            } finally {\n                experienceDao.setRemoteApplyState(false)\n            }\n            experienceDao.upsertSyncCursor'''
    new_end = '''                    }\n                } finally {\n                    experienceDao.setRemoteApplyState(false)\n                }\n            }\n            experienceDao.upsertSyncCursor'''
    if old_end not in sync:
        raise RuntimeError("Remote apply end marker not found")
    sync = sync.replace(old_end, new_end, 1)
    sync = sync.replace(
        "        return SyncCoordinator(\n            governanceDao = database.governanceDao(),",
        "        return SyncCoordinator(\n            database = database,\n            governanceDao = database.governanceDao(),",
        1,
    )

# Remote Repository payload must preserve the existing owner cardinality invariant.
repo_parse = '''                    val entity = moshi.adapter(Repository::class.java).fromJson(payload) ?: return false\n                    if (entity.id != mutation.entityId) return false\n                    governanceDao.insertRepository(entity)'''
repo_safe = '''                    val entity = moshi.adapter(Repository::class.java).fromJson(payload) ?: return false\n                    if (entity.id != mutation.entityId) return false\n                    if (entity.ownerType != com.example.data.model.OwnerType.USER && entity.ownerType != com.example.data.model.OwnerType.ORGANIZATION) return false\n                    if (entity.ownerId.isBlank() || entity.enterpriseId.isBlank()) return false\n                    governanceDao.insertRepository(entity)'''
if repo_parse in sync:
    sync = sync.replace(repo_parse, repo_safe, 1)
save(sync_path, sync)

# Repeated FCM hints should coalesce rather than continuously cancel a running sync.
worker_path = "app/src/main/java/com/example/sync/SyncWorker.kt"
worker = read(worker_path).replace("ExistingWorkPolicy.REPLACE", "ExistingWorkPolicy.KEEP")
save(worker_path, worker)

print("current-main mobile v2 security hardening applied")
