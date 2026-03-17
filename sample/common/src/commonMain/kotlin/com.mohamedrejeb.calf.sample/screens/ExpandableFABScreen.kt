package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
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
import com.mohamedrejeb.calf.ui.fab.AdaptiveExpandableFAB
import com.mohamedrejeb.calf.ui.fab.UIKitExpandableFABItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun ExpandableFABScreen(
    navigateBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("Tap the FAB to expand") }

    SampleScreenScaffold(
        title = "Adaptive Expandable FAB",
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

            AdaptiveExpandableFAB(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                mainIcon = Icons.Filled.Add,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                iosMainImage = UIKitExpandableFABItem(
                    title = "",
                    image = UIKitImage.SystemName("plus"),
                ),
                iosItems = listOf(
                    UIKitExpandableFABItem(
                        title = "",
                        image = UIKitImage.SystemName("square.and.pencil"),
                    ),
                    UIKitExpandableFABItem(
                        title = "",
                        image = UIKitImage.SystemName("envelope"),
                    ),
                    UIKitExpandableFABItem(
                        title = "",
                        image = UIKitImage.SystemName("square.and.arrow.up"),
                    ),
                ),
                iosOnItemSelected = { index ->
                    lastAction = when (index) {
                        0 -> "Edit tapped"
                        1 -> "Email tapped"
                        2 -> "Share tapped"
                        else -> "Item $index tapped"
                    }
                    expanded = false
                },
                items = {
                    SmallFloatingActionButton(
                        onClick = {
                            lastAction = "Edit tapped"
                            expanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            lastAction = "Email tapped"
                            expanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = "Email")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            lastAction = "Share tapped"
                            expanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                },
            )
        }
    }
}
