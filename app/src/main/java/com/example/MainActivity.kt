package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavigationHost
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SophisticatedBg
import com.example.ui.viewmodel.CollaborationExperienceViewModel
import com.example.ui.viewmodel.GovernanceViewModel

enum class MainNavigationTab {
    HOME,
    INBOX,
    WORK,
    EXPLORE,
    PROFILE,
}

internal val PrimaryBottomNavigationTabs = listOf(
    MainNavigationTab.HOME,
    MainNavigationTab.INBOX,
    MainNavigationTab.WORK,
    MainNavigationTab.EXPLORE,
    MainNavigationTab.PROFILE,
)

internal fun MainNavigationTab.bottomNavigationLabel(): String = when (this) {
    MainNavigationTab.HOME -> "首頁"
    MainNavigationTab.INBOX -> "收件匣"
    MainNavigationTab.WORK -> "工作"
    MainNavigationTab.EXPLORE -> "探索"
    MainNavigationTab.PROFILE -> "個人"
}

internal fun MainNavigationTab.bottomNavigationTestTag(): String = when (this) {
    MainNavigationTab.HOME -> "nav_tab_home"
    MainNavigationTab.INBOX -> "nav_tab_inbox"
    MainNavigationTab.WORK -> "nav_tab_work"
    MainNavigationTab.EXPLORE -> "nav_tab_explore"
    MainNavigationTab.PROFILE -> "nav_tab_profile"
}

class MainActivity : ComponentActivity() {
    private val viewModel: GovernanceViewModel by viewModels()
    private val experienceViewModel: CollaborationExperienceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GovernanceApp(viewModel = viewModel, experienceViewModel = experienceViewModel)
            }
        }
    }
}

@Composable
fun GovernanceApp(
    viewModel: GovernanceViewModel,
    experienceViewModel: CollaborationExperienceViewModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = SophisticatedBg,
    ) {
        AppNavigationHost(
            viewModel = viewModel,
            experienceViewModel = experienceViewModel,
        )
    }
}
