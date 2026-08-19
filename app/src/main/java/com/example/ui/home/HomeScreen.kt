package com.example.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Repository

@Composable
fun HomeScreen(
    repositories: List<Repository>,
    onSelectRepository: (Repository) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            HomeHealthSection()
        }
        item {
            HomeActiveReposSection(
                repositories = repositories,
                onSelectRepository = onSelectRepository,
            )
        }
        item {
            HomeRecentActivitySection()
        }
    }
}
