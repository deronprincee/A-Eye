package com.example.aeye

data class MedicationLog(
    val id: String? = null,        // Firestore document ID
    val name: String = "",         // Medication name
    val dosage: String = "",       // Dosage amount (e.g., "500mg")
    val date: String = "",         // Date to take the medication
    val time: String = "",         // Time to take the medication (optional)
    val frequency: String = "",    // Frequency (e.g., "Once a day")
    val reminderSet: Boolean = false // Whether a reminder is active
)