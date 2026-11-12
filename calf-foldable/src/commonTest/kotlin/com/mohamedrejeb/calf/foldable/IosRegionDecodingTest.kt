package com.mohamedrejeb.calf.foldable

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IosRegionDecodingTest {

    // The Duo's division region as the simulator reports it, in points.
    private val duoDivision = listOf(455.5, 0.0, 40.0, 669.0, 0.0, 20.0, 0.0, 20.0, 0.0)

    @Test
    fun `region values decode in UIKit order`() {
        val region = foldRegionOfIosValues(duoDivision, density = 1f)

        assertEquals(
            FoldRegion(
                bounds = Rect(455.5f, 0f, 495.5f, 669f),
                margins = FoldMargins(left = 20f, top = 0f, right = 20f, bottom = 0f),
                isActive = false,
            ),
            region,
        )
    }

    @Test
    fun `points are scaled to pixels`() {
        val region = foldRegionOfIosValues(listOf(10.0, 20.0, 30.0, 40.0, 1.0, 2.0, 3.0, 4.0, 1.0), density = 3f)

        assertEquals(Rect(30f, 60f, 120f, 180f), region?.bounds)
        assertEquals(FoldMargins(left = 6f, top = 3f, right = 12f, bottom = 9f), region?.margins)
        assertEquals(true, region?.isActive)
    }

    @Test
    fun `entry of the wrong length is dropped`() {
        assertNull(foldRegionOfIosValues(duoDivision.dropLast(1), density = 1f))
        assertNull(foldRegionOfIosValues(duoDivision + 1.0, density = 1f))
        assertNull(foldRegionOfIosValues(emptyList(), density = 1f))
    }

    @Test
    fun `entry with a value that is not a finite number is dropped`() {
        assertNull(foldRegionOfIosValues(duoDivision.toMutableList().also { it[2] = Double.NaN }, density = 1f))
        assertNull(foldRegionOfIosValues(duoDivision.toMutableList().also { it[0] = Double.POSITIVE_INFINITY }, density = 1f))
    }

    @Test
    fun `entry with a negative size is dropped`() {
        assertNull(foldRegionOfIosValues(duoDivision.toMutableList().also { it[2] = -1.0 }, density = 1f))
    }
}
