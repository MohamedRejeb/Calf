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

private val DarkColorScheme =
    darkColorScheme(
        primary = Blue400,
        onPrimary = Color.White,
        primaryContainer = Blue700,
        onPrimaryContainer = Blue200,
        secondary = Teal400,
        onSecondary = Color.White,
        secondaryContainer = Teal700,
        onSecondaryContainer = Teal200,
        tertiary = Purple400,
        onTertiary = Color.White,
        tertiaryContainer = Purple400,
        onTertiaryContainer = Purple200,
        background = SurfaceDark,
        onBackground = Color(0xFFE4E2E6),
        surface = SurfaceDark,
        onSurface = Color(0xFFE4E2E6),
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        surfaceContainer = SurfaceContainerDark,
        surfaceContainerHigh = SurfaceContainerHighDark,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Blue400,
        onPrimary = Color.White,
        primaryContainer = Blue200,
        onPrimaryContainer = Blue700,
        secondary = Teal400,
        onSecondary = Color.White,
        secondaryContainer = Teal200,
        onSecondaryContainer = Teal700,
        tertiary = Purple400,
        onTertiary = Color.White,
        tertiaryContainer = Purple200,
        onTertiaryContainer = Purple400,
        background = SurfaceLight,
        onBackground = Color(0xFF1B1B1F),
        surface = SurfaceLight,
        onSurface = Color(0xFF1B1B1F),
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = SurfaceContainerHighLight,
    )

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
internal fun CalfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    setSingletonImageLoaderFactory()

    val colorScheme =
        if (darkTheme)
            DarkColorScheme
        else
            LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
