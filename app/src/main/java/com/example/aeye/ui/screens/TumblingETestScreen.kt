package com.example.aeye.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.pow

// If you created the enum in a model file, import it instead
enum class EDirection { UP, RIGHT, DOWN, LEFT }

@Composable
fun TumblingETestScreen(navController: NavController) {

    // --- Test settings (simple + stable) ---
    // logMAR levels: bigger -> smaller letters
    // 1.0 is large, 0.0 is normal-ish, -0.1 would be very sharp
    val levels = remember { listOf(1.0f, 0.8f, 0.6f, 0.4f, 0.2f, 0.0f) }

    var levelIndex by remember { mutableStateOf(0) }
    var trialCount by remember { mutableStateOf(0) }
    val totalTrials = 12

    var correctCount by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    // Track last “best” level the user got mostly right
    var bestLevelIndex by remember { mutableStateOf(0) }

    var currentDirection by remember { mutableStateOf(randomDirection()) }
    var feedback by remember { mutableStateOf<String?>(null) }

    // Convert logMAR level to a text size (NOT clinically calibrated).
    // This keeps the size changes reasonable without huge memory usage.
    fun textSizeForLogmar(logmar: Float): Float {
        // Bigger logMAR => bigger letter
        // You can tune these safely
        val base = 140f
        val scale = 10.0.pow(logmar.toDouble()).toFloat() // 10^(logMAR)
        val size = base * scale
        return size.coerceIn(22f, 220f) // safety clamp
    }

    fun onAnswer(selected: EDirection) {
        if (finished) return

        val isCorrect = selected == currentDirection
        trialCount++

        if (isCorrect) {
            correctCount++
            feedback = "Correct ✅"
        } else {
            feedback = "Wrong ❌"
        }

        // Simple progression rule:
        // - Every 2 trials, move to next (smaller) level if user is doing OK
        // - This keeps it smooth and avoids ending early
        val doneWithThisLevel = (trialCount % 2 == 0)
        if (doneWithThisLevel) {
            // If user got at least 1 correct in the last 2, let them go smaller
            val gotOneCorrectRecently = feedback == "Correct ✅" || isCorrect
            if (gotOneCorrectRecently && levelIndex < levels.lastIndex) {
                levelIndex++
                bestLevelIndex = levelIndex
            }
        }

        // Next target
        currentDirection = randomDirection()

        // Finish after totalTrials
        if (trialCount >= totalTrials) {
            finished = true
            feedback = null
        }
    }

    val bestLogmar = levels[bestLevelIndex]

    val scoreText = "Score: $correctCount / $totalTrials"
    val logmarText = "Estimated logMAR: ${"%.1f".format(bestLogmar)}"
    val snellenText = "Approx Snellen: ${approxSnellen(bestLogmar)}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        // ---------- TOP ----------
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LogMAR (Tumbling E) Test",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Cover one eye. Tap the direction the E is pointing.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ---------- CENTER ----------
        if (!finished) {
            Text(
                text = "E",
                fontSize = textSizeForLogmar(levels[levelIndex]).sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(rotationFor(currentDirection)),
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Test Finished", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(scoreText, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(6.dp))
                Text(logmarText, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(6.dp))
                Text(snellenText, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // ---------- BOTTOM CONTROLS ----------
        if (!finished) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding() // safe even though we don't use keyboard
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                feedback?.let {
                    Text(it, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(10.dp))
                }

                // Direction pad
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { onAnswer(EDirection.UP) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                    ) { Text("↑") }

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onAnswer(EDirection.LEFT) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                        ) { Text("←") }

                        Button(
                            onClick = { onAnswer(EDirection.RIGHT) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                        ) { Text("→") }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { onAnswer(EDirection.DOWN) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                    ) { Text("↓") }
                }

                Spacer(Modifier.height(14.dp))

                // Exit
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exit")
                }
            }
        } else {
            // Bottom buttons when finished
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // restart
                        levelIndex = 0
                        bestLevelIndex = 0
                        trialCount = 0
                        correctCount = 0
                        finished = false
                        feedback = null
                        currentDirection = randomDirection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                ) { Text("Restart") }

                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White)
                ) { Text("Back") }
            }
        }
    }
}

private fun randomDirection(): EDirection {
    return EDirection.values().random()
}

private fun rotationFor(d: EDirection): Float = when (d) {
    EDirection.UP -> 0f
    EDirection.RIGHT -> 90f
    EDirection.DOWN -> 180f
    EDirection.LEFT -> 270f
}

// Very rough mapping (good enough for your app UI; mention limitation in report)
private fun approxSnellen(logmar: Float): String {
    // Snellen denominator approx = 20 * (10^logMAR)
    val denom = (20 * 10.0.pow(logmar.toDouble())).toInt().coerceAtLeast(10)
    return "20/$denom"
}

