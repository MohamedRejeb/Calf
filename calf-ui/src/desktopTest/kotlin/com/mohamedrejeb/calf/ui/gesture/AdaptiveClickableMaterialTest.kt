package com.mohamedrejeb.calf.ui.gesture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AdaptiveClickableMaterialTest {

    @Test
    fun `invokes onClick when clicked`() = runComposeUiTest {
        var clickCount = 0

        setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .adaptiveClickable(onClick = { clickCount++ })
            ) {
                Text("Target")
            }
        }

        onNodeWithText("Target").performClick()

        assertEquals(1, clickCount)
    }

    @Test
    fun `invokes onLongClick when long clicked`() = runComposeUiTest {
        var clickCount = 0
        var longClickCount = 0

        setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .adaptiveClickable(
                        onLongClick = { longClickCount++ },
                        onClick = { clickCount++ },
                    )
            ) {
                Text("Target")
            }
        }

        onNodeWithText("Target").performTouchInput { longClick() }

        assertEquals(1, longClickCount)
        assertEquals(0, clickCount)
    }

    @Test
    fun `does not invoke onClick when disabled`() = runComposeUiTest {
        var clickCount = 0

        setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .adaptiveClickable(
                        enabled = false,
                        onClick = { clickCount++ },
                    )
            ) {
                Text("Target")
            }
        }

        onNodeWithText("Target").performClick()

        assertEquals(0, clickCount)
    }

    @Test
    fun `when onLongClick is null, invoke onClick when long clicked`() = runComposeUiTest {
        var clickCount = 0

        setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .adaptiveClickable(
                        onLongClick = null,
                        onClick = { clickCount++ },
                    )
            ) {
                Text("Target")
            }
        }

        onNodeWithText("Target").performTouchInput { longClick() }

        assertEquals(1, clickCount)
    }
}
