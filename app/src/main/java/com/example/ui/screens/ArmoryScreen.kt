package com.example.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.R
import com.example.data.WeaponEntity
import com.example.ui.TacticalAudio
import com.example.ui.VanguardViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ArmoryScreen(
    viewModel: VanguardViewModel,
    modifier: Modifier = Modifier
) {
    val weaponsList by viewModel.weapons.collectAsStateWithLifecycle()
    val selectedWeapon by viewModel.selectedWeapon.collectAsStateWithLifecycle()

    var customName by remember { mutableStateOf("") }
    var customType by remember { mutableStateOf("Assault Rifle") }
    var customDamage by remember { mutableStateOf(40) }
    var customRange by remember { mutableStateOf(100) }
    var customRecoil by remember { mutableStateOf(50) }
    var customScope by remember { mutableStateOf("RDS") }
    var customCamo by remember { mutableStateOf("Carbon Black") }

    var showCreateDialog by remember { mutableStateOf(false) }

    // Firing range coordinates state
    val bulletImpacts = remember { mutableStateListOf<Offset>() }
    val coroutineScope = rememberCoroutineScope()
    var isFiring by remember { mutableStateOf(false) }
    var gunKickback by remember { mutableStateOf(0f) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .highDensityBackground()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column - Weapon catalog list
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MILITARY ARMORY",
                    style = MaterialTheme.typography.titleLarge,
                    color = TacticalLight,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        TacticalAudio.playClick()
                        showCreateDialog = true
                    },
                    modifier = Modifier.background(TacticalGreenDim, RoundedCornerShape(4.dp)).size(36.dp).testTag("add_weapon_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Weapon", tint = TacticalGreen)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
                    .background(TacticalSurface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weaponsList) { wp ->
                    val isSelected = selectedWeapon?.id == wp.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) TacticalSurfaceVariant else Color(0x301B263B))
                            .border(1.dp, if (isSelected) TacticalGreen else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable {
                                TacticalAudio.playClick()
                                viewModel.selectWeapon(wp)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = wp.name,
                                color = TacticalLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = wp.type.uppercase(),
                                color = TacticalGray,
                                fontSize = 10.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Select",
                            tint = if (isSelected) TacticalGreen else TacticalGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Right Column - Detailed Blueprint & Firing Range
        Column(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedWeapon?.let { wp ->
                // Weapon showcase visual card using our generated image asset
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_weapon_showcase_1782791771459),
                        contentDescription = "Weapon Showcase Blueprint",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Scrim overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xEB070B13))
                                )
                            )
                    )

                    // Specs summary
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = wp.name.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = TacticalGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "CAMO PATTERN: ${wp.camo.uppercase()} | OPTICS: ${wp.scopeType.uppercase()}",
                            color = TacticalLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Split Layout: Specs and Interactive Firing Range
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Specs Details Card (LEFT SUB-PANEL)
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                        border = BorderStroke(1.dp, TacticalBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "WEAPON CALIBRATIONS",
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalGreen
                            )

                            Text(
                                text = wp.description,
                                color = TacticalGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            HorizontalDivider(color = TacticalBorder)

                            StatProgressBar("TERMINAL BALLISTICS (DAMAGE)", wp.damage, TacticalOrange)
                            StatProgressBar("COMPRESSION (FIRE RATE / RPM)", wp.fireRate / 10, TacticalBlue)
                            StatProgressBar("BARREL DISPERSION (RECOIL)", wp.recoil, TacticalAmber)
                            StatProgressBar("TACTICAL FOCUS RANGE", wp.range / 10, TacticalLight)

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    TacticalAudio.playAlert()
                                    viewModel.deleteWeapon(wp)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalDanger),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Scrap", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DECONSTRUCT WEAPON", fontSize = 11.sp)
                            }
                        }
                    }

                    // Interactive Firing Range Canvas Target Pad (RIGHT SUB-PANEL)
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF04060A)),
                        border = BorderStroke(1.dp, TacticalBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "BALLISTIC TEST RANGE",
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalBlue
                            )

                            // Target Canvas
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0C0F16))
                                    .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
                            ) {
                                Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val canvasCenter = Offset(size.width / 2, size.height / 2)

                                    // Draw concentric target bullseyes with recoil kick displacements
                                    val kickOffsetY = -gunKickback * 1.5f
                                    val centerWithKick = Offset(canvasCenter.x, canvasCenter.y + kickOffsetY)

                                    drawCircle(Color(0xFF2C3E50), radius = size.width / 2.2f, center = centerWithKick, style = Stroke(1f))
                                    drawCircle(Color(0xFF34495E), radius = size.width / 3.2f, center = centerWithKick, style = Stroke(1.5f))
                                    drawCircle(Color(0xFF95A5A6), radius = size.width / 5.2f, center = centerWithKick, style = Stroke(2f))
                                    drawCircle(TacticalOrange, radius = size.width / 12f, center = centerWithKick)

                                    // Draw coordinate grids
                                    drawLine(Color(0x15FFFFFF), Offset(0f, centerWithKick.y), Offset(size.width, centerWithKick.y))
                                    drawLine(Color(0x15FFFFFF), Offset(centerWithKick.x, 0f), Offset(centerWithKick.x, size.height))

                                    // Draw impact bullet holes
                                    bulletImpacts.forEach { offset ->
                                        // Draw charcoal lead entry hole
                                        drawCircle(Color(0xFF1A1A1A), radius = 6f, center = offset)
                                        // Draw glowing lead core ring
                                        drawCircle(TacticalAmber, radius = 2f, center = offset)
                                    }
                                }

                                if (isFiring) {
                                    // Muzzle flash visual trigger on Firing pad center bottom
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                            .size(24.dp)
                                            .background(TacticalAmber, RoundedCornerShape(12.dp))
                                    )
                                }
                            }

                            // Trigger Shoot Control Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (!isFiring) {
                                            isFiring = true
                                            coroutineScope.launch {
                                                val count = if (wp.type == "Sniper Rifle") 1 else 6
                                                for (i in 0 until count) {
                                                    if (wp.type == "Sniper Rifle") {
                                                        TacticalAudio.playSniperShot()
                                                    } else {
                                                        TacticalAudio.playGunshot()
                                                    }

                                                    // Calculate impact offset with weapon's recoil dispersion rating
                                                    val dispX = (Random.nextFloat() * 2f - 1f) * wp.recoil * 0.8f
                                                    val dispY = (Random.nextFloat() * 2f - 1f) * wp.recoil * 0.8f
                                                    
                                                    // Base kickback
                                                    gunKickback = wp.recoil.toFloat() / 2f
                                                    viewModel.triggerMuzzleFlash(150f, 150f)

                                                    // Centered hit plus recoil error
                                                    bulletImpacts.add(
                                                        Offset(100f + dispX, 100f + dispY)
                                                    )

                                                    delay(120)
                                                    gunKickback *= 0.5f
                                                }
                                                isFiring = false
                                                TacticalAudio.playReload()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TacticalAmber),
                                    modifier = Modifier.weight(1.3f).testTag("firing_range_shoot_button"),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Shoot", tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PULL TRIGGER", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        TacticalAudio.playClick()
                                        bulletImpacts.clear()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TacticalSurfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = TacticalLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("RESET", color = TacticalLight, fontSize = 11.sp)
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
                Text("ACQUIRING WEAPON SCHEMATIC LOGISTICS...", color = TacticalGray, fontFamily = FontFamily.Monospace)
            }
        }
    }

    // New Custom Weapon builder overlay Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    "CONSTRUCT MODULAR MILITARY WEAPON",
                    color = TacticalGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("WEAPON PROTOCOL DESIGNATION") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TacticalSurfaceVariant,
                            unfocusedContainerColor = TacticalSurface,
                            focusedTextColor = TacticalLight,
                            unfocusedTextColor = TacticalLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("weapon_name_input")
                    )

                    // Weapon Type Select
                    Text("TACTICAL SYSTEM CHASSIS", color = TacticalGray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val types = listOf("Assault Rifle", "Sniper Rifle", "SMG")
                        types.forEach { t ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (customType == t) TacticalGreenDim else TacticalSurface)
                                    .border(1.dp, if (customType == t) TacticalGreen else TacticalBorder, RoundedCornerShape(4.dp))
                                    .clickable { customType = t }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(t.split(" ").first().uppercase(), color = if (customType == t) TacticalGreen else TacticalLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Stats customization with Sliders
                    Text("BARREL LETHAL DAMAGE: $customDamage HP", color = TacticalGray, fontSize = 11.sp)
                    Slider(
                        value = customDamage.toFloat(),
                        onValueChange = { customDamage = it.toInt() },
                        valueRange = 25f..95f,
                        colors = SliderDefaults.colors(activeTrackColor = TacticalGreen, thumbColor = TacticalGreen)
                    )

                    Text("WEAPON STABILIZER RECOIL: $customRecoil", color = TacticalGray, fontSize = 11.sp)
                    Slider(
                        value = customRecoil.toFloat(),
                        onValueChange = { customRecoil = it.toInt() },
                        valueRange = 10f..90f,
                        colors = SliderDefaults.colors(activeTrackColor = TacticalGreen, thumbColor = TacticalGreen)
                    )

                    // Camouflage choice
                    Text("FRACTAL CAMOUFLAGE TYPE", color = TacticalGray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val camos = listOf("Digital Urban", "Carbon Black", "Desert Storm")
                        camos.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (customCamo == c) TacticalGreenDim else TacticalSurface)
                                    .border(1.dp, if (customCamo == c) TacticalGreen else TacticalBorder, RoundedCornerShape(4.dp))
                                    .clickable { customCamo = c }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(c.split(" ").last().uppercase(), color = if (customCamo == c) TacticalGreen else TacticalLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            TacticalAudio.playAlert()
                            viewModel.addWeapon(
                                WeaponEntity(
                                    name = customName,
                                    type = customType,
                                    damage = customDamage,
                                    fireRate = if (customType == "SMG") 900 else if (customType == "Sniper Rifle") 60 else 650,
                                    recoil = customRecoil,
                                    range = if (customType == "Sniper Rifle") 800 else if (customType == "SMG") 90 else 250,
                                    magCapacity = if (customType == "SMG") 45 else if (customType == "Sniper Rifle") 5 else 30,
                                    scopeType = if (customType == "Sniper Rifle") "8x Ballistic" else "RDS",
                                    camo = customCamo,
                                    description = "Custom calibrated $customType featuring premium $customCamo tactical frame, customized to reduce recoil and maximize kinetic force."
                                )
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen),
                    modifier = Modifier.testTag("save_weapon_button")
                ) {
                    Text("SAVE TO ARMORY", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = TacticalGray)
                }
            },
            containerColor = TacticalSurface
        )
    }
}
