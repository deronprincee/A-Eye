package com.example.aeye.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aeye.ui.screens.LoginPage
import com.example.aeye.ui.screens.SignupPage
import androidx.compose.runtime.collectAsState
import  androidx.compose.runtime.getValue
import androidx.navigation.navArgument
import com.example.aeye.viewmodel.AuthViewModel
import com.example.aeye.ui.screens.CycleLogListScreen
import com.example.aeye.ui.screens.MainScreen
import com.example.aeye.ui.screens.CycleLogFormScreen
import com.example.aeye.ui.screens.DietPage
import com.example.aeye.ui.screens.ExercisePage
import com.example.aeye.ui.screens.MedicationPage
import com.example.aeye.ui.screens.HospitalScreen
import com.example.aeye.ui.screens.SearchScreen

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val startDestination =  "login"
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginPage(navController, authViewModel)
        }
        composable("signup") {
            SignupPage(navController, authViewModel)
        }
        composable("home") {
            MainScreen(navController = navController)
        }
        composable("cyclelogs") {
            CycleLogListScreen(navController)
        }

        composable("exercise") {
            ExercisePage(navController)
        }

        composable("diet") {
            DietPage(navController)
        }

        composable("medication") {
            MedicationPage(navController)
        }

        composable(
            "cyclelogform?docId={docId}",
            arguments = listOf(navArgument("docId") { defaultValue = "" })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId")
            CycleLogFormScreen(navController, docId)
        }

        composable("search") {
            SearchScreen()
        }

        composable("hospitals") {
            HospitalScreen(navController = navController)
        }


    }
}