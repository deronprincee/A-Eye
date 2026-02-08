package com.example.aeye

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aeye.pages.LoginPage
import com.example.aeye.pages.SignupPage
import androidx.compose.runtime.collectAsState
import  androidx.compose.runtime.getValue
import androidx.navigation.navArgument
import com.example.aeye.pages.MainScreen
import com.example.aeye.pages.CycleLogFormScreen
import com.example.aeye.pages.DietPage
import com.example.aeye.pages.ExercisePage
import com.example.aeye.pages.MedicationPage
import com.example.aeye.ui.herascreens.HospitalScreen
import com.example.aeye.ui.herascreens.SearchScreen

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