package com.example.sync

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.CollaborationExperienceDao
import com.example.data.local.GovernanceDao
import com.example.data.model.NoCodeArtifact
import com.example.data.model.PushRegistration
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.SyncConflict
import com.example.data.model.SyncCursor
import com.example.data.model.SyncEntityType
import com.example.data.model.SyncMetadata
import com.example.data.model.SyncOperation
import com.example.data.model.SyncOutbox
import com.example.data.model.SyncState
import com.example.data.model.UserFollow
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.net.URI
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import androidx.room.withTransaction

@JsonClass(generateAdapter = true)
data class SyncPushRequest(
    val eventId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val localVersion: Long,
    val payloadJson: String?
)

@JsonClass(generateAdapter = true)
data class SyncPushResponse(val serverVersion: Long, val cursor: String? = null)

@JsonClass(generateAdapter = true)
data class RemoteMutation(
    val entityType: String,
    val entityId: String,
    val operation: String,
    val serverVersion: Long,
    val payloadJson: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncPullResponse(val cursor: String?, val mutations: List<RemoteMutation>)

@JsonClass(generateAdapter = true)
data class PushRegistrationRequest(val token: String)

interface CollaborationSyncApi {
    @POST("v1/sync/push")
    suspend fun push(
        @Header("Authorization") authorization: String,
        @Body request: SyncPushRequest
    ): Response<SyncPushResponse>

    @GET("v1/sync/pull")
    suspend fun pull(
        @Header("Authorization") authorization: String,
        @Query("cursor") cursor: String?
    ): Response<SyncPullResponse>

    @POST("v1/push-registrations")
    suspend fun registerPushToken(
        @Header("Authorization") authorization: String,
        @Body request: PushRegistrationRequest
    ): Response<Unit>
}

interface AuthTokenProvider {
    suspend fun accessToken(): String?
}

class FirebaseAuthTokenProvider(private val context: Context) : AuthTokenProvider {
    override suspend fun accessToken(): String? {
        if (FirebaseApp.getApps(context).isEmpty()) return null
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return suspendCancellableCoroutine { continuation ->
            user.getIdToken(false)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result.token)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }
}

object SyncEndpointPolicy {
    fun normalizedHttpsBaseUrl(raw: String): String? {
        val value = raw.trim()
        if (value.isBlank() || value.contains(".invalid", ignoreCase = true)) return null
        return runCatching {
            val uri = URI(value)
            if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank() || uri.userInfo != null) return null
            if (value.endsWith('/')) value else "$value/"
        }.getOrNull()
    }
}

private const val MAX_SYNC_PAYLOAD_CHARS = 262_144
private const val MAX_PULL_MUTATIONS = 500

sealed interface SyncRunResult {
    data object Disabled : SyncRunResult
    data object AuthRequired : SyncRunResult
    data class Completed(val pushed: Int, val pulled: Int, val conflicts: Int) : SyncRunResult
    data class RetryableFailure(val reason: String) : SyncRunResult
}

class SyncPayloadAssembler(
    private val governanceDao: GovernanceDao,
    private val experienceDao: CollaborationExperienceDao,
    private val moshi: Moshi
) {
    suspend fun payload(item: SyncOutbox): String? = when (item.entityType) {
        SyncEntityType.REPOSITORY -> governanceDao.getRepositoryByIdOnce(item.entityId)
            ?.let { moshi.adapter(Repository::class.java).toJson(it) }
        SyncEntityType.ISSUE -> governanceDao.getIssueByIdOnce(item.entityId)
            ?.let { moshi.adapter(RepoIssue::class.java).toJson(it) }
        SyncEntityType.ARTIFACT -> governanceDao.getArtifactByIdOnce(item.entityId)
            ?.let { moshi.adapter(NoCodeArtifact::class.java).toJson(it) }
        SyncEntityType.DISCUSSION -> governanceDao.getDiscussionByIdOnce(item.entityId)
            ?.let { moshi.adapter(RepoDiscussion::class.java).toJson(it) }
        SyncEntityType.SAVED_TARGET -> experienceDao.getSavedTargetById(item.entityId)
            ?.let { moshi.adapter(SavedTarget::class.java).toJson(it) }
        SyncEntityType.USER_FOLLOW -> experienceDao.getUserFollowById(item.entityId)
            ?.let { moshi.adapter(UserFollow::class.java).toJson(it) }
        else -> null
    }
}

class SyncCoordinator(
    private val database: AppDatabase,
    private val governanceDao: GovernanceDao,
    private val experienceDao: CollaborationExperienceDao,
    private val api: CollaborationSyncApi,
    private val tokenProvider: AuthTokenProvider,
    private val moshi: Moshi
) {
    suspend fun runOnce(): SyncRunResult {
        val token = tokenProvider.accessToken() ?: return SyncRunResult.AuthRequired
        val authorization = "Bearer $token"
        val assembler = SyncPayloadAssembler(governanceDao, experienceDao, moshi)
        var pushed = 0
        var pulled = 0
        var conflicts = 0

        return try {
            experienceDao.getPendingOutbox().forEach { item ->
                experienceDao.markOutboxState(item.id, SyncState.IN_FLIGHT)
                val payload = if (item.operation == SyncOperation.DELETE) null else assembler.payload(item)
                if (payload != null && payload.length > MAX_SYNC_PAYLOAD_CHARS) {
                    experienceDao.failOutbox(item.id, SyncState.FAILED, "Payload exceeds client safety limit")
                    return@forEach
                }
                if (item.operation != SyncOperation.DELETE && payload == null) {
                    experienceDao.failOutbox(item.id, SyncState.FAILED, "Local entity is missing")
                    return@forEach
                }
                val response = api.push(
                    authorization,
                    SyncPushRequest(
                        eventId = item.id,
                        entityType = item.entityType,
                        entityId = item.entityId,
                        operation = item.operation,
                        localVersion = item.localVersion,
                        payloadJson = payload
                    )
                )
                when {
                    response.isSuccessful && response.body() != null -> {
                        val body = response.body()!!
                        experienceDao.markOutboxState(item.id, SyncState.SYNCED)
                        experienceDao.upsertSyncMetadata(
                            SyncMetadata(
                                id = "${item.entityType}:${item.entityId}",
                                entityType = item.entityType,
                                entityId = item.entityId,
                                localVersion = item.localVersion,
                                serverVersion = body.serverVersion,
                                status = SyncState.SYNCED,
                                updatedAt = System.currentTimeMillis(),
                                deletedAt = if (item.operation == SyncOperation.DELETE) System.currentTimeMillis() else null,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        )
                        body.cursor?.let {
                            experienceDao.upsertSyncCursor(SyncCursor(cursor = it))
                        }
                        pushed += 1
                    }
                    response.code() == 401 || response.code() == 403 -> {
                        experienceDao.failOutbox(item.id, SyncState.AUTH_REQUIRED, "Authentication required")
                        return SyncRunResult.AuthRequired
                    }
                    response.code() == 409 -> {
                        experienceDao.failOutbox(item.id, SyncState.CONFLICT, "Server version conflict")
                        recordConflict(item.entityType, item.entityId, item.localVersion, item.localVersion + 1, "Push conflict")
                        conflicts += 1
                    }
                    else -> {
                        experienceDao.failOutbox(item.id, SyncState.FAILED, "Push failed: HTTP ${response.code()}")
                        return SyncRunResult.RetryableFailure("Push failed: HTTP ${response.code()}")
                    }
                }
            }

            val cursor = experienceDao.getSyncCursor()?.cursor
            val pullResponse = api.pull(authorization, cursor)
            if (pullResponse.code() == 401 || pullResponse.code() == 403) return SyncRunResult.AuthRequired
            if (!pullResponse.isSuccessful || pullResponse.body() == null) {
                return SyncRunResult.RetryableFailure("Pull failed: HTTP ${pullResponse.code()}")
            }
            val body = pullResponse.body()!!
            if (body.mutations.size > MAX_PULL_MUTATIONS) {
                return SyncRunResult.RetryableFailure("Pull page exceeds client safety limit")
            }
            database.withTransaction {
                experienceDao.setRemoteApplyState(true)
                try {
                    body.mutations.forEach { mutation ->
                    if (experienceDao.countUnsynced(mutation.entityType, mutation.entityId) > 0) {
                        val localVersion = experienceDao
                            .getSyncMetadata(mutation.entityType, mutation.entityId)
                            ?.localVersion ?: 0L
                        recordConflict(
                            mutation.entityType,
                            mutation.entityId,
                            localVersion,
                            mutation.serverVersion,
                            "Remote change arrived while local change is pending"
                        )
                        conflicts += 1
                    } else if (applyRemoteMutation(mutation)) {
                        experienceDao.upsertSyncMetadata(
                            SyncMetadata(
                                id = "${mutation.entityType}:${mutation.entityId}",
                                entityType = mutation.entityType,
                                entityId = mutation.entityId,
                                localVersion = mutation.serverVersion,
                                serverVersion = mutation.serverVersion,
                                status = SyncState.SYNCED,
                                updatedAt = System.currentTimeMillis(),
                                deletedAt = if (mutation.operation == SyncOperation.DELETE) System.currentTimeMillis() else null,
                                lastSyncedAt = System.currentTimeMillis()
                            )
                        )
                        pulled += 1
                    } else {
                        recordConflict(
                            mutation.entityType,
                            mutation.entityId,
                            0L,
                            mutation.serverVersion,
                            "Remote payload failed validation or requested a protected hard delete"
                        )
                        conflicts += 1
                    }
                    }
                } finally {
                    experienceDao.setRemoteApplyState(false)
                }
            }
            experienceDao.upsertSyncCursor(SyncCursor(cursor = body.cursor))
            experienceDao.getPushRegistration()?.let { registration ->
                api.registerPushToken(authorization, PushRegistrationRequest(registration.token))
            }
            experienceDao.deleteOldSyncedOutbox(System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L)
            SyncRunResult.Completed(pushed, pulled, conflicts)
        } catch (error: Exception) {
            experienceDao.setRemoteApplyState(false)
            SyncRunResult.RetryableFailure(error.message ?: error::class.java.simpleName)
        }
    }

    private suspend fun applyRemoteMutation(mutation: RemoteMutation): Boolean {
        if (mutation.operation == SyncOperation.DELETE) {
            return when (mutation.entityType) {
                SyncEntityType.SAVED_TARGET -> {
                    experienceDao.deleteSavedTargetById(mutation.entityId)
                    true
                }
                SyncEntityType.USER_FOLLOW -> {
                    experienceDao.deleteUserFollowById(mutation.entityId)
                    true
                }
                // Governance records are not hard-deleted from an unverified remote envelope.
                else -> false
            }
        }
        val payload = mutation.payloadJson ?: return false
        if (payload.length > MAX_SYNC_PAYLOAD_CHARS) return false
        return runCatching {
            when (mutation.entityType) {
                SyncEntityType.REPOSITORY -> {
                    val entity = moshi.adapter(Repository::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId) return false
                    if (entity.ownerType != com.example.data.model.OwnerType.USER && entity.ownerType != com.example.data.model.OwnerType.ORGANIZATION) return false
                    if (entity.ownerId.isBlank() || entity.enterpriseId.isBlank()) return false
                    governanceDao.insertRepository(entity)
                }
                SyncEntityType.ISSUE -> {
                    val entity = moshi.adapter(RepoIssue::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || governanceDao.getRepositoryByIdOnce(entity.repoId) == null) return false
                    governanceDao.insertIssue(entity)
                }
                SyncEntityType.ARTIFACT -> {
                    val entity = moshi.adapter(NoCodeArtifact::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || governanceDao.getRepositoryByIdOnce(entity.repoId) == null) return false
                    governanceDao.insertArtifact(entity)
                }
                SyncEntityType.DISCUSSION -> {
                    val entity = moshi.adapter(RepoDiscussion::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || governanceDao.getRepositoryByIdOnce(entity.repoId) == null) return false
                    governanceDao.insertDiscussion(entity)
                }
                SyncEntityType.SAVED_TARGET -> {
                    val entity = moshi.adapter(SavedTarget::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId) return false
                    experienceDao.upsertSavedTarget(entity)
                }
                SyncEntityType.USER_FOLLOW -> {
                    val entity = moshi.adapter(UserFollow::class.java).fromJson(payload) ?: return false
                    if (entity.id != mutation.entityId || entity.followerUserId == entity.followedUserId) return false
                    experienceDao.upsertUserFollow(entity)
                }
                else -> return false
            }
            true
        }.getOrDefault(false)
    }

    private suspend fun recordConflict(
        entityType: String,
        entityId: String,
        localVersion: Long,
        remoteVersion: Long,
        reason: String
    ) {
        experienceDao.upsertConflict(
            SyncConflict(
                entityType = entityType,
                entityId = entityId,
                localVersion = localVersion,
                remoteVersion = remoteVersion,
                reason = reason
            )
        )
    }
}

object SyncRuntime {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun createCoordinator(context: Context): SyncCoordinator? {
        val baseUrl = SyncEndpointPolicy.normalizedHttpsBaseUrl(BuildConfig.SYNC_BASE_URL) ?: return null
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CollaborationSyncApi::class.java)
        val database = AppDatabase.getInstance(context)
        return SyncCoordinator(
            database = database,
            governanceDao = database.governanceDao(),
            experienceDao = database.collaborationExperienceDao(),
            api = api,
            tokenProvider = FirebaseAuthTokenProvider(context.applicationContext),
            moshi = moshi
        )
    }
}
