package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.navigation.internal.PaneRoleKind

/**
 * The place of an entry in a list-detail layout. Pass it to [adaptiveNavEntry]; it is only read by
 * a display that was given `listDetail` settings.
 *
 * List and detail are ordinary entries of one back stack. With room for two panes they are shown
 * side by side, without it the detail is a normal push over the list. The back stack is the same
 * in both cases, so a change of layout (opening a foldable, rotating a tablet, resizing a window)
 * re-arranges the screen and loses nothing.
 */
@ExperimentalCalfNavigationApi
@Immutable
sealed interface AdaptivePaneRole {

    /** An ordinary entry. It belongs to whichever side it was pushed on. */
    data object None : AdaptivePaneRole

    /**
     * The list pane.
     *
     * @property detailPlaceholder fills the detail pane while two panes are shown and no detail
     *   is open ("Select a conversation"). It is not an entry: it has no bar and is never on the
     *   back stack.
     */
    class List(
        val detailPlaceholder: @Composable () -> Unit = {},
    ) : AdaptivePaneRole

    /**
     * The first entry of the detail pane. Entries pushed after it stay in the detail pane without
     * being marked, so a detail can push its own screens.
     */
    data object Detail : AdaptivePaneRole
}

@OptIn(ExperimentalCalfNavigationApi::class)
internal val AdaptivePaneRole.kind: PaneRoleKind
    get() = when (this) {
        AdaptivePaneRole.None -> PaneRoleKind.None
        is AdaptivePaneRole.List -> PaneRoleKind.List
        AdaptivePaneRole.Detail -> PaneRoleKind.Detail
    }
