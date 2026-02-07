package com.example.aeye

// Added a class that delegates messages between a specialized bot (Hera) and a general-purpose bot (CohereAI)
class HybridChatbot(
    val heraBot: Chatbot, // Handles cycle, diet, pain, etc.
    val generalBot: Chatbot // Handles general queries
) : Chatbot {

    // Added a function for sending a user message and decides which bot should handle the response
    override suspend fun sendMessage(userMessage: String): String {
        // Tries getting a response from HeraBot first
        val heraResponse = heraBot.sendMessage(userMessage)

        // If HeraBot gives a vague or uncertain response, fall back to the general bot
        return if (heraResponse.contains("I'm not sure", ignoreCase = true) ||
            heraResponse.contains("learning", ignoreCase = true)
        ) {
            generalBot.sendMessage(userMessage)
        } else {
            heraResponse
        }
    }

    // This function passes user symptom and cycle data to the HeraBot so it responds with suggestions (from HeraChatBot file)
    override fun updateUserData(symptoms: Symptoms, cycleData: CycleData) {
        heraBot.updateUserData(symptoms, cycleData)
    }
}