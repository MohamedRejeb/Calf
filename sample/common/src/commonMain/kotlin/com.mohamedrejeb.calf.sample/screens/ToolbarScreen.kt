package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.toolbar.AdaptiveToolbar
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun ToolbarScreen(
    navigateBack: () -> Unit
) {
    var lastAction by remember { mutableStateOf("Tap a toolbar button") }

    SampleScreenScaffold(
        title = "Adaptive Toolbar",
        navigateBack = navigateBack,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = lastAction,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
            )

            AdaptiveToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                iosItems = listOf(
                    UIKitUIBarButtonItem(
                        image = UIKitImage.SystemName("chevron.left"),
                        onClick = { lastAction = "Back tapped" },
                    ),
                    UIKitUIBarButtonItem(
                        image = UIKitImage.SystemName("chevron.right"),
                        onClick = { lastAction = "Forward tapped" },
                    ),
                    UIKitUIBarButtonItem(
                        image = UIKitImage.SystemName("square.and.arrow.up"),
                        onClick = { lastAction = "Share tapped" },
                    ),
                    UIKitUIBarButtonItem(
                        image = UIKitImage.SystemName("book"),
                        onClick = { lastAction = "Bookmark tapped" },
                    ),
                ),
                content = {
                    IconButton(onClick = { lastAction = "Back tapped" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    IconButton(onClick = { lastAction = "Forward tapped" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                    }
                    IconButton(onClick = { lastAction = "Share tapped" }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { lastAction = "Bookmark tapped" }) {
                        Icon(Icons.Filled.BookmarkBorder, contentDescription = "Bookmark")
                    }
                },
            )
        }
    }
}
