package com.mohamedrejeb.calf.foldable

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class HingeAngleTest {

    @Test
    fun `radians convert to degrees`() {
        assertEquals(0f, hingeDegreesOf(0.0))
        assertEquals(90f, hingeDegreesOf(PI / 2))
        assertEquals(180f, hingeDegreesOf(PI))
    }

    @Test
    fun `simulator reading converts with float precision`() {
        assertEquals(127.77778f, hingeDegreesOf(2.2301429478992048), absoluteTolerance = 0.0001f)
    }

    @Test
    fun `angle is clamped to what a hinge can do`() {
        assertEquals(0f, hingeDegreesOf(-0.2))
        assertEquals(180f, hingeDegreesOf(PI + 0.2))
        assertEquals(180f, hingeDegreesOf(Double.POSITIVE_INFINITY))
    }

    @Test
    fun `not a number reads as shut`() {
        assertEquals(0f, hingeDegreesOf(Double.NaN))
    }
}
