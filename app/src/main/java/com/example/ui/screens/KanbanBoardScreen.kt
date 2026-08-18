package com.example.ui.screens

// Temporary compatibility anchor for the one-time bootstrap workflow.
// private fun androidx.compose.foundation.layout.ColumnScope.EmptyKanban(title: String, message: String) {

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.WbsProjectionRow
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
import com.example.ui.viewmodel.GovernanceViewModel

private enum class WorkProjectionMode(val label: String) {
    KANBAN("看板"),
    WBS("WBS")
}

/**
 * Global mobile work entry point over Repository-owned Issues.
 * Kanban and WBS are two projections of the same records; neither persists work data.
 */
@Composable
fun KanbanBoardScreen(
    repositories: List<Repository>,
    allIssues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit,
    onOpenRepository: (Repository) -> Unit,
    modifier: Modifier = Modifier
) {
    val governanceViewModel: GovernanceViewModel = viewModel()
    val activeUser by governanceViewModel.activeUser.collectAsState()
    val teamMemberships by governanceViewModel.allTeamMemberships.collectAsState()

    var selectedRepoId by remember { mutableStateOf<String?>(null) }
    var assignmentScope by remember { mutableStateOf(WorkAssignmentScope.MINE) }
    var projectionMode by remember { mutableStateOf(WorkProjectionMode.KANBAN) }

    LaunchedEffect(repositories) {
        if (selectedRepoId != null && repositories.none { it.id == selectedRepoId }) {
            selectedRepoId = null
            projectionMode = WorkProjectionMode.KANBAN
        }
    }

    val selectedRepo = repositories.firstOrNull { it.id == selectedRepoId }
    val assignmentScopedIssues = remember(allIssues, activeUser?.id, teamMemberships, assignmentScope) {
        projectWorkIssues(
            allIssues = allIssues,
            activeUserId = activeUser?.id,
            teamMemberships = teamMemberships,
            assignmentScope = assignmentScope,
            repositoryId = null
        )
    }
    val issues = remember(assignmentScopedIssues, selectedRepoId) {
        if (selectedRepoId == null) assignmentScopedIssues
        else assignmentScopedIssues.filter { it.repoId == selectedRepoId }
    }
    val ordered = remember(issues) { IssueHierarchyRules.orderedForDisplay(issues) }
    val wbsRows = remember(issues, selectedRepoId) {
        if (selectedRepoId == null) emptyList() else IssueHierarchyRules.wbsProjection(issues)
    }
    val overallProgress = remember(issues) { IssueHierarchyRules.overallProgress(issues) }
    val repositoryById = remember(repositories) { repositories.associateBy { it.id } }
    val visibleRepositoryCount = remember(issues) { issues.map { it.repoId }.toSet().size }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(horizontal = 14.dp)
            .testTag("kanban_board_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        WorkHeader(
            projectionMode = projectionMode,
            assignmentScope = assignmentScope,
            selectedRepo = selectedRepo,
            issues = issues,
            visibleRepositoryCount = visibleRepositoryCount,
            overallProgress = overallProgress,
            onOpenRepository = onOpenRepository
        )

        if (repositories.isEmpty()) {
            EmptyWorkState("尚無儲存庫", "建立儲存庫後即可在此管理工作。")
            return@Column
        }

        AssignmentScopeSelector(
            selectedScope = assignmentScope,
            onSelectScope = { assignmentScope = it }
        )

        RepositorySelector(
            repositories = repositories,
            issues = assignmentScopedIssues,
            selectedRepoId = selectedRepoId,
            onSelectAll = {
                selectedRepoId = null
                projectionMode = WorkProjectionMode.KANBAN
            },
            onSelectRepository = { selectedRepoId = it.id }
        )

        ProjectionModeSelector(
            selectedMode = projectionMode,
            wbsEnabled = selectedRepo != null,
            onSelectMode = { projectionMode = it }
        )

        when {
            assignmentScope == WorkAssignmentScope.MINE && activeUser == null -> {
                EmptyWorkState("尚未選擇使用者", "切換至有效使用者，或改看目前範圍內的全部工作。")
            }

            issues.isEmpty() -> {
                val title = when {
                    assignmentScope == WorkAssignmentScope.MINE && selectedRepo == null -> "目前沒有指派工作"
                    selectedRepo == null -> "目前沒有可顯示的工作"
                    else -> "此儲存庫目前沒有符合條件的任務"
                }
                val message = when {
                    assignmentScope == WorkAssignmentScope.MINE -> "直接指派與團隊指派的任務會集中顯示在這裡。"
                    selectedRepo == null -> "目前範圍內尚無 Repository Issue。"
                    else -> "調整工作範圍，或在儲存庫內新增任務。"
                }
                EmptyWorkState(title, message)
            }

            projectionMode == WorkProjectionMode.KANBAN -> {
                KanbanProjection(
                    ordered = ordered,
                    issues = issues,
                    repositoryById = repositoryById,
                    showRepository = selectedRepo == null,
                    onUpdateIssueStatus = onUpdateIssueStatus,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }

            selectedRepo != null -> {
                WbsProjection(
                    rows = wbsRows,
                    overallProgress = overallProgress,
                    onUpdateIssueStatus = onUpdateIssueStatus,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }

            else -> {
                EmptyWorkState("請先選擇儲存庫", "WBS 是單一 Repository Issue 樹的投影。")
            }
        }
    }
}

@Composable
private fun WorkHeader(
    projectionMode: WorkProjectionMode,
    assignmentScope: WorkAssignmentScope,
    selectedRepo: Repository?,
    issues: List<RepoIssue>,
    visibleRepositoryCount: Int,
    overallProgress: Float,
    onOpenRepository: (Repository) -> Unit
) {
    val percentage = (overallProgress * 100).toInt()
    val repositoryCount = if (selectedRepo != null) 1 else visibleRepositoryCount
    val scopeLabel = selectedRepo?.displayName ?: "全部可存取儲存庫"
    val description = when {
        projectionMode == WorkProjectionMode.WBS -> {
            "WBS 顯示單一儲存庫的上下層分解與完成率；資料仍是 Repository Issue。"
        }
        selectedRepo == null && assignmentScope == WorkAssignmentScope.MINE -> {
            "聚合直接指派與團隊指派；資料仍是 Repository Issue。"
        }
        selectedRepo == null -> {
            "顯示目前範圍內所有可存取任務；資料仍是 Repository Issue。"
        }
        else -> {
            "看板依狀態投影已篩選任務；資料仍是 Repository Issue。"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.size(40.dp).background(LavenderContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (projectionMode == WorkProjectionMode.WBS) Icons.Default.AccountTree else Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = LavenderPrimary
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        assignmentScope.label,
                        color = TextHighEmphasis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        color = TextMediumEmphasis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (selectedRepo != null) {
                    OutlinedButton(
                        onClick = { onOpenRepository(selectedRepo) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("kanban_open_repository")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("儲存庫")
                    }
                }
            }
            Text(
                "$scopeLabel · ${issues.size} 個任務 · $repositoryCount 個儲存庫 · $percentage%",
                color = LavenderGlow,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun AssignmentScopeSelector(
    selectedScope: WorkAssignmentScope,
    onSelectScope: (WorkAssignmentScope) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SophisticatedSurfaceDark, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        WorkAssignmentScope.entries.forEach { scope ->
            val selected = scope == selectedScope
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clickable { onSelectScope(scope) }
                    .testTag("work_scope_${scope.name.lowercase()}"),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) LavenderPrimary else SophisticatedSurfaceDark
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (scope == WorkAssignmentScope.MINE) Icons.Default.Person else Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = if (selected) LavenderOnPrimary else TextMediumEmphasis,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        scope.label,
                        color = if (selected) LavenderOnPrimary else TextHighEmphasis,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RepositorySelector(
    repositories: List<Repository>,
    issues: List<RepoIssue>,
    selectedRepoId: String?,
    onSelectAll: () -> Unit,
    onSelectRepository: (Repository) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
        item(key = "all_repositories") {
            val selected = selectedRepoId == null
            Surface(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onSelectAll)
                    .testTag("kanban_repo_all"),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) LavenderPrimary else SophisticatedSurface,
                border = BorderStroke(1.dp, if (selected) LavenderPrimary else SophisticatedBorder)
            ) {
                Column(Modifier.padding(horizontal = 13.dp, vertical = 8.dp)) {
                    Text(
                        "全部儲存庫",
                        color = if (selected) LavenderOnPrimary else TextHighEmphasis,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        "${issues.size} 個任務",
                        color = if (selected) LavenderOnPrimary else TextMediumEmphasis,
                        fontSize = 10.sp
                    )
                }
            }
        }

        items(repositories, key = { it.id }) { repo ->
            val selected = repo.id == selectedRepoId
            Surface(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable { onSelectRepository(repo) }
                    .testTag("kanban_repo_${repo.id}"),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) LavenderPrimary else SophisticatedSurface,
                border = BorderStroke(1.dp, if (selected) LavenderPrimary else SophisticatedBorder)
            ) {
                Column(Modifier.padding(horizontal = 13.dp, vertical = 8.dp)) {
                    Text(
                        repo.displayName,
                        color = if (selected) LavenderOnPrimary else TextHighEmphasis,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        "${issues.count { it.repoId == repo.id }} 個任務",
                        color = if (selected) LavenderOnPrimary else TextMediumEmphasis,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectionModeSelector(
    selectedMode: WorkProjectionMode,
    wbsEnabled: Boolean,
    onSelectMode: (WorkProjectionMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SophisticatedSurfaceDark, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        WorkProjectionMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            val enabled = mode != WorkProjectionMode.WBS || wbsEnabled
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clickable(enabled = enabled) { onSelectMode(mode) }
                    .testTag("work_projection_${mode.name.lowercase()}"),
                shape = RoundedCornerShape(10.dp),
                color = if (selected) LavenderPrimary else SophisticatedSurfaceDark
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (mode == WorkProjectionMode.WBS) Icons.Default.AccountTree else Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = when {
                            selected -> LavenderOnPrimary
                            enabled -> TextMediumEmphasis
                            else -> TextLowEmphasis
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (mode == WorkProjectionMode.WBS && !enabled) "WBS（先選儲存庫）" else mode.label,
                        color = when {
                            selected -> LavenderOnPrimary
                            enabled -> TextHighEmphasis
                            else -> TextLowEmphasis
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanProjection(
    ordered: List<Pair<RepoIssue, Int>>,
    issues: List<RepoIssue>,
    repositoryById: Map<String, Repository>,
    showRepository: Boolean,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 18.dp, end = 8.dp)
    ) {
        items(IssueStatus.entries, key = { it.name }) { status ->
            KanbanColumn(
                status = status,
                tasks = ordered.filter { (issue, _) -> issue.status == status },
                allIssues = issues,
                repositoryById = repositoryById,
                showRepository = showRepository,
                onUpdateIssueStatus = onUpdateIssueStatus
            )
        }
    }
}

@Composable
private fun WbsProjection(
    rows: List<WbsProjectionRow>,
    overallProgress: Float,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("wbs_summary"),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("WBS 工作樹", color = TextHighEmphasis, fontWeight = FontWeight.Bold)
                        Text("父層進度由自身與全部子孫任務等權推導", color = TextMediumEmphasis, fontSize = 11.sp)
                    }
                    Text(
                        "${(overallProgress * 100).toInt()}%",
                        color = if (overallProgress == 1f) EmeraldSuccess else LavenderGlow,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (overallProgress == 1f) EmeraldSuccess else LavenderPrimary,
                    trackColor = SophisticatedContainer
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            items(rows, key = { it.issue.id }) { row ->
                WbsTaskCard(row = row, onUpdateIssueStatus = onUpdateIssueStatus)
            }
        }
    }
}

@Composable
private fun WbsTaskCard(
    row: WbsProjectionRow,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    val issue = row.issue
    val accent = priorityColor(issue.priority)
    val indent = (row.depth.coerceAtMost(4) * 14).dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .testTag("wbs_task_${issue.id}"),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(shape = RoundedCornerShape(7.dp), color = LavenderContainer) {
                    Text(
                        row.code,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = LavenderGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        issue.title,
                        color = TextHighEmphasis,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "#${issue.issueNumber} · ${issue.assigneeName ?: "未指派"}",
                        color = TextMediumEmphasis,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(shape = RoundedCornerShape(7.dp), color = accent.copy(alpha = 0.12f)) {
                    Text(
                        issue.status.label,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        color = accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${row.completedCount}/${row.totalCount} 完成",
                    color = TextMediumEmphasis,
                    fontSize = 11.sp
                )
                Text(
                    "${(row.progress * 100).toInt()}%",
                    color = if (row.progress == 1f) EmeraldSuccess else LavenderGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { row.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (row.progress == 1f) EmeraldSuccess else LavenderPrimary,
                trackColor = SophisticatedContainer
            )
            KanbanMoveActions(issue, onUpdateIssueStatus)
        }
    }
}

@Composable
private fun KanbanColumn(
    status: IssueStatus,
    tasks: List<Pair<RepoIssue, Int>>,
    allIssues: List<RepoIssue>,
    repositoryById: Map<String, Repository>,
    showRepository: Boolean,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    val title = status.label
    Surface(
        modifier = Modifier.width(300.dp).fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        color = SophisticatedSurface,
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = TextHighEmphasis, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(10.dp), color = SophisticatedContainer) {
                    Text(tasks.size.toString(), Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = LavenderGlow, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(tasks, key = { it.first.id }) { (issue, depth) ->
                    KanbanTaskCard(
                        issue = issue,
                        depth = depth,
                        allIssues = allIssues,
                        repositoryName = if (showRepository) repositoryById[issue.repoId]?.displayName else null,
                        onUpdate = onUpdateIssueStatus
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanTaskCard(
    issue: RepoIssue,
    depth: Int,
    allIssues: List<RepoIssue>,
    repositoryName: String?,
    onUpdate: (String, IssueStatus) -> Unit
) {
    val descendants = remember(issue.id, allIssues) { IssueHierarchyRules.descendantIds(issue.id, allIssues) }
    val nested = allIssues.filter { it.id in descendants }
    val nestedClosed = nested.count { it.status == IssueStatus.CLOSED }
    val accent = priorityColor(issue.priority)
    Card(
        modifier = Modifier.fillMaxWidth().testTag("kanban_task_${issue.id}"),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text("#${issue.issueNumber} ${issue.title}", Modifier.weight(1f), color = TextHighEmphasis, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = 0.12f)) {
                    Text(issue.priority.label, Modifier.padding(horizontal = 7.dp, vertical = 3.dp), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            repositoryName?.let { name ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                    Text(name, color = LavenderGlow, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (depth > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(15.dp))
                    Text("第 ${depth + 1} 層巢狀任務", color = LavenderGlow, fontSize = 11.sp)
                }
            }
            issue.parentIssueTitle?.let {
                Text("上層任務：$it", color = TextMediumEmphasis, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (nested.isNotEmpty()) {
                Text(
                    "子孫任務：$nestedClosed/${nested.size} 已完成",
                    color = if (nestedClosed == nested.size) EmeraldSuccess else TextMediumEmphasis,
                    fontSize = 11.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextLowEmphasis, modifier = Modifier.size(14.dp))
                Text(issue.assigneeName ?: "未指派", color = TextMediumEmphasis, fontSize = 11.sp)
            }
            KanbanMoveActions(issue, onUpdate)
        }
    }
}

@Composable
private fun KanbanMoveActions(issue: RepoIssue, onUpdate: (String, IssueStatus) -> Unit) {
    val back = when (issue.status) {
        IssueStatus.OPEN -> null
        IssueStatus.IN_PROGRESS -> IssueStatus.OPEN
        IssueStatus.CLOSED -> IssueStatus.IN_PROGRESS
    }
    val forward = when (issue.status) {
        IssueStatus.OPEN -> IssueStatus.IN_PROGRESS
        IssueStatus.IN_PROGRESS -> IssueStatus.CLOSED
        IssueStatus.CLOSED -> null
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (back != null) {
            OutlinedButton(
                onClick = { onUpdate(issue.id, back) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("work_${issue.id}_back")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(back.label, fontSize = 11.sp)
            }
        }
        if (forward != null) {
            Button(
                onClick = { onUpdate(issue.id, forward) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("work_${issue.id}_forward")
            ) {
                Text(forward.label, fontSize = 11.sp)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun priorityColor(priority: IssuePriority) = when (priority) {
    IssuePriority.CRITICAL -> RoseError
    IssuePriority.HIGH -> AmberWarning
    IssuePriority.MEDIUM -> LavenderGlow
    IssuePriority.LOW -> TextMediumEmphasis
}

@Composable
private fun ColumnScope.EmptyWorkState(title: String, message: String) {
    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Dashboard, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(46.dp))
            Text(title, color = TextHighEmphasis, fontWeight = FontWeight.Bold)
            Text(message, color = TextMediumEmphasis, style = MaterialTheme.typography.bodySmall)
        }
    }
}
