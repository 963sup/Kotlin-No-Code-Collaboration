package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkItemDetailScreen(
    evidenceList: List<WorkEvidence>,
    checklist: List<TaskChecklist>,
    onToggleChecklist: (String, Boolean) -> Unit,
    onAddChecklistItem: (String) -> Unit = {},
    onAddEvidence: (String) -> Unit = {},
    issue: RepoIssue,
    activeUser: User?,
    onBack: () -> Unit,
    onNavigateToVerification: (String) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("任務", "描述", "附件", "活動")
    var isStarred by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var showAddEvidenceDialog by remember { mutableStateOf(false) }
    var newEvidenceDesc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "#${issue.issueNumber} ${issue.title}",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("work_item_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextHighEmphasis)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isStarred = !isStarred },
                        modifier = Modifier.testTag("work_item_star_btn"),
                    ) {
                        Icon(
                            imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "收藏",
                            tint = if (isStarred) AmberWarning else LavenderPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SophisticatedSurfaceDark),
            )
        },
        containerColor = SophisticatedBg,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // Status and Metadata Grid
            Surface(
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Status Badge Pill
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (statusBg, statusFg, statusText) = when (issue.status) {
                            IssueStatus.OPEN -> Triple(LavenderContainer, LavenderPrimary, "待處理")

                            IssueStatus.IN_PROGRESS -> Triple(
                                LavenderPrimary.copy(alpha = 0.2f),
                                LavenderPrimary,
                                "進行中",
                            )

                            IssueStatus.CLOSED -> Triple(EmeraldDark, EmeraldSuccess, "已完成")

                            else -> Triple(AmberGlow, AmberWarning, issue.status.label)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusBg,
                            border = BorderStroke(1.dp, statusFg.copy(alpha = 0.5f)),
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = statusFg,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 1: 負責人 & 截止日
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("負責人", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(LavenderPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        (issue.assigneeName ?: "王").take(1),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LavenderPrimary,
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    issue.assigneeName ?: "王小明",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextHighEmphasis,
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("截止日", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                            Text(
                                if (issue.plannedEndAt != null) "2024-05-20" else "2024-05-20",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextHighEmphasis,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: 來源 & 優先級
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("來源", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                            Text(
                                issue.parentIssueTitle ?: "WBS-3.2",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextHighEmphasis,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("優先級", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RoseDark,
                                    border = BorderStroke(1.dp, RoseError.copy(alpha = 0.6f)),
                                ) {
                                    Text(
                                        text = issue.priority.label.ifEmpty { "高" },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = RoseError,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SophisticatedSurfaceDark,
                contentColor = LavenderPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LavenderPrimary,
                    )
                },
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        selectedContentColor = LavenderPrimary,
                        unselectedContentColor = TextMediumEmphasis,
                    )
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (selectedTab) {
                    0 -> { // 任務 Tab
                        item {
                            Text(
                                "任務清單",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (checklist.isEmpty()) {
                                // Default preview tasks if empty
                                TaskItem(
                                    title = "現場測量與數據收集",
                                    isCompleted = true,
                                    assignee = "王小明",
                                    date = "05-10",
                                    onToggle = {},
                                )
                                TaskItem(
                                    title = "根因分析",
                                    isCompleted = true,
                                    assignee = "李佳穎",
                                    date = "05-12",
                                    onToggle = {},
                                )
                                TaskItem(
                                    title = "提出優化方案",
                                    isCompleted = false,
                                    assignee = "張小華",
                                    date = "05-18",
                                    onToggle = {},
                                )
                            } else {
                                for (task in checklist) {
                                    TaskItem(
                                        title = task.title,
                                        isCompleted = task.isCompleted,
                                        assignee = task.completedByDisplayName ?: "未指派",
                                        date = if (task.completedAt != null) "已完成" else "進行中",
                                        onToggle = { onToggleChecklist(task.id, it) },
                                    )
                                }
                            }

                            TextButton(
                                onClick = { showAddTaskDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = LavenderPrimary),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("＋ 新增任務", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Evidence (成果 / 證據) Section
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Evidence (成果 / 證據)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Mockup evidence thumbnails
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SophisticatedContainer,
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier.size(88.dp, 72.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null,
                                            tint = LavenderPrimary,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("現場照片", fontSize = 10.sp, color = TextMediumEmphasis)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SophisticatedContainer,
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier.size(88.dp, 72.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Icon(
                                            Icons.Default.Assessment,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("數據報告", fontSize = 10.sp, color = TextMediumEmphasis)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SophisticatedContainer,
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier
                                        .size(56.dp, 72.dp)
                                        .clickable { showAddEvidenceDialog = true },
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            "＋3",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = LavenderPrimary,
                                        )
                                    }
                                }
                            }
                        }

                        // Next Action Card (下一步行動)
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "下一步行動",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToVerification(issue.id) }
                                    .testTag("work_item_next_action_btn"),
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldDark),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "提交 Evidence 等待驗證",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = TextHighEmphasis,
                                        )
                                        Text(
                                            "進入獨立驗證關卡評估品質與合規",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMediumEmphasis,
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = LavenderPrimary,
                                    )
                                }
                            }
                        }
                    }

                    1 -> { // 描述 Tab
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                border = BorderStroke(1.dp, SophisticatedBorder),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "工作說明與目標",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextHighEmphasis,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = issue.description.ifEmpty {
                                            "依據現場檢查數據完成基底沉降原因分析，並提出結構補強與優化方案，附帶完整測量數據與成本估算報告。"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMediumEmphasis,
                                        lineHeight = 22.sp,
                                    )
                                }
                            }
                        }
                    }

                    2 -> { // 附件 Tab
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                AttachmentRowItem("現場高程測量表_20240510.xlsx", "2.4 MB • 測量組")
                                AttachmentRowItem("基底沉降成因分析簡報_v1.pdf", "8.1 MB • 李佳穎")
                                AttachmentRowItem("結構加固工法比較.docx", "1.2 MB • 張小華")
                            }
                        }
                    }

                    3 -> { // 活動 Tab
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ActivityLogItem(
                                    "王小明",
                                    "提交了現場測量數據報告與照片",
                                    "2 小時前",
                                    Icons.Default.CloudUpload,
                                    EmeraldSuccess,
                                )
                                ActivityLogItem("李佳穎", "更新了任務: 根因分析完成", "5 小時前", Icons.Default.TaskAlt, LavenderPrimary)
                                ActivityLogItem(
                                    "系統",
                                    "指派工作項目給 王小明，截止日設為 2024-05-20",
                                    "1 天前",
                                    Icons.Default.AssignmentInd,
                                    CyanAccent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "新增任務檢查項",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("例如: 方案設計審查會議") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                        ),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddTaskDialog = false }) { Text("取消", color = TextMediumEmphasis) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    onAddChecklistItem(newTaskTitle)
                                    newTaskTitle = ""
                                    showAddTaskDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                        ) {
                            Text("新增", color = LavenderOnPrimary)
                        }
                    }
                }
            }
        }
    }

    // Add Evidence Dialog
    if (showAddEvidenceDialog) {
        Dialog(onDismissRequest = { showAddEvidenceDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "上傳 / 附加 Evidence 成果物",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newEvidenceDesc,
                        onValueChange = { newEvidenceDesc = it },
                        placeholder = { Text("說明成果內容、驗收標準或成果物摘要...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                        ),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { showAddEvidenceDialog = false },
                        ) { Text("取消", color = TextMediumEmphasis) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newEvidenceDesc.isNotBlank()) {
                                    onAddEvidence(newEvidenceDesc)
                                    newEvidenceDesc = ""
                                    showAddEvidenceDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                        ) {
                            Text("提交 Evidence", color = LavenderOnPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(title: String, isCompleted: Boolean, assignee: String, date: String, onToggle: (Boolean) -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SophisticatedContainer,
        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle(!isCompleted) },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isCompleted) EmeraldSuccess else TextMediumEmphasis,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isCompleted) TextMediumEmphasis else TextHighEmphasis,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                "$assignee  $date",
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumEmphasis,
            )
        }
    }
}

@Composable
fun AttachmentRowItem(name: String, sizeAndAuthor: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SophisticatedSurfaceDark,
        border = BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LavenderPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextHighEmphasis,
                    maxLines = 1,
                )
                Text(sizeAndAuthor, style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
            }
            Icon(
                Icons.Default.FileDownload,
                contentDescription = "下載",
                tint = TextMediumEmphasis,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun ActivityLogItem(
    author: String,
    text: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    author,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Text(time, style = MaterialTheme.typography.labelSmall, color = TextLowEmphasis)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
        }
    }
}
