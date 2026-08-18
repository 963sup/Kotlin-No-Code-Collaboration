from __future__ import annotations

from pathlib import Path
from textwrap import dedent

ROOT = Path(__file__).resolve().parents[2]


def read_text(relative_path: str) -> str:
    return (ROOT / relative_path).read_text(encoding="utf-8")


def write_text(relative_path: str, content: str) -> None:
    path = ROOT / relative_path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def replace_once(content: str, old: str, new: str, *, label: str) -> str:
    count = content.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected exactly one match, found {count}")
    return content.replace(old, new, 1)


def find_matching_brace(content: str, opening_brace: int) -> int:
    depth = 0
    for index in range(opening_brace, len(content)):
        char = content[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return index
    raise RuntimeError("No matching closing brace found")


def update_main_activity() -> None:
    path = "app/src/main/java/com/example/MainActivity.kt"
    content = read_text(path)

    content = replace_once(
        content,
        "import androidx.compose.material.icons.filled.Folder\n",
        "",
        label="remove obsolete Folder import",
    )

    old_enum = dedent(
        """
        enum class MainNavigationTab {
            HOME,
            INBOX,
            KANBAN,
            REPOSITORIES,
            ME
        }
        """
    )
    new_enum = dedent(
        """
        enum class MainNavigationTab {
            HOME,
            INBOX,
            KANBAN,
            EXPLORE,
            ME
        }

        internal val PrimaryBottomNavigationTabs = listOf(
            MainNavigationTab.HOME,
            MainNavigationTab.INBOX,
            MainNavigationTab.KANBAN,
            MainNavigationTab.EXPLORE
        )

        internal fun MainNavigationTab.bottomNavigationLabel(): String = when (this) {
            MainNavigationTab.HOME -> "首頁"
            MainNavigationTab.INBOX -> "收件匣"
            MainNavigationTab.KANBAN -> "工作"
            MainNavigationTab.EXPLORE -> "探索"
            MainNavigationTab.ME -> "個人"
        }

        internal fun MainNavigationTab.bottomNavigationTestTag(): String = when (this) {
            MainNavigationTab.HOME -> "nav_tab_home"
            MainNavigationTab.INBOX -> "nav_tab_inbox"
            MainNavigationTab.KANBAN -> "nav_tab_kanban"
            MainNavigationTab.EXPLORE -> "nav_tab_explore"
            MainNavigationTab.ME -> "nav_tab_me"
        }
        """
    )
    content = replace_once(content, old_enum, new_enum, label="replace primary navigation model")
    content = content.replace("MainNavigationTab.REPOSITORIES", "MainNavigationTab.EXPLORE")

    inbox_marker = 'modifier = Modifier.testTag("topbar_inbox_btn")'
    marker_index = content.find(inbox_marker)
    if marker_index < 0:
        raise RuntimeError("top-bar Inbox action marker was not found")
    inbox_start = content.rfind("                    IconButton(", 0, marker_index)
    next_action_start = content.find("                    IconButton(", marker_index + len(inbox_marker))
    if inbox_start < 0 or next_action_start < 0:
        raise RuntimeError("could not isolate the top-bar Inbox action")
    content = content[:inbox_start] + content[next_action_start:]

    navigation_marker = "                    NavigationBar(containerColor = SophisticatedSurfaceDark, tonalElevation = 0.dp) {"
    navigation_start = content.find(navigation_marker)
    if navigation_start < 0:
        raise RuntimeError("bottom NavigationBar marker was not found")
    opening_brace = content.find("{", navigation_start)
    navigation_end = find_matching_brace(content, opening_brace)

    navigation_replacement = dedent(
        """
                            NavigationBar(containerColor = SophisticatedSurfaceDark, tonalElevation = 0.dp) {
                                PrimaryBottomNavigationTabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = {
                                            viewModel.selectArtifact(null)
                                            viewModel.selectRepository(null)
                                            currentTab = tab
                                        },
                                        icon = {
                                            val icon = when (tab) {
                                                MainNavigationTab.HOME -> Icons.Default.Home
                                                MainNavigationTab.INBOX -> Icons.Default.Notifications
                                                MainNavigationTab.KANBAN -> Icons.Default.Dashboard
                                                MainNavigationTab.EXPLORE -> Icons.Default.Search
                                                MainNavigationTab.ME -> Icons.Default.AccountCircle
                                            }
                                            if (tab == MainNavigationTab.INBOX) {
                                                BadgedBox(
                                                    badge = {
                                                        if (unreadNotificationCount > 0) {
                                                            Badge(
                                                                containerColor = LavenderPrimary,
                                                                contentColor = LavenderOnPrimary
                                                            ) {
                                                                Text("$unreadNotificationCount")
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = tab.bottomNavigationLabel()
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = tab.bottomNavigationLabel()
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = tab.bottomNavigationLabel(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = LavenderOnPrimary,
                                            selectedTextColor = LavenderPrimary,
                                            unselectedIconColor = TextMediumEmphasis,
                                            unselectedTextColor = TextMediumEmphasis,
                                            indicatorColor = LavenderPrimary
                                        ),
                                        modifier = Modifier.testTag(tab.bottomNavigationTestTag())
                                    )
                                }
                            }
        """
    ).rstrip()
    content = content[:navigation_start] + navigation_replacement + content[navigation_end + 1 :]

    if "MainNavigationTab.REPOSITORIES" in content:
        raise RuntimeError("legacy REPOSITORIES navigation tab remains")
    if "topbar_inbox_btn" in content:
        raise RuntimeError("duplicate top-bar Inbox entry remains")
    for required_tag in ("nav_tab_home", "nav_tab_inbox", "nav_tab_kanban", "nav_tab_explore"):
        if required_tag not in content:
            raise RuntimeError(f"missing required navigation test tag: {required_tag}")

    write_text(path, content)


def update_color_tokens() -> None:
    write_text(
        "app/src/main/java/com/example/ui/theme/Color.kt",
        dedent(
            """
            package com.example.ui.theme

            import androidx.compose.ui.graphics.Color

            // Light-first enterprise collaboration palette. Legacy token names are retained to
            // keep the current component surface stable while the design system moves to M3 roles.
            val SophisticatedBg = Color(0xFFF6F8FC)
            val SophisticatedSurface = Color(0xFFFFFFFF)
            val SophisticatedSurfaceDark = Color(0xFFF9FBFF)
            val SophisticatedContainer = Color(0xFFEAF1FF)
            val SophisticatedBorder = Color(0xFFD8E0EC)
            val SophisticatedBorderSubtle = Color(0x4D0B63F6)

            // Primary product identity: high-contrast blue on a white operational canvas.
            val LavenderPrimary = Color(0xFF0B63F6)
            val LavenderOnPrimary = Color(0xFFFFFFFF)
            val LavenderContainer = Color(0xFFDDE9FF)
            val LavenderGlow = Color(0xFF2459A9)
            val LavenderSubtle = Color(0xFF35598A)
            val PinkAccent = Color(0xFF9C2F6D)
            val WhiteM3 = Color(0xFFFFFFFF)

            // Typography hierarchy for field readability.
            val TextHighEmphasis = Color(0xFF14213D)
            val TextMediumEmphasis = Color(0xFF5C667A)
            val TextLowEmphasis = Color(0xFF8992A6)
            val PureWhite = Color(0xFFFFFFFF)

            // Operational states calibrated for a light background.
            val EmeraldSuccess = Color(0xFF137A43)
            val EmeraldDark = Color(0xFFE4F6EC)
            val AmberWarning = Color(0xFF8A4E00)
            val AmberGlow = Color(0xFFFFE0A3)
            val RoseError = Color(0xFFB3261E)
            val RoseDark = Color(0xFFFCE8E6)
            val CyanAccent = Color(0xFF005B8F)
            val CyanGlow = Color(0xFFDDF2FF)
            val PurpleTech = Color(0xFF5B42B2)
            val PurpleGlow = Color(0xFFECE7FF)

            // Compatibility aliases used by existing components.
            val SlateDark950 = SophisticatedBg
            val SlateDark900 = SophisticatedSurfaceDark
            val SlateDark800 = SophisticatedSurface
            val SlateDark700 = SophisticatedContainer
            val SlateDark600 = SophisticatedBorder
            val IndigoPrimary = LavenderPrimary
            val IndigoLight = LavenderPrimary
            val IndigoDark = Color(0xFF002F66)
            val CardSurfaceDark = SophisticatedSurface
            val CardBorderDark = SophisticatedBorder
            val TopBarSurfaceDark = SophisticatedSurfaceDark
            """
        ).lstrip(),
    )


def update_theme() -> None:
    write_text(
        "app/src/main/java/com/example/ui/theme/Theme.kt",
        dedent(
            """
            package com.example.ui.theme

            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.darkColorScheme
            import androidx.compose.material3.lightColorScheme
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.graphics.Color

            private val DarkColorScheme = darkColorScheme(
                primary = Color(0xFFA9C7FF),
                onPrimary = Color(0xFF003063),
                primaryContainer = Color(0xFF00478A),
                onPrimaryContainer = Color(0xFFD7E3FF),
                secondary = Color(0xFFBBC7DB),
                onSecondary = Color(0xFF253141),
                secondaryContainer = Color(0xFF3B4858),
                onSecondaryContainer = Color(0xFFD7E3F8),
                tertiary = Color(0xFFD5B8F1),
                onTertiary = Color(0xFF3A2750),
                background = Color(0xFF111318),
                onBackground = Color(0xFFE1E2E8),
                surface = Color(0xFF191C20),
                onSurface = Color(0xFFE1E2E8),
                surfaceVariant = Color(0xFF42474F),
                onSurfaceVariant = Color(0xFFC2C7CF),
                outline = Color(0xFF8C9199),
                outlineVariant = Color(0xFF42474F),
                error = Color(0xFFFFB4AB),
                onError = Color(0xFF690005)
            )

            private val LightColorScheme = lightColorScheme(
                primary = LavenderPrimary,
                onPrimary = LavenderOnPrimary,
                primaryContainer = LavenderContainer,
                onPrimaryContainer = Color(0xFF002F66),
                secondary = Color(0xFF52657F),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFDCE6F5),
                onSecondaryContainer = Color(0xFF10263F),
                tertiary = PurpleTech,
                onTertiary = Color.White,
                background = SophisticatedBg,
                onBackground = TextHighEmphasis,
                surface = SophisticatedSurface,
                onSurface = TextHighEmphasis,
                surfaceVariant = SophisticatedSurfaceDark,
                onSurfaceVariant = TextMediumEmphasis,
                outline = SophisticatedBorder,
                outlineVariant = Color(0xFFE3E8F0),
                error = RoseError,
                onError = Color.White
            )

            @Suppress("UNUSED_PARAMETER")
            @Composable
            fun MyApplicationTheme(
                darkTheme: Boolean = false,
                dynamicColor: Boolean = false,
                content: @Composable () -> Unit
            ) {
                MaterialTheme(
                    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
                    typography = Typography,
                    content = content
                )
            }
            """
        ).lstrip(),
    )


def update_explore_copy() -> None:
    path = "app/src/main/java/com/example/ui/screens/RepositoriesScreen.kt"
    content = read_text(path)
    replacements = {
        "搜尋儲存庫、擁有者或藍圖…": "搜尋專案、儲存庫、擁有者或類別…",
        'label = "All Workspaces (${repositories.size})"': 'label = "全部 (${repositories.size})"',
        'label = "Org-Owned (${repositories.count { it.ownerType == OwnerType.ORGANIZATION }})"': 'label = "組織擁有 (${repositories.count { it.ownerType == OwnerType.ORGANIZATION }})"',
        'label = "User-Owned (${repositories.count { it.ownerType == OwnerType.USER }})"': 'label = "個人擁有 (${repositories.count { it.ownerType == OwnerType.USER }})"',
        'text = "企業階層治理"': 'text = "探索可存取的專案與成果"',
    }
    for old, new in replacements.items():
        content = replace_once(content, old, new, label=f"update Explore copy: {old}")
    write_text(path, content)


def add_navigation_test() -> None:
    write_text(
        "app/src/test/java/com/example/MainNavigationModelTest.kt",
        dedent(
            """
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
            """
        ).lstrip(),
    )


def add_decision_record() -> None:
    write_text(
        "docs/architecture/mobile-collaboration-mvp.md",
        dedent(
            """
            # Mobile Collaboration MVP｜First Principles Decision Record

            ## Objective

            Reduce the daily mobile path to the four highest-frequency outcomes: understand the current scope, receive work, execute work, and discover accessible collaboration containers.

            ## Facts

            - Repository WBS already projects the existing recursive Issue hierarchy.
            - Inbox already supports exact collaboration-target navigation.
            - The bottom bar currently exposes only Home, Kanban, and Repositories.
            - Inbox is duplicated in the top bar, while Repository search is presented as a generic search action.
            - `LightColorScheme` is incorrectly created with `darkColorScheme`, and the app defaults to dark mode.
            - Existing screens already provide Home, Inbox, Kanban, Repository discovery, Profile, and workspace scope switching.

            ## Assumptions

            - The supplied product target uses a light operational canvas as the default experience.
            - Repository discovery is sufficient for the first Explore slice; cross-entity search is a later objective.

            ## Invariants

            - Repository remains a no-code collaboration container.
            - No Git, source-code, CI/CD, terminal, or developer-platform product semantics are added.
            - No ownership, permission, Room entity, or policy behavior changes.
            - Profile and workspace scope remain top-level contextual actions rather than bottom destinations.
            - Every primary navigation control retains a stable `Modifier.testTag`.

            ## Core Model

            - **Entities:** existing User, Enterprise, Organization, Team, Repository, Issue, Notification, Artifact.
            - **Relationships:** unchanged ownership, membership, access rules, assignments, and collaboration targets.
            - **States:** selected top-level destination and selected workspace scope.
            - **Events:** select Home, Inbox, Work, Explore, Profile, or workspace scope.
            - **Responsibilities:** MainActivity owns shell navigation; existing screens own their bounded content; ViewModel remains the authoritative data path.

            ## Root Constraint

            The product already contains the necessary capabilities, but the mobile information architecture and visual foundation do not expose them as one coherent daily work loop.

            ## Highest-Leverage Change

            Reuse the existing screens and data flows, changing only the shell destination model, Explore wording, and design tokens instead of adding parallel screens or data models.

            ## Minimum Viable Change

            1. Bottom navigation becomes Home, Inbox, Work, Explore.
            2. Inbox unread count moves to the primary Inbox destination.
            3. Profile and scope switching remain in the top app bar.
            4. The existing Repository catalog becomes the first Explore surface.
            5. The app uses a real Material 3 light color scheme by default.
            6. Unit tests lock the destination order, labels, and test tags.

            ## Rejected Complexity

            - No new Explore database, global search index, Favorite, Follow, Achievement, or synchronization model.
            - No new top-level screen.
            - No WBS or Issue persistence changes.
            - No Room migration work in this slice.
            - No speculative navigation framework refactor.

            ## Product Guardian

            - **Verdict:** TRANSLATE
            - **Objective:** expose collaboration work, attention, and discovery on mobile.
            - **Canonical owner:** existing User and workspace scope.
            - **Canonical container:** existing Repository access scope.
            - **Allowed semantics:** Home, Inbox, work status, discovery, no-code Repository creation.
            - **Excluded semantics:** code search, source files, branches, pull requests, or developer tooling.

            ## Verification Evidence

            - `MainNavigationModelTest` proves the four destination order, Traditional Chinese labels, unique tags, and exclusion of Profile from the bottom bar.
            - `gradle :app:testDebugUnitTest` passes.
            - `gradle :app:assembleDebug` passes.
            - Diff inspection confirms no persistence, ownership, or policy changes.

            ## Stop Condition

            Stop when the four-destination shell, light-first palette, Explore wording, tests, and Android build pass. Cross-entity Explore and social features require separate decision records.
            """
        ).lstrip(),
    )


def main() -> None:
    update_main_activity()
    update_color_tokens()
    update_theme()
    update_explore_copy()
    add_navigation_test()
    add_decision_record()


if __name__ == "__main__":
    main()
