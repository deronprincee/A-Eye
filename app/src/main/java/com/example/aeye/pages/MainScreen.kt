package com.example.aeye.pages
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aeye.R
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.aeye.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.aeye.AuthViewModel
import com.example.aeye.CycleLogListScreen
import com.example.aeye.ui.herascreens.HomeScreen
import com.example.aeye.ui.herascreens.HospitalScreen
import com.example.aeye.ui.herascreens.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier) {
    // Track the currently selected bottom navigation item
    val selectedItem = remember { mutableStateOf("Home") }

    // Define bottom navigation items with icon and title
    val bottomNavigationItems = listOf(
        BottomNavigationItem("Home", Icons.Filled.Home),
        BottomNavigationItem("Results", Icons.Filled.Favorite),
        BottomNavigationItem("Search", Icons.Filled.Search),
        BottomNavigationItem("Hospitals", Icons.Filled.LocalHospital)
    )

    // UI with top bar and bottom navigation
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // A-Eye logo and app name in top bar
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
                                "Results" -> navController.navigate("cyclelogs")
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

                authViewModel = authViewModel,
                navController = navController
            )
            "Results" -> CycleLogListScreen(
                navController = navController
            )
            "Search" -> SearchScreen(modifier = Modifier.padding(innerPadding))
            "Hospitals" -> HospitalScreen(navController = navController)
        }
    }
}