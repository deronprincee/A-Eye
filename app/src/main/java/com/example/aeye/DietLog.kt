package com.example.aeye

data class DietLog(
    val id: String? = null,      // Firestore document ID
    val food: String = "",       // Food or meal description
    val notes: String = "",      // Optional user notes
    val date: String = "",       // Date of meal (dd/MM/yyyy)
    val time: String = "",       // Time of meal (HH:mm)
    val mealType: String = ""    // Type of meal (e.g., Breakfast, Lunch)
)