package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis

enum class RepositoryWorkView {
    KANBAN,
    NESTED
}

internal data class NestedTaskRow(
    val issue: RepoIssue,
    val depth: Int
)

internal data class NestedTaskProgress(
    val total: Int,
    val closed: Int
)

/**
 * Flattens an arbitrarily deep issue hierarchy into a stable mobile rendering order.
 *
 * No new persistence model is required: RepoIssue.parentIssueId is already a recursive
 * relationship scoped to the Repository. Orphans and malformed cycles are surfaced once
 * instead of disappearing or recursing forever.
 */
internal fun flattenNestedTasks(issues: List<RepoIssue>): List<NestedTaskRow> {
    if (issues.isEmpty()) return emptyList()

    val byId = issues.associateBy { it.id }
    val childrenByParent = issues.groupBy { it.parentIssueId }
    val result = mutableListOf<NestedTaskRow>()
    val visited = mutableSetOf<String>()

    fun visit(issue: RepoIssue, depth: Int) {
        if (!visited.add(issue.id)) return
        result += NestedTaskRow(issue = issue, depth = depth)
        childrenByParent[issue.id]
            .orEmpty()
            .sortedBy { it.issueNumber }
            .forEach { child -> visit(child, depth + 1) }
    }

    issues
        .filter { issue -> issue.parentIssueId == null || issue.parentIssueId !in byId }
        .sortedBy { it.issueNumber }
        .forEach { root -> visit(root, 0) }

    // Defensive fallback: cyclic or otherwise unreachable nodes remain visible exactly once.
    issues
        .sortedBy { it.issueNumber }
        .filterNot { it.id in visited }
        .forEach { remaining -> visit(remaining, 0) }

    return result
}

internal fun nestedTaskProgress(issueId: String, issues: List<RepoIssue>): NestedTaskProgress {
    val childrenByParent = issues.groupBy { it.parentIssueId }
    val visited = mutableSetOf(issueId)
    var total = 0
    var closed = 0

    fun collect(parentId: String) {
        childrenByParent[parentId].orEmpty().forEach { child ->
            if (!visited.add(child.id)) return@forEach
            total += 1
            if (child.status == IssueStatus.CLOSED) closed += 1
            collect(child.id)
        }
    }

    collect(issueId)
    return NestedTaskProgress(total = total, closed = closed)
}

private fun previousStatus(status: IssueStatus): IssueStatus? = when (status) {
    IssueStatus.OPEN -> null
    IssueStatus.IN_PROGRESS -> IssueStatus.OPEN
    IssueStatus.CLOSED -> IssueStatus.IN_PROGRESS
}

private fun nextStatus(status: IssueStatus): IssueStatus? = when (status) {
    IssueStatus.OPEN -> IssueStatus.IN_PROGRESS
    IssueStatus.IN_PROGRESS -> IssueStatus.CLOSED
    IssueStatus.CLOSED -> null
}

private fun boardLabel(status: IssueStatus): String = when (status) {
    IssueStatus.OPEN -> "To do"
    IssueStatus.IN_PROGRESS -> "In progress"
    IssueStatus.CLOSED -> "Done"
}

@Composable
fun RepositoryWorkBoardDialog(
    repo: Repository,
    issues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedView by remember { mutableStateOf(RepositoryWorkView.KANBAN) }
    val nestedRows = remember(issues) { flattenNestedTasks(issues) }
    val nestedCount = remember(issues) { issues.count { it.parentIssueId != null } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .testTag("repository_work_board"),
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedBg,
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                WorkBoardHeader(
                    repo = repo,
                    issueCount = issues.size,
                    nestedCount = nestedCount,
                    onDismiss = onDismiss
                )

                WorkViewSelector(
                    selectedView = selectedView,
                    onSelect = { selectedView = it }
                )

                when (selectedView) {
                    RepositoryWorkView.KANBAN -> KanbanBoard(
                        issues = issues,
                        onUpdateIssueStatus = onUpdateIssueStatus
                    )

                    RepositoryWorkView.NESTED -> NestedTaskTree(
                        rows = nestedRows,
                        allIssues = issues,
                        onUpdateIssueStatus = onUpdateIssueStatus
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkBoardHeader(
    repo: Repository,
    issueCount: Int,
    nestedCount: Int,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SophisticatedSurfaceDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(LavenderContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$issueCount tasks • $nestedCount nested",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis
                )
            }
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(48.dp)
                .testTag("close_repository_work_board")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close work board",
                tint = TextHighEmphasis
            )
        }
    }
}

@Composable
private fun WorkViewSelector(
    selectedView: RepositoryWorkView,
    onSelect: (RepositoryWorkView) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SophisticatedSurfaceDark)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (selectedView == RepositoryWorkView.KANBAN) {
            Button(
                onClick = { onSelect(RepositoryWorkView.KANBAN) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("work_view_kanban"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = LavenderOnPrimary
                )
            ) {
                Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kanban Board")
            }
        } else {
            OutlinedButton(
                onClick = { onSelect(RepositoryWorkView.KANBAN) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("work_view_kanban")
            ) {
                Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kanban Board")
            }
        }

        if (selectedView == RepositoryWorkView.NESTED) {
            Button(
                onClick = { onSelect(RepositoryWorkView.NESTED) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("work_view_nested"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = LavenderOnPrimary
                )
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nested Tasks")
            }
        } else {
            OutlinedButton(
                onClick = { onSelect(RepositoryWorkView.NESTED) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("work_view_nested")
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nested Tasks")
            }
        }
    }
}

@Composable
private fun KanbanBoard(
    issues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    if (issues.isEmpty()) {
        EmptyWorkState(
            iconText = "#",
            title = "No tasks yet",
            message = "Create an Issue in this Repository. It will appear here automatically."
        )
        return
    }

    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.width(4.dp)) }
        items(IssueStatus.entries, key = { it.name }) { status ->
            val statusIssues = issues
                .filter { it.status == status }
                .sortedWith(
                    compareByDescending<RepoIssue> { it.priority == IssuePriority.CRITICAL }
                        .thenByDescending { it.priority == IssuePriority.HIGH }
                        .thenBy { it.issueNumber }
                )
            KanbanColumn(
                status = status,
                issues = statusIssues,
                allIssues = issues,
                onUpdateIssueStatus = onUpdateIssueStatus
            )
        }
        item { Spacer(modifier = Modifier.width(4.dp)) }
    }
}

@Composable
private fun KanbanColumn(
    status: IssueStatus,
    issues: List<RepoIssue>,
    allIssues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        color = SophisticatedSurface,
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = boardLabel(status),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis
                    )
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextLowEmphasis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SophisticatedContainer
                ) {
                    Text(
                        text = issues.size.toString(),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = LavenderGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (issues.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLowEmphasis
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(issues, key = { it.id }) { issue ->
                        WorkIssueCard(
                            issue = issue,
                            progress = nestedTaskProgress(issue.id, allIssues),
                            onUpdateIssueStatus = onUpdateIssueStatus
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NestedTaskTree(
    rows: List<NestedTaskRow>,
    allIssues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    if (rows.isEmpty()) {
        EmptyWorkState(
            iconText = "↳",
            title = "No nested tasks yet",
            message = "Create sub-issues from the existing Issues workspace to build a task hierarchy."
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows, key = { it.issue.id }) { row ->
            NestedTaskCard(
                row = row,
                progress = nestedTaskProgress(row.issue.id, allIssues),
                onUpdateIssueStatus = onUpdateIssueStatus
            )
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun NestedTaskCard(
    row: NestedTaskRow,
    progress: NestedTaskProgress,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    val indent = (row.depth.coerceAtMost(4) * 16).dp

    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(indent))
        Card(
            modifier = Modifier
                .weight(1f)
                .testTag("nested_task_${row.issue.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (row.depth > 0) "↳ #${row.issue.issueNumber} ${row.issue.title}" else "#${row.issue.issueNumber} ${row.issue.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextHighEmphasis
                        )
                        if (row.issue.parentIssueTitle != null) {
                            Text(
                                text = "Parent: ${row.issue.parentIssueTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLowEmphasis,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    StatusPill(row.issue.status)
                }

                if (progress.total > 0) {
                    Text(
                        text = "Nested progress: ${progress.closed}/${progress.total} complete",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (progress.closed == progress.total) EmeraldSuccess else LavenderGlow
                    )
                }

                IssueMetaLine(row.issue)
                StatusMoveActions(row.issue, onUpdateIssueStatus)
            }
        }
    }
}

@Composable
private fun WorkIssueCard(
    issue: RepoIssue,
    progress: NestedTaskProgress,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kanban_task_${issue.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "#${issue.issueNumber} ${issue.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextHighEmphasis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                PriorityPill(issue.priority)
            }

            if (issue.parentIssueTitle != null) {
                Text(
                    text = "↳ ${issue.parentIssueTitle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LavenderGlow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (progress.total > 0) {
                Text(
                    text = "Nested: ${progress.closed}/${progress.total} complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (progress.closed == progress.total) EmeraldSuccess else TextMediumEmphasis
                )
            }

            IssueMetaLine(issue)
            StatusMoveActions(issue, onUpdateIssueStatus)
        }
    }
}

@Composable
private fun IssueMetaLine(issue: RepoIssue) {
    val assignee = issue.assigneeName ?: "Unassigned"
    val labels = issue.labels.ifBlank { "No labels" }
    Text(
        text = "$assignee • $labels",
        style = MaterialTheme.typography.labelSmall,
        color = TextMediumEmphasis,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun StatusMoveActions(
    issue: RepoIssue,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    val previous = previousStatus(issue.status)
    val next = nextStatus(issue.status)
    if (previous == null && next == null) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (previous != null) {
            OutlinedButton(
                onClick = { onUpdateIssueStatus(issue.id, previous) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("task_${issue.id}_move_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(boardLabel(previous), fontSize = 11.sp)
            }
        }
        if (next != null) {
            Button(
                onClick = { onUpdateIssueStatus(issue.id, next) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("task_${issue.id}_move_forward"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = LavenderOnPrimary
                )
            ) {
                Text(boardLabel(next), fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(status: IssueStatus) {
    val color = when (status) {
        IssueStatus.OPEN -> LavenderGlow
        IssueStatus.IN_PROGRESS -> AmberWarning
        IssueStatus.CLOSED -> EmeraldSuccess
    }
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = boardLabel(status),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun PriorityPill(priority: IssuePriority) {
    val color = when (priority) {
        IssuePriority.CRITICAL -> RoseError
        IssuePriority.HIGH -> AmberWarning
        IssuePriority.MEDIUM -> LavenderGlow
        IssuePriority.LOW -> TextMediumEmphasis
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = priority.label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun EmptyWorkState(
    iconText: String,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(LavenderContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    color = LavenderPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis
            )
        }
    }
}
