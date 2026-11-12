package com.mohamedrejeb.calf.sample.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.Platform
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.isLiquidGlassAvailable
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.navigation.nativeImplementation
import com.mohamedrejeb.calf.sample.ui.theme.Spacing

private val BadgeDotSize = 6.dp

/**
 * The top of a demo page: what the demo is about, the Calf API it uses and what draws it on this
 * platform. The bar above already shows the title.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DemoHeader(
    screen: Screen,
    modifier: Modifier = Modifier,
) {
    val native = screen.nativeImplementation(currentPlatform, isLiquidGlassAvailable)
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
        modifier = modifier.padding(horizontal = Spacing.xs),
    ) {
        Text(
            text = screen.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (screen.api.isNotEmpty()) CodePill(screen.api)
            if (native != null) NativeBadge(platform = currentPlatform, implementation = native)
        }
    }
}

/** A Calf API name, set in code type. */
@Composable
fun CodePill(
    code: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs),
        )
    }
}

/**
 * `iOS | UISwitch`: the platform the sample runs on, and what draws the demo there. Two joined
 * segments, the platform on the brand colour and the implementation on its container.
 */
@Composable
fun NativeBadge(
    platform: Platform,
    implementation: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.small),
    ) {
        BadgeSegment(
            text = platform.displayName,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
        BadgeSegment(
            text = implementation,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun BadgeSegment(text: String, containerColor: Color, contentColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxHeight()
            .background(containerColor)
            .padding(horizontal = Spacing.s, vertical = Spacing.xs),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

/** A small filled circle, for status and legends. */
@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(BadgeDotSize).background(color, CircleShape))
}

/** How the sample names a platform in its UI. */
val Platform.displayName: String
    get() = when (this) {
        Platform.Android -> "Android"
        Platform.IOS -> "iOS"
        Platform.Desktop -> "Desktop"
        Platform.Web -> "Web"
    }
