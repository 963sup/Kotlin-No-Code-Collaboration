package com.example.ui.work

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RepoIssue
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkItemDetailSheet(
    issue: RepoIssue,
    onDismiss: () -> Unit,
    onSubmitEvidenceForVerification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("任務", "描述", "附件", "活動")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SophisticatedSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            // Header: #128 + Title + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${issue.issueNumber} ${issue.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    color = LavenderContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "進行中",
                        color = LavenderPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meta Info Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "負責人: 王小明", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
                        Text(text = "截止日: 2024-05-20", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "來源: WBS-3.2", style = MaterialTheme.typography.bodySmall, color = LavenderPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "優先級: 高", style = MaterialTheme.typography.bodySmall, color = RoseError, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        selectedContentColor = LavenderPrimary,
                        unselectedContentColor = TextMediumEmphasis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checklist & Evidence
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChecklistItem(title = "現場測量與數據收集", isChecked = true)
                ChecklistItem(title = "根因分析與土質建模", isChecked = true)
                ChecklistItem(title = "提出優化與加固方案", isChecked = false)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Evidence Photos
            Text(text = "Evidence (成果 / 證據)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextHighEmphasis)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = SophisticatedSurfaceDark,
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(60.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = LavenderPrimary)
                    }
                }
                Surface(
                    color = SophisticatedSurfaceDark,
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(60.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = EmeraldSuccess)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Action Button
            Button(
                onClick = onSubmitEvidenceForVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = "提交 Evidence 等待驗證", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ChecklistItem(title: String, isChecked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isChecked) EmeraldSuccess else TextLowEmphasis,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isChecked) TextMediumEmphasis else TextHighEmphasis,
        )
    }
}
