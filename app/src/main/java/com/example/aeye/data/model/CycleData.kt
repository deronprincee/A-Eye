package com.example.aeye.data.model

data class CycleData(
    val userId: String,   // Firebase user ID
    val cycleStartDate: String,  // Start date of the cycle (dd/MM/yyyy)
    val cycleEndDate: String, // End date of the cycle (dd/MM/yyyy)
    val symptoms: Symptoms  // Associated symptom data for the cycle
)