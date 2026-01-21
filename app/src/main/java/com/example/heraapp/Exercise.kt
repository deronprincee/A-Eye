package com.example.heraapp

data class ExerciseLog(
    val id: String? = null,      // Firestore document ID
    val activity: String = "",   // Name of the exercise (e.g., Running)
    val duration: String = "",   // Duration (e.g., "30 mins")
    val date: String = "",       // Date of exercise (dd/MM/yyyy)
    val time: String = "",       // Time of exercise (HH:mm)
    val intensity: String = ""   // Intensity level (Light, Moderate, Intense)
)