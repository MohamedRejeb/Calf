package com.mohamedrejeb.calf.ui.dialog

import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveAlertDialogActionsTest {

    @Test
    fun `builds only the confirm action when dismissText is null`() {
        val actions = adaptiveAlertDialogIosActions(
            confirmText = "OK",
            dismissText = null,
            onConfirm = {},
            onDismiss = {},
            confirmStyle = AlertDialogIosActionStyle.Default,
            dismissStyle = AlertDialogIosActionStyle.Cancel,
            confirmIsPreferred = false,
        )

        assertEquals(listOf("OK"), actions.map { it.title })
    }

    @Test
    fun `builds only the confirm action when dismissText is blank`() {
        val actions = adaptiveAlertDialogIosActions(
            confirmText = "OK",
            dismissText = "   ",
            onConfirm = {},
            onDismiss = {},
            confirmStyle = AlertDialogIosActionStyle.Default,
            dismissStyle = AlertDialogIosActionStyle.Cancel,
            confirmIsPreferred = false,
        )

        assertEquals(listOf("OK"), actions.map { it.title })
    }

    @Test
    fun `builds confirm then dismiss action when dismissText is provided`() {
        val actions = adaptiveAlertDialogIosActions(
            confirmText = "OK",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {},
            confirmStyle = AlertDialogIosActionStyle.Default,
            dismissStyle = AlertDialogIosActionStyle.Destructive,
            confirmIsPreferred = true,
        )

        assertEquals(listOf("OK", "Cancel"), actions.map { it.title })
        assertEquals(
            listOf(AlertDialogIosActionStyle.Default, AlertDialogIosActionStyle.Destructive),
            actions.map { it.style },
        )
        assertEquals(listOf(true, false), actions.map { it.isPreferred })
    }

    @Test
    fun `wires confirm and dismiss callbacks to their own actions`() {
        var confirmed = false
        var dismissed = false
        val actions = adaptiveAlertDialogIosActions(
            confirmText = "OK",
            dismissText = "Cancel",
            onConfirm = { confirmed = true },
            onDismiss = { dismissed = true },
            confirmStyle = AlertDialogIosActionStyle.Default,
            dismissStyle = AlertDialogIosActionStyle.Cancel,
            confirmIsPreferred = false,
        )

        actions[0].onClick()
        assertTrue(confirmed)
        assertFalse(dismissed)

        actions[1].onClick()
        assertTrue(dismissed)
    }
}
