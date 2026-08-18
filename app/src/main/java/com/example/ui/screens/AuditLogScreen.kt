package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.PolicyVerdict
import com.example.ui.components.PolicyVerdictBadge
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
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

@Composable
fun AuditLogScreen(auditLogs: List<AuditLog>) {
    var searchQuery by remember { mutableStateOf("") }
    var filterBlockedOnly by remember { mutableStateOf<Boolean?>(null) } // null = All, false = Allowed, true = Denied

    val filteredLogs = auditLogs.filter { log ->
        val matchesVerdict = when (filterBlockedOnly) {
            null -> true
            false -> log.verdict == PolicyVerdict.ALLOWED
            true -> log.verdict != PolicyVerdict.ALLOWED
        }
        val matchesQuery = log.actionName.contains(searchQuery, ignoreCase = true) ||
            log.actorDisplayName.contains(searchQuery, ignoreCase = true) ||
            (log.repoName?.contains(searchQuery, ignoreCase = true) ?: false) ||
            log.reasoning.contains(searchQuery, ignoreCase = true)
        matchesVerdict && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SophisticatedContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "企業稽核軌跡與監測",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextHighEmphasis,
                            )
                            Text(
                                text = "不可任意竄改的即時治理紀錄",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = TextMediumEmphasis,
                            )
                        }
                    }
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("依動作、執行者或儲存庫篩選稽核軌跡…", color = TextLowEmphasis, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LavenderPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SophisticatedSurfaceDark,
                    unfocusedContainerColor = SophisticatedSurfaceDark,
                    focusedBorderColor = LavenderPrimary,
                    unfocusedBorderColor = SophisticatedBorder,
                    focusedTextColor = TextHighEmphasis,
                    unfocusedTextColor = TextHighEmphasis,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_audit_input"),
            )
        }

        // Verdict Filter Chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AuditFilterChip(
                    label = "All 個事件 (${auditLogs.size})",
                    isSelected = filterBlockedOnly == null,
                    onClick = { filterBlockedOnly = null },
                )
                AuditFilterChip(
                    label = "Allowed (${auditLogs.count { it.verdict == PolicyVerdict.ALLOWED }})",
                    isSelected = filterBlockedOnly == false,
                    onClick = { filterBlockedOnly = false },
                )
                AuditFilterChip(
                    label = "Denied (${auditLogs.count { it.verdict != PolicyVerdict.ALLOWED }})",
                    isSelected = filterBlockedOnly == true,
                    onClick = { filterBlockedOnly = true },
                )
            }
        }

        // Audit Items
        if (filteredLogs.isEmpty()) {
            item {
                EmptyStateCard(message = "No audit log entries matching the selected criteria.")
            }
        } else {
            items(filteredLogs) { log ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateStr = dateFormat.format(Date(log.timestamp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_log_item_${log.id}"),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = log.actionName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextHighEmphasis,
                            )

                            PolicyVerdictBadge(verdict = log.verdict)
                        }

                        Text(
                            text = log.reasoning,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            lineHeight = 18.sp,
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SophisticatedSurfaceDark)
                                .border(1.dp, SophisticatedBorderSubtle, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "執行者：${log.actorDisplayName} • 儲存庫：${log.repoName ?: "N/A"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderSubtle,
                            )

                            Text(
                                text = dateStr,
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
fun AuditFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) LavenderPrimary else SophisticatedSurfaceDark)
            .border(
                1.dp,
                if (isSelected) LavenderPrimary else SophisticatedBorder,
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (isSelected) LavenderOnPrimary else TextMediumEmphasis,
        )
    }
}
