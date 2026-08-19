package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.*

@Composable
fun ProfileHeaderSection(
    user: User?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(LavenderPrimary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = user?.displayName?.take(1) ?: "A",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.displayName ?: "王小明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user?.title?.ifBlank { "資深架構與專案負責人" } ?: "資深架構與專案負責人",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SophisticatedBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                StatItem(label = "參與專案", value = "8")
                StatItem(label = "完成任務", value = "24")
                StatItem(label = "已提交實證", value = "15")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = LavenderPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMediumEmphasis,
        )
    }
}
