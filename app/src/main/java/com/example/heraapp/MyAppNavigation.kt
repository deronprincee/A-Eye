package com.example.heraapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.heraapp.pages.LoginPage
import com.example.heraapp.pages.SignupPage
import androidx.compose.runtime.collectAsState
import  androidx.compose.runtime.getValue
import androidx.navigation.navArgument
import com.example.heraapp.pages.MainScreen
import com.example.heraapp.pages.CycleLogFormScreen
import com.example.heraapp.HeraChatScreen
import com.example.heraapp.pages.DietPage
import com.example.heraapp.pages.ExercisePage
import com.example.heraapp.pages.MedicationPage
import com.example.heraapp.ui.herascreens.HospitalScreen
import com.example.heraapp.ui.herascreens.SearchScreen

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
            MainScreen(navController, authViewModel)
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