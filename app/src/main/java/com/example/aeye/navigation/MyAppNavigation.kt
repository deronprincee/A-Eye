package com.example.aeye.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aeye.ui.screens.LoginPage
import com.example.aeye.ui.screens.SignupPage
import androidx.navigation.navArgument
import com.example.aeye.viewmodel.AuthViewModel
import com.example.aeye.ui.screens.MainScreen
import com.example.aeye.ui.screens.SearchScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.aeye.ui.screens.SnellenTestScreen

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null) "home"
        else "login"

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
        composable("snellen") {
            SnellenTestScreen(navController)
        }
        composable("search") {
            SearchScreen()
        }
    }
}