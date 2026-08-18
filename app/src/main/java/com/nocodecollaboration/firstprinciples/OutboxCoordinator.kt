package com.nocodecollaboration.firstprinciples

interface OutboxStore {
    suspend fun due(
        enterpriseId: String,
        nowEpochMillis: Long,
        limit: Int,
    ): List<SyncOutboxItem>

    suspend fun mark(
        itemId: String,
        enterpriseId: String,
        state: OutboxState,
        attempts: Int,
        nextAttemptAtEpochMillis: Long,
    )
}

fun interface SyncSessionProvider {
    fun requireSession(): SecureAuthenticatedSyncContext
}

fun interface EpochClock {
    fun nowEpochMillis(): Long
}

class VersionConflictException(
    val remoteVersion: ServerVersion,
) : Exception("Remote version conflict")

class RetryableSyncException(message: String) : Exception(message)
class PermanentSyncException(message: String) : Exception(message)

data class OutboxRunSummary(
    val applied: Int,
    val conflicts: Int,
    val retrying: Int,
    val deadLettered: Int,
)

/**
 * Enterprise-scoped, bounded outbox execution. A row from another enterprise is
 * rejected even if an underlying store implementation returns it by mistake.
 */
class OutboxCoordinator(
    private val sessionProvider: SyncSessionProvider,
    private val store: OutboxStore,
    private val transport: SecureAuthenticatedSyncTransport,
    private val clock: EpochClock,
) {
    suspend fun runOnce(limit: Int = 50): OutboxRunSummary {
        require(limit in 1..100) { "Outbox batch limit must be between 1 and 100" }
        val session = sessionProvider.requireSession()
        val now = clock.nowEpochMillis()
        val batch = store.due(session.enterpriseId, now, limit)

        var applied = 0
        var conflicts = 0
        var retrying = 0
        var deadLettered = 0

        for (item in batch) {
            check(item.enterpriseId == session.enterpriseId) {
                "Outbox store returned a cross-enterprise row"
            }
            val attempt = item.attempts + 1
            store.mark(
                itemId = item.id,
                enterpriseId = session.enterpriseId,
                state = OutboxState.IN_FLIGHT,
                attempts = attempt,
                nextAttemptAtEpochMillis = now,
            )

            try {
                transport.push(session, item)
                store.mark(
                    itemId = item.id,
                    enterpriseId = session.enterpriseId,
                    state = OutboxState.APPLIED,
                    attempts = attempt,
                    nextAttemptAtEpochMillis = now,
                )
                applied += 1
            } catch (_: VersionConflictException) {
                store.mark(
                    itemId = item.id,
                    enterpriseId = session.enterpriseId,
                    state = OutboxState.CONFLICT,
                    attempts = attempt,
                    nextAttemptAtEpochMillis = now,
                )
                conflicts += 1
            } catch (_: RetryableSyncException) {
                val next = now + RetryPolicy.nextDelaySeconds(attempt) * 1_000L
                store.mark(
                    itemId = item.id,
                    enterpriseId = session.enterpriseId,
                    state = OutboxState.RETRY,
                    attempts = attempt,
                    nextAttemptAtEpochMillis = next,
                )
                retrying += 1
            } catch (_: PermanentSyncException) {
                store.mark(
                    itemId = item.id,
                    enterpriseId = session.enterpriseId,
                    state = OutboxState.DEAD_LETTER,
                    attempts = attempt,
                    nextAttemptAtEpochMillis = now,
                )
                deadLettered += 1
            }
        }

        return OutboxRunSummary(applied, conflicts, retrying, deadLettered)
    }
}
