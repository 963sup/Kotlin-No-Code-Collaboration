package com.example.ui.work

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RepoIssue
import com.example.ui.theme.*

@Composable
fun EvidenceVerificationDialog(
    issue: RepoIssue,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    var comments by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "驗證 (獨立驗證)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "#${issue.issueNumber} ${issue.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LavenderPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "驗證摘要 (4 大規約):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                )

                CriteriaRow(label = "資料完整性", status = "通過", color = EmeraldSuccess)
                CriteriaRow(label = "方法合規性", status = "通過", color = EmeraldSuccess)
                CriteriaRow(label = "結論一致性", status = "通過", color = EmeraldSuccess)
                CriteriaRow(label = "風險與影響", status = "低風險", color = EmeraldSuccess)

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("驗證意見與備註 (選填)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("通過 (Accept)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onReject,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                border = BorderStroke(1.dp, RoseError),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("駁回 (Reject)", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SophisticatedSurface,
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun CriteriaRow(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
        Text(text = status, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
    }
}
