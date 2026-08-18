package com.nocodecollaboration.firstprinciples

/**
 * Defense-in-depth wrapper for devices that can change signed-in users. It prevents
 * a new session from transmitting outbox mutations created by a previous actor,
 * even when both users belong to the same enterprise.
 */
class SessionBoundOutboxStore(
    private val delegate: OutboxStore,
    private val sessionProvider: SyncSessionProvider,
) : OutboxStore {
    override suspend fun due(
        enterpriseId: String,
        nowEpochMillis: Long,
        limit: Int,
    ): List<SyncOutboxItem> {
        val session = sessionProvider.requireSession()
        check(enterpriseId == session.enterpriseId) { "Cross-enterprise outbox query denied" }
        return delegate.due(enterpriseId, nowEpochMillis, limit).also { rows ->
            check(rows.all { it.enterpriseId == session.enterpriseId }) {
                "Outbox store returned a cross-enterprise row"
            }
            check(rows.all { it.actorUserId == session.userId }) {
                "Outbox row belongs to a different authenticated actor"
            }
        }
    }

    override suspend fun mark(
        itemId: String,
        enterpriseId: String,
        state: OutboxState,
        attempts: Int,
        nextAttemptAtEpochMillis: Long,
    ) {
        val session = sessionProvider.requireSession()
        check(enterpriseId == session.enterpriseId) { "Cross-enterprise outbox mutation denied" }
        delegate.mark(
            itemId = itemId,
            enterpriseId = enterpriseId,
            state = state,
            attempts = attempts,
            nextAttemptAtEpochMillis = nextAttemptAtEpochMillis,
        )
    }
}
