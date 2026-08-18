package com.nocodecollaboration.firstprinciples

@JvmInline
value class ServerVersion(val value: Long)

enum class OutboxState {
    PENDING,
    IN_FLIGHT,
    RETRY,
    CONFLICT,
    APPLIED,
    DEAD_LETTER,
}

data class SyncOutboxItem(
    val id: String,
    val enterpriseId: String,
    val actorUserId: String,
    val aggregateType: String,
    val aggregateId: String,
    val operation: String,
    val payloadJson: String,
    val expectedServerVersion: ServerVersion?,
    val idempotencyKey: String,
    val state: OutboxState,
    val attempts: Int,
    val nextAttemptAtEpochMillis: Long,
)

data class SyncCursor(
    val enterpriseId: String,
    val stream: String,
    val cursor: String?,
    val updatedAtEpochMillis: Long,
)

data class AuthenticatedSyncContext(
    val enterpriseId: String,
    val userId: String,
    /** Supplied at runtime by an authenticated session provider; never persisted here. */
    val bearerToken: String,
)

data class RemoteChange(
    val stream: String,
    val cursor: String,
    val payloadJson: String,
    val serverVersion: ServerVersion,
)

interface AuthenticatedSyncTransport {
    suspend fun push(
        context: AuthenticatedSyncContext,
        item: SyncOutboxItem,
    ): ServerVersion

    suspend fun pull(
        context: AuthenticatedSyncContext,
        cursor: SyncCursor,
        limit: Int = 100,
    ): List<RemoteChange>
}

sealed interface PushTargetHint {
    data class Target(val target: CollaborationTarget) : PushTargetHint
    data object Invalid : PushTargetHint
}

/** Push payloads are untrusted hints and must pass SafeTargetResolver before navigation. */
fun interface PushTargetParser {
    fun parse(untrustedPayload: Map<String, String>): PushTargetHint
}

object RetryPolicy {
    /** Bounded exponential backoff; callers add jitter before scheduling. */
    fun nextDelaySeconds(attempt: Int): Long =
        (1L shl attempt.coerceIn(0, 10)).coerceAtMost(900L)
}

sealed interface SyncApplyResult {
    data class Applied(val newVersion: ServerVersion) : SyncApplyResult
    data class Conflict(
        val localExpectedVersion: ServerVersion?,
        val remoteVersion: ServerVersion,
    ) : SyncApplyResult
    data class RetryableFailure(val reason: String) : SyncApplyResult
    data class PermanentFailure(val reason: String) : SyncApplyResult
}
