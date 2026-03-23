package com.example.aeye.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aeye.viewmodel.ResultsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    resultsViewModel: ResultsViewModel,
    modifier: Modifier = Modifier
) {
    val allResults by resultsViewModel.results.collectAsState()

    val testTypes = remember {
        listOf("All", "LOGMAR_NEAR", "SNELLEN", "OTHER")
    }

    var selectedTestType by remember { mutableStateOf("All") }
    var testTypeExpanded by remember { mutableStateOf(false) }

    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var endDateMillis by remember { mutableStateOf<Long?>(null) }

    var appliedTestType by remember { mutableStateOf("All") }
    var appliedStart by remember { mutableStateOf<Long?>(null) }
    var appliedEnd by remember { mutableStateOf<Long?>(null) }

    //DATE PICKERS
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState()
    val endPickerState = rememberDatePickerState()

    //DELETE CONFIRM
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    //FILTERING
    val filtered = remember(allResults, appliedTestType, appliedStart, appliedEnd) {
        allResults.filter { r ->
            // NOTE: adjust field names to your actual TestResult model
            val typeOk = appliedTestType == "All" || (r.testType == appliedTestType)
            val time = r.createdAtMillis ?: 0L
            val startOk = appliedStart == null || time >= appliedStart!!
            val endOk = appliedEnd == null || time <= appliedEnd!!
            typeOk && startOk && endOk
        }
    }

    val pageGrey = Color(0xFFEDEDED)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageGrey)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)

        ) {

            Text(
                text = "Test Results",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            //TEST TYPE DROPDOWN
            ExposedDropdownMenuBox(
                expanded = testTypeExpanded,
                onExpandedChange = { testTypeExpanded = !testTypeExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    value = selectedTestType,
                    onValueChange = {},
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                ExposedDropdownMenu(
                    expanded = testTypeExpanded,
                    onDismissRequest = { testTypeExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    testTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedTestType = type
                                testTypeExpanded = false
                            }
                        )
                    }
                }
            }

            //DATE RANGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(text = startDateMillis.formatAsDateOr("Start date"))
                }

                Button(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(text = endDateMillis.formatAsDateOr("End date"))
                }
            }

            //SEARCH BUTTON
            Button(
                onClick = {
                    appliedTestType = selectedTestType

                    // Normalize end date to include full day (optional)
                    appliedStart = startDateMillis
                    appliedEnd = endDateMillis?.let { it + 86_399_000L } // up to 23:59:59
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Search")
            }

            HorizontalDivider()

            //RESULTS LIST
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found for the selected filters.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { r ->
                        ResultCard(
                            testName = r.testType,
                            dateText = r.createdAtMillis?.formatAsDateTime() ?: "Date unavailable",
                            finalLogmar = r.finalLogmar,
                            snellenApprox = r.snellenApprox,
                            totalLetters = r.totalLetters,
                            totalCorrectLetters = r.totalCorrectLetters,
                            correctPerLine = r.correctPerLine,
                            pxPerMm = r.pxPerMm,
                            lastAttemptedRowLogmar = r.lastAttemptedRowLogmar,
                            lastPassedRowLogmar = r.lastPassedRowLogmar,
                            inputMode = r.inputMode,
                            onDelete = { pendingDeleteId = r.id }
                        )
                    }
                }
            }
        }

        //START DATE PICKER
        if (showStartPicker) {
            DatePickerDialog(
                onDismissRequest = { showStartPicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            startDateMillis = startPickerState.selectedDateMillis
                            showStartPicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = startPickerState)
            }
        }

        //END DATE PICKER
        if (showEndPicker) {
            DatePickerDialog(
                onDismissRequest = { showEndPicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            endDateMillis = endPickerState.selectedDateMillis
                            showEndPicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = endPickerState)
            }
        }

        //DELETE CONFIRM
        if (pendingDeleteId != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                title = { Text("Delete result?") },
                text = { Text("This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = pendingDeleteId!!
                            pendingDeleteId = null
                            resultsViewModel.deleteResult(id)
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun ResultCard(
    testName: String,
    dateText: String,
    finalLogmar: Double?,
    snellenApprox: String?,
    totalCorrectLetters: Int?,
    totalLetters: Int?,
    correctPerLine: List<Int>,
    pxPerMm: Double?,
    lastAttemptedRowLogmar: Double?,
    lastPassedRowLogmar: Double?,
    inputMode: String?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = testName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                finalLogmar?.let {
                    Text("LogMAR: ${"%.2f".format(it)}")
                }
                snellenApprox?.let {
                    Text("Snellen: $it")
                }
                totalLetters?.let {
                    Text("Total Letters: $it")
                }
                totalCorrectLetters?.let {
                    Text("Total Correct Letters: $it")
                }
                if (correctPerLine.isNotEmpty()) {
                    Text("Correct Letters Per row: ${correctPerLine.joinToString(", ")}")
                }
                pxPerMm?.let {
                    Text("Pixels per Millimeters: $it")
                }
                lastAttemptedRowLogmar?.let {
                    Text("last Row Attempted: $it")
                }
                lastPassedRowLogmar?.let {
                    Text("last Row Passed: $it")
                }
                inputMode?.let {
                    Text("Input Mode: $it")
                }

            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

//Date helpers
private fun Long?.formatAsDateOr(fallback: String): String {
    if (this == null) return fallback
    val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return df.format(Date(this))
}

private fun Long.formatAsDateTime(): String {
    val df = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return df.format(Date(this))
}