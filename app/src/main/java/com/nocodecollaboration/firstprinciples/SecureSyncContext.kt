package com.nocodecollaboration.firstprinciples

/**
 * Runtime-only credential wrapper. Its string representation is always redacted,
 * and equality/hashCode are intentionally not value-based to reduce accidental use
 * as a cache key or log field.
 */
class SecureBearerToken private constructor(
    private val value: String,
) {
    init {
        require(value.isNotBlank()) { "Bearer token cannot be blank" }
    }

    fun <T> useAuthorizationHeader(block: (String) -> T): T = block("Bearer $value")

    override fun toString(): String = "SecureBearerToken(<redacted>)"

    companion object {
        fun fromAuthenticatedSession(value: String): SecureBearerToken = SecureBearerToken(value)
    }
}

class SecureAuthenticatedSyncContext(
    val enterpriseId: String,
    val userId: String,
    val token: SecureBearerToken,
) {
    override fun toString(): String =
        "SecureAuthenticatedSyncContext(enterpriseId=$enterpriseId, userId=$userId, token=<redacted>)"
}

/** Production adapters should implement this contract rather than logging raw context objects. */
interface SecureAuthenticatedSyncTransport {
    suspend fun push(
        context: SecureAuthenticatedSyncContext,
        item: SyncOutboxItem,
    ): ServerVersion

    suspend fun pull(
        context: SecureAuthenticatedSyncContext,
        cursor: SyncCursor,
        limit: Int = 100,
    ): List<RemoteChange>
}
