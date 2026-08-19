package com.example.ui.work

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.ui.theme.*

@Composable
fun KanbanBoardView(
    issues: List<RepoIssue>,
    onSelectIssue: (RepoIssue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todoIssues = issues.filter { it.status == IssueStatus.OPEN }
    val inProgressIssues = issues.filter { it.status == IssueStatus.IN_PROGRESS }
    val completedIssues = issues.filter { it.status == IssueStatus.CLOSED }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            KanbanColumnHeader(title = "進行中", count = inProgressIssues.size, color = LavenderPrimary)
        }
        items(inProgressIssues) { issue ->
            WorkItemKanbanCard(issue = issue, onClick = { onSelectIssue(issue) })
        }

        item {
            KanbanColumnHeader(title = "待處理", count = todoIssues.size, color = AmberWarning)
        }
        items(todoIssues) { issue ->
            WorkItemKanbanCard(issue = issue, onClick = { onSelectIssue(issue) })
        }

        item {
            KanbanColumnHeader(title = "已完成", count = completedIssues.size, color = EmeraldSuccess)
        }
        items(completedIssues) { issue ->
            WorkItemKanbanCard(issue = issue, onClick = { onSelectIssue(issue) })
        }
    }
}

@Composable
private fun KanbanColumnHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp),
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Text(
            text = "($count)",
            fontSize = 13.sp,
            color = TextMediumEmphasis,
        )
    }
}

@Composable
fun WorkItemKanbanCard(issue: RepoIssue, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${issue.issueNumber} ${issue.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "來源: WBS-3.2",
                    fontSize = 11.sp,
                    color = TextMediumEmphasis,
                )
                Text(
                    text = "負責人: 王小明",
                    fontSize = 11.sp,
                    color = LavenderPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
