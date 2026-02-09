package com.example.aeye.data.model

data class CycleLog(
    val id: String = "",                  // Firestore document ID
    val startDate: String = "",           // Start date of the cycle
    val endDate: String = "",             // End date of the cycle
    val symptoms: List<String> = listOf(),// List of recorded symptoms (text)
    val mood: String = "",                // User's mood during the cycle
    val energyLevel: String = "",         // Energy level reported
    val painLevel: Int = 0,               // Pain level (0–10 scale)
    val hydration: Double = 0.0,          // Hydration level (0–100 or ml)
    val notes: String = ""                // Additional notes
)