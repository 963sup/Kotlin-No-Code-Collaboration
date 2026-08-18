package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NoCodeArtifact
import com.example.data.model.Organization
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.User
import com.example.navigation.CollaborationTarget
import com.example.ui.model.ExploreProjection

@Composable
fun UnifiedExploreScreen(
    activeUser: User?,
    repositories: List<Repository>,
    artifacts: List<NoCodeArtifact>,
    issues: List<RepoIssue>,
    discussions: List<RepoDiscussion>,
    organizations: List<Organization>,
    teams: List<Team>,
    users: List<User>,
    savedTargets: List<SavedTarget>,
    onOpenTarget: (CollaborationTarget) -> Unit,
    onToggleSaved: (CollaborationTarget) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(activeUser, repositories, artifacts, issues, discussions, organizations, teams, users, savedTargets) {
        ExploreProjection.build(activeUser, repositories, artifacts, issues, discussions, organizations, teams, users, savedTargets)
    }
    val visible = remember(results, query) {
        val q = query.trim()
        if (q.isBlank()) results else results.filter {
            it.title.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true) || it.searchableText.contains(q, ignoreCase = true)
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("探索", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().testTag("explore_search"),
            label = { Text("搜尋儲存庫、Issue、成果、討論、團隊與用戶") },
            singleLine = true
        )
        if (visible.isEmpty()) {
            CollaborationEmptyStateCard("沒有結果", "目前授權範圍內沒有符合條件的協作目標。")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.target.toString() }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenTarget(item.target) }
                            .testTag("explore_result_${item.typeLabel}_${item.title.hashCode()}")
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("${item.typeLabel} · ${item.title}", fontWeight = FontWeight.SemiBold)
                            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            IconButton(onClick = { onToggleSaved(item.target) }) {
                                Text(if (item.isSaved) "★" else "☆", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
