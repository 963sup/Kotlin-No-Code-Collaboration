package com.example.ui.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Repository
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    repositories: List<Repository>,
    onSelectRepository: (Repository) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("全部", "我建立的", "我參與的", "已收藏")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg),
    ) {
        // Search & Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜尋倉庫、專案或工作實體...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMediumEmphasis) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SophisticatedSurface,
                    unfocusedContainerColor = SophisticatedSurface,
                    focusedBorderColor = LavenderPrimary,
                    unfocusedBorderColor = SophisticatedBorder,
                ),
                singleLine = true,
            )
            Surface(
                color = SophisticatedSurface,
                border = BorderStroke(1.dp, SophisticatedBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FilterList, contentDescription = "篩選", tint = TextHighEmphasis)
                }
            }
        }

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

        // Repo List
        val filteredRepos = repositories.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.displayName.contains(searchQuery, ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(filteredRepos) { repo ->
                ExploreRepoCard(repo = repo, onClick = { onSelectRepository(repo) })
            }
        }
    }
}

@Composable
private fun ExploreRepoCard(repo: Repository, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = repo.displayName.ifBlank { repo.name },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                )
                Surface(
                    color = LavenderContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "進行中",
                        color = LavenderPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = repo.description.ifBlank { "無程式碼協作專案容器與工作流程" },
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
            )

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = LavenderPrimary,
                trackColor = LavenderContainer,
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "WBS 60% • Issue 18",
                    fontSize = 11.sp,
                    color = TextMediumEmphasis,
                )

                // Avatars
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    listOf("王", "李", "張").forEach { char ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(LavenderPrimary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = char, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
