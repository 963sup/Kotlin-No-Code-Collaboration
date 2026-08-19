package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoCodeArtifact
import com.example.data.model.Organization
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.SavedTarget
import com.example.data.model.Team
import com.example.data.model.User
import com.example.navigation.CollaborationTarget
import com.example.navigation.storageKey
import com.example.ui.model.ExploreCategory
import com.example.ui.model.ExploreProjection
import com.example.ui.model.matches
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedBorderSubtle
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis

enum class ExploreMainTab(val title: String) {
    ACTIVITY("活動記錄"),
    SEARCH("搜索"),
    HONOR("榮譽"),
}

enum class ActivityFilterChip(val label: String) {
    ALL("全部"),
    IMPORTANT("重要"),
    MENTIONED("@我"),
}

data class ActivityFeedItem(
    val id: String,
    val actorName: String,
    val actorAvatarLetter: String,
    val avatarColor: Color,
    val actionText: String,
    val targetTitle: String,
    val timeText: String,
    val isImportant: Boolean = false,
    val isMentioned: Boolean = false,
    val icon: ImageVector,
    val iconTint: Color,
)

data class HonorBadge(
    val id: String,
    val title: String,
    val count: Int,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val containerColor: Color,
)

@Composable
fun UnifiedExploreScreen(
    activeUser: User?,
    repositories: List<Repository>,
    artifacts: List<NoCodeArtifact>,
    issues: List<RepoIssue>,
    discussions: List<RepoDiscussion>,
    organizations: List<Organization>,
    teams: List<Team>,
    users: List<User>,
    savedTargets: List<SavedTarget>,
    onOpenTarget: (CollaborationTarget) -> Unit,
    onToggleSaved: (CollaborationTarget) -> Unit,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = ExploreMainTab.entries

    // Activity Feed filter
    var selectedActivityFilter by rememberSaveable { mutableStateOf(ActivityFilterChip.ALL) }

    // Search query & category
    var query by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf(ExploreCategory.ALL.name) }
    val selectedCategory = ExploreCategory.valueOf(categoryName)

    val results = remember(activeUser, repositories, artifacts, issues, discussions, organizations, teams, users, savedTargets) {
        ExploreProjection.build(activeUser, repositories, artifacts, issues, discussions, organizations, teams, users, savedTargets)
    }
    val visibleSearchResults = remember(results, query, selectedCategory) {
        val normalizedQuery = query.trim()
        results.filter { result ->
            val matchesCategory = result.matches(selectedCategory)
            val matchesQuery = normalizedQuery.isBlank() ||
                result.title.contains(normalizedQuery, ignoreCase = true) ||
                result.subtitle.contains(normalizedQuery, ignoreCase = true) ||
                result.searchableText.contains(normalizedQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val sampleActivities = remember {
        listOf(
            ActivityFeedItem(
                id = "act_01",
                actorName = "王小明",
                actorAvatarLetter = "王",
                avatarColor = LavenderPrimary,
                actionText = "提交了 Evidence",
                targetTitle = "#128 基座沉降問題優化",
                timeText = "10:30",
                isImportant = true,
                isMentioned = true,
                icon = Icons.Default.AssignmentTurnedIn,
                iconTint = LavenderPrimary,
            ),
            ActivityFeedItem(
                id = "act_02",
                actorName = "李佳穎",
                actorAvatarLetter = "李",
                avatarColor = EmeraldSuccess,
                actionText = "通過了驗證",
                targetTitle = "#110 現場檢查完成",
                timeText = "09:40",
                isImportant = true,
                isMentioned = false,
                icon = Icons.Default.Verified,
                iconTint = EmeraldSuccess,
            ),
            ActivityFeedItem(
                id = "act_03",
                actorName = "張小華",
                actorAvatarLetter = "張",
                avatarColor = CyanAccent,
                actionText = "更新了任務",
                targetTitle = "#131 優化方案設計",
                timeText = "09:15",
                isImportant = false,
                isMentioned = true,
                icon = Icons.Default.Edit,
                iconTint = CyanAccent,
            ),
            ActivityFeedItem(
                id = "act_04",
                actorName = "系統",
                actorAvatarLetter = "系",
                avatarColor = AmberWarning,
                actionText = "更新狀態為 進行中",
                targetTitle = "#128 基座沉降問題優化",
                timeText = "08:20",
                isImportant = false,
                isMentioned = false,
                icon = Icons.Default.Sync,
                iconTint = AmberWarning,
            ),
            ActivityFeedItem(
                id = "act_05",
                actorName = "陳志強",
                actorAvatarLetter = "陳",
                avatarColor = PinkAccent,
                actionText = "新增了討論回覆",
                targetTitle = "討論: #128 優化方案",
                timeText = "昨天 16:45",
                isImportant = false,
                isMentioned = true,
                icon = Icons.AutoMirrored.Filled.Comment,
                iconTint = PinkAccent,
            ),
        )
    }

    val sampleHonors = remember {
        listOf(
            HonorBadge(
                id = "honor_01",
                title = "積極參與者",
                count = 3,
                description = "本月累積完成與更新 15 項以上工作項目",
                icon = Icons.Default.EmojiEvents,
                iconColor = AmberWarning,
                containerColor = AmberWarning.copy(alpha = 0.15f),
            ),
            HonorBadge(
                id = "honor_02",
                title = "優質貢獻",
                count = 3,
                description = "成果證據一次性通過核驗且獲評優良",
                icon = Icons.Default.WorkspacePremium,
                iconColor = LavenderPrimary,
                containerColor = LavenderPrimary.copy(alpha = 0.15f),
            ),
            HonorBadge(
                id = "honor_03",
                title = "驗證專家",
                count = 3,
                description = "嚴格執行 4 項核驗標準，提供專業回饋",
                icon = Icons.Default.MilitaryTech,
                iconColor = CyanAccent,
                containerColor = CyanAccent.copy(alpha = 0.15f),
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBg),
    ) {
        // Header
        Surface(
            color = SophisticatedSurfaceDark,
            border = BorderStroke(1.dp, SophisticatedBorderSubtle),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "活動記錄",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                        )
                        Text(
                            text = "動態與榮譽",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LavenderPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = LavenderPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Main Tabs: 活動記錄 / 搜索 / 榮譽
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SophisticatedSurfaceDark,
                    contentColor = LavenderPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = LavenderPrimary,
                        )
                    },
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                )
                            },
                            selectedContentColor = LavenderPrimary,
                            unselectedContentColor = TextMediumEmphasis,
                            modifier = Modifier.testTag("explore_tab_${tab.name.lowercase()}"),
                        )
                    }
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            when (tabs[selectedTabIndex]) {
                ExploreMainTab.ACTIVITY -> {
                    val filteredActivities = sampleActivities.filter {
                        when (selectedActivityFilter) {
                            ActivityFilterChip.ALL -> true
                            ActivityFilterChip.IMPORTANT -> it.isImportant
                            ActivityFilterChip.MENTIONED -> it.isMentioned
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Filter Chips: 全部 / 重要 / @我
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                ActivityFilterChip.entries.forEach { chip ->
                                    FilterChip(
                                        selected = selectedActivityFilter == chip,
                                        onClick = { selectedActivityFilter = chip },
                                        label = { Text(chip.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LavenderPrimary,
                                            selectedLabelColor = LavenderOnPrimary,
                                            containerColor = SophisticatedSurfaceDark,
                                            labelColor = TextMediumEmphasis,
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (selectedActivityFilter == chip) LavenderPrimary else SophisticatedBorder,
                                        ),
                                        modifier = Modifier.testTag("activity_filter_${chip.name.lowercase()}"),
                                    )
                                }
                            }
                        }

                        // Feed items
                        items(filteredActivities, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("activity_feed_item_${item.id}"),
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                border = BorderStroke(1.dp, SophisticatedBorder),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Avatar Circle
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(item.avatarColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = item.actorAvatarLetter,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = item.avatarColor,
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "${item.actorName} ${item.actionText}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = TextHighEmphasis,
                                            )
                                            Text(
                                                text = item.timeText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMediumEmphasis,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SophisticatedContainer,
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = null,
                                                    tint = item.iconTint,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = item.targetTitle,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Medium,
                                                    ),
                                                    color = TextHighEmphasis,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ExploreMainTab.SEARCH -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("explore_search"),
                                placeholder = { Text("搜尋儲存庫、工作、成果、討論、團隊與用戶...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = TextMediumEmphasis,
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LavenderPrimary,
                                    unfocusedBorderColor = SophisticatedBorder,
                                    focusedContainerColor = SophisticatedSurfaceDark,
                                    unfocusedContainerColor = SophisticatedSurfaceDark,
                                    focusedTextColor = TextHighEmphasis,
                                    unfocusedTextColor = TextHighEmphasis,
                                ),
                            )
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("explore_category_filters"),
                            ) {
                                items(ExploreCategory.entries, key = { it.name }) { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { categoryName = category.name },
                                        label = { Text(category.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LavenderPrimary,
                                            selectedLabelColor = LavenderOnPrimary,
                                            containerColor = SophisticatedSurfaceDark,
                                            labelColor = TextMediumEmphasis,
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (selectedCategory == category) LavenderPrimary else SophisticatedBorder,
                                        ),
                                        modifier = Modifier.testTag("explore_filter_${category.name.lowercase()}"),
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "${visibleSearchResults.size} 個結果",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMediumEmphasis,
                            )
                        }

                        if (visibleSearchResults.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = SophisticatedSurfaceDark,
                                    border = BorderStroke(1.dp, SophisticatedBorderSubtle),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            "沒有符合條件的項目",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextHighEmphasis,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "請嘗試更換關鍵字或類別篩選",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMediumEmphasis,
                                        )
                                    }
                                }
                            }
                        } else {
                            items(visibleSearchResults, key = { it.target.storageKey() }) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenTarget(item.target) }
                                        .testTag("explore_result_${item.target.storageKey().hashCode()}"),
                                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                    border = BorderStroke(1.dp, SophisticatedBorder),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${item.typeLabel} · ${item.title}",
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextHighEmphasis,
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                item.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMediumEmphasis,
                                            )
                                        }
                                        IconButton(
                                            onClick = { onToggleSaved(item.target) },
                                            modifier = Modifier.testTag("explore_save_${item.target.storageKey().hashCode()}"),
                                        ) {
                                            Icon(
                                                imageVector = if (item.isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "收藏",
                                                tint = if (item.isSaved) AmberWarning else LavenderPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ExploreMainTab.HONOR -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SophisticatedSurfaceDark,
                                border = BorderStroke(1.dp, SophisticatedBorder),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "本月獲頒榮譽徽章",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHighEmphasis,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "基於無代碼協同治理貢獻、審核質量與證據真實性核驗頒發",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMediumEmphasis,
                                    )
                                }
                            }
                        }

                        items(sampleHonors, key = { it.id }) { honor ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("honor_card_${honor.id}"),
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                border = BorderStroke(1.dp, SophisticatedBorder),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(honor.containerColor),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = honor.icon,
                                            contentDescription = null,
                                            tint = honor.iconColor,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = honor.title,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = TextHighEmphasis,
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = honor.containerColor,
                                            ) {
                                                Text(
                                                    text = "${honor.count} 枚",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                    color = honor.iconColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = honor.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMediumEmphasis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
