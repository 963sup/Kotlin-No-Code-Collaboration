package com.nocodecollaboration.firstprinciples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScopeAwareHomeContent(
    scope: WorkspaceScope,
    summary: OperationalSummary,
    onRepositoriesClick: () -> Unit,
    onIssuesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = when (scope.kind) {
                    WorkspaceScopeKind.ENTERPRISE -> "企業營運總覽"
                    WorkspaceScopeKind.ORGANIZATION -> "組織營運總覽"
                    WorkspaceScopeKind.TEAM -> "團隊工作空間"
                    WorkspaceScopeKind.USER -> "我的工作總覽"
                },
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            MetricCard(
                title = "可存取儲存庫",
                value = summary.accessibleRepositoryCount.toString(),
                onClick = onRepositoriesClick,
            )
        }
        item {
            MetricCard(
                title = "開放工作／阻塞",
                value = "${summary.openIssueCount}／${summary.blockedIssueCount}",
                onClick = onIssuesClick,
            )
        }
        item {
            MetricCard(
                title = "待處理通知",
                value = summary.pendingNotificationCount.toString(),
                onClick = onNotificationsClick,
            )
        }
        item {
            MetricCard(
                title = "近期成果／稽核事件",
                value = "${summary.recentArtifactCount}／${summary.recentAuditEventCount}",
                onClick = {},
            )
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun RepositoryWbsContent(
    nodes: List<WbsIssueProjection>,
    issueTitles: Map<String, String>,
    onIssueClick: (String) -> Unit,
) {
    val numbering = WbsProjection.numbering(nodes)
    val progress = WbsProjection.rollUp(nodes)
    val ordered = nodes.sortedBy { numbering[it.issueId] ?: "" }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("WBS 工作樹", style = MaterialTheme.typography.headlineSmall)
        }
        items(ordered, key = { it.issueId }) { node ->
            val number = numbering[node.issueId].orEmpty()
            val depth = number.count { it == '.' }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (depth * 16).dp)
                    .clickable { onIssueClick(node.issueId) },
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("$number  ${issueTitles[node.issueId] ?: node.issueId}")
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (progress[node.issueId] ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("${progress[node.issueId] ?: 0}%")
                }
            }
        }
    }
}

@Composable
fun MyWorkContent(
    grouped: Map<WorkStatus, List<AccessibleIssue>>,
    repositoryNames: Map<String, String>,
    activeRepositoryFilter: String?,
    onRepositoryFilterClick: () -> Unit,
    onIssueClick: (AccessibleIssue) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("我的工作", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onRepositoryFilterClick) {
                    Text(activeRepositoryFilter?.let { repositoryNames[it] } ?: "全部儲存庫")
                }
            }
        }
        WorkStatus.entries.forEach { status ->
            item {
                Text(
                    text = when (status) {
                        WorkStatus.TODO -> "待處理"
                        WorkStatus.IN_PROGRESS -> "進行中"
                        WorkStatus.DONE -> "已完成"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(grouped[status].orEmpty(), key = { it.issueId }) { issue ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIssueClick(issue) },
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(issue.title)
                        Text(
                            repositoryNames[issue.repositoryId] ?: issue.repositoryId,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreContent(
    query: String,
    results: List<SearchableCollaborationItem>,
    savedStableIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onTargetClick: (CollaborationTarget) -> Unit,
    onToggleSaved: (CollaborationTarget) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("探索", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜尋儲存庫、工作、文件、討論、團隊或用戶") },
                singleLine = true,
            )
        }
        items(results, key = { "${it.target::class.simpleName}:${it.target.stableId}" }) { result ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTargetClick(result.target) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(result.title)
                        Text(
                            result.target::class.simpleName.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { onToggleSaved(result.target) }) {
                        Text(if (result.target.stableId in savedStableIds) "已收藏" else "收藏")
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalCenterContent(
    displayName: String,
    projection: AchievementProjection,
    followers: Int,
    following: Int,
    savedTargets: Int,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(displayName, style = MaterialTheme.typography.headlineSmall)
            Text("Lv.${projection.level}　${projection.xp} XP")
        }
        item { MetricCard("成就", projection.awards.size.toString(), onClick = {}) }
        item { MetricCard("追隨者／追蹤中", "$followers／$following", onClick = {}) }
        item { MetricCard("我的收藏", savedTargets.toString(), onClick = {}) }
    }
}
