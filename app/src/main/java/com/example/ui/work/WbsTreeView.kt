package com.example.ui.work

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RepoIssue
import com.example.ui.theme.*

data class WbsNode(
    val code: String,
    val title: String,
    val isCompleted: Boolean,
    val relatedIssue: RepoIssue? = null,
    val children: List<WbsNode> = emptyList(),
)

@Composable
fun WbsTreeView(
    issues: List<RepoIssue>,
    onSelectIssue: (RepoIssue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sampleTree = remember(issues) {
        listOf(
            WbsNode(
                code = "3.1",
                title = "現場檢查與測量",
                isCompleted = true,
                children = listOf(
                    WbsNode("3.1.1", "地基深度測量", isCompleted = true),
                    WbsNode("3.1.2", "土壤樣本採集與化驗", isCompleted = true),
                ),
            ),
            WbsNode(
                code = "3.2",
                title = "原因分析與建模",
                isCompleted = false,
                children = listOf(
                    WbsNode(
                        code = "3.2.1",
                        title = "#128 基底沉降問題根因分析",
                        isCompleted = false,
                        relatedIssue = issues.firstOrNull { it.issueNumber == 128 } ?: issues.firstOrNull(),
                    ),
                    WbsNode("3.2.2", "結構受力數據建模", isCompleted = false),
                ),
            ),
            WbsNode(
                code = "3.3",
                title = "優化方案提出與評估",
                isCompleted = false,
                children = listOf(
                    WbsNode("3.3.1", "加固工法設計方案", isCompleted = false),
                    WbsNode("3.3.2", "跨部門成本效益評估", isCompleted = false),
                ),
            ),
            WbsNode(
                code = "3.4",
                title = "實施與驗證階段",
                isCompleted = false,
                children = listOf(
                    WbsNode("3.4.1", "灌漿加固現場施工", isCompleted = false),
                    WbsNode("3.4.2", "二次沉降監測與獨立驗證", isCompleted = false),
                ),
            ),
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        sampleTree.forEach { parentNode ->
            item {
                WbsNodeCard(node = parentNode, onSelectIssue = onSelectIssue)
            }
        }
    }
}

@Composable
private fun WbsNodeCard(node: WbsNode, onSelectIssue: (RepoIssue) -> Unit) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMediumEmphasis,
                    )
                    Text(
                        text = node.code,
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = node.title,
                        fontWeight = FontWeight.Bold,
                        color = TextHighEmphasis,
                        fontSize = 14.sp,
                    )
                }

                if (node.isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                }
            }

            if (expanded && node.children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    node.children.forEach { child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (child.relatedIssue != null) {
                                        onSelectIssue(child.relatedIssue)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = child.code,
                                    color = TextMediumEmphasis,
                                    fontSize = 12.sp,
                                )
                                Text(
                                    text = child.title,
                                    color = if (child.relatedIssue != null) LavenderPrimary else TextHighEmphasis,
                                    fontSize = 13.sp,
                                    fontWeight = if (child.relatedIssue != null) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                            if (child.isCompleted) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = TextLowEmphasis, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
