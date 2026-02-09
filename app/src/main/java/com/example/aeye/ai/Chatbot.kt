package com.example.aeye.ai

import com.example.aeye.data.model.CycleData
import com.example.aeye.data.model.Symptoms

interface Chatbot {
    // Processes a user message and returns a chatbot reply
    suspend fun sendMessage(userMessage: String) : String
    // Updates the chatbot with current user symptoms and cycle data
    fun updateUserData(symptoms: Symptoms, cycleData: CycleData)
}