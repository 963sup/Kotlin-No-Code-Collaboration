package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ApprovalStatus
import com.example.data.model.ArtifactApproval
import com.example.data.model.ArtifactReview
import com.example.data.model.GovernanceAction
import com.example.data.model.LifecycleState
import com.example.data.model.NoCodeArtifact
import com.example.data.model.PolicyEvaluationDetail
import com.example.data.model.Repository
import com.example.data.model.ReviewDecision
import com.example.data.model.User
import com.example.ui.components.LifecycleBadge
import com.example.ui.components.PolicyTraceDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.RoseError
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArtifactDetailScreen(
    artifact: NoCodeArtifact,
    repo: Repository,
    reviews: List<ArtifactReview>,
    approvals: List<ArtifactApproval>,
    activeUser: User?,
    onBack: () -> Unit,
    onSubmitForReview: () -> Unit,
    onSubmitReview: (ReviewDecision, String) -> Unit,
    onSubmitApproverSignOff: () -> Unit,
    onPublishAndLock: () -> Unit,
    onInspectPolicy: (GovernanceAction) -> Unit,
    simulationResult: PolicyEvaluationDetail?,
    onClearSimulation: () -> Unit
) {
    var showReviewDialog by remember { mutableStateOf(false) }

    val distinctApprovalsCount = approvals.filter { it.status == ApprovalStatus.APPROVED }.distinct由 { it.approverUserId }.size
    val requiredApprovers = repo.requiredApproverCount
    val isAlreadySignedByActiveUser = activeUser != null && approvals.any { it.approverUserId == activeUser.id && it.status == ApprovalStatus.APPROVED }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            ArtifactHeader(
                artifact = artifact,
                repo = repo,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lifecycle Stepper Pipeline
                LifecyclePipelineView(currentState = artifact.lifecycleState)

                // No-Code Content / Blueprint Presentation
                NoCodeBlueprintViewer(artifact = artifact)

                // Governance Actions & Gatekeeper Panel
                GovernanceActionPanel(
                    artifact = artifact,
                    activeUser = activeUser,
                    requiredApprovers = requiredApprovers,
                    collectedApprovals = distinctApprovalsCount,
                    isAlreadySigned = isAlreadySignedByActiveUser,
                    onSubmitForReview = onSubmitForReview,
                    onOpenReviewDialog = { showReviewDialog = true },
                    onSubmitApproverSignOff = onSubmitApproverSignOff,
                    onPublishAndLock = onPublishAndLock,
                    onInspectPolicy = onInspectPolicy
                )

                // Approver Signatures & Formal Review Feedback Section
                SignaturesAndReviewsSection(
                    approvals = approvals,
                    reviews = reviews,
                    requiredApprovers = requiredApprovers
                )
            }
        }
    }

    if (showReviewDialog) {
        SubmitReviewDialog(
            artifact = artifact,
            onDismiss = { showReviewDialog = false },
            onSubmit = { decision, feedback ->
                onSubmitReview(decision, feedback)
                showReviewDialog = false
            }
        )
    }

    if (simulationResult != null) {
        PolicyTraceDialog(
            evaluation = simulationResult,
            onDismiss = onClearSimulation
        )
    }
}

@Composable
fun ArtifactHeader(
    artifact: NoCodeArtifact,
    repo: Repository,
    onBack: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateDark900),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp).testTag("back_from_artifact_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = IndigoLight)
                }

                Text(
                    text = "${repo.displayName} > ${artifact.type.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artifact.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "作者：${artifact.authorDisplayName} • 版本 ${artifact.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent
                    )
                }

                LifecycleBadge(state = artifact.lifecycleState)
            }
        }
    }
}

@Composable
fun LifecyclePipelineView(currentState: LifecycleState) {
    val steps = listOf(
        LifecycleState.DRAFT,
        LifecycleState.IN_REVIEW,
        LifecycleState.PENDING_APPROVAL,
        LifecycleState.APPROVED,
        LifecycleState.PUBLISHED
    )

    val currentIndex = steps.indexOf(currentState).coerceAtLeast(0)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "階層治理生命週期流程",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, state ->
                    val isPast = index < currentIndex
                    val isCurrent = index == currentIndex
                    val color = when {
                        isCurrent -> IndigoLight
                        isPast -> EmeraldSuccess
                        else -> Color(0xFF475569)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.25f))
                                .border(1.5.dp, color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPast) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = color
                                )
                            }
                        }

                        Text(
                            text = state.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isCurrent) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoCodeBlueprintViewer(artifact: NoCodeArtifact) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "無程式碼藍圖規格",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = CyanAccent
            )

            Text(
                text = artifact.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.padding(vertical = 6.dp),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Structured schema card box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D16))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = artifact.structuredContent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFA5F3FC),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun GovernanceActionPanel(
    artifact: NoCodeArtifact,
    activeUser: User?,
    requiredApprovers: Int,
    collectedApprovals: Int,
    isAlreadySigned: Boolean,
    onSubmitForReview: () -> Unit,
    onOpenReviewDialog: () -> Unit,
    onSubmitApproverSignOff: () -> Unit,
    onPublishAndLock: () -> Unit,
    onInspectPolicy: (GovernanceAction) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateDark900),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "治理簽核與生命週期關卡",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "目前身分：${activeUser?.displayName ?: "Guest"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
            }

            when (artifact.lifecycleState) {
                LifecycleState.DRAFT -> {
                    Text(
                        text = "此藍圖目前為草稿；協作者或維護者可送出以開始正式同儕審查。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Button(
                        onClick = onSubmitForReview,
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_for_review_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("送出藍圖進行同儕審查")
                    }
                }

                LifecycleState.IN_REVIEW -> {
                    Text(
                        text = "此藍圖正在審查中；指定審查者可核准或要求修改。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Button(
                        onClick = onOpenReviewDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_review_dialog_button")
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("送出審查決定與回饋", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                LifecycleState.PENDING_APPROVAL, LifecycleState.APPROVED -> {
                    Text(
                        text = "多重簽核關卡：已取得 $collectedApprovals / $requiredApprovers 個必要簽核。",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (collectedApprovals >= requiredApprovers) EmeraldSuccess else PurpleGlow
                    )

                    if (collectedApprovals < requiredApprovers) {
                        Button(
                            onClick = onSubmitApproverSignOff,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isAlreadySigned,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_approver_signoff_button")
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isAlreadySigned) "Signature Already Recorded" else "Grant Approver Signature", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (collectedApprovals >= requiredApprovers) {
                        Button(
                            onClick = onPublishAndLock,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_and_lock_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("發布並鎖定藍圖", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                LifecycleState.PUBLISHED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldSuccess)
                            Text(
                                text = "成果已依企業簽核政策正式發布並鎖定。",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                LifecycleState.ARCHIVED -> {
                    Text("此藍圖已封存。", color = Color(0xFF94A3B8))
                }
            }

            OutlinedButton(
                onClick = {
                    val action = when (artifact.lifecycleState) {
                        LifecycleState.DRAFT -> GovernanceAction.SUBMIT_FOR_REVIEW
                        LifecycleState.IN_REVIEW -> GovernanceAction.SUBMIT_REVIEW
                        LifecycleState.PENDING_APPROVAL -> GovernanceAction.SUBMIT_FINAL_APPROVAL
                        LifecycleState.APPROVED, LifecycleState.PUBLISHED -> GovernanceAction.PUBLISH_AND_LOCK
                        LifecycleState.ARCHIVED -> GovernanceAction.VIEW_ARTIFACT
                    }
                    onInspectPolicy(action)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inspect_artifact_policy_button"),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("檢視此動作的存取控制政策", color = CyanAccent, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SignaturesAndReviewsSection(
    approvals: List<ArtifactApproval>,
    reviews: List<ArtifactReview>,
    requiredApprovers: Int
) {
    val validApprovalsCount = approvals.filter { it.status == ApprovalStatus.APPROVED }.distinct由 { it.approverUserId }.size
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "核准與簽章（$validApprovalsCount / $requiredApprovers）",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        if (approvals.isEmpty()) {
            Text("尚無核准紀錄。", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        } else {
            approvals.forEach { approval ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            Column {
                                Text(approval.approverDisplayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text(approval.approverTitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            }
                        }

                        Text(approval.signatureProof, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = CyanAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "同儕審查決定（${reviews.size}）",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        if (reviews.isEmpty()) {
            Text("尚無同儕審查紀錄。", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        } else {
            reviews.forEach { review ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24334D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(review.reviewerDisplayName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text(
                                text = review.decision.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (review.decision == ReviewDecision.APPROVED) EmeraldSuccess else RoseError
                            )
                        }
                        if (review.feedbackNote.isNotBlank()) {
                            Text(
                                text = "\"${review.feedbackNote}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmitReviewDialog(
    artifact: NoCodeArtifact,
    onDismiss: () -> Unit,
    onSubmit: (ReviewDecision, String) -> Unit
) {
    var selectedDecision by remember { mutableStateOf(ReviewDecision.APPROVED) }
    var feedback by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = SlateDark900,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "送出審查者簽核",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "評估中：${artifact.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAccent
                )

                // Decision Radio options
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        Pair(ReviewDecision.APPROVED, "Approve (Promotes to Approver Gate)"),
                        Pair(ReviewDecision.CHANGES_REQUESTED, "Request Changes (Returns to Draft)"),
                        Pair(ReviewDecision.COMMENTED, "Comment Only")
                    ).forEach { (decision, label) ->
                        val isSelected = selectedDecision == decision
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A))
                                .clickable { selectedDecision = decision }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedDecision = decision },
                                colors = RadioButtonDefaults.colors(selectedColor = IndigoLight)
                            )
                            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("審查備註與驗證回饋") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("review_feedback_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = Color(0xFF94A3B8)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(selectedDecision, feedback) },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.testTag("submit_review_decision_button")
                    ) {
                        Text("送出決定")
                    }
                }
            }
        }
    }
}
