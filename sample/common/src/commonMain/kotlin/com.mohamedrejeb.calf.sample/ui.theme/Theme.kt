package com.mohamedrejeb.calf.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.coil.setSingletonImageLoaderFactory

// Cards use surfaceContainerLowest: white on the grey background in light, one step above the
// background in dark. Material components keep their usual container roles.
private val LightColorScheme =
    lightColorScheme(
        primary = Indigo40,
        onPrimary = Color.White,
        primaryContainer = Indigo90,
        onPrimaryContainer = Indigo10,
        inversePrimary = Indigo80,
        secondary = Slate40,
        onSecondary = Color.White,
        secondaryContainer = Slate90,
        onSecondaryContainer = Slate20,
        tertiary = Coral40,
        onTertiary = Color.White,
        tertiaryContainer = Coral90,
        onTertiaryContainer = Coral20,
        background = LightBackground,
        onBackground = LightOnSurface,
        surface = LightBackground,
        onSurface = LightOnSurface,
        surfaceVariant = LightContainerHigh,
        onSurfaceVariant = LightOnSurfaceVariant,
        surfaceTint = Indigo40,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        surfaceBright = LightCard,
        surfaceDim = LightContainerHigh,
        surfaceContainerLowest = LightCard,
        surfaceContainerLow = LightCard,
        surfaceContainer = LightContainer,
        surfaceContainerHigh = LightContainerHigh,
        surfaceContainerHighest = LightContainerHighest,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Indigo80,
        onPrimary = Indigo20,
        primaryContainer = Indigo30,
        onPrimaryContainer = Indigo90,
        inversePrimary = Indigo40,
        secondary = Slate80,
        onSecondary = Slate20,
        secondaryContainer = Slate30,
        onSecondaryContainer = Slate90,
        tertiary = Coral80,
        onTertiary = Coral20,
        tertiaryContainer = Coral30,
        onTertiaryContainer = Coral90,
        background = DarkBackground,
        onBackground = DarkOnSurface,
        surface = DarkBackground,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkContainerHigh,
        onSurfaceVariant = DarkOnSurfaceVariant,
        surfaceTint = Indigo80,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        surfaceBright = DarkContainerHighest,
        surfaceDim = DarkBackground,
        surfaceContainerLowest = DarkCard,
        surfaceContainerLow = DarkCard,
        surfaceContainer = DarkContainer,
        surfaceContainerHigh = DarkContainerHigh,
        surfaceContainerHighest = DarkContainerHighest,
    )

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
internal fun CalfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    setSingletonImageLoaderFactory()

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
