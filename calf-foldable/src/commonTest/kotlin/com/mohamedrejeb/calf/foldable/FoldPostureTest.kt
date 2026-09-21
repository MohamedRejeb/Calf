package com.mohamedrejeb.calf.foldable

import kotlin.test.Test
import kotlin.test.assertEquals

class FoldPostureTest {

    @Test
    fun `ios hinge status raw values map to postures`() {
        assertEquals(FoldPosture.NoFold, foldPostureOfIosHingeStatus(0))
        assertEquals(FoldPosture.Closed, foldPostureOfIosHingeStatus(1))
        assertEquals(FoldPosture.HalfOpen, foldPostureOfIosHingeStatus(2))
        assertEquals(FoldPosture.Flat, foldPostureOfIosHingeStatus(3))
    }

    @Test
    fun `ios hinge status from a newer OS is treated as no fold`() {
        assertEquals(FoldPosture.NoFold, foldPostureOfIosHingeStatus(4))
        assertEquals(FoldPosture.NoFold, foldPostureOfIosHingeStatus(-1))
    }

    @Test
    fun `window without a folding feature has no fold`() {
        assertEquals(FoldPosture.NoFold, foldPostureOfFoldingFeature(hasFoldingFeature = false, isHalfOpened = false))
        assertEquals(FoldPosture.NoFold, foldPostureOfFoldingFeature(hasFoldingFeature = false, isHalfOpened = true))
    }

    @Test
    fun `folding feature is half open or flat`() {
        assertEquals(FoldPosture.HalfOpen, foldPostureOfFoldingFeature(hasFoldingFeature = true, isHalfOpened = true))
        assertEquals(FoldPosture.Flat, foldPostureOfFoldingFeature(hasFoldingFeature = true, isHalfOpened = false))
    }

    @Test
    fun `only an open device can show two panes`() {
        assertEquals(false, FoldPosture.NoFold.isOpen)
        assertEquals(false, FoldPosture.Closed.isOpen)
        assertEquals(true, FoldPosture.HalfOpen.isOpen)
        assertEquals(true, FoldPosture.Flat.isOpen)
    }
}
