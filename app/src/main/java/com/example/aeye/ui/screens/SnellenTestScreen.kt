package com.example.aeye.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aeye.data.model.snellenChart

@Composable
fun SnellenTestScreen(navController: NavController) {

    var currentRowIndex by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf("") }

    var testFinished by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val currentRow = snellenChart.getOrNull(currentRowIndex)
    val rowResults = remember { mutableStateListOf<Boolean>() }

    fun normalize(input: String): String {
        // Remove ALL whitespace and uppercase
        return input.replace("\\s+".toRegex(), "").uppercase()
    }

    fun submitAnswer() {
        if (currentRow == null) return

        val expected = normalize(currentRow.letters)
        val typed = normalize(answer)
        val isCorrect = typed.isNotBlank() && typed == expected
        rowResults.add(isCorrect)

        feedback = if (isCorrect) "Correct ✅" else "Incorrect ❌"
        answer = ""

        if (currentRowIndex < snellenChart.lastIndex) {
            currentRowIndex++
        } else {
            // User reached the end correctly
            testFinished = true
        }
    }

    val bestCorrectIndex = rowResults.indexOfLast { it }
    val scoreText = "Score: ${rowResults.count { it }} / ${snellenChart.size} correct"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        // ---------------- TOP SECTION ----------------
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Snellen Visual Acuity Test",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cover one eye. Type the letters you see (no spaces).",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ---------------- CENTER LETTERS ----------------
        if (!testFinished && currentRow != null) {

            Text(
                text = currentRow.letters,
                fontSize = currentRow.textSizeSp.sp,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ---------------- BOTTOM INPUT + BUTTONS ----------------
        if (!testFinished) {

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Type the letters you see") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { submitAnswer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text("Submit")
                    }

                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text("Exit")
                    }
                }
            }
        }

        // ---------------- FINISHED STATE ----------------
        if (testFinished) {

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Test Finished",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = scoreText,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            rowResults.clear()
                            currentRowIndex = 0
                            answer = ""
                            feedback = null
                            testFinished = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text("Restart")
                    }

                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}