package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.Enterprise
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.Repository
import com.example.data.model.ReviewDecision
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedBorderSubtle
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ProfileSubTab(val label: String, val icon: ImageVector) {
    OVERVIEW("總覽", Icons.Default.Person),
    GOVERNANCE_ACCESS("Access & Hierarchy", Icons.Default.Security),
    CONTRIBUTIONS("Contributions", Icons.Default.Description),
    SECURITY_AUTH("Auth & Security", Icons.Default.VpnKey),
    SETTINGS("設定", Icons.Default.Settings),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    user: User,
    allUsers: List<User>,
    enterprise: Enterprise?,
    organizations: List<Organization>,
    teams: List<Team>,
    repositories: List<Repository>,
    allAccessRules: List<RepoAccessRule>,
    allOrgMemberships: List<OrgMembership>,
    allTeamMemberships: List<TeamMembership>,
    allArtifacts: List<NoCodeArtifact>,
    userArtifacts: List<NoCodeArtifact>,
    userReviews: List<ArtifactReview>,
    userApprovals: List<ArtifactApproval>,
    userAuditLogs: List<AuditLog>,
    userIssues: List<RepoIssue>,
    userDiscussions: List<RepoDiscussion>,
    activeUser: User?,
    onSelectUserToInspect: (User) -> Unit,
    onSwitchActivePersona: (User) -> Unit,
    onSelectRepository: (Repository) -> Unit,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
    onUpdateProfile: (User, String, String, String, String, String, String, String) -> Unit,
) {
    var selectedSubTab by remember { mutableStateOf(ProfileSubTab.OVERVIEW) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showUserPickerDropdown by remember { mutableStateOf(false) }

    // Derived relationship calculations
    val userOrgMemberships = remember(user, allOrgMemberships, organizations) {
        allOrgMemberships.filter { it.userId == user.id }.mapNotNull { mem ->
            organizations.firstOrNull { it.id == mem.orgId }?.let { org -> Pair(mem, org) }
        }
    }

    val userTeamMemberships = remember(user, allTeamMemberships, teams, organizations) {
        allTeamMemberships.filter { it.userId == user.id }.mapNotNull { mem ->
            teams.firstOrNull { it.id == mem.teamId }?.let { team ->
                val parentOrg = organizations.firstOrNull { it.id == team.orgId }
                Triple(mem, team, parentOrg)
            }
        }
    }

    val ownedRepos = remember(user, repositories) {
        repositories.filter { it.ownerType == OwnerType.USER && it.ownerId == user.id }
    }

    val collaboratorRepos = remember(user, repositories, allAccessRules) {
        val userTeamIds = userTeamMemberships.map { it.second.id }.toSet()
        val directRules = allAccessRules.filter {
            (it.granteeType == com.example.data.model.GranteeType.USER && it.granteeId == user.id) ||
                (it.granteeType == com.example.data.model.GranteeType.TEAM && userTeamIds.contains(it.granteeId))
        }
        val repoIds = directRules.map { it.repoId }.toSet()
        repositories.filter { repoIds.contains(it.id) && !(it.ownerType == OwnerType.USER && it.ownerId == user.id) }
    }

    val isSelf = activeUser?.id == user.id

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .testTag("user_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // =========================================================================
        // 1. TOP HEADER: USER IDENTITY CARD & ENTERPRISE CONTEXT
        // =========================================================================
        item {
            UserProfileHeroCard(
                user = user,
                enterprise = enterprise,
                allUsers = allUsers,
                isSelf = isSelf,
                onEditClick = { showEditProfileDialog = true },
                onSelectUserToInspect = onSelectUserToInspect,
                onSwitchActivePersona = onSwitchActivePersona,
            )
        }

        // =========================================================================
        // 2. SUB-NAVIGATION TABS (OVERVIEW, HIERARCHY, CONTRIBUTIONS, AUTH/SEC, SETTINGS)
        // =========================================================================
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorder),
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedSubTab.ordinal,
                    containerColor = SophisticatedSurfaceDark,
                    contentColor = LavenderPrimary,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedSubTab.ordinal]),
                            color = LavenderPrimary,
                            height = 3.dp,
                        )
                    },
                    divider = {},
                ) {
                    ProfileSubTab.values().forEach { tab ->
                        Tab(
                            selected = selectedSubTab == tab,
                            onClick = { selectedSubTab = tab },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedSubTab == tab) LavenderPrimary else TextMediumEmphasis,
                                    )
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selectedSubTab ==
                                                tab
                                            ) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Medium
                                            },
                                        ),
                                        color = if (selectedSubTab == tab) LavenderPrimary else TextMediumEmphasis,
                                    )
                                }
                            },
                            modifier = Modifier.testTag("profile_tab_${tab.name.lowercase()}"),
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 3. TAB CONTENT
        // =========================================================================
        when (selectedSubTab) {
            ProfileSubTab.OVERVIEW -> {
                // Quick Metrics Grid
                item {
                    ProfileMetricsGrid(
                        orgCount = userOrgMemberships.size,
                        teamCount = userTeamMemberships.size,
                        ownedRepoCount = ownedRepos.size,
                        collaboratorRepoCount = collaboratorRepos.size,
                        artifactCount = userArtifacts.size,
                        reviewCount = userReviews.size,
                        approvalCount = userApprovals.size,
                        issueCount = userIssues.size,
                    )
                }

                // Identity & Bio Card
                item {
                    ProfileIdentitySummaryCard(user = user, enterprise = enterprise)
                }

                // Cross-Enterprise Governance Hierarchy Map (Enterprise -> Org -> Team -> Repo)
                item {
                    GovernanceHierarchyRelationalCard(
                        user = user,
                        enterprise = enterprise,
                        orgMemberships = userOrgMemberships,
                        teamMemberships = userTeamMemberships,
                        ownedRepos = ownedRepos,
                        collaboratorRepos = collaboratorRepos,
                        onSelectRepository = onSelectRepository,
                    )
                }

                // Recent Activities Card
                item {
                    RecentProfileActivityCard(
                        auditLogs = userAuditLogs.take(5),
                        user = user,
                    )
                }
            }

            ProfileSubTab.GOVERNANCE_ACCESS -> {
                // Enterprise Boundary & Admin Authority Card
                item {
                    EnterpriseAuthorityCard(user = user, enterprise = enterprise)
                }

                // Organization 個成員hips & Roles
                item {
                    OrganizationMembershipsSection(
                        memberships = userOrgMemberships,
                        user = user,
                    )
                }

                // Team 個成員hips & Collaborative Context
                item {
                    TeamMembershipsSection(
                        teamMemberships = userTeamMemberships,
                        user = user,
                    )
                }

                // Repositories & Direct Grants
                item {
                    RepositoryGrantsSection(
                        ownedRepos = ownedRepos,
                        collaboratorRepos = collaboratorRepos,
                        user = user,
                        onSelectRepository = onSelectRepository,
                    )
                }

                // Effective Permissions Matrix
                item {
                    EffectivePermissionsMatrixCard(user = user, isEnterpriseAdmin = user.isEnterpriseAdmin)
                }
            }

            ProfileSubTab.CONTRIBUTIONS -> {
                // Authored 個成果 Section
                item {
                    ProfileAuthoredArtifactsSection(
                        artifacts = userArtifacts,
                        user = user,
                        onSelectArtifact = onSelectArtifact,
                    )
                }

                // Formal Code/No-Code Reviews Section
                item {
                    ProfileReviewsSection(
                        reviews = userReviews,
                        allArtifacts = allArtifacts,
                        user = user,
                    )
                }

                // Formal Approver 個簽核s Section
                item {
                    ProfileApprovalsSection(
                        approvals = userApprovals,
                        allArtifacts = allArtifacts,
                        user = user,
                    )
                }

                // Issues & Discussions Section
                item {
                    ProfileIssuesAndDiscussionsSection(
                        issues = userIssues,
                        discussions = userDiscussions,
                        user = user,
                    )
                }
            }

            ProfileSubTab.SECURITY_AUTH -> {
                // Architecture Callout: Separation of Identity / Auth / Authorization
                item {
                    SecurityArchitectureCalloutCard()
                }

                // Authentication & Federated Identity Status
                item {
                    AuthenticationIdentityCard(user = user)
                }

                // Session Security & MFA Enforcement
                item {
                    SessionSecurityCard(user = user)
                }
            }

            ProfileSubTab.SETTINGS -> {
                // Account Settings & Preferences
                item {
                    AccountSettingsCard(
                        user = user,
                        isSelf = isSelf,
                        onEditClick = { showEditProfileDialog = true },
                    )
                }

                // Notification & Policy Subscriptions
                item {
                    NotificationPreferencesCard(user = user)
                }
            }
        }
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        EditProfileModal(
            user = user,
            onDismiss = { showEditProfileDialog = false },
            onSave = { displayName, title, bio, location, pronouns, avatarColor, notifPrefs ->
                onUpdateProfile(user, displayName, title, bio, location, pronouns, avatarColor, notifPrefs)
                showEditProfileDialog = false
            },
        )
    }
}

// =========================================================================
// HERO HEADER CARD
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileHeroCard(
    user: User,
    enterprise: Enterprise?,
    allUsers: List<User>,
    isSelf: Boolean,
    onEditClick: () -> Unit,
    onSelectUserToInspect: (User) -> Unit,
    onSwitchActivePersona: (User) -> Unit,
) {
    var showUserPicker by remember { mutableStateOf(false) }
    val avatarColor = remember(user.avatarColorHex) {
        com.example.ui.theme.parseHexColor(user.avatarColorHex, fallback = Color(0xFF6366F1))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SophisticatedSurface,
        border = BorderStroke(1.dp, SophisticatedBorderSubtle),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Enterprise Context Banner
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
                            .clip(CircleShape)
                            .background(LavenderContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = LavenderPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        text = (enterprise?.name ?: "NEXUS ENTERPRISE").uppercase() + " DIRECTORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = LavenderPrimary,
                    )
                }

                // Switch Persona / User Selector
                Box {
                    OutlinedButton(
                        onClick = { showUserPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SophisticatedSurfaceDark,
                            contentColor = TextHighEmphasis,
                        ),
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("user_profile_switch_picker_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = LavenderPrimary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("檢視使用者", style = MaterialTheme.typography.labelSmall)
                    }

                    DropdownMenu(
                        expanded = showUserPicker,
                        onDismissRequest = { showUserPicker = false },
                        modifier = Modifier.background(SophisticatedSurfaceDark),
                    ) {
                        allUsers.forEach { otherUser ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    com.example.ui.theme.parseHexColor(otherUser.avatarColorHex),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = otherUser.displayName.take(1),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = Color.White,
                                            )
                                        }
                                        Column {
                                            Text(
                                                text =
                                                otherUser.displayName +
                                                    if (otherUser.id == user.id) " (Current)" else "",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                                color = TextHighEmphasis,
                                            )
                                            Text(
                                                text = "@${otherUser.username} • ${otherUser.title}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMediumEmphasis,
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectUserToInspect(otherUser)
                                    showUserPicker = false
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = SophisticatedBorder)

            // Primary Identity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Large Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(avatarColor)
                        .border(2.dp, LavenderPrimary.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = user.displayName.split(
                            " ",
                        ).mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                }

                // Name & Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                            ),
                            color = TextHighEmphasis,
                        )

                        if (isSelf) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LavenderContainer,
                            ) {
                                Text(
                                    text = "你",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                    ),
                                    color = LavenderPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    Text(
                        text = "@${user.username} • ${user.title}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = LavenderPrimary,
                    )

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                    )

                    // Location & Pronouns
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        if (user.location.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TextMediumEmphasis,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = user.location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMediumEmphasis,
                                )
                            }
                        }

                        if (user.pronouns.isNotEmpty()) {
                            Text(
                                text = "(${user.pronouns})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLowEmphasis,
                            )
                        }
                    }
                }
            }

            // Badges & Authority Pill Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (user.isEnterpriseAdmin) {
                    AuthorityChip(
                        icon = Icons.Default.Security,
                        label = "Enterprise Administrator",
                        bgColor = LavenderContainer,
                        textColor = LavenderPrimary,
                    )
                } else {
                    AuthorityChip(
                        icon = Icons.Default.Badge,
                        label = "Enterprise Contributor",
                        bgColor = SophisticatedSurfaceDark,
                        textColor = TextHighEmphasis,
                    )
                }

                AuthorityChip(
                    icon = Icons.Default.CheckCircle,
                    label = user.authStatus,
                    bgColor = SophisticatedSurfaceDark,
                    textColor = EmeraldSuccess,
                )

                if (user.canOwnerRepository) {
                    AuthorityChip(
                        icon = Icons.Default.Folder,
                        label = "Personal Workspaces Allowed",
                        bgColor = SophisticatedSurfaceDark,
                        textColor = LavenderSubtle,
                    )
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SophisticatedSurfaceDark,
                        contentColor = TextHighEmphasis,
                    ),
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_edit_profile"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = LavenderPrimary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("編輯個人檔案", style = MaterialTheme.typography.labelMedium)
                }

                if (!isSelf) {
                    Button(
                        onClick = { onSwitchActivePersona(user) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_switch_persona"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("切換為此身分", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthorityChip(icon: ImageVector, label: String, bgColor: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
            )
        }
    }
}

// =========================================================================
// METRICS GRID
// =========================================================================
@Composable
private fun ProfileMetricsGrid(
    orgCount: Int,
    teamCount: Int,
    ownedRepoCount: Int,
    collaboratorRepoCount: Int,
    artifactCount: Int,
    reviewCount: Int,
    approvalCount: Int,
    issueCount: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SophisticatedSurface,
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "企業範圍與責任",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                ),
                color = LavenderPrimary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricTile(
                    count = orgCount,
                    label = "組織",
                    icon = Icons.Default.Apartment,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    count = teamCount,
                    label = "團隊",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    count = ownedRepoCount + collaboratorRepoCount,
                    label = "儲存庫",
                    icon = Icons.Default.Folder,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    count = artifactCount,
                    label = "成果",
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricTile(
                    count = reviewCount,
                    label = "審查",
                    icon = Icons.Default.RateReview,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    count = approvalCount,
                    label = "核准",
                    icon = Icons.Default.Approval,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    count = issueCount,
                    label = "Issues / RFCs",
                    icon = Icons.Default.Forum,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MetricTile(count: Int, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SophisticatedSurfaceDark,
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LavenderPrimary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMediumEmphasis,
                maxLines = 1,
            )
        }
    }
}

// =========================================================================
// IDENTITY SUMMARY & BIO CARD
// =========================================================================
@Composable
private fun ProfileIdentitySummaryCard(user: User, enterprise: Enterprise?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "身分與角色檔案",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            if (user.bio.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SophisticatedSurfaceDark,
                    border = BorderStroke(1.dp, SophisticatedBorder),
                ) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHighEmphasis,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileFieldRow(label = "Directory Identity", value = user.username)
                ProfileFieldRow(label = "Enterprise Root", value = enterprise?.name ?: "Nexus Enterprise")
                ProfileFieldRow(label = "Primary Email", value = user.email)
                ProfileFieldRow(
                    label = "Governance Authority",
                    value = if (user.isEnterpriseAdmin) "Global Enterprise Admin (Unrestricted Policy Enforcement)" else "Standard Member & Contributor",
                )
                ProfileFieldRow(
                    label = "Workspace Creation",
                    value = if (user.canOwnerRepository) "Allowed (User-Owned Workspace Repositories)" else "Restricted (Org Only)",
                )
            }
        }
    }
}

@Composable
private fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMediumEmphasis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextHighEmphasis,
        )
    }
}

// =========================================================================
// GOVERNANCE HIERARCHY RELATIONAL CARD
// =========================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GovernanceHierarchyRelationalCard(
    user: User,
    enterprise: Enterprise?,
    orgMemberships: List<Pair<OrgMembership, Organization>>,
    teamMemberships: List<Triple<TeamMembership, Team, Organization?>>,
    ownedRepos: List<Repository>,
    collaboratorRepos: List<Repository>,
    onSelectRepository: (Repository) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "階層與關係架構",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            Text(
                text = "顯示使用者檔案向下連結企業、組織、團隊與儲存庫的關係：",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
            )

            // Diagram visualizer
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorder),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Level 1: Enterprise
                    HierarchyLevelRow(
                        level = "ENTERPRISE",
                        title = enterprise?.name ?: "Nexus Enterprise",
                        subtitle = if (user.isEnterpriseAdmin) "Global Admin Scope" else "Directory Member",
                        icon = Icons.Default.Apartment,
                        tint = LavenderPrimary,
                    )

                    // Level 2: Organizations
                    HierarchyLevelRow(
                        level = "ORGANIZATIONS",
                        title = "${orgMemberships.size} Linked Organization(s)",
                        subtitle = orgMemberships.joinToString(
                            ", ",
                        ) { "${it.second.name} (${it.first.role.name})" }.ifEmpty { "No direct org memberships" },
                        icon = Icons.Default.Apartment,
                        tint = EmeraldSuccess,
                    )

                    // Level 3: 個團隊
                    HierarchyLevelRow(
                        level = "團隊",
                        title = "${teamMemberships.size} Team 個成員hip(s)",
                        subtitle = teamMemberships.joinToString(
                            ", ",
                        ) { "${it.second.name} (${it.first.role.name})" }.ifEmpty { "No team assignments" },
                        icon = Icons.Default.Groups,
                        tint = PinkAccent,
                    )

                    // Level 4: Repositories
                    HierarchyLevelRow(
                        level = "REPOSITORIES",
                        title = "${ownedRepos.size} Owned • ${collaboratorRepos.size} Collaborations",
                        subtitle = (ownedRepos + collaboratorRepos).take(
                            4,
                        ).joinToString(", ") { it.name } + if ((ownedRepos + collaboratorRepos).size > 4) "..." else "",
                        icon = Icons.Default.Folder,
                        tint = LavenderGlow,
                    )
                }
            }
        }
    }
}

@Composable
private fun HierarchyLevelRow(level: String, title: String, subtitle: String, icon: ImageVector, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = level,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    ),
                    color = tint,
                )
                Text(
                    text = "• " + title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumEmphasis,
                maxLines = 2,
            )
        }
    }
}

// =========================================================================
// RECENT ACTIVITY CARD
// =========================================================================
@Composable
private fun RecentProfileActivityCard(auditLogs: List<AuditLog>, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "近期歸屬活動",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SophisticatedSurfaceDark,
                ) {
                    Text(
                        text = "${auditLogs.size} 個事件",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            if (auditLogs.isEmpty()) {
                Text(
                    text = "尚無歸屬於此使用者的近期稽核事件：${user.displayName}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                auditLogs.forEach { log ->
                    ActivityLogItem(log = log)
                }
            }
        }
    }
}

@Composable
private fun ActivityLogItem(log: AuditLog) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val isAllowed = log.verdict == PolicyVerdict.ALLOWED

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = SophisticatedSurfaceDark,
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (isAllowed) EmeraldSuccess else RoseError,
                modifier = Modifier.size(16.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.actionName.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextHighEmphasis,
                )
                Text(
                    text = log.reasoning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                    maxLines = 1,
                )
            }

            Text(
                text = dateFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextLowEmphasis,
            )
        }
    }
}

// =========================================================================
// GOVERNANCE & ACCESS SUB-TAB CARDS
// =========================================================================

@Composable
private fun EnterpriseAuthorityCard(user: User, enterprise: Enterprise?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "企業政策與權限範圍",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            Text(
                text = "使用者檔案隸屬企業 '${enterprise?.name ?: "Nexus Enterprise"}'. Security policies active at this root level govern this identity across all Organizations and Repositories:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorder),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PolicyCheckRow(
                        title = "Segregation of Duties Enforced",
                        desc = "User cannot approve or formally review artifacts they authored.",
                        isActive = enterprise?.enforceSegregationOfDuties ?: true,
                    )
                    PolicyCheckRow(
                        title = "Dual-Approval Gate Enforced",
                        desc = "Requires independent reviewer and approver sign-offs before publishing.",
                        isActive = enterprise?.enforceDualApproval ?: true,
                    )
                    PolicyCheckRow(
                        title = "Sequential Review Order Enforced",
                        desc = "Formal review must precede final approver sign-off.",
                        isActive = enterprise?.enforceReviewerBeforeApprover ?: true,
                    )
                    PolicyCheckRow(
                        title = "Personal Repositories Permitted",
                        desc = "User can create and own personal workspace repositories.",
                        isActive = (enterprise?.allowUserOwnedRepos ?: true) && user.canOwnerRepository,
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyCheckRow(title: String, desc: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isActive) EmeraldSuccess else AmberWarning,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumEmphasis,
            )
        }
    }
}

@Composable
private fun OrganizationMembershipsSection(memberships: List<Pair<OrgMembership, Organization>>, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "組織成員關係（${memberships.size}）",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            if (memberships.isEmpty()) {
                Text(
                    text = "此使用者目前不屬於任何組織。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            } else {
                memberships.forEach { (membership, org) ->
                    val orgColor = remember(org.badgeColorHex) {
                        com.example.ui.theme.parseHexColor(org.badgeColorHex)
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(orgColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = orgColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = org.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextHighEmphasis,
                                )
                                Text(
                                    text = "org/${org.slug} • 預設儲存庫角色：${org.defaultMemberRole.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMediumEmphasis,
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LavenderContainer,
                            ) {
                                Text(
                                    text = membership.role.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LavenderPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamMembershipsSection(teamMemberships: List<Triple<TeamMembership, Team, Organization?>>, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "團隊成員與維護者角色（${teamMemberships.size}）",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            if (teamMemberships.isEmpty()) {
                Text(
                    text = "此使用者目前未加入任何協作團隊。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            } else {
                teamMemberships.forEach { (membership, team, parentOrg) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PinkAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = PinkAccent,
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = team.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextHighEmphasis,
                                )
                                Text(
                                    text = "${parentOrg?.name ?: "Org"} / ${team.slug}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMediumEmphasis,
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (membership.role ==
                                    com.example.data.model.TeamRole.MAINTAINER
                                ) {
                                    PinkAccent.copy(
                                        alpha = 0.2f,
                                    )
                                } else {
                                    SophisticatedContainer
                                },
                            ) {
                                Text(
                                    text = membership.role.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (membership.role ==
                                        com.example.data.model.TeamRole.MAINTAINER
                                    ) {
                                        PinkAccent
                                    } else {
                                        TextHighEmphasis
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoryGrantsSection(
    ownedRepos: List<Repository>,
    collaboratorRepos: List<Repository>,
    user: User,
    onSelectRepository: (Repository) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "儲存庫與工作區",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            // User-Owned Personal Repos
            if (ownedRepos.isNotEmpty()) {
                Text(
                    text = "個人工作區（由 ${user.username.uppercase()} 擁有）",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    ),
                    color = LavenderPrimary,
                )

                ownedRepos.forEach { repo ->
                    RepositorySummaryRow(
                        repo = repo,
                        roleBadge = "OWNER",
                        roleColor = LavenderPrimary,
                        onClick = { onSelectRepository(repo) },
                    )
                }
            }

            // Collaborator Grants
            if (collaboratorRepos.isNotEmpty()) {
                Text(
                    text = "協作者與組織儲存庫",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    ),
                    color = EmeraldSuccess,
                )

                collaboratorRepos.forEach { repo ->
                    RepositorySummaryRow(
                        repo = repo,
                        roleBadge = "COLLABORATOR",
                        roleColor = EmeraldSuccess,
                        onClick = { onSelectRepository(repo) },
                    )
                }
            }

            if (ownedRepos.isEmpty() && collaboratorRepos.isEmpty()) {
                Text(
                    text = "此使用者目前沒有可存取的儲存庫。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            }
        }
    }
}

@Composable
private fun RepositorySummaryRow(repo: Repository, roleBadge: String, roleColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = SophisticatedSurfaceDark,
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = LavenderPrimary,
                modifier = Modifier.size(18.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
                Text(
                    text = "${repo.ownerDisplayName}/${repo.name} • ${repo.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = roleColor.copy(alpha = 0.15f),
            ) {
                Text(
                    text = roleBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = roleColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun EffectivePermissionsMatrixCard(user: User, isEnterpriseAdmin: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Policy,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "有效權限矩陣",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            Text(
                text = "實際能力由企業、組織、團隊成員關係與儲存庫存取規則共同決定：",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorder),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PermissionMatrixRow(
                        "Create No-Code 個成果",
                        "Allowed in Collaborator/Maintainer/Owner workspaces",
                        true,
                    )
                    PermissionMatrixRow(
                        "Submit Formal Artifact Reviews",
                        "Allowed in assigned Reviewer/Collaborator scopes (Subject to Segregation of Duties)",
                        true,
                    )
                    PermissionMatrixRow(
                        "Grant Cryptographic Approvals",
                        "Allowed for Approver/Maintainer/Owner roles (Requires independent reviewer gate)",
                        true,
                    )
                    PermissionMatrixRow(
                        "Publish & Lock Production Releases",
                        "Requires dual approvals and approver sign-offs",
                        true,
                    )
                    PermissionMatrixRow(
                        "Manage Access Rules & Collaborators",
                        "Allowed for Repo Maintainers, Org Admins, and Enterprise Admins",
                        isEnterpriseAdmin,
                    )
                    PermissionMatrixRow(
                        "Configure Enterprise Policies & Audits",
                        "Restricted strictly to Enterprise Administrators",
                        isEnterpriseAdmin,
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionMatrixRow(capability: String, scope: String, isGranted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isGranted) EmeraldSuccess else RoseError,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp),
        )
        Column {
            Text(
                text = capability,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis,
            )
            Text(
                text = scope,
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumEmphasis,
            )
        }
    }
}

// =========================================================================
// CONTRIBUTIONS SUB-TAB CARDS
// =========================================================================

@Composable
private fun ProfileAuthoredArtifactsSection(
    artifacts: List<NoCodeArtifact>,
    user: User,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "建立的無程式碼成果（${artifacts.size}）",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            if (artifacts.isEmpty()) {
                Text(
                    text = "尚未由此使用者建立成果：${user.displayName}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            } else {
                artifacts.forEach { artifact ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectArtifact(artifact) },
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LavenderContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = LavenderPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = artifact.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextHighEmphasis,
                                )
                                Text(
                                    text = "${artifact.type.name} • ${artifact.summary}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMediumEmphasis,
                                    maxLines = 1,
                                )
                            }

                            ArtifactStatePill(state = artifact.lifecycleState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtifactStatePill(state: LifecycleState) {
    val (bgColor, textColor) = when (state) {
        LifecycleState.DRAFT -> Pair(SophisticatedContainer, TextMediumEmphasis)
        LifecycleState.IN_REVIEW -> Pair(AmberWarning.copy(alpha = 0.2f), AmberWarning)
        LifecycleState.PENDING_APPROVAL -> Pair(LavenderContainer, LavenderPrimary)
        LifecycleState.APPROVED -> Pair(EmeraldSuccess.copy(alpha = 0.2f), EmeraldSuccess)
        LifecycleState.PUBLISHED -> Pair(EmeraldSuccess, LavenderOnPrimary)
        LifecycleState.ARCHIVED -> Pair(SophisticatedContainer, TextLowEmphasis)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
    ) {
        Text(
            text = state.name.replace("_", " "),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ProfileReviewsSection(reviews: List<ArtifactReview>, allArtifacts: List<NoCodeArtifact>, user: User) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "已提交正式審查（${reviews.size}）",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            if (reviews.isEmpty()) {
                Text(
                    text = "此使用者尚無正式成果審查紀錄。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            } else {
                reviews.forEach { review ->
                    val linkedArtifact = allArtifacts.firstOrNull { it.id == review.artifactId }
                    val decisionColor = when (review.decision) {
                        ReviewDecision.APPROVED -> EmeraldSuccess
                        ReviewDecision.CHANGES_REQUESTED -> RoseError
                        ReviewDecision.COMMENTED -> AmberWarning
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = linkedArtifact?.title ?: "Artifact ID: ${review.artifactId.take(8)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextHighEmphasis,
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = decisionColor.copy(alpha = 0.15f),
                                ) {
                                    Text(
                                        text = review.decision.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = decisionColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                            }

                            if (review.feedbackNote.isNotEmpty()) {
                                Text(
                                    text = "\"${review.feedbackNote}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMediumEmphasis,
                                )
                            }

                            Text(
                                text = "審查於 ${dateFormat.format(Date(review.reviewedAt))}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextLowEmphasis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileApprovalsSection(approvals: List<ArtifactApproval>, allArtifacts: List<NoCodeArtifact>, user: User) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Approval,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "正式核准簽核（${approvals.size}）",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            if (approvals.isEmpty()) {
                Text(
                    text = "此使用者尚無正式簽核紀錄。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            } else {
                approvals.forEach { approval ->
                    val linkedArtifact = allArtifacts.firstOrNull { it.id == approval.artifactId }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = linkedArtifact?.title ?: "Artifact: ${approval.artifactId.take(8)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextHighEmphasis,
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.15f),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TaskAlt,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(12.dp),
                                        )
                                        Text(
                                            text = "已簽核",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = EmeraldSuccess,
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "簽章證明：${approval.signatureProof}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                ),
                                color = LavenderSubtle,
                            )

                            Text(
                                text = "簽核於 ${dateFormat.format(Date(approval.signedAt))}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextLowEmphasis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileIssuesAndDiscussionsSection(issues: List<RepoIssue>, discussions: List<RepoDiscussion>, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "任務與 RFC 討論",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            if (issues.isNotEmpty()) {
                Text(
                    text = "建立／被指派的任務（${issues.size}）",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    ),
                    color = LavenderPrimary,
                )

                issues.take(4).forEach { issue ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "#${issue.id.takeLast(4)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = LavenderPrimary,
                            )
                            Text(
                                text = issue.title,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextHighEmphasis,
                                modifier = Modifier.weight(1f),
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SophisticatedContainer,
                            ) {
                                Text(
                                    text = issue.status.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextMediumEmphasis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (discussions.isNotEmpty()) {
                Text(
                    text = "已發起的 RFC 討論（${discussions.size}）",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    ),
                    color = PinkAccent,
                )

                discussions.take(4).forEach { disc ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SophisticatedSurfaceDark,
                        border = BorderStroke(1.dp, SophisticatedBorder),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = PinkAccent,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = disc.title,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextHighEmphasis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${disc.upvoteCount} 票贊成",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextMediumEmphasis,
                            )
                        }
                    }
                }
            }

            if (issues.isEmpty() && discussions.isEmpty()) {
                Text(
                    text = "此使用者尚無任務或 RFC 討論紀錄。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            }
        }
    }
}

// =========================================================================
// SECURITY & AUTH SUB-TAB CARDS
// =========================================================================

@Composable
private fun SecurityArchitectureCalloutCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LavenderContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = LavenderPrimary,
                modifier = Modifier.size(22.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "架構責任分離",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LavenderPrimary,
                )
                Text(
                    text = "使用者檔案是企業中的集中身分表示，同時嚴格分離驗證（SAML／OIDC SSO、FIDO2 權杖）、授權（角色與權限階層）以及帳號設定。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHighEmphasis,
                )
            }
        }
    }
}

@Composable
private fun AuthenticationIdentityCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "聯邦驗證與單一登入",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileFieldRow(label = "Identity Provider (IdP)", value = user.ssoProvider)
                ProfileFieldRow(label = "Authentication Method", value = "Enterprise SAML 2.0 / OIDC")
                ProfileFieldRow(
                    label = "Domain Enforcement",
                    value = "Enforced via ${user.email.split("@").getOrElse(1) { "enterprise.internal" }}",
                )
                ProfileFieldRow(label = "Federated Status", value = user.authStatus)
            }
        }
    }
}

@Composable
private fun SessionSecurityCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "多因素驗證與安全權杖",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileFieldRow(
                    label = "Two-Factor Auth (2FA)",
                    value = if (user.twoFactorEnabled) "Active (Hardware Token)" else "Disabled",
                )
                ProfileFieldRow(
                    label = "FIDO2 / WebAuthn Security Key",
                    value = if (user.securityKeyEnforced) "Mandatory & Verified" else "Optional",
                )
                ProfileFieldRow(label = "Session Security Level", value = "High (Cryptographic Mutual TLS)")
                ProfileFieldRow(label = "Approver Key Signature", value = "ED25519 Hardware-backed")
            }
        }
    }
}

// =========================================================================
// SETTINGS SUB-TAB CARDS
// =========================================================================

@Composable
private fun AccountSettingsCard(user: User, isSelf: Boolean, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "帳號顯示與身分自訂",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileFieldRow(label = "顯示名稱", value = user.displayName)
                ProfileFieldRow(label = "Job Title / Role", value = user.title)
                ProfileFieldRow(label = "位置", value = user.location.ifEmpty { "Not specified" })
                ProfileFieldRow(label = "Pronouns", value = user.pronouns.ifEmpty { "Not specified" })
                ProfileFieldRow(label = "Avatar Color Hex", value = user.avatarColorHex)
            }

            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SophisticatedSurfaceDark,
                    contentColor = TextHighEmphasis,
                ),
                border = BorderStroke(1.dp, SophisticatedBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = LavenderPrimary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("修改個人檔案資訊", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun NotificationPreferencesCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "通知路由與政策訂閱",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SophisticatedSurfaceDark,
                border = BorderStroke(1.dp, SophisticatedBorder),
            ) {
                Text(
                    text = user.notificationPreferences,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextHighEmphasis,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

// =========================================================================
// EDIT PROFILE MODAL DIALOG
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileModal(
    user: User,
    onDismiss: () -> Unit,
    onSave: (
        displayName: String,
        title: String,
        bio: String,
        location: String,
        pronouns: String,
        avatarColor: String,
        notifPrefs: String,
    ) -> Unit,
) {
    var displayName by remember { mutableStateOf(user.displayName) }
    var title by remember { mutableStateOf(user.title) }
    var bio by remember { mutableStateOf(user.bio) }
    var location by remember { mutableStateOf(user.location) }
    var pronouns by remember { mutableStateOf(user.pronouns) }
    var avatarColorHex by remember { mutableStateOf(user.avatarColorHex) }
    var notifPrefs by remember { mutableStateOf(user.notificationPreferences) }

    val colorOptions = listOf("#6366F1", "#8B5CF6", "#EC4899", "#3B82F6", "#10B981", "#F59E0B", "#EF4444")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = LavenderPrimary,
                )
                Text(
                    text = "編輯使用者檔案",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis,
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("顯示名稱") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedTextColor = TextHighEmphasis,
                            unfocusedTextColor = TextHighEmphasis,
                        ),
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("職稱／專業角色") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedTextColor = TextHighEmphasis,
                            unfocusedTextColor = TextHighEmphasis,
                        ),
                    )
                }

                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("簡介／專注領域") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedTextColor = TextHighEmphasis,
                            unfocusedTextColor = TextHighEmphasis,
                        ),
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("位置") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedTextColor = TextHighEmphasis,
                            unfocusedTextColor = TextHighEmphasis,
                        ),
                    )
                }

                item {
                    OutlinedTextField(
                        value = pronouns,
                        onValueChange = { pronouns = it },
                        label = { Text("代名詞（例如：they/them）") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedTextColor = TextHighEmphasis,
                            unfocusedTextColor = TextHighEmphasis,
                        ),
                    )
                }

                item {
                    Text(
                        text = "頭像識別色",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMediumEmphasis,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        colorOptions.forEach { hex ->
                            val color = com.example.ui.theme.parseHexColor(hex)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (avatarColorHex == hex) 3.dp else 1.dp,
                                        if (avatarColorHex == hex) Color.White else Color.Transparent,
                                        CircleShape,
                                    )
                                    .clickable { avatarColorHex = hex },
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notifPrefs,
                        onValueChange = { notifPrefs = it },
                        label = { Text("通知訂閱") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedTextColor = TextHighEmphasis,
                            unfocusedTextColor = TextHighEmphasis,
                        ),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(displayName, title, bio, location, pronouns, avatarColorHex, notifPrefs)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderPrimary,
                    contentColor = LavenderOnPrimary,
                ),
                modifier = Modifier.testTag("btn_save_profile"),
            ) {
                Text("儲存個人檔案", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextMediumEmphasis),
            ) {
                Text("取消")
            }
        },
        containerColor = SophisticatedSurface,
        shape = RoundedCornerShape(16.dp),
    )
}
