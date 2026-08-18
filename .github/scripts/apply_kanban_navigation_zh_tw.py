from pathlib import Path
import re

ROOT = Path('.')


def require_replace(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise RuntimeError(f'missing required pattern: {label}')
    return text.replace(old, new, 1)


def replace_all_literal_strings(text: str, mapping: dict[str, str]) -> str:
    for src, dst in sorted(mapping.items(), key=lambda kv: len(kv[0]), reverse=True):
        text = text.replace(f'"{src}"', f'"{dst}"')
    return text


# -----------------------------------------------------------------------------
# 1. Canonical recursive Issue hierarchy. Nested Tasks remain RepoIssue relations.
# -----------------------------------------------------------------------------
hierarchy_file = ROOT / 'app/src/main/java/com/example/data/model/IssueHierarchyRules.kt'
hierarchy_file.write_text(r'''package com.example.data.model

/**
 * Pure recursive hierarchy rules for RepoIssue.
 * Nested tasks are relationships between Issues, never a second Task entity.
 */
object IssueHierarchyRules {
    fun descendantIds(rootIssueId: String, issues: List<RepoIssue>): Set<String> {
        val childrenByParent = issues.groupBy { it.parentIssueId }
        val descendants = linkedSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.addLast(rootIssueId)
        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            childrenByParent[currentId].orEmpty().forEach { child ->
                if (descendants.add(child.id)) queue.addLast(child.id)
            }
        }
        return descendants
    }

    fun canAssignParent(issueId: String, candidateParentId: String?, issues: List<RepoIssue>): Boolean {
        if (candidateParentId == null) return true
        if (candidateParentId == issueId) return false
        val issue = issues.firstOrNull { it.id == issueId } ?: return false
        val candidate = issues.firstOrNull { it.id == candidateParentId } ?: return false
        if (issue.repoId != candidate.repoId) return false
        return candidateParentId !in descendantIds(issueId, issues)
    }

    fun depthOf(issueId: String, issues: List<RepoIssue>): Int {
        val byId = issues.associateBy { it.id }
        val visited = mutableSetOf<String>()
        var depth = 0
        var current = byId[issueId]
        while (current?.parentIssueId != null) {
            val parentId = current.parentIssueId ?: break
            if (!visited.add(parentId)) break
            current = byId[parentId] ?: break
            depth += 1
        }
        return depth
    }

    fun orderedForDisplay(issues: List<RepoIssue>): List<Pair<RepoIssue, Int>> {
        if (issues.isEmpty()) return emptyList()
        val byId = issues.associateBy { it.id }
        val childrenByParent = issues.groupBy { it.parentIssueId }
            .mapValues { (_, children) -> children.sortedBy { it.issueNumber } }
        val result = mutableListOf<Pair<RepoIssue, Int>>()
        val visited = mutableSetOf<String>()

        fun visit(issue: RepoIssue, depth: Int) {
            if (!visited.add(issue.id)) return
            result += issue to depth
            childrenByParent[issue.id].orEmpty().forEach { child -> visit(child, depth + 1) }
        }

        issues.filter { it.parentIssueId == null || it.parentIssueId !in byId }
            .sortedBy { it.issueNumber }
            .forEach { visit(it, 0) }

        // Keep malformed/cyclic records visible once instead of recursing forever.
        issues.sortedBy { it.issueNumber }.forEach { issue ->
            if (issue.id !in visited) visit(issue, depthOf(issue.id, issues))
        }
        return result
    }
}
''', encoding='utf-8')


# -----------------------------------------------------------------------------
# 2. Persistence guard: arbitrary nesting is allowed; transitive cycles are not.
# -----------------------------------------------------------------------------
repo_path = ROOT / 'app/src/main/java/com/example/data/repository/GovernanceRepository.kt'
repo_text = repo_path.read_text(encoding='utf-8')
repo_text = require_replace(
    repo_text,
    'import com.example.data.model.IssueDependency\n',
    'import com.example.data.model.IssueDependency\nimport com.example.data.model.IssueHierarchyRules\n',
    'repository hierarchy import'
)
repo_text = require_replace(
    repo_text,
'''        // Prevent direct circular hierarchy (parent cannot have issue as its parent)\n        if (parent.parentIssueId == issueId) {\n            return Pair(false, "Circular hierarchy detected: Issue #${parent.issueNumber} is already a child of Issue #${issue.issueNumber}.")\n        }\n''',
'''        // Nested tasks may have arbitrary depth, but the hierarchy must stay acyclic.\n        val repoIssues = dao.getIssuesByRepoOnce(issue.repoId)\n        if (!IssueHierarchyRules.canAssignParent(issueId, parentIssueId, repoIssues)) {\n            return Pair(false, "無法將任務設為自己或其任一子孫任務的下層，避免形成循環階層。")\n        }\n''',
    'transitive cycle guard'
)
repo_path.write_text(repo_text, encoding='utf-8')


# -----------------------------------------------------------------------------
# 3. Issues UI: use the recursive hierarchy for list indentation and parent choices.
# -----------------------------------------------------------------------------
issues_path = ROOT / 'app/src/main/java/com/example/ui/components/RepoIssuesSection.kt'
issues_text = issues_path.read_text(encoding='utf-8')
issues_text = require_replace(
    issues_text,
    'import com.example.data.model.IssueDependency\n',
    'import com.example.data.model.IssueDependency\nimport com.example.data.model.IssueHierarchyRules\n',
    'ui hierarchy import'
)

pattern = re.compile(r'''        \} else \{\n            LazyColumn\(\n                modifier = Modifier\n                    \.fillMaxWidth\(\)\n                    \.weight\(1f\),\n                verticalArrangement = Arrangement\.spacedBy\(10\.dp\),\n                contentPadding = PaddingValues\(bottom = 24\.dp\)\n            \) \{\n                items\(filteredIssues, key = \{ it\.id \}\) \{ issue ->\n                    val subIssues = issues\.filter \{ it\.parentIssueId == issue\.id \}\n                    val isBlocked = issue\.id in blockedIssueIds\n                    val blockedByList = dependencies\.filter \{ it\.blockedIssueId == issue\.id \}\n                        \.mapNotNull \{ dep -> issues\.firstOrNull \{ it\.id == dep\.blockingIssueId \} \}\n                    val blockingList = dependencies\.filter \{ it\.blockingIssueId == issue\.id \}\n                        \.mapNotNull \{ dep -> issues\.firstOrNull \{ it\.id == dep\.blockedIssueId \} \}\n\n                    HierarchicalIssueCard\(\n                        issue = issue,\n                        subIssues = subIssues,\n                        isBlocked = isBlocked,\n                        blockedByIssues = blockedByList,\n                        blockingIssues = blockingList,\n                        onClick = \{ viewingIssue = issue \},\n                        onAddSubIssue = \{\n                            preselectedParentForCreate = issue\n                            showCreateDialog = true\n                        \}\n                    \)\n                \}\n            \}\n        \}''', re.S)
replacement = '''        } else {\n            val visibleIssueIds = filteredIssues.map { it.id }.toSet()\n            val orderedFilteredIssues = remember(issues, filteredIssues) {\n                IssueHierarchyRules.orderedForDisplay(issues).filter { (issue, _) -> issue.id in visibleIssueIds }\n            }\n            LazyColumn(\n                modifier = Modifier.fillMaxWidth().weight(1f),\n                verticalArrangement = Arrangement.spacedBy(10.dp),\n                contentPadding = PaddingValues(bottom = 24.dp)\n            ) {\n                items(orderedFilteredIssues, key = { it.first.id }) { (issue, depth) ->\n                    val nestedIds = IssueHierarchyRules.descendantIds(issue.id, issues)\n                    val nestedTasks = issues.filter { it.id in nestedIds }\n                    val isBlocked = issue.id in blockedIssueIds\n                    val blockedByList = dependencies.filter { it.blockedIssueId == issue.id }\n                        .mapNotNull { dep -> issues.firstOrNull { it.id == dep.blockingIssueId } }\n                    val blockingList = dependencies.filter { it.blockingIssueId == issue.id }\n                        .mapNotNull { dep -> issues.firstOrNull { it.id == dep.blockedIssueId } }\n                    HierarchicalIssueCard(\n                        issue = issue, subIssues = nestedTasks, depth = depth, isBlocked = isBlocked,\n                        blockedByIssues = blockedByList, blockingIssues = blockingList,\n                        onClick = { viewingIssue = issue },\n                        onAddSubIssue = { preselectedParentForCreate = issue; showCreateDialog = true }\n                    )\n                }\n            }\n        }'''
issues_text, count = pattern.subn(replacement, issues_text, count=1)
if count != 1:
    raise RuntimeError(f'issue hierarchy list replacement count={count}')

issues_text = require_replace(
    issues_text,
    '        val currentIssueSubIssues = issues.filter { it.parentIssueId == currentViewingIssue.id }\n',
    '        val currentIssueNestedIds = IssueHierarchyRules.descendantIds(currentViewingIssue.id, issues)\n        val currentIssueSubIssues = IssueHierarchyRules.orderedForDisplay(issues).map { it.first }.filter { it.id in currentIssueNestedIds }\n',
    'nested detail descendants'
)
issues_text = require_replace(
    issues_text,
    'fun HierarchicalIssueCard(\n    issue: RepoIssue,\n    subIssues: List<RepoIssue>,\n    isBlocked: Boolean,',
    'fun HierarchicalIssueCard(\n    issue: RepoIssue,\n    subIssues: List<RepoIssue>,\n    depth: Int = 0,\n    isBlocked: Boolean,',
    'card depth param'
)
issues_text = require_replace(
    issues_text,
    '        modifier = Modifier\n            .fillMaxWidth()\n            .clickable(onClick = onClick)\n            .testTag("issue_card_${issue.issueNumber}"),',
    '        modifier = Modifier\n            .fillMaxWidth()\n            .padding(start = (depth.coerceAtMost(5) * 10).dp)\n            .clickable(onClick = onClick)\n            .testTag("issue_card_${issue.issueNumber}"),',
    'card depth indent'
)
issues_text = require_replace(
    issues_text,
'''    // Eligible issues that can be chosen as parent (must not be itself or existing child)\n    val eligibleParents = remember(allRepoIssues, issue, subIssues) {\n        val childIds = subIssues.map { it.id }.toSet()\n        allRepoIssues.filter { it.id != issue.id && it.id !in childIds && it.parentIssueId == null }\n    }''',
'''    val eligibleParents = remember(allRepoIssues, issue) {\n        IssueHierarchyRules.orderedForDisplay(allRepoIssues).map { it.first }.filter { candidate ->\n            IssueHierarchyRules.canAssignParent(issue.id, candidate.id, allRepoIssues)\n        }\n    }''',
    'eligible recursive parents'
)
issues_text = require_replace(
    issues_text,
'''                        allRepoIssues.filter { it.parentIssueId == null }.forEach { parent ->\n                            DropdownMenuItem(\n                                leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },\n                                text = { Text("#${parent.issueNumber} ${parent.title}", color = TextHighEmphasis) },''',
'''                        IssueHierarchyRules.orderedForDisplay(allRepoIssues).forEach { (parent, depth) ->\n                            DropdownMenuItem(\n                                leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },\n                                text = { Text("${"· ".repeat(depth)}#${parent.issueNumber} ${parent.title}", color = TextHighEmphasis) },''',
    'recursive parent dropdown'
)
issues_path.write_text(issues_text, encoding='utf-8')


# -----------------------------------------------------------------------------
# 4. Independent bottom-nav Kanban entry; data ownership remains Repository-scoped.
# -----------------------------------------------------------------------------
kanban_screen = ROOT / 'app/src/main/java/com/example/ui/screens/KanbanBoardScreen.kt'
kanban_screen.write_text(r'''package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldDark
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

/**
 * Global entry point, Repository-owned data.
 * The board never persists a second copy of tasks; it only projects RepoIssue by status.
 */
@Composable
fun KanbanBoardScreen(
    repositories: List<Repository>,
    allIssues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit,
    onOpenRepository: (Repository) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRepoId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repositories, allIssues) {
        if (selectedRepoId == null || repositories.none { it.id == selectedRepoId }) {
            selectedRepoId = allIssues.firstOrNull()?.repoId ?: repositories.firstOrNull()?.id
        }
    }

    val selectedRepo = repositories.firstOrNull { it.id == selectedRepoId }
    val issues = allIssues.filter { it.repoId == selectedRepoId }
    val ordered = remember(issues) { IssueHierarchyRules.orderedForDisplay(issues) }

    Column(
        modifier = modifier.fillMaxSize().background(SophisticatedBg).padding(horizontal = 14.dp).testTag("kanban_board_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).background(LavenderContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, tint = LavenderPrimary)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("工作看板", color = TextHighEmphasis, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("看板只是任務狀態視圖；任務資料仍隸屬各自的儲存庫。", color = TextMediumEmphasis, style = MaterialTheme.typography.bodySmall)
                    }
                    if (selectedRepo != null) {
                        OutlinedButton(
                            onClick = { onOpenRepository(selectedRepo) },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("kanban_open_repository")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("開啟儲存庫")
                        }
                    }
                }
                if (selectedRepo != null) {
                    Text(
                        "${selectedRepo.displayName} · ${issues.size} 個任務 · ${issues.count { it.parentIssueId != null }} 個巢狀任務",
                        color = LavenderGlow,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (repositories.isEmpty()) {
            EmptyKanban("尚無儲存庫", "建立儲存庫後即可在此管理工作看板。")
            return@Column
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(repositories, key = { it.id }) { repo ->
                val selected = repo.id == selectedRepoId
                Surface(
                    modifier = Modifier.heightIn(min = 48.dp).clickable { selectedRepoId = repo.id }.testTag("kanban_repo_${repo.id}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) LavenderPrimary else SophisticatedSurface,
                    border = BorderStroke(1.dp, if (selected) LavenderPrimary else SophisticatedBorder)
                ) {
                    Column(Modifier.padding(horizontal = 13.dp, vertical = 8.dp)) {
                        Text(repo.displayName, color = if (selected) LavenderOnPrimary else TextHighEmphasis, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(
                            "${allIssues.count { it.repoId == repo.id }} 個任務",
                            color = if (selected) LavenderOnPrimary else TextMediumEmphasis,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        if (selectedRepo == null || issues.isEmpty()) {
            EmptyKanban("此儲存庫尚無任務", "新增任務後會自動依狀態出現在看板中。")
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 18.dp, end = 8.dp)
            ) {
                items(IssueStatus.entries, key = { it.name }) { status ->
                    KanbanColumn(
                        status = status,
                        tasks = ordered.filter { (issue, _) -> issue.status == status },
                        allIssues = issues,
                        onUpdateIssueStatus = onUpdateIssueStatus
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanColumn(
    status: IssueStatus,
    tasks: List<Pair<RepoIssue, Int>>,
    allIssues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit
) {
    val title = when (status) {
        IssueStatus.OPEN -> "待處理"
        IssueStatus.IN_PROGRESS -> "進行中"
        IssueStatus.CLOSED -> "已完成"
    }
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
                    KanbanTaskCard(issue, depth, allIssues, onUpdateIssueStatus)
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
    onUpdate: (String, IssueStatus) -> Unit
) {
    val descendants = remember(issue.id, allIssues) { IssueHierarchyRules.descendantIds(issue.id, allIssues) }
    val nested = allIssues.filter { it.id in descendants }
    val nestedClosed = nested.count { it.status == IssueStatus.CLOSED }
    val accent = when (issue.priority) {
        IssuePriority.CRITICAL -> RoseError
        IssuePriority.HIGH -> AmberWarning
        IssuePriority.MEDIUM -> LavenderGlow
        IssuePriority.LOW -> TextMediumEmphasis
    }
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
            if (depth > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(15.dp))
                    Text("第 ${depth + 1} 層巢狀任務", color = LavenderGlow, fontSize = 11.sp)
                }
            }
            issue.parentIssueTitle?.let { Text("上層任務：$it", color = TextMediumEmphasis, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            if (nested.isNotEmpty()) {
                Text("子孫任務：$nestedClosed/${nested.size} 已完成", color = if (nestedClosed == nested.size) EmeraldSuccess else TextMediumEmphasis, fontSize = 11.sp)
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
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("kanban_${issue.id}_back")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (back == IssueStatus.OPEN) "待處理" else "進行中", fontSize = 11.sp)
            }
        }
        if (forward != null) {
            Button(
                onClick = { onUpdate(issue.id, forward) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("kanban_${issue.id}_forward")
            ) {
                Text(if (forward == IssueStatus.IN_PROGRESS) "進行中" else "已完成", fontSize = 11.sp)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyKanban(title: String, message: String) {
    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Dashboard, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(46.dp))
            Text(title, color = TextHighEmphasis, fontWeight = FontWeight.Bold)
            Text(message, color = TextMediumEmphasis, style = MaterialTheme.typography.bodySmall)
        }
    }
}
''', encoding='utf-8')


# -----------------------------------------------------------------------------
# 5. Bottom navigation: Home → Inbox → Kanban → Repositories → Me.
# -----------------------------------------------------------------------------
main_path = ROOT / 'app/src/main/java/com/example/MainActivity.kt'
main_text = main_path.read_text(encoding='utf-8')
main_text = require_replace(
    main_text,
    'import com.example.ui.screens.InboxScreen\n',
    'import com.example.ui.screens.InboxScreen\nimport com.example.ui.screens.KanbanBoardScreen\n',
    'Kanban screen import'
)
main_text = require_replace(
    main_text,
'''enum class MainNavigationTab {\n    HOME,\n    REPOSITORIES,\n    INBOX,\n    ME\n}''',
'''enum class MainNavigationTab {\n    HOME,\n    INBOX,\n    KANBAN,\n    REPOSITORIES,\n    ME\n}''',
    'navigation enum'
)

bottom_pattern = re.compile(r'''        bottomBar = \{\n            if \(selectedRepo == null && selectedArtifact == null\) \{.*?\n            \}\n        \}\n    \) \{ innerPadding ->''', re.S)
bottom_replacement = '''        bottomBar = {\n            if (selectedRepo == null && selectedArtifact == null) {\n                Surface(\n                    color = SophisticatedSurfaceDark,\n                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder)\n                ) {\n                    NavigationBar(containerColor = SophisticatedSurfaceDark, tonalElevation = 0.dp) {\n                        NavigationBarItem(\n                            selected = currentTab == MainNavigationTab.HOME,\n                            onClick = { currentTab = MainNavigationTab.HOME },\n                            icon = { Icon(Icons.Default.Home, contentDescription = "首頁") },\n                            label = { Text("首頁", style = MaterialTheme.typography.labelSmall) },\n                            colors = NavigationBarItemDefaults.colors(\n                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,\n                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary\n                            ), modifier = Modifier.testTag("nav_tab_home")\n                        )\n                        NavigationBarItem(\n                            selected = currentTab == MainNavigationTab.INBOX,\n                            onClick = { currentTab = MainNavigationTab.INBOX },\n                            icon = {\n                                BadgedBox(badge = {\n                                    if (unreadNotificationCount > 0) Badge(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary) { Text("$unreadNotificationCount") }\n                                }) { Icon(Icons.Default.Notifications, contentDescription = "收件匣") }\n                            },\n                            label = { Text("收件匣", style = MaterialTheme.typography.labelSmall) },\n                            colors = NavigationBarItemDefaults.colors(\n                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,\n                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary\n                            ), modifier = Modifier.testTag("nav_tab_inbox")\n                        )\n                        NavigationBarItem(\n                            selected = currentTab == MainNavigationTab.KANBAN,\n                            onClick = { currentTab = MainNavigationTab.KANBAN },\n                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "工作看板") },\n                            label = { Text("看板", style = MaterialTheme.typography.labelSmall) },\n                            colors = NavigationBarItemDefaults.colors(\n                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,\n                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary\n                            ), modifier = Modifier.testTag("nav_tab_kanban")\n                        )\n                        NavigationBarItem(\n                            selected = currentTab == MainNavigationTab.REPOSITORIES,\n                            onClick = { currentTab = MainNavigationTab.REPOSITORIES },\n                            icon = { Icon(Icons.Default.Folder, contentDescription = "儲存庫") },\n                            label = { Text("儲存庫", style = MaterialTheme.typography.labelSmall) },\n                            colors = NavigationBarItemDefaults.colors(\n                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,\n                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary\n                            ), modifier = Modifier.testTag("nav_tab_repos")\n                        )\n                        NavigationBarItem(\n                            selected = currentTab == MainNavigationTab.ME,\n                            onClick = {\n                                if (inspectedProfileUser == null && activeUser != null) viewModel.selectProfileUser(activeUser)\n                                currentTab = MainNavigationTab.ME\n                            },\n                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "我的") },\n                            label = { Text("我的", style = MaterialTheme.typography.labelSmall) },\n                            colors = NavigationBarItemDefaults.colors(\n                                selectedIconColor = LavenderOnPrimary, selectedTextColor = LavenderPrimary,\n                                unselectedIconColor = TextMediumEmphasis, unselectedTextColor = TextMediumEmphasis, indicatorColor = LavenderPrimary\n                            ), modifier = Modifier.testTag("nav_tab_me")\n                        )\n                    }\n                }\n            }\n        }\n    ) { innerPadding ->'''
main_text, count = bottom_pattern.subn(bottom_replacement, main_text, count=1)
if count != 1:
    raise RuntimeError(f'bottom navigation replacement count={count}')

main_text = require_replace(
    main_text,
    '\n                        MainNavigationTab.REPOSITORIES -> {',
'''\n                        MainNavigationTab.KANBAN -> {\n                            KanbanBoardScreen(\n                                repositories = repositories,\n                                allIssues = allIssues,\n                                onUpdateIssueStatus = { issueId, status -> viewModel.updateIssueStatus(issueId, status) },\n                                onOpenRepository = { repo -> viewModel.selectRepository(repo) }\n                            )\n                        }\n\n                        MainNavigationTab.REPOSITORIES -> {''',
    'Kanban navigation destination'
)
main_path.write_text(main_text, encoding='utf-8')


# -----------------------------------------------------------------------------
# 6. zh-TW UI mapping. Keep technical acronyms (RBAC, ABAC, RFC, SSO) when useful.
# -----------------------------------------------------------------------------
zh = {
    'Access Governance':'存取治理','NO-CODE PLATFORM':'無程式碼協作平台','Home':'首頁','Repositories':'儲存庫','Inbox':'收件匣','Me':'我的','Unified Inbox':'統一收件匣','Switch Persona':'切換身分','Switch':'切換','Guest User':'訪客使用者','Collaborator':'協作者','Enterprise':'企業','Enterprise Admin':'企業管理員','Org Member':'組織成員',
    'Overview':'總覽','Issues':'任務','Discussions':'討論','Artifacts':'成果','General':'一般','Access & Members':'存取與成員','Policies':'政策','Audit':'稽核','Settings':'設定','Repository Settings':'儲存庫設定','Back to Workspace':'返回工作區','Repository Overview':'儲存庫總覽','Workspace Overview':'工作區總覽','Owner':'擁有者','Effective Role':'有效角色','Repository Access':'儲存庫存取','Repository Policies':'儲存庫政策','Audit Log':'稽核紀錄','No-Code Artifacts':'無程式碼成果','New Artifact':'新增成果','New Repository':'新增儲存庫','Create Repository':'建立儲存庫','Repository Name':'儲存庫名稱','Display Name':'顯示名稱','Description':'說明','Category':'分類','Create':'建立','Cancel':'取消','Close':'關閉','Save':'儲存','Delete':'刪除','Remove':'移除','Add':'新增','Search':'搜尋','Clear':'清除','View':'查看','Manage':'管理','None':'無','Unknown':'未知',
    'No-Code Collaboration Containers':'無程式碼協作容器','ENTERPRISE HIERARCHICAL GOVERNANCE':'企業階層治理','Total Repos':'儲存庫總數','Org-Owned':'組織擁有','User-Owned':'使用者擁有','All Workspaces':'所有工作區','Search No-Code Repositories, Owners, Blueprints...':'搜尋儲存庫、擁有者或藍圖…','No matching No-Code Repositories found.':'找不到符合條件的無程式碼儲存庫。',
    'Collaboration Issues':'協作任務','Track hierarchical sub-issues, blocked dependencies, and task assignments':'追蹤巢狀任務、阻擋相依與工作指派','New Issue':'新增任務','Filter issues by title, label, author, or assignee...':'依標題、標籤、作者或受派者篩選任務…','All':'全部','Blocked':'受阻','Epics / Parents':'上層任務','Open':'待處理','In Progress':'進行中','Closed':'已完成','No Issues in Repository':'此儲存庫尚無任務','No Matching Issues':'找不到符合條件的任務','Issues enable collaborative task decomposition, sub-issue progress tracking, and dependency blocking across Users & Teams.':'任務可拆解成多層子任務，並追蹤進度、相依關係與跨使用者／團隊指派。','Try clearing or adjusting your filter criteria.':'請清除或調整篩選條件。','Create First Issue':'建立第一個任務','Sub-issue of':'上層任務','Sub-issues':'子任務','Hide':'收合','View tree':'查看階層','Spec':'規格','CRITICAL':'緊急','HIGH':'高','MEDIUM':'中','LOW':'低','Hierarchy & Sub-issues':'任務階層與子任務','Set Parent':'設定上層','Unlink Parent (Make Root Issue)':'解除上層關聯（設為根任務）','No other root issues available':'沒有可用的其他上層任務','Add Sub-issue':'新增子任務','Dependencies & Blockers':'相依與阻擋','Add Blocker':'新增前置任務','No eligible issues to block this issue':'沒有可設為前置任務的項目','SELECT BLOCKING PREREQUISITE':'選擇前置阻擋任務','BLOCKED BY (Prerequisites):':'受以下前置任務阻擋：','Remove dependency':'移除相依','Leave a comment or governance note...':'留下留言或治理備註…','Reply':'回覆','No comments yet. Start the conversation below.':'尚無留言，請在下方開始討論。','New Sub-Issue':'新增子任務','New Governance Issue':'新增治理任務','Parent Issue (Hierarchy Breakdown)':'上層任務（階層拆解）','None (Top-Level Root Issue)':'無（最上層根任務）','Issue Title *':'任務標題 *','Description & Context':'說明與脈絡','Describe the task, security rule, or bug...':'描述任務、治理規則或問題…','Priority Level':'優先級','Assignee (User or Team)':'受派者（使用者或團隊）','Unassigned (Select User or Team)':'未指派（選擇使用者或團隊）','None / Unassigned':'無／未指派','TEAMS':'團隊','USERS':'使用者','Link to No-Code Blueprint (Optional)':'連結無程式碼藍圖（選填）','None (Select Artifact)':'無（選擇成果）','Labels (comma separated)':'標籤（以逗號分隔）','Create Issue':'建立任務','Linked No-Code Blueprint':'已連結無程式碼藍圖','No detailed description provided.':'尚未提供詳細說明。','Work Blocked':'工作受阻',
    'Community & Governance Discussions':'社群與治理討論','RFCs, Architecture Decision Records, Q&A, and policy debate':'提案、決策紀錄、問答與政策討論','New Discussion':'新增討論','Filter discussions by topic, RFC proposals, or author...':'依主題、提案或作者篩選討論…','All Categories':'所有分類','No Discussions Started':'尚未開始任何討論','No Matching Discussions':'找不到符合條件的討論','Start Discussion':'開始討論','Reply to Discussion':'回覆討論','Lock Discussion':'鎖定討論','Unlock Discussion':'解除鎖定','Accepted Answer':'已採納回答','Mark as Answer':'設為回答',
    'Centralized collaboration queue & actionable events':'集中管理協作待辦與可執行事件','Unread':'未讀','Action Required':'需處理','Archived':'已封存','CATEGORY FILTER':'分類篩選','All Caught Up!':'目前都處理完了','You have no active notifications in your Inbox.':'收件匣目前沒有進行中的通知。','You have read all pending notifications.':'所有待處理通知皆已讀取。','No pending review requests, approvals, or issue assignments.':'目前沒有待審查、待核准或任務指派。','No archived notifications stored.':'目前沒有已封存通知。','Mark All Read':'全部標為已讀','Archive':'封存','Inspect Context':'檢視脈絡',
    'Assigned Issues':'指派給我的任務','Review Requests':'審查請求','Approval Requests':'核准請求','Mentions & Unread':'提及與未讀','Activity Trail':'活動軌跡','Assigned':'已指派','Reviews':'審查','Approvals':'核准','Mentions':'提及','Work Requiring Attention':'需要處理的工作','Recent Repositories':'最近使用的儲存庫','View All':'查看全部','Recent Activity':'最近活動',
    'Draft':'草稿','In Review':'審查中','Pending Sign-Off':'待簽核','Approved':'已核准','Published & Locked':'已發布並鎖定','Published':'已發布','Submit for Review':'送出審查','Submit Review':'提交審查','Request Changes':'要求修改','Approve':'核准','Publish & Lock':'發布並鎖定','Review':'審查','Approval':'核准','Comments':'留言',
    'Organizations':'組織','Teams':'團隊','Members':'成員','Profile':'個人檔案','Organizations & Teams':'組織與團隊','Security':'安全性','Activity':'活動','Member':'成員','Maintainer':'維護者','Reviewer':'審查者','Approver':'核准者','Viewer':'檢視者','Admin':'管理員','Billing Manager':'帳務管理員','Organization':'組織','User':'使用者','Team':'團隊',
    'Policy Simulator':'政策模擬器','Allowed':'允許','Denied':'拒絕','Policy Trace':'政策追蹤','Reasoning':'判定理由','Role Source':'角色來源','Enterprise Checks':'企業層檢查','Repository Checks':'儲存庫層檢查','Governance & Policy':'治理與政策','General community & repository discussions':'一般社群與儲存庫討論','Formal blueprints, architecture, and schema proposals':'正式藍圖、架構與結構提案','Official updates from repository maintainers and owners':'由儲存庫維護者與擁有者發布的正式更新','Collaborative brainstorms for no-code workflows':'無程式碼工作流程的協作腦力激盪','Ask questions and get verified answers':'提問並取得已驗證的回答','Discussion on compliance gates, access roles, and audit rules':'討論守規關卡、存取角色與稽核規則',
    'Approvals & Sign-offs':'核准與簽核','Issue Assignments':'任務指派','Mentions & Replies':'提及與回覆','Access & Permissions':'存取與權限','Org & Team Memberships':'組織與團隊成員關係','Releases & Publications':'發布與公告','Governance & Policy Alerts':'治理與政策警示','Low':'低','Normal':'一般','High':'高','Urgent':'緊急',
    'Repository Kanban Board and Nested Tasks':'儲存庫工作看板與巢狀任務','Kanban Board':'工作看板','Nested Tasks':'巢狀任務','To do':'待處理','In progress':'進行中','Done':'已完成','No tasks yet':'尚無任務','Create an Issue in this Repository; it will appear here automatically.':'在此儲存庫建立任務後，會自動顯示於工作看板。','No nested tasks yet':'尚無巢狀任務','Create sub-issues in Issues to build the hierarchy.':'請在任務中建立子任務以形成階層。','Close work board':'關閉工作看板','Unassigned':'未指派','No labels':'無標籤','Parent':'上層任務','Nested':'巢狀','complete':'已完成',
    'Create No-Code Repo':'建立無程式碼儲存庫','Create No-Code Draft':'建立無程式碼草稿','View Artifact':'查看成果','Edit No-Code Draft':'編輯無程式碼草稿','Submit for Peer Review':'送出同儕審查','Submit Formal Review':'提交正式審查','Grant Approver Sign-Off':'核准人簽核','Manage Collaborators & Roles':'管理協作者與角色','Update Repository Policies':'更新儲存庫政策','Transfer Repository Ownership':'移轉儲存庫所有權','Delete Repository':'刪除儲存庫','Create Repository Issue':'建立儲存庫任務','Comment on Issue':'回覆任務','Assign Issue (User/Team)':'指派任務（使用者／團隊）','Close/Reopen Issue':'關閉／重開任務','Delete Issue':'刪除任務','Create Discussion Thread':'建立討論串','Reply to Discussion':'回覆討論','Lock/Unlock Discussion':'鎖定／解除鎖定討論','Mark Accepted Answer':'標記採納回答'
}

zh_templates = {
    '${dependencies.size} links':'${dependencies.size} 個相依連結','Blocked ($blockedCount)':'受阻 ($blockedCount)','Epics / Parents ($parentCount)':'上層任務 ($parentCount)','Open ($count)':'待處理 ($count)','In Progress ($count)':'進行中 ($count)','Closed ($count)':'已完成 ($count)','Sub-issue of #${issue.parentIssueNumber} ${issue.parentIssueTitle ?: ""}':'上層任務 #${issue.parentIssueNumber} ${issue.parentIssueTitle ?: ""}','Sub-issues: $closedSub of $totalSub completed':'子任務：$closedSub / $totalSub 已完成','Blocked ($blockedByCount)':'受阻 ($blockedByCount)','$closedSub of $totalSub Sub-tasks Resolved':'$closedSub / $totalSub 子任務已完成','Parent: #${issue.parentIssueNumber}':'上層：#${issue.parentIssueNumber}','Repository: ${repo.name}':'儲存庫：${repo.name}','$unreadCount new':'$unreadCount 則新通知',
    '${issues.size} tasks • ${issues.count { it.parentIssueId != null }} nested':'${issues.size} 個任務 • ${issues.count { it.parentIssueId != null }} 個巢狀任務','Parent: $it':'上層任務：$it','Nested: ${progress.closed}/${progress.total} complete':'巢狀任務：${progress.closed}/${progress.total} 已完成','${issue.assigneeName ?: "Unassigned"} • ${issue.labels.ifBlank { "No labels" }}':'${issue.assigneeName ?: "未指派"} • ${issue.labels.ifBlank { "無標籤" }}'
}

ui_paths = list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [
    ROOT / 'app/src/main/java/com/example/MainActivity.kt',
    ROOT / 'app/src/main/java/com/example/data/model/GovernanceModels.kt',
    ROOT / 'app/src/main/java/com/example/data/repository/GovernanceRepository.kt'
]
for path in ui_paths:
    if path.exists():
        text = path.read_text(encoding='utf-8')
        text = replace_all_literal_strings(text, zh_templates)
        text = replace_all_literal_strings(text, zh)
        path.write_text(text, encoding='utf-8')

model_path = ROOT / 'app/src/main/java/com/example/data/model/GovernanceModels.kt'
model_text = model_path.read_text(encoding='utf-8')
for old, new in {
    'SPECIFICATION_DOC("Product Specification", "Description")':'SPECIFICATION_DOC("產品規格", "Description")',
    'PROCESS_WORKFLOW("No-Code Workflow", "AccountTree")':'PROCESS_WORKFLOW("無程式碼工作流程", "AccountTree")',
    'DECISION_RECORD("Architecture Decision Record (RFC)", "Gavel")':'DECISION_RECORD("決策紀錄（RFC）", "Gavel")',
    'FORM_SCHEMA("Form & Data Schema", "DynamicForm")':'FORM_SCHEMA("表單與資料結構", "DynamicForm")',
    'CANVAS_BOARD("Visual Process Canvas", "DashboardCustomize")':'CANVAS_BOARD("視覺流程畫布", "DashboardCustomize")',
    'MILESTONE_RELEASE("Milestone Release Gate", "Flag")':'MILESTONE_RELEASE("里程碑發布關卡", "Flag")',
    'OPEN("Open")':'OPEN("待處理")','IN_PROGRESS("In Progress")':'IN_PROGRESS("進行中")','CLOSED("Closed")':'CLOSED("已完成")',
    'LOW("Low")':'LOW("低")','MEDIUM("Medium")':'MEDIUM("中")','HIGH("High")':'HIGH("高")','CRITICAL("Critical")':'CRITICAL("緊急")'
}.items():
    model_text = model_text.replace(old, new)
model_path.write_text(model_text, encoding='utf-8')

(ROOT / 'app/src/main/res/values/strings.xml').write_text('<resources>\n    <string name="app_name">協作治理</string>\n</resources>\n', encoding='utf-8')
metadata_path = ROOT / 'metadata.json'
metadata = metadata_path.read_text(encoding='utf-8')
metadata = metadata.replace('"name": "RepoGovernance"', '"name": "協作治理"')
metadata = metadata.replace(
    '"description": "Hierarchical access control policy engine and no-code repository collaboration platform enforcing strict Enterprise, Organization, Team, User, Member, Collaborator, Maintainer, Reviewer, and Approver schema mappings."',
    '"description": "以 Repository 為無程式碼協作容器的企業治理行動應用，整合組織、團隊、任務、討論、成果、權限、審查與稽核。"'
)
metadata_path.write_text(metadata, encoding='utf-8')


# -----------------------------------------------------------------------------
# 7. Unit tests for recursive hierarchy and cycle prevention.
# -----------------------------------------------------------------------------
test_path = ROOT / 'app/src/test/java/com/example/IssueHierarchyRulesTest.kt'
test_path.write_text(r'''package com.example

import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.RepoIssue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IssueHierarchyRulesTest {
    private fun issue(id: String, number: Int, parentId: String? = null, repoId: String = "repo-1"): RepoIssue = RepoIssue(
        id = id, repoId = repoId, issueNumber = number, title = "任務 $number", description = "",
        priority = IssuePriority.MEDIUM, authorUserId = "user-1", authorDisplayName = "使用者", parentIssueId = parentId
    )

    @Test fun `supports arbitrary nested task depth`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"), issue("d", 4, "c"))
        assertEquals(setOf("b", "c", "d"), IssueHierarchyRules.descendantIds("a", issues))
        assertEquals(3, IssueHierarchyRules.depthOf("d", issues))
        assertEquals(listOf(0, 1, 2, 3), IssueHierarchyRules.orderedForDisplay(issues).map { it.second })
    }

    @Test fun `rejects self descendant and cross repository parent`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"), issue("x", 9, repoId = "repo-2"))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "a", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "b", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "c", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "x", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", "a", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", null, issues))
    }
}
''', encoding='utf-8')


# -----------------------------------------------------------------------------
# 8. UI language audit. Critical top-level English is a hard failure.
# -----------------------------------------------------------------------------
critical = [
    'Access Governance','Home','Repositories','Inbox','Me','Overview','Issues','Discussions','Artifacts',
    'New Issue','New Discussion','Repository Settings','Unified Inbox','Work Requiring Attention',
    'Kanban Board','Nested Tasks','To do','In progress','Done'
]
joined = '\n'.join(path.read_text(encoding='utf-8') for path in list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt'])
remaining = [token for token in critical if f'"{token}"' in joined]
if remaining:
    raise RuntimeError(f'critical UI English literals remain: {remaining}')

print('--- Residual probable English Text/contentDescription literals (audit) ---')
patterns = [
    re.compile(r'Text\(\s*(?:text\s*=\s*)?"([^"\\]*(?:\\.[^"\\]*)*)"'),
    re.compile(r'contentDescription\s*=\s*"([^"\\]*(?:\\.[^"\\]*)*)"')
]
residual = set()
for path in list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt']:
    source = path.read_text(encoding='utf-8')
    for pattern in patterns:
        for match in pattern.finditer(source):
            value = match.group(1)
            if re.search(r'[A-Za-z]{3,}', value) and not value.startswith(('http','SIG_','RBAC','ABAC','RFC','SSO','OIDC')):
                residual.add(f'{path.name}: {value}')
for item in sorted(residual):
    print(item)
print(f'Residual count: {len(residual)}')
print('Kanban navigation + recursive Nested Tasks + zh-TW transformation completed.')
