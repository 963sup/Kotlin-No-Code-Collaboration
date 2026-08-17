package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LifecycleState
import com.example.data.model.OwnerType
import com.example.data.model.PolicyVerdict
import com.example.data.model.RepoRole
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderGlow
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SophisticatedBorder
import com.example.ui.theme.SophisticatedContainer
import com.example.ui.theme.SophisticatedSurface
import com.example.ui.theme.SophisticatedSurfaceDark
import com.example.ui.theme.TextHighEmphasis
import com.example.ui.theme.TextMediumEmphasis
import com.example.ui.theme.WhiteM3

@Composable
fun RoleBadge(role: RepoRole, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon, hasSolidPill) = when (role) {
        RepoRole.OWNER -> Quad(LavenderPrimary, LavenderOnPrimary, Icons.Default.Star, true)
        RepoRole.MAINTAINER -> Quad(SophisticatedContainer, LavenderPrimary, Icons.Default.Security, false)
        RepoRole.APPROVER -> Quad(EmeraldDark.copy(alpha = 0.6f), EmeraldSuccess, Icons.Default.Gavel, false)
        RepoRole.REVIEWER -> Quad(SophisticatedSurfaceDark, WhiteM3, Icons.Default.RateReview, false)
        RepoRole.COLLABORATOR -> Quad(SophisticatedSurface, LavenderSubtle, Icons.Default.Edit, false)
        RepoRole.VIEWER -> Quad(SophisticatedSurfaceDark, TextMediumEmphasis, Icons.Default.Visibility, false)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .then(
                if (!hasSolidPill) Modifier.border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = role.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun RoleBadge(roleName: String, modifier: Modifier = Modifier) {
    val role = try {
        RepoRole.valueOf(roleName)
    } catch (e: Exception) {
        RepoRole.VIEWER
    }
    RoleBadge(role = role, modifier = modifier)
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun OwnerTypeTag(ownerType: OwnerType, ownerDisplayName: String, modifier: Modifier = Modifier) {
    val (icon, tint, bg) = when (ownerType) {
        OwnerType.ORGANIZATION -> Triple(Icons.Default.Apartment, LavenderPrimary, SophisticatedContainer)
        OwnerType.USER -> Triple(Icons.Default.Person, PinkAccent, SophisticatedContainer)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, SophisticatedBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "${ownerType.displayName()}: $ownerDisplayName",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = TextHighEmphasis
        )
    }
}

@Composable
fun LifecycleBadge(state: LifecycleState, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (state) {
        LifecycleState.DRAFT -> Triple(SophisticatedSurfaceDark, TextMediumEmphasis, Icons.Default.Edit)
        LifecycleState.IN_REVIEW -> Triple(SophisticatedContainer, AmberWarning, Icons.Default.RateReview)
        LifecycleState.PENDING_APPROVAL -> Triple(SophisticatedContainer, LavenderPrimary, Icons.Default.HourglassTop)
        LifecycleState.APPROVED -> Triple(EmeraldDark.copy(alpha = 0.7f), EmeraldSuccess, Icons.Default.CheckCircle)
        LifecycleState.PUBLISHED -> Triple(LavenderContainer.copy(alpha = 0.7f), LavenderGlow, Icons.Default.Lock)
        LifecycleState.ARCHIVED -> Triple(SophisticatedSurfaceDark, TextMediumEmphasis, Icons.Default.Close)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, SophisticatedBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = state.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun PolicyVerdictBadge(verdict: PolicyVerdict, modifier: Modifier = Modifier) {
    val isAllowed = verdict == PolicyVerdict.ALLOWED
    val bgColor = if (isAllowed) EmeraldDark.copy(alpha = 0.8f) else RoseDark.copy(alpha = 0.8f)
    val textColor = if (isAllowed) EmeraldSuccess else RoseError
    val icon = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Close

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (isAllowed) "POLICY PASSED: ALLOWED" else "POLICY BLOCKED: ${verdict.name}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = textColor
        )
    }
}
