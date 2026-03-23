package com.example.aeye.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aeye.chat.Sender
import com.example.aeye.viewmodel.ChatbotViewModel
import com.example.aeye.viewmodel.ResultsViewModel

@Composable
fun ChatbotScreen(
    resultsViewModel: ResultsViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val results by resultsViewModel.results.collectAsState()
    val latestResult = results.firstOrNull()

    val chatbotViewModel = remember { ChatbotViewModel() }
    var userInput by remember { mutableStateOf("") }

    val pageGrey = Color(0xFFEDEDED)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageGrey)
            .padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp
            )
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            text = "A-Eye Assistant",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatbotViewModel.messages) { message ->
                ChatBubble(
                    text = message.text,
                    isBot = message.sender == Sender.BOT
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF2F2F2)
                )
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = {
                        Text(
                            "Ask about your result...",
                            color = Color.DarkGray
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                        disabledContainerColor = MaterialTheme.colorScheme.tertiary,

                        focusedTextColor = Color.DarkGray,
                        unfocusedTextColor = Color.DarkGray,

                        cursorColor = Color.DarkGray,

                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }
            Button(
                onClick = {
                    chatbotViewModel.sendMessage(userInput, latestResult)
                    userInput = ""
                },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isBot: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.82f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBot) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(14.dp),
                color = if (isBot) {
                    Color.Black
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}