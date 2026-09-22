package com.mohamedrejeb.calf.sample.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem

/**
 * The bar insets of the navigation entry a demo is shown in, when the navigator paints the bar.
 * Set by the sample's list-detail display for the demos whose bar is a plain title with a back
 * button; `null` when a demo draws its own bar.
 */
val LocalSampleEntryPadding: ProvidableCompositionLocal<PaddingValues?> = compositionLocalOf { null }

/**
 * The scaffold of a plain demo: a title bar with a back button, or, inside a navigation entry
 * that already paints that bar ([LocalSampleEntryPadding]), only the body.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
fun SampleScreenScaffold(
    title: String,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    iosTrailingItems: List<UIKitUIBarButtonItem> = emptyList(),
    content: @Composable (PaddingValues) -> Unit,
) {
    val entryPadding = LocalSampleEntryPadding.current
    if (entryPadding != null) {
        content(entryPadding)
    } else {
        SampleTopBarScaffold(
            title = title,
            navigateBack = navigateBack,
            modifier = modifier,
            actions = actions,
            iosTrailingItems = iosTrailingItems,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
private fun SampleTopBarScaffold(
    title: String,
    navigateBack: () -> Unit,
    modifier: Modifier,
    actions: @Composable RowScope.() -> Unit,
    iosTrailingItems: List<UIKitUIBarButtonItem>,
    content: @Composable (PaddingValues) -> Unit,
) {
    AdaptiveScaffold(
        modifier = modifier,
        topBar = {
            AdaptiveTopBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = actions,
                iosTitle = title,
                iosLeadingItems = listOf(
                    UIKitUIBarButtonItem(
                        title = "Back",
                        onClick = navigateBack,
                    )
                ),
                iosTrailingItems = iosTrailingItems,
            )
        },
        content = content,
    )
}
