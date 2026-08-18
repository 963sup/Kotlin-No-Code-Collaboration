package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditLog
import com.example.data.model.SavedTarget
import com.example.data.model.SyncStatusSummary
import com.example.data.model.User
import com.example.data.model.UserFollow
import com.example.ui.model.SocialProjection

@Composable
fun SocialProfileScreen(
    profileUser: User,
    activeUser: User,
    auditLogs: List<AuditLog>,
    visibleRepositoryIds: Set<String>,
    follows: List<UserFollow>,
    savedTargets: List<SavedTarget>,
    syncStatus: SyncStatusSummary,
    onToggleFollow: (String) -> Unit,
    onSyncNow: () -> Unit
) {
    val stats = SocialProjection.stats(profileUser, auditLogs, visibleRepositoryIds)
    val followers = follows.count { it.followedUserId == profileUser.id }
    val following = follows.count { it.followerUserId == profileUser.id }
    val isFollowing = follows.any { it.followerUserId == activeUser.id && it.followedUserId == profileUser.id }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(profileUser.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("@${profileUser.username} · ${profileUser.title}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Lv.${stats.level} · XP ${stats.xp}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("公開協作成果 ${stats.publicActions} · 追隨者 $followers · 追蹤中 $following")
                if (profileUser.id == activeUser.id) {
                    Text("收藏 ${savedTargets.count { it.userId == activeUser.id }}")
                }
            }
        }
        if (profileUser.id != activeUser.id) {
            OutlinedButton(onClick = { onToggleFollow(profileUser.id) }, modifier = Modifier.testTag("profile_follow")) {
                Text(if (isFollowing) "取消追蹤" else "追蹤")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSyncNow, modifier = Modifier.testTag("profile_sync_now")) { Text("立即同步") }
                Text("待同步 ${syncStatus.pending} · 衝突 ${syncStatus.conflicts} · 失敗 ${syncStatus.failed}")
            }
        }
    }
}
