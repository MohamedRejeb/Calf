package com.mohamedrejeb.calf.sample.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.sample.ui.theme.IconTileSize

private const val MAX_INITIALS = 2

/**
 * A person's initials on a tinted circle: the stand-in for a profile photo. The default [size] is
 * the one of an [IconTile], so the dividers of a list line up with [DividerIndentWithIcon].
 */
@Composable
fun InitialsAvatar(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = IconTileSize,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(color, CircleShape),
    ) {
        Text(text = initialsOf(name), style = textStyle, color = Color.White)
    }
}

/** "Inès Duarte" → "ID": the first letter of the first two words, in capitals. */
private fun initialsOf(name: String): String =
    name.split(' ')
        .filter { it.isNotBlank() }
        .take(MAX_INITIALS)
        .joinToString("") { it.first().uppercase() }
