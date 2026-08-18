package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DiscussionCategory
import com.example.data.model.DiscussionComment
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.User
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RepoDiscussionsSection(
    repo: Repository,
    discussions: List<RepoDiscussion>,
    selectedDiscussionComments: List<DiscussionComment>,
    activeUser: User?,
    effectiveRole: RepoRole,
    canCreateDiscussion: Boolean,
    onCreateDiscussion: (title: String, category: DiscussionCategory, body: String, () -> Unit) -> Unit,
    onAddComment: (discussionId: String, content: String, () -> Unit) -> Unit,
    onToggleLock: (discussionId: String) -> Unit,
    onMarkAcceptedAnswer: (discussionId: String, commentId: String) -> Unit,
    onUpvoteDiscussion: (discussionId: String) -> Unit,
    onUpvoteComment: (commentId: String, discussionId: String) -> Unit,
    onLoadComments: (discussionId: String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<DiscussionCategory?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var viewingDiscussion by remember { mutableStateOf<RepoDiscussion?>(null) }

    // Sync selected viewing discussion if updated
    val currentViewingDiscussion = viewingDiscussion?.let { curr ->
        discussions.firstOrNull { it.id == curr.id } ?: curr
    }

    LaunchedEffect(currentViewingDiscussion?.id) {
        currentViewingDiscussion?.id?.let { onLoadComments(it) }
    }

    val filteredDiscussions = remember(discussions, searchQuery, selectedCategoryFilter) {
        discussions.filter { disc ->
            val matchesSearch = searchQuery.isBlank() ||
                disc.title.contains(searchQuery, ignoreCase = true) ||
                disc.body.contains(searchQuery, ignoreCase = true) ||
                disc.authorDisplayName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == null || disc.category == selectedCategoryFilter

            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .padding(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "社群與治理討論",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Text(
                    text = "提案、決策紀錄、問答與政策討論",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = LavenderOnPrimary,
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = canCreateDiscussion,
                modifier = Modifier.testTag("create_discussion_button"),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("新增討論", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("discussion_search_input"),
            placeholder = { Text("依主題、提案或作者篩選討論…", color = TextLowEmphasis, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMediumEmphasis) },
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "清除", tint = TextMediumEmphasis)
                    }
                }
            } else {
                null
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SophisticatedSurface,
                unfocusedContainerColor = SophisticatedSurface,
                focusedBorderColor = LavenderPrimary,
                unfocusedBorderColor = SophisticatedBorder,
                focusedTextColor = TextHighEmphasis,
                unfocusedTextColor = TextHighEmphasis,
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null },
                label = { Text("所有分類（${discussions.size}）", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LavenderContainer,
                    selectedLabelColor = LavenderGlow,
                    containerColor = SophisticatedSurface,
                    labelColor = TextMediumEmphasis,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategoryFilter == null,
                    borderColor = SophisticatedBorder,
                    selectedBorderColor = LavenderPrimary,
                ),
            )

            DiscussionCategory.values().forEach { cat ->
                val isSelected = selectedCategoryFilter == cat
                val count = discussions.count { it.category == cat }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategoryFilter = if (isSelected) null else cat
                    },
                    label = { Text("${cat.label} ($count)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SophisticatedContainer,
                        selectedLabelColor = LavenderGlow,
                        containerColor = SophisticatedSurface,
                        labelColor = TextMediumEmphasis,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = SophisticatedBorder,
                        selectedBorderColor = LavenderPrimary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Discussions List or Empty State
        if (filteredDiscussions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(SophisticatedSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (discussions.isEmpty()) "尚未開始任何討論" else "找不到符合條件的討論",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (discussions.isEmpty()) {
                            "Start an open RFC, ask a policy Q&A question, or share announcements with repository contributors."
                        } else {
                            "Try clearing or adjusting your search keyword or category filter."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    if (discussions.isEmpty() && canCreateDiscussion) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("開始討論")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(filteredDiscussions, key = { it.id }) { disc ->
                    DiscussionCard(
                        discussion = disc,
                        onClick = { viewingDiscussion = disc },
                        onUpvote = { onUpvoteDiscussion(disc.id) },
                    )
                }
            }
        }
    }

    // View & Reply Discussion Dialog
    if (currentViewingDiscussion != null) {
        DiscussionDetailDialog(
            discussion = currentViewingDiscussion,
            comments = selectedDiscussionComments,
            activeUser = activeUser,
            effectiveRole = effectiveRole,
            onDismiss = { viewingDiscussion = null },
            onAddComment = { content ->
                onAddComment(currentViewingDiscussion.id, content) {
                    onLoadComments(currentViewingDiscussion.id)
                }
            },
            onToggleLock = { onToggleLock(currentViewingDiscussion.id) },
            onMarkAcceptedAnswer = { commentId ->
                onMarkAcceptedAnswer(currentViewingDiscussion.id, commentId)
            },
            onUpvoteDiscussion = { onUpvoteDiscussion(currentViewingDiscussion.id) },
            onUpvoteComment = { commentId ->
                onUpvoteComment(commentId, currentViewingDiscussion.id)
            },
        )
    }

    // Create Discussion Dialog
    if (showCreateDialog) {
        CreateDiscussionDialog(
            repo = repo,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, category, body ->
                onCreateDiscussion(title, category, body) {
                    showCreateDialog = false
                }
            },
        )
    }
}

@Composable
fun DiscussionCard(discussion: RepoDiscussion, onClick: () -> Unit, onUpvote: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("discussion_card_${discussion.discussionNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category Badge, Number, Locked / Answered icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DiscussionCategoryBadge(category = discussion.category)
                    Text(
                        text = "#${discussion.discussionNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LavenderPrimary,
                    )

                    if (discussion.isAnswered) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = EmeraldDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text("已回答", color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (discussion.isLocked) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "已鎖定",
                            tint = RoseError,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                Text(
                    text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(discussion.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                    fontSize = 11.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Upvote Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = discussion.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextHighEmphasis,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Upvote Counter Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SophisticatedSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier.clickable { onUpvote() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = "贊成",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "${discussion.upvoteCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LavenderGlow,
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            // Body preview
            if (discussion.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = discussion.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                    maxLines = 2,
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Author & Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(LavenderContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = discussion.authorDisplayName.take(1).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LavenderGlow,
                        )
                    }

                    Text(
                        text = discussion.authorDisplayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHighEmphasis,
                        fontSize = 12.sp,
                    )

                    RoleBadge(roleName = discussion.authorRole)
                }
            }
        }
    }
}

@Composable
fun DiscussionCategoryBadge(category: DiscussionCategory) {
    val (bgColor, textColor, icon) = when (category) {
        DiscussionCategory.ANNOUNCEMENTS -> Triple(RoseDark, RoseError, Icons.Default.Announcement)

        DiscussionCategory.RFC_PROPOSALS -> Triple(SophisticatedContainer, LavenderGlow, Icons.Default.Lightbulb)

        DiscussionCategory.Q_AND_A -> Triple(EmeraldDark, EmeraldSuccess, Icons.Default.HelpOutline)

        DiscussionCategory.GENERAL -> Triple(Color(0xFF422E10), AmberGlow, Icons.Default.Forum)

        DiscussionCategory.IDEAS_AND_BRAINSTORM -> Triple(SophisticatedSurfaceDark, CyanAccent, Icons.Default.Lightbulb)

        DiscussionCategory.GOVERNANCE_DEBATE -> Triple(
            SophisticatedContainer,
            LavenderPrimary,
            Icons.Default.QuestionAnswer,
        )
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
fun DiscussionDetailDialog(
    discussion: RepoDiscussion,
    comments: List<DiscussionComment>,
    activeUser: User?,
    effectiveRole: RepoRole,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onToggleLock: () -> Unit,
    onMarkAcceptedAnswer: (String) -> Unit,
    onUpvoteDiscussion: () -> Unit,
    onUpvoteComment: (String) -> Unit,
) {
    var replyText by remember { mutableStateOf("") }
    val canLock = effectiveRole.canPerform(RepoRole.MAINTAINER)
    val canAcceptAnswer = activeUser?.id == discussion.authorUserId || effectiveRole.canPerform(RepoRole.COLLABORATOR)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .testTag("discussion_detail_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DiscussionCategoryBadge(category = discussion.category)
                            Text(
                                text = "#${discussion.discussionNumber}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = LavenderPrimary,
                            )
                            if (discussion.isLocked) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RoseDark,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        RoseError.copy(alpha = 0.5f),
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = RoseError,
                                            modifier = Modifier.size(12.dp),
                                        )
                                        Text("已鎖定", color = RoseError, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = discussion.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉", tint = TextMediumEmphasis)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Discussion Body & Author Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Author bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(LavenderContainer, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = discussion.authorDisplayName.take(1).uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LavenderGlow,
                                        )
                                    }
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = discussion.authorDisplayName,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = TextHighEmphasis,
                                            )
                                            RoleBadge(roleName = discussion.authorRole)
                                        }
                                        Text(
                                            text = "發布於 ${SimpleDateFormat(
                                                "MMM d, yyyy 'at' HH:mm",
                                                Locale.getDefault(),
                                            ).format(Date(discussion.createdAt))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMediumEmphasis,
                                            fontSize = 11.sp,
                                        )
                                    }
                                }

                                // Upvote button
                                OutlinedButton(
                                    onClick = onUpvoteDiscussion,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = LavenderPrimary,
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                                    modifier = Modifier.testTag("upvote_discussion_button"),
                                ) {
                                    Icon(
                                        Icons.Default.ThumbUp,
                                        contentDescription = "贊成",
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${discussion.upvoteCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = discussion.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextHighEmphasis,
                            )
                        }
                    }

                    // Admin Actions (Lock / Unlock thread)
                    if (canLock) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = onToggleLock,
                                modifier = Modifier.testTag("toggle_lock_discussion_button"),
                            ) {
                                Icon(
                                    imageVector = if (discussion.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (discussion.isLocked) EmeraldSuccess else RoseError,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (discussion.isLocked) "Unlock Conversation" else "Lock Conversation",
                                    color = if (discussion.isLocked) EmeraldSuccess else RoseError,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Replies List
                    Text(
                        text = "回覆與回答（${comments.size}）",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (comments.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SophisticatedSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "尚無回覆；請針對此 RFC 提供意見或回答下方問題。",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMediumEmphasis,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            comments.forEach { comment ->
                                val isAccepted = comment.isAcceptedAnswer
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isAccepted) Color(0xFF143026) else SophisticatedSurfaceDark,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isAccepted) EmeraldSuccess else SophisticatedBorder,
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Accepted answer banner
                                        if (isAccepted) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(bottom = 6.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = EmeraldSuccess,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Text(
                                                    text = "已採納回答",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                    color = EmeraldSuccess,
                                                )
                                            }
                                        }

                                        // Author row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Text(
                                                    text = comment.authorDisplayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                    ),
                                                    color = TextHighEmphasis,
                                                )
                                                RoleBadge(roleName = comment.authorRole)
                                            }

                                            Text(
                                                text = SimpleDateFormat(
                                                    "MMM d, HH:mm",
                                                    Locale.getDefault(),
                                                ).format(Date(comment.createdAt)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMediumEmphasis,
                                                fontSize = 10.sp,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = comment.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextHighEmphasis,
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Reply actions: Upvote & Mark as accepted answer
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            // Upvote reply
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clickable { onUpvoteComment(comment.id) }
                                                    .padding(4.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.ThumbUp,
                                                    contentDescription = "贊成",
                                                    tint = LavenderPrimary,
                                                    modifier = Modifier.size(12.dp),
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${comment.upvotes}", fontSize = 11.sp, color = LavenderGlow)
                                            }

                                            // Accept answer button
                                            if (canAcceptAnswer && discussion.category == DiscussionCategory.Q_AND_A) {
                                                TextButton(
                                                    onClick = { onMarkAcceptedAnswer(comment.id) },
                                                    modifier = Modifier.testTag("accept_answer_${comment.id}"),
                                                ) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = if (isAccepted) EmeraldSuccess else TextMediumEmphasis,
                                                        modifier = Modifier.size(14.dp),
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isAccepted) "已採納回答" else "設為回答",
                                                        color = if (isAccepted) EmeraldSuccess else TextMediumEmphasis,
                                                        fontSize = 11.sp,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Add Reply Input Box
                Spacer(modifier = Modifier.height(10.dp))
                if (discussion.isLocked && !effectiveRole.canPerform(RepoRole.MAINTAINER)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SophisticatedSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = RoseError,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "此討論已被儲存庫維護者鎖定，無法再回覆。",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMediumEmphasis,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discussion_reply_input"),
                            placeholder = { Text("撰寫回覆或 RFC 意見…", color = TextLowEmphasis, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SophisticatedSurfaceDark,
                                unfocusedContainerColor = SophisticatedSurfaceDark,
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = SophisticatedBorder,
                                focusedTextColor = TextHighEmphasis,
                                unfocusedTextColor = TextHighEmphasis,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3,
                        )

                        Button(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onAddComment(replyText.trim())
                                    replyText = ""
                                }
                            },
                            enabled = replyText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_discussion_reply_button"),
                        ) {
                            Text("回覆", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiscussionDialog(
    repo: Repository,
    onDismiss: () -> Unit,
    onCreate: (title: String, category: DiscussionCategory, body: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(DiscussionCategory.RFC_PROPOSALS) }
    var body by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .testTag("create_discussion_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "開始討論",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis,
                        )
                        Text(
                            text = "儲存庫：${repo.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LavenderPrimary,
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉", tint = TextMediumEmphasis)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("討論標題 *") },
                    placeholder = { Text("例如：RFC－統一宣告式狀態機模型") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_discussion_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis,
                        focusedLabelColor = LavenderPrimary,
                        unfocusedLabelColor = TextMediumEmphasis,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection
                Text("分類", style = MaterialTheme.typography.labelMedium, color = TextMediumEmphasis)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DiscussionCategory.values().forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { category = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) LavenderPrimary else SophisticatedBorder,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                DiscussionCategoryBadge(category = cat)
                                Text(
                                    text = when (cat) {
                                        DiscussionCategory.ANNOUNCEMENTS -> "Broadcast updates & governance news"
                                        DiscussionCategory.RFC_PROPOSALS -> "Formal architectural request for comments"
                                        DiscussionCategory.Q_AND_A -> "Ask questions, marked answers"
                                        DiscussionCategory.GENERAL -> "Open conversation on workflow practices"
                                        DiscussionCategory.IDEAS_AND_BRAINSTORM -> "Brainstorming and proposals"
                                        DiscussionCategory.GOVERNANCE_DEBATE -> "Policy debate, compliance gates & audit"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) TextHighEmphasis else TextMediumEmphasis,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Body content
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("討論內容與規格 *") },
                    placeholder = { Text("說明提案、問題、權衡或治理規則…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("new_discussion_body_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis,
                        focusedLabelColor = LavenderPrimary,
                        unfocusedLabelColor = TextMediumEmphasis,
                    ),
                    shape = RoundedCornerShape(8.dp),
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Submit / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = TextMediumEmphasis)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && body.isNotBlank()) {
                                onCreate(title.trim(), category, body.trim())
                            }
                        },
                        enabled = title.isNotBlank() && body.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_create_discussion_button"),
                    ) {
                        Text("發布討論")
                    }
                }
            }
        }
    }
}
