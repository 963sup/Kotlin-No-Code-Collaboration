package com.example.ui.inbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationPriority
import com.example.data.model.NotificationStatus
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis

private val SelectedFilterBlue = Color(0xFF1F6FEB)
private val CheckmarkBlue = Color(0xFF58A6FF)
private val BannerRed = Color(0xFFEA4335)
private val BannerConfigureBlue = Color(0xFF58A6FF)
private val BottomSheetBg = Color(0xFF161B22)
private val ReasonIconRed = Color(0xFFEA4335)
private val ReasonIconYellow = Color(0xFFF59E0B)

enum class InboxFilterOption(val label: String) {
    INBOX("Inbox"),
    SAVED("Saved"),
    DONE("Done"),
    ASSIGNED("Assigned"),
    PARTICIPATING("Participating"),
    MENTIONED("Mentioned"),
    TEAM_MENTIONED("Team mentioned"),
    REVIEW_REQUESTED("Review requested"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    notifications: List<AppNotification>,
    onNotificationClick: (AppNotification) -> Unit,
    onMarkAllAsRead: () -> Unit = {},
    onConfigureNotifications: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedFilterOption by remember { mutableStateOf(InboxFilterOption.INBOX) }
    var isFocusedActive by remember { mutableStateOf(false) }
    var isUnreadActive by remember { mutableStateOf(false) }
    var selectedRepoFilter by remember { mutableStateOf<String?>(null) }
    var isBannerDismissed by remember { mutableStateOf(false) }

    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var showRepoBottomSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val repoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calculate active filters count
    val activeFilterCount = (if (isFocusedActive) 1 else 0) +
        (if (isUnreadActive) 1 else 0) +
        (if (selectedRepoFilter != null) 1 else 0) +
        (if (selectedFilterOption != InboxFilterOption.INBOX) 1 else 0)

    // Distinct repositories extracted from notifications
    val distinctRepos = remember(notifications) {
        notifications.mapNotNull { it.repoName }.distinct()
    }

    // Filter notification list
    val filteredNotifications = remember(
        notifications,
        selectedFilterOption,
        isFocusedActive,
        isUnreadActive,
        selectedRepoFilter,
    ) {
        notifications.filter { notification ->
            // 1. Primary filter option
            val matchesOption = when (selectedFilterOption) {
                InboxFilterOption.INBOX -> notification.status != NotificationStatus.ARCHIVED
                InboxFilterOption.SAVED -> false // Sample placeholder for saved items
                InboxFilterOption.DONE -> notification.status == NotificationStatus.READ
                InboxFilterOption.ASSIGNED -> notification.category == NotificationCategory.ISSUE_ASSIGNMENT ||
                    notification.title.contains("Assigned", ignoreCase = true) ||
                    notification.title.contains("處理", ignoreCase = true)
                InboxFilterOption.PARTICIPATING -> notification.category == NotificationCategory.MENTION_AND_REPLY ||
                    notification.category == NotificationCategory.REVIEW_REQUEST ||
                    notification.title.contains("Discussion", ignoreCase = true) ||
                    notification.title.contains("討論", ignoreCase = true)
                InboxFilterOption.MENTIONED -> notification.category == NotificationCategory.MENTION_AND_REPLY ||
                    notification.title.contains("Mention", ignoreCase = true) ||
                    notification.title.contains("@", ignoreCase = true)
                InboxFilterOption.TEAM_MENTIONED -> notification.teamId != null ||
                    notification.teamName != null ||
                    notification.category == NotificationCategory.MEMBERSHIP_CHANGE
                InboxFilterOption.REVIEW_REQUESTED -> notification.category == NotificationCategory.REVIEW_REQUEST ||
                    notification.category == NotificationCategory.APPROVAL_GATE
            }

            // 2. Focused filter
            val matchesFocused = if (isFocusedActive) {
                notification.priority == NotificationPriority.URGENT ||
                    notification.priority == NotificationPriority.HIGH ||
                    notification.category == NotificationCategory.APPROVAL_GATE ||
                    notification.category == NotificationCategory.ISSUE_ASSIGNMENT
            } else {
                true
            }

            // 3. Unread filter
            val matchesUnread = if (isUnreadActive) {
                notification.status == NotificationStatus.UNREAD
            } else {
                true
            }

            // 4. Repository filter
            val matchesRepo = if (selectedRepoFilter != null) {
                notification.repoName == selectedRepoFilter ||
                    notification.repoName?.startsWith(selectedRepoFilter.orEmpty()) == true
            } else {
                true
            }

            matchesOption && matchesFocused && matchesUnread && matchesRepo
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg),
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Inbox",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
                fontSize = 24.sp,
            )

            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = TextHighEmphasis,
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    modifier = Modifier.background(SophisticatedSurface),
                ) {
                    DropdownMenuItem(
                        text = { Text("Mark all as read", color = TextHighEmphasis) },
                        leadingIcon = {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = TextHighEmphasis)
                        },
                        onClick = {
                            showMoreMenu = false
                            onMarkAllAsRead()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Notification settings", color = TextHighEmphasis) },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = TextHighEmphasis)
                        },
                        onClick = {
                            showMoreMenu = false
                            onConfigureNotifications()
                        },
                    )
                }
            }
        }

        // --- Filter Chips Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Filter count indicator (when filters are active)
            if (activeFilterCount > 0) {
                Surface(
                    onClick = {
                        // Reset extra filters
                        isFocusedActive = false
                        isUnreadActive = false
                        selectedRepoFilter = null
                        selectedFilterOption = InboxFilterOption.INBOX
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = SelectedFilterBlue,
                    border = BorderStroke(1.dp, SelectedFilterBlue),
                    modifier = Modifier.height(34.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Active filters",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = activeFilterCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // Dropdown: Inbox ⌵
            InboxFilterChip(
                label = selectedFilterOption.label,
                hasDropdown = true,
                isSelected = selectedFilterOption != InboxFilterOption.INBOX,
                onClick = { showFilterBottomSheet = true },
            )

            // Toggle: Focused
            InboxFilterChip(
                label = "Focused",
                hasDropdown = false,
                isSelected = isFocusedActive,
                onClick = { isFocusedActive = !isFocusedActive },
            )

            // Toggle: Unread
            InboxFilterChip(
                label = "Unread",
                hasDropdown = false,
                isSelected = isUnreadActive,
                onClick = { isUnreadActive = !isUnreadActive },
            )

            // Dropdown: Repository ⌵
            InboxFilterChip(
                label = selectedRepoFilter ?: "Repository",
                hasDropdown = true,
                isSelected = selectedRepoFilter != null,
                onClick = { showRepoBottomSheet = true },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Notification List & Promo Banner ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Dismissible Promo Banner: "Never miss what's important to you."
            if (!isBannerDismissed) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BannerRed),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                IconButton(
                                    onClick = { isBannerDismissed = true },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss banner",
                                        tint = TextMediumEmphasis,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Never miss what's important to you.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                                fontSize = 15.sp,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Configure your Notification experience with push notifications, working hours, and swipe actions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMediumEmphasis,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = onConfigureNotifications,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = "CONFIGURE",
                                        color = BannerConfigureBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.5.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredNotifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = TextLowEmphasis,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No notifications found",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextMediumEmphasis,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing filters to see more activity.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLowEmphasis,
                            )
                        }
                    }
                }
            } else {
                items(filteredNotifications, key = { it.id }) { notification ->
                    InboxNotificationCard(
                        notification = notification,
                        onNotificationClick = onNotificationClick,
                    )
                }
            }
        }
    }

    // --- Modal Bottom Sheet 1: Filter notifications by (Image 2) ---
    if (showFilterBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = filterSheetState,
            containerColor = BottomSheetBg,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                // Header with Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { showFilterBottomSheet = false },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close filter sheet",
                            tint = TextHighEmphasis,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Filter notifications by",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        fontSize = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Standard views (Inbox, Saved, Done)
                FilterSheetRow(
                    title = "Inbox",
                    count = notifications.size.toString(),
                    isSelected = selectedFilterOption == InboxFilterOption.INBOX,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.INBOX
                        showFilterBottomSheet = false
                    },
                )

                FilterSheetRow(
                    title = "Saved",
                    count = null,
                    isSelected = selectedFilterOption == InboxFilterOption.SAVED,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.SAVED
                        showFilterBottomSheet = false
                    },
                )

                FilterSheetRow(
                    title = "Done",
                    count = null,
                    isSelected = selectedFilterOption == InboxFilterOption.DONE,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.DONE
                        showFilterBottomSheet = false
                    },
                )

                HorizontalDivider(
                    color = SophisticatedBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                // Section 2: Reason filters (Assigned, Participating, Mentioned, Team mentioned, Review requested)
                FilterSheetRow(
                    icon = Icons.Default.TrackChanges,
                    iconTint = ReasonIconRed,
                    title = "Assigned",
                    count = null,
                    isSelected = selectedFilterOption == InboxFilterOption.ASSIGNED,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.ASSIGNED
                        showFilterBottomSheet = false
                    },
                )

                FilterSheetRow(
                    icon = Icons.Default.ChatBubbleOutline,
                    iconTint = Color.White,
                    title = "Participating",
                    count = "19",
                    isSelected = selectedFilterOption == InboxFilterOption.PARTICIPATING,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.PARTICIPATING
                        showFilterBottomSheet = false
                    },
                )

                FilterSheetRow(
                    icon = Icons.Default.AlternateEmail,
                    iconTint = ReasonIconYellow,
                    title = "Mentioned",
                    count = null,
                    isSelected = selectedFilterOption == InboxFilterOption.MENTIONED,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.MENTIONED
                        showFilterBottomSheet = false
                    },
                )

                FilterSheetRow(
                    icon = Icons.Default.Groups,
                    iconTint = ReasonIconYellow,
                    title = "Team mentioned",
                    count = null,
                    isSelected = selectedFilterOption == InboxFilterOption.TEAM_MENTIONED,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.TEAM_MENTIONED
                        showFilterBottomSheet = false
                    },
                )

                FilterSheetRow(
                    icon = Icons.Default.RateReview,
                    iconTint = Color.White,
                    title = "Review requested",
                    count = null,
                    isSelected = selectedFilterOption == InboxFilterOption.REVIEW_REQUESTED,
                    onClick = {
                        selectedFilterOption = InboxFilterOption.REVIEW_REQUESTED
                        showFilterBottomSheet = false
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // --- Modal Bottom Sheet 2: Filter by repository (Image 3) ---
    if (showRepoBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRepoBottomSheet = false },
            sheetState = repoSheetState,
            containerColor = BottomSheetBg,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                // Header with Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { showRepoBottomSheet = false },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close repository filter sheet",
                            tint = TextHighEmphasis,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Filter by repository",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        fontSize = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // All repositories option
                FilterSheetRow(
                    title = "All repositories",
                    count = notifications.size.toString(),
                    isSelected = selectedRepoFilter == null,
                    onClick = {
                        selectedRepoFilter = null
                        showRepoBottomSheet = false
                    },
                )

                HorizontalDivider(
                    color = SophisticatedBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                // Each repository option
                distinctRepos.forEach { repoName ->
                    val repoCount = notifications.count { it.repoName == repoName }
                    FilterSheetRow(
                        title = repoName,
                        count = repoCount.toString(),
                        isSelected = selectedRepoFilter == repoName,
                        onClick = {
                            selectedRepoFilter = repoName
                            showRepoBottomSheet = false
                        },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun InboxFilterChip(
    label: String,
    hasDropdown: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSelected && !hasDropdown) SelectedFilterBlue else SophisticatedSurface
    val contentColor = if (isSelected && !hasDropdown) Color.White else TextHighEmphasis
    val borderColor = if (isSelected) SelectedFilterBlue else SophisticatedBorder

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(34.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
            if (hasDropdown) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterSheetRow(
    title: String,
    count: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextHighEmphasis,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (count != null) {
                Text(
                    text = count,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLowEmphasis,
                    fontSize = 14.sp,
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = CheckmarkBlue,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
