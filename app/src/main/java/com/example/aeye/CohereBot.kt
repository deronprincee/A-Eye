package com.example.aeye


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

// Class that integrates Hera Chatbot with Cohere's text generation API
class CohereBot(private val apiKey: String) : Chatbot {

    // Sends a user message to Cohere and returns the generated response
    override suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO){
        val client = OkHttpClient()

        // Constructs JSON request for Cohere's generate endpoint
        val json = JSONObject().apply {
            put("model", "command-r-plus") // model used
            put("prompt", userMessage) // prompt = user's message
            put("max_tokens", 10000) // Max number of tokens in Chatbot's response
        }

        val mediaType = "application/json".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        // Added the HTTP POST request with authentication headers
        val request = Request.Builder()
            .url("https://api.cohere.ai/v1/generate")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        // Attempt to send the request and parse the response
        return@withContext try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonResponse = JSONObject(responseBody)

            // Extracts the generated text from the response JSON
            jsonResponse.getJSONArray("generations")
                .getJSONObject(0)
                .getString("text")
                .trim()
        } catch (e: IOException) {
            // Added error message in case of request failure
            e.printStackTrace()
            "Sorry, I couldn't get a response right now."
        }
    }

    // Added required function for satisfying Chatbot's interface contract
    override fun updateUserData(symptoms: Symptoms, cycleData: CycleData) {
        // But remains empty since Cohere doesn't need the data
    }
}