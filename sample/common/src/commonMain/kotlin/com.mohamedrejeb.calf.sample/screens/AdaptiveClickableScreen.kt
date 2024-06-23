package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable

@Composable
fun AdaptiveClickableScreen(
    navigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        IconButton(
            onClick = {
                navigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .adaptiveClickable(
                    shape = MaterialTheme.shapes.medium,
                ) {
                    // Handle click
                }
                .background(MaterialTheme.colorScheme.primary)
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                )
        ) {
            Text(
                text = "Adaptive Clickable",
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}