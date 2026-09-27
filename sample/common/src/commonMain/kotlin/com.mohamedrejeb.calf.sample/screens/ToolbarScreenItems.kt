package com.mohamedrejeb.calf.sample.screens

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/** Width of the fixed spaces of [ToolbarSpacing.FixedSpace], in points. */
private const val FIXED_SPACE_WIDTH = 32.0

/** The formatting the toolbar toggles on the draft. */
@Immutable
internal data class DraftFormat(
    val bold: Boolean = false,
    val italic: Boolean = true,
    val underline: Boolean = false,
) {
    val description: String
        get() = listOfNotNull(
            "Bold".takeIf { bold },
            "Italic".takeIf { italic },
            "Underline".takeIf { underline },
        ).joinToString().ifEmpty { "Plain" }
}

/** What the toolbar buttons do, shared by the Material content and the iOS items. */
internal class DraftActions(
    val toggleBold: () -> Unit,
    val toggleItalic: () -> Unit,
    val toggleUnderline: () -> Unit,
    val share: () -> Unit,
)

/**
 * Ways to arrange the native iOS toolbar items with flexible and fixed spaces. Material's
 * floating toolbar lays out its own buttons, so these only change the iOS toolbar.
 */
internal enum class ToolbarSpacing(val title: String, val subtitle: String) {
    Default("Default", "No spaces between the items"),
    FlexibleSpace("Flexible space", "Share pushed to the far end"),
    FixedSpace("Fixed space", "32 pt between the items"),
    EvenlySpaced("Evenly spaced", "A flexible space around every item"),
    LeadingGroup("Two groups", "Bold and italic at the start, the rest at the end"),
    Spread("Spread", "A flexible space between the items"),
    ;

    /** Places [items] (bold, italic, underline, share) with this pattern's spaces. */
    fun arrange(items: List<UIKitUIBarButtonItem>): List<UIKitUIBarButtonItem> = when (this) {
        Default -> items
        FlexibleSpace -> items.dropLast(1) + UIKitUIBarButtonItem.flexibleSpace() + items.last()
        FixedSpace -> items.joinedBy { UIKitUIBarButtonItem.fixedSpace(FIXED_SPACE_WIDTH) }
        EvenlySpaced -> listOf(UIKitUIBarButtonItem.flexibleSpace()) +
            items.joinedBy { UIKitUIBarButtonItem.flexibleSpace() } +
            UIKitUIBarButtonItem.flexibleSpace()
        LeadingGroup -> items.take(2) + UIKitUIBarButtonItem.flexibleSpace() + items.drop(2)
        Spread -> items.joinedBy { UIKitUIBarButtonItem.flexibleSpace() }
    }
}

/** The draft's toolbar as native UIBarButtonItems: SF Symbols, selected while a style is on. */
internal fun draftToolbarItems(
    format: DraftFormat,
    actions: DraftActions,
    spacing: ToolbarSpacing,
): List<UIKitUIBarButtonItem> {
    val items = listOf(
        symbolItem(SFSymbol.bold, "Bold", actions.toggleBold).copy(selected = format.bold),
        symbolItem(SFSymbol.italic, "Italic", actions.toggleItalic).copy(selected = format.italic),
        symbolItem(SFSymbol.underline, "Underline", actions.toggleUnderline).copy(selected = format.underline),
        symbolItem(SFSymbol.squareAndArrowUp, "Share", actions.share),
    )
    return spacing.arrange(items)
}

private fun symbolItem(symbol: String, label: String, onClick: () -> Unit) =
    UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName(symbol),
        label = label,
        onClick = onClick,
    )

/** `[a, b, c]` becomes `[a, space, b, space, c]`, with a new space item each time. */
private fun List<UIKitUIBarButtonItem>.joinedBy(
    space: () -> UIKitUIBarButtonItem,
): List<UIKitUIBarButtonItem> = flatMapIndexed { index, item ->
    if (index == 0) listOf(item) else listOf(space(), item)
}
