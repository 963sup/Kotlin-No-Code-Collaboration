package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.SavedTarget
import com.example.data.model.SyncStatusSummary
import com.example.data.model.User
import com.example.data.model.UserFollow

@Composable
fun PersonalCenterSwitchScreen(
    profileUser: User,
    activeUser: User,
    auditLogs: List<AuditLog>,
    visibleRepositoryIds: Set<String>,
    follows: List<UserFollow>,
    savedTargets: List<SavedTarget>,
    syncStatus: SyncStatusSummary,
    onToggleFollow: (String) -> Unit,
    onSyncNow: () -> Unit,
    governanceContent: @Composable () -> Unit,
) {
    var social by rememberSaveable(profileUser.id) { mutableStateOf(true) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            FilterChip(selected = social, onClick = { social = true }, label = { Text("成就與動態") })
            FilterChip(
                selected = !social,
                onClick = { social = false },
                label = { Text("身份與治理") },
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(Modifier.fillMaxWidth()) {
            if (social) {
                SocialProfileScreen(profileUser, activeUser, auditLogs, visibleRepositoryIds, follows, savedTargets, syncStatus, onToggleFollow, onSyncNow)
            } else {
                governanceContent()
            }
        }
    }
}
