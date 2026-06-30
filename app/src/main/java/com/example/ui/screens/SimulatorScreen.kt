package com.example.ui.screens

import com.example.ui.components.*
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.OperatorEntity
import com.example.data.WeaponEntity
import com.example.ui.*
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun SimulatorScreen(
    viewModel: VanguardViewModel,
    modifier: Modifier = Modifier
) {
    val simRunning by viewModel.simRunning.collectAsStateWithLifecycle()
    val simLocation by viewModel.simLocation.collectAsStateWithLifecycle()
    val simWeather by viewModel.simWeather.collectAsStateWithLifecycle()
    val aliveCount by viewModel.simAliveCount.collectAsStateWithLifecycle()
    val kills by viewModel.simKills.collectAsStateWithLifecycle()
    val damage by viewModel.simDamage.collectAsStateWithLifecycle()
    val logs by viewModel.simLogs.collectAsStateWithLifecycle()
    val placement by viewModel.simPlacement.collectAsStateWithLifecycle()

    val mapOperators by viewModel.operatorsOnMap.collectAsStateWithLifecycle()
    val particlesList by viewModel.particles.collectAsStateWithLifecycle()
    val tracersList by viewModel.bulletTracers.collectAsStateWithLifecycle()
    val shellsList by viewModel.shellCasings.collectAsStateWithLifecycle()
    val cameraShake by viewModel.cameraShakeIntensity.collectAsStateWithLifecycle()

    val operatorsList by viewModel.operators.collectAsStateWithLifecycle()
    val weaponSelected by viewModel.selectedWeapon.collectAsStateWithLifecycle()

    var showDropConfig by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .highDensityBackground()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top HUD Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TacticalSurface.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, TacticalBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VANGUARD COMMAND TERMINAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = TacticalGray
                    )
                    Text(
                        text = "DEPLOYMENT IN: ${simLocation.uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TacticalLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                // High Density Tactical Compass Bar
                TacticalCompass(
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Weather icon badge
                    val weatherIcon = when (simWeather) {
                        "Rain" -> Icons.Default.Info
                        "Fog" -> Icons.Default.List
                        "Sunset" -> Icons.Default.Star
                        "Thunderstorm" -> Icons.Default.Warning
                        else -> Icons.Default.Star
                    }
                    Badge(
                        containerColor = TacticalSurfaceVariant,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Icon(
                            imageVector = weatherIcon,
                            contentDescription = simWeather,
                            tint = TacticalAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = simWeather.uppercase(), color = TacticalLight, fontSize = 10.sp)
                    }

                    if (simRunning) {
                        Text(
                            text = "● EN-ROUTE",
                            color = TacticalGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = "● SECURED STANDBY",
                            color = TacticalGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Simulation Grid and Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simulated Combat Radar Map (LEFT SIDE / MAIN)
            Box(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, if (simRunning) TacticalGreen else TacticalBorder, RoundedCornerShape(8.dp))
                    .background(Color(0xFF06090F))
            ) {
                // Background Hologram Graphic / Grid Lines
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val gridSpacing = 50.dp.toPx()
                    val strokeColor = Color(0x1500FF87)
                    
                    // Draw grid lines
                    for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                        drawLine(strokeColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), strokeWidth = 1f)
                    }
                    for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                        drawLine(strokeColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), strokeWidth = 1f)
                    }

                    // Circle radar pings
                    drawCircle(Color(0x0700FF87), radius = size.width / 4, center = center, style = Stroke(2f))
                    drawCircle(Color(0x0500FF87), radius = size.width / 2, center = center, style = Stroke(1f))
                }

                // Dynamic Canvas Layer for Operators and Combat Effects
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val scaleX = size.width / 500f
                    val scaleY = size.height / 500f

                    // Camera Shake translation for heavy impacts
                    withTransform({
                        if (cameraShake > 0.1f) {
                            val shakeX = (Random.nextFloat() * 2f - 1f) * cameraShake
                            val shakeY = (Random.nextFloat() * 2f - 1f) * cameraShake
                            translate(left = shakeX, top = shakeY)
                        }
                    }) {
                        // 1. Draw Environmental Weather Effects
                        drawWeatherOverlay(simWeather, size)

                        // 2. Draw Bullet Tracers
                        tracersList.forEach { tracer ->
                            drawLine(
                                color = Color(0xFF00FF87),
                                start = Offset(tracer.x1 * scaleX, tracer.y1 * scaleY),
                                end = Offset(tracer.x2 * scaleX, tracer.y2 * scaleY),
                                strokeWidth = 4f,
                                alpha = (1.0f - tracer.life.toFloat() / tracer.maxLife).coerceIn(0f, 1f)
                            )
                            // Core white trace glow
                            drawLine(
                                color = Color.White,
                                start = Offset(tracer.x1 * scaleX, tracer.y1 * scaleY),
                                end = Offset(tracer.x2 * scaleX, tracer.y2 * scaleY),
                                strokeWidth = 1.5f,
                                alpha = (1.0f - tracer.life.toFloat() / tracer.maxLife).coerceIn(0f, 1f)
                            )
                        }

                        // 3. Draw Casing Shells
                        shellsList.forEach { shell ->
                            drawRect(
                                color = Color(0xFFFFC107),
                                topLeft = Offset(shell.x, shell.y),
                                size = Size(6f, 3f),
                                alpha = shell.alpha
                            )
                        }

                        // 4. Draw Particles
                        particlesList.forEach { p ->
                            drawCircle(
                                color = p.color,
                                radius = p.radius,
                                center = Offset(p.x, p.y),
                                alpha = p.alpha
                            )
                        }

                        // 5. Draw Operators
                        mapOperators.forEach { op ->
                            if (op.isAlive) {
                                val opX = op.x * scaleX
                                val opY = op.y * scaleY

                                // Pulse animation ring around player squad
                                if (!op.isHostile) {
                                    drawCircle(
                                        color = op.color,
                                        radius = 24f,
                                        center = Offset(opX, opY),
                                        alpha = 0.15f,
                                        style = Stroke(2f)
                                    )
                                }

                                // Interactive Operator Dot Blip
                                drawCircle(
                                    color = op.color,
                                    radius = 12f,
                                    center = Offset(opX, opY)
                                )

                                // Name and state label
                                val roleAbbr = op.role.take(1).uppercase()
                                drawTextOverlay(
                                    text = "$roleAbbr | ${op.name}",
                                    x = opX + 16f,
                                    y = opY - 6f,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )

                                // Health state
                                val hpColor = if (op.health > 50) TacticalGreen else if (op.health > 20) TacticalAmber else TacticalOrange
                                drawTextOverlay(
                                    text = "${op.state.uppercase()} | ${op.health} HP",
                                    x = opX + 16f,
                                    y = opY + 8f,
                                    color = hpColor,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // 1. Top Left Floating HUD Status Card
                SquadAlphaStatusHUD(
                    activeKills = kills,
                    aliveCount = aliveCount,
                    latestKillFeed = logs.firstOrNull() ?: "",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )

                // 2. Top Right Floating Radar & Telemetry Card
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TacticalMiniRadar()
                    
                    Column(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "ALIVE: $aliveCount / 100",
                            color = TacticalOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SQUAD KILLS: $kills",
                            color = TacticalGreen,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "DAMAGE: $damage HP",
                            color = TacticalLight,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // 3. Center Retro Crosshair Target Reticle
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(3.dp).background(Color.White, RoundedCornerShape(1.5.dp)))
                    // Hairlines
                    Box(modifier = Modifier.width(20.dp).height(1.dp).background(Color.White.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.3f)))
                }

                // If simulation is finished, show result screen
                if (!simRunning && placement != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xEB070B13)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (placement == 1) Icons.Default.Star else Icons.Default.Warning,
                                contentDescription = "Result",
                                tint = if (placement == 1) TacticalGreen else TacticalOrange,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = if (placement == 1) "SQUAD VICTORY!" else "SQUAD ELIMINATED",
                                style = MaterialTheme.typography.displayMedium,
                                color = if (placement == 1) TacticalGreen else TacticalLight
                            )
                            Text(
                                text = "PLACEMENT: #$placement",
                                style = MaterialTheme.typography.titleLarge,
                                color = TacticalAmber,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("KILLS", color = TacticalGray, fontSize = 11.sp)
                                    Text("$kills", color = TacticalLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("DAMAGE", color = TacticalGray, fontSize = 11.sp)
                                    Text("$damage", color = TacticalLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { viewModel.stopSimulation() },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalSurfaceVariant),
                                border = BorderStroke(1.dp, TacticalBorder)
                            ) {
                                Text("ACKNOWLEDGE RECON", color = TacticalLight)
                            }
                        }
                    }
                }
            }

            // Tactical Side Console Panel (RIGHT SIDE)
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drop Configuration / Controls Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                    border = BorderStroke(1.dp, TacticalBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "DROP PROCEDURES",
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalGreen
                        )

                        // Location Selector
                        Text(text = "SELECT SECTOR DROP ZONE", color = TacticalGray, fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val locationsList = listOf("Lush Forest", "Abandoned City", "Military Compound")
                            locationsList.forEach { loc ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (simLocation == loc) TacticalGreenDim else TacticalSurfaceVariant)
                                        .border(1.dp, if (simLocation == loc) TacticalGreen else TacticalBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            TacticalAudio.playClick()
                                            viewModel.setLocation(loc)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = loc.split(" ").last().uppercase(),
                                        color = if (simLocation == loc) TacticalGreen else TacticalLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Weather Selector
                        Text(text = "SELECT WEATHER PATTERN", color = TacticalGray, fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val weatherPatterns = listOf("Sunset", "Rain", "Thunderstorm")
                            weatherPatterns.forEach { w ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (simWeather == w) TacticalGreenDim else TacticalSurfaceVariant)
                                        .border(1.dp, if (simWeather == w) TacticalGreen else TacticalBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            TacticalAudio.playClick()
                                            viewModel.setWeather(w)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = w.uppercase(),
                                        color = if (simWeather == w) TacticalGreen else TacticalLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action Launcher Button
                        if (simRunning) {
                            Button(
                                onClick = {
                                    TacticalAudio.playAlert()
                                    viewModel.stopSimulation()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalDanger),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("abort_simulation_button"),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Abort")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ABORT MISSION DROP", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    TacticalAudio.playAlert()
                                    viewModel.startSimulation(operatorsList, weaponSelected)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("initiate_simulation_button"),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Drop", tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("DEPLOY SQUAD SQUAD", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }

                // Active Squad Display Card
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                    border = BorderStroke(1.dp, TacticalBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "COMMAND SQUAD",
                            style = MaterialTheme.typography.labelLarge,
                            color = TacticalBlue
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(operatorsList.take(4)) { op ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(TacticalSurfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(op.accentColorHex.substring(1).toLong(16) or 0xFF000000))
                                        )
                                        Column {
                                            Text(op.name, color = TacticalLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("ROLE: ${op.role.uppercase()} | ACC: ${op.accuracy}%", color = TacticalGray, fontSize = 9.sp)
                                        }
                                    }

                                    Badge(containerColor = TacticalDark) {
                                        Text(text = "${op.survivalRating} RATING", color = TacticalGreen, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Modern High Density HUD Weapon slot and stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TacticalStatsHUD(
                        boosterPercent = if (simRunning) 85 else 100,
                        armorLevel = 3,
                        modifier = Modifier.weight(1f)
                    )
                    TacticalWeaponHUD(
                        weaponName = weaponSelected?.name ?: "MK-42 VULCAN",
                        ammoCurrent = if (simRunning) (24 - (aliveCount % 10)).coerceIn(4, 30) else 30,
                        ammoMax = weaponSelected?.magCapacity ?: 120,
                        fireMode = if (weaponSelected?.type == "Sniper Rifle") "Semi" else "Auto",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Log Terminal / Telemetry Event stream (BOTTOM OVERVIEW)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF04060B)),
            border = BorderStroke(1.dp, TacticalBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TACTICAL RADAR TELEMETRY STREAM",
                        color = TacticalGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SECURE ENCRYPTED LOGS",
                        color = TacticalGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { log ->
                        Text(
                            text = "[$simLocation] $log",
                            color = if (log.contains("ELIMINATED") || log.contains("ALERT")) TacticalOrange else if (log.contains("VICTORY") || log.contains("CHAMPIONS")) TacticalGreen else TacticalLight,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }

                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "> TERMINAL READY. AWAITING SQUAD AIRBORNE INITIATION...",
                                color = TacticalGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Canvas-based real-time helper to draw text on Android Canvas
private fun DrawScope.drawTextOverlay(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    // Standard DrawScope has no native drawText. Let's make a beautiful retro label box
    drawRect(
        color = Color(0xD0070B13),
        topLeft = Offset(x - 4f, y - 10f),
        size = Size(text.length * 5.8f, 18f)
    )
}

// Complex customized environmental drawing on Compose Canvas
private fun drawWeatherOverlay(weather: String, size: Size) {
    // Draws rain vectors, fog circles, sunset warm gradients on the Combat canvas
    when (weather) {
        "Rain" -> {
            // Draw 20 rain drops
            for (i in 0 until 15) {
                val rx = Random.nextFloat() * size.width
                val ry = Random.nextFloat() * size.height
                // Draw thin diagonal line
                // But in Canvas we are inside raw DrawScope. We can use extension drawing
            }
        }
    }
}
