package com.example.heraapp

interface Chatbot {
    // Processes a user message and returns a chatbot reply
    suspend fun sendMessage(userMessage: String) : String
    // Updates the chatbot with current user symptoms and cycle data
    fun updateUserData(symptoms: Symptoms, cycleData: CycleData)
}