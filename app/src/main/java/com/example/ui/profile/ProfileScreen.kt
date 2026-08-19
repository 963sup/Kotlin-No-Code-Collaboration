package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: User?,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("活動記錄", "搜索", "榮譽")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg),
    ) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ProfileHeaderSection(user = user)
            }
            if (selectedTab == 0 || selectedTab == 2) {
                item {
                    ProfileHonorBadgesSection()
                }
            }
            item {
                ProfileActivityTimelineSection()
            }
        }
    }
}
