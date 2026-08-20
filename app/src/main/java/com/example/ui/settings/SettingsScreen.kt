package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun showToast(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .testTag("settings_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                        color = TextHighEmphasis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        ) {
            // ==========================================
            // Section 1: Notifications
            // ==========================================
            item {
                SettingsSectionHeader(title = "Notifications")
            }
            item {
                SettingsRowItem(
                    title = "Notification Options",
                    subtitle = "Setup needed",
                    onClick = { showToast("Notification options") },
                    testTag = "settings_notifications_item",
                )
            }
            item {
                SettingsSectionDivider()
            }

            // ==========================================
            // Section 2: General
            // ==========================================
            item {
                SettingsSectionHeader(title = "General")
            }
            item {
                SettingsRowItem(
                    title = "Theme",
                    subtitle = "Follow system",
                    onClick = { showToast("Theme settings") },
                    testTag = "settings_theme_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Code Options",
                    subtitle = null,
                    onClick = { showToast("Code options") },
                    testTag = "settings_code_options_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Language",
                    subtitle = "Follow system",
                    onClick = { showToast("Language settings") },
                    testTag = "settings_language_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Accounts",
                    subtitle = null,
                    badge = "2",
                    onClick = { showToast("Accounts management") },
                    testTag = "settings_accounts_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "App Lock",
                    subtitle = null,
                    onClick = { showToast("App lock settings") },
                    testTag = "settings_app_lock_item",
                )
            }
            item {
                SettingsSectionDivider()
            }

            // ==========================================
            // Section 3: Subscriptions
            // ==========================================
            item {
                SettingsSectionHeader(title = "Subscriptions")
            }
            item {
                SettingsRowItem(
                    title = "Copilot",
                    subtitle = "Copilot Free",
                    onClick = { showToast("Copilot Free active") },
                    testTag = "settings_copilot_item",
                )
            }
            item {
                SettingsSectionDivider()
            }

            // ==========================================
            // Section 4: More Options
            // ==========================================
            item {
                SettingsSectionHeader(title = "More Options")
            }
            item {
                SettingsRowItem(
                    title = "Share Feedback",
                    subtitle = null,
                    onClick = { showToast("Feedback dialog") },
                    testTag = "settings_share_feedback_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Get Help",
                    subtitle = null,
                    onClick = { showToast("Help center") },
                    testTag = "settings_get_help_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Terms of Service",
                    subtitle = null,
                    onClick = { showToast("Terms of Service") },
                    testTag = "settings_terms_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Privacy Policy & Analytics",
                    subtitle = null,
                    onClick = { showToast("Privacy Policy & Analytics") },
                    testTag = "settings_privacy_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Open Source Libraries",
                    subtitle = null,
                    onClick = { showToast("Open Source Licenses") },
                    testTag = "settings_open_source_item",
                )
            }
            item {
                SettingsRowItem(
                    title = "Sign Out",
                    subtitle = null,
                    onClick = {
                        onSignOut()
                        showToast("Signed Out")
                    },
                    testTag = "settings_sign_out_item",
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        ),
        color = TextMediumEmphasis,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SettingsRowItem(
    title: String,
    subtitle: String?,
    badge: String? = null,
    onClick: () -> Unit,
    testTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
                color = TextHighEmphasis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                    ),
                    color = TextMediumEmphasis,
                )
            }
        }

        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                ),
                color = TextMediumEmphasis,
            )
        }
    }
}

@Composable
private fun SettingsSectionDivider() {
    HorizontalDivider(
        color = SophisticatedBorder,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}
