package com.example.ui.work

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IssueStatus
import com.example.data.model.RepoIssue
import com.example.ui.theme.*

enum class WorkProjectionTab(val label: String) {
    WBS("WBS 視圖"),
    KANBAN("Kanban 視圖"),
    ISSUE("Issue 視圖"),
}

@Composable
fun WorkScreen(
    issues: List<RepoIssue>,
    onUpdateIssueStatus: (String, IssueStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedProjection by remember { mutableStateOf(WorkProjectionTab.WBS) }
    var selectedIssueForDetail by remember { mutableStateOf<RepoIssue?>(null) }
    var selectedIssueForVerification by remember { mutableStateOf<RepoIssue?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg),
    ) {
        // Top Projection Switcher
        TabRow(
            selectedTabIndex = selectedProjection.ordinal,
            containerColor = SophisticatedSurface,
            contentColor = LavenderPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedProjection.ordinal]),
                    color = LavenderPrimary,
                )
            },
        ) {
            WorkProjectionTab.values().forEach { tab ->
                Tab(
                    selected = selectedProjection == tab,
                    onClick = { selectedProjection = tab },
                    text = {
                        Text(
                            text = tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (selectedProjection == tab) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    selectedContentColor = LavenderPrimary,
                    unselectedContentColor = TextMediumEmphasis,
                )
            }
        }

        // Active Projection Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedProjection) {
                WorkProjectionTab.WBS -> {
                    WbsTreeView(
                        issues = issues,
                        onSelectIssue = { selectedIssueForDetail = it },
                    )
                }
                WorkProjectionTab.KANBAN -> {
                    KanbanBoardView(
                        issues = issues,
                        onSelectIssue = { selectedIssueForDetail = it },
                    )
                }
                WorkProjectionTab.ISSUE -> {
                    IssueListView(
                        issues = issues,
                        onSelectIssue = { selectedIssueForDetail = it },
                    )
                }
            }
        }

        // Work Item Detail Sheet (Screen 4)
        selectedIssueForDetail?.let { issue ->
            WorkItemDetailSheet(
                issue = issue,
                onDismiss = { selectedIssueForDetail = null },
                onSubmitEvidenceForVerification = {
                    val current = selectedIssueForDetail
                    selectedIssueForDetail = null
                    selectedIssueForVerification = current
                },
            )
        }

        // Evidence Verification Dialog (Screen 7)
        selectedIssueForVerification?.let { issue ->
            EvidenceVerificationDialog(
                issue = issue,
                onDismiss = { selectedIssueForVerification = null },
                onAccept = {
                    onUpdateIssueStatus(issue.id, IssueStatus.CLOSED)
                    selectedIssueForVerification = null
                },
                onReject = {
                    onUpdateIssueStatus(issue.id, IssueStatus.IN_PROGRESS)
                    selectedIssueForVerification = null
                },
            )
        }
    }
}
