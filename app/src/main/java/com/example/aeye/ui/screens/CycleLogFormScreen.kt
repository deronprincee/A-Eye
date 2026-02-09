package com.example.aeye.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.aeye.data.model.CycleLog
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import java.util.Calendar
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.aeye.ui.components.AEyeTopBar

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleLogFormScreen(
    navController: NavController,
    docId: String?,
    modifier: Modifier = Modifier
) {
    // Initialized firebase, context and calendar
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Added UI state variables
    var selectedItem by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("Select Start Date") }
    var endDate by remember { mutableStateOf("Select End Date") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var mood by remember { mutableStateOf("Normal") }
    var painLevel by remember { mutableStateOf(5f) }
    var energySliderValue by remember { mutableStateOf(5f) }
    var energyLevel by remember { mutableStateOf("Medium") }
    var hydrationLevel by remember { mutableStateOf("") }
    var extraNotes by remember { mutableStateOf("") }
    val moods = listOf("Happy", "Normal", "Angry", "Sad")
    val moodEmojis = mapOf("Happy" to "😊", "Normal" to "🙂", "Angry" to "😠", "Sad" to "😢")

    // Load existing log data if editing
    LaunchedEffect(docId) {
        if (!docId.isNullOrBlank()) {
            db.collection("menstrual_logs").document(userId)
                .collection("cycles").document(docId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val log = snapshot.toObject(CycleLog::class.java)
                    if (log != null) {
                        startDate = log.startDate
                        endDate = log.endDate
                        mood = log.mood
                        painLevel = log.painLevel.toFloat()
                        energyLevel = log.energyLevel
                        hydrationLevel = log.hydration.toString()
                        extraNotes = log.notes
                        energySliderValue = when (log.energyLevel) {
                            "Low" -> 2f
                            "Medium" -> 5f
                            "High" -> 9f
                            else -> 5f
                        }
                    }
                }
        }
    }

    // Show date pickers when toggled
    if (showStartDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                startDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                showStartDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                endDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                showEndDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Added scaffold layout with top and bottom UI bars
    Scaffold(
        topBar = { AEyeTopBar(onSettingsClick = { /* nav later */  }) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Hi there!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.DarkGray
            )
            Text(
                "Any symptoms today? We want to hear everything!",
                color = Color.DarkGray)

            // UI for selecting and displaying start/end cycle dates
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xA9DFF6FF), Color(0xFFF89AAC)),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 500f)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Cycle Dates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        "When did your cycle start and end?",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showStartDatePicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(50),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Start Date", tint = Color(0xFFF89AAC))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start: $startDate", color = Color.DarkGray)
                        }

                        Button(
                            onClick = { showEndDatePicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(50),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "End Date", tint = Color(0xFFF89AAC))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End: $endDate", color = Color.DarkGray)
                        }
                    }
                }
            }

            // UI for mood, energy and pain levels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xA9DFF6FF), Color(0xFFF89AAC)),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 1800f)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Mood",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("What is your mood right now?", color = Color.DarkGray)

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        moods.forEach { moodOption ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(moodEmojis[moodOption] ?: "", fontSize = 24.sp)
                                RadioButton(
                                    selected = mood == moodOption,
                                    onClick = { mood = moodOption })
                                Text(moodOption, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Energy Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("What is your energy level?", color = Color.DarkGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Low", color = Color(0xFFDA627D))
                        Text("Medium", color = Color(0xFFDA627D))
                        Text("High", color = Color(0xFFDA627D))
                    }
                    Slider(
                        value = energySliderValue,
                        onValueChange = {
                            energySliderValue = it
                            energyLevel = when {
                                it < 3.5 -> "Low"
                                it < 7 -> "Medium"
                                else -> "High"
                            }
                        },
                        valueRange = 0f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFDA627D),
                            inactiveTrackColor = Color(0xFFF8C1CA)
                        )
                    )
                    Text("Energy Level: $energyLevel", color = Color(0xFFDA627D))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Pain Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "On a scale of 1 to 10, how much pain do you feel?",
                        color = Color.DarkGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1", color = Color(0xFFDA627D))
                        Text("10", color = Color(0xFFDA627D))
                    }
                    Slider(
                        value = painLevel,
                        onValueChange = { painLevel = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFDA627D),
                            inactiveTrackColor = Color(0xFFF8C1CA)
                        )
                    )
                    Text("Pain Level: ${painLevel.toInt()}", color = Color(0xFFDA627D))
                }
            }

            //UI for hydration and notes input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xA9DFF6FF), Color(0xFFF89AAC)),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 1000f)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Hydration Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("How much water did you drink today?", color = Color.DarkGray)
                    OutlinedTextField(
                        value = hydrationLevel,
                        onValueChange = { hydrationLevel = it },
                        label = { Text("/ litres") },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFFF8F8F8),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFF89AAC)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Additional Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Tell us anything else you'd like to log today", color = Color.DarkGray)

                    OutlinedTextField(
                        value = extraNotes,
                        onValueChange = { extraNotes = it },
                        label = { Text("e.g., cramps, cravings, mood shifts...") },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp),
                        maxLines = 4,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFFF8F8F8),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFF89AAC)
                        )
                    )
                }
            }

            //UI for cancel and Save buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6A9B2)), // Hera pink
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Cancel", color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (startDate == "Select Start Date" || endDate == "Select End Date") {
                            Toast.makeText(context, "Please select valid start and end dates", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Construct CycleLog object and save to Firestore (Firebase)
                        val cycleLog = CycleLog(
                            startDate = startDate,
                            endDate = endDate,
                            mood = mood,
                            painLevel = painLevel.toInt(),
                            energyLevel = energyLevel,
                            hydration = hydrationLevel.toDoubleOrNull() ?: 0.0,
                            notes = extraNotes
                        )

                        val cycleRef = db.collection("menstrual_logs")
                            .document(userId)
                            .collection("cycles")

                        // Update existing log
                        if (!docId.isNullOrBlank()) {
                            cycleRef.document(docId).set(cycleLog)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Cycle log updated!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                        }

                        // Add new log
                        else {
                            cycleRef.add(cycleLog)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Cycle log saved!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6A9B2)), // Hera pink
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Save", color = Color.DarkGray)
                }
            }
        }
    }
}