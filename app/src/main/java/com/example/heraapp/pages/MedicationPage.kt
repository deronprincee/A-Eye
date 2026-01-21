package com.example.heraapp.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavController
import com.example.heraapp.MedicationLog
import com.example.heraapp.ui.theme.HeraBackground
import com.example.heraapp.ui.theme.HeraBottomBar
import com.example.heraapp.ui.theme.HeraTopBar
import com.example.heraapp.ui.theme.handleBottomNavSelection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicationPage(navController: NavController) {
    // Initializing firebase and context
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    // Adding UI state variables
    var selectedItem by remember { mutableStateOf("") }
    var medicationLog by remember { mutableStateOf(listOf<MedicationLog>()) }
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var selectedTime by remember { mutableStateOf("Select Time") }
    var frequency by remember { mutableStateOf("Once a day") }
    var isLoading by remember { mutableStateOf(true) }
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editingDocId by remember { mutableStateOf("") }

    // Fetches and sort's user medication logs by date and time from Firestore
    fun loadLogs() {
        db.collection("users").document(userId).collection("medications")
            .get()
            .addOnSuccessListener { documents ->
                medicationLog = documents.mapNotNull { doc ->
                    doc.toObject(MedicationLog::class.java).copy(id = doc.id)
                }.filter { it.date.isNotBlank() }
                    .sortedByDescending {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK).parse("${it.date} ${it.time}")
                    }
            }
    }

    // Loads logs when screen is first composed
    LaunchedEffect(Unit) {
        loadLogs()
        isLoading = false
    }

    // Added scaffold layout with top and bottom UI bars
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
            MedicationContent(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(24.dp),
                name = name,
                onNameChange = { name = it },
                dosage = dosage,
                onDosageChange = { dosage = it },
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                selectedTime = selectedTime,
                onTimeChange = { selectedTime = it },
                frequency = frequency,
                onFrequencyChange = { frequency = it },
                medicationLog = medicationLog,
                isLoading = isLoading,
                isEditing = isEditing,
                onEdit = { log ->
                    // Populates UI with selected log for editing
                    name = log.name
                    dosage = log.dosage
                    selectedDate = log.date
                    selectedTime = log.time
                    frequency = log.frequency
                    editingDocId = log.id.orEmpty()
                    isEditing = true
                },
                // Option to delete the log entry from Firestore database
                onDelete = { log ->
                    log.id?.let {
                        db.collection("users").document(userId)
                            .collection("medications").document(it).delete()
                            .addOnSuccessListener { loadLogs() }
                    }
                },
                // Validates for time before saving
                onSave = onSave@{
                    if(selectedTime == "Select Time") {
                        Toast.makeText(context, "Please select a valid time for your medication.",Toast.LENGTH_SHORT).show()
                        return@onSave
                    }
                    // Creates MedicationLog instance
                    val log = MedicationLog(
                        name = name.trim(),
                        dosage = dosage.trim(),
                        date = selectedDate,
                        time = selectedTime,
                        frequency = frequency
                    )
                    // Updates an existing medication log in Firestore, reloads logs and resets the form
                    if (isEditing && editingDocId.isNotEmpty()) {
                        db.collection("users").document(userId).collection("medications")
                            .document(editingDocId)
                            .set(log)
                            .addOnSuccessListener {
                                loadLogs()
                                name = ""
                                dosage = ""
                                selectedDate = getCurrentDate()
                                selectedTime = "Select Time"
                                frequency = ""
                                isEditing = false
                                editingDocId = ""
                            }
                    } else {
                        // Adds a new medication log to Firestore, reloads logs and resets the form
                        db.collection("users").document(userId)
                            .collection("medications")
                            .add(log)
                            .addOnSuccessListener {
                                loadLogs()
                                name = ""
                                dosage = ""
                                selectedDate = getCurrentDate()
                                selectedTime = "Select Time"
                                frequency = ""
                            }
                    }
                },
                // Form is reset to default values when edit is canceled
                onCancelEdit = {
                    isEditing = false
                    editingDocId = ""
                    name = ""
                    dosage = ""
                    selectedDate = getCurrentDate()
                    selectedTime = "Select Time"
                    frequency = ""
                },
                showDatePicker = showDatePicker,
                showTimePicker = showTimePicker
            )
        }
    }

    // Displays the date and time picker dialog once user toggles them
    if (showDatePicker.value) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                showDatePicker.value = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showTimePicker.value) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            LocalContext.current,
            { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                showTimePicker.value = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}

// MedicationContent renders the form and list of medication logs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationContent(
    modifier: Modifier = Modifier,
    name: String,
    onNameChange: (String) -> Unit,
    dosage: String,
    onDosageChange: (String) -> Unit,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    medicationLog: List<MedicationLog>,
    isLoading: Boolean,
    isEditing: Boolean,
    onEdit: (MedicationLog) -> Unit,
    onDelete: (MedicationLog) -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    showDatePicker: MutableState<Boolean>,
    showTimePicker: MutableState<Boolean>
) {
    val frequencyOptions = listOf("Once a day", "Twice a day", "As needed")
    var expanded by remember { mutableStateOf(false) }

    // Introductory text
    Column(modifier = modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Hi there!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.DarkGray
        )
        Text(
            text = "Taking care of yourself is important. Log your medications here!",
            color = Color.DarkGray
        )

        // UI for medication details
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
                    "Medication Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "What is the name of your medication?",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Medication Name") },
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
                    "How much are you taking?",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = onDosageChange,
                    label = { Text("Dosage (e.g., 500mg)") },
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
            }
        }

        // UI for Date + Time + Frequency
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
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    "When and how often should you take this?",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showDatePicker.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = Color(0xFFF89AAC)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Date: $selectedDate", color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showTimePicker.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Select Time",
                        tint = Color(0xFFF89AAC)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Time: $selectedTime", color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = "Select Frequency",
                            tint = Color(0xFFF89AAC)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Frequency: $frequency", color = Color.DarkGray)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        frequencyOptions.forEach { option ->
                            DropdownMenuItem(onClick = {
                                onFrequencyChange(option)
                                expanded = false
                            }, text = { Text(option) })
                        }
                    }
                }
            }
        }

        // UI for editing mode
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditing) Color(
                        0xFF6C63FF
                    ) else Color(0xFFF6A9B2)
                )
            ) {
                Text(if (isEditing) "Update" else "Save", color = Color.White)
            }
            if (isEditing) {
                OutlinedButton(
                    onClick = onCancelEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("Cancel")
                }
            }

        }

        Spacer(modifier = Modifier.height(5.dp))

        // Show previous logs of medication info
        Text(
            "Your Medications",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFD6336C)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFF6A9B2))
        } else if (medicationLog.isEmpty()) {
            Text("No medications logged yet.", color = Color.Gray)
        } else {
            medicationLog.forEach { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1)),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "💊 Medication: ${log.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD6336C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📊 Dosage: ${log.dosage}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📅 Date/Time: ${log.date} at ${log.time}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🔄 Frequency: ${log.frequency}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {  onEdit(log) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8BBD0))
                            ) {
                                Text("Edit")
                            }
                            Button(
                                onClick = { onDelete(log) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        }
                    }

                }
            }
        }
    }
}
