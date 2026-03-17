package com.example.aeye.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.example.aeye.viewmodel.CalibrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    navController: NavController,
    calibrationViewModel: CalibrationViewModel
) {
    val cardWidthMm = 85.60f
    val cardHeightMm = 53.98f
    val cardAspectRatio = cardHeightMm / cardWidthMm

    var frameWidthDp by remember { mutableStateOf(260f) }

    var outerWidthPx by remember { mutableStateOf(0f) }
    var outerHeightPx by remember { mutableStateOf(0f) }

    var calibrationSubmitted by remember { mutableStateOf(false) }
    val savedPxPerMm: Double? by calibrationViewModel.pxPerMm.collectAsState()

    val borderThickness = 3.dp
    val cornerRadius = 16.dp
    val density = LocalDensity.current
    val borderThicknessPx = with(density) { borderThickness.toPx() }

    LaunchedEffect(savedPxPerMm, calibrationSubmitted) {
        if (calibrationSubmitted && savedPxPerMm != null && savedPxPerMm!! > 0.0) {
            navController.navigate("logmar") {
                popUpTo("calibration") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val maxFrameWidthDp = maxWidth.value - 14f
        val safeFrameWidthDp = frameWidthDp.coerceIn(140f, maxFrameWidthDp)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Screen Calibration", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Place a bank card inside the rounded rectangle.\nAdjust the slider until the card fits exactly inside the border.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(safeFrameWidthDp.dp)
                        .aspectRatio(cardAspectRatio)
                        .onGloballyPositioned { coordinates ->
                            outerWidthPx = coordinates.size.width.toFloat()
                            outerHeightPx = coordinates.size.height.toFloat()
                        }
                        .border(
                            width = borderThickness,
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(cornerRadius)
                        )
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Slider(
                    value = safeFrameWidthDp,
                    onValueChange = { frameWidthDp = it },
                    valueRange = 140f..maxFrameWidthDp,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.LightGray,
                        inactiveTrackColor = Color.DarkGray
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        val innerWidthPx = (outerWidthPx - 2f * borderThicknessPx).coerceAtLeast(1f)
                        val innerHeightPx = (outerHeightPx - 2f * borderThicknessPx).coerceAtLeast(1f)

                        val pxPerMmFromWidth = innerWidthPx / cardHeightMm
                        val pxPerMmFromHeight = innerHeightPx / cardWidthMm

                        val pxPerMm = (pxPerMmFromWidth + pxPerMmFromHeight) / 2f

                        calibrationSubmitted = true
                        calibrationViewModel.setCalibration(pxPerMm.toDouble())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.DarkGray
                    )
                ) {
                    Text("Confirm Calibration")
                }
            }
        }
    }
}