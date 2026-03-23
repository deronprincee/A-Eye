package com.example.aeye.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import com.example.aeye.ui.components.*
import com.example.aeye.viewmodel.CalibrationViewModel
import com.example.aeye.viewmodel.ResultsViewModel


/**
 * MainScreen owns the Scaffold (TopBar + BottomBar).
 * HomeScreen is ONLY content (no Scaffold) to prevent recursion/ANR.
 */
@Composable
fun MainScreen(
    navController: NavController,
    resultsViewModel: ResultsViewModel,
    calibrationViewModel: CalibrationViewModel,
    onOpenSettings: () -> Unit = { /* navController.navigate("settings") later */ },
    onTabSelected: (BottomTab) -> Unit = { /* optional callback */ }
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Home) }

    Scaffold(
        topBar = { AEyeTopBar(onSettingsClick = { /* nav later */ }) },
        bottomBar = {
            AEyeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                }
            )
        }
    ) { innerPadding ->
        // Main content switches by selected bottom tab
        when (selectedTab) {
            BottomTab.Home -> HomeScreen(
                modifier = Modifier,
                contentPadding = innerPadding,
                calibrationViewModel = calibrationViewModel,
                onTestClick = { route ->
                    navController.navigate(route)
                }
            )
            BottomTab.Results -> ResultScreen(
                resultsViewModel = resultsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            BottomTab.Search -> ChatbotScreen(
                resultsViewModel = resultsViewModel,
                contentPadding = innerPadding
            )
            BottomTab.Clinics -> PlaceholderScreen("Clinics (Coming Soon)")
        }
    }
}

// Bottom tabs
enum class BottomTab(
    val label: String,
    val placeholderIcon: ImageVector,
    val route: String
) {
    Home("Home", Icons.Filled.Home, "home"),
    Results("Results", Icons.Filled.PieChart, "results"),
    Search("Search", Icons.Filled.Search, "chat"),
    Clinics("Clinics", Icons.Filled.LocalHospital, "clinics")
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}
