package com.mohamedrejeb.calf.ui.dialog

import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosAction
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle

/**
 * Resolves the label of the dismiss button shown by [AdaptiveAlertDialog].
 *
 * Both null and blank text mean "no dismiss button". A blank label would otherwise
 * render an empty but tappable button, which is never a valid dialog.
 */
internal fun dismissLabelOrNull(dismissText: String?): String? =
    dismissText?.takeIf { it.isNotBlank() }

/**
 * Builds the native iOS actions shown by [AdaptiveAlertDialog].
 *
 * The confirm action always comes first. The dismiss action is only added when
 * [dismissText] resolves to a label through [dismissLabelOrNull], which is how a
 * single-button dialog is expressed.
 */
internal fun adaptiveAlertDialogIosActions(
    confirmText: String,
    dismissText: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmStyle: AlertDialogIosActionStyle,
    dismissStyle: AlertDialogIosActionStyle,
    confirmIsPreferred: Boolean,
): List<AlertDialogIosAction> = listOfNotNull(
    AlertDialogIosAction(
        title = confirmText,
        style = confirmStyle,
        onClick = onConfirm,
        isPreferred = confirmIsPreferred,
    ),
    dismissLabelOrNull(dismissText)?.let { label ->
        AlertDialogIosAction(
            title = label,
            style = dismissStyle,
            onClick = onDismiss,
        )
    },
)
