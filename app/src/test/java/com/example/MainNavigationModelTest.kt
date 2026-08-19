package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MainNavigationModelTest {

    @Test
    fun `primary navigation follows the mobile collaboration loop`() {
        assertEquals(
            listOf(
                MainNavigationTab.HOME,
                MainNavigationTab.INBOX,
                MainNavigationTab.WORK,
                MainNavigationTab.EXPLORE,
                MainNavigationTab.PROFILE,
            ),
            PrimaryBottomNavigationTabs
        )
        assertEquals(
            listOf("首頁", "收件匣", "工作", "探索", "個人"),
            PrimaryBottomNavigationTabs.map { it.bottomNavigationLabel() }
        )
    }

    @Test
    fun `primary navigation test tags are stable and unique`() {
        assertEquals(
            listOf(
                "nav_tab_home",
                "nav_tab_inbox",
                "nav_tab_work",
                "nav_tab_explore",
                "nav_tab_profile",
            ),
            PrimaryBottomNavigationTabs.map { it.bottomNavigationTestTag() }
        )
        assertEquals(
            PrimaryBottomNavigationTabs.size,
            PrimaryBottomNavigationTabs.map { it.bottomNavigationTestTag() }.toSet().size
        )
    }
}
