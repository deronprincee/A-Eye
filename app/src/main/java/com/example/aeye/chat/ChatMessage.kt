package com.example.aeye.chat

enum class Sender {
    BOT, USER
}

data class ChatMessage(
    val text: String,
    val sender: Sender
)