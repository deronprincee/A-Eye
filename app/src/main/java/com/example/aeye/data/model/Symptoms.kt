package com.example.aeye.data.model

data class Symptoms(
    val mood: String,         // User's emotional state
    val energy: String,       // Energy level (e.g., "High", "Low")
    val painLevel: Int,       // Pain level (0–10)
    val hydration: Int        // Hydration score or amount
)