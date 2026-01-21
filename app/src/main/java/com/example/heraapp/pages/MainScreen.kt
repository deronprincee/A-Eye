package com.example.heraapp.pages
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.heraapp.R
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.heraapp.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.heraapp.AuthViewModel
import com.example.heraapp.CycleLogListScreen
import com.example.heraapp.ui.herascreens.HomeScreen
import com.example.heraapp.ui.herascreens.HospitalScreen
import com.example.heraapp.ui.herascreens.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController,authViewModel: AuthViewModel) {
    // Track the currently selected bottom navigation item
    val selectedItem = remember { mutableStateOf("Home") }

    // Define bottom navigation items with icon and title
    val bottomNavigationItems = listOf(
        BottomNavigationItem("Home", Icons.Filled.Home),
        BottomNavigationItem("Cycle", Icons.Filled.Favorite),
        BottomNavigationItem("Search", Icons.Filled.Search),
        BottomNavigationItem("Hospitals", Icons.Filled.LocalHospital)
    )

    // UI with top bar and bottom navigation
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Hera logo and app name in top bar
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.hera),
                            contentDescription = "Hera Text Logo",
                            modifier = Modifier.align(Alignment.Center).height(100.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.flower),
                            contentDescription = "Hera Logo",
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp).size(45.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF89AAC)
                )
            )
        },
        // Render each item in bottom navigation
        bottomBar = {
            BottomAppBar {
                bottomNavigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem.value == item.title,
                        onClick = {
                            selectedItem.value = item.title
                            // Navigate to the appropriate screen
                            when (item.title) {
                                "Home" -> navController.navigate("home")
                                "Cycle" -> navController.navigate("cyclelogs")
                                "Search" -> navController.navigate("search")
                                "Hospitals" -> navController.navigate("hospitals")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Display content based on selected navigation item
        when (selectedItem.value) {
            "Home" -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel,
                navController = navController
            )
            "Cycle" -> CycleLogListScreen(
                navController = navController
            )
            "Search" -> SearchScreen(modifier = Modifier.padding(innerPadding))
            "Hospitals" -> HospitalScreen(navController = navController)
        }
    }
}