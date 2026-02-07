package com.example.aeye.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.aeye.R
import kotlinx.coroutines.delay

@Composable
fun HeraFlowerReveal(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    // Values for scaling and opacity (Animation)
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    // Values for infinite rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "Full Rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Rotation"
    )

    // Triggers the animation
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(800, easing = EaseOutBack))
        alpha.animateTo(1f, animationSpec = tween(800))
        delay(1800L)
        onFinished()
    }

    // UI layout for flower image rotation + typewriter text
    Box(
        modifier = modifier.fillMaxSize()
            .background(Color(0xFFF6A9B2)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.setting),
                contentDescription = "Hera Flower Logo",
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        alpha = alpha.value,
                        rotationZ = rotation
                    )
            )
            Spacer(modifier = Modifier.height(20.dp))
            TypewriterText(fullText = "Loading Hera...",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = Color(0xFFFFFFFF)
            )
        }
    }
}

@Composable
fun TypewriterText(
    fullText: String,
    delayPerChar: Long = 60L,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
    color: Color = Color.White
) {
    var text by remember {mutableStateOf("")}

    // Logic for revealing each character via a sequence + delay
    LaunchedEffect(fullText) {
        text = ""
        for (i in fullText.indices) {
            text += fullText[i]
            delay(delayPerChar)
        }
    }

    // Added fade animation for the text
    val infiniteTransition = rememberInfiniteTransition(label = "Fade")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Alpha"
    )

    // Displays animated text
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier.graphicsLayer(alpha = alpha)
    )
}