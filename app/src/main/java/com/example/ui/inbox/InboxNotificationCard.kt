package com.example.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationStatus
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis

private val IconRed = Color(0xFFDA3633)
private val IconPurple = Color(0xFF8957E5)
private val IconGreen = Color(0xFF238636)
private val IconBlue = Color(0xFF1F6FEB)
private val UnreadCyanDot = Color(0xFF58A6FF)
private val BadgeBg = Color(0xFF1E3A5F)
private val BadgeText = Color(0xFF58A6FF)

@Composable
fun InboxNotificationCard(
    notification: AppNotification,
    onNotificationClick: (AppNotification) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUnread = notification.status == NotificationStatus.UNREAD
    val relativeTime = formatRelativeTime(notification.createdAt)
    val visuals = getNotificationVisuals(notification)
    val badgeCount = getBadgeCount(notification)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SophisticatedSurface)
            .clickable { onNotificationClick(notification) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Left icon column with unread dot
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(visuals.backgroundColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                }

                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(UnreadCyanDot),
                    )
                } else {
                    Spacer(modifier = Modifier.size(7.dp))
                }
            }

            // Main Content column
            Column(
                modifier = Modifier.weight(1f),
            ) {
                // Header row: repo/target + relative time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = notification.repoName ?: "Governance Platform",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = relativeTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLowEmphasis,
                        fontSize = 12.sp,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title row
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle row with mini avatar and reason / description
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Mini actor avatar / bot icon
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF30363D)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (notification.actorDisplayName.contains("bot", ignoreCase = true) ||
                                notification.actorDisplayName.contains("Actions", ignoreCase = true)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color(0xFFF0883E),
                                    modifier = Modifier.size(11.dp),
                                )
                            } else {
                                Text(
                                    text = notification.actorDisplayName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = notification.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (badgeCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BadgeBg)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                color = BadgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color = SophisticatedBorder,
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 62.dp),
        )
    }
}

private data class NotificationVisualState(
    val icon: ImageVector,
    val backgroundColor: Color,
)

private fun getNotificationVisuals(notification: AppNotification): NotificationVisualState {
    val title = notification.title.lowercase()
    return when {
        title.contains("failed") || title.contains("failure") || title.contains("error") ->
            NotificationVisualState(Icons.Default.Close, IconRed)
        title.contains("bump") || title.contains("deps") || title.contains("fix(auth)") ->
            NotificationVisualState(Icons.Default.AltRoute, IconRed)
        title.contains("feat") || title.contains("merge") || title.contains("chore") || title.contains("fix(security)") ->
            NotificationVisualState(Icons.Default.MergeType, IconPurple)
        notification.category == NotificationCategory.APPROVAL_GATE ||
            notification.category == NotificationCategory.REVIEW_REQUEST ||
            title.contains("驗證") || title.contains("approval") ->
            NotificationVisualState(Icons.Default.VerifiedUser, IconGreen)
        notification.category == NotificationCategory.MENTION_AND_REPLY ||
            title.contains("回覆") || title.contains("mention") || title.contains("disc") ->
            NotificationVisualState(Icons.Default.Chat, IconBlue)
        else ->
            NotificationVisualState(Icons.Default.Notifications, IconBlue)
    }
}

private fun getBadgeCount(notification: AppNotification): Int {
    val title = notification.title.lowercase()
    return when {
        title.contains("okhttp") -> 2
        title.contains("chore:") || title.contains("fix(security)") || title.contains("fix(auth)") -> 1
        else -> 0
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = (diff / 1000).coerceAtLeast(1)
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "now"
    }
}
