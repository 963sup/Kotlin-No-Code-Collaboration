package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HomeHealthSection(
    healthScore: Int = 68,
    inProgressCount: Int = 24,
    pendingCount: Int = 18,
    toVerifyCount: Int = 6,
    trendPercent: Int = 12,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "企業健康度",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextHighEmphasis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Circle Progress Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 16.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                        CircularProgressIndicator(
                            progress = { healthScore / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = LavenderPrimary,
                            strokeWidth = 7.dp,
                            trackColor = LavenderContainer,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$healthScore%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = EmeraldDark,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "較上週 ↑$trendPercent%",
                            color = EmeraldSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                // Stats Columns
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MetricItem(label = "進行中", count = inProgressCount, color = LavenderPrimary)
                    MetricItem(label = "待處理", count = pendingCount, color = AmberWarning)
                    MetricItem(label = "待驗證", count = toVerifyCount, color = EmeraldSuccess)
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMediumEmphasis,
        )
    }
}
