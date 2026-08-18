package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.SyncState
import com.example.data.model.SyncStatusSummary
import com.example.data.model.UserFollow
import com.example.navigation.CollaborationTarget
import com.example.navigation.storageKey
import com.example.navigation.toSavedTarget
import com.example.sync.SyncScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollaborationExperienceViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).collaborationExperienceDao()

    val savedTargets = dao.observeSavedTargets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val userFollows = dao.observeUserFollows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val outbox = dao.observeOutbox()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val conflicts = dao.observeConflicts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val lastSyncedAt = dao.observeLastSyncedAt()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val syncStatus = combine(outbox, conflicts, lastSyncedAt) { queue, conflictRows, lastSync ->
        SyncStatusSummary(
            pending = queue.count { it.state == SyncState.PENDING || it.state == SyncState.IN_FLIGHT },
            failed = queue.count { it.state == SyncState.FAILED },
            conflicts = conflictRows.count { it.resolvedAt == null },
            authRequired = queue.count { it.state == SyncState.AUTH_REQUIRED },
            lastSyncedAt = lastSync,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncStatusSummary())

    init {
        SyncScheduler.ensurePeriodic(application)
    }

    fun toggleSaved(userId: String, target: CollaborationTarget) {
        viewModelScope.launch {
            val key = target.storageKey()
            val existing = dao.getSavedTarget(userId, key)
            if (existing == null) {
                dao.upsertSavedTarget(target.toSavedTarget(userId))
            } else {
                dao.deleteSavedTarget(userId, key)
            }
        }
    }

    fun toggleFollow(followerUserId: String, followedUserId: String) {
        if (followerUserId == followedUserId) return
        viewModelScope.launch {
            val existing = dao.getUserFollow(followerUserId, followedUserId)
            if (existing == null) {
                dao.upsertUserFollow(
                    UserFollow(
                        followerUserId = followerUserId,
                        followedUserId = followedUserId,
                    ),
                )
            } else {
                dao.deleteUserFollow(followerUserId, followedUserId)
            }
        }
    }

    fun syncNow() {
        SyncScheduler.requestNow(getApplication())
    }
}
