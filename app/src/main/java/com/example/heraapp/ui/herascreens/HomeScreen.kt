package com.example.heraapp.ui.herascreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Logout
import com.example.heraapp.AuthViewModel
import androidx.compose.runtime.*
import com.example.heraapp.ui.theme.HeraBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



@Composable
fun HomeScreen( modifier: Modifier = Modifier, authViewModel: AuthViewModel, navController: NavController) {
    // Fetch user Id and add name state
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var name by remember { mutableStateOf("User") }

    // Fetch user name from Firestore
    LaunchedEffect(userId) {
        userId?.let {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    name = document.getString("name") ?: "User"
                }
        }
    }

    HeraBackground {
        Column(
            modifier = modifier.padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Welcome back, $name 🌸",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF14213D),
                modifier = Modifier.padding(top = 80.dp, bottom = 40.dp)
            )

            // ✨ Quick Access Buttons
            HeraFeatureButton(
                text = "Log a Cycle",
                icon = Icons.Default.EditCalendar,
                onClick = {
                    navController.navigate("cyclelogform") // Route to your cycle logging form
                }
            )
            // 🌸 Exercise Button
            HeraFeatureButton(
                text = "Exercise",
                icon = Icons.Default.FitnessCenter,
                onClick = { navController.navigate("exercise") }
            )

            // 🥗 Diet Button
            HeraFeatureButton(
                text = "Diet",
                icon = Icons.Default.Spa,
                onClick = { navController.navigate("diet") }
            )

            // 💊 Medication Button
            HeraFeatureButton(
                text = "Medication",
                icon = Icons.Default.Medication,
                onClick = { navController.navigate("medication") }
            )

            // 🔓 Sign Out Button
            HeraFeatureButton(
                text = "Sign Out",
                icon = Icons.Default.Logout,
                onClick = {
                    authViewModel.logoutUser()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                        this.launchSingleTop = true
                    }
                }
            )
        }
    }
}

// Reusable feature button composable with icon + label
@Composable
fun HeraFeatureButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6A9B2)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White)
    }

}
