package com.example.aeye

import java.text.SimpleDateFormat
import java.util.*

// Added class for Chatbot handling of user queries via CycleLog data
class HeraChatbot : Chatbot {
    private var symptoms: Symptoms? = null // Stores latest user symptoms
    private var cycleData: CycleData? = null // Stores current cycle metadata
    private var cycleLogs: List<CycleLog> = listOf() // Stores historical cycle data

    // Function for processing user messages and return the chatbot's response
    override suspend fun sendMessage(userMessage: String): String {
        val message = userMessage.lowercase()

        // list of keywords for the chatbot to generate message for each predefined category
        val response = when {
            listOf("eat", "food", "meal", "diet", "hungry").any { it in message } -> generateDietRecommendation()
            listOf("pain", "cramps", "hurt", "ache").any { it in message } -> generatePainReliefTips()
            listOf("exercise", "workout", "activity").any { it in message } -> generateExerciseTips()
            listOf("period", "cycle", "menstruation", "next period").any { it in message } -> predictNextPeriod()
            listOf("help", "support", "what can you do").any { it in message } -> listCapabilities()
            else -> "I'm not sure how to help with that yet, but I'm learning! Try asking about diet, pain, exercise, or your cycle."
        }

        println("User asked: \"$userMessage\"")
        println("Matched response: \"$response\"")

        return response
    }

    // Updates the chatbot with the user's current symptoms and cycle data
    override fun updateUserData(symptoms: Symptoms, cycleData: CycleData) {
        this.symptoms = symptoms
        this.cycleData = cycleData
    }

    // Updates the chatbot with the user's full cycle history
    fun updateCycleHistory(logs: List<CycleLog>) {
        this.cycleLogs = logs
    }

    // Generates a diet recommendation based on current mood, energy, and hydration
    private fun generateDietRecommendation(): String {
        val mood = symptoms?.mood?.trim()?.lowercase()
        val energy = symptoms?.energy?.trim()?.lowercase()
        val hydration = symptoms?.hydration ?: 0

        return when {
            mood == "happy" || energy == "high" -> "You're feeling $mood and have $energy energy! Consider a quinoa salad with citrus fruits."
            mood == "sad" || energy == "low" -> "You might need comfort. Try warm soup, whole grains, and dark chocolate."
            hydration < 1 -> "You're a bit dehydrated—start with water, then enjoy juicy fruits or a smoothie."
            else -> "A balanced meal with proteins, fiber, and healthy fats will support you today."
        }
    }

    // Suggests pain relief tips based on the user's reported pain level
    private fun generatePainReliefTips(): String {
        val pain = symptoms?.painLevel ?: 0

        return when (pain) {
            in 1..3 -> "Your pain is low. A light walk or stretching can help keep you feeling good."
            in 4..6 -> "Moderate pain today. A warm bath, herbal tea, and a heating pad might help."
            in 7..10 -> "High pain detected. Please rest, consider taking prescribed relief, and consult a doctor if needed."
            else -> "Let me know more about how you're feeling, and I’ll suggest something soothing."
        }
    }

    // Provides exercise suggestions depending on the user's energy and pain level
    private fun generateExerciseTips(): String {
        val energy = symptoms?.energy ?: "medium"
        val pain = symptoms?.painLevel ?: 0

        return when {
            energy == "high" && pain <= 3 -> "You're feeling great! Try light cardio or yoga to stay active."
            energy == "medium" && pain in 4..6 -> "Moderate pain today, gentle stretching or walking might help."
            energy == "low" || pain >= 7 -> "Rest is best today. Focus on relaxation and self-care."
            else -> "Listen to your body, even a short walk can make a difference."
        }
    }

    // Predicts the user's next period date based on average cycle length
    fun predictNextPeriod(): String {
        if (cycleLogs.size < 2) return "I need more data to predict your next period. Please keep logging your cycles!"

        // Calculate cycle lengths between consecutive logs
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.UK)
        val cycleLengths = cycleLogs
            .zipWithNext()
            .mapNotNull { (prev, next) ->
                val prevDate = sdf.parse(prev.startDate)
                val nextDate = sdf.parse(next.startDate)
                if (prevDate != null && nextDate != null) {
                    ((nextDate.time - prevDate.time) / (1000 * 60 * 60 * 24)).toInt()
                } else null
            }

        // Use average cycle length to predict next start date
        val avgCycleLength = cycleLengths.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 28
        val lastCycleStart = sdf.parse(cycleLogs.last().startDate)
        val predictedDate = Calendar.getInstance().apply {
            time = lastCycleStart!!
            add(Calendar.DAY_OF_YEAR, avgCycleLength)
        }

        return "Based on your cycle history, your next period might start around ${sdf.format(predictedDate.time)}. It's a good idea to keep track of how you're feeling as the date approaches—watch for changes in mood, energy, or cravings. Let me know anytime if you need tips on easing symptoms. You've got this 🌸"
    }

    // Function for Chatbot to list its capabilities
    private fun listCapabilities(): String {
        return "I can help with:\n- Diet tips\n- Pain relief suggestions\n- Exercise ideas\n- Predicting your next period.\n\nTry asking:\n- What should I eat?\n- How to reduce pain?\n- When's my next period?\n- What exercise should I do?"
    }
}