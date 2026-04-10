package com.mohamedrejeb.calf.cupertino.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.calf_cupertino_icons.generated.resources.Res
import com.mohamedrejeb.calf.calf_cupertino_icons.generated.resources.sf_symbols
import org.jetbrains.compose.resources.Font

/**
 * Displays a Cupertino-style icon from the bundled SF Symbols font.
 *
 * Use [CupertinoIcons] constants for the [iconCode] parameter:
 * ```kotlin
 * CupertinoIcon(CupertinoIcons.house)
 * CupertinoIcon(CupertinoIcons.heartFill, tint = Color.Red)
 * ```
 *
 * @param iconCode The Unicode codepoint from [CupertinoIcons].
 * @param modifier Modifier to be applied to the icon.
 * @param size The size of the icon. Default is 24.dp.
 * @param tint The color to tint the icon. Default is [LocalContentColor].
 * @param contentDescription Accessibility description for the icon.
 */
@Composable
fun CupertinoIcon(
    iconCode: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null,
) {
    val fontFamily = rememberCupertinoIconsFontFamily()
    val density = LocalDensity.current
    val fontSize = with(density) { size.toSp() }
    val iconChar = remember(iconCode) { Char(iconCode).toString() }

    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .then(semanticsModifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iconChar,
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = fontSize,
                lineHeight = fontSize,
                color = tint,
                textAlign = TextAlign.Center,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}

@Composable
private fun rememberCupertinoIconsFontFamily(): FontFamily {
    val font = Font(Res.font.sf_symbols)
    return remember(font) { FontFamily(font) }
}
