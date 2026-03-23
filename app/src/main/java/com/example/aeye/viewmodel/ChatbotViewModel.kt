package com.example.aeye.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.aeye.chat.ChatMessage
import ChatbotResponseMapper
import com.example.aeye.chat.Sender
import com.example.aeye.data.model.TestResult

class ChatbotViewModel : ViewModel() {

    val messages = mutableStateListOf<ChatMessage>()

    init {
        messages.add(
            ChatMessage(
                text = "Hello, I’m your A-Eye assistant. I can help explain your latest result. Ask me something like 'What does my result mean?' or 'Do I need to visit a clinic?'",
                sender = Sender.BOT
            )
        )
    }

    fun sendMessage(userInput: String, latestResult: TestResult?) {
        if (userInput.isBlank()) return

        messages.add(ChatMessage(userInput, Sender.USER))

        val reply = buildReply(userInput, latestResult)
        messages.add(ChatMessage(reply, Sender.BOT))
    }

    private fun buildReply(userInput: String, latestResult: TestResult?): String {
        val input = userInput.lowercase()

        if (latestResult == null) {
            return "I cannot analyse your vision yet. Please complete a LogMAR test first."
        }

        val totalCorrect = latestResult.totalCorrectLetters ?: 0
        val totalLetters = latestResult.totalLetters ?: 0
        val resultSummary = "Your latest result was $totalCorrect / $totalLetters correct letters."

        return when {
            input.contains("result") ||
                    input.contains("mean") ||
                    input.contains("vision") ||
                    input.contains("eyesight") -> {
                "$resultSummary\n\n${ChatbotResponseMapper.responseForTotalCorrect(totalCorrect)}"
            }

            input.contains("clinic") ||
                    input.contains("doctor") ||
                    input.contains("optician") ||
                    input.contains("hospital") -> {
                "$resultSummary\n\n${ChatbotResponseMapper.responseForTotalCorrect(totalCorrect)}"
            }

            input.contains("hello") ||
                    input.contains("hi") -> {
                "Hello. Ask me about your latest result, for example: 'What does my result mean?'"
            }

            else -> {
                "I can help explain your latest test result. Ask me something like 'What does my result mean?' or 'Do I need to visit a clinic?'"
            }
        }
    }
}