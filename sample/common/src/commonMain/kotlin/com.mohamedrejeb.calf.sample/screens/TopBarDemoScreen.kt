package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.sf.symbols.SFSymbol

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
fun TopBarDemoScreen(
    navigateBack: () -> Unit,
) {
    AdaptiveScaffold(
        topBar = {
            AdaptiveTopBar(
                title = { Text("Adaptive Top Bar") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    AdaptiveIconButton(onClick = { /* share */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    AdaptiveIconButton(onClick = { /* more */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
                iosTitle = "Adaptive Top Bar",
                iosLeadingItems = listOf(
                    UIKitUIBarButtonItem(
                        title = "Back",
                        onClick = navigateBack,
                    ),
                ),
                iosTrailingItems = listOf(
                    UIKitUIBarButtonItem(
                        image = UIKitImage.SystemName(SFSymbol.squareAndArrowUp),
                        onClick = { /* share */ },
                    ),
                    UIKitUIBarButtonItem(
                        image = UIKitImage.SystemName(SFSymbol.ellipsisCircle),
                        onClick = { /* more */ },
                    ),
                ),
                iosConfiguration = UIKitNavigationBarConfiguration(
                    prefersLargeTitles = true,
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Top app bar using native iOS UINavigationBar and Material3 TopAppBar on other platforms.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            InfoCard(
                title = "Material3 (Android/Desktop/Web)",
                description = "Uses standard Material3 TopAppBar with navigationIcon, title, and actions slots. Supports scroll behavior and expanded height.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "iOS (UINavigationBar)",
                description = "Uses native UIKit UINavigationBar with iosTitle, iosLeadingItems, and iosTrailingItems. Automatically adapts to iOS navigation patterns.",
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "This Screen",
                description = "This screen demonstrates a top bar with a back button (leading) and two action buttons (trailing: Share and More). On iOS, these become native UIBarButtonItems.",
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
