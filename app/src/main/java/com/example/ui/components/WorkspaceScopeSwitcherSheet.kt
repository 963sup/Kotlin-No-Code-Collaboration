package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Enterprise
import com.example.data.model.Organization
import com.example.data.model.Team
import com.example.data.model.User
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.SophisticatedBg
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis

enum class WorkspaceScopeKind(val label: String) {
    ENTERPRISE("企業"),
    ORGANIZATION("組織"),
    TEAM("團隊"),
    USER("用戶")
}

data class WorkspaceScopeSelection(
    val kind: WorkspaceScopeKind,
    val id: String,
    val name: String,
    val subtitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScopeSwitcherSheet(
    enterprises: List<Enterprise>,
    organizations: List<Organization>,
    teams: List<Team>,
    users: List<User>,
    selectedScope: WorkspaceScopeSelection?,
    onSelectScope: (WorkspaceScopeSelection) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedKind by remember(selectedScope?.kind) {
        mutableStateOf(selectedScope?.kind ?: WorkspaceScopeKind.USER)
    }
    var query by remember { mutableStateOf("") }

    val enterpriseById = remember(enterprises) { enterprises.associateBy { it.id } }
    val organizationById = remember(organizations) { organizations.associateBy { it.id } }

    val itemsForKind = when (selectedKind) {
        WorkspaceScopeKind.ENTERPRISE -> enterprises.map {
            WorkspaceScopeSelection(
                kind = WorkspaceScopeKind.ENTERPRISE,
                id = it.id,
                name = it.name,
                subtitle = it.slug
            )
        }

        WorkspaceScopeKind.ORGANIZATION -> organizations.map {
            WorkspaceScopeSelection(
                kind = WorkspaceScopeKind.ORGANIZATION,
                id = it.id,
                name = it.name,
                subtitle = enterpriseById[it.enterpriseId]?.name ?: "企業"
            )
        }

        WorkspaceScopeKind.TEAM -> teams.map {
            WorkspaceScopeSelection(
                kind = WorkspaceScopeKind.TEAM,
                id = it.id,
                name = it.name,
                subtitle = organizationById[it.orgId]?.name ?: "組織"
            )
        }

        WorkspaceScopeKind.USER -> users.map {
            WorkspaceScopeSelection(
                kind = WorkspaceScopeKind.USER,
                id = it.id,
                name = it.displayName,
                subtitle = "@${it.username} • ${it.title}"
            )
        }
    }

    val normalizedQuery = query.trim()
    val filteredItems = remember(itemsForKind, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            itemsForKind
        } else {
            itemsForKind.filter {
                it.name.contains(normalizedQuery, ignoreCase = true) ||
                    it.subtitle.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SophisticatedSurface,
        contentColor = TextHighEmphasis,
        modifier = Modifier.testTag("workspace_scope_switcher_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "切換工作範圍",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextHighEmphasis
            )
            Text(
                text = "只改變目前瀏覽範圍，不改變登入身分。",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumEmphasis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workspace_scope_search"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("搜尋目前類型") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMediumEmphasis
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SophisticatedSurfaceDark,
                    unfocusedContainerColor = SophisticatedSurfaceDark,
                    focusedIndicatorColor = LavenderPrimary,
                    unfocusedIndicatorColor = SophisticatedBorder,
                    cursorColor = LavenderPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkspaceScopeKind.entries.forEach { kind ->
                    FilterChip(
                        selected = selectedKind == kind,
                        onClick = {
                            selectedKind = kind
                            query = ""
                        },
                        label = { Text(kind.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = scopeIcon(kind),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LavenderPrimary,
                            selectedLabelColor = LavenderOnPrimary,
                            selectedLeadingIconColor = LavenderOnPrimary
                        ),
                        modifier = Modifier.testTag("workspace_scope_kind_${kind.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "找不到符合的${selectedKind.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMediumEmphasis
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredItems, key = { "${it.kind.name}:${it.id}" }) { item ->
                        val isSelected = selectedScope?.kind == item.kind && selectedScope.id == item.id

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onSelectScope(item)
                                    onDismiss()
                                }
                                .testTag("workspace_scope_item_${item.kind.name.lowercase()}_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) SophisticatedContainer else SophisticatedSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) LavenderPrimary else SophisticatedBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SophisticatedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = scopeIcon(item.kind),
                                        contentDescription = null,
                                        tint = if (isSelected) LavenderPrimary else TextMediumEmphasis,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = TextHighEmphasis,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMediumEmphasis,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "目前工作範圍",
                                        tint = LavenderPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun scopeIcon(kind: WorkspaceScopeKind): ImageVector = when (kind) {
    WorkspaceScopeKind.ENTERPRISE -> Icons.Default.Business
    WorkspaceScopeKind.ORGANIZATION -> Icons.Default.Apartment
    WorkspaceScopeKind.TEAM -> Icons.Default.Groups
    WorkspaceScopeKind.USER -> Icons.Default.Person
}
