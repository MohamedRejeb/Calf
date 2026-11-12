package com.mohamedrejeb.calf.foldable

import androidx.compose.ui.geometry.Rect

// Layout of one entry returned by calf_foldable_reservedRegions (see foldable.def).
private const val INDEX_X = 0
private const val INDEX_Y = 1
private const val INDEX_WIDTH = 2
private const val INDEX_HEIGHT = 3
private const val INDEX_MARGIN_TOP = 4
private const val INDEX_MARGIN_LEFT = 5
private const val INDEX_MARGIN_BOTTOM = 6
private const val INDEX_MARGIN_RIGHT = 7
private const val INDEX_IS_ACTIVE = 8
private const val ENTRY_SIZE = 9

/**
 * Decodes one reserved region from the iOS bridge, converting points to pixels. The values come
 * from an OS API that is newer than anything Calf was compiled against, so an entry that does not
 * look as expected is dropped (`null`) instead of trusted.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
internal fun foldRegionOfIosValues(values: List<Double>, density: Float): FoldRegion? {
    if (values.size != ENTRY_SIZE || values.any { !it.isFinite() }) return null
    if (values[INDEX_WIDTH] < 0.0 || values[INDEX_HEIGHT] < 0.0) return null

    fun pixels(index: Int): Float = (values[index] * density).toFloat()

    val left = pixels(INDEX_X)
    val top = pixels(INDEX_Y)
    return FoldRegion(
        bounds = Rect(left, top, left + pixels(INDEX_WIDTH), top + pixels(INDEX_HEIGHT)),
        margins = FoldMargins(
            left = pixels(INDEX_MARGIN_LEFT),
            top = pixels(INDEX_MARGIN_TOP),
            right = pixels(INDEX_MARGIN_RIGHT),
            bottom = pixels(INDEX_MARGIN_BOTTOM),
        ),
        isActive = values[INDEX_IS_ACTIVE] != 0.0,
    )
}
