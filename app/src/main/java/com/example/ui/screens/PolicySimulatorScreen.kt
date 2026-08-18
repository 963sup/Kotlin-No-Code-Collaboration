package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.data.model.Enterprise
import com.example.data.model.GovernanceAction
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OwnerType
import com.example.data.model.PolicyEvaluationDetail
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.User
import com.example.ui.components.PolicyTraceDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.RoseError
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicySimulatorScreen(
    enterprise: Enterprise?,
    users: List<User>,
    repositories: List<Repository>,
    artifacts: List<NoCodeArtifact>,
    simulationResult: PolicyEvaluationDetail?,
    onRunSimulation: (User, Repository, NoCodeArtifact?, GovernanceAction) -> Unit,
    onClearSimulation: () -> Unit,
    onUpdateEnterprisePolicies: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var selectedActorId by remember(users) { mutableStateOf(users.firstOrNull()?.id ?: "") }
    var selectedRepoId by remember(repositories) { mutableStateOf(repositories.firstOrNull()?.id ?: "") }
    var selectedArtifactId by remember(artifacts) { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf(GovernanceAction.SUBMIT_FINAL_APPROVAL) }

    var actorExpanded by remember { mutableStateOf(false) }
    var repoExpanded by remember { mutableStateOf(false) }
    var artifactExpanded by remember { mutableStateOf(false) }
    var actionExpanded by remember { mutableStateOf(false) }

    val selectedActor = users.firstOrNull { it.id == selectedActorId } ?: users.firstOrNull()
    val selectedRepo = repositories.firstOrNull { it.id == selectedRepoId } ?: repositories.firstOrNull()
    val repoArtifacts = artifacts.filter { it.repoId == selectedRepo?.id }
    val selectedArtifact = repoArtifacts.firstOrNull { it.id == selectedArtifactId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateDark900),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Policy, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            text = "Access Policy Engine & Simulator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Reverse-Engineered GitHub Enterprise Access Control Semantics",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent
                        )
                    }
                }
            }
        }

        // Schema Hierarchy Mapping View
        SchemaHierarchyVisualizer()

        // Enterprise Compliance Guardrails Toggles
        if (enterprise != null) {
            EnterprisePolicyControlsCard(
                enterprise = enterprise,
                onUpdate = onUpdateEnterprisePolicies
            )
        }

        // Interactive Simulation Playground Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Live Access Evaluation Inspector",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "Simulate how enterprise policies, team inheritances, and segregation of duties resolve for any combination of Actor, Repository, and Action.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                // Actor Selector
                ExposedDropdownMenuBox(
                    expanded = actorExpanded,
                    onExpandedChange = { actorExpanded = !actorExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedActor?.displayName ?: ""} (${selectedActor?.title ?: ""})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Actor (User Persona)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actorExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("simulator_actor_selector"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = actorExpanded,
                        onDismissRequest = { actorExpanded = false },
                        modifier = Modifier.background(SlateDark900)
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.displayName} • ${user.title}", color = Color.White) },
                                onClick = {
                                    selectedActorId = user.id
                                    actorExpanded = false
                                }
                            )
                        }
                    }
                }

                // Repository Selector
                ExposedDropdownMenuBox(
                    expanded = repoExpanded,
                    onExpandedChange = { repoExpanded = !repoExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedRepo?.displayName ?: ""} [${selectedRepo?.ownerType?.displayName() ?: ""}]",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Repository Container") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repoExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("simulator_repo_selector"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = repoExpanded,
                        onDismissRequest = { repoExpanded = false },
                        modifier = Modifier.background(SlateDark900)
                    ) {
                        repositories.forEach { repo ->
                            DropdownMenuItem(
                                text = { Text("${repo.displayName} (${repo.ownerDisplayName})", color = Color.White) },
                                onClick = {
                                    selectedRepoId = repo.id
                                    selectedArtifactId = ""
                                    repoExpanded = false
                                }
                            )
                        }
                    }
                }

                // Artifact Selector
                ExposedDropdownMenuBox(
                    expanded = artifactExpanded,
                    onExpandedChange = { artifactExpanded = !artifactExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedArtifact?.title ?: "None (Evaluate on Repository Scope)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target No-Code Artifact (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = artifactExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = artifactExpanded,
                        onDismissRequest = { artifactExpanded = false },
                        modifier = Modifier.background(SlateDark900)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Repository Scope)", color = Color.White) },
                            onClick = {
                                selectedArtifactId = ""
                                artifactExpanded = false
                            }
                        )
                        repoArtifacts.forEach { art ->
                            DropdownMenuItem(
                                text = { Text("${art.title} [${art.lifecycleState.name}]", color = Color.White) },
                                onClick = {
                                    selectedArtifactId = art.id
                                    artifactExpanded = false
                                }
                            )
                        }
                    }
                }

                // Action Selector
                ExposedDropdownMenuBox(
                    expanded = actionExpanded,
                    onExpandedChange = { actionExpanded = !actionExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedAction.label} (Requires ${selectedAction.minimumRole.name}+)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Governance Action to Test") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("simulator_action_selector"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = actionExpanded,
                        onDismissRequest = { actionExpanded = false },
                        modifier = Modifier.background(SlateDark900)
                    ) {
                        GovernanceAction.values().forEach { act ->
                            DropdownMenuItem(
                                text = { Text("${act.label} (Min: ${act.minimumRole.name})", color = Color.White) },
                                onClick = {
                                    selectedAction = act
                                    actionExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (selectedActor != null && selectedRepo != null) {
                            onRunSimulation(selectedActor, selectedRepo, selectedArtifact, selectedAction)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_policy_simulation_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Evaluate Access Policy", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (simulationResult != null) {
        PolicyTraceDialog(
            evaluation = simulationResult,
            onDismiss = onClearSimulation
        )
    }
}

@Composable
fun SchemaHierarchyVisualizer() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                Text(
                    text = "Hierarchy & Entity Schema Mapping",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Text(
                text = "Strict structural constraints reverse-engineered from GitHub enterprise governance:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            // Entity Tree Nodes
            SchemaNodeItem(
                level = 1,
                entity = "企業",
                roleScope = "Admin, Compliance Auditor",
                detail = "Root governance scope; enforces mandatory dual approver & segregation policies across all child Orgs.",
                color = PurpleGlow
            )

            SchemaNodeItem(
                level = 2,
                entity = "組織",
                roleScope = "Org Owner, Admin, Member, Billing",
                detail = "Primary container; CAN Owner Repositories. Groups Teams and inherits base permissions.",
                color = IndigoLight
            )

            SchemaNodeItem(
                level = 3,
                entity = "Team (under Org)",
                roleScope = "Team Maintainer, Team Member",
                detail = "Groups users for permission inheritance; CANNOT Owner Repositories (Strictly enforced).",
                color = AmberWarning
            )

            SchemaNodeItem(
                level = 3,
                entity = "User (Account)",
                roleScope = "Personal Account, Member, Collaborator",
                detail = "Independent persona; CAN Owner personal Repositories or be granted roles in Orgs/Teams.",
                color = CyanAccent
            )

            SchemaNodeItem(
                level = 4,
                entity = "Repository (No-Code)",
                roleScope = "Owner, Maintainer, Approver, Reviewer, Collaborator, Viewer",
                detail = "No-Code Collaboration Container. Owned strictly by Organization OR User.",
                color = EmeraldSuccess
            )

            SchemaNodeItem(
                level = 5,
                entity = "無程式碼成果",
                roleScope = "Draft -> In Review -> Pending Sign-Off -> Published",
                detail = "Workflows, RFC Specs, Decision Records, Form Schemas, Milestone Releases.",
                color = Color(0xFFA5F3FC)
            )
        }
    }
}

@Composable
fun SchemaNodeItem(
    level: Int,
    entity: String,
    roleScope: String,
    detail: String,
    color: Color
) {
    val indent = (level - 1) * 12

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .align(Alignment.CenterVertically)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entity,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = roleScope,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = Color(0xFFCBD5E1),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun EnterprisePolicyControlsCard(
    enterprise: Enterprise,
    onUpdate: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var dualApproval by remember(enterprise) { mutableStateOf(enterprise.enforceDualApproval) }
    var allowUserRepos by remember(enterprise) { mutableStateOf(enterprise.allowUserOwnedRepos) }
    var reviewerGate by remember(enterprise) { mutableStateOf(enterprise.enforceReviewerBeforeApprover) }
    var segregation by remember(enterprise) { mutableStateOf(enterprise.enforceSegregationOfDuties) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(20.dp))
                Text(
                    text = "Enterprise Compliance Guardrails",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Text(
                text = "These global policies cascade strictly down to all Organizations, Teams, and Repositories:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            PolicySwitchRow(
                title = "Mandatory Dual Approver Quorum",
                subtitle = "Requires >= 2 distinct Approver signatures on all publish actions.",
                checked = dualApproval,
                onCheckedChange = {
                    dualApproval = it
                    onUpdate(dualApproval, allowUserRepos, reviewerGate, segregation)
                },
                testTag = "toggle_dual_approval"
            )

            PolicySwitchRow(
                title = "Allow User-Owned Repositories",
                subtitle = "If disabled, only Organizations are allowed to own repository workspaces.",
                checked = allowUserRepos,
                onCheckedChange = {
                    allowUserRepos = it
                    onUpdate(dualApproval, allowUserRepos, reviewerGate, segregation)
                },
                testTag = "toggle_user_owned_repos"
            )

            PolicySwitchRow(
                title = "Enforce Reviewer Gate Before Approver",
                subtitle = "Peer Reviewer sign-off is mandatory before Approver signatures can be granted.",
                checked = reviewerGate,
                onCheckedChange = {
                    reviewerGate = it
                    onUpdate(dualApproval, allowUserRepos, reviewerGate, segregation)
                },
                testTag = "toggle_reviewer_gate"
            )

            PolicySwitchRow(
                title = "Segregation of Duties (Anti-Self-Approval)",
                subtitle = "Original author of an artifact cannot review or approve their own draft.",
                checked = segregation,
                onCheckedChange = {
                    segregation = it
                    onUpdate(dualApproval, allowUserRepos, reviewerGate, segregation)
                },
                testTag = "toggle_segregation_duties"
            )
        }
    }
}

@Composable
fun PolicySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IndigoPrimary,
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}

fun List<NoCodeArtifact>.filterByRepo(repoId: String?): List<NoCodeArtifact> {
    return if (repoId == null) emptyList() else this.filter { it.repoId == repoId }
}
