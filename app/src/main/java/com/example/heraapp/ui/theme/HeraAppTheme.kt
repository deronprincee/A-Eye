package com.example.heraapp.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.heraapp.BottomNavigationItem
import com.example.heraapp.R

// Composable for Hera top bar with centered logo and app name
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AEyeTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.hera),
                    contentDescription = "A-Eye Text Logo",
                    modifier = Modifier.align(Alignment.Center).height(100.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.setting),
                    contentDescription = "A-Eye Logo",
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp).size(45.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

// Composable for bottom navigation bar for main navigation
@Composable
fun AEyeBottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val bottomNavigationItems = listOf(
        BottomNavigationItem("Home", Icons.Filled.Home),
        BottomNavigationItem("Test Results", Icons.Filled.Favorite),
        BottomNavigationItem("Search", Icons.Filled.Search),
        BottomNavigationItem("Eye Clinics", Icons.Filled.LocalHospital)
    )

    BottomAppBar {
        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedItem == item.title,
                onClick = { onItemSelected(item.title) }
            )
        }
    }
}

// Handle navigation based on bottom bar selection
fun handleBottomNavSelection(navController: NavController, selectedItem: String) {
    when (selectedItem) {
        "Home" -> navController.navigate("home")
        "Results" -> navController.navigate("results")
        "Search" -> navController.navigate("search")
        "Hospitals" -> navController.navigate("hospitals")
    }
}

// Composable for background image
@Composable
fun AEyeBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.hera_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}



