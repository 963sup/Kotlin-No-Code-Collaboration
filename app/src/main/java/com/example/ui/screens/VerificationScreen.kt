package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RepoIssue
import com.example.data.model.WorkEvidence
import com.example.data.model.WorkVerification
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    evidenceList: List<WorkEvidence>,
    verifications: List<WorkVerification>,
    onVerifySubmit: (Boolean, String) -> Unit,
    issue: RepoIssue,
    onBack: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    var comments by remember { mutableStateOf("") }
    val tabs = listOf("Evidence", "描述", "活動")

    // 4 Verification criteria state
    var dataCompleteness by remember { mutableStateOf(true) }
    var methodologyCompliance by remember { mutableStateOf(true) }
    var conclusionConsistency by remember { mutableStateOf(true) }
    var riskImpactLevel by remember { mutableStateOf("低") } // 低, 中, 高

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "驗證 (獨立驗證)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("verification_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextHighEmphasis)
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
            // Header Info Bar
            Surface(
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "#${issue.issueNumber} ${issue.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "提交人: 王小明 05-16 10:30",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMediumEmphasis,
                    )
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (selectedTab) {
                    0 -> { // Evidence Tab
                        // Evidence Previews
                        item {
                            Text(
                                "成果 / 證據 (Evidence)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SophisticatedContainer,
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier.size(92.dp, 76.dp),
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
                                        Text("現場高程相片", fontSize = 10.sp, color = TextMediumEmphasis)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SophisticatedContainer,
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier.size(92.dp, 76.dp),
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
                                        Text("沉降測量報告", fontSize = 10.sp, color = TextMediumEmphasis)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SophisticatedContainer,
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier.size(60.dp, 76.dp),
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

                        // Verification Checklist
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "驗證摘要 (4 項核驗標準)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SophisticatedSurfaceDark,
                                border = BorderStroke(1.dp, SophisticatedBorder),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    InteractiveVerificationCheckItem(
                                        label = "資料完整性",
                                        isChecked = dataCompleteness,
                                        onToggle = { dataCompleteness = it },
                                    )
                                    HorizontalDivider(
                                        color = SophisticatedBorderSubtle,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                    InteractiveVerificationCheckItem(
                                        label = "方法合規性",
                                        isChecked = methodologyCompliance,
                                        onToggle = { methodologyCompliance = it },
                                    )
                                    HorizontalDivider(
                                        color = SophisticatedBorderSubtle,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                    InteractiveVerificationCheckItem(
                                        label = "結論一致性",
                                        isChecked = conclusionConsistency,
                                        onToggle = { conclusionConsistency = it },
                                    )
                                    HorizontalDivider(
                                        color = SophisticatedBorderSubtle,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            "風險與影響",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextHighEmphasis,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        listOf("低", "中", "高").forEach { level ->
                                            val isSelected = riskImpactLevel == level
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) LavenderPrimary else SophisticatedContainer,
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .clickable { riskImpactLevel = level },
                                            ) {
                                                Text(
                                                    text = level,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                    color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Action Buttons: Pass (Accept) / Fail (Reject)
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "驗證結論",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Button(
                                    onClick = { onVerifySubmit(true, comments) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("verify_accept_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("通過 (Accept)", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onVerifySubmit(false, comments) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("verify_reject_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("不通過 (Reject)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Feedback input
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "意見與建議 (可選)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = comments,
                                onValueChange = { comments = it },
                                placeholder = { Text("輸入你的意見或退回修改說明...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LavenderPrimary,
                                    unfocusedBorderColor = SophisticatedBorder,
                                    focusedTextColor = TextHighEmphasis,
                                    unfocusedTextColor = TextHighEmphasis,
                                ),
                            )
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
                                        "工作項目目標與驗證要求",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextHighEmphasis,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = issue.description.ifEmpty {
                                            "現場測量點高程誤差需在 ±3mm 內，並完成地質鑽探數據交叉比對與結構沉降模擬預測。"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMediumEmphasis,
                                        lineHeight = 22.sp,
                                    )
                                }
                            }
                        }
                    }

                    2 -> { // 活動 Tab
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ActivityLogItem(
                                    "王小明",
                                    "提交了現場測量數據報告",
                                    "05-16 10:30",
                                    Icons.Default.CloudUpload,
                                    EmeraldSuccess,
                                )
                                ActivityLogItem(
                                    "李佳穎",
                                    "指定王小明執行現場測量",
                                    "05-10 09:00",
                                    Icons.Default.Person,
                                    LavenderPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveVerificationCheckItem(label: String, isChecked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isChecked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextHighEmphasis)
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isChecked) EmeraldDark else RoseDark,
            border = BorderStroke(1.dp, if (isChecked) EmeraldSuccess else RoseError),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Icon(
                    imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isChecked) EmeraldSuccess else RoseError,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isChecked) "合規" else "待補",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) EmeraldSuccess else RoseError,
                )
            }
        }
    }
}
