package com.example.aeye.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aeye.navigation.BottomNavigationItem
import com.example.aeye.R
import com.example.aeye.ui.screens.BottomTab

// Composable for Hera top bar with centered logo and app name

@Composable
fun AppScaffold(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onSettingsClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { AEyeTopBar(onSettingsClick) },
        bottomBar = { AEyeBottomBar(selectedTab, onTabSelected) },
        content = content
    )

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AEyeTopBar(
    onSettingsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "A-Eye Text Logo",
                    modifier = Modifier.align(Alignment.Center).height(100.dp)
                )
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.setting),
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

// Composable for bottom navigation bar for main navigation
@Composable
fun AEyeBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    val bottomNavigationItems = listOf(
        BottomNavigationItem("Home", Icons.Filled.Home),
        BottomNavigationItem("Test Results", Icons.Filled.Favorite),
        BottomNavigationItem("Search", Icons.Filled.Search),
        BottomNavigationItem("Eye Clinics", Icons.Filled.LocalHospital)
    )

    BottomAppBar {
        BottomTab.values().forEach { tab ->
            NavigationBarItem(
                icon = { Icon(tab.placeholderIcon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

// Handle navigation based on bottom bar selection
fun handleBottomNavSelection(navController: NavController, tab: BottomTab) {
    when (tab) {
        BottomTab.Home -> navController.navigate("home")
        BottomTab.Results -> navController.navigate("results")
        BottomTab.Search -> navController.navigate("search")
        BottomTab.Clinics -> navController.navigate("clinics")
    }
}

// Composable for background image
/*
@Composable
fun AEyeBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.aeye_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}
*/


