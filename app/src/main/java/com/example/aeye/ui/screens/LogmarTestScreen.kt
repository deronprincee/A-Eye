package com.example.aeye.ui.screens

import LogmarRow
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aeye.data.model.TestResult
import com.example.aeye.viewmodel.CalibrationViewModel
import com.example.aeye.viewmodel.ResultsViewModel
import kotlin.math.pow
import kotlin.random.Random
import androidx.compose.ui.platform.LocalContext
import com.example.aeye.ui.components.isSupportedForLogmar
import com.example.aeye.ui.components.isTabletDevice
import com.example.aeye.ui.components.logmarSupportReason

enum class InputMode { SPEECH, TYPING }
private const val MIN_LETTER_SP = 8f

@Composable
fun LogmarTestScreen(
    navController: NavController,
    calibrationViewModel: CalibrationViewModel,
    resultsViewModel: ResultsViewModel,
    calibrationRoute: String = "calibration"
) {

    val pxPerMm: Double? by calibrationViewModel.pxPerMm.collectAsState()
    val context = LocalContext.current

    val isSupportedDevice = isSupportedForLogmar(context, pxPerMm)
    val unsupportedReason = logmarSupportReason(context, pxPerMm)

    if (!isSupportedDevice) {
        UnsupportedDeviceScreen(
            message = unsupportedReason ?: "This device is not supported for LogMAR testing.",
            onExit = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        return
    }

    if (pxPerMm == null || pxPerMm!! <= 0.0) {
        CalibrationRequiredScreen(
            onCalibrate = { navController.navigate(calibrationRoute) },
            onExit = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
        return
    }

    val sloanLetters = remember { listOf('C', 'D', 'H', 'K', 'N', 'O', 'R', 'S', 'V', 'Z') }

    val rows = remember {
        buildLogmarRows(
            logmarValues = listOf(1.0f, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f, 0.0f),
            sloanLetters = sloanLetters
        )
    }

    val lettersPerLine = 5
    val letterValue = 0.02f

    var rowIndex by remember { mutableStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var deviceLimitReached by remember { mutableStateOf(false) }

    var lastPassedRowIndex by remember { mutableStateOf<Int?>(null) }
    var lastAttemptedRowIndex by remember { mutableStateOf<Int?>(null) }

    val correctPerLine = remember { mutableStateListOf<Int>() }

    val nextRowCorrectCount = if (
        lastPassedRowIndex != null &&
        lastPassedRowIndex!! + 1 < correctPerLine.size
    ) {
        correctPerLine[lastPassedRowIndex!! + 1]
    } else {
        0
    }

    var savedToFirestore by remember { mutableStateOf(false) }

    var inputMode by remember { mutableStateOf(InputMode.SPEECH) }
    var spokenText by remember { mutableStateOf("") }
    var typedText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }

    val typeFocusRequester = remember { FocusRequester() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) feedback = "Microphone permission denied."
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

    //Calibrated sizing (40 cm near)
    val density = LocalDensity.current
    val pxPerMmF = pxPerMm!!.toFloat()

    fun calibratedFontSizeSp(logmar: Float): Pair<Float, Boolean> {
        val mar = 10.0.pow(logmar.toDouble()).toFloat()
        val heightMm = 0.5818f * mar
        val heightPx = heightMm * pxPerMmF

        val scaledDensity = density.fontScale * density.density
        val heightSp = heightPx / scaledDensity

        val reachedLimit = heightSp <= MIN_LETTER_SP
        val finalSize = heightSp.coerceAtLeast(MIN_LETTER_SP)

        return finalSize to reachedLimit
    }

    fun scoreCurrentLineAndNext() {
        val currentRow = rows.getOrNull(rowIndex) ?: return

        val expected = normalizeLetters(currentRow.letters) // "CDHKN"
        val rawInput = when (inputMode) {
            InputMode.SPEECH -> spokenText
            InputMode.TYPING -> typedText
        }

        if (rawInput.isBlank()) {
            feedback = if (inputMode == InputMode.SPEECH) {
                "Tap Speak first (or switch to Typing)."
            } else {
                "Type the letters first (or switch to Speech)."
            }
            return
        }

        val userLetters = extractLetters(rawInput) // keep A–Z only
        val correctCount = countCorrectByPosition(expected, userLetters)
        correctPerLine.add(correctCount)

        lastAttemptedRowIndex = rowIndex

        if (correctCount >= 3) {
            lastPassedRowIndex = rowIndex
        }

        feedback = "Scored: $correctCount / $lettersPerLine correct"
        spokenText = ""
        typedText = ""

        val nextIndex = rowIndex + 1

        if (nextIndex <= rows.lastIndex) {
            val (_, nextWouldHitLimit) = calibratedFontSizeSp(rows[nextIndex].logmar)

            if (nextWouldHitLimit) {
                deviceLimitReached = true
                finished = true
                return
            }
        }

        if (rowIndex < rows.lastIndex) rowIndex++ else finished = true
    }

    //Results
    val totalCorrectLetters = correctPerLine.sum()
    val attemptedLetters = correctPerLine.size * lettersPerLine

    val lastAttemptedRowLogmar = lastAttemptedRowIndex?.let { rows[it].logmar }
    val lastPassedRowLogmar = lastPassedRowIndex?.let { rows[it].logmar }

    val finalLogmar = when {
        lastPassedRowLogmar != null -> {
            (lastPassedRowLogmar - (nextRowCorrectCount * letterValue)).coerceAtLeast(-0.3f)
        }
        lastAttemptedRowLogmar != null && correctPerLine.isNotEmpty() -> {
            val attemptedCorrect = correctPerLine[lastAttemptedRowIndex!!]
            (lastAttemptedRowLogmar - (attemptedCorrect * letterValue)).coerceAtLeast(-0.3f)
        }
        else -> rows.first().logmar
    }

    val passedRowText = lastPassedRowLogmar?.let {
        "Smallest passed row: ${"%.1f".format(it)} logMAR"
    } ?: "No row passed"

    val attemptedRowText = lastAttemptedRowLogmar?.let {
        "Last attempted row: ${"%.1f".format(it)} logMAR"
    } ?: "No row attempted"

    LaunchedEffect(finished) {
        if (finished && !savedToFirestore) {
            savedToFirestore = true

            val result = TestResult(
                testType = "LOGMAR_NEAR",
                finalLogmar = finalLogmar.toDouble(),
                snellenApprox = approxSnellen(finalLogmar),
                totalLetters = attemptedLetters,
                totalCorrectLetters = totalCorrectLetters,
                correctPerLine = correctPerLine.toList(),
                inputMode = inputMode.toString(),
                lastAttemptedRowLogmar = lastAttemptedRowLogmar?.toDouble(),
                lastPassedRowLogmar = lastPassedRowLogmar?.toDouble(),
                pxPerMm = pxPerMm
            )

            resultsViewModel.saveResult(result)
        }
    }

    val scoreText = "Correct letters: $totalCorrectLetters / $attemptedLetters"
    val logmarText = "Estimated logMAR: ${"%.2f".format(finalLogmar)}"
    val snellenText = "Approx Snellen: ${approxSnellen(finalLogmar)}"

    //UI layout
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
            Text("LogMAR Near Test", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Distance: 40 cm. Cover one eye.\nRead the 5 letters using Speech or Typing.\nTap Submit to score the line.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Center chart row (5 letters)
        if (!finished) {
            val row = rows[rowIndex]
            val (fontSizeSp, _) = calibratedFontSizeSp(row.logmar)

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = row.letters,
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
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
                Spacer(Modifier.height(6.dp))
                Text(passedRowText, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(6.dp))
                Text(attemptedRowText, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Bottom controls
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
                // Mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = inputMode == InputMode.SPEECH,
                        onClick = { inputMode = InputMode.SPEECH },
                        label = { Text("Speech") }
                    )
                    FilterChip(
                        selected = inputMode == InputMode.TYPING,
                        onClick = {
                            inputMode = InputMode.TYPING
                            feedback = null
                        },
                        label = { Text("Type") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                val previewText = when (inputMode) {
                    InputMode.SPEECH ->
                        if (spokenText.isBlank()) "Spoken text will appear here…" else spokenText
                    InputMode.TYPING ->
                        if (typedText.isBlank()) "Typed text will appear here…" else typedText
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = inputMode == InputMode.TYPING) {
                            // tapping card focuses hidden field
                            typeFocusRequester.requestFocus()
                        }
                ) {
                    Text(
                        text = previewText,
                        modifier = Modifier.padding(12.dp),
                        color = Color.DarkGray
                    )
                }

                // Hidden input field to capture keyboard typing
                if (inputMode == InputMode.TYPING) {
                    Spacer(Modifier.height(8.dp))

                    BasicTextField(
                        value = typedText,
                        onValueChange = { new ->
                            typedText = new.uppercase().filter { it == ' ' || it in 'A'..'Z' }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.dp) // keep it visually hidden
                            .focusRequester(typeFocusRequester),
                        textStyle = TextStyle(fontSize = 1.sp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            capitalization = KeyboardCapitalization.Characters,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Speak/Type + Submit row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (inputMode == InputMode.SPEECH) startVoiceInput()
                            else typeFocusRequester.requestFocus()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text(if (inputMode == InputMode.SPEECH) "Speak" else "Type")
                    }

                    Button(
                        onClick = { scoreCurrentLineAndNext() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) { Text("Submit") }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.DarkGray
                    )
                ) { Text("Exit") }

            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            rowIndex = 0
                            finished = false
                            inputMode = InputMode.SPEECH
                            spokenText = ""
                            typedText = ""
                            feedback = null
                            correctPerLine.clear()
                            savedToFirestore = false
                            lastPassedRowIndex = null
                            lastAttemptedRowIndex = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) { Text("Restart") }

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) { Text("Exit") }
                }
            }
        }
    }
}

@Composable
private fun CalibrationRequiredScreen(
    onCalibrate: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Calibration required", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        Text(
            "To size optotypes correctly at 40 cm, calibrate your screen using a bank card.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onCalibrate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.DarkGray
            )
        ) {
            Text("Calibrate now")
        }
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.DarkGray
            )
        ) {
            Text("Exit")
        }
    }
}

private fun buildLogmarRows(
    logmarValues: List<Float>,
    sloanLetters: List<Char>
): List<LogmarRow> {
    return logmarValues.map { log ->
        val letters = (1..5)
            .map { sloanLetters[Random.nextInt(sloanLetters.size)] }
            .joinToString(" ")

        LogmarRow(
            logmar = log,
            letters = letters,
            snellenApprox = approxSnellen(log)
        )
    }
}

@Composable
private fun UnsupportedDeviceScreen(
    message: String,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Device not supported", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.DarkGray
            )
        ) {
            Text("Exit")
        }
    }
}

//Keep only A–Z from any input (speech or typed).
private fun extractLetters(text: String): String =
    text.uppercase().filter { it in 'A'..'Z' }

//Normalize expected chart letters like "C D H K N" -> "CDHKN".
private fun normalizeLetters(rowLetters: String): String =
    rowLetters.uppercase().filter { it in 'A'..'Z' }

//Counts correct letters by position (expects 5 letters in order)
private fun countCorrectByPosition(expected: String, user: String): Int =
    expected.zip(user).count { (e, s) -> e == s }

//Rough conversion shown to user (report limitation: not clinically calibrated).
private fun approxSnellen(logmar: Float): String {
    val denom = (20 * 10.0.pow(logmar.toDouble())).toInt().coerceAtLeast(10)
    return "20/$denom"
}

