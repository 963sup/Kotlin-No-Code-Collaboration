package com.example.ui.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CorporateFare
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.Repository
import com.example.data.model.User
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GithubProfileScreen(
    user: User?,
    repositories: List<Repository> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSelectRepository: (Repository) -> Unit = {},
    onOpenPersonaSwitcher: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("") }
    var isEditingStatus by remember { mutableStateOf(false) }

    val userRepos = remember(user, repositories) {
        if (user != null) {
            repositories.filter { it.ownerId == user.id || it.name.isNotEmpty() }
        } else {
            repositories
        }
    }

    val popularRepos = remember(userRepos) {
        userRepos.take(3)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .testTag("github_profile_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextHighEmphasis,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Share link copied to clipboard")
                            }
                        },
                        modifier = Modifier.testTag("profile_share_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextHighEmphasis,
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("profile_settings_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextHighEmphasis,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SophisticatedBg,
                ),
            )
        },
        containerColor = SophisticatedBg,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ==========================================
            // Top Section: Avatar & Username Dropdown
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Avatar & Username Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(LavenderPrimary)
                                .border(2.dp, PinkAccent.copy(alpha = 0.5f), CircleShape)
                                .clickable { onOpenPersonaSwitcher() }
                                .testTag("profile_avatar"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = user?.displayName?.take(1)?.uppercase() ?: "9",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                ),
                                color = LavenderOnPrimary,
                            )
                        }

                        // Username with Dropdown Chevron
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenPersonaSwitcher() }
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .testTag("profile_user_switcher"),
                        ) {
                            Text(
                                text = user?.username ?: user?.displayName ?: "963sup",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                ),
                                color = TextHighEmphasis,
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Switch Account",
                                tint = TextMediumEmphasis,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    // Set Your Status Box
                    if (!isEditingStatus && statusText.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SophisticatedSurfaceDark)
                                .border(1.dp, SophisticatedBorder, RoundedCornerShape(8.dp))
                                .clickable { isEditingStatus = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("profile_status_box"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mood,
                                    contentDescription = "Status Icon",
                                    tint = TextMediumEmphasis,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = "Set your status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMediumEmphasis,
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Status",
                                tint = TextLowEmphasis,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else if (isEditingStatus) {
                        OutlinedTextField(
                            value = statusText,
                            onValueChange = { statusText = it },
                            placeholder = { Text("Set your status", color = TextMediumEmphasis) },
                            leadingIcon = {
                                Icon(Icons.Default.Mood, contentDescription = null, tint = TextMediumEmphasis)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isEditingStatus = false }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Done", tint = LavenderPrimary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_status_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SophisticatedSurfaceDark,
                                unfocusedContainerColor = SophisticatedSurfaceDark,
                                focusedBorderColor = LavenderPrimary,
                                unfocusedBorderColor = SophisticatedBorder,
                            ),
                            singleLine = true,
                        )
                    } else {
                        // Display Active Status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SophisticatedSurfaceDark)
                                .border(1.dp, SophisticatedBorder, RoundedCornerShape(8.dp))
                                .clickable { isEditingStatus = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mood,
                                    contentDescription = "Status Icon",
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextHighEmphasis,
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Status",
                                tint = TextLowEmphasis,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // Trophy / Badges Row (as shown in screenshot)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("profile_trophies_row"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = TextLowEmphasis,
                            modifier = Modifier.size(20.dp),
                        )

                        // 3 Achievement Circles
                        BadgeCircle(icon = Icons.Default.MilitaryTech, bg = Color(0xFF1E88E5), contentDesc = "Arctic Code Vault")
                        BadgeCircle(icon = Icons.Default.Star, bg = Color(0xFFF57C00), contentDesc = "Star Pro")
                        BadgeCircle(icon = Icons.Default.SentimentSatisfiedAlt, bg = Color(0xFFE91E63), contentDesc = "YOLO")
                    }
                }
            }

            // Divider separating Profile header from Popular Section
            item {
                HorizontalDivider(
                    color = SophisticatedBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            // ==========================================
            // Section: Popular Repositories
            // ==========================================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.StarBorder,
                            contentDescription = "Popular",
                            tint = TextHighEmphasis,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Popular",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            ),
                            color = TextHighEmphasis,
                        )
                    }

                    // Popular Repo Cards Carousel
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (popularRepos.isEmpty()) {
                            item {
                                PopularRepoCard(
                                    ownerName = user?.username ?: "963sup",
                                    repoName = "shu",
                                    description = "书",
                                    stars = 0,
                                    language = "JavaScript",
                                    languageColor = Color(0xFFF1E05A),
                                    onClick = { },
                                )
                            }
                            item {
                                PopularRepoCard(
                                    ownerName = user?.username ?: "963sup",
                                    repoName = "bugua",
                                    description = "卜卦",
                                    stars = 0,
                                    language = "Rust",
                                    languageColor = Color(0xFFDEA584),
                                    onClick = { },
                                )
                            }
                        } else {
                            items(popularRepos) { repo ->
                                PopularRepoCard(
                                    ownerName = user?.username ?: "963sup",
                                    repoName = repo.name,
                                    description = repo.description,
                                    stars = 0,
                                    language = "Kotlin",
                                    languageColor = Color(0xFFA97BFF),
                                    onClick = { onSelectRepository(repo) },
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // Navigation Rows: Repositories, Organizations, Starred
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ProfileLinkRow(
                        icon = Icons.Outlined.Book,
                        iconBg = Color(0xFF37474F),
                        title = "Repositories",
                        count = "${userRepos.size.coerceAtLeast(5)}",
                        onClick = {
                            if (popularRepos.isNotEmpty()) onSelectRepository(popularRepos.first())
                        },
                        testTag = "profile_link_repositories",
                    )

                    ProfileLinkRow(
                        icon = Icons.Outlined.CorporateFare,
                        iconBg = Color(0xFFE65100),
                        title = "Organizations",
                        count = "0",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("No organizations joined")
                            }
                        },
                        testTag = "profile_link_organizations",
                    )

                    ProfileLinkRow(
                        icon = Icons.Default.Star,
                        iconBg = Color(0xFFFBC02D),
                        title = "Starred",
                        count = "6",
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("6 starred repositories")
                            }
                        },
                        testTag = "profile_link_starred",
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeCircle(
    icon: ImageVector,
    bg: Color,
    contentDesc: String,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun PopularRepoCard(
    ownerName: String,
    repoName: String,
    description: String,
    stars: Int,
    language: String,
    languageColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() }
            .testTag("popular_repo_$repoName"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Owner avatar & name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(LavenderPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ownerName.take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = LavenderOnPrimary,
                    )
                }
                Text(
                    text = ownerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            }

            // Repository Name
            Text(
                text = repoName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
                color = TextHighEmphasis,
            )

            // Description
            Text(
                text = description.ifEmpty { "No description provided" },
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
                maxLines = 1,
            )

            // Language & Stars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                // Star count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = Color(0xFFFBC02D),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "$stars",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                    )
                }

                // Language
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(languageColor),
                    )
                    Text(
                        text = language,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileLinkRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    count: String,
    onClick: () -> Unit,
    testTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
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

        Text(
            text = count,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
            ),
            color = TextMediumEmphasis,
        )
    }
}
