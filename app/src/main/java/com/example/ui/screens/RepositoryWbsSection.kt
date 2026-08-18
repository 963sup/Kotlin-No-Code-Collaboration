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
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@Composable
fun RepositoryWbsSection(issues: List<RepoIssue>, onUpdatePlan: (String, Int, Long?, Long?, Double, Int) -> Unit) {
    val rows = remember(issues) { IssueHierarchyRules.wbsProjection(issues) }
    val overallProgress = remember(issues) { IssueHierarchyRules.overallProgress(issues) }
    val parentIds = remember(issues) { issues.mapNotNull { it.parentIssueId }.toSet() }
    if (rows.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            CollaborationEmptyStateCard("WBS 尚無工作", "Repository Issue 建立後會自動形成工作分解樹。")
        }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "wbs_summary") {
            Card(Modifier.fillMaxWidth().testTag("repo_wbs_summary")) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("WBS 工作樹", fontWeight = FontWeight.Bold)
                            Text(
                                "${rows.size} 個工作節點 · ${rows.count { it.depth == 0 }} 個根工作",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            "${(overallProgress * 100).toInt()}%",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier.fillMaxWidth().testTag("repo_wbs_overall_progress"),
                    )
                }
            }
        }
        items(rows, key = { it.issue.id }) { row ->
            val issue = row.issue
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(start = (row.depth.coerceAtMost(6) * 12).dp)
                    .testTag("repo_wbs_${issue.id}"),
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${row.code} ${issue.title}", fontWeight = FontWeight.SemiBold)
                        Text("${(row.progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(progress = { row.progress }, modifier = Modifier.fillMaxWidth())
                    Text(
                        "${issue.status.label} · ${row.completedCount}/${row.totalCount} 完成 · 權重 ${issue.wbsWeight}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "計畫 ${formatPlanDate(
                            issue.plannedStartAt,
                        )} → ${formatPlanDate(issue.plannedEndAt)} · 排序 ${issue.sortOrder}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (issue.id !in parentIds && issue.status != IssueStatus.CLOSED) {
                        Button(
                            onClick = {
                                onUpdatePlan(
                                    issue.id,
                                    issue.sortOrder,
                                    issue.plannedStartAt,
                                    issue.plannedEndAt,
                                    issue.wbsWeight,
                                    min(100, issue.progressPercent + 10),
                                )
                            },
                            modifier = Modifier.testTag("repo_wbs_progress_${issue.id}"),
                        ) { Text("完成率 +10%") }
                    }
                }
            }
        }
    }
}

private fun formatPlanDate(timestamp: Long?): String = timestamp?.let {
    SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).format(Date(it))
} ?: "未排"
