package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Repository
import com.example.ui.theme.*

@Composable
fun HomeActiveReposSection(
    repositories: List<Repository>,
    totalCount: Int = 12,
    passedCount: Int = 7,
    warningCount: Int = 3,
    dangerCount: Int = 2,
    onSelectRepository: (Repository) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "活躍倉庫 $totalCount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusTag(label = "通過 $passedCount", bg = EmeraldDark, fg = EmeraldSuccess)
                StatusTag(label = "異常 $warningCount", bg = AmberGlow, fg = AmberWarning)
                StatusTag(label = "危險 $dangerCount", bg = RoseDark, fg = RoseError)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repositories.take(4).forEach { repo ->
                ActiveRepoCard(repo = repo, onClick = { onSelectRepository(repo) })
            }
        }
    }
}

@Composable
private fun StatusTag(label: String, bg: Color, fg: Color) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ActiveRepoCard(repo: Repository, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.displayName.ifBlank { repo.name },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "WBS 60% • Issue 18",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMediumEmphasis,
                )
            }
            Surface(
                color = LavenderContainer,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = "進行中",
                    color = LavenderPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}
