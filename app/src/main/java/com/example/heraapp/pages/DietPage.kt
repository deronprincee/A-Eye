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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.heraapp.DietLog
import com.example.heraapp.ui.theme.AEyeBackground
import com.example.heraapp.ui.theme.AEyeBottomBar
import com.example.heraapp.ui.theme.AEyeTopBar
import com.example.heraapp.ui.theme.handleBottomNavSelection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DietPage(navController: NavController) {
    // Initializes firebase and context
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    // Added UI state variables
    var selectedItem by remember { mutableStateOf("") }
    var dietLogs by remember { mutableStateOf(listOf<DietLog>()) }
    var food by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var selectedTime by remember { mutableStateOf("Select Time") }
    var mealType by remember { mutableStateOf("Lunch") }
    var isLoading by remember { mutableStateOf(true) }
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editingDocId by remember { mutableStateOf("") }

    // Fetches and sort's user diet logs by date and time from Firestore
    fun loadLogs() {
        db.collection("users").document(userId).collection("diets")
            .get()
            .addOnSuccessListener { documents ->
                dietLogs = documents.mapNotNull { doc ->
                    doc.toObject(DietLog::class.java).copy(id = doc.id)
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
        topBar = { AEyeTopBar() },
        bottomBar = {
            AEyeBottomBar(selectedItem = selectedItem) { newItem ->
                selectedItem = newItem
                handleBottomNavSelection(navController, newItem)
            }
        }
    ) { innerPadding ->
        AEyeBackground {
            DietContent(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(24.dp),
                food = food,
                onFoodChange = { food = it },
                notes = notes,
                onNotesChange = { notes = it },
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                selectedTime = selectedTime,
                onTimeChange = { selectedTime = it },
                mealType = mealType,
                onMealTypeChange = { mealType = it },
                logs = dietLogs,
                isLoading = isLoading,
                isEditing = isEditing,
                // Populates UI with selected log for editing
                onEdit = { log ->
                    food = log.food
                    notes = log.notes
                    selectedDate = log.date
                    selectedTime = log.time
                    editingDocId = log.id.orEmpty()
                    isEditing = true
                },
                // Option to delete the log entry from Firestore database
                onDelete = { log ->
                    log.id?.let {
                        db.collection("users").document(userId)
                            .collection("diets").document(it).delete()
                            .addOnSuccessListener { loadLogs() }
                    }
                },
                // Validates for time before saving
                onSave = onSave@{
                    if (selectedTime == "Select Time") {
                        Toast.makeText(context, "Please select a valid time for your meal.", Toast.LENGTH_SHORT).show()
                        return@onSave
                    }
                    // Creates DietLog instance
                    val log = DietLog(
                        food = food.trim(),
                        notes = notes.trim(),
                        date = selectedDate,
                        time = selectedTime,
                        mealType = mealType
                    )
                    // Updates an existing diet log in Firestore, reloads logs and resets the form
                    if (isEditing && editingDocId.isNotEmpty()) {
                        db.collection("users").document(userId).collection("diets")
                            .document(editingDocId)
                            .set(log)
                            .addOnSuccessListener {
                                loadLogs()
                                food = ""
                                notes = ""
                                selectedDate = getCurrentDate()
                                selectedTime = "Select Time"
                                mealType = "Lunch"
                                isEditing = false
                                editingDocId = ""
                            }
                    } else {
                        db.collection("users").document(userId)
                            .collection("diets")
                            .add(log)
                            .addOnSuccessListener {
                                // Adds a new diet log to Firestore, reloads logs and resets the form
                                loadLogs()
                                food = ""
                                notes = ""
                                selectedDate = getCurrentDate()
                                selectedTime = "Select Time"
                                mealType = "Lunch"
                            }
                    }
                },
                // Form is reset to default values when edit is canceled
                onCancelEdit = {
                    food = ""
                    notes = ""
                    selectedDate = getCurrentDate()
                    selectedTime = "Select Time"
                    mealType = "Lunch"
                    isEditing = false
                    editingDocId = ""
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

// ExerciseContent renders the form and list of exercise logs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietContent(
    modifier: Modifier = Modifier,
    food: String,
    onFoodChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    selectedTime: String,
    onTimeChange: (String) -> Unit,
    mealType: String,
    onMealTypeChange: (String) -> Unit,
    logs: List<DietLog>,
    isLoading: Boolean,
    isEditing: Boolean,
    onEdit: (DietLog) -> Unit,
    onDelete: (DietLog) -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    showDatePicker: MutableState<Boolean>,
    showTimePicker: MutableState<Boolean>
) {
    val mealOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    var expanded by remember { mutableStateOf(false) }

    // Introductory text
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Hi there!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.DarkGray
        )
        Text(
            text = "What's on your plate? Share your delicious meals with us!",
            color = Color.DarkGray
        )

        // UI for meal details + optional notes
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
                    "Meal Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("What did you eat?", color = Color.DarkGray, fontSize = 14.sp)
                OutlinedTextField(
                    value = food,
                    onValueChange = onFoodChange,
                    label = { Text("Food / Meal") },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(62.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF8F8F8),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFFF89AAC)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Any notes about the meal?", color = Color.DarkGray, fontSize = 14.sp)
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes (optional)") },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(62.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFFF8F8F8),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFFF89AAC)
                    )
                )
            }
        }

        // UI for meal timing + meal type
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xA9DFF6FF), Color(0xFFF89AAC)),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 500f)
                    ),
                    shape = RoundedCornerShape(25.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Meal Timing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "When did you have this meal and what type was it?",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showDatePicker.value = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                    shape = RoundedCornerShape(50)
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
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                    shape = RoundedCornerShape(50)
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
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8F8F8)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = "Meal Type",
                            tint = Color(0xFFF89AAC)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Meal Type: $mealType", color = Color.DarkGray)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        mealOptions.forEach { option ->
                            DropdownMenuItem(onClick = {
                                onMealTypeChange(option)
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
                modifier = Modifier.weight(1f).height(48.dp),
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
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("Cancel")
                }
            }

        }

        Spacer(modifier = Modifier.height(5.dp))

        // Show previous logs of diet info
        Text(
            "Your Diet Logs",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFD6336C)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFF6A9B2))
        } else if (logs.isEmpty()) {
            Text("No diet entries logged yet.", color = Color.Gray)
        } else {
            logs.forEach { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🍽 Meal: ${log.food}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD6336C)
                        )
                        if (log.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📝 Notes: ${log.notes}")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📅 Date/Time: ${log.date} at ${log.time}")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🍴 Meal Type: ${log.mealType}")

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