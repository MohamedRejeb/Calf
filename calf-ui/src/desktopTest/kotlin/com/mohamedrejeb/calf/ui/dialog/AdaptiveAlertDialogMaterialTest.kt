package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AdaptiveAlertDialogMaterialTest {

    private val isButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    @Test
    fun `shows only the confirm button when dismissText is null`() = runComposeUiTest {
        setContent {
            AdaptiveAlertDialog(
                onConfirm = {},
                onDismiss = {},
                confirmText = "OK",
                dismissText = null,
                title = "Title",
                text = "Message",
            )
        }

        onNodeWithText("OK").assertIsDisplayed()
        onAllNodes(isButton).assertCountEquals(1)
    }

    @Test
    fun `shows only the confirm button when dismissText is blank`() = runComposeUiTest {
        setContent {
            AdaptiveAlertDialog(
                onConfirm = {},
                onDismiss = {},
                confirmText = "OK",
                dismissText = "   ",
                title = "Title",
                text = "Message",
            )
        }

        onNodeWithText("OK").assertIsDisplayed()
        onAllNodes(isButton).assertCountEquals(1)
    }

    @Test
    fun `renders materialDismissButton even when dismissText is null`() = runComposeUiTest {
        setContent {
            AdaptiveAlertDialog(
                onConfirm = {},
                onDismiss = {},
                confirmText = "OK",
                dismissText = null,
                title = "Title",
                text = "Message",
                materialDismissButton = {
                    TextButton(onClick = {}) {
                        Text("Custom")
                    }
                },
            )
        }

        onNodeWithText("Custom").assertIsDisplayed()
        onAllNodes(isButton).assertCountEquals(2)
    }

    @Test
    fun `shows confirm and dismiss buttons when dismissText is provided`() = runComposeUiTest {
        setContent {
            AdaptiveAlertDialog(
                onConfirm = {},
                onDismiss = {},
                confirmText = "OK",
                dismissText = "Cancel",
                title = "Title",
                text = "Message",
            )
        }

        onNodeWithText("OK").assertIsDisplayed()
        onNodeWithText("Cancel").assertIsDisplayed()
        onAllNodes(isButton).assertCountEquals(2)
    }
}
