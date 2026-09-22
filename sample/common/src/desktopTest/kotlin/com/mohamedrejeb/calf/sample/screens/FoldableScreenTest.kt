package com.mohamedrejeb.calf.sample.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldInfo
import com.mohamedrejeb.calf.foldable.FoldOrientation
import com.mohamedrejeb.calf.foldable.FoldPosture
import com.mohamedrejeb.calf.foldable.FoldSnapshot
import com.mohamedrejeb.calf.foldable.FoldState
import com.mohamedrejeb.calf.foldable.LocalFoldStateOverride
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme
import kotlin.test.Test

/** The Foldable demo's tabletop layout, driven by calf-foldable's fake state. One dp is one pixel. */
@OptIn(ExperimentalTestApi::class, ExperimentalCalfFoldableApi::class)
class FoldableScreenTest {

    private object Window : WindowInfo {
        override val isWindowFocused: Boolean = true
        override val containerSize: IntSize = IntSize(WIDTH, HEIGHT)
    }

    private fun foldAcross(posture: FoldPosture) = FoldState(
        FoldSnapshot(
            posture = posture,
            fold = FoldInfo(
                bounds = Rect(0f, HEIGHT / 2f, WIDTH.toFloat(), HEIGHT / 2f),
                orientation = FoldOrientation.Horizontal,
                isSeparating = posture == FoldPosture.HalfOpen,
            ),
        ),
    )

    @Test
    fun `half open with the fold across shows the tabletop layout`() = runComposeUiTest {
        setContent { Foldable(foldAcross(FoldPosture.HalfOpen)) }
        waitForIdle()

        onNodeWithText(TABLETOP_HINT).assertIsDisplayed()
        onNodeWithText("posture: HalfOpen").assertIsDisplayed()
    }

    @Test
    fun `a flat device keeps the single layout`() = runComposeUiTest {
        setContent { Foldable(foldAcross(FoldPosture.Flat)) }
        waitForIdle()

        onNodeWithText(TABLETOP_HINT).assertDoesNotExist()
        onNodeWithText("posture: Flat").assertIsDisplayed()
    }

    @Composable
    private fun Foldable(state: FoldState) {
        CompositionLocalProvider(
            LocalWindowInfo provides Window,
            LocalDensity provides Density(1f),
            LocalFoldStateOverride provides state,
        ) {
            CalfTheme { FoldableScreen(navigateBack = {}) }
        }
    }

    private companion object {
        const val WIDTH = 800
        const val HEIGHT = 800
        const val TABLETOP_HINT = "Tabletop: the values stay above the fold, these controls below it."
    }
}
