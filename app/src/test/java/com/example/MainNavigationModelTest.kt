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
                MainNavigationTab.KANBAN,
                MainNavigationTab.EXPLORE
            ),
            PrimaryBottomNavigationTabs
        )
        assertEquals(
            listOf("首頁", "收件匣", "工作", "探索"),
            PrimaryBottomNavigationTabs.map { it.bottomNavigationLabel() }
        )
        assertFalse(PrimaryBottomNavigationTabs.contains(MainNavigationTab.ME))
    }

    @Test
    fun `primary navigation test tags are stable and unique`() {
        assertEquals(
            listOf(
                "nav_tab_home",
                "nav_tab_inbox",
                "nav_tab_kanban",
                "nav_tab_explore"
            ),
            PrimaryBottomNavigationTabs.map { it.bottomNavigationTestTag() }
        )
        assertEquals(
            PrimaryBottomNavigationTabs.size,
            PrimaryBottomNavigationTabs.map { it.bottomNavigationTestTag() }.toSet().size
        )
    }
}
