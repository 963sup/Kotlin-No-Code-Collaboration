package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HomeRecentActivitySection(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "最近活動",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextHighEmphasis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ActivityRow(
                    icon = Icons.Default.TaskAlt,
                    iconBg = LavenderContainer,
                    iconTint = LavenderPrimary,
                    title = "製造線優化專案",
                    subtitle = "#128 Issue 已更新",
                    time = "2 小時前",
                )
                HorizontalDivider(color = SophisticatedBorder)
                ActivityRow(
                    icon = Icons.Default.Description,
                    iconBg = AmberGlow,
                    iconTint = AmberWarning,
                    title = "設備檢修管理系統",
                    subtitle = "WBS 30% → 45%",
                    time = "3 小時前",
                )
                HorizontalDivider(color = SophisticatedBorder)
                ActivityRow(
                    icon = Icons.Default.CheckCircle,
                    iconBg = EmeraldDark,
                    iconTint = EmeraldSuccess,
                    title = "客服流程優化專案",
                    subtitle = "Issue #56 已驗證",
                    time = "5 小時前",
                )
            }
        }
    }
}

@Composable
private fun ActivityRow(
    icon: ImageVector,
    iconBg: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    time: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
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
        Text(
            text = time,
            fontSize = 11.sp,
            color = TextLowEmphasis,
        )
    }
}
