package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.navigation.IosBarEdgeInsets
import kotlin.test.Test
import kotlin.test.assertEquals

class SheetContentInsetsTest {

    @Test
    fun `a sheet clear of every unsafe edge pads nothing`() {
        // Open iPhone Duo, the system places the sheet in the middle of the window.
        assertEquals(IosBarEdgeInsets.Zero, sheetContentInsets(IosBarEdgeInsets(bottom = 34.dp)))
    }

    @Test
    fun `a sheet under the vertical bar keeps its content out of it`() {
        // Closed iPhone Duo: the sheet spans under the 84pt strip, UIKit reports 76pt of it.
        assertEquals(
            IosBarEdgeInsets(right = 76.dp),
            sheetContentInsets(IosBarEdgeInsets(right = 76.dp, bottom = 34.dp)),
        )
    }

    @Test
    fun `both sides are kept and the top and bottom are left to the content`() {
        // iPhone landscape: a full-screen sheet between the Dynamic Island and the home indicator.
        assertEquals(
            IosBarEdgeInsets(left = 62.dp, right = 62.dp),
            sheetContentInsets(IosBarEdgeInsets(left = 62.dp, top = 0.dp, right = 62.dp, bottom = 21.dp)),
        )
    }
}
