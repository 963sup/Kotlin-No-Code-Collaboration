package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.AuditLog
import com.example.data.model.Enterprise
import com.example.data.model.GovernanceAction
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.OrgRole
import com.example.data.model.Organization
import com.example.data.model.PolicyEvaluationDetail
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoDiscussion
import com.example.data.model.RepoIssue
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.TeamRole
import com.example.data.model.User
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextMediumEmphasis

enum class MeSubTab(val label: String, val icon: ImageVector, val tag: String) {
    PROFILE("Profile & Identity", Icons.Default.Person, "me_tab_profile"),
    ORGS_AND_TEAMS("Orgs & 個團隊", Icons.Default.CorporateFare, "me_tab_orgs"),
    POLICIES("Policies & Security", Icons.Default.Policy, "me_tab_policies"),
    AUDIT("Audit Trail", Icons.Default.History, "me_tab_audit"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    currentSubTab: MeSubTab,
    onSubTabChange: (MeSubTab) -> Unit,
    activeUser: User?,
    inspectedProfileUser: User?,
    allUsers: List<User>,
    enterprise: Enterprise?,
    enterprises: List<Enterprise>,
    organizations: List<Organization>,
    teams: List<Team>,
    repositories: List<Repository>,
    allAccessRules: List<RepoAccessRule>,
    allOrgMemberships: List<OrgMembership>,
    allTeamMemberships: List<TeamMembership>,
    allArtifacts: List<NoCodeArtifact>,
    profileUserArtifacts: List<NoCodeArtifact>,
    profileUserReviews: List<ArtifactReview>,
    profileUserApprovals: List<ArtifactApproval>,
    profileUserAuditLogs: List<AuditLog>,
    profileUserIssues: List<RepoIssue>,
    profileUserDiscussions: List<RepoDiscussion>,
    auditLogs: List<AuditLog>,
    simulationResult: PolicyEvaluationDetail?,
    onSelectUserToInspect: (User) -> Unit,
    onSwitchActivePersona: (User) -> Unit,
    onSelectRepository: (Repository) -> Unit,
    onSelectArtifact: (NoCodeArtifact) -> Unit,
    onUpdateProfile: (User, String, String, String, String, String, String, String) -> Unit,
    onCreateEnterprise: (String, String, String, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUpdateEnterprisePolicies: (Enterprise) -> Unit,
    onCreateEnterpriseUser: (String, String, String, String, String, Boolean, String) -> Unit,
    onCreateOrganization: (String, String, String, String, String, RepoRole, String) -> Unit,
    onUpdateOrganization: (Organization) -> Unit,
    onAddOrgMember: (String, String, OrgRole) -> Unit,
    onRemoveOrgMember: (String, String) -> Unit,
    onCreateTeam: (String, String, String, String, String?) -> Unit,
    onAddTeamMember: (String, String, TeamRole) -> Unit,
    onRemoveTeamMember: (String, String) -> Unit,
    onRunPolicySimulation: (User, Repository, NoCodeArtifact?, GovernanceAction) -> Unit,
    onClearPolicySimulation: () -> Unit,
    onUpdatePolicySettings: (Boolean, Boolean, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBg)
            .testTag("me_screen"),
    ) {
        // Top Sub-Navigation Tab Row
        ScrollableTabRow(
            selectedTabIndex = currentSubTab.ordinal,
            containerColor = SophisticatedSurfaceDark,
            contentColor = LavenderPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentSubTab.ordinal]),
                    color = LavenderPrimary,
                )
            },
            divider = {},
            modifier = Modifier.fillMaxWidth().testTag("me_sub_tab_row"),
        ) {
            MeSubTab.values().forEach { tab ->
                val selected = currentSubTab == tab
                Tab(
                    selected = selected,
                    onClick = { onSubTabChange(tab) },
                    text = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                            ),
                            color = if (selected) LavenderPrimary else TextMediumEmphasis,
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(16.dp),
                            tint = if (selected) LavenderPrimary else TextMediumEmphasis,
                        )
                    },
                    modifier = Modifier.testTag(tab.tag),
                )
            }
        }

        // Sub-screen rendering
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentSubTab) {
                MeSubTab.PROFILE -> {
                    val targetUser = inspectedProfileUser ?: activeUser
                    if (targetUser != null) {
                        UserProfileScreen(
                            user = targetUser,
                            allUsers = allUsers,
                            enterprise = enterprise,
                            organizations = organizations,
                            teams = teams,
                            repositories = repositories,
                            allAccessRules = allAccessRules,
                            allOrgMemberships = allOrgMemberships,
                            allTeamMemberships = allTeamMemberships,
                            allArtifacts = allArtifacts,
                            userArtifacts = profileUserArtifacts,
                            userReviews = profileUserReviews,
                            userApprovals = profileUserApprovals,
                            userAuditLogs = profileUserAuditLogs,
                            userIssues = profileUserIssues,
                            userDiscussions = profileUserDiscussions,
                            activeUser = activeUser,
                            onSelectUserToInspect = onSelectUserToInspect,
                            onSwitchActivePersona = onSwitchActivePersona,
                            onSelectRepository = onSelectRepository,
                            onSelectArtifact = onSelectArtifact,
                            onUpdateProfile = onUpdateProfile,
                        )
                    }
                }

                MeSubTab.ORGS_AND_TEAMS -> {
                    OrgTeamScreen(
                        enterprise = enterprise,
                        enterprises = enterprises,
                        organizations = organizations,
                        teams = teams,
                        users = allUsers,
                        repositories = repositories,
                        orgMemberships = allOrgMemberships,
                        teamMemberships = allTeamMemberships,
                        allAccessRules = allAccessRules,
                        activeUser = activeUser,
                        onCreateEnterprise = onCreateEnterprise,
                        onUpdateEnterprisePolicies = onUpdateEnterprisePolicies,
                        onCreateEnterpriseUser = onCreateEnterpriseUser,
                        onCreateOrganization = onCreateOrganization,
                        onUpdateOrganization = onUpdateOrganization,
                        onAddOrgMember = onAddOrgMember,
                        onRemoveOrgMember = onRemoveOrgMember,
                        onCreateTeam = onCreateTeam,
                        onAddTeamMember = onAddTeamMember,
                        onRemoveTeamMember = onRemoveTeamMember,
                        onNavigateToRepo = onSelectRepository,
                    )
                }

                MeSubTab.POLICIES -> {
                    PolicySimulatorScreen(
                        enterprise = enterprise,
                        users = allUsers,
                        repositories = repositories,
                        artifacts = allArtifacts,
                        simulationResult = simulationResult,
                        onRunSimulation = onRunPolicySimulation,
                        onClearSimulation = onClearPolicySimulation,
                        onUpdateEnterprisePolicies = onUpdatePolicySettings,
                    )
                }

                MeSubTab.AUDIT -> {
                    AuditLogScreen(
                        auditLogs = auditLogs,
                    )
                }
            }
        }
    }
}
