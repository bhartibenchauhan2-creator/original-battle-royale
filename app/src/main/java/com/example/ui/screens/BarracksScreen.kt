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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.random.Random
import com.example.ui.components.*
import com.example.R
import com.example.data.OperatorEntity
import com.example.ui.TacticalAudio
import com.example.ui.VanguardViewModel
import com.example.ui.theme.*

@Composable
fun BarracksScreen(
    viewModel: VanguardViewModel,
    modifier: Modifier = Modifier
) {
    val operatorsList by viewModel.operators.collectAsStateWithLifecycle()
    val selectedOp by viewModel.selectedOperator.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .highDensityBackground()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel - Operator Selection List
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
                    text = "BARRACKS ROSTER",
                    style = MaterialTheme.typography.titleLarge,
                    color = TacticalLight,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = {
                        TacticalAudio.playClick()
                        showCreateDialog = true
                    },
                    modifier = Modifier.background(TacticalGreenDim, RoundedCornerShape(4.dp)).size(36.dp).testTag("add_operator_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Operator", tint = TacticalGreen)
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
                items(operatorsList) { op ->
                    val isSelected = selectedOp?.id == op.id
                    val opColor = Color(op.accentColorHex.substring(1).toLong(16) or 0xFF000000)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) TacticalSurfaceVariant else Color(0x301B263B))
                            .border(1.dp, if (isSelected) opColor else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable {
                                TacticalAudio.playClick()
                                viewModel.selectOperator(op)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mini accent dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(opColor)
                            )
                            Column {
                                Text(
                                    text = op.name,
                                    color = TacticalLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ROLE: ${op.role.uppercase()}",
                                    color = TacticalGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Select",
                            tint = if (isSelected) opColor else TacticalGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Right Panel - Detailed Operator Customs View
        Column(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedOp?.let { op ->
                val opAccent = Color(op.accentColorHex.substring(1).toLong(16) or 0xFF000000)

                // Operator Hero Banner using our custom generated high-quality asset!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_operator_banner_1782791745105),
                        contentDescription = "Operator Render Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Scrim overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xE0070B13))
                                )
                            )
                    )

                    // Role Badge
                    Badge(
                        containerColor = opAccent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = op.role.uppercase(),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Operator Name Tag on Banner bottom
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = op.name.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = TacticalLight,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "ACTIVE SQUADRON MEMBER",
                            color = TacticalGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Operator Visual Customs Specification Grid
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                    border = BorderStroke(1.dp, TacticalBorder)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "VISUAL IDENTITY SPECIFICATIONS",
                                style = MaterialTheme.typography.labelLarge,
                                color = opAccent
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Left details column
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    DetailItem("BODY RATIO", "${String.format("%.2f", op.bodyProportion)}x (ATHLETIC BUILD)", Icons.Default.Build)
                                    DetailItem("FACIAL SKIN", op.skinTexture.uppercase(), Icons.Default.AccountCircle)
                                    DetailItem("EYE RETINA", op.eyeColor.uppercase(), Icons.Default.Info)
                                    DetailItem("HAIR MATRIX", op.hairStyle.uppercase(), Icons.Default.Settings)
                                }

                                // Right details column
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    DetailItem("TACTICAL HELMET", op.helmet.uppercase(), Icons.Default.Warning)
                                    DetailItem("ARMOR PLATING", op.armor.uppercase(), Icons.Default.Lock)
                                    DetailItem("GEAR BACKPACK", op.backpack.uppercase(), Icons.Default.Star)
                                    DetailItem("ACCENT SIGNAL", op.accentColorHex, Icons.Default.Favorite)
                                }
                            }
                        }

                        // Combat statistics section
                        item {
                            Text(
                                text = "OPERATOR PERFORMANCE TELEMETRY",
                                style = MaterialTheme.typography.labelLarge,
                                color = TacticalLight
                            )
                        }

                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatProgressBar("ACCURACY PROTOCOL", op.accuracy, opAccent)
                                StatProgressBar("SURVIVAL PROBABILITY", op.survivalRating, TacticalGreen)
                            }
                        }

                        // Disconnect / Delete button
                        item {
                            Button(
                                onClick = {
                                    TacticalAudio.playAlert()
                                    viewModel.deleteOperator(op)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalDanger),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Retire")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("RETIRE OPERATOR FROM ROSTER")
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
                Text("AWAITING OPERATOR ROSTER CONGESTION...", color = TacticalGray, fontFamily = FontFamily.Monospace)
            }
        }
    }

    // Interactive Custom Operator Creation Panel Overlay
    if (showCreateDialog) {
        var opName by remember { mutableStateOf("") }
        var opRole by remember { mutableStateOf("Assault") }
        var opSkin by remember { mutableStateOf("Rugged") }
        var opHair by remember { mutableStateOf("Undercut") }
        var opEyes by remember { mutableStateOf("Tactical Green") }
        var opHelmet by remember { mutableStateOf("Lightweight Assault") }
        var opArmor by remember { mutableStateOf("Carbon Chestplate") }
        var opBackpack by remember { mutableStateOf("Assault Pack") }
        var opAccent by remember { mutableStateOf("#3DDC84") }
        var opProportions by remember { mutableStateOf(1.0f) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    "FABRICATE ORIGINAL CUSTOM OPERATOR",
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
                        value = opName,
                        onValueChange = { opName = it },
                        label = { Text("OPERATOR CALLSIGN") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TacticalSurfaceVariant,
                            unfocusedContainerColor = TacticalSurface,
                            focusedTextColor = TacticalLight,
                            unfocusedTextColor = TacticalLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("operator_name_input")
                    )

                    // Role Select
                    Text("TACTICAL SPECIALIZATION", color = TacticalGray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val roles = listOf("Assault", "Sniper", "Recon", "Support")
                        roles.forEach { r ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (opRole == r) TacticalGreenDim else TacticalSurface)
                                    .border(1.dp, if (opRole == r) TacticalGreen else TacticalBorder, RoundedCornerShape(4.dp))
                                    .clickable { opRole = r }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(r.uppercase(), color = if (opRole == r) TacticalGreen else TacticalLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Slide slider for Proportions
                    Text("ATHLETIC BUILD SCALE: ${String.format("%.2f", opProportions)}x", color = TacticalGray, fontSize = 11.sp)
                    Slider(
                        value = opProportions,
                        onValueChange = { opProportions = it },
                        valueRange = 0.8f..1.2f,
                        colors = SliderDefaults.colors(activeTrackColor = TacticalGreen, thumbColor = TacticalGreen)
                    )

                    // Helmet dropdown list representation
                    Text("SURVIVAL HELMET SYSTEM", color = TacticalGray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val helmets = listOf("None", "Lightweight", "Adv. NVG", "SpecOps Cap")
                        helmets.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (opHelmet == h) TacticalGreenDim else TacticalSurface)
                                    .border(1.dp, if (opHelmet == h) TacticalGreen else TacticalBorder, RoundedCornerShape(4.dp))
                                    .clickable { opHelmet = h }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(h.uppercase(), color = if (opHelmet == h) TacticalGreen else TacticalLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Armor Select representation
                    Text("LIGHTWEIGHT TACTICAL ARMOR PLATES", color = TacticalGray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val armors = listOf("Carbon Vest", "Light Kevlar", "Ghillie Hood")
                        armors.forEach { a ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (opArmor == a) TacticalGreenDim else TacticalSurface)
                                    .border(1.dp, if (opArmor == a) TacticalGreen else TacticalBorder, RoundedCornerShape(4.dp))
                                    .clickable { opArmor = a }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(a.split(" ").first().uppercase(), color = if (opArmor == a) TacticalGreen else TacticalLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Accent custom color signal selector
                    Text("SQUADRON EMITTER COLOR SIGNAL", color = TacticalGray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf("#3DDC84", "#00E5FF", "#FFD600", "#FF3D00")
                        colors.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(col.substring(1).toLong(16) or 0xFF000000))
                                    .border(2.dp, if (opAccent == col) TacticalLight else Color.Transparent, RoundedCornerShape(4.dp))
                                    .clickable { opAccent = col }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (opName.isNotBlank()) {
                            TacticalAudio.playAlert()
                            viewModel.addOperator(
                                OperatorEntity(
                                    name = opName,
                                    role = opRole,
                                    bodyProportion = opProportions,
                                    skinTexture = opSkin,
                                    eyeColor = opEyes,
                                    hairStyle = opHair,
                                    helmet = opHelmet,
                                    armor = opArmor,
                                    backpack = opBackpack,
                                    accentColorHex = opAccent,
                                    survivalRating = Random.nextInt(20) + 70,
                                    accuracy = Random.nextInt(25) + 70
                                )
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalGreen),
                    modifier = Modifier.testTag("save_operator_button")
                ) {
                    Text("SAVE TO DATABASE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("ABORT", color = TacticalGray)
                }
            },
            containerColor = TacticalSurface
        )
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TacticalSurfaceVariant, RoundedCornerShape(4.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = TacticalBlue, modifier = Modifier.size(16.dp))
        Column {
            Text(label, color = TacticalGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            Text(value, color = TacticalLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatProgressBar(
    label: String,
    value: Int,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TacticalGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("$value%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = TacticalSurfaceVariant
        )
    }
}
