package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.MainNavigationTab
import com.example.data.model.Repository
import com.example.data.model.User
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis

// GitHub official signature iconography colors
private val GitHubGreen = Color(0xFF238636)
private val GitHubBlue = Color(0xFF1F6FEB)
private val GitHubPurple = Color(0xFF8957E5)
private val GitHubSlate = Color(0xFF30363D)
private val GitHubOrange = Color(0xFFDB6D28)
private val GitHubYellow = Color(0xFFE3B341)
private val GitHubMagenta = Color(0xFFDB61A2)
private val GitHubIndigo = Color(0xFF7057FF)

data class MyWorkItemConfig(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconBg: Color,
    val testTag: String,
)

private val DefaultMyWorkItems = listOf(
    MyWorkItemConfig("issues", "Issues", Icons.Default.Adjust, GitHubGreen, "home_work_item_issues"),
    MyWorkItemConfig("reviews", "Reviews", Icons.Default.RateReview, GitHubBlue, "home_work_item_reviews"),
    MyWorkItemConfig("discussions", "Discussions", Icons.Default.ChatBubble, GitHubPurple, "home_work_item_discussions"),
    MyWorkItemConfig("projects", "Projects", Icons.Default.ViewWeek, GitHubSlate, "home_work_item_projects"),
    MyWorkItemConfig("repositories", "Top Repositories", Icons.Default.Bookmark, GitHubSlate, "home_work_item_repositories"),
    MyWorkItemConfig("organizations", "Organizations", Icons.Default.Apartment, GitHubOrange, "home_work_item_organizations"),
    MyWorkItemConfig("starred", "Starred", Icons.Default.Star, GitHubYellow, "home_work_item_starred"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repositories: List<Repository>,
    activeUser: User?,
    onSelectRepository: (Repository) -> Unit,
    onNavigateToTab: (MainNavigationTab) -> Unit,
    onOpenPersonaSwitcher: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onSyncRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var showSearchBottomSheet by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showShortcutsDialog by remember { mutableStateOf(false) }
    var showEditMyWorkScreen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val favoriteRepoIds = remember { mutableStateListOf<String>() }
    val orderedMyWorkKeys = remember {
        mutableStateListOf("issues", "reviews", "discussions", "projects", "repositories", "organizations", "starred")
    }
    val visibleMyWorkKeys = remember {
        mutableStateListOf("issues", "reviews", "discussions", "projects", "repositories", "organizations", "starred")
    }
    val activeShortcutKeys = remember {
        mutableStateListOf("issues", "reviews", "discussions", "artifacts")
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val favoriteRepos = repositories.filter { it.id in favoriteRepoIds }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SophisticatedBg),
        ) {
            // Dedicated Home Header (Only present on Home screen)
            HomeTopBar(
                activeUser = activeUser,
                onSearchClick = { showSearchBottomSheet = true },
                onRefreshClick = onSyncRefresh,
                onAddClick = { showAddBottomSheet = true },
                onPersonaClick = onOpenProfile,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Section 1: My Work (Customizable via ...)
                item {
                    HomeMyWorkSection(
                        orderedKeys = orderedMyWorkKeys,
                        visibleKeys = visibleMyWorkKeys,
                        onEditMyWorkClick = { showEditMyWorkScreen = true },
                        onNavigateToIssues = { onNavigateToTab(MainNavigationTab.WORK) },
                        onNavigateToReviews = { onNavigateToTab(MainNavigationTab.WORK) },
                        onNavigateToDiscussions = {
                            if (repositories.isNotEmpty()) {
                                onSelectRepository(repositories.first())
                            } else {
                                onNavigateToTab(MainNavigationTab.EXPLORE)
                            }
                        },
                        onNavigateToProjects = { onNavigateToTab(MainNavigationTab.WORK) },
                        onNavigateToRepositories = { onNavigateToTab(MainNavigationTab.EXPLORE) },
                        onNavigateToOrganizations = { onNavigateToTab(MainNavigationTab.PROFILE) },
                        onNavigateToStarred = { onNavigateToTab(MainNavigationTab.EXPLORE) },
                    )
                }

                // Divider
                item {
                    HorizontalDivider(
                        color = SophisticatedBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                // Section 2: Favorites
                item {
                    HomeFavoritesSection(
                        favoriteRepos = favoriteRepos,
                        onAddFavoritesClick = { showFavoritesDialog = true },
                        onSelectRepository = onSelectRepository,
                    )
                }

                // Divider
                item {
                    HorizontalDivider(
                        color = SophisticatedBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                // Section 3: Shortcuts (As shown in screenshot)
                item {
                    HomeShortcutsSection(
                        activeShortcutKeys = activeShortcutKeys,
                        onGetStartedClick = { showShortcutsDialog = true },
                        onNavigateToIssues = { onNavigateToTab(MainNavigationTab.WORK) },
                        onNavigateToReviews = { onNavigateToTab(MainNavigationTab.WORK) },
                        onNavigateToDiscussions = {
                            if (repositories.isNotEmpty()) {
                                onSelectRepository(repositories.first())
                            } else {
                                onNavigateToTab(MainNavigationTab.EXPLORE)
                            }
                        },
                        onNavigateToArtifacts = {
                            if (repositories.isNotEmpty()) {
                                onSelectRepository(repositories.first())
                            } else {
                                onNavigateToTab(MainNavigationTab.EXPLORE)
                            }
                        },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Full Screen "Edit My Work" Dialog / Overlay (Matching screenshot)
        if (showEditMyWorkScreen) {
            EditMyWorkDialog(
                currentOrderedKeys = orderedMyWorkKeys,
                currentVisibleKeys = visibleMyWorkKeys,
                onSave = { updatedOrderedKeys, updatedVisibleKeys ->
                    orderedMyWorkKeys.clear()
                    orderedMyWorkKeys.addAll(updatedOrderedKeys)
                    visibleMyWorkKeys.clear()
                    visibleMyWorkKeys.addAll(updatedVisibleKeys)
                    showEditMyWorkScreen = false
                },
                onDismiss = { showEditMyWorkScreen = false },
            )
        }
    }

    // Quick Add Menu BottomSheet
    if (showAddBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = SophisticatedSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "建立新項目",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    IconButton(onClick = { showAddBottomSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "關閉", tint = TextMediumEmphasis)
                    }
                }

                HomeQuickAddOption(
                    title = "新增任務 (New Issue)",
                    subtitle = "在工作清單中指派協作任務與驗證規則",
                    icon = Icons.Default.Adjust,
                    iconBg = GitHubGreen,
                    onClick = {
                        showAddBottomSheet = false
                        onNavigateToTab(MainNavigationTab.WORK)
                    },
                )

                HomeQuickAddOption(
                    title = "發起審批簽核 (New Review Request)",
                    subtitle = "提交無程式碼成果物或規格決策供跨部門審核",
                    icon = Icons.Default.RateReview,
                    iconBg = GitHubBlue,
                    onClick = {
                        showAddBottomSheet = false
                        onNavigateToTab(MainNavigationTab.WORK)
                    },
                )

                HomeQuickAddOption(
                    title = "發起協作討論 (New Discussion)",
                    subtitle = "開啟技術問答、方案評估與團隊回饋",
                    icon = Icons.Default.QuestionAnswer,
                    iconBg = GitHubPurple,
                    onClick = {
                        showAddBottomSheet = false
                        if (repositories.isNotEmpty()) onSelectRepository(repositories.first())
                    },
                )

                HomeQuickAddOption(
                    title = "建立無程式碼成果物 (New Artifact)",
                    subtitle = "撰寫規格書、決策記錄或發布治理流程",
                    icon = Icons.Default.Description,
                    iconBg = GitHubSlate,
                    onClick = {
                        showAddBottomSheet = false
                        if (repositories.isNotEmpty()) onSelectRepository(repositories.first())
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Quick Search BottomSheet
    if (showSearchBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = SophisticatedSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜尋倉庫、成果物或任務...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LavenderPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "清除", tint = TextMediumEmphasis)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                    ),
                    singleLine = true,
                )

                val filtered = repositories.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "輸入關鍵字即時檢索" else "查無相關倉庫",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLowEmphasis,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filtered) { repo ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSearchBottomSheet = false
                                        onSelectRepository(repo)
                                    },
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                                border = BorderStroke(1.dp, SophisticatedBorder),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = LavenderPrimary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = repo.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextHighEmphasis,
                                        )
                                        Text(
                                            text = repo.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMediumEmphasis,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Favorites Selector Dialog
    if (showFavoritesDialog) {
        ModalBottomSheet(
            onDismissRequest = { showFavoritesDialog = false },
            sheetState = bottomSheetState,
            containerColor = SophisticatedSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "選擇要釘選的常用倉庫",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(repositories) { repo ->
                        val isFavorited = repo.id in favoriteRepoIds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isFavorited) {
                                        favoriteRepoIds.remove(repo.id)
                                    } else {
                                        favoriteRepoIds.add(repo.id)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFavorited) LavenderContainer else SophisticatedSurfaceDark,
                            ),
                            border = BorderStroke(1.dp, if (isFavorited) LavenderPrimary else SophisticatedBorder),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = repo.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isFavorited) LavenderPrimary else TextHighEmphasis,
                                    )
                                    Text(
                                        text = repo.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMediumEmphasis,
                                        maxLines = 1,
                                    )
                                }
                                Icon(
                                    imageVector = if (isFavorited) Icons.Default.Star else Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = if (isFavorited) GitHubYellow else TextLowEmphasis,
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { showFavoritesDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                ) {
                    Text("完成", color = LavenderOnPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Shortcuts Manager Dialog
    if (showShortcutsDialog) {
        ModalBottomSheet(
            onDismissRequest = { showShortcutsDialog = false },
            sheetState = bottomSheetState,
            containerColor = SophisticatedSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "自訂 Shortcuts 快速捷徑",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Text(
                    text = "勾選常用快捷項目，一鍵直達清單與工作篩選視圖",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )

                val availableShortcuts = listOf(
                    Triple("issues", "Issues (待辦任務)", GitHubGreen),
                    Triple("reviews", "Reviews (審批簽核)", GitHubBlue),
                    Triple("discussions", "Discussions (協作討論)", GitHubPurple),
                    Triple("artifacts", "Artifacts (無程式碼成果物)", GitHubIndigo),
                    Triple("starred", "Starred (星標收藏)", GitHubYellow),
                    Triple("projects", "Projects (專案看板)", GitHubSlate),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(availableShortcuts) { (key, label, color) ->
                        val isEnabled = key in activeShortcutKeys
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isEnabled) {
                                        activeShortcutKeys.remove(key)
                                    } else {
                                        activeShortcutKeys.add(key)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEnabled) LavenderContainer else SophisticatedSurfaceDark,
                            ),
                            border = BorderStroke(1.dp, if (isEnabled) LavenderPrimary else SophisticatedBorder),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(color),
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (isEnabled) LavenderPrimary else TextHighEmphasis,
                                    )
                                }
                                Icon(
                                    imageVector = if (isEnabled) Icons.Default.CheckCircleOutline else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isEnabled) LavenderPrimary else TextLowEmphasis,
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { showShortcutsDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                ) {
                    Text("儲存捷徑設定", color = LavenderOnPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    activeUser: User?,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onAddClick: () -> Unit,
    onPersonaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Large Bold Title: "Home"
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            ),
            color = TextHighEmphasis,
        )

        // Actions: Search, Refresh, Add (+), Persona Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("home_search_btn"),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextHighEmphasis,
                    modifier = Modifier.size(24.dp),
                )
            }

            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier.testTag("home_refresh_btn"),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = TextHighEmphasis,
                    modifier = Modifier.size(24.dp),
                )
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier.testTag("home_add_btn"),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = TextHighEmphasis,
                    modifier = Modifier.size(24.dp),
                )
            }

            // User Avatar Pill
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(LavenderPrimary)
                    .border(1.5.dp, SophisticatedBorder, CircleShape)
                    .clickable { onPersonaClick() }
                    .testTag("home_persona_btn"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = activeUser?.displayName?.take(1) ?: "U",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = LavenderOnPrimary,
                )
            }
        }
    }
}

@Composable
private fun HomeMyWorkSection(
    orderedKeys: List<String>,
    visibleKeys: List<String>,
    onEditMyWorkClick: () -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToDiscussions: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToRepositories: () -> Unit,
    onNavigateToOrganizations: () -> Unit,
    onNavigateToStarred: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Section Header Row with "..." Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "My Work",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis,
            )
            IconButton(
                onClick = onEditMyWorkClick,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("home_edit_my_work_btn"),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Edit My Work",
                    tint = TextMediumEmphasis,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        // Dynamically rendered and ordered items according to user customization
        orderedKeys.forEach { key ->
            if (key in visibleKeys) {
                val item = DefaultMyWorkItems.find { it.id == key }
                if (item != null) {
                    HomeWorkRowItem(
                        title = item.title,
                        icon = item.icon,
                        iconBg = item.iconBg,
                        testTag = item.testTag,
                        onClick = when (item.id) {
                            "issues" -> onNavigateToIssues
                            "reviews" -> onNavigateToReviews
                            "discussions" -> onNavigateToDiscussions
                            "projects" -> onNavigateToProjects
                            "repositories" -> onNavigateToRepositories
                            "organizations" -> onNavigateToOrganizations
                            "starred" -> onNavigateToStarred
                            else -> onNavigateToIssues
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMyWorkDialog(
    currentOrderedKeys: List<String>,
    currentVisibleKeys: List<String>,
    onSave: (List<String>, List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val orderedKeys = remember { mutableStateListOf<String>().apply { addAll(currentOrderedKeys) } }
    val selectedKeys = remember { mutableStateListOf<String>().apply { addAll(currentVisibleKeys) } }
    var showMenu by remember { mutableStateOf(false) }
    var showReorderActions by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SophisticatedBg,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar exactly matching screenshot
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("edit_my_work_back_btn"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextHighEmphasis,
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Edit My Work",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                        )
                    },
                    actions = {
                        TextButton(
                            onClick = { onSave(orderedKeys.toList(), selectedKeys.toList()) },
                            modifier = Modifier.testTag("edit_my_work_save_btn"),
                        ) {
                            Text(
                                text = "SAVE",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = LavenderPrimary,
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("edit_my_work_more_btn"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = TextMediumEmphasis,
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(SophisticatedSurfaceDark),
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (showReorderActions) "Hide reorder actions" else "Show reorder actions",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextHighEmphasis,
                                        )
                                    },
                                    onClick = {
                                        showReorderActions = !showReorderActions
                                        showMenu = false
                                    },
                                    modifier = Modifier.testTag("edit_my_work_toggle_reorder_btn"),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SophisticatedBg,
                        titleContentColor = TextHighEmphasis,
                    ),
                )

                // List of customizable work items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    itemsIndexed(orderedKeys, key = { _, key -> key }) { index, key ->
                        val item = DefaultMyWorkItems.find { it.id == key }
                        if (item != null) {
                            val isChecked = item.id in selectedKeys

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isChecked) {
                                            selectedKeys.remove(item.id)
                                        } else {
                                            selectedKeys.add(item.id)
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    // Checkbox
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (item.id !in selectedKeys) selectedKeys.add(item.id)
                                            } else {
                                                selectedKeys.remove(item.id)
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = LavenderPrimary,
                                            uncheckedColor = SophisticatedBorder,
                                            checkmarkColor = LavenderOnPrimary,
                                        ),
                                        modifier = Modifier.size(24.dp),
                                    )

                                    // Rounded square icon
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(item.iconBg),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }

                                    // Title
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp,
                                        ),
                                        color = TextHighEmphasis,
                                    )
                                }

                                if (showReorderActions) {
                                    // Up and Down chevron reorder actions (Image 2)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (index > 0) {
                                                    val temp = orderedKeys[index]
                                                    orderedKeys[index] = orderedKeys[index - 1]
                                                    orderedKeys[index - 1] = temp
                                                }
                                            },
                                            enabled = index > 0,
                                            modifier = Modifier.size(36.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Move Up",
                                                tint = if (index > 0) TextMediumEmphasis else TextLowEmphasis.copy(alpha = 0.25f),
                                                modifier = Modifier.size(26.dp),
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (index < orderedKeys.size - 1) {
                                                    val temp = orderedKeys[index]
                                                    orderedKeys[index] = orderedKeys[index + 1]
                                                    orderedKeys[index + 1] = temp
                                                }
                                            },
                                            enabled = index < orderedKeys.size - 1,
                                            modifier = Modifier.size(36.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Move Down",
                                                tint = if (index < orderedKeys.size - 1) TextMediumEmphasis else TextLowEmphasis.copy(alpha = 0.25f),
                                                modifier = Modifier.size(26.dp),
                                            )
                                        }
                                    }
                                } else {
                                    // 6-dot Drag handle on the right
                                    Icon(
                                        imageVector = Icons.Default.DragIndicator,
                                        contentDescription = "Drag to reorder",
                                        tint = TextLowEmphasis,
                                        modifier = Modifier.size(22.dp),
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

@Composable
private fun HomeWorkRowItem(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Rounded square colorful icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                ),
                color = TextHighEmphasis,
            )
        }
    }
}

@Composable
private fun HomeFavoritesSection(
    favoriteRepos: List<Repository>,
    onAddFavoritesClick: () -> Unit,
    onSelectRepository: (Repository) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextHighEmphasis,
        )

        if (favoriteRepos.isEmpty()) {
            // Empty State Card (Exact layout matching GitHub Mobile official design)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                border = BorderStroke(1.dp, SophisticatedBorder),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Add favorite repositories for quick access at any time, without having to search",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMediumEmphasis,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Button(
                        onClick = onAddFavoritesClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_add_favorites_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SophisticatedContainer,
                            contentColor = LavenderPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "ADD FAVORITES",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                            ),
                        )
                    }
                }
            }
        } else {
            // Populated State with favorite cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                favoriteRepos.forEach { repo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRepository(repo) },
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GitHubSlate),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Column {
                                    Text(
                                        text = repo.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextHighEmphasis,
                                    )
                                    Text(
                                        text = repo.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMediumEmphasis,
                                        maxLines = 1,
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GitHubYellow,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onAddFavoritesClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("+ 編輯常用清單", color = LavenderPrimary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun HomeShortcutsSection(
    activeShortcutKeys: List<String>,
    onGetStartedClick: () -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToDiscussions: () -> Unit,
    onNavigateToArtifacts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Shortcuts",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextHighEmphasis,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Circular Badges Row from screenshot
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShortcutBadgeIcon(icon = Icons.Default.FlashOn, bg = Color(0xFF21262D))
                    ShortcutBadgeIcon(icon = Icons.Default.Adjust, bg = GitHubGreen)
                    ShortcutBadgeIcon(icon = Icons.Default.RateReview, bg = GitHubBlue)
                    ShortcutBadgeIcon(icon = Icons.Default.ChatBubble, bg = GitHubPurple)
                    ShortcutBadgeIcon(icon = Icons.Default.Apartment, bg = GitHubOrange)
                    ShortcutBadgeIcon(icon = Icons.Default.Groups, bg = GitHubMagenta)
                    ShortcutBadgeIcon(icon = Icons.Default.WorkOutline, bg = GitHubIndigo)
                    ShortcutBadgeIcon(icon = Icons.Default.Description, bg = Color(0xFF30363D))
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "The things you need, one tap away",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = TextHighEmphasis,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Fast access your lists of Issues, Reviews, or Discussions",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMediumEmphasis,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_shortcuts_get_started_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedContainer,
                        contentColor = LavenderPrimary,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "GET STARTED",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortcutBadgeIcon(
    icon: ImageVector,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
private fun HomeQuickAddOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = SophisticatedSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SophisticatedBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            }
        }
    }
}
