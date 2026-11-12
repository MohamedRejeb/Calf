package com.mohamedrejeb.calf.foldable

import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.ui.geometry.Rect
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(CalfIosOnlyApi::class)
class FoldSnapshotTest {

    // What the Duo simulator reports for the inner display, scaled to pixels at density 1.
    private val duoDivision = FoldRegion(
        bounds = Rect(455.5f, 0f, 495.5f, 669f),
        margins = FoldMargins(left = 20f, right = 20f),
        isActive = false,
    )

    @Test
    fun `default snapshot has no fold and empty iOS values`() {
        val snapshot = FoldSnapshot()

        assertEquals(FoldPosture.NoFold, snapshot.posture)
        assertNull(snapshot.fold)
        assertEquals(false, snapshot.isSeparating)
        assertNull(snapshot.iosFoldRegion)
        assertEquals(emptyList(), snapshot.iosOcclusionRegions)
        assertEquals(IosSizeClass.Unspecified, snapshot.iosHorizontalSizeClass)
        assertEquals(IosSizeClass.Unspecified, snapshot.iosVerticalSizeClass)
        assertEquals(IosFoldCapabilities.Unavailable, snapshot.iosCapabilities)
        assertEquals(FoldSnapshot.NoFold, snapshot)
    }

    @Test
    fun `region bounds without margins are the crease itself`() {
        assertEquals(Rect(475.5f, 0f, 475.5f, 669f), duoDivision.reservedBounds)
    }

    @Test
    fun `ios division region becomes the shared fold`() {
        val flat = foldInfoOfIosRegion(duoDivision, FoldPosture.Flat)
        val halfOpen = foldInfoOfIosRegion(duoDivision, FoldPosture.HalfOpen)

        assertEquals(FoldInfo(Rect(475.5f, 0f, 475.5f, 669f), FoldOrientation.Vertical, isSeparating = false), flat)
        assertEquals(true, halfOpen?.isSeparating)
    }

    @Test
    fun `posture and not the region active flag decides whether the fold separates`() {
        // Regions lag the hinge: about a second after folding the region still says inactive.
        val staleRegion = duoDivision.copy(isActive = false)
        val earlyRegion = duoDivision.copy(isActive = true)

        assertEquals(true, foldInfoOfIosRegion(staleRegion, FoldPosture.HalfOpen)?.isSeparating)
        assertEquals(false, foldInfoOfIosRegion(earlyRegion, FoldPosture.Flat)?.isSeparating)
    }

    @Test
    fun `wide region is a horizontal fold`() {
        val region = FoldRegion(bounds = Rect(0f, 455.5f, 669f, 495.5f), margins = FoldMargins(top = 20f, bottom = 20f))

        assertEquals(FoldOrientation.Horizontal, foldInfoOfIosRegion(region, FoldPosture.Flat)?.orientation)
    }

    @Test
    fun `no region means no fold`() {
        assertNull(foldInfoOfIosRegion(null, FoldPosture.Flat))
    }

    @Test
    fun `snapshot without a fold is an empty window posture`() {
        assertEquals(Posture(), FoldSnapshot().toWindowPosture())
    }

    @Test
    fun `flat vertical fold becomes a flat vertical hinge`() {
        val fold = FoldInfo(Rect(475.5f, 0f, 475.5f, 669f), FoldOrientation.Vertical, isSeparating = false)

        val posture = FoldSnapshot(posture = FoldPosture.Flat, fold = fold).toWindowPosture()

        val hinge = HingeInfo(fold.bounds, isFlat = true, isVertical = true, isSeparating = false, isOccluding = false)
        assertEquals(Posture(isTabletop = false, hingeList = listOf(hinge)), posture)
    }

    @Test
    fun `half open horizontal fold is tabletop`() {
        val fold = FoldInfo(Rect(0f, 330f, 951f, 340f), FoldOrientation.Horizontal, isSeparating = true)

        val posture = FoldSnapshot(posture = FoldPosture.HalfOpen, fold = fold).toWindowPosture()

        assertEquals(true, posture.isTabletop)
        assertEquals(false, posture.hingeList.single().isFlat)
        assertEquals(true, posture.hingeList.single().isSeparating)
    }

    @Test
    fun `half open vertical fold is not tabletop`() {
        val fold = FoldInfo(Rect(475.5f, 0f, 475.5f, 669f), FoldOrientation.Vertical, isSeparating = true)

        assertEquals(false, FoldSnapshot(posture = FoldPosture.HalfOpen, fold = fold).toWindowPosture().isTabletop)
    }

    @Test
    fun `closed device reports no hinge to material3 adaptive`() {
        val fold = FoldInfo(Rect(475.5f, 0f, 475.5f, 669f), FoldOrientation.Vertical, isSeparating = false)

        assertEquals(Posture(), FoldSnapshot(posture = FoldPosture.Closed, fold = fold).toWindowPosture())
    }
}
