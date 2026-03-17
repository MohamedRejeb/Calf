package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
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

private enum class SpacingPattern(val label: String) {
    Default("Default"),
    FlexibleSpace("Flexible Space"),
    FixedSpace("Fixed Space"),
    EvenlySpaced("Evenly Spaced"),
    LeadingGroup("Leading Group"),
    Spread("Spread"),
}

@OptIn(ExperimentalCalfUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ToolbarScreen(
    navigateBack: () -> Unit
) {
    var lastAction by remember { mutableStateOf("Tap a toolbar button") }
    var selectedPattern by remember { mutableStateOf(SpacingPattern.Default) }

    val backItem = UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName("chevron.left"),
        onClick = { lastAction = "Back tapped" },
    )
    val forwardItem = UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName("chevron.right"),
        onClick = { lastAction = "Forward tapped" },
    )
    val shareItem = UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName("square.and.arrow.up"),
        onClick = { lastAction = "Share tapped" },
    )
    val bookmarkItem = UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName("book"),
        onClick = { lastAction = "Bookmark tapped" },
    )

    val iosItems = when (selectedPattern) {
        SpacingPattern.Default -> listOf(
            backItem, forwardItem, shareItem, bookmarkItem,
        )
        SpacingPattern.FlexibleSpace -> listOf(
            backItem, forwardItem, shareItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            bookmarkItem,
        )
        SpacingPattern.FixedSpace -> listOf(
            backItem,
            UIKitUIBarButtonItem.fixedSpace(32.0),
            forwardItem,
            UIKitUIBarButtonItem.fixedSpace(32.0),
            shareItem,
            UIKitUIBarButtonItem.fixedSpace(32.0),
            bookmarkItem,
        )
        SpacingPattern.EvenlySpaced -> listOf(
            UIKitUIBarButtonItem.flexibleSpace(),
            backItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            forwardItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            shareItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            bookmarkItem,
            UIKitUIBarButtonItem.flexibleSpace(),
        )
        SpacingPattern.LeadingGroup -> listOf(
            backItem, forwardItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            shareItem, bookmarkItem,
        )
        SpacingPattern.Spread -> listOf(
            backItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            forwardItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            shareItem,
            UIKitUIBarButtonItem.flexibleSpace(),
            bookmarkItem,
        )
    }

    SampleScreenScaffold(
        title = "Adaptive Toolbar",
        navigateBack = navigateBack,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Spacing Pattern",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SpacingPattern.entries.forEach { pattern ->
                        FilterChip(
                            selected = selectedPattern == pattern,
                            onClick = { selectedPattern = pattern },
                            label = { Text(pattern.label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = lastAction,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            AdaptiveToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                iosItems = iosItems,
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
