package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TacticalAudio
import com.example.ui.VanguardViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalDark
import com.example.ui.theme.TacticalSurface

enum class TacticalTab {
    SIMULATOR,
    BARRACKS,
    ARMORY,
    INTEL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(TacticalTab.SIMULATOR) }
                val viewModel: VanguardViewModel = viewModel()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column {
                            HorizontalDivider(color = TacticalBorder)
                            NavigationBar(
                                containerColor = TacticalSurface,
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                            NavigationBarItem(
                                selected = currentTab == TacticalTab.SIMULATOR,
                                onClick = {
                                    TacticalAudio.playClick()
                                    currentTab = TacticalTab.SIMULATOR
                                },
                                label = { Text("SIMULATOR") },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Simulator") },
                                modifier = Modifier.testTag("nav_simulator_tab")
                            )

                            NavigationBarItem(
                                selected = currentTab == TacticalTab.BARRACKS,
                                onClick = {
                                    TacticalAudio.playClick()
                                    currentTab = TacticalTab.BARRACKS
                                },
                                label = { Text("BARRACKS") },
                                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Barracks") },
                                modifier = Modifier.testTag("nav_barracks_tab")
                            )

                            NavigationBarItem(
                                selected = currentTab == TacticalTab.ARMORY,
                                onClick = {
                                    TacticalAudio.playClick()
                                    currentTab = TacticalTab.ARMORY
                                },
                                label = { Text("ARMORY") },
                                icon = { Icon(Icons.Default.Lock, contentDescription = "Armory") },
                                modifier = Modifier.testTag("nav_armory_tab")
                            )

                            NavigationBarItem(
                                selected = currentTab == TacticalTab.INTEL,
                                onClick = {
                                    TacticalAudio.playClick()
                                    currentTab = TacticalTab.INTEL
                                },
                                label = { Text("INTEL") },
                                icon = { Icon(Icons.Default.Info, contentDescription = "Intel") },
                                modifier = Modifier.testTag("nav_intel_tab")
                            )
                        }
                    }
                }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TacticalDark)
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            TacticalTab.SIMULATOR -> SimulatorScreen(viewModel = viewModel)
                            TacticalTab.BARRACKS -> BarracksScreen(viewModel = viewModel)
                            TacticalTab.ARMORY -> ArmoryScreen(viewModel = viewModel)
                            TacticalTab.INTEL -> IntelScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
