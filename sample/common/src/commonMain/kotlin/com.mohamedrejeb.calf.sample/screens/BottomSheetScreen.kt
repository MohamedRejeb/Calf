package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.rememberFoldState
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.components.tabletopPanes
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.coroutines.launch

private const val USAGE = """
val sheetState = rememberAdaptiveSheetState()
var showSheet by remember { mutableStateOf(false) }

if (showSheet) {
    AdaptiveBottomSheet(
        onDismissRequest = { showSheet = false },
        adaptiveSheetState = sheetState,
        sheetGesturesEnabled = !locked,
    ) {
        ShareOptions(onFullHeight = { scope.launch { sheetState.expand() } })
    }
}
"""

private const val NO_ACTION = "None yet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScreen(navigateBack: () -> Unit) {
    var allowHide by rememberSaveable { mutableStateOf(true) }
    var fullHeight by rememberSaveable { mutableStateOf(false) }
    var locked by rememberSaveable { mutableStateOf(false) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    var lastAction by rememberSaveable { mutableStateOf(NO_ACTION) }
    val scope = rememberCoroutineScope()
    // Swipe to close off: the sheet refuses to hide, only Done closes it.
    val sheetState = rememberAdaptiveSheetState(
        skipPartiallyExpanded = fullHeight,
        confirmValueChange = { it != SheetValue.Hidden || allowHide },
    )
    val sheetTopInset = tabletopSheetTopInset()

    SampleScreenScaffold(title = Screen.BottomSheet.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.BottomSheet)
            AlbumSection(onShare = { openBottomSheet = true })
            OptionsSection(
                fullHeight = fullHeight,
                onFullHeightChange = { fullHeight = it },
                locked = locked,
                onLockedChange = { locked = it },
                allowHide = allowHide,
                onAllowHideChange = { allowHide = it },
            )
            DemoSection(title = "Result") {
                DemoCard {
                    ValueRow(label = "Last action", value = lastAction)
                    DemoDivider()
                    ValueRow(label = "Sheet value", value = sheetState.currentValue.name)
                }
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }

    if (openBottomSheet) {
        AdaptiveBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            modifier = Modifier.padding(top = sheetTopInset),
            adaptiveSheetState = sheetState,
            sheetGesturesEnabled = !locked,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            ShareSheetContent(
                allowHide = allowHide,
                onAllowHideChange = { allowHide = it },
                heightToggle = sheetHeightToggle(
                    isExpanded = sheetState.currentValue == SheetValue.Expanded,
                    canToggle = sheetState.hasPartiallyExpandedState,
                    onToggle = {
                        scope.launch {
                            if (sheetState.currentValue == SheetValue.Expanded) sheetState.partialExpand()
                            else sheetState.expand()
                        }
                    },
                ),
                onAction = { lastAction = it },
                onDone = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            openBottomSheet = false
                        }
                    }
                },
            )
        }
    }
}

/**
 * The top padding that keeps the Material sheet below the fold in tabletop (half open, fold
 * across). A top padding, not a height cap: the sheet computes its anchors from the space it is
 * given. iOS is left alone: the modifier would only pad the content of the native sheet, which
 * UIKit places.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
private fun tabletopSheetTopInset(): Dp {
    val foldState = rememberFoldState()
    val windowSize = LocalWindowInfo.current.containerSize.toSize()
    val lowerPane = tabletopPanes(foldState.posture, foldState.calculatePanes(windowSize))?.second
    return lowerPane
        ?.takeUnless { currentPlatform.isIOS }
        ?.let { with(LocalDensity.current) { it.top.toDp() } }
        ?: 0.dp
}

@Composable
private fun AlbumSection(onShare: () -> Unit) {
    DemoSection(
        title = "Album",
        footer = "On iOS the sheet opens at half height (the medium detent); drag it up for the whole list.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Lisbon, May 2026",
                subtitle = "42 photos",
                leading = { IconTile(icon = Icons.Outlined.PhotoLibrary, color = TileTeal) },
                trailing = {
                    AdaptiveButton(onClick = onShare) {
                        Text("Share")
                    }
                },
            )
        }
    }
}

@Composable
private fun OptionsSection(
    fullHeight: Boolean,
    onFullHeightChange: (Boolean) -> Unit,
    locked: Boolean,
    onLockedChange: (Boolean) -> Unit,
    allowHide: Boolean,
    onAllowHideChange: (Boolean) -> Unit,
) {
    DemoSection(
        title = "Options",
        footer = "A locked sheet cannot be dragged down, only Done closes it; on iOS it loses its grabber. " +
            "With swipe to close off, confirmValueChange refuses SheetValue.Hidden. On a foldable in " +
            "tabletop posture, the Material sheet stays below the fold.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Open at full height",
                subtitle = "skipPartiallyExpanded",
                leading = { IconTile(icon = Icons.Outlined.UnfoldMore, color = TileIndigo) },
                trailing = { AdaptiveSwitch(checked = fullHeight, onCheckedChange = onFullHeightChange) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = "Lock the sheet",
                subtitle = "sheetGesturesEnabled = false, no drag to dismiss",
                leading = { IconTile(icon = Icons.Outlined.Lock, color = TileCoral) },
                trailing = { AdaptiveSwitch(checked = locked, onCheckedChange = onLockedChange) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            SwipeToCloseRow(allowHide = allowHide, onAllowHideChange = onAllowHideChange)
        }
    }
}

/** Shared by the page and the sheet: flip it while the sheet is open to feel the difference. */
@Composable
internal fun SwipeToCloseRow(allowHide: Boolean, onAllowHideChange: (Boolean) -> Unit) {
    DemoRow(
        title = "Swipe down to close",
        leading = { IconTile(icon = Icons.Outlined.SwipeDown, color = TileAmber) },
        trailing = { AdaptiveSwitch(checked = allowHide, onCheckedChange = onAllowHideChange) },
    )
}
