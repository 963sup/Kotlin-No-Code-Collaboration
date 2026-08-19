package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ProfileHonorBadgesSection(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "本月榮譽",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextHighEmphasis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BadgeCard(
                icon = Icons.Default.EmojiEvents,
                iconColor = AmberWarning,
                iconBg = AmberGlow,
                title = "積極參與者",
                count = "3",
                modifier = Modifier.weight(1f),
            )
            BadgeCard(
                icon = Icons.Default.Star,
                iconColor = LavenderPrimary,
                iconBg = LavenderContainer,
                title = "優質貢獻",
                count = "3",
                modifier = Modifier.weight(1f),
            )
            BadgeCard(
                icon = Icons.Default.Verified,
                iconColor = EmeraldSuccess,
                iconBg = EmeraldDark,
                title = "驗證專家",
                count = "3",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BadgeCard(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    title: String,
    count: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    tint = iconColor,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${count}次獲獎",
                fontSize = 11.sp,
                color = TextMediumEmphasis,
            )
        }
    }
}
