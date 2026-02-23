package com.example.aeye.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.pow
import kotlin.random.Random

data class LogmarRow(
    val logmar: Float,
    val letters: String, // stored like "C D H K N"
    val snellenApprox: String
)

@Composable
fun LogmarTestScreen(navController: NavController) {

    // ----- ETDRS-style settings -----
    // Typical Sloan letters used in ETDRS/logMAR charts
    val sloanLetters = remember { listOf('C', 'D', 'H', 'K', 'N', 'O', 'R', 'S', 'V', 'Z') }

    // logMAR lines: 1.0 (large) down to 0.0 (normal). Add more if you want.
    val rows = remember {
        buildLogmarRows(
            logmarValues = listOf(1.0f, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.0f),
            sloanLetters = sloanLetters
        )
    }

    // Each line has 5 letters; each letter is worth 0.02 logMAR
    val lettersPerLine = 5
    val letterValue = 0.02f

    var rowIndex by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    // Voice captured text
    var spokenText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }

    // Track correctness per line (0..5 correct)
    val correctPerLine = remember { mutableStateListOf<Int>() }

    // ----- Permission + Speech launchers -----
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            feedback = "Microphone permission denied."
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenText = matches?.firstOrNull().orEmpty()
            feedback = "Heard: \"$spokenText\""
        }
    }

    fun startVoiceInput() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the letters clearly, e.g., C D H K N")
        }
        speechLauncher.launch(intent)
    }

    fun scoreCurrentLineAndNext() {
        val currentRow = rows.getOrNull(rowIndex) ?: return

        val expected = normalizeLetters(currentRow.letters)          // "CDHKN"
        val spoken = extractLettersFromSpeech(spokenText)            // "CDHKN" (best effort)

        val correctCount = countCorrectByPosition(expected, spoken)  // 0..5
        correctPerLine.add(correctCount)

        feedback = "Scored: $correctCount / $lettersPerLine correct"
        spokenText = "" // clear for next

        if (rowIndex < rows.lastIndex) {
            rowIndex++
        } else {
            finished = true
        }
    }

    // ----- Calculate results when finished -----
    val totalCorrectLetters = correctPerLine.sum()
    val startLogmar = rows.first().logmar
    val finalLogmar = (startLogmar - (totalCorrectLetters * letterValue)).coerceAtLeast(-0.3f)

    val scoreText = "Correct letters: $totalCorrectLetters / ${rows.size * lettersPerLine}"
    val logmarText = "Estimated logMAR: ${"%.2f".format(finalLogmar)}"
    val snellenText = "Approx Snellen: ${approxSnellen(finalLogmar)}"

    // ----- UI layout -----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        // Top title + instructions
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("LogMAR Chart (Voice)", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Cover one eye. Read the 5 letters aloud.\nTap Speak → then Submit to score the line.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Center chart row (5 letters)
        if (!finished) {
            val row = rows[rowIndex]

            Text(
                text = row.letters,
                modifier = Modifier.align(Alignment.Center),
                fontSize = textSizeForLogmar(row.logmar).sp,
                fontWeight = FontWeight.ExtraBold
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

        // Bottom controls pinned above keyboard/nav bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            feedback?.let {
                Text(it, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(10.dp))
            }

            if (!finished) {
                // Spoken preview box (so user knows what was heard)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (spokenText.isBlank()) "Spoken text will appear here…" else spokenText,
                        modifier = Modifier.padding(12.dp),
                        color = Color.DarkGray
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { startVoiceInput() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                    ) {
                        Text("Speak")
                    }

                    Button(
                        onClick = { scoreCurrentLineAndNext() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                    ) {
                        Text("Submit")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White)
                ) {
                    Text("Exit")
                }

            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // Restart
                            rowIndex = 0
                            finished = false
                            spokenText = ""
                            feedback = null
                            correctPerLine.clear()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                    ) { Text("Restart") }

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White)
                    ) { Text("Back") }
                }
            }
        }
    }
}

/** Build a chart with 5 Sloan letters per line (ETDRS style). */
private fun buildLogmarRows(
    logmarValues: List<Float>,
    sloanLetters: List<Char>
): List<LogmarRow> {
    return logmarValues.map { log ->
        val letters = (1..5).map { sloanLetters[Random.nextInt(sloanLetters.size)] }.joinToString(" ")
        LogmarRow(
            logmar = log,
            letters = letters,
            snellenApprox = approxSnellen(log)
        )
    }
}

/** Approximate font size changes with logMAR (not clinically calibrated). */
private fun textSizeForLogmar(logmar: Float): Float {
    val base = 120f
    val scale = 10.0.pow(logmar.toDouble()).toFloat() // 10^(logMAR)
    return (base * scale).coerceIn(18f, 220f)
}

/** Keep only A–Z from speech result. */
private fun extractLettersFromSpeech(text: String): String {
    return text.uppercase().filter { it in 'A'..'Z' }
}

/** Normalize expected chart letters like "C D H K N" -> "CDHKN". */
private fun normalizeLetters(rowLetters: String): String {
    return rowLetters.uppercase().filter { it in 'A'..'Z' }
}

/** Counts correct letters by position (ETDRS style expects 5 letters in order). */
private fun countCorrectByPosition(expected: String, spoken: String): Int {
    return expected.zip(spoken).count { (e, s) -> e == s }
}

/** Rough conversion shown to user (report limitation: not clinically calibrated). */
private fun approxSnellen(logmar: Float): String {
    val denom = (20 * 10.0.pow(logmar.toDouble())).toInt().coerceAtLeast(10)
    return "20/$denom"
}