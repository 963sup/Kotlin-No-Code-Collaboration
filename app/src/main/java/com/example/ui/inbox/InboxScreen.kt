package com.example.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.ui.theme.*

@Composable
fun InboxScreen(
    notifications: List<AppNotification>,
    onNotificationClick: (AppNotification) -> Unit,
    onQuickAction: ((AppNotification) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("全部 12", "待處理 6", "@我 3", "系統 3")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg),
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SophisticatedSurface,
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
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    selectedContentColor = LavenderPrimary,
                    unselectedContentColor = TextMediumEmphasis,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "今天",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextMediumEmphasis,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            items(notifications.take(3)) { notification ->
                InboxNotificationCard(
                    notification = notification,
                    onNotificationClick = onNotificationClick,
                    onQuickAction = onQuickAction,
                )
            }

            if (notifications.size > 3) {
                item {
                    Text(
                        text = "昨天",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextMediumEmphasis,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }

                items(notifications.drop(3)) { notification ->
                    InboxNotificationCard(
                        notification = notification,
                        onNotificationClick = onNotificationClick,
                        onQuickAction = onQuickAction,
                    )
                }
            }
        }
    }
}
