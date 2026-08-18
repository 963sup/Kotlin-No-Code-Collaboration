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


hierarchy_file = ROOT / 'app/src/main/java/com/example/data/model/IssueHierarchyRules.kt'
hierarchy_file.write_text(r'''package com.example.data.model

/**
 * Pure hierarchy rules shared by persistence and UI.
 * Nested tasks are RepoIssue relationships, not a second task entity.
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
        val roots = issues.filter { it.parentIssueId == null || it.parentIssueId !in byId }.sortedBy { it.issueNumber }
        roots.forEach { visit(it, 0) }
        issues.sortedBy { it.issueNumber }.forEach { issue ->
            if (issue.id !in visited) visit(issue, depthOf(issue.id, issues))
        }
        return result
    }
}
''', encoding='utf-8')

kanban_file = ROOT / 'app/src/main/java/com/example/ui/components/RepoKanbanBoard.kt'
kanban_file.write_text(r'''package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis

/** Kanban is a projection of Repository Issues by IssueStatus; it owns no separate data model. */
@Composable
fun RepoKanbanBoard(
    visibleIssues: List<RepoIssue>,
    allIssues: List<RepoIssue>,
    blockedIssueIds: Set<String>,
    onIssueClick: (RepoIssue) -> Unit,
    onUpdateStatus: (String, IssueStatus) -> Unit
) {
    val visibleIds = visibleIssues.map { it.id }.toSet()
    val ordered = IssueHierarchyRules.orderedForDisplay(allIssues).filter { (issue, _) -> issue.id in visibleIds }
    LazyRow(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(IssueStatus.values().toList(), key = { it.name }) { status ->
            KanbanColumn(
                status = status,
                issues = ordered.filter { (issue, _) -> issue.status == status },
                blockedIssueIds = blockedIssueIds,
                onIssueClick = onIssueClick,
                onUpdateStatus = onUpdateStatus
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    status: IssueStatus,
    issues: List<Pair<RepoIssue, Int>>,
    blockedIssueIds: Set<String>,
    onIssueClick: (RepoIssue) -> Unit,
    onUpdateStatus: (String, IssueStatus) -> Unit
) {
    val (title, accent, background) = when (status) {
        IssueStatus.OPEN -> Triple("待處理", EmeraldSuccess, EmeraldDark)
        IssueStatus.IN_PROGRESS -> Triple("進行中", AmberGlow, SophisticatedContainer)
        IssueStatus.CLOSED -> Triple("已完成", LavenderPrimary, LavenderContainer)
    }
    Surface(
        modifier = Modifier.width(286.dp).fillMaxHeight(),
        shape = RoundedCornerShape(14.dp), color = SophisticatedSurfaceDark,
        border = BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = background, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))) {
                    Text(title, color = accent, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                }
                Text(issues.size.toString(), color = TextMediumEmphasis, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (issues.isEmpty()) {
                Text("目前沒有任務", color = TextMediumEmphasis, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                    items(issues, key = { it.first.id }) { (issue, depth) ->
                        KanbanIssueCard(
                            issue = issue, depth = depth, isBlocked = issue.id in blockedIssueIds,
                            onClick = { onIssueClick(issue) },
                            onMoveBackward = when (status) {
                                IssueStatus.OPEN -> null
                                IssueStatus.IN_PROGRESS -> { { onUpdateStatus(issue.id, IssueStatus.OPEN) } }
                                IssueStatus.CLOSED -> { { onUpdateStatus(issue.id, IssueStatus.IN_PROGRESS) } }
                            },
                            onMoveForward = when (status) {
                                IssueStatus.OPEN -> { { onUpdateStatus(issue.id, IssueStatus.IN_PROGRESS) } }
                                IssueStatus.IN_PROGRESS -> { { onUpdateStatus(issue.id, IssueStatus.CLOSED) } }
                                IssueStatus.CLOSED -> null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanIssueCard(
    issue: RepoIssue,
    depth: Int,
    isBlocked: Boolean,
    onClick: () -> Unit,
    onMoveBackward: (() -> Unit)?,
    onMoveForward: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, if (isBlocked) RoseError.copy(alpha = 0.45f) else SophisticatedBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("#${issue.issueNumber}", color = LavenderPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (depth > 0) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary)
                        Text("第 ${depth + 1} 層", color = TextMediumEmphasis, fontSize = 10.sp)
                    }
                    if (isBlocked) Icon(Icons.Default.Lock, contentDescription = "受阻", tint = RoseError)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(issue.title, color = TextHighEmphasis, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2)
            if (issue.parentIssueNumber != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("上層任務 #${issue.parentIssueNumber}", color = TextMediumEmphasis, fontSize = 10.sp)
            }
            if (issue.assigneeName != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMediumEmphasis)
                    Text(issue.assigneeName, color = TextMediumEmphasis, fontSize = 10.sp, maxLines = 1)
                }
            }
            if (isBlocked) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(color = RoseDark, shape = RoundedCornerShape(6.dp)) {
                    Text("受前置任務阻擋", color = RoseError, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
            if (onMoveBackward != null || onMoveForward != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (onMoveBackward != null) TextButton(onClick = onMoveBackward) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null); Text("退回", fontSize = 11.sp)
                    } else Spacer(modifier = Modifier.width(1.dp))
                    if (onMoveForward != null) TextButton(onClick = onMoveForward) {
                        Text("下一步", fontSize = 11.sp); Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}
''', encoding='utf-8')

repo_path = ROOT / 'app/src/main/java/com/example/data/repository/GovernanceRepository.kt'
repo_text = repo_path.read_text(encoding='utf-8')
repo_text = require_replace(repo_text, 'import com.example.data.model.IssueDependency\n', 'import com.example.data.model.IssueDependency\nimport com.example.data.model.IssueHierarchyRules\n', 'repository hierarchy import')
repo_text = require_replace(repo_text,
'''        // Prevent direct circular hierarchy (parent cannot have issue as its parent)\n        if (parent.parentIssueId == issueId) {\n            return Pair(false, "Circular hierarchy detected: Issue #${parent.issueNumber} is already a child of Issue #${issue.issueNumber}.")\n        }\n''',
'''        // Nested tasks may have arbitrary depth, but the hierarchy must stay acyclic.\n        val repoIssues = dao.getIssuesByRepoOnce(issue.repoId)\n        if (!IssueHierarchyRules.canAssignParent(issueId, parentIssueId, repoIssues)) {\n            return Pair(false, "無法將任務設為自己或其任一子孫任務的下層，避免形成循環階層。")\n        }\n''', 'transitive cycle guard')
repo_path.write_text(repo_text, encoding='utf-8')

issues_path = ROOT / 'app/src/main/java/com/example/ui/components/RepoIssuesSection.kt'
issues_text = issues_path.read_text(encoding='utf-8')
issues_text = require_replace(issues_text, 'import androidx.compose.material.icons.filled.SubdirectoryArrowRight\n', 'import androidx.compose.material.icons.filled.SubdirectoryArrowRight\nimport androidx.compose.material.icons.filled.ViewAgenda\nimport androidx.compose.material.icons.filled.ViewKanban\n', 'view icons')
issues_text = require_replace(issues_text, 'import com.example.data.model.IssueDependency\n', 'import com.example.data.model.IssueDependency\nimport com.example.data.model.IssueHierarchyRules\n', 'ui hierarchy import')
issues_text = require_replace(issues_text, '@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)\n@Composable\nfun RepoIssuesSection(', 'private enum class IssueViewMode { LIST, KANBAN }\n\n@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)\n@Composable\nfun RepoIssuesSection(', 'view mode enum')
issues_text = require_replace(issues_text, '    var viewingIssue by remember { mutableStateOf<RepoIssue?>(null) }\n', '    var viewingIssue by remember { mutableStateOf<RepoIssue?>(null) }\n    var issueViewMode by remember { mutableStateOf(IssueViewMode.LIST) }\n', 'view mode state')
issues_text = require_replace(issues_text,
'''        Spacer(modifier = Modifier.height(10.dp))\n\n        // Filter Chips (Status, Priority & Hierarchy Breakdown)\n''',
'''        Spacer(modifier = Modifier.height(10.dp))\n\n        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {\n            FilterChip(\n                selected = issueViewMode == IssueViewMode.LIST,\n                onClick = { issueViewMode = IssueViewMode.LIST },\n                leadingIcon = { Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp)) },\n                label = { Text("清單") },\n                modifier = Modifier.testTag("issue_view_list")\n            )\n            FilterChip(\n                selected = issueViewMode == IssueViewMode.KANBAN,\n                onClick = { issueViewMode = IssueViewMode.KANBAN },\n                leadingIcon = { Icon(Icons.Default.ViewKanban, contentDescription = null, modifier = Modifier.size(16.dp)) },\n                label = { Text("看板") },\n                modifier = Modifier.testTag("issue_view_kanban")\n            )\n        }\n\n        Spacer(modifier = Modifier.height(10.dp))\n\n        // Filter Chips (Status, Priority & Hierarchy Breakdown)\n''', 'view toggle')

pattern = re.compile(r'''        \} else \{\n            LazyColumn\(\n                modifier = Modifier\n                    \.fillMaxWidth\(\)\n                    \.weight\(1f\),\n                verticalArrangement = Arrangement\.spacedBy\(10\.dp\),\n                contentPadding = PaddingValues\(bottom = 24\.dp\)\n            \) \{\n                items\(filteredIssues, key = \{ it\.id \}\) \{ issue ->\n                    val subIssues = issues\.filter \{ it\.parentIssueId == issue\.id \}\n                    val isBlocked = issue\.id in blockedIssueIds\n                    val blockedByList = dependencies\.filter \{ it\.blockedIssueId == issue\.id \}\n                        \.mapNotNull \{ dep -> issues\.firstOrNull \{ it\.id == dep\.blockingIssueId \} \}\n                    val blockingList = dependencies\.filter \{ it\.blockingIssueId == issue\.id \}\n                        \.mapNotNull \{ dep -> issues\.firstOrNull \{ it\.id == dep\.blockedIssueId \} \}\n\n                    HierarchicalIssueCard\(\n                        issue = issue,\n                        subIssues = subIssues,\n                        isBlocked = isBlocked,\n                        blockedByIssues = blockedByList,\n                        blockingIssues = blockingList,\n                        onClick = \{ viewingIssue = issue \},\n                        onAddSubIssue = \{\n                            preselectedParentForCreate = issue\n                            showCreateDialog = true\n                        \}\n                    \)\n                \}\n            \}\n        \}''', re.S)
replacement = '''        } else {\n            val visibleIssueIds = filteredIssues.map { it.id }.toSet()\n            val orderedFilteredIssues = remember(issues, filteredIssues) {\n                IssueHierarchyRules.orderedForDisplay(issues).filter { (issue, _) -> issue.id in visibleIssueIds }\n            }\n            if (issueViewMode == IssueViewMode.LIST) {\n                LazyColumn(\n                    modifier = Modifier.fillMaxWidth().weight(1f),\n                    verticalArrangement = Arrangement.spacedBy(10.dp),\n                    contentPadding = PaddingValues(bottom = 24.dp)\n                ) {\n                    items(orderedFilteredIssues, key = { it.first.id }) { (issue, depth) ->\n                        val nestedIds = IssueHierarchyRules.descendantIds(issue.id, issues)\n                        val nestedTasks = issues.filter { it.id in nestedIds }\n                        val isBlocked = issue.id in blockedIssueIds\n                        val blockedByList = dependencies.filter { it.blockedIssueId == issue.id }.mapNotNull { dep -> issues.firstOrNull { it.id == dep.blockingIssueId } }\n                        val blockingList = dependencies.filter { it.blockingIssueId == issue.id }.mapNotNull { dep -> issues.firstOrNull { it.id == dep.blockedIssueId } }\n                        HierarchicalIssueCard(\n                            issue = issue, subIssues = nestedTasks, depth = depth, isBlocked = isBlocked,\n                            blockedByIssues = blockedByList, blockingIssues = blockingList,\n                            onClick = { viewingIssue = issue },\n                            onAddSubIssue = { preselectedParentForCreate = issue; showCreateDialog = true }\n                        )\n                    }\n                }\n            } else {\n                RepoKanbanBoard(\n                    visibleIssues = filteredIssues, allIssues = issues, blockedIssueIds = blockedIssueIds,\n                    onIssueClick = { viewingIssue = it }, onUpdateStatus = onUpdateStatus\n                )\n            }\n        }'''
issues_text, n = pattern.subn(replacement, issues_text, count=1)
if n != 1: raise RuntimeError(f'flat list replacement count={n}')
issues_text = require_replace(issues_text, '        val currentIssueSubIssues = issues.filter { it.parentIssueId == currentViewingIssue.id }\n', '        val currentIssueNestedIds = IssueHierarchyRules.descendantIds(currentViewingIssue.id, issues)\n        val currentIssueSubIssues = IssueHierarchyRules.orderedForDisplay(issues).map { it.first }.filter { it.id in currentIssueNestedIds }\n', 'nested detail descendants')
issues_text = require_replace(issues_text, 'fun HierarchicalIssueCard(\n    issue: RepoIssue,\n    subIssues: List<RepoIssue>,\n    isBlocked: Boolean,', 'fun HierarchicalIssueCard(\n    issue: RepoIssue,\n    subIssues: List<RepoIssue>,\n    depth: Int = 0,\n    isBlocked: Boolean,', 'card depth param')
issues_text = require_replace(issues_text, '        modifier = Modifier\n            .fillMaxWidth()\n            .clickable(onClick = onClick)\n            .testTag("issue_card_${issue.issueNumber}"),', '        modifier = Modifier\n            .fillMaxWidth()\n            .padding(start = (depth.coerceAtMost(5) * 10).dp)\n            .clickable(onClick = onClick)\n            .testTag("issue_card_${issue.issueNumber}"),', 'card depth indent')
issues_text = require_replace(issues_text,
'''    // Eligible issues that can be chosen as parent (must not be itself or existing child)\n    val eligibleParents = remember(allRepoIssues, issue, subIssues) {\n        val childIds = subIssues.map { it.id }.toSet()\n        allRepoIssues.filter { it.id != issue.id && it.id !in childIds && it.parentIssueId == null }\n    }''',
'''    val eligibleParents = remember(allRepoIssues, issue) {\n        IssueHierarchyRules.orderedForDisplay(allRepoIssues).map { it.first }.filter { candidate ->\n            IssueHierarchyRules.canAssignParent(issue.id, candidate.id, allRepoIssues)\n        }\n    }''', 'eligible parents')
issues_text = require_replace(issues_text,
'''                        allRepoIssues.filter { it.parentIssueId == null }.forEach { parent ->\n                            DropdownMenuItem(\n                                leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },\n                                text = { Text("#${parent.issueNumber} ${parent.title}", color = TextHighEmphasis) },''',
'''                        IssueHierarchyRules.orderedForDisplay(allRepoIssues).forEach { (parent, depth) ->\n                            DropdownMenuItem(\n                                leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },\n                                text = { Text("${"· ".repeat(depth)}#${parent.issueNumber} ${parent.title}", color = TextHighEmphasis) },''', 'nested parent dropdown')
issues_path.write_text(issues_text, encoding='utf-8')

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
'Approvals & Sign-offs':'核准與簽核','Issue Assignments':'任務指派','Mentions & Replies':'提及與回覆','Access & Permissions':'存取與權限','Org & Team Memberships':'組織與團隊成員關係','Releases & Publications':'發布與公告','Governance & Policy Alerts':'治理與政策警示','Low':'低','Normal':'一般','High':'高','Urgent':'緊急'
}
zh_templates = {
'${dependencies.size} links':'${dependencies.size} 個相依連結','Blocked ($blockedCount)':'受阻 ($blockedCount)','Epics / Parents ($parentCount)':'上層任務 ($parentCount)','Open ($count)':'待處理 ($count)','In Progress ($count)':'進行中 ($count)','Closed ($count)':'已完成 ($count)','Sub-issue of #${issue.parentIssueNumber} ${issue.parentIssueTitle ?: ""}':'上層任務 #${issue.parentIssueNumber} ${issue.parentIssueTitle ?: ""}','Sub-issues: $closedSub of $totalSub completed':'子任務：$closedSub / $totalSub 已完成','Blocked ($blockedByCount)':'受阻 ($blockedByCount)','$closedSub of $totalSub Sub-tasks Resolved':'$closedSub / $totalSub 子任務已完成','Parent: #${issue.parentIssueNumber}':'上層：#${issue.parentIssueNumber}','Repository: ${repo.name}':'儲存庫：${repo.name}','$unreadCount new':'$unreadCount 則新通知'
}

for path in list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt', ROOT / 'app/src/main/java/com/example/data/model/GovernanceModels.kt', ROOT / 'app/src/main/java/com/example/data/repository/GovernanceRepository.kt']:
    if path.exists():
        text = path.read_text(encoding='utf-8')
        text = replace_all_literal_strings(text, zh_templates)
        text = replace_all_literal_strings(text, zh)
        path.write_text(text, encoding='utf-8')

model_path = ROOT / 'app/src/main/java/com/example/data/model/GovernanceModels.kt'
model_text = model_path.read_text(encoding='utf-8')
for old, new in {
'SPECIFICATION_DOC("Product Specification", "Description")':'SPECIFICATION_DOC("產品規格", "Description")','PROCESS_WORKFLOW("No-Code Workflow", "AccountTree")':'PROCESS_WORKFLOW("無程式碼工作流程", "AccountTree")','DECISION_RECORD("Architecture Decision Record (RFC)", "Gavel")':'DECISION_RECORD("決策紀錄（RFC）", "Gavel")','FORM_SCHEMA("Form & Data Schema", "DynamicForm")':'FORM_SCHEMA("表單與資料結構", "DynamicForm")','CANVAS_BOARD("Visual Process Canvas", "DashboardCustomize")':'CANVAS_BOARD("視覺流程畫布", "DashboardCustomize")','MILESTONE_RELEASE("Milestone Release Gate", "Flag")':'MILESTONE_RELEASE("里程碑發布關卡", "Flag")','OPEN("Open")':'OPEN("待處理")','IN_PROGRESS("In Progress")':'IN_PROGRESS("進行中")','CLOSED("Closed")':'CLOSED("已完成")','LOW("Low")':'LOW("低")','MEDIUM("Medium")':'MEDIUM("中")','HIGH("High")':'HIGH("高")','CRITICAL("Critical")':'CRITICAL("緊急")'}.items():
    model_text = model_text.replace(old, new)
model_path.write_text(model_text, encoding='utf-8')

(ROOT / 'app/src/main/res/values/strings.xml').write_text('<resources>\n    <string name="app_name">協作治理</string>\n</resources>\n', encoding='utf-8')
metadata_path = ROOT / 'metadata.json'
metadata = metadata_path.read_text(encoding='utf-8').replace('"name": "RepoGovernance"', '"name": "協作治理"')
metadata = metadata.replace('"description": "Hierarchical access control policy engine and no-code repository collaboration platform enforcing strict Enterprise, Organization, Team, User, Member, Collaborator, Maintainer, Reviewer, and Approver schema mappings."', '"description": "以 Repository 為無程式碼協作容器的企業治理行動應用，整合組織、團隊、任務、討論、成果、權限、審查與稽核。"')
metadata_path.write_text(metadata, encoding='utf-8')

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
    private fun issue(id: String, number: Int, parentId: String? = null): RepoIssue = RepoIssue(
        id = id, repoId = "repo-1", issueNumber = number, title = "Task $number", description = "",
        priority = IssuePriority.MEDIUM, authorUserId = "user-1", authorDisplayName = "User", parentIssueId = parentId
    )

    @Test fun `supports arbitrary nested task depth`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"), issue("d", 4, "c"))
        assertEquals(setOf("b", "c", "d"), IssueHierarchyRules.descendantIds("a", issues))
        assertEquals(3, IssueHierarchyRules.depthOf("d", issues))
        assertEquals(listOf(0, 1, 2, 3), IssueHierarchyRules.orderedForDisplay(issues).map { it.second })
    }

    @Test fun `rejects self and descendant as new parent`() {
        val issues = listOf(issue("a", 1), issue("b", 2, "a"), issue("c", 3, "b"))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "a", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "b", issues))
        assertFalse(IssueHierarchyRules.canAssignParent("a", "c", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", "a", issues))
        assertTrue(IssueHierarchyRules.canAssignParent("c", null, issues))
    }
}
''', encoding='utf-8')

forbidden_visible = ['Access Governance','Home','Repositories','Inbox','Me','Overview','Issues','Discussions','Artifacts','New Issue','New Discussion','Repository Settings','Unified Inbox','Work Requiring Attention']
joined = '\n'.join(path.read_text(encoding='utf-8') for path in list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt'])
remaining = [token for token in forbidden_visible if f'"{token}"' in joined]
if remaining: raise RuntimeError(f'critical UI English literals remain: {remaining}')
print('--- Residual probable English Text literals (audit only) ---')
text_literal = re.compile(r'Text\(\s*(?:text\s*=\s*)?"([^"\\]*(?:\\.[^"\\]*)*)"')
residual = set()
for path in list((ROOT / 'app/src/main/java/com/example/ui').rglob('*.kt')) + [ROOT / 'app/src/main/java/com/example/MainActivity.kt']:
    source = path.read_text(encoding='utf-8')
    for match in text_literal.finditer(source):
        value = match.group(1)
        if re.search(r'[A-Za-z]{3,}', value) and not value.startswith(('http','SIG_','RBAC','ABAC')):
            residual.add(f'{path.name}: {value}')
for item in sorted(residual): print(item)
print(f'Residual count: {len(residual)}')
print('Workboard + nested tasks + zh-TW transformation completed.')
