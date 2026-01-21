package com.example.heraapp

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.heraapp.ui.theme.HeraBackground
import com.example.heraapp.ui.theme.HeraBottomBar
import com.example.heraapp.ui.theme.HeraTopBar
import com.example.heraapp.ui.theme.handleBottomNavSelection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CycleLogListScreen(navController: NavController) {
    // Initialize User ID and database instance
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    // Added state variables for logs and loading status
    var logs by remember { mutableStateOf(listOf<CycleLog>()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf("Cycle") }

    // Fetches logs from Firestore on first render
    LaunchedEffect(Unit) {
        db.collection("menstrual_logs")
            .document(userId)
            .collection("cycles")
            .get()
            .addOnSuccessListener { documents ->
                logs = documents.mapNotNull { doc ->
                    doc.toObject(CycleLog::class.java).copy(id = doc.id)
                }.filter {it.startDate.isNotBlank()}
                .sortedByDescending {
                    try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.UK).parse(it.startDate)
                    } catch (e: Exception) {
                        null
                    }
                }
                isLoading = false
            }
            .addOnFailureListener {
                println("Error fetching logs: ${it.message}")
                isLoading = false
            }
    }

    //UI for top and bottom bar
    Scaffold(
        topBar = { HeraTopBar() },
        bottomBar = {
            HeraBottomBar(selectedItem = selectedItem) { newItem ->
                selectedItem = newItem
                handleBottomNavSelection(navController, newItem)
            }
        }
    ) { innerPadding ->
        HeraBackground {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                // Header text
                Text(
                    text = "Your Cycle Logs",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD6336C)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button to navigate to form
                Button(
                    onClick = { navController.navigate("cyclelogform") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6A9B2))
                ) {
                    Text("Log a New Cycle", color = Color.White)
                }

                Spacer(modifier = Modifier.height(5.dp))

                if (isLoading) {
                    // Show loading spinner
                    CircularProgressIndicator(color = Color(0xFFF48FB1))
                } else {
                    Column {
                        // Lists cycle logs once loaded
                        logs.forEach {log ->
                            CycleLogItem(
                                log = log,
                                // Navigates to CycleLogFormScreen with ID
                                onEdit = {
                                    navController.navigate("cyclelogform?docId=${log.id}")
                                },
                                // Deletes log from Firebase
                                onDelete = {
                                    db.collection("menstrual_logs")
                                        .document(userId)
                                        .collection("cycles")
                                        .document(log.id)
                                        .delete()
                                        .addOnSuccessListener {
                                            logs = logs.filterNot { it.id == log.id }
                                        }
                                        .addOnFailureListener {
                                            println("Error deleting log: ${it.message}")
                                        }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Card UI for displaying a single cycle log with edit/delete buttons
@Composable
fun CycleLogItem(log: CycleLog, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🩸 ${log.startDate} – ${log.endDate}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD6336C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("😊 Mood: ${log.mood}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("⚡ Energy: ${log.energyLevel}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("💧 Hydration: ${log.hydration} litres")
            Spacer(modifier = Modifier.height(4.dp))
            Text("😖 Pain: ${log.painLevel}")
            Spacer(modifier = Modifier.height(4.dp))
            if (log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("📝 Notes: ${log.notes}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8BBD0))
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}

