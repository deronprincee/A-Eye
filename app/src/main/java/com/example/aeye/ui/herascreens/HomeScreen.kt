package com.example.aeye.ui.herascreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * HomeScreen is ONLY the scrollable content.
 * No Scaffold here -> prevents recursion/ANR.
 * No heavy images -> reduces memory pressure.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onTestClick: (route: String) -> Unit
) {
    // Add or rename tests anytime
    val tests = listOf(
        TestItem("Visual Acuity Test", "test_visual_acuity"),
        TestItem("Colour Vision Test", "test_colour_vision"),
        TestItem("Astigmatism Test", "test_astigmatism"),
        TestItem("Contrast Sensitivity", "test_contrast"),
        TestItem("Peripheral Vision", "test_peripheral"),
        TestItem("Eye Strain Check", "test_eye_strain"),
        TestItem("Night Vision Check", "test_night_vision"),
        TestItem("Dry Eye Check", "test_dry_eye"),
        TestItem("Focus & Clarity", "test_focus_clarity")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Choose a Test",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Scroll to explore available eye tests.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(tests) { test ->
            LargeCircleTestButton(
                title = test.title,
                onClick = { onTestClick(test.route) }
            )
        }
    }
}

private data class TestItem(
    val title: String,
    val route: String
)

@Composable
private fun LargeCircleTestButton(
    title: String,
    onClick: () -> Unit
) {
    // Large circular “button” using Surface for Material styling
    Surface(
        onClick = onClick,
        shape = CircleShape,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
