package com.example.ui.repository

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Repository
import com.example.ui.theme.*

@Composable
fun RepositoryOverviewTab(
    repository: Repository,
    onNavigateTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "WBS 總體進度",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                    )
                    Text(
                        text = "60%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { 0.6f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = LavenderPrimary,
                    trackColor = LavenderContainer,
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SophisticatedBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    MetricCol(label = "WBS 任務", count = "128")
                    MetricCol(label = "Issue 數", count = "36")
                    MetricCol(label = "成員", count = "18")
                }
            }
        }

        // 4 Module Entry Cards
        Text(
            text = "核心協作模組",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextHighEmphasis,
        )

        ModuleCard(
            icon = Icons.Default.AccountTree,
            iconBg = EmeraldDark,
            iconTint = EmeraldSuccess,
            title = "WBS 工作樹",
            subtitle = "分析結構與階層進度",
            onClick = { onNavigateTab(1) },
        )

        ModuleCard(
            icon = Icons.Default.FormatListBulleted,
            iconBg = RoseDark,
            iconTint = RoseError,
            title = "Issue 清單",
            subtitle = "問題追蹤與待辦清單",
            onClick = { onNavigateTab(2) },
        )

        ModuleCard(
            icon = Icons.Default.ViewKanban,
            iconBg = AmberGlow,
            iconTint = AmberWarning,
            title = "Kanban 看板",
            subtitle = "即時狀態與任務調度",
            onClick = { onNavigateTab(1) },
        )

        ModuleCard(
            icon = Icons.Default.Description,
            iconBg = LavenderContainer,
            iconTint = LavenderPrimary,
            title = "文件 / 成果物",
            subtitle = "知識資產與實證歸檔",
            onClick = { onNavigateTab(4) },
        )
    }
}

@Composable
private fun MetricCol(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextHighEmphasis)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = TextMediumEmphasis)
    }
}

@Composable
private fun ModuleCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMediumEmphasis,
            )
        }
    }
}
