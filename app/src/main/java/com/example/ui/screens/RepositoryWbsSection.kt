package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.RepoIssue
import kotlin.math.min

@Composable
fun RepositoryWbsSection(
    issues: List<RepoIssue>,
    onUpdatePlan: (String, Int, Long?, Long?, Double, Int) -> Unit
) {
    val rows = remember(issues) { IssueHierarchyRules.wbsProjection(issues) }
    val parentIds = remember(issues) { issues.mapNotNull { it.parentIssueId }.toSet() }
    if (rows.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            CollaborationEmptyStateCard("WBS 尚無工作", "Repository Issue 建立後會自動形成工作分解樹。")
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(rows, key = { it.issue.id }) { row ->
            val issue = row.issue
            Card(Modifier.fillMaxWidth().padding(start = (row.depth * 12).dp).testTag("repo_wbs_${issue.id}")) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${row.code} ${issue.title}", fontWeight = FontWeight.SemiBold)
                        Text("${(row.progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(progress = { row.progress }, modifier = Modifier.fillMaxWidth())
                    Text(
                        "排序 ${issue.sortOrder} · 權重 ${issue.wbsWeight} · ${issue.plannedStartAt ?: "未排開始"} → ${issue.plannedEndAt ?: "未排結束"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (issue.id !in parentIds && issue.status != com.example.data.model.IssueStatus.CLOSED) {
                        Button(
                            onClick = {
                                onUpdatePlan(
                                    issue.id,
                                    issue.sortOrder,
                                    issue.plannedStartAt,
                                    issue.plannedEndAt,
                                    issue.wbsWeight,
                                    min(100, issue.progressPercent + 10)
                                )
                            },
                            modifier = Modifier.testTag("repo_wbs_progress_${issue.id}")
                        ) { Text("進度 +10%") }
                    }
                }
            }
        }
    }
}
