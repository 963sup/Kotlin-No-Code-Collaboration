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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.ui.theme.*

@Composable
fun IssueListView(
    issues: List<RepoIssue>,
    onSelectIssue: (RepoIssue) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(issues) { issue ->
            IssueItemCard(issue = issue, onClick = { onSelectIssue(issue) })
        }
    }
}

@Composable
private fun IssueItemCard(issue: RepoIssue, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "#${issue.issueNumber}",
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = issue.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "截止日: 2024-05-20 • 負責人: 王小明",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            }

            Surface(
                color = when (issue.priority) {
                    IssuePriority.CRITICAL, IssuePriority.HIGH -> RoseDark
                    IssuePriority.MEDIUM -> AmberGlow
                    else -> SophisticatedSurfaceDark
                },
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = when (issue.priority) {
                        IssuePriority.CRITICAL, IssuePriority.HIGH -> "高優先級"
                        IssuePriority.MEDIUM -> "中優先級"
                        else -> "低優先級"
                    },
                    color = when (issue.priority) {
                        IssuePriority.CRITICAL, IssuePriority.HIGH -> RoseError
                        IssuePriority.MEDIUM -> AmberWarning
                        else -> TextMediumEmphasis
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}
