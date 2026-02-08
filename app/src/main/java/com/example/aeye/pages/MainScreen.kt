package com.example.aeye.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aeye.R
import com.example.aeye.ui.herascreens.HomeScreen
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.*


/**
 * MainScreen owns the Scaffold (TopBar + BottomBar).
 * HomeScreen is ONLY content (no Scaffold) to prevent recursion/ANR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    onOpenSettings: () -> Unit = { /* navController.navigate("settings") later */ },
    onTabSelected: (BottomTab) -> Unit = { /* optional callback */ }
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Home) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Center logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(140.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            onTabSelected(tab)

                            // Optional: if you want real navigation later, do it here safely:
                            // navController.navigate(tab.route) {
                            //     launchSingleTop = true
                            //     restoreState = true
                            //     popUpTo(navController.graph.startDestinationId) { saveState = true }
                            // }
                        },
                        icon = {
                            // Placeholder icons — you said you will replace them
                            Icon(
                                imageVector = tab.placeholderIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Main content switches by selected bottom tab (no nav loops)
        when (selectedTab) {
            BottomTab.Home -> HomeScreen(
                modifier = Modifier,
                contentPadding = innerPadding,
                onTestClick = { testRoute ->
                    // We'll create test pages later. For now, safe call:
                    // navController.navigate(testRoute)
                }
            )

            BottomTab.Results -> PlaceholderScreen("Results (Coming Soon)")
            BottomTab.Chat -> PlaceholderScreen("Chat (Coming Soon)")
            BottomTab.Clinics -> PlaceholderScreen("Clinics (Coming Soon)")
        }
    }
}

/** Bottom tabs */
enum class BottomTab(
    val label: String,
    val placeholderIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) {
    Home("Home", androidx.compose.material.icons.Icons.Filled.Home, "home"),
    Results("Results", androidx.compose.material.icons.Icons.Filled.Favorite, "results"),
    Chat("Chat", androidx.compose.material.icons.Icons.Filled.Chat, "chat"),
    Clinics("Clinics", androidx.compose.material.icons.Icons.Filled.LocalHospital, "clinics")
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
