package com.example.aeye.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onTestClick: (String) -> Unit
) {
    val userName = remember {
        val u = FirebaseAuth.getInstance().currentUser
        // Prefer displayName, else email prefix, else fallback
        u?.displayName?.takeIf { it.isNotBlank() }
            ?: u?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "User"
    }

    val tests = remember {
        listOf(
            TestItem("eye\ntest\n1", "test_1"),
            TestItem("eye\ntest\n2", "test_2"),
            TestItem("eye\ntest\n3", "test_3"),
            TestItem("eye\ntest\n4", "test_4"),
            TestItem("eye\ntest\n5", "test_5"),
        )
    }

    // Light grey page background like your wireframe
    val pageGrey = Color(0xFFEDEDED)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageGrey)
            .padding(horizontal = 20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 18.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "WELCOME BACK, ${userName.uppercase()}!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B5EA8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Click on the button below to start\nyour eyesight test",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF111111),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))
            }

            items(tests) { test ->
                EqualCircleTestButton(
                    label = "Snellen Test",
                    onClick = { onTestClick("snellen") }
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Swipe down for the next test",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF111111),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

private data class TestItem(
    val label: String,
    val route: String
)

@Composable
private fun EqualCircleTestButton(
    label: String,
    onClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Circle size based on screen width
        val size: Dp = (maxWidth * 0.75f).coerceAtMost(260.dp)

        val circleBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1577C9),
                Color(0xFF0E5EA7)
            )
        )

        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.size(size)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(circleBrush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    textAlign = TextAlign.Center,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF9A3D), // orange like your wireframe
                    lineHeight = 46.sp,
                    modifier = Modifier.shadow(0.dp) // no heavy effects
                )
            }
        }
    }
}
