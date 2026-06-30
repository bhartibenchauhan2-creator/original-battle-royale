package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom background modifier for applying the High Density survival theme
 * deep radial gradient: from #2D3628 (40% up the screen) to #0E110D.
 */
fun Modifier.highDensityBackground(): Modifier = this.then(
    Modifier.background(
        Brush.radialGradient(
            0.0f to Color(0xFF2D3628).copy(alpha = 0.35f),
            1.0f to TacticalDark,
            radius = 1200f
        )
    )
)

/**
 * Top center precise tactical compass bar displaying ticks and degrees, matching HTML mockup.
 */
@Composable
fun TacticalCompass(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(220.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, TacticalBorder), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left & Right boundary lines
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.2f))
                .align(Alignment.CenterStart)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.2f))
                .align(Alignment.CenterEnd)
        )

        // Ticks list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompassTick("285", false)
            CompassTick("300", false)
            CompassTick("315", true) // highlighted center tick
            CompassTick("330", false)
            CompassTick("345", false)
        }

        // Center Indicator Arrow pointing down
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(8.dp)
                .height(4.dp)
                .background(TacticalAmber, RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
        )
    }
}

@Composable
private fun CompassTick(value: String, isCenter: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            color = if (isCenter) TacticalAmber else Color.White.copy(alpha = 0.6f),
            fontSize = if (isCenter) 11.sp else 9.sp,
            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            modifier = if (isCenter) Modifier.scale(1.1f) else Modifier
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(if (isCenter) 8.dp else if (value.toInt() % 30 == 0) 5.dp else 3.dp)
                .background(if (isCenter) TacticalAmber else Color.White.copy(alpha = 0.5f))
        )
    }
}

// Simple helper extension to apply scale
private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.padding(horizontal = ((scale - 1f) * 4f).coerceAtLeast(0f).dp)
)

/**
 * Squad status dashboard HUD mimicking "SQUAD ALPHA" stats container.
 */
@Composable
fun SquadAlphaStatusHUD(
    modifier: Modifier = Modifier,
    activeKills: Int = 4,
    aliveCount: Int = 52,
    latestKillFeed: String = "SERPENT_X7 KILLED NOMAD_99"
) {
    var tickState by remember { mutableStateOf(false) }
    
    // Pulse animation for green status dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TacticalGreen.copy(alpha = alphaPulse))
                    .border(1.dp, TacticalGreen, RoundedCornerShape(4.dp))
            )
            Text(
                text = "SQUAD ALPHA",
                color = TacticalLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // Member bars
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Member 1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFD1E6C1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("1", color = Color(0xFF1A1C18), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { 1.0f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = TacticalGreen,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }

            // Member 2
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { 0.66f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = TacticalAmber,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }

        HorizontalDivider(color = TacticalBorder)

        // Kill count & Alive indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$aliveCount ALIVE",
                    color = TacticalLight,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d KILLS", activeKills),
                    color = TacticalAmber,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Tiny live feed text
        if (latestKillFeed.isNotEmpty()) {
            Text(
                text = latestKillFeed.uppercase(),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                lineHeight = 10.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Holographic Mini Map Radar scan grid with pulsing Orange core point
 */
@Composable
fun TacticalMiniRadar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    // Scanning sweep angle rotation
    val angleSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    // Pulsing outer ring
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseOutExpo),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TacticalDark)
            .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerPt = Offset(size.width / 2f, size.height / 2f)
            
            // Draw coordinate grid lines
            drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, centerPt.y), Offset(size.width, centerPt.y))
            drawLine(Color.White.copy(alpha = 0.1f), Offset(centerPt.x, 0f), Offset(centerPt.x, size.height))
            
            // Draw scanning range circles
            drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width / 2.2f, center = centerPt, style = Stroke(1f))
            drawCircle(Color.White.copy(alpha = 0.08f), radius = size.width / 3.5f, center = centerPt, style = Stroke(1f))
            
            // Pulsing target locator ring
            drawCircle(
                color = TacticalBlue.copy(alpha = (1.0f - (ringPulse / 44f)).coerceIn(0f, 1f)),
                radius = ringPulse,
                center = centerPt,
                style = Stroke(1.5f)
            )

            // Dynamic scan line sweep vector
            val rad = Math.toRadians(angleSweep.toDouble())
            val endX = centerPt.x + (size.width / 2f) * cos(rad).toFloat()
            val endY = centerPt.y + (size.height / 2f) * sin(rad).toFloat()
            drawLine(
                color = TacticalBlue.copy(alpha = 0.4f),
                start = centerPt,
                end = Offset(endX, endY),
                strokeWidth = 2f
            )

            // High Density core orange target point
            drawCircle(
                color = TacticalAmber,
                radius = 4f,
                center = centerPt
            )
        }

        // Label overlay
        Text(
            text = "N_SECTOR",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, start = 6.dp)
        )
    }
}

/**
 * Modern progress trackers for Booster energy & Armor levels
 */
@Composable
fun TacticalStatsHUD(
    modifier: Modifier = Modifier,
    boosterPercent: Int = 85,
    armorLevel: Int = 3
) {
    Column(
        modifier = modifier.width(180.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Booster Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(TacticalBlue))
                Text("BOOSTER: $boosterPercent%", color = TacticalLight.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
        LinearProgressIndicator(
            progress = { boosterPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = TacticalBlue,
            trackColor = Color.White.copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Armor Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(TacticalGreen))
                Text("ARMOR: LVL $armorLevel", color = TacticalLight.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
        LinearProgressIndicator(
            progress = { 1.0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp)),
            color = TacticalGreen,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

/**
 * High Density Weapon readout slot displaying active weapon details, ammo and configuration.
 */
@Composable
fun TacticalWeaponHUD(
    modifier: Modifier = Modifier,
    weaponName: String = "MK-42 VULCAN",
    ammoCurrent: Int = 30,
    ammoMax: Int = 120,
    fireMode: String = "Auto"
) {
    Box(
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Weapon name & fire mode tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weaponName.uppercase(),
                    color = TacticalAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .background(TacticalAmber.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = fireMode.uppercase(),
                        color = TacticalAmber,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Bullet visual matrix & text count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Bullet vertical bars indicator representing ammo capacity remaining
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    val activeTicks = (ammoCurrent / 7.5).toInt().coerceIn(1, 4)
                    for (i in 0 until 4) {
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(10.dp)
                                .background(if (i < activeTicks) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }

                // Digital text
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$ammoCurrent",
                        color = TacticalLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = " / $ammoMax",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}
