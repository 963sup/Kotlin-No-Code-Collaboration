package com.nocodecollaboration.firstprinciples

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val FirstPrinciplesLightScheme = lightColorScheme(
    primary = Color(0xFF1457D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF425F91),
    background = Color(0xFFF8FAFF),
    surface = Color.White,
    onBackground = Color(0xFF171C24),
    onSurface = Color(0xFF171C24),
    outline = Color(0xFF73777F),
)

private val FirstPrinciplesDarkScheme = darkColorScheme(
    primary = Color(0xFFADC6FF),
    onPrimary = Color(0xFF002E69),
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD8E2FF),
)

@Composable
fun FirstPrinciplesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) FirstPrinciplesDarkScheme else FirstPrinciplesLightScheme,
        content = content,
    )
}

enum class PrimaryDestination(
    val label: String,
    val compactIconLabel: String,
) {
    HOME("首頁", "首"),
    INBOX("收件匣", "收"),
    KANBAN("工作", "工"),
    EXPLORE("探索", "探"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstPrinciplesMobileShell(
    selectedDestination: PrimaryDestination,
    scopeLabel: String,
    onDestinationSelected: (PrimaryDestination) -> Unit,
    onScopeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextButton(onClick = onScopeClick) {
                        Text(scopeLabel)
                    }
                },
                actions = {
                    TextButton(onClick = onSearchClick) { Text("搜尋") }
                    TextButton(onClick = onProfileClick) { Text("個人") }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                PrimaryDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == selectedDestination,
                        onClick = { onDestinationSelected(destination) },
                        icon = { Text(destination.compactIconLabel) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            content(PaddingValues())
        }
    }
}
