package com.example.aeye.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Slider
import androidx.compose.ui.graphics.Color
import com.example.aeye.viewmodel.CalibrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    navController: NavController,
    calibrationViewModel: CalibrationViewModel
) {
    val cardWidthMm = 53.98f

    // This is dp, not px
    var widthDp by remember { mutableStateOf(200f) } // starting guess (dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Screen Calibration", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Place a bank card against your screen.\nAdjust the bar to match the card width.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Visual bar width is directly in dp (no conversion)
        Box(
            modifier = Modifier
                .height(56.dp)
                .width(widthDp.dp)
                .background(MaterialTheme.colorScheme.primary)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Slider(
                value = widthDp,
                onValueChange = { widthDp = it },
                valueRange = 300f..900f,
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
                    // Store dp per mm (NOT px/mm)
                    val dpPerMm = widthDp / cardWidthMm
                    calibrationViewModel.setCalibration(dpPerMm.toDouble())
                    navController.navigate("logmar")
                }
            ) {
                Text("Confirm Calibration")
            }
        }
    }
}