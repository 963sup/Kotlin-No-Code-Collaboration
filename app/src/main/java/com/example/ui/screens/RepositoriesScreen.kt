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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import com.example.data.model.NoCodeArtifact
import com.example.data.model.OrgMembership
import com.example.data.model.Organization
import com.example.data.model.OwnerType
import com.example.data.model.RepoAccessRule
import com.example.data.model.RepoRole
import com.example.data.model.Repository
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.User
import com.example.engine.HierarchicalPolicyEngine
import com.example.ui.components.OwnerTypeTag
import com.example.ui.components.RoleBadge
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
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
import com.example.ui.theme.WhiteM3

enum class RepoFilter {
    ALL,
    ORG_OWNED,
    USER_OWNED
}

@Composable
fun RepositoriesScreen(
    repositories: List<Repository>,
    organizations: List<Organization>,
    users: List<User>,
    teams: List<Team>,
    allAccessRules: List<RepoAccessRule>,
    allOrgMemberships: List<OrgMembership>,
    allTeamMemberships: List<TeamMembership>,
    allArtifacts: List<NoCodeArtifact>,
    activeUser: User?,
    onSelectRepo: (Repository) -> Unit,
    onCreateRepo: (String, String, OwnerType, String, String, String, String, (Boolean) -> Unit) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(RepoFilter.ALL) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredRepos = repositories.filter { repo ->
        val matchesFilter = when (selectedFilter) {
            RepoFilter.ALL -> true
            RepoFilter.ORG_OWNED -> repo.ownerType == OwnerType.ORGANIZATION
            RepoFilter.USER_OWNED -> repo.ownerType == OwnerType.USER
        }
        val matchesQuery = repo.displayName.contains(searchQuery, ignoreCase = true) ||
                repo.name.contains(searchQuery, ignoreCase = true) ||
                repo.ownerDisplayName.contains(searchQuery, ignoreCase = true) ||
                repo.category.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Enterprise Governance Hero Banner
            item {
                EnterpriseGovernanceHeroBanner(
                    totalRepos = repositories.size,
                    orgRepos = repositories.count { it.ownerType == OwnerType.ORGANIZATION },
                    userRepos = repositories.count { it.ownerType == OwnerType.USER },
                    totalArtifacts = allArtifacts.size
                )
            }

            // Search Bar & Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜尋專案、儲存庫、擁有者或類別…", color = TextLowEmphasis, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LavenderPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_repos_input")
                )
            }

            // Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            label = "全部 (${repositories.size})",
                            isSelected = selectedFilter == RepoFilter.ALL,
                            onClick = { selectedFilter = RepoFilter.ALL },
                            testTag = "filter_all_repos"
                        )
                    }
                    item {
                        FilterChip(
                            label = "組織擁有 (${repositories.count { it.ownerType == OwnerType.ORGANIZATION }})",
                            isSelected = selectedFilter == RepoFilter.ORG_OWNED,
                            onClick = { selectedFilter = RepoFilter.ORG_OWNED },
                            testTag = "filter_org_repos"
                        )
                    }
                    item {
                        FilterChip(
                            label = "個人擁有 (${repositories.count { it.ownerType == OwnerType.USER }})",
                            isSelected = selectedFilter == RepoFilter.USER_OWNED,
                            onClick = { selectedFilter = RepoFilter.USER_OWNED },
                            testTag = "filter_user_repos"
                        )
                    }
                }
            }

            // Repositories List
            if (filteredRepos.isEmpty()) {
                item {
                    EmptyStateCard(message = "找不到符合條件的無程式碼儲存庫。")
                }
            } else {
                items(filteredRepos) { repo ->
                    val repoArtifacts = allArtifacts.filter { it.repoId == repo.id }
                    val effectiveRolePair = if (activeUser != null) {
                        HierarchicalPolicyEngine.resolveEffectiveRole(
                            actor = activeUser,
                            repo = repo,
                            orgMemberships = allOrgMemberships,
                            teamMemberships = allTeamMemberships,
                            teams = teams,
                            accessRules = allAccessRules
                        )
                    } else Pair(RepoRole.VIEWER, "Default")

                    RepoCardItem(
                        repo = repo,
                        artifactCount = repoArtifacts.size,
                        effectiveRole = effectiveRolePair.first,
                        roleSource = effectiveRolePair.second,
                        onClick = { onSelectRepo(repo) }
                    )
                }
            }
        }

        // Floating Action Button to Create No-Code Repository
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_repo_fab"),
            containerColor = LavenderPrimary,
            contentColor = LavenderOnPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "建立無程式碼儲存庫")
                Text("新增儲存庫", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showCreateDialog) {
        CreateRepositoryDialog(
            organizations = organizations,
            users = users,
            activeUser = activeUser,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, displayName, ownerType, ownerId, ownerDisplayName, desc, category ->
                onCreateRepo(name, displayName, ownerType, ownerId, ownerDisplayName, desc, category) { success ->
                    if (success) showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun EnterpriseGovernanceHeroBanner(
    totalRepos: Int,
    orgRepos: Int,
    userRepos: Int,
    totalArtifacts: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SophisticatedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "無程式碼協作容器",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextHighEmphasis
                    )
                    Text(
                        text = "探索可存取的專案與成果",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextMediumEmphasis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SophisticatedSurfaceDark)
                    .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(14.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HeroStatItem(label = "儲存庫總數", value = totalRepos.toString(), color = TextHighEmphasis)
                HeroStatItem(label = "組織擁有", value = orgRepos.toString(), color = LavenderPrimary)
                HeroStatItem(label = "使用者擁有", value = userRepos.toString(), color = PinkAccent)
                HeroStatItem(label = "成果", value = totalArtifacts.toString(), color = LavenderSubtle)
            }
        }
    }
}

@Composable
fun HeroStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMediumEmphasis
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) LavenderPrimary else SophisticatedSurfaceDark)
            .border(
                1.dp,
                if (isSelected) LavenderPrimary else SophisticatedBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis
        )
    }
}

@Composable
fun RepoCardItem(
    repo: Repository,
    artifactCount: Int,
    effectiveRole: RepoRole,
    roleSource: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("repo_card_${repo.name}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Owner Badge & Effective User Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OwnerTypeTag(ownerType = repo.ownerType, ownerDisplayName = repo.ownerDisplayName)
                RoleBadge(role = effectiveRole)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Repo Title & Category
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = repo.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis
                )
            }

            Text(
                text = repo.name,
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumEmphasis,
                modifier = Modifier.padding(start = 28.dp, bottom = 6.dp)
            )

            Text(
                text = repo.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
                maxLines = 2,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SophisticatedSurfaceDark)
                    .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "$artifactCount 個成果",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LavenderSubtle
                    )
                    Text(
                        text = "•",
                        color = SophisticatedBorder
                    )
                    Text(
                        text = "${repo.requiredApproverCount} 個核准人關卡",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "開啟儲存庫",
                    tint = TextMediumEmphasis,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CreateRepositoryDialog(
    organizations: List<Organization>,
    users: List<User>,
    activeUser: User?,
    onDismiss: () -> Unit,
    onCreate: (String, String, OwnerType, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Process Automation") }
    var selectedOwnerType by remember { mutableStateOf(OwnerType.ORGANIZATION) }
    var selectedOwnerId by remember {
        mutableStateOf(organizations.firstOrNull()?.id ?: activeUser?.id ?: "")
    }

    val selectedOwnerDisplayName = when (selectedOwnerType) {
        OwnerType.ORGANIZATION -> organizations.firstOrNull { it.id == selectedOwnerId }?.name ?: ""
        OwnerType.USER -> users.firstOrNull { it.id == selectedOwnerId }?.displayName ?: ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "新增無程式碼儲存庫容器",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = LavenderPrimary
                )

                // Governance Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SophisticatedContainer)
                        .border(1.dp, SophisticatedBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "只有組織或使用者可以擁有儲存庫；團隊不能擁有儲存庫，只能繼承協作角色。",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = LavenderSubtle
                        )
                    }
                }

                // Owner Type Selection
                Text(
                    text = "儲存庫擁有者實體",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextHighEmphasis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OwnerTypeOption(
                        title = "組織",
                        subtitle = "Enterprise Org Container",
                        icon = Icons.Default.Apartment,
                        isSelected = selectedOwnerType == OwnerType.ORGANIZATION,
                        onClick = {
                            selectedOwnerType = OwnerType.ORGANIZATION
                            selectedOwnerId = organizations.firstOrNull()?.id ?: ""
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "owner_type_org"
                    )
                    OwnerTypeOption(
                        title = "使用者",
                        subtitle = "Personal Account",
                        icon = Icons.Default.Person,
                        isSelected = selectedOwnerType == OwnerType.USER,
                        onClick = {
                            selectedOwnerType = OwnerType.USER
                            selectedOwnerId = activeUser?.id ?: users.firstOrNull()?.id ?: ""
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "owner_type_user"
                    )
                }

                // Owner Entity Dropdown / Selector List
                Text(
                    text = "選擇指定的 ${selectedOwnerType.displayName()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedOwnerType == OwnerType.ORGANIZATION) {
                        organizations.forEach { org ->
                            val isSelected = org.id == selectedOwnerId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark)
                                    .border(1.dp, if (isSelected) LavenderPrimary.copy(alpha = 0.6f) else SophisticatedBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedOwnerId = org.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedOwnerId = org.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = LavenderPrimary)
                                )
                                Column {
                                    Text(org.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                    Text(org.description, style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis, maxLines = 1)
                                }
                            }
                        }
                    } else {
                        users.forEach { user ->
                            val isSelected = user.id == selectedOwnerId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark)
                                    .border(1.dp, if (isSelected) PinkAccent.copy(alpha = 0.6f) else SophisticatedBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedOwnerId = user.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedOwnerId = user.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = PinkAccent)
                                )
                                Column {
                                    Text(user.displayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighEmphasis)
                                    Text("@${user.username} • ${user.title}", style = MaterialTheme.typography.labelSmall, color = TextMediumEmphasis)
                                }
                            }
                        }
                    }
                }

                // Name & Display Name Fields
                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        if (name.isEmpty() || name == displayName.lowercase().replace(" ", "-").dropLast(1)) {
                            name = it.lowercase().replace(" ", "-")
                        }
                    },
                    label = { Text("顯示名稱（例如：Core API 藍圖）") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("repo_display_name_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.lowercase().replace(" ", "-") },
                    label = { Text("儲存庫 Slug 識別碼") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("repo_slug_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("目的與治理範圍") },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("repo_desc_input")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分類（例如：流程自動化、RFC）") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SophisticatedSurfaceDark,
                        unfocusedContainerColor = SophisticatedSurfaceDark,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = SophisticatedBorder,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("repo_category_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = TextMediumEmphasis)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && displayName.isNotBlank() && selectedOwnerId.isNotBlank()) {
                                onCreate(name, displayName, selectedOwnerType, selectedOwnerId, selectedOwnerDisplayName, description, category)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = name.isNotBlank() && displayName.isNotBlank(),
                        modifier = Modifier.testTag("submit_create_repo_button")
                    ) {
                        Text("建立工作區", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerTypeOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark)
            .border(
                1.dp,
                if (isSelected) LavenderPrimary.copy(alpha = 0.8f) else SophisticatedBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) LavenderPrimary else TextMediumEmphasis,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMediumEmphasis
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMediumEmphasis
            )
        }
    }
}
