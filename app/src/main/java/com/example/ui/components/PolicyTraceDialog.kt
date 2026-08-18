package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PolicyCheckItem
import com.example.data.model.PolicyEvaluationDetail
import com.example.data.model.PolicyVerdict
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis

@Composable
fun PolicyTraceDialog(evaluation: PolicyEvaluationDetail, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = SophisticatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (evaluation.verdict == PolicyVerdict.ALLOWED) EmeraldSuccess else RoseError,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "政策評估軌跡",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = LavenderPrimary,
                        )
                        Text(
                            text = "階層治理引擎",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = TextMediumEmphasis,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                PolicyVerdictBadge(verdict = evaluation.verdict, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(14.dp))

                // Evaluation Context Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SophisticatedBorder),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TraceKeyVal("Actor", "${evaluation.actor.displayName} (@${evaluation.actor.username})")
                        TraceKeyVal("Action Requested", evaluation.action.label)
                        TraceKeyVal(
                            "Target Repository",
                            "${evaluation.targetRepo.displayName} (${evaluation.targetRepo.ownerType.name})",
                        )
                        if (evaluation.targetArtifact != null) {
                            TraceKeyVal(
                                "Target Artifact",
                                "${evaluation.targetArtifact.title} [${evaluation.targetArtifact.lifecycleState.name}]",
                            )
                        }
                        TraceKeyVal("有效角色", evaluation.effectiveRole.name)
                        TraceKeyVal("角色來源", evaluation.roleSource)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enterprise Checks Section
                Text(
                    text = "1. 企業護欄評估",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LavenderPrimary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    evaluation.enterpriseChecks.forEach { check ->
                        PolicyCheckCard(check)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Repository & Role Checks Section
                Text(
                    text = "2. 儲存庫角色與生命週期門檻檢查",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LavenderSubtle,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    evaluation.repositoryChecks.forEach { check ->
                        PolicyCheckCard(check)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Final Policy Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (evaluation.verdict == PolicyVerdict.ALLOWED) {
                                EmeraldDark.copy(
                                    alpha = 0.6f,
                                )
                            } else {
                                RoseDark.copy(alpha = 0.6f)
                            },
                        )
                        .border(
                            1.dp,
                            if (evaluation.verdict == PolicyVerdict.ALLOWED) {
                                EmeraldSuccess.copy(
                                    alpha = 0.4f,
                                )
                            } else {
                                RoseError.copy(alpha = 0.4f)
                            },
                            RoundedCornerShape(14.dp),
                        )
                        .padding(14.dp),
                ) {
                    Text(
                        text = evaluation.finalExplanation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp,
                        ),
                        color = TextHighEmphasis,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_policy_trace_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = LavenderOnPrimary,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("確認並關閉", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TraceKeyVal(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall,
            color = TextMediumEmphasis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextHighEmphasis,
        )
    }
}

@Composable
fun PolicyCheckCard(check: PolicyCheckItem) {
    val passed = check.passed
    val icon = if (passed) Icons.Default.Check else Icons.Default.Close
    val color = if (passed) EmeraldSuccess else RoseError

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SophisticatedSurfaceDark)
            .border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = check.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHighEmphasis,
            )
            Text(
                text = check.detail,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextMediumEmphasis,
            )
        }
    }
}
