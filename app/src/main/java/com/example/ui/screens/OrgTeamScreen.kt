package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Enterprise
import com.example.data.model.OrgMembership
import com.example.data.model.OrgRole
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedBorderSubtle
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextLowEmphasis
import com.example.ui.theme.TextMediumEmphasis

enum class OrgHubViewMode {
    ORGANIZATIONS,
    ENTERPRISE_GOVERNANCE,
    HIERARCHY_MATRIX
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrgTeamScreen(
    enterprise: Enterprise?,
    enterprises: List<Enterprise> = emptyList(),
    organizations: List<Organization>,
    teams: List<Team>,
    users: List<User>,
    repositories: List<Repository> = emptyList(),
    orgMemberships: List<OrgMembership>,
    teamMemberships: List<TeamMembership>,
    allAccessRules: List<RepoAccessRule> = emptyList(),
    activeUser: User?,
    onCreateEnterprise: (String, String, String, Boolean, Boolean, Boolean, Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onUpdateEnterprisePolicies: (Enterprise) -> Unit = {},
    onCreateEnterpriseUser: (String, String, String, String, String, Boolean, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onCreateOrganization: (String, String, String, String, String, RepoRole, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onUpdateOrganization: (Organization) -> Unit = {},
    onAddOrgMember: (String, String, OrgRole) -> Unit = { _, _, _ -> },
    onRemoveOrgMember: (String, String) -> Unit = { _, _ -> },
    onCreateTeam: (String, String, String, String, String?) -> Unit = { _, _, _, _, _ -> },
    onAddTeamMember: (String, String, TeamRole) -> Unit = { _, _, _ -> },
    onRemoveTeamMember: (String, String) -> Unit = { _, _ -> },
    onNavigateToRepo: (Repository) -> Unit = {}
) {
    var viewMode by remember { mutableStateOf(OrgHubViewMode.ORGANIZATIONS) }

    // Dialog control states
    var showCreateEnterpriseDialog by remember { mutableStateOf(false) }
    var showEditEnterprisePoliciesDialog by remember { mutableStateOf(false) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var showCreateOrgDialog by remember { mutableStateOf(false) }
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var selectedOrgForMemberAdd by remember { mutableStateOf<Organization?>(null) }
    var selectedTeamForMemberAdd by remember { mutableStateOf<Team?>(null) }
    var selectedOrgForTeamCreate by remember { mutableStateOf<Organization?>(null) }

    val activeEnterprise = enterprise ?: enterprises.firstOrNull()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Enterprise Identity & Mode Switcher
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LavenderPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CorporateFare,
                                        contentDescription = null,
                                        tint = LavenderOnPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = activeEnterprise?.name ?: "Enterprise Foundation",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextHighEmphasis
                                    )
                                    Text(
                                        text = "根治理實體 • @${activeEnterprise?.slug ?: "root"}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = LavenderPrimary
                                    )
                                }
                            }

                            Button(
                                onClick = { showCreateEnterpriseDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SophisticatedContainer,
                                    contentColor = LavenderPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("create_enterprise_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("+ 企業", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        // Mode Navigation Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SophisticatedSurfaceDark)
                                .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OrgHubViewTab(
                                title = "組織",
                                icon = Icons.Default.Apartment,
                                isSelected = viewMode == OrgHubViewMode.ORGANIZATIONS,
                                onClick = { viewMode = OrgHubViewMode.ORGANIZATIONS },
                                modifier = Modifier.weight(1f),
                                testTag = "tab_orgs_view"
                            )
                            OrgHubViewTab(
                                title = "Governance",
                                icon = Icons.Default.Security,
                                isSelected = viewMode == OrgHubViewMode.ENTERPRISE_GOVERNANCE,
                                onClick = { viewMode = OrgHubViewMode.ENTERPRISE_GOVERNANCE },
                                modifier = Modifier.weight(1f),
                                testTag = "tab_enterprise_view"
                            )
                            OrgHubViewTab(
                                title = "Hierarchy Tree",
                                icon = Icons.Default.AccountTree,
                                isSelected = viewMode == OrgHubViewMode.HIERARCHY_MATRIX,
                                onClick = { viewMode = OrgHubViewMode.HIERARCHY_MATRIX },
                                modifier = Modifier.weight(1f),
                                testTag = "tab_hierarchy_tree_view"
                            )
                        }
                    }
                }
            }

            when (viewMode) {
                OrgHubViewMode.ORGANIZATIONS -> {
                    // Quick Action Bar for Orgs
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ORGANIZATIONS (${organizations.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 11.sp
                                    ),
                                    color = TextHighEmphasis
                                )
                                Text(
                                    text = "包含團隊、成員與工作區的營運實體",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextMediumEmphasis
                                )
                            }

                            Button(
                                onClick = { showCreateOrgDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LavenderPrimary,
                                    contentColor = LavenderOnPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("create_org_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("新增組織", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    if (organizations.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Apartment, contentDescription = null, tint = TextMediumEmphasis, modifier = Modifier.size(40.dp))
                                    Text("尚未建立組織", style = MaterialTheme.typography.titleMedium, color = TextHighEmphasis)
                                    Text("建立第一個組織，用來管理團隊並擁有協作容器。", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = { showCreateOrgDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("建立組織")
                                    }
                                }
                            }
                        }
                    } else {
                        items(organizations) { org ->
                            val orgTeams = teams.filter { it.orgId == org.id }
                            val orgMems = orgMemberships.filter { it.orgId == org.id }
                            val orgRepos = repositories.filter { it.ownerType == OwnerType.ORGANIZATION && it.ownerId == org.id }

                            OrganizationCard(
                                org = org,
                                teams = orgTeams,
                                members = orgMems,
                                repositories = orgRepos,
                                allUsers = users,
                                onAddMember = { selectedOrgForMemberAdd = org },
                                onRemoveMember = { userId -> onRemoveOrgMember(org.id, userId) },
                                onCreateTeam = { selectedOrgForTeamCreate = org },
                                onAddTeamMember = { team -> selectedTeamForMemberAdd = team },
                                onRemoveTeamMember = { teamId, userId -> onRemoveTeamMember(teamId, userId) },
                                teamMemberships = teamMemberships,
                                onNavigateToRepo = onNavigateToRepo
                            )
                        }
                    }
                }

                OrgHubViewMode.ENTERPRISE_GOVERNANCE -> {
                    // Enterprise Security Posture & User Management
                    item {
                        EnterpriseGovernanceSection(
                            enterprise = activeEnterprise,
                            enterprises = enterprises,
                            users = users,
                            organizations = organizations,
                            teams = teams,
                            repositories = repositories,
                            onEditPolicies = { showEditEnterprisePoliciesDialog = true },
                            onAddUser = { showCreateUserDialog = true }
                        )
                    }
                }

                OrgHubViewMode.HIERARCHY_MATRIX -> {
                    // Full End-to-End Hierarchy Visualizer
                    item {
                        HierarchyVisualizerSection(
                            enterprise = activeEnterprise,
                            organizations = organizations,
                            teams = teams,
                            users = users,
                            orgMemberships = orgMemberships,
                            teamMemberships = teamMemberships,
                            repositories = repositories
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                when (viewMode) {
                    OrgHubViewMode.ORGANIZATIONS -> showCreateOrgDialog = true
                    OrgHubViewMode.ENTERPRISE_GOVERNANCE -> showCreateUserDialog = true
                    OrgHubViewMode.HIERARCHY_MATRIX -> showCreateOrgDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("org_hub_fab"),
            containerColor = LavenderPrimary,
            contentColor = LavenderOnPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增操作")
                Text(
                    text = when (viewMode) {
                        OrgHubViewMode.ORGANIZATIONS -> "新增組織"
                        OrgHubViewMode.ENTERPRISE_GOVERNANCE -> "Add User"
                        OrgHubViewMode.HIERARCHY_MATRIX -> "新增組織"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    if (showCreateEnterpriseDialog) {
        CreateEnterpriseDialog(
            onDismiss = { showCreateEnterpriseDialog = false },
            onCreate = { name, slug, desc, dualApp, allowUserRepos, revGate, segDuties ->
                onCreateEnterprise(name, slug, desc, dualApp, allowUserRepos, revGate, segDuties)
                showCreateEnterpriseDialog = false
            }
        )
    }

    if (showEditEnterprisePoliciesDialog && activeEnterprise != null) {
        EditEnterprisePoliciesDialog(
            enterprise = activeEnterprise,
            onDismiss = { showEditEnterprisePoliciesDialog = false },
            onSave = { updated ->
                onUpdateEnterprisePolicies(updated)
                showEditEnterprisePoliciesDialog = false
            }
        )
    }

    if (showCreateOrgDialog) {
        CreateOrganizationDialog(
            enterpriseId = activeEnterprise?.id ?: "ent_default",
            allUsers = users,
            onDismiss = { showCreateOrgDialog = false },
            onCreate = { entId, name, slug, desc, colorHex, defaultRole, ownerId ->
                onCreateOrganization(entId, name, slug, desc, colorHex, defaultRole, ownerId)
                showCreateOrgDialog = false
            }
        )
    }

    if (showCreateUserDialog && activeEnterprise != null) {
        CreateUserDialog(
            enterpriseId = activeEnterprise.id,
            onDismiss = { showCreateUserDialog = false },
            onCreate = { entId, username, displayName, email, title, isAdmin, avatarColor ->
                onCreateEnterpriseUser(entId, username, displayName, email, title, isAdmin, avatarColor)
                showCreateUserDialog = false
            }
        )
    }

    if (selectedOrgForMemberAdd != null) {
        AddOrgMemberDialog(
            organization = selectedOrgForMemberAdd!!,
            allUsers = users,
            existingMemberships = orgMemberships.filter { it.orgId == selectedOrgForMemberAdd!!.id },
            onDismiss = { selectedOrgForMemberAdd = null },
            onAdd = { userId, role ->
                onAddOrgMember(selectedOrgForMemberAdd!!.id, userId, role)
                selectedOrgForMemberAdd = null
            }
        )
    }

    if (selectedOrgForTeamCreate != null || showCreateTeamDialog) {
        val targetOrg = selectedOrgForTeamCreate ?: organizations.firstOrNull()
        if (targetOrg != null) {
            CreateTeamDialog(
                organization = targetOrg,
                organizations = organizations,
                allTeams = teams.filter { it.orgId == targetOrg.id },
                onDismiss = {
                    selectedOrgForTeamCreate = null
                    showCreateTeamDialog = false
                },
                onCreate = { orgId, name, slug, desc, parentTeamId ->
                    onCreateTeam(orgId, name, slug, desc, parentTeamId)
                    selectedOrgForTeamCreate = null
                    showCreateTeamDialog = false
                }
            )
        }
    }

    if (selectedTeamForMemberAdd != null) {
        AddTeamMemberDialog(
            team = selectedTeamForMemberAdd!!,
            allUsers = users,
            existingMemberships = teamMemberships.filter { it.teamId == selectedTeamForMemberAdd!!.id },
            onDismiss = { selectedTeamForMemberAdd = null },
            onAdd = { userId, role ->
                onAddTeamMember(selectedTeamForMemberAdd!!.id, userId, role)
                selectedTeamForMemberAdd = null
            }
        )
    }
}

// -----------------------------------------------------------------------------
// SUB-COMPONENTS & SECTIONS
// -----------------------------------------------------------------------------

@Composable
fun OrgHubViewTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LavenderPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) LavenderOnPrimary else TextMediumEmphasis,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizationCard(
    org: Organization,
    teams: List<Team>,
    members: List<OrgMembership>,
    repositories: List<Repository>,
    allUsers: List<User>,
    teamMemberships: List<TeamMembership>,
    onAddMember: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onCreateTeam: () -> Unit,
    onAddTeamMember: (Team) -> Unit,
    onRemoveTeamMember: (String, String) -> Unit,
    onNavigateToRepo: (Repository) -> Unit
) {
    var expandedSection by remember { mutableStateOf<String>("團隊") }

    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("org_card_${org.slug}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LavenderPrimary.copy(alpha = 0.15f))
                            .border(1.dp, LavenderPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Apartment, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(24.dp))
                    }

                    Column {
                        Text(
                            text = org.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "@${org.slug}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderSubtle
                            )
                            Text("•", color = TextLowEmphasis, fontSize = 10.sp)
                            Text(
                                text = "預設：${org.defaultMemberRole.name}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextMediumEmphasis
                            )
                        }
                    }
                }

                // Stats Tag Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LavenderContainer)
                        .border(1.dp, LavenderPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${members.size} 個成員 • ${teams.size} 個團隊",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LavenderPrimary
                    )
                }
            }

            Text(
                text = org.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis
            )

            // Sub-Navigation Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SophisticatedSurfaceDark)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OrgSubTab(
                    title = "Teams (${teams.size})",
                    isSelected = expandedSection == "團隊",
                    onClick = { expandedSection = "團隊" },
                    modifier = Modifier.weight(1f)
                )
                OrgSubTab(
                    title = "Members (${members.size})",
                    isSelected = expandedSection == "MEMBERS",
                    onClick = { expandedSection = "MEMBERS" },
                    modifier = Modifier.weight(1f)
                )
                OrgSubTab(
                    title = "Workspaces (${repositories.size})",
                    isSelected = expandedSection == "REPOS",
                    onClick = { expandedSection = "REPOS" },
                    modifier = Modifier.weight(1f)
                )
            }

            when (expandedSection) {
                "團隊" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "團隊名冊",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                                color = TextMediumEmphasis
                            )
                            Button(
                                onClick = onCreateTeam,
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedContainer, contentColor = LavenderPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_team_to_org_${org.slug}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("+ 團隊", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        if (teams.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SophisticatedSurfaceDark)
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "此組織下尚未建立團隊。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextLowEmphasis
                                )
                            }
                        } else {
                            teams.forEach { team ->
                                val teamMems = teamMemberships.filter { it.teamId == team.id }
                                TeamItemCard(
                                    team = team,
                                    memberships = teamMems,
                                    allUsers = allUsers,
                                    onAddMember = { onAddTeamMember(team) },
                                    onRemoveMember = { userId -> onRemoveTeamMember(team.id, userId) }
                                )
                            }
                        }
                    }
                }

                "MEMBERS" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "組織成員與治理角色",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                                color = TextMediumEmphasis
                            )
                            Button(
                                onClick = onAddMember,
                                colors = ButtonDefaults.buttonColors(containerColor = SophisticatedContainer, contentColor = LavenderPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("add_member_to_org_${org.slug}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("+ 成員", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        if (members.isEmpty()) {
                            Text("尚無成員。", color = TextLowEmphasis, style = MaterialTheme.typography.bodySmall)
                        } else {
                            members.forEach { mem ->
                                val user = allUsers.firstOrNull { it.id == mem.userId }
                                if (user != null) {
                                    OrgMemberRow(
                                        user = user,
                                        role = mem.role,
                                        onRemove = { onRemoveMember(user.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                "REPOS" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "擁有的無程式碼儲存庫",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                            color = TextMediumEmphasis
                        )

                        if (repositories.isEmpty()) {
                            Text("此組織尚未擁有任何儲存庫。", color = TextLowEmphasis, style = MaterialTheme.typography.bodySmall)
                        } else {
                            repositories.forEach { repo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SophisticatedSurfaceDark)
                                        .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable { onNavigateToRepo(repo) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                                        Column {
                                            Text(repo.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                            Text(repo.name, style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SophisticatedContainer)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("${repo.requiredApproverCount} 個簽核為必要", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = LavenderPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrgSubTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) SophisticatedContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) LavenderPrimary else TextMediumEmphasis
        )
    }
}

@Composable
fun TeamItemCard(
    team: Team,
    memberships: List<TeamMembership>,
    allUsers: List<User>,
    onAddMember: () -> Unit,
    onRemoveMember: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SophisticatedSurfaceDark)
            .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(LavenderPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighEmphasis
                        )
                        Text(
                            text = "@${team.slug}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMediumEmphasis
                        )
                    }
                }

                IconButton(
                    onClick = onAddMember,
                    modifier = Modifier.size(28.dp).testTag("add_user_to_team_${team.slug}")
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "新增成員", tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                }
            }

            Text(
                text = team.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextMediumEmphasis
            )

            // Team 個成員 roster pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                memberships.forEach { tm ->
                    val u = allUsers.firstOrNull { it.id == tm.userId }
                    if (u != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SophisticatedContainer)
                                .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${u.displayName} (${tm.role.name})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = LavenderSubtle
                                )
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "移除",
                                    tint = TextLowEmphasis,
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clickable { onRemoveMember(u.id) }
                                )
                            }
                        }
                    }
                }
                if (memberships.isEmpty()) {
                    Text(
                        text = "尚未指派團隊成員",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextLowEmphasis
                    )
                }
            }
        }
    }
}

@Composable
fun OrgMemberRow(
    user: User,
    role: OrgRole,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SophisticatedSurfaceDark)
            .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(user.avatarColorHex))
                        } catch (e: Exception) {
                            LavenderPrimary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Column {
                Text(user.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                Text("@${user.username} • ${user.title}", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (role) {
                            OrgRole.OWNER -> PinkAccent.copy(alpha = 0.2f)
                            OrgRole.ADMIN -> LavenderPrimary.copy(alpha = 0.2f)
                            OrgRole.BILLING_MANAGER -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            OrgRole.MEMBER -> SophisticatedContainer
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = role.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = when (role) {
                        OrgRole.OWNER -> PinkAccent
                        OrgRole.ADMIN -> LavenderPrimary
                        OrgRole.BILLING_MANAGER -> Color(0xFFF59E0B)
                        OrgRole.MEMBER -> LavenderSubtle
                    }
                )
            }

            if (role != OrgRole.OWNER) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "移除", tint = TextLowEmphasis, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ENTERPRISE GOVERNANCE SECTION
// -----------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnterpriseGovernanceSection(
    enterprise: Enterprise?,
    enterprises: List<Enterprise>,
    users: List<User>,
    organizations: List<Organization>,
    teams: List<Team>,
    repositories: List<Repository>,
    onEditPolicies: () -> Unit,
    onAddUser: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Enterprise Security Posture Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LavenderContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("企業治理政策", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                            Text("根層守規關卡與存取控制繼承", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.8.sp), color = TextMediumEmphasis)
                        }
                    }

                    Button(
                        onClick = onEditPolicies,
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("edit_enterprise_policies_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("設定", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Security Policy Gates Summary
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PolicyGateRow(
                        title = "職責分離（SoD）",
                        description = "Strictly prohibits artifact authors from reviewing or granting final release approvals to their own work.",
                        isEnabled = enterprise?.enforceSegregationOfDuties ?: true
                    )
                    PolicyGateRow(
                        title = "Dual-Approval Gate (Four-Eyes Principle)",
                        description = "Mandates two independent sign-offs by designated Approver roles for release promotion.",
                        isEnabled = enterprise?.enforceDualApproval ?: true
                    )
                    PolicyGateRow(
                        title = "Peer Reviewer Gate Required",
                        description = "Requires at least one formal peer review prior to final Approver release promotion.",
                        isEnabled = enterprise?.enforceReviewerBeforeApprover ?: true
                    )
                    PolicyGateRow(
                        title = "User-Owned Repositories Allowed",
                        description = "Permits enterprise members to create and govern personal no-code collaboration containers.",
                        isEnabled = enterprise?.allowUserOwnedRepos ?: true
                    )
                }
            }
        }

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(label = "組織", count = organizations.size.toString(), icon = Icons.Default.Apartment, modifier = Modifier.weight(1f))
            MetricCard(label = "團隊", count = teams.size.toString(), icon = Icons.Default.Groups, modifier = Modifier.weight(1f))
            MetricCard(label = "Identities", count = users.size.toString(), icon = Icons.Default.Person, modifier = Modifier.weight(1f))
            MetricCard(label = "Workspaces", count = repositories.size.toString(), icon = Icons.Default.Folder, modifier = Modifier.weight(1f))
        }

        // Enterprise User Directory
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("企業身分名冊（${users.size}）", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                        Text("已建立的所有帳號皆位於 ${enterprise?.name ?: "企業"}", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
                    }

                    Button(
                        onClick = onAddUser,
                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedContainer, contentColor = LavenderPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_user_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("+ 新增使用者", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                users.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SophisticatedSurfaceDark)
                            .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(user.avatarColorHex))
                                        } catch (e: Exception) {
                                            LavenderPrimary
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    user.displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(user.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                Text("@${user.username} • ${user.title}", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                            }
                        }

                        if (user.isEnterpriseAdmin) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LavenderContainer)
                                    .border(1.dp, LavenderPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("企業管理員", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = LavenderPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PolicyGateRow(
    title: String,
    description: String,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SophisticatedSurfaceDark)
            .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isEnabled) LavenderPrimary.copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isEnabled) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isEnabled) LavenderPrimary else Color(0xFFEF4444),
                modifier = Modifier.size(14.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
            Text(description, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 15.sp), color = TextMediumEmphasis)
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(18.dp))
            Text(count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMediumEmphasis)
        }
    }
}

// -----------------------------------------------------------------------------
// HIERARCHY VISUALIZER SECTION
// -----------------------------------------------------------------------------

@Composable
fun HierarchyVisualizerSection(
    enterprise: Enterprise?,
    organizations: List<Organization>,
    teams: List<Team>,
    users: List<User>,
    orgMemberships: List<OrgMembership>,
    teamMemberships: List<TeamMembership>,
    repositories: List<Repository>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LavenderContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("階層模型與權限矩陣", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                    Text("閉環多層級存取控制授權", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.8.sp), color = TextMediumEmphasis)
                }
            }

            // Level 1: Root Enterprise
            HierarchyLevelNode(
                levelNumber = "LEVEL 1",
                levelName = "Enterprise Domain",
                nodeTitle = enterprise?.name ?: "Enterprise Root",
                nodeSubtitle = "Highest-level boundary. Enforces Segregation of Duties, Dual Approval, and User Ownership policies.",
                badgeText = "ROOT AUTHORITY",
                badgeColor = LavenderPrimary
            )

            // Level 2: Organizations
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "第 2 層：組織（${organizations.size}）",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = LavenderPrimary
                )

                organizations.forEach { org ->
                    val orgTeams = teams.filter { it.orgId == org.id }
                    val orgRepos = repositories.filter { it.ownerType == OwnerType.ORGANIZATION && it.ownerId == org.id }
                    val orgMems = orgMemberships.filter { it.orgId == org.id }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Apartment, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                                    Text(org.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                }
                                Text("預設：${org.defaultMemberRole.name}", style = MaterialTheme.typography.labelSmall, color = LavenderSubtle)
                            }

                            // Level 3: Nested 個團隊 inside Org
                            if (orgTeams.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("第 3 層：團隊（隸屬組織）", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = TextMediumEmphasis)
                                    orgTeams.forEach { team ->
                                        val teamMems = teamMemberships.filter { it.teamId == team.id }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SophisticatedContainer)
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Groups, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                                                Text(team.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                            }
                                            Text("${teamMems.size} 個成員", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMediumEmphasis)
                                        }
                                    }
                                }
                            }

                            // Owned Repos
                            if (orgRepos.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.padding(start = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("第 4 層：工作區（由組織擁有）", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = TextMediumEmphasis)
                                    orgRepos.forEach { repo ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SophisticatedSurface)
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Folder, contentDescription = null, tint = PinkAccent, modifier = Modifier.size(14.dp))
                                            Text(repo.displayName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                            Text("(${repo.category})", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMediumEmphasis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HierarchyLevelNode(
    levelNumber: String,
    levelName: String,
    nodeTitle: String,
    nodeSubtitle: String,
    badgeText: String,
    badgeColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$levelNumber: $levelName",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                    color = LavenderPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(badgeText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = badgeColor)
                }
            }
            Text(nodeTitle, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
            Text(nodeSubtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp), color = TextMediumEmphasis)
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG IMPLEMENTATIONS
// -----------------------------------------------------------------------------

@Composable
fun CreateEnterpriseDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dualApproval by remember { mutableStateOf(true) }
    var allowUserRepos by remember { mutableStateOf(true) }
    var revGate by remember { mutableStateOf(true) }
    var segDuties by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("建立新企業", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LavenderPrimary)
                Text("建立具自訂治理基準的根組織邊界。", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (slug.isEmpty() || slug == name.lowercase().replace(" ", "-").dropLast(1)) {
                            slug = it.lowercase().replace(" ", "-")
                        }
                    },
                    label = { Text("企業名稱") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ent_name_input")
                )

                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it.lowercase().replace(" ", "-") },
                    label = { Text("Slug 識別碼") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ent_slug_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("治理目的") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("ent_desc_input")
                )

                Text("安全治理政策", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("職責分離（SoD）", style = MaterialTheme.typography.bodySmall, color = TextHighEmphasis)
                    Switch(checked = segDuties, onCheckedChange = { segDuties = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("雙核准人關卡", style = MaterialTheme.typography.bodySmall, color = TextHighEmphasis)
                    Switch(checked = dualApproval, onCheckedChange = { dualApproval = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("簽核前審查者關卡", style = MaterialTheme.typography.bodySmall, color = TextHighEmphasis)
                    Switch(checked = revGate, onCheckedChange = { revGate = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("允許使用者擁有儲存庫", style = MaterialTheme.typography.bodySmall, color = TextHighEmphasis)
                    Switch(checked = allowUserRepos, onCheckedChange = { allowUserRepos = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name, slug, description, dualApproval, allowUserRepos, revGate, segDuties)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("submit_create_enterprise_button")
                    ) {
                        Text("建立企業")
                    }
                }
            }
        }
    }
}

@Composable
fun EditEnterprisePoliciesDialog(
    enterprise: Enterprise,
    onDismiss: () -> Unit,
    onSave: (Enterprise) -> Unit
) {
    var segDuties by remember { mutableStateOf(enterprise.enforceSegregationOfDuties) }
    var dualApp by remember { mutableStateOf(enterprise.enforceDualApproval) }
    var revGate by remember { mutableStateOf(enterprise.enforceReviewerBeforeApprover) }
    var allowUserRepos by remember { mutableStateOf(enterprise.allowUserOwnedRepos) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("設定治理政策", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LavenderPrimary)
                Text("變更會立即套用至 ${enterprise.name} 下所有組織與儲存庫。", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("職責分離（SoD）", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                        Text("禁止作者審查或核准自己的成果", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                    }
                    Switch(checked = segDuties, onCheckedChange = { segDuties = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("雙重核准關卡", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                        Text("發布提升需取得 2 個簽核", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                    }
                    Switch(checked = dualApp, onCheckedChange = { dualApp = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("同儕審查關卡", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                        Text("核准人簽核前必須先通過審查", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                    }
                    Switch(checked = revGate, onCheckedChange = { revGate = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("使用者擁有的工作區", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                        Text("允許非組織工作區", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                    }
                    Switch(checked = allowUserRepos, onCheckedChange = { allowUserRepos = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                enterprise.copy(
                                    enforceSegregationOfDuties = segDuties,
                                    enforceDualApproval = dualApp,
                                    enforceReviewerBeforeApprover = revGate,
                                    allowUserOwnedRepos = allowUserRepos
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                        modifier = Modifier.testTag("save_enterprise_policies_button")
                    ) {
                        Text("儲存政策")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateOrganizationDialog(
    enterpriseId: String,
    allUsers: List<User>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, RepoRole, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4F46E5") }
    var selectedRole by remember { mutableStateOf(RepoRole.COLLABORATOR) }
    var selectedOwnerId by remember { mutableStateOf(allUsers.firstOrNull()?.id ?: "") }

    val colorOptions = listOf("#4F46E5", "#059669", "#EC4899", "#8B5CF6", "#F59E0B", "#06B6D4")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("建立組織", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LavenderPrimary)
                Text("建立可管理團隊並擁有工作區的專屬營運實體。", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (slug.isEmpty() || slug == name.lowercase().replace(" ", "-").dropLast(1)) {
                            slug = it.lowercase().replace(" ", "-")
                        }
                    },
                    label = { Text("組織名稱（例如：Fintech & Payments）") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("org_name_input")
                )

                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it.lowercase().replace(" ", "-") },
                    label = { Text("Slug 識別碼（@fintech-payments）") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("org_slug_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("組織使命與範圍") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("org_desc_input")
                )

                Text("成員預設儲存庫角色", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(RepoRole.VIEWER, RepoRole.COLLABORATOR, RepoRole.REVIEWER).forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LavenderPrimary else SophisticatedSurfaceDark)
                                .clickable { selectedRole = role }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                role.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis
                            )
                        }
                    }
                }

                Text("初始組織擁有者", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    allUsers.forEach { user ->
                        val isSelected = user.id == selectedOwnerId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark)
                                .clickable { selectedOwnerId = user.id }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { selectedOwnerId = user.id }, colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary))
                            Column {
                                Text(user.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                Text("@${user.username} • ${user.title}", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(enterpriseId, name, slug, description, selectedColor, selectedRole, selectedOwnerId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("submit_create_org_button")
                    ) {
                        Text("建立組織")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateUserDialog(
    enterpriseId: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, Boolean, String) -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("建立企業使用者", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LavenderPrimary)

                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        if (username.isEmpty() || username == displayName.lowercase().replace(" ", "_").dropLast(1)) {
                            username = it.lowercase().replace(" ", "_")
                        }
                        if (email.isEmpty() || email == "${username}@acme.io".dropLast(9)) {
                            email = "${username}@acme.io"
                        }
                    },
                    label = { Text("顯示名稱（例如：Maya Lin）") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("user_name_input")
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.lowercase().replace(" ", "_") },
                    label = { Text("使用者帳號") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("user_handle_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("企業電子郵件") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("user_email_input")
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("職稱／專業角色") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("user_title_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("企業管理員權限", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                        Text("授予最高層管理權限", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                    }
                    Switch(checked = isAdmin, onCheckedChange = { isAdmin = it }, colors = SwitchDefaults.colors(checkedThumbColor = LavenderPrimary))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (displayName.isNotBlank() && username.isNotBlank()) {
                                onCreate(enterpriseId, username, displayName, email, title, isAdmin, "#8B5CF6")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                        enabled = displayName.isNotBlank(),
                        modifier = Modifier.testTag("submit_create_user_button")
                    ) {
                        Text("建立身分")
                    }
                }
            }
        }
    }
}

@Composable
fun AddOrgMemberDialog(
    organization: Organization,
    allUsers: List<User>,
    existingMemberships: List<OrgMembership>,
    onDismiss: () -> Unit,
    onAdd: (String, OrgRole) -> Unit
) {
    val existingUserIds = existingMemberships.map { it.userId }.toSet()
    val availableUsers = allUsers.filter { it.id !in existingUserIds }
    var selectedUserId by remember { mutableStateOf(availableUsers.firstOrNull()?.id ?: "") }
    var selectedRole by remember { mutableStateOf(OrgRole.MEMBER) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("新增成員至『${organization.name}'", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LavenderPrimary)

                if (availableUsers.isEmpty()) {
                    Text("所有企業使用者都已是此組織成員。", color = TextMediumEmphasis, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("關閉", color = LavenderPrimary) }
                } else {
                    Text("選擇使用者", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        availableUsers.forEach { user ->
                            val isSelected = user.id == selectedUserId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark)
                                    .clickable { selectedUserId = user.id }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedUserId = user.id }, colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary))
                                Column {
                                    Text(user.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                    Text("@${user.username} • ${user.title}", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                                }
                            }
                        }
                    }

                    Text("指派組織角色", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(OrgRole.MEMBER, OrgRole.BILLING_MANAGER, OrgRole.ADMIN, OrgRole.OWNER).forEach { role ->
                            val isSelected = selectedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LavenderPrimary else SophisticatedSurfaceDark)
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    role.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (selectedUserId.isNotBlank()) {
                                    onAdd(selectedUserId, selectedRole)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                            modifier = Modifier.testTag("confirm_add_org_member")
                        ) {
                            Text("新增成員")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTeamDialog(
    organization: Organization,
    organizations: List<Organization>,
    allTeams: List<Team>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var parentTeamId by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("在『${organization.name}'", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LavenderPrimary)
                Text("團隊是組織範圍內的群組，可取得儲存庫權限。", style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (slug.isEmpty() || slug == name.lowercase().replace(" ", "-").dropLast(1)) {
                            slug = it.lowercase().replace(" ", "-")
                        }
                    },
                    label = { Text("團隊名稱（例如：Core Infrastructure）") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("team_name_input")
                )

                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it.lowercase().replace(" ", "-") },
                    label = { Text("Slug 識別碼（@core-infra）") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("team_slug_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("團隊目的與使命") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("team_desc_input")
                )

                if (allTeams.isNotEmpty()) {
                    Text("隸屬上層團隊（選填）", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (parentTeamId == null) SophisticatedContainer else SophisticatedSurfaceDark)
                                .clickable { parentTeamId = null }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = parentTeamId == null, onClick = { parentTeamId = null }, colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary))
                            Text("最上層團隊（無上層）", style = MaterialTheme.typography.bodySmall, color = TextHighEmphasis)
                        }

                        allTeams.forEach { parent ->
                            val isSelected = parent.id == parentTeamId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark)
                                    .clickable { parentTeamId = parent.id }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { parentTeamId = parent.id }, colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary))
                                Text("${parent.name} (@${parent.slug})", style = MaterialTheme.typography.bodySmall, color = TextHighEmphasis)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(organization.id, name, slug, description, parentTeamId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("submit_create_team_button")
                    ) {
                        Text("建立團隊")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTeamMemberDialog(
    team: Team,
    allUsers: List<User>,
    existingMemberships: List<TeamMembership>,
    onDismiss: () -> Unit,
    onAdd: (String, TeamRole) -> Unit
) {
    val existingUserIds = existingMemberships.map { it.userId }.toSet()
    val availableUsers = allUsers.filter { it.id !in existingUserIds }
    var selectedUserId by remember { mutableStateOf(availableUsers.firstOrNull()?.id ?: "") }
    var selectedRole by remember { mutableStateOf(TeamRole.MEMBER) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("新增成員至『${team.name}'", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LavenderPrimary)

                if (availableUsers.isEmpty()) {
                    Text("所有使用者都已加入此團隊。", color = TextMediumEmphasis, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("關閉", color = LavenderPrimary) }
                } else {
                    Text("選擇使用者", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        availableUsers.forEach { user ->
                            val isSelected = user.id == selectedUserId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark)
                                    .clickable { selectedUserId = user.id }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedUserId = user.id }, colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary))
                                Column {
                                    Text(user.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                    Text("@${user.username} • ${user.title}", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                                }
                            }
                        }
                    }

                    Text("團隊角色", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(TeamRole.MEMBER, TeamRole.MAINTAINER).forEach { role ->
                            val isSelected = selectedRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LavenderPrimary else SophisticatedSurfaceDark)
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    role.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("取消", color = TextMediumEmphasis) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (selectedUserId.isNotBlank()) {
                                    onAdd(selectedUserId, selectedRole)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = LavenderOnPrimary),
                            modifier = Modifier.testTag("confirm_add_team_member")
                        ) {
                            Text("新增成員")
                        }
                    }
                }
            }
        }
    }
}
