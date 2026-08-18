package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DependencyType
import com.example.data.model.GranteeType
import com.example.data.model.IssueComment
import com.example.data.model.IssueDependency
import com.example.data.model.IssueHierarchyRules
import com.example.data.model.IssuePriority
import com.example.data.model.IssueStatus
import com.example.data.model.NoCodeArtifact
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.User
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RepoIssuesSection(
    repo: Repository,
    issues: List<RepoIssue>,
    dependencies: List<IssueDependency> = emptyList(),
    selectedIssueComments: List<IssueComment>,
    allUsers: List<User>,
    allTeams: List<Team>,
    repoArtifacts: List<NoCodeArtifact>,
    activeUser: User?,
    canCreateIssue: Boolean,
    onCreateIssue: (
        title: String,
        desc: String,
        priority: IssuePriority,
        assigneeType: GranteeType?,
        assigneeId: String?,
        assigneeName: String?,
        linkedArtifactId: String?,
        linkedArtifactTitle: String?,
        parentIssueId: String?,
        labels: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onLinkParentIssue: (issueId: String, parentIssueId: String?, onSuccess: () -> Unit) -> Unit = { _, _, _ -> },
    onAddDependency: (repoId: String, blockedIssueId: String, blockingIssueId: String, onSuccess: () -> Unit) -> Unit = { _, _, _, _ -> },
    onRemoveDependency: (dependencyId: String, onSuccess: () -> Unit) -> Unit = { _, _ -> },
    onAddComment: (issueId: String, content: String, onSuccess: () -> Unit) -> Unit,
    onUpdateStatus: (issueId: String, newStatus: IssueStatus) -> Unit,
    onAssignIssue: (issueId: String, assigneeType: GranteeType?, assigneeId: String?, assigneeName: String?) -> Unit,
    onLoadComments: (issueId: String) -> Unit,
    onSelectArtifact: ((NoCodeArtifact) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<IssueStatus?>(null) }
    var selectedPriorityFilter by remember { mutableStateOf<IssuePriority?>(null) }
    var hierarchyFilter by remember { mutableStateOf<String>("ALL") } // ALL, PARENTS_ONLY, BLOCKED_ONLY, SUB_ISSUES_ONLY
    var showCreateDialog by remember { mutableStateOf(false) }
    var preselectedParentForCreate by remember { mutableStateOf<RepoIssue?>(null) }
    var viewingIssue by remember { mutableStateOf<RepoIssue?>(null) }

    // Sync selected viewing issue if updated in state
    val currentViewingIssue = viewingIssue?.let { curr ->
        issues.firstOrNull { it.id == curr.id } ?: curr
    }

    LaunchedEffect(currentViewingIssue?.id) {
        currentViewingIssue?.id?.let { onLoadComments(it) }
    }

    // Precalculate blocking status per issue
    val blockedIssueIds = remember(issues, dependencies) {
        val openIssueIds = issues.filter { it.status != IssueStatus.CLOSED }.map { it.id }.toSet()
        dependencies.filter { it.blockingIssueId in openIssueIds }.map { it.blockedIssueId }.toSet()
    }

    val filteredIssues = remember(
        issues,
        searchQuery,
        selectedStatusFilter,
        selectedPriorityFilter,
        hierarchyFilter,
        blockedIssueIds
    ) {
        issues.filter { issue ->
            val matchesSearch = searchQuery.isBlank() ||
                    issue.title.contains(searchQuery, ignoreCase = true) ||
                    issue.description.contains(searchQuery, ignoreCase = true) ||
                    issue.labels.contains(searchQuery, ignoreCase = true) ||
                    issue.authorDisplayName.contains(searchQuery, ignoreCase = true) ||
                    (issue.assigneeName?.contains(searchQuery, ignoreCase = true) == true)

            val matchesStatus = selectedStatusFilter == null || issue.status == selectedStatusFilter
            val matchesPriority = selectedPriorityFilter == null || issue.priority == selectedPriorityFilter

            val matchesHierarchy = when (hierarchyFilter) {
                "PARENTS_ONLY" -> issue.parentIssueId == null && issues.any { it.parentIssueId == issue.id }
                "BLOCKED_ONLY" -> issue.id in blockedIssueIds
                "SUB_ISSUES_ONLY" -> issue.parentIssueId != null
                else -> true
            }

            matchesSearch && matchesStatus && matchesPriority && matchesHierarchy
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(16.dp)
    ) {
        // Top Action Bar: Header & Stats & New Issue CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "協作任務",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis
                    )
                    if (dependencies.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(LavenderContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${dependencies.size} 個相依連結",
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderGlow,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Text(
                    text = "追蹤巢狀任務、阻擋相依與工作指派",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis
                )
            }

            Button(
                onClick = {
                    preselectedParentForCreate = null
                    showCreateDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = LavenderOnPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = canCreateIssue,
                modifier = Modifier.testTag("create_issue_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("新增任務", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("issue_search_input"),
            placeholder = { Text("依標題、標籤、作者或受派者篩選任務…", color = TextLowEmphasis, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMediumEmphasis) },
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "清除", tint = TextMediumEmphasis)
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SophisticatedSurface,
                unfocusedContainerColor = SophisticatedSurface,
                focusedBorderColor = LavenderPrimary,
                unfocusedBorderColor = SophisticatedBorder,
                focusedTextColor = TextHighEmphasis,
                unfocusedTextColor = TextHighEmphasis
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips (Status, Priority & Hierarchy Breakdown)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedStatusFilter == null && selectedPriorityFilter == null && hierarchyFilter == "ALL",
                onClick = {
                    selectedStatusFilter = null
                    selectedPriorityFilter = null
                    hierarchyFilter = "ALL"
                },
                label = { Text("All (${issues.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LavenderContainer,
                    selectedLabelColor = LavenderGlow,
                    containerColor = SophisticatedSurface,
                    labelColor = TextMediumEmphasis
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedStatusFilter == null && selectedPriorityFilter == null && hierarchyFilter == "ALL",
                    borderColor = SophisticatedBorder,
                    selectedBorderColor = LavenderPrimary
                )
            )

            // Blocked filter chip
            val blockedCount = issues.count { it.id in blockedIssueIds }
            FilterChip(
                selected = hierarchyFilter == "BLOCKED_ONLY",
                onClick = {
                    hierarchyFilter = if (hierarchyFilter == "BLOCKED_ONLY") "ALL" else "BLOCKED_ONLY"
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = if (hierarchyFilter == "BLOCKED_ONLY") RoseError else TextMediumEmphasis,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("受阻 ($blockedCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoseDark,
                    selectedLabelColor = RoseError,
                    containerColor = SophisticatedSurface,
                    labelColor = TextMediumEmphasis
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = hierarchyFilter == "BLOCKED_ONLY",
                    borderColor = SophisticatedBorder,
                    selectedBorderColor = RoseError
                )
            )

            // Epics/Parents filter chip
            val parentCount = issues.count { parent -> issues.any { it.parentIssueId == parent.id } }
            FilterChip(
                selected = hierarchyFilter == "PARENTS_ONLY",
                onClick = {
                    hierarchyFilter = if (hierarchyFilter == "PARENTS_ONLY") "ALL" else "PARENTS_ONLY"
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = if (hierarchyFilter == "PARENTS_ONLY") LavenderGlow else TextMediumEmphasis,
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = { Text("上層任務 ($parentCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LavenderContainer,
                    selectedLabelColor = LavenderGlow,
                    containerColor = SophisticatedSurface,
                    labelColor = TextMediumEmphasis
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = hierarchyFilter == "PARENTS_ONLY",
                    borderColor = SophisticatedBorder,
                    selectedBorderColor = LavenderPrimary
                )
            )

            FilterChip(
                selected = selectedStatusFilter == IssueStatus.OPEN,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == IssueStatus.OPEN) null else IssueStatus.OPEN
                },
                label = {
                    val count = issues.count { it.status == IssueStatus.OPEN }
                    Text("待處理 ($count)", fontSize = 12.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldDark,
                    selectedLabelColor = EmeraldSuccess,
                    containerColor = SophisticatedSurface,
                    labelColor = TextMediumEmphasis
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedStatusFilter == IssueStatus.OPEN,
                    borderColor = SophisticatedBorder,
                    selectedBorderColor = EmeraldSuccess
                )
            )

            FilterChip(
                selected = selectedStatusFilter == IssueStatus.IN_PROGRESS,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == IssueStatus.IN_PROGRESS) null else IssueStatus.IN_PROGRESS
                },
                label = {
                    val count = issues.count { it.status == IssueStatus.IN_PROGRESS }
                    Text("進行中 ($count)", fontSize = 12.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF422E10),
                    selectedLabelColor = AmberGlow,
                    containerColor = SophisticatedSurface,
                    labelColor = TextMediumEmphasis
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedStatusFilter == IssueStatus.IN_PROGRESS,
                    borderColor = SophisticatedBorder,
                    selectedBorderColor = AmberWarning
                )
            )

            FilterChip(
                selected = selectedStatusFilter == IssueStatus.CLOSED,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == IssueStatus.CLOSED) null else IssueStatus.CLOSED
                },
                label = {
                    val count = issues.count { it.status == IssueStatus.CLOSED }
                    Text("已完成 ($count)", fontSize = 12.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SophisticatedContainer,
                    selectedLabelColor = LavenderSubtle,
                    containerColor = SophisticatedSurface,
                    labelColor = TextMediumEmphasis
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedStatusFilter == IssueStatus.CLOSED,
                    borderColor = SophisticatedBorder,
                    selectedBorderColor = LavenderPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Issues List or Empty State
        if (filteredIssues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(SophisticatedSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (issues.isEmpty()) "此儲存庫尚無任務" else "找不到符合條件的任務",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (issues.isEmpty())
                            "任務可拆解成多層子任務，並追蹤進度、相依關係與跨使用者／團隊指派。"
                        else
                            "請清除或調整篩選條件。",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (issues.isEmpty() && canCreateIssue) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                preselectedParentForCreate = null
                                showCreateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("建立第一個任務")
                        }
                    }
                }
            }
        } else {
            val visibleIssueIds = filteredIssues.map { it.id }.toSet()
            val orderedFilteredIssues = remember(issues, filteredIssues) {
                IssueHierarchyRules.orderedForDisplay(issues).filter { (issue, _) -> issue.id in visibleIssueIds }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(orderedFilteredIssues, key = { it.first.id }) { (issue, depth) ->
                    val nestedIds = IssueHierarchyRules.descendantIds(issue.id, issues)
                    val nestedTasks = issues.filter { it.id in nestedIds }
                    val isBlocked = issue.id in blockedIssueIds
                    val blockedByList = dependencies.filter { it.blockedIssueId == issue.id }
                        .mapNotNull { dep -> issues.firstOrNull { it.id == dep.blockingIssueId } }
                    val blockingList = dependencies.filter { it.blockingIssueId == issue.id }
                        .mapNotNull { dep -> issues.firstOrNull { it.id == dep.blockedIssueId } }
                    HierarchicalIssueCard(
                        issue = issue, subIssues = nestedTasks, depth = depth, isBlocked = isBlocked,
                        blockedByIssues = blockedByList, blockingIssues = blockingList,
                        onClick = { viewingIssue = issue },
                        onAddSubIssue = { preselectedParentForCreate = issue; showCreateDialog = true }
                    )
                }
            }
        }
    }

    // View & Manage Issue Dialog
    if (currentViewingIssue != null) {
        val currentIssueNestedIds = IssueHierarchyRules.descendantIds(currentViewingIssue.id, issues)
        val currentIssueSubIssues = IssueHierarchyRules.orderedForDisplay(issues).map { it.first }.filter { it.id in currentIssueNestedIds }
        val currentBlockedBy = dependencies.filter { it.blockedIssueId == currentViewingIssue.id }
            .mapNotNull { dep ->
                val blocking = issues.firstOrNull { it.id == dep.blockingIssueId }
                if (blocking != null) Pair(dep, blocking) else null
            }
        val currentBlocking = dependencies.filter { it.blockingIssueId == currentViewingIssue.id }
            .mapNotNull { dep ->
                val blocked = issues.firstOrNull { it.id == dep.blockedIssueId }
                if (blocked != null) Pair(dep, blocked) else null
            }

        IssueDetailDialog(
            repo = repo,
            issue = currentViewingIssue,
            subIssues = currentIssueSubIssues,
            blockedByDependencies = currentBlockedBy,
            blockingDependencies = currentBlocking,
            allRepoIssues = issues,
            comments = selectedIssueComments,
            allUsers = allUsers,
            allTeams = allTeams,
            repoArtifacts = repoArtifacts,
            activeUser = activeUser,
            onDismiss = { viewingIssue = null },
            onAddComment = { content ->
                onAddComment(currentViewingIssue.id, content) {
                    onLoadComments(currentViewingIssue.id)
                }
            },
            onUpdateStatus = { status ->
                onUpdateStatus(currentViewingIssue.id, status)
            },
            onAssignIssue = { type, id, name ->
                onAssignIssue(currentViewingIssue.id, type, id, name)
            },
            onLinkParent = { parentId ->
                onLinkParentIssue(currentViewingIssue.id, parentId) {}
            },
            onAddDependency = { blockingId ->
                onAddDependency(repo.id, currentViewingIssue.id, blockingId) {}
            },
            onRemoveDependency = { depId ->
                onRemoveDependency(depId) {}
            },
            onAddSubIssue = {
                preselectedParentForCreate = currentViewingIssue
                showCreateDialog = true
            },
            onSelectArtifact = { artId ->
                val art = repoArtifacts.firstOrNull { it.id == artId }
                if (art != null && onSelectArtifact != null) {
                    viewingIssue = null
                    onSelectArtifact(art)
                }
            }
        )
    }

    // Create Issue Dialog
    if (showCreateDialog) {
        CreateIssueDialog(
            repo = repo,
            initialParentIssue = preselectedParentForCreate,
            allRepoIssues = issues,
            allUsers = allUsers,
            allTeams = allTeams,
            repoArtifacts = repoArtifacts,
            onDismiss = {
                showCreateDialog = false
                preselectedParentForCreate = null
            },
            onCreate = { title, desc, priority, assigneeType, assigneeId, assigneeName, linkedArtifactId, linkedArtifactTitle, parentId, labels ->
                onCreateIssue(
                    title,
                    desc,
                    priority,
                    assigneeType,
                    assigneeId,
                    assigneeName,
                    linkedArtifactId,
                    linkedArtifactTitle,
                    parentId,
                    labels
                ) {
                    showCreateDialog = false
                    preselectedParentForCreate = null
                }
            }
        )
    }
}

/**
 * Rich Hierarchical Issue Card displaying sub-issue completion progress bar,
 * parent breadcrumb tag, and blocked dependency indicators.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HierarchicalIssueCard(
    issue: RepoIssue,
    subIssues: List<RepoIssue>,
    depth: Int = 0,
    isBlocked: Boolean,
    blockedByIssues: List<RepoIssue>,
    blockingIssues: List<RepoIssue>,
    onClick: () -> Unit,
    onAddSubIssue: () -> Unit
) {
    var expandedSubList by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth.coerceAtMost(5) * 10).dp)
            .clickable(onClick = onClick)
            .testTag("issue_card_${issue.issueNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(
            1.dp,
            if (isBlocked && issue.status != IssueStatus.CLOSED) RoseError.copy(alpha = 0.5f) else SophisticatedBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Parent Issue Breadcrumb (if this is a sub-issue)
            if (issue.parentIssueNumber != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        Icons.Default.SubdirectoryArrowRight,
                        contentDescription = "上層任務",
                        tint = LavenderPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "上層任務 #${issue.parentIssueNumber} ${issue.parentIssueTitle ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderPrimary,
                        maxLines = 1,
                        fontSize = 11.sp
                    )
                }
            }

            // Header Row: Issue Number, Status Badge, Priority Badge, Blocked Badge, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${issue.issueNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = LavenderPrimary
                    )

                    IssueStatusBadge(status = issue.status)
                    IssuePriorityBadge(priority = issue.priority)

                    // Blocked indicator pill
                    if (isBlocked && issue.status != IssueStatus.CLOSED) {
                        BlockedIndicatorBadge(blockedByCount = blockedByIssues.count { it.status != IssueStatus.CLOSED })
                    }
                }

                Text(
                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(issue.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Issue Title
            Text(
                text = issue.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextHighEmphasis
            )

            // Snippet Description
            if (issue.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = issue.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                    maxLines = 2,
                    fontSize = 12.sp
                )
            }

            // Sub-issues Progress Breakdown (if this issue has children)
            if (subIssues.isNotEmpty()) {
                val totalSub = subIssues.size
                val closedSub = subIssues.count { it.status == IssueStatus.CLOSED }
                val progress = if (totalSub > 0) closedSub.toFloat() / totalSub.toFloat() else 0f

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SophisticatedSurfaceDark,
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.AccountTree,
                                    contentDescription = null,
                                    tint = LavenderGlow,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "子任務：$closedSub / $totalSub 已完成",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderGlow,
                                    fontSize = 11.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.clickable { expandedSubList = !expandedSubList }
                            ) {
                                Text(
                                    text = if (expandedSubList) "收合" else "查看階層",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LavenderPrimary,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = if (expandedSubList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (progress == 1f) EmeraldSuccess else LavenderPrimary,
                            trackColor = SophisticatedContainer,
                        )

                        // Expandable mini sub-issue tree preview
                        AnimatedVisibility(
                            visible = expandedSubList,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                subIssues.forEach { sub ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (sub.status == IssueStatus.CLOSED) Icons.Default.CheckCircle else Icons.Default.SubdirectoryArrowRight,
                                                contentDescription = null,
                                                tint = if (sub.status == IssueStatus.CLOSED) EmeraldSuccess else TextMediumEmphasis,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "#${sub.issueNumber} ${sub.title}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (sub.status == IssueStatus.CLOSED) TextMediumEmphasis else TextHighEmphasis,
                                                maxLines = 1,
                                                fontSize = 11.sp
                                            )
                                        }
                                        IssueStatusBadge(status = sub.status)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Labels Chips
            if (issue.labels.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    issue.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { label ->
                        Box(
                            modifier = Modifier
                                .background(SophisticatedContainer, RoundedCornerShape(6.dp))
                                .border(1.dp, SophisticatedBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderGlow,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Author & Role + Assignee / Blueprint tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author & Role
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(LavenderContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = issue.authorDisplayName.take(1).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LavenderGlow
                        )
                    }

                    Text(
                        text = issue.authorDisplayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHighEmphasis,
                        fontSize = 12.sp
                    )

                    RoleBadge(roleName = issue.authorRole)
                }

                // Assignee & Linked Artifact
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (issue.assigneeName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(SophisticatedSurfaceDark, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (issue.assigneeType == GranteeType.TEAM) Icons.Default.Groups else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (issue.assigneeType == GranteeType.TEAM) CyanAccent else LavenderPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = issue.assigneeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextHighEmphasis,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    if (issue.linkedArtifactTitle != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(SophisticatedSurfaceDark, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = PinkAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "規格",
                                style = MaterialTheme.typography.labelSmall,
                                color = PinkAccent,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedIndicatorBadge(blockedByCount: Int) {
    Box(
        modifier = Modifier
            .background(RoseDark, RoundedCornerShape(6.dp))
            .border(1.dp, RoseError.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = RoseError, modifier = Modifier.size(11.dp))
            Text(
                text = if (blockedByCount > 0) "受阻 ($blockedByCount)" else "受阻",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = RoseError,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun IssueStatusBadge(status: IssueStatus) {
    val (bgColor, textColor, text) = when (status) {
        IssueStatus.OPEN -> Triple(EmeraldDark, EmeraldSuccess, "待處理")
        IssueStatus.IN_PROGRESS -> Triple(Color(0xFF422E10), AmberGlow, "進行中")
        IssueStatus.CLOSED -> Triple(SophisticatedContainer, LavenderSubtle, "已完成")
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun IssuePriorityBadge(priority: IssuePriority) {
    val (bgColor, textColor, label) = when (priority) {
        IssuePriority.CRITICAL -> Triple(RoseDark, RoseError, "緊急")
        IssuePriority.HIGH -> Triple(Color(0xFF4A2800), AmberWarning, "高")
        IssuePriority.MEDIUM -> Triple(SophisticatedContainer, LavenderPrimary, "中")
        IssuePriority.LOW -> Triple(SophisticatedSurfaceDark, TextMediumEmphasis, "低")
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            fontSize = 10.sp
        )
    }
}

/**
 * Full Comprehensive Dialog to View & Manage Issue Details:
 * Includes Parent Issue link/unlink, Sub-issues list + quick add, Dependencies manager (Blocked By & Blocking),
 * Assignee, Status actions, and threaded comments.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IssueDetailDialog(
    repo: Repository,
    issue: RepoIssue,
    subIssues: List<RepoIssue>,
    blockedByDependencies: List<Pair<IssueDependency, RepoIssue>>,
    blockingDependencies: List<Pair<IssueDependency, RepoIssue>>,
    allRepoIssues: List<RepoIssue>,
    comments: List<IssueComment>,
    allUsers: List<User>,
    allTeams: List<Team>,
    repoArtifacts: List<NoCodeArtifact>,
    activeUser: User?,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onUpdateStatus: (IssueStatus) -> Unit,
    onAssignIssue: (GranteeType?, String?, String?) -> Unit,
    onLinkParent: (String?) -> Unit,
    onAddDependency: (blockingIssueId: String) -> Unit,
    onRemoveDependency: (dependencyId: String) -> Unit,
    onAddSubIssue: () -> Unit,
    onSelectArtifact: ((String) -> Unit)? = null
) {
    var newCommentText by remember { mutableStateOf("") }
    var showAssignMenu by remember { mutableStateOf(false) }
    var showParentMenu by remember { mutableStateOf(false) }
    var showAddBlockerMenu by remember { mutableStateOf(false) }

    val openBlockersCount = blockedByDependencies.count { it.second.status != IssueStatus.CLOSED }
    val isCurrentlyBlocked = openBlockersCount > 0 && issue.status != IssueStatus.CLOSED

    val eligibleParents = remember(allRepoIssues, issue) {
        IssueHierarchyRules.orderedForDisplay(allRepoIssues).map { it.first }.filter { candidate ->
            IssueHierarchyRules.canAssignParent(issue.id, candidate.id, allRepoIssues)
        }
    }

    // Eligible issues that can block this issue (must not be itself, and not already linked)
    val existingBlockedByIssueIds = blockedByDependencies.map { it.second.id }.toSet()
    val eligibleBlockers = remember(allRepoIssues, issue, existingBlockedByIssueIds) {
        allRepoIssues.filter { it.id != issue.id && it.id !in existingBlockedByIssueIds }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .testTag("issue_detail_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SophisticatedSurface,
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Title, Issue #, Status & Close Dialog Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Parent breadcrumb if linked
                        if (issue.parentIssueNumber != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.SubdirectoryArrowRight,
                                    contentDescription = null,
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "上層任務 #${issue.parentIssueNumber} ${issue.parentIssueTitle ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LavenderPrimary,
                                    maxLines = 1,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "#${issue.issueNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LavenderPrimary
                            )
                            IssueStatusBadge(status = issue.status)
                            IssuePriorityBadge(priority = issue.priority)
                            if (isCurrentlyBlocked) {
                                BlockedIndicatorBadge(blockedByCount = openBlockersCount)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉", tint = TextMediumEmphasis)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Blocked Warning Banner (if blocked by unresolved prerequisite issues)
                    if (isCurrentlyBlocked) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RoseDark,
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = RoseError, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "Work Blocked by $openBlockersCount Prerequisite Issue(s)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RoseError
                                    )
                                    Text(
                                        text = "Resolve prerequisite blocking tasks before executing or closing this issue.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMediumEmphasis,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Author & Meta Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(LavenderContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = issue.authorDisplayName.take(1).uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LavenderGlow
                                        )
                                    }
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = issue.authorDisplayName,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = TextHighEmphasis
                                            )
                                            RoleBadge(roleName = issue.authorRole)
                                        }
                                        Text(
                                            text = "Created on ${SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault()).format(Date(issue.createdAt))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMediumEmphasis,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = issue.description.ifBlank { "尚未提供詳細說明。" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextHighEmphasis
                            )

                            // Labels
                            if (issue.labels.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    issue.labels.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { label ->
                                        Box(
                                            modifier = Modifier
                                                .background(SophisticatedContainer, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LavenderGlow,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Linked Blueprint / Artifact
                            if (issue.linkedArtifactTitle != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SophisticatedContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            issue.linkedArtifactId?.let { onSelectArtifact?.invoke(it) }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = PinkAccent, modifier = Modifier.size(16.dp))
                                        Column {
                                            Text("已連結無程式碼藍圖", style = MaterialTheme.typography.labelSmall, color = PinkAccent, fontSize = 10.sp)
                                            Text(issue.linkedArtifactTitle, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextHighEmphasis)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 1: HIERARCHY & PARENT ISSUE MANAGEMENT
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "任務階層與子任務",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextHighEmphasis
                                    )
                                }

                                // Parent linking selector button
                                Box {
                                    OutlinedButton(
                                        onClick = { showParentMenu = true },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        border = BorderStroke(1.dp, SophisticatedBorder),
                                        modifier = Modifier.testTag("set_parent_issue_button")
                                    ) {
                                        Icon(
                                            imageVector = if (issue.parentIssueId != null) Icons.Default.Link else Icons.Default.AddLink,
                                            contentDescription = null,
                                            tint = LavenderPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (issue.parentIssueNumber != null) "上層：#${issue.parentIssueNumber}" else "設定上層",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextHighEmphasis,
                                            fontSize = 11.sp
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showParentMenu,
                                        onDismissRequest = { showParentMenu = false },
                                        modifier = Modifier.background(SophisticatedSurfaceDark)
                                    ) {
                                        if (issue.parentIssueId != null) {
                                            DropdownMenuItem(
                                                leadingIcon = { Icon(Icons.Default.LinkOff, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp)) },
                                                text = { Text("解除上層關聯（設為根任務）", color = RoseError) },
                                                onClick = {
                                                    showParentMenu = false
                                                    onLinkParent(null)
                                                }
                                            )
                                        }
                                        if (eligibleParents.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("沒有可用的其他上層任務", color = TextLowEmphasis) },
                                                onClick = { showParentMenu = false }
                                            )
                                        } else {
                                            eligibleParents.forEach { parent ->
                                                DropdownMenuItem(
                                                    leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },
                                                    text = { Text("#${parent.issueNumber} ${parent.title}", color = TextHighEmphasis) },
                                                    onClick = {
                                                        showParentMenu = false
                                                        onLinkParent(parent.id)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Sub-issues breakdown within dialog
                            if (subIssues.isNotEmpty()) {
                                val totalSub = subIssues.size
                                val closedSub = subIssues.count { it.status == IssueStatus.CLOSED }
                                val progress = closedSub.toFloat() / totalSub.toFloat()

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$closedSub / $totalSub 子任務已完成",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMediumEmphasis,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (progress == 1f) EmeraldSuccess else LavenderPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (progress == 1f) EmeraldSuccess else LavenderPrimary,
                                    trackColor = SophisticatedContainer,
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    subIssues.forEach { sub ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SophisticatedContainer, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (sub.status == IssueStatus.CLOSED) Icons.Default.CheckCircle else Icons.Default.SubdirectoryArrowRight,
                                                    contentDescription = null,
                                                    tint = if (sub.status == IssueStatus.CLOSED) EmeraldSuccess else LavenderPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "#${sub.issueNumber} ${sub.title}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (sub.status == IssueStatus.CLOSED) TextMediumEmphasis else TextHighEmphasis,
                                                    maxLines = 1,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            IssueStatusBadge(status = sub.status)
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No sub-issues linked yet. Break this task down into tracked sub-components.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumEmphasis,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = onAddSubIssue,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("add_sub_issue_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("新增子任務", style = MaterialTheme.typography.labelSmall, color = LavenderPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 2: ISSUE DEPENDENCIES (BLOCKED BY & BLOCKING)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "相依與阻擋",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextHighEmphasis
                                    )
                                }

                                // Add blocker button
                                Box {
                                    OutlinedButton(
                                        onClick = { showAddBlockerMenu = true },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        border = BorderStroke(1.dp, SophisticatedBorder),
                                        modifier = Modifier.testTag("add_issue_blocker_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("新增前置任務", style = MaterialTheme.typography.labelSmall, color = TextHighEmphasis, fontSize = 11.sp)
                                    }

                                    DropdownMenu(
                                        expanded = showAddBlockerMenu,
                                        onDismissRequest = { showAddBlockerMenu = false },
                                        modifier = Modifier.background(SophisticatedSurfaceDark)
                                    ) {
                                        if (eligibleBlockers.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("沒有可設為前置任務的項目", color = TextLowEmphasis) },
                                                onClick = { showAddBlockerMenu = false }
                                            )
                                        } else {
                                            Text(
                                                text = "  SELECT BLOCKING PREREQUISITE",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = RoseError,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                            eligibleBlockers.forEach { blk ->
                                                DropdownMenuItem(
                                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoseError, modifier = Modifier.size(14.dp)) },
                                                    text = { Text("#${blk.issueNumber} ${blk.title}", color = TextHighEmphasis) },
                                                    onClick = {
                                                        showAddBlockerMenu = false
                                                        onAddDependency(blk.id)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Blocked By list
                            if (blockedByDependencies.isNotEmpty()) {
                                Text(
                                    text = "受以下前置任務阻擋：",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RoseError,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    blockedByDependencies.forEach { (dep, blockingIssue) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SophisticatedContainer, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (blockingIssue.status == IssueStatus.CLOSED) Icons.Default.LockOpen else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = if (blockingIssue.status == IssueStatus.CLOSED) EmeraldSuccess else RoseError,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "#${blockingIssue.issueNumber} ${blockingIssue.title}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextHighEmphasis,
                                                    maxLines = 1,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                IssueStatusBadge(status = blockingIssue.status)
                                                IconButton(
                                                    onClick = { onRemoveDependency(dep.id) },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.RemoveCircleOutline,
                                                        contentDescription = "移除相依",
                                                        tint = RoseError,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Blocking others list
                            if (blockingDependencies.isNotEmpty()) {
                                Text(
                                    text = "BLOCKING (Blocks Downstream):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberGlow,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    blockingDependencies.forEach { (dep, blockedIssue) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SophisticatedContainer, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Default.SubdirectoryArrowRight,
                                                    contentDescription = null,
                                                    tint = AmberGlow,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "#${blockedIssue.issueNumber} ${blockedIssue.title}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextHighEmphasis,
                                                    maxLines = 1,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            IssueStatusBadge(status = blockedIssue.status)
                                        }
                                    }
                                }
                            }

                            if (blockedByDependencies.isEmpty() && blockingDependencies.isEmpty()) {
                                Text(
                                    text = "No blocking dependencies attached to this issue.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumEmphasis,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Assignee & State Transitions Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Assignee management
                        Box {
                            OutlinedButton(
                                onClick = { showAssignMenu = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextHighEmphasis
                                ),
                                border = BorderStroke(1.dp, SophisticatedBorder),
                                modifier = Modifier.testTag("reassign_issue_button")
                            ) {
                                Icon(
                                    imageVector = if (issue.assigneeType == GranteeType.TEAM) Icons.Default.Groups else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = issue.assigneeName?.let { "Assigned: $it" } ?: "Unassigned (Click to Assign)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 12.sp
                                )
                            }

                            DropdownMenu(
                                expanded = showAssignMenu,
                                onDismissRequest = { showAssignMenu = false },
                                modifier = Modifier.background(SophisticatedSurfaceDark)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("未指派", color = TextMediumEmphasis) },
                                    onClick = {
                                        showAssignMenu = false
                                        onAssignIssue(null, null, null)
                                    }
                                )

                                Text(
                                    text = "  TEAMS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                allTeams.forEach { team ->
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp)) },
                                        text = { Text(team.name, color = TextHighEmphasis) },
                                        onClick = {
                                            showAssignMenu = false
                                            onAssignIssue(GranteeType.TEAM, team.id, team.name)
                                        }
                                    )
                                }

                                Text(
                                    text = "  USERS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                allUsers.forEach { u ->
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },
                                        text = { Text("${u.displayName} (${u.title})", color = TextHighEmphasis) },
                                        onClick = {
                                            showAssignMenu = false
                                            onAssignIssue(GranteeType.USER, u.id, u.displayName)
                                        }
                                    )
                                }
                            }
                        }

                        // Status transitions
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (issue.status != IssueStatus.IN_PROGRESS && issue.status != IssueStatus.CLOSED) {
                                Button(
                                    onClick = { onUpdateStatus(IssueStatus.IN_PROGRESS) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF422E10),
                                        contentColor = AmberGlow
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("start_issue_button")
                                ) {
                                    Text("Start Work", fontSize = 12.sp)
                                }
                            }

                            if (issue.status != IssueStatus.CLOSED) {
                                Button(
                                    onClick = { onUpdateStatus(IssueStatus.CLOSED) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SophisticatedContainer,
                                        contentColor = LavenderGlow
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("close_issue_button")
                                ) {
                                    Text("Close Issue", fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { onUpdateStatus(IssueStatus.OPEN) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldDark,
                                        contentColor = EmeraldSuccess
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("reopen_issue_button")
                                ) {
                                    Text("Reopen Issue", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Discussion / Comments Thread
                    Text(
                        text = "Activity & Comments (${comments.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (comments.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SophisticatedSurfaceDark,
                            border = BorderStroke(1.dp, SophisticatedBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "尚無留言，請在下方開始討論。",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMediumEmphasis,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            comments.forEach { comment ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SophisticatedSurfaceDark,
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = comment.authorDisplayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = TextHighEmphasis
                                                )
                                                RoleBadge(roleName = comment.authorRole)
                                            }

                                            Text(
                                                text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(comment.createdAt)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMediumEmphasis,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = comment.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextHighEmphasis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Add Comment Input Box
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("issue_comment_input"),
                        placeholder = { Text("留下留言或治理備註…", color = TextLowEmphasis, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SophisticatedSurfaceDark,
                            unfocusedContainerColor = SophisticatedSurfaceDark,
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedTextColor = TextHighEmphasis,
                            unfocusedTextColor = TextHighEmphasis
                        ),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText.trim())
                                newCommentText = ""
                            }
                        },
                        enabled = newCommentText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_issue_comment_button")
                    ) {
                        Text("回覆", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Modal to create a new Issue or Sub-issue with optional parent linkage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueDialog(
    repo: Repository,
    initialParentIssue: RepoIssue? = null,
    allRepoIssues: List<RepoIssue> = emptyList(),
    allUsers: List<User>,
    allTeams: List<Team>,
    repoArtifacts: List<NoCodeArtifact>,
    onDismiss: () -> Unit,
    onCreate: (
        title: String,
        desc: String,
        priority: IssuePriority,
        assigneeType: GranteeType?,
        assigneeId: String?,
        assigneeName: String?,
        linkedArtifactId: String?,
        linkedArtifactTitle: String?,
        parentIssueId: String?,
        labels: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(IssuePriority.MEDIUM) }
    var labels by remember { mutableStateOf("") }

    var selectedParentId by remember { mutableStateOf<String?>(initialParentIssue?.id) }
    var selectedParentTitle by remember { mutableStateOf<String?>(initialParentIssue?.let { "#${it.issueNumber} ${it.title}" }) }

    var assigneeType by remember { mutableStateOf<GranteeType?>(null) }
    var assigneeId by remember { mutableStateOf<String?>(null) }
    var assigneeName by remember { mutableStateOf<String?>(null) }

    var linkedArtifactId by remember { mutableStateOf<String?>(null) }
    var linkedArtifactTitle by remember { mutableStateOf<String?>(null) }

    var showParentDropdown by remember { mutableStateOf(false) }
    var showAssignDropdown by remember { mutableStateOf(false) }
    var showArtifactDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .testTag("create_issue_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SophisticatedSurface,
            border = BorderStroke(1.dp, SophisticatedBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedParentId != null) "新增子任務" else "新增治理任務",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis
                        )
                        Text(
                            text = "儲存庫：${repo.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LavenderPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉", tint = TextMediumEmphasis)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Parent Issue link
                Text("上層任務（階層拆解）", style = MaterialTheme.typography.labelMedium, color = TextMediumEmphasis)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showParentDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SophisticatedSurfaceDark,
                            contentColor = TextHighEmphasis
                        ),
                        border = BorderStroke(1.dp, SophisticatedBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedParentTitle ?: "無（最上層根任務）",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedParentTitle != null) LavenderGlow else TextLowEmphasis,
                                maxLines = 1
                            )
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary)
                        }
                    }

                    DropdownMenu(
                        expanded = showParentDropdown,
                        onDismissRequest = { showParentDropdown = false },
                        modifier = Modifier.background(SophisticatedSurfaceDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("無（最上層根任務）", color = TextMediumEmphasis) },
                            onClick = {
                                selectedParentId = null
                                selectedParentTitle = null
                                showParentDropdown = false
                            }
                        )
                        IssueHierarchyRules.orderedForDisplay(allRepoIssues).forEach { (parent, depth) ->
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },
                                text = { Text("${"· ".repeat(depth)}#${parent.issueNumber} ${parent.title}", color = TextHighEmphasis) },
                                onClick = {
                                    selectedParentId = parent.id
                                    selectedParentTitle = "#${parent.issueNumber} ${parent.title}"
                                    showParentDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任務標題 *") },
                    placeholder = { Text("e.g., Update KYC schema for Tier-3 approvals") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_issue_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis,
                        focusedLabelColor = LavenderPrimary,
                        unfocusedLabelColor = TextMediumEmphasis
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("說明與脈絡") },
                    placeholder = { Text("描述任務、治理規則或問題…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("new_issue_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis,
                        focusedLabelColor = LavenderPrimary,
                        unfocusedLabelColor = TextMediumEmphasis
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Selection
                Text("優先級", style = MaterialTheme.typography.labelMedium, color = TextMediumEmphasis)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IssuePriority.values().forEach { p ->
                        val isSelected = priority == p
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) LavenderPrimary else SophisticatedBorder
                            )
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) LavenderGlow else TextMediumEmphasis,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Assignee Selection
                Text("受派者（使用者或團隊）", style = MaterialTheme.typography.labelMedium, color = TextMediumEmphasis)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showAssignDropdown = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_issue_assignee_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SophisticatedSurfaceDark,
                            contentColor = TextHighEmphasis
                        ),
                        border = BorderStroke(1.dp, SophisticatedBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = assigneeName ?: "未指派（選擇使用者或團隊）",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (assigneeName != null) TextHighEmphasis else TextLowEmphasis
                            )
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMediumEmphasis)
                        }
                    }

                    DropdownMenu(
                        expanded = showAssignDropdown,
                        onDismissRequest = { showAssignDropdown = false },
                        modifier = Modifier.background(SophisticatedSurfaceDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("無／未指派", color = TextMediumEmphasis) },
                            onClick = {
                                assigneeType = null
                                assigneeId = null
                                assigneeName = null
                                showAssignDropdown = false
                            }
                        )

                        Text(
                            text = "  TEAMS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LavenderPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        allTeams.forEach { team ->
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp)) },
                                text = { Text(team.name, color = TextHighEmphasis) },
                                onClick = {
                                    assigneeType = GranteeType.TEAM
                                    assigneeId = team.id
                                    assigneeName = team.name
                                    showAssignDropdown = false
                                }
                            )
                        }

                        Text(
                            text = "  USERS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LavenderPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        allUsers.forEach { u ->
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp)) },
                                text = { Text("${u.displayName} (${u.title})", color = TextHighEmphasis) },
                                onClick = {
                                    assigneeType = GranteeType.USER
                                    assigneeId = u.id
                                    assigneeName = u.displayName
                                    showAssignDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Link to Blueprint / Spec
                if (repoArtifacts.isNotEmpty()) {
                    Text("連結無程式碼藍圖（選填）", style = MaterialTheme.typography.labelMedium, color = TextMediumEmphasis)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showArtifactDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SophisticatedSurfaceDark,
                                contentColor = TextHighEmphasis
                            ),
                            border = BorderStroke(1.dp, SophisticatedBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = linkedArtifactTitle ?: "無（選擇成果）",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (linkedArtifactTitle != null) PinkAccent else TextLowEmphasis
                                )
                                Icon(Icons.Default.Description, contentDescription = null, tint = PinkAccent)
                            }
                        }

                        DropdownMenu(
                            expanded = showArtifactDropdown,
                            onDismissRequest = { showArtifactDropdown = false },
                            modifier = Modifier.background(SophisticatedSurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("無", color = TextMediumEmphasis) },
                                onClick = {
                                    linkedArtifactId = null
                                    linkedArtifactTitle = null
                                    showArtifactDropdown = false
                                }
                            )
                            repoArtifacts.forEach { art ->
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = PinkAccent, modifier = Modifier.size(16.dp)) },
                                    text = { Text(art.title, color = TextHighEmphasis) },
                                    onClick = {
                                        linkedArtifactId = art.id
                                        linkedArtifactTitle = art.title
                                        showArtifactDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Labels
                OutlinedTextField(
                    value = labels,
                    onValueChange = { labels = it },
                    label = { Text("Labels (comma-separated)") },
                    placeholder = { Text("e.g. compliance, security, bug, sla") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_issue_labels_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis,
                        focusedLabelColor = LavenderPrimary,
                        unfocusedLabelColor = TextMediumEmphasis
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Submit / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = TextMediumEmphasis)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(
                                    title.trim(),
                                    description.trim(),
                                    priority,
                                    assigneeType,
                                    assigneeId,
                                    assigneeName,
                                    linkedArtifactId,
                                    linkedArtifactTitle,
                                    selectedParentId,
                                    labels.trim()
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_create_issue_button")
                    ) {
                        Text("Submit Issue")
                    }
                }
            }
        }
    }
}
