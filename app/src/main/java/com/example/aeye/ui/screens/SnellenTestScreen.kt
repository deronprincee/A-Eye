package com.example.aeye.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aeye.data.model.snellenChart

@Composable
fun SnellenTestScreen(navController: NavController) {

    var currentRowIndex by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf("") }

    var lastCorrectIndex by remember { mutableStateOf(-1) }
    var testFinished by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val currentRow = snellenChart.getOrNull(currentRowIndex)

    fun normalize(input: String): String {
        // Remove ALL whitespace and uppercase
        return input.replace("\\s+".toRegex(), "").uppercase()
    }

    fun submitAnswer() {
        if (currentRow == null) return

        val expected = normalize(currentRow.letters)
        val typed = normalize(answer)

        // Blank OR wrong => user can't see this row => finish test
        if (typed.isBlank() || typed != expected) {
            testFinished = true
            feedback = "Incorrect / Blank → counted as not visible."
            return
        }

        // Correct => move to next row
        lastCorrectIndex = currentRowIndex
        feedback = "Correct ✅"
        answer = ""

        if (currentRowIndex < snellenChart.lastIndex) {
            currentRowIndex++
        } else {
            // User reached the end correctly
            testFinished = true
        }
    }

    val resultText = when {
        lastCorrectIndex >= 0 -> {
            val bestRow = snellenChart[lastCorrectIndex]
            "Best readable line: ${bestRow.acuityLabel}"
        }
        else -> "Best readable line: Not determined (couldn’t pass the first line)."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Snellen Visual Acuity Test",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Short, clear instructions under the title
        Text(
            text = "Cover one eye. Type the letters you see (spaces don’t matter).\n" +
                    "If you leave it blank or type wrong, the test will stop.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (!testFinished && currentRow != null) {

            // Letters row
            Text(
                text = currentRow.letters,
                fontSize = currentRow.textSizeSp.sp,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Answer input
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Type what you see") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Feedback (optional)
            feedback?.let {
                Text(text = it, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { submitAnswer() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Submit")
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Exit")
                }
            }

        } else {
            // Finished state
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Test Finished", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = resultText, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // Restart
                        currentRowIndex = 0
                        lastCorrectIndex = -1
                        answer = ""
                        feedback = null
                        testFinished = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restart")
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            }
        }
    }
}