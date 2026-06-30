package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.R
import com.example.data.SimulationHistoryEntity
import com.example.ui.TacticalAudio
import com.example.ui.VanguardViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IntelScreen(
    viewModel: VanguardViewModel,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.history.collectAsStateWithLifecycle()
    var selectedHistory by remember { mutableStateOf<SimulationHistoryEntity?>(null) }

    // Calculate aggregated telemetry metrics
    val totalMissions = historyList.size
    val championCount = historyList.count { it.placement == 1 }
    val avgKills = if (totalMissions > 0) historyList.map { it.kills }.average() else 0.0
    val avgDamage = if (totalMissions > 0) historyList.map { it.damageDealt }.average() else 0.0
    val bestDamage = if (totalMissions > 0) historyList.maxOf { it.damageDealt } else 0

    Row(
        modifier = modifier
            .fillMaxSize()
            .highDensityBackground()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Historical Logs list
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INTEL RECORDS",
                    style = MaterialTheme.typography.titleLarge,
                    color = TacticalLight,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        TacticalAudio.playAlert()
                        viewModel.clearHistory()
                        selectedHistory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalSurfaceVariant),
                    border = BorderStroke(1.dp, TacticalBorder),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = TacticalOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PURGE", color = TacticalOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // High Tech Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = BorderStroke(1.dp, TacticalBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("SQUAD TELEMETRY METRICS", color = TacticalGreen, style = MaterialTheme.typography.labelMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBlock("TOTAL DROPS", "$totalMissions", TacticalLight)
                        MetricBlock("VICTORIES", "$championCount", TacticalGreen)
                        MetricBlock("AVG KILLS", String.format("%.1f", avgKills), TacticalBlue)
                        MetricBlock("BEST DMG", "$bestDamage", TacticalAmber)
                    }
                }
            }

            // Scrollable History list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
                    .background(TacticalSurface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList) { item ->
                    val isSelected = selectedHistory?.id == item.id
                    val sdf = SimpleDateFormat("HH:mm - MM/dd", Locale.getDefault())
                    val dateStr = sdf.format(Date(item.timestamp))

                    val placementColor = if (item.placement == 1) TacticalGreen else if (item.placement <= 10) TacticalAmber else TacticalOrange

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) TacticalSurfaceVariant else Color(0x301B263B))
                            .border(1.dp, if (isSelected) TacticalGreen else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable {
                                TacticalAudio.playClick()
                                selectedHistory = item
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // High contrast circular placement indicator
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(placementColor.copy(alpha = 0.15f))
                                    .border(1.dp, placementColor, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#${item.placement}", color = placementColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            Column {
                                Text(item.location.uppercase(), color = TacticalLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("DEATH ZONE | $dateStr", color = TacticalGray, fontSize = 9.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("KILLS: ${item.kills}", color = TacticalLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("DMG: ${item.damageDealt}", color = TacticalGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                if (historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("NO TACTICAL DEPLOYMENTS RECORDED.", color = TacticalGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Right Column: Detailed timeline replay recap
        Column(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedHistory?.let { item ->
                val events = viewModel.deserializeEvents(item.gameLogJson)

                // Holographic telemetry banner image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_tactical_map_1782791758642),
                        contentDescription = "Holographic telemetry map",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xEB070B13))
                                )
                            )
                    )

                    // Overlay Labels
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "MISSION TELEMETRY RECAP",
                            style = MaterialTheme.typography.titleMedium,
                            color = TacticalGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SECTOR DEPLOYMENT: ${item.location.uppercase()} (${item.weather.uppercase()})",
                            color = TacticalLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Scrollable Event Timeline
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                    border = BorderStroke(1.dp, TacticalBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "TACTICAL CHRONICLE FEED",
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalBlue
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(events) { ev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x301B263B), RoundedCornerShape(4.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (ev.contains("killed") || ev.contains("ELIMINATED")) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = "Event icon",
                                        tint = if (ev.contains("killed")) TacticalOrange else TacticalGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    
                                    Text(
                                        text = ev,
                                        color = TacticalLight,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TacticalSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Awaiting", tint = TacticalGray, modifier = Modifier.size(48.dp))
                    Text("SELECT INTEL ENTRY TO RETRIEVE HISTORIC LOGS", color = TacticalGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun MetricBlock(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(TacticalSurfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = TacticalGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
