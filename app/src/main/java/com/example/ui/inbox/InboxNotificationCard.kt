package com.example.ui.inbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.ui.theme.*

@Composable
fun InboxNotificationCard(
    notification: AppNotification,
    onNotificationClick: (AppNotification) -> Unit,
    onQuickAction: ((AppNotification) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNotificationClick(notification) },
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val (icon, iconBg, iconTint) = getNotificationVisuals(notification)

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                    )
                    Text(
                        text = "10:30",
                        fontSize = 11.sp,
                        color = TextLowEmphasis,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )

                if (notification.isActionable && onQuickAction != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(
                            onClick = { onQuickAction(notification) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text(text = "前往處理", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun getNotificationVisuals(notification: AppNotification): Triple<ImageVector, Color, Color> {
    return when {
        notification.title.contains("驗證") || notification.category.name.contains("VERIFICATION") ->
            Triple(Icons.Default.VerifiedUser, EmeraldDark, EmeraldSuccess)
        notification.title.contains("處理") || notification.category.name.contains("ASSIGN") ->
            Triple(Icons.Default.Assignment, LavenderContainer, LavenderPrimary)
        notification.title.contains("回覆") || notification.category.name.contains("DISCUSSION") ->
            Triple(Icons.Default.Chat, AmberGlow, AmberWarning)
        else ->
            Triple(Icons.Default.Notifications, SophisticatedSurfaceDark, TextMediumEmphasis)
    }
}
