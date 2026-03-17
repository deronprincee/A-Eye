package com.example.aeye.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aeye.data.Firestore
import com.example.aeye.ui.components.handleBottomNavSelection
import com.example.aeye.ui.screens.BottomTab
import com.example.aeye.ui.screens.CalibrationScreen
import com.example.aeye.ui.screens.LoginPage
import com.example.aeye.ui.screens.SignupPage
import com.example.aeye.ui.screens.LogmarTestScreen
import com.example.aeye.viewmodel.AuthViewModel
import com.example.aeye.ui.screens.MainScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.aeye.ui.screens.SnellenTestScreen
import com.example.aeye.viewmodel.CalibrationViewModel
import com.example.aeye.viewmodel.CalibrationViewModelFactory
import com.example.aeye.viewmodel.ResultsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.aeye.viewmodel.ResultsViewModelFactory

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val repo = Firestore(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )

    val navController = rememberNavController()
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null) "home"
        else "login"
    val calibrationViewModel: CalibrationViewModel =
        viewModel(
            factory = CalibrationViewModelFactory(
                LocalContext.current.applicationContext
            )
        )
    val repository = Firestore(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )
    val resultsViewModel: ResultsViewModel = viewModel(
        factory = ResultsViewModelFactory(repository)
    )

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginPage(navController, authViewModel)
        }
        composable("signup") {
            SignupPage(navController, authViewModel)
        }
        composable("home") {
            MainScreen(
                navController = navController,
                resultsViewModel = resultsViewModel,
                calibrationViewModel = calibrationViewModel
            )
        }
        composable("snellen") {
            SnellenTestScreen(navController)
        }
        composable("calibration") {
            CalibrationScreen(
                navController = navController,
                calibrationViewModel = calibrationViewModel
            )
        }
        composable("logmar") {
            LogmarTestScreen(
                navController = navController,
                calibrationViewModel = calibrationViewModel,
                resultsViewModel = resultsViewModel
            )
        }
    }
}