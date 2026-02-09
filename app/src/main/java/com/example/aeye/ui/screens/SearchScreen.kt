package com.example.aeye.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.aeye.ChatActivity
import com.example.aeye.ui.animation.HeraFlowerReveal


@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    // Initializes context and loading variable
    val context = LocalContext.current
    var showLoading by remember { mutableStateOf(true) }

    // Added full screen container for content/loading overlay
    Box(modifier = Modifier.fillMaxSize()) {
        if (showLoading) {
            // Displays animated loading
            HeraFlowerReveal(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6A9B2)) // Hera pink
            ) {
                showLoading = false // hides the overlay
                val intent = Intent(context, ChatActivity::class.java)
                context.startActivity(intent) //Starts chatActivity
            }
        }
    }

}