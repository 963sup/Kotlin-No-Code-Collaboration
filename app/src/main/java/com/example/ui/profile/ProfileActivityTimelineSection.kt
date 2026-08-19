package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FileUpload
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
fun ProfileActivityTimelineSection(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "操作留痕時間軸",
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
                TimelineItem(
                    icon = Icons.Default.FileUpload,
                    iconTint = LavenderPrimary,
                    title = "提交了 Evidence 實證附件",
                    subtitle = "在 #128 基底沉降問題優化",
                    time = "今天 10:30",
                )
                HorizontalDivider(color = SophisticatedBorder)
                TimelineItem(
                    icon = Icons.Default.AssignmentTurnedIn,
                    iconTint = EmeraldSuccess,
                    title = "通過了獨立驗證",
                    subtitle = "在 #110 現場檢查完成",
                    time = "昨天 18:30",
                )
                HorizontalDivider(color = SophisticatedBorder)
                TimelineItem(
                    icon = Icons.Default.Comment,
                    iconTint = AmberWarning,
                    title = "回覆了協作討論",
                    subtitle = "在 方案設計評估工作群",
                    time = "昨天 14:15",
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    icon: ImageVector,
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
                    .background(SophisticatedSurfaceDark, CircleShape),
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
