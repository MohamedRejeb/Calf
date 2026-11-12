package com.mohamedrejeb.calf.sample.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveButton

/**
 * The top of a bottom sheet's content: what the sheet is for, a line of context under it and,
 * with [onDone], a Done button that closes the sheet. The sheet draws no title bar of its own.
 */
@Composable
fun SheetHeader(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onDone: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = Spacing.xs),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = Spacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (onDone != null) {
            AdaptiveButton(onClick = onDone) {
                Text("Done")
            }
        }
    }
}
