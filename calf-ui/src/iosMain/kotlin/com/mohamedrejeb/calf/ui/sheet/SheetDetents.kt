package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UISheetPresentationControllerDetentIdentifierLarge
import platform.UIKit.UISheetPresentationControllerDetentIdentifierMedium

/**
 * How a [SheetValue] maps onto a `UISheetPresentationController` detent: [SheetValue.Expanded] is
 * the large detent, [SheetValue.PartiallyExpanded] the medium one, and [SheetValue.Hidden] a sheet
 * that is not presented, so it has no detent.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun SheetValue.toDetentIdentifier(): String? = when (this) {
    SheetValue.Expanded -> UISheetPresentationControllerDetentIdentifierLarge
    SheetValue.PartiallyExpanded -> UISheetPresentationControllerDetentIdentifierMedium
    SheetValue.Hidden -> null
}

/** The detent a visible [SheetValue] rests at; [SheetValue.Hidden] has none. */
@OptIn(ExperimentalMaterial3Api::class)
internal fun SheetValue.toDetent(): UISheetPresentationControllerDetent = when (this) {
    SheetValue.Expanded -> UISheetPresentationControllerDetent.largeDetent()
    SheetValue.PartiallyExpanded -> UISheetPresentationControllerDetent.mediumDetent()
    SheetValue.Hidden -> error("A hidden sheet has no detent.")
}

/**
 * The [SheetValue] of a selected detent identifier. UIKit keeps `selectedDetentIdentifier` nil
 * until the user or the app picks a detent, and rests the sheet at the first detent of its list
 * meanwhile: the medium one when the sheet has a partially expanded state, else the large one.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun sheetValueOfDetent(
    identifier: String?,
    hasPartiallyExpandedState: Boolean = true,
): SheetValue = when (identifier) {
    UISheetPresentationControllerDetentIdentifierMedium -> SheetValue.PartiallyExpanded
    UISheetPresentationControllerDetentIdentifierLarge -> SheetValue.Expanded
    else -> if (hasPartiallyExpandedState) SheetValue.PartiallyExpanded else SheetValue.Expanded
}

/** The detents a sheet offers, smallest first as UIKit requires. */
@OptIn(ExperimentalMaterial3Api::class)
internal fun sheetDetentValues(hasPartiallyExpandedState: Boolean): List<SheetValue> =
    if (hasPartiallyExpandedState) listOf(SheetValue.PartiallyExpanded, SheetValue.Expanded)
    else listOf(SheetValue.Expanded)
