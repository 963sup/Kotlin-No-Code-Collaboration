package com.nocodecollaboration.firstprinciples

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OutboxCoordinatorTest {
    @Test
    fun rejectsCrossEnterpriseRowsBeforeTransport() = runBlocking {
        val row = sampleItem(enterpriseId = "enterprise-b")
        val store = RecordingStore(listOf(row))
        val transport = RecordingTransport()
        val coordinator = OutboxCoordinator(
            sessionProvider = SyncSessionProvider {
                SecureAuthenticatedSyncContext(
                    enterpriseId = "enterprise-a",
                    userId = "user-a",
                    token = SecureBearerToken.fromAuthenticatedSession("runtime-token"),
                )
            },
            store = store,
            transport = transport,
            clock = EpochClock { 100L },
        )

        var failedClosed = false
        try {
            coordinator.runOnce()
        } catch (_: IllegalStateException) {
            failedClosed = true
        }

        assertEquals(true, failedClosed)
        assertEquals(0, transport.pushes)
    }

    @Test
    fun retryableFailureUsesBoundedRetryState() = runBlocking {
        val row = sampleItem(enterpriseId = "enterprise-a")
        val store = RecordingStore(listOf(row))
        val transport = RecordingTransport(retry = true)
        val coordinator = OutboxCoordinator(
            sessionProvider = SyncSessionProvider {
                SecureAuthenticatedSyncContext(
                    enterpriseId = "enterprise-a",
                    userId = "user-a",
                    token = SecureBearerToken.fromAuthenticatedSession("runtime-token"),
                )
            },
            store = store,
            transport = transport,
            clock = EpochClock { 1_000L },
        )

        val summary = coordinator.runOnce()

        assertEquals(1, summary.retrying)
        assertEquals(OutboxState.RETRY, store.states.last())
    }

    private fun sampleItem(enterpriseId: String) = SyncOutboxItem(
        id = "outbox-1",
        enterpriseId = enterpriseId,
        actorUserId = "user-a",
        aggregateType = "ISSUE",
        aggregateId = "issue-1",
        operation = "UPDATE",
        payloadJson = "{}",
        expectedServerVersion = ServerVersion(1),
        idempotencyKey = "idem-1",
        state = OutboxState.PENDING,
        attempts = 0,
        nextAttemptAtEpochMillis = 0L,
    )

    private class RecordingStore(
        private val rows: List<SyncOutboxItem>,
    ) : OutboxStore {
        val states = mutableListOf<OutboxState>()

        override suspend fun due(
            enterpriseId: String,
            nowEpochMillis: Long,
            limit: Int,
        ): List<SyncOutboxItem> = rows

        override suspend fun mark(
            itemId: String,
            enterpriseId: String,
            state: OutboxState,
            attempts: Int,
            nextAttemptAtEpochMillis: Long,
        ) {
            states += state
        }
    }

    private class RecordingTransport(
        private val retry: Boolean = false,
    ) : SecureAuthenticatedSyncTransport {
        var pushes: Int = 0

        override suspend fun push(
            context: SecureAuthenticatedSyncContext,
            item: SyncOutboxItem,
        ): ServerVersion {
            pushes += 1
            if (retry) throw RetryableSyncException("temporary")
            return ServerVersion(2)
        }

        override suspend fun pull(
            context: SecureAuthenticatedSyncContext,
            cursor: SyncCursor,
            limit: Int,
        ): List<RemoteChange> = emptyList()
    }
}
