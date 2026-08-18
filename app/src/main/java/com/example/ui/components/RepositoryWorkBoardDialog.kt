package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

enum class RepositoryWorkView { KANBAN, NESTED }

internal data class NestedTaskRow(val issue: RepoIssue, val depth: Int)
internal data class NestedTaskProgress(val total: Int, val closed: Int)

/** RepoIssue.parentIssueId is already the canonical recursive task relationship. */
internal fun flattenNestedTasks(issues: List<RepoIssue>): List<NestedTaskRow> {
    if (issues.isEmpty()) return emptyList()
    val byId = issues.associateBy { it.id }
    val children = issues.groupBy { it.parentIssueId }
    val result = mutableListOf<NestedTaskRow>()
    val visited = mutableSetOf<String>()

    fun visit(issue: RepoIssue, depth: Int) {
        if (!visited.add(issue.id)) return
        result += NestedTaskRow(issue, depth)
        children[issue.id].orEmpty().sortedBy { it.issueNumber }.forEach { visit(it, depth + 1) }
    }

    issues
        .filter { it.parentIssueId == null || it.parentIssueId !in byId }
        .sortedBy { it.issueNumber }
        .forEach { visit(it, 0) }

    // Keep malformed cyclic/unreachable records visible exactly once instead of recursing forever.
    issues.sortedBy { it.issueNumber }.filterNot { it.id in visited }.forEach { visit(it, 0) }
    return result
}

internal fun nestedTaskProgress(issueId: String, issues: List<RepoIssue>): NestedTaskProgress {
    val children = issues.groupBy { it.parentIssueId }
    val visited = mutableSetOf(issueId)
    var total = 0
    var closed = 0

    fun collect(parentId: String) {
        children[parentId].orEmpty().forEach { child ->
            if (!visited.add(child.id)) return@forEach
            total++
            if (child.status == IssueStatus.CLOSED) closed++
            collect(child.id)
        }
    }

    collect(issueId)
    return NestedTaskProgress(total, closed)
}

private fun previousStatus(status: IssueStatus) = when (status) {
    IssueStatus.OPEN -> null
    IssueStatus.IN_PROGRESS -> IssueStatus.OPEN
    IssueStatus.CLOSED -> IssueStatus.IN_PROGRESS
}

private fun nextStatus(status: IssueStatus) = when (status) {
    IssueStatus.OPEN -> IssueStatus.IN_PROGRESS
    IssueStatus.IN_PROGRESS -> IssueStatus.CLOSED
    IssueStatus.CLOSED -> null
}

private fun boardLabel(status: IssueStatus) = when (status) {
    IssueStatus.OPEN -> "待處理"
    IssueStatus.IN_PROGRESS -> "進行中"
    IssueStatus.CLOSED -> "已完成"
}

@Composable
fun RepositoryWorkBoardDialog(
    repo: Repository,
    issues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit,
    onDismiss: () -> Unit
) {
    var view by remember { mutableStateOf(RepositoryWorkView.KANBAN) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(10.dp).testTag("repository_work_board"),
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedBg,
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Column(Modifier.fillMaxSize()) {
                BoardHeader(repo, issues, onDismiss)
                ViewSelector(view) { view = it }
                when (view) {
                    RepositoryWorkView.KANBAN -> KanbanBoard(issues, onUpdateIssueStatus)
                    RepositoryWorkView.NESTED -> NestedTasks(issues, onUpdateIssueStatus)
                }
            }
        }
    }
}

@Composable
private fun BoardHeader(repo: Repository, issues: List<RepoIssue>, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(SophisticatedSurfaceDark).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(LavenderContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Dashboard, null, tint = LavenderPrimary)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(repo.displayName, fontWeight = FontWeight.Bold, color = TextHighEmphasis, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${issues.size} 個任務 • ${issues.count { it.parentIssueId != null }} 個巢狀任務", color = TextMediumEmphasis, fontSize = 12.sp)
            }
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp).testTag("close_repository_work_board")) {
            Icon(Icons.Default.Close, "關閉工作看板", tint = TextHighEmphasis)
        }
    }
}

@Composable
private fun ViewSelector(selected: RepositoryWorkView, onSelect: (RepositoryWorkView) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(SophisticatedSurfaceDark).padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ViewButton(
            selected = selected == RepositoryWorkView.KANBAN,
            label = "工作看板",
            icon = { Icon(Icons.Default.Dashboard, null, Modifier.size(18.dp)) },
            tag = "work_view_kanban",
            modifier = Modifier.weight(1f)
        ) { onSelect(RepositoryWorkView.KANBAN) }
        ViewButton(
            selected = selected == RepositoryWorkView.NESTED,
            label = "巢狀任務",
            icon = { Icon(Icons.Default.AccountTree, null, Modifier.size(18.dp)) },
            tag = "work_view_nested",
            modifier = Modifier.weight(1f)
        ) { onSelect(RepositoryWorkView.NESTED) }
    }
}

@Composable
private fun ViewButton(
    selected: Boolean,
    label: String,
    icon: @Composable () -> Unit,
    tag: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val m = modifier.heightIn(min = 48.dp).testTag(tag)
    if (selected) Button(onClick = onClick, modifier = m) {
        icon(); Spacer(Modifier.width(6.dp)); Text(label)
    } else OutlinedButton(onClick = onClick, modifier = m) {
        icon(); Spacer(Modifier.width(6.dp)); Text(label)
    }
}

@Composable
private fun KanbanBoard(issues: List<RepoIssue>, onUpdate: (String, IssueStatus) -> Unit) {
    if (issues.isEmpty()) return EmptyState("#", "尚無任務", "在此儲存庫建立任務後，會自動顯示於工作看板。")
    LazyRow(
        Modifier.fillMaxSize().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.width(4.dp)) }
        items(IssueStatus.entries, key = { it.name }) { status ->
            val columnIssues = issues.filter { it.status == status }.sortedWith(
                compareByDescending<RepoIssue> { it.priority == IssuePriority.CRITICAL }
                    .thenByDescending { it.priority == IssuePriority.HIGH }
                    .thenBy { it.issueNumber }
            )
            KanbanColumn(status, columnIssues, issues, onUpdate)
        }
        item { Spacer(Modifier.width(4.dp)) }
    }
}

@Composable
private fun KanbanColumn(
    status: IssueStatus,
    columnIssues: List<RepoIssue>,
    allIssues: List<RepoIssue>,
    onUpdate: (String, IssueStatus) -> Unit
) {
    Surface(
        Modifier.width(300.dp).fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        color = SophisticatedSurface,
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(boardLabel(status), fontWeight = FontWeight.Bold, color = TextHighEmphasis)
                    Text(status.label, fontSize = 11.sp, color = TextLowEmphasis)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = SophisticatedContainer) {
                    Text(columnIssues.size.toString(), Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = LavenderGlow, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(columnIssues, key = { it.id }) { issue ->
                    TaskCard(issue, nestedTaskProgress(issue.id, allIssues), onUpdate)
                }
            }
        }
    }
}

@Composable
private fun NestedTasks(issues: List<RepoIssue>, onUpdate: (String, IssueStatus) -> Unit) {
    val rows = remember(issues) { flattenNestedTasks(issues) }
    if (rows.isEmpty()) return EmptyState("↳", "尚無巢狀任務", "請在任務中建立子任務以形成階層。")
    LazyColumn(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rows, key = { it.issue.id }) { row ->
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.width((row.depth.coerceAtMost(4) * 16).dp))
                Box(Modifier.weight(1f)) {
                    TaskCard(row.issue, nestedTaskProgress(row.issue.id, issues), onUpdate, nested = row.depth > 0)
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    issue: RepoIssue,
    progress: NestedTaskProgress,
    onUpdate: (String, IssueStatus) -> Unit,
    nested: Boolean = false
) {
    Card(
        Modifier.fillMaxWidth().testTag(if (nested) "nested_task_${issue.id}" else "kanban_task_${issue.id}"),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(
                    "${if (nested) "↳ " else ""}#${issue.issueNumber} ${issue.title}",
                    Modifier.weight(1f),
                    color = TextHighEmphasis,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp)); Priority(issue.priority)
            }
            issue.parentIssueTitle?.let { Text("上層任務：$it", color = LavenderGlow, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            if (progress.total > 0) Text(
                "巢狀任務：${progress.closed}/${progress.total} 已完成",
                color = if (progress.closed == progress.total) EmeraldSuccess else TextMediumEmphasis,
                fontSize = 11.sp
            )
            Text("${issue.assigneeName ?: "未指派"} • ${issue.labels.ifBlank { "無標籤" }}", color = TextMediumEmphasis, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            MoveActions(issue, onUpdate)
        }
    }
}

@Composable
private fun MoveActions(issue: RepoIssue, onUpdate: (String, IssueStatus) -> Unit) {
    val back = previousStatus(issue.status)
    val forward = nextStatus(issue.status)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (back != null) OutlinedButton(
            onClick = { onUpdate(issue.id, back) },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("task_${issue.id}_move_back")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(boardLabel(back), fontSize = 11.sp)
        }
        if (forward != null) Button(
            onClick = { onUpdate(issue.id, forward) },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("task_${issue.id}_move_forward")
        ) {
            Text(boardLabel(forward), fontSize = 11.sp); Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
        }
    }
}

@Composable
private fun Priority(priority: IssuePriority) {
    val color = when (priority) {
        IssuePriority.CRITICAL -> RoseError
        IssuePriority.HIGH -> AmberWarning
        IssuePriority.MEDIUM -> LavenderGlow
        IssuePriority.LOW -> TextMediumEmphasis
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.12f)) {
        Text(priority.label, Modifier.padding(horizontal = 7.dp, vertical = 3.dp), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState(icon: String, title: String, message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(icon, color = LavenderPrimary, style = MaterialTheme.typography.headlineMedium)
            Text(title, color = TextHighEmphasis, fontWeight = FontWeight.Bold)
            Text(message, color = TextMediumEmphasis, fontSize = 12.sp)
        }
    }
}
