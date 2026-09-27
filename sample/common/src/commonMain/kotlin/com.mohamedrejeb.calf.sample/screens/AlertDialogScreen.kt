package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle

private const val USAGE = """
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    AdaptiveAlertDialog(
        onConfirm = { deleteDraft(); showDialog = false },
        onDismiss = { showDialog = false },
        confirmText = "Delete",
        dismissText = "Cancel", // leave out for a single button
        title = "Delete this draft?",
        text = "This can't be undone.",
        iosConfirmButtonStyle = AlertDialogIosActionStyle.Destructive,
        iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
    )
}
"""

private const val NO_RESULT = "None yet"

/** Every dialog the page can open. Saveable, so an open dialog survives a configuration change. */
internal enum class DialogDemo {
    DeleteDraft,
    SignOut,
    BackupDone,
    Rename,
    RemoveAccount,
    PhotoActions,
}

@Composable
fun AlertDialogScreen(navigateBack: () -> Unit) {
    var openDialog by rememberSaveable { mutableStateOf<DialogDemo?>(null) }
    var lastResult by rememberSaveable { mutableStateOf(NO_RESULT) }
    val onResult: (String) -> Unit = { result ->
        lastResult = result
        openDialog = null
    }

    SampleScreenScaffold(title = Screen.Dialog.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.Dialog)
            ConfirmationsSection(onOpen = { openDialog = it })
            UIKitOptionsSection(onOpen = { openDialog = it })
            DemoSection(title = "Result") {
                DemoCard {
                    ValueRow(label = "Last choice", value = lastResult)
                }
            }
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }

    when (openDialog) {
        DialogDemo.DeleteDraft -> DeleteDraftDialog(onResult)
        DialogDemo.SignOut -> SignOutDialog(onResult)
        DialogDemo.BackupDone -> BackupDoneDialog(onResult)
        DialogDemo.Rename -> RenameFileDialog(onResult)
        DialogDemo.RemoveAccount -> RemoveAccountDialog(onResult)
        DialogDemo.PhotoActions -> PhotoActionSheet(onResult)
        null -> Unit
    }
}

@Composable
private fun ConfirmationsSection(onOpen: (DialogDemo) -> Unit) {
    DemoSection(
        title = "AdaptiveAlertDialog",
        footer = "One or two buttons. The iOS button styles make Delete red and Cancel bold, like the system does.",
    ) {
        DemoListCard {
            DialogRow(
                title = "Delete draft",
                subtitle = "Destructive confirm and a Cancel button",
                icon = Icons.Outlined.Delete,
                tint = TileCoral,
                onClick = { onOpen(DialogDemo.DeleteDraft) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DialogRow(
                title = "Sign out",
                subtitle = "Preferred action, the Return key picks it",
                icon = Icons.AutoMirrored.Outlined.Logout,
                tint = TileIndigo,
                onClick = { onOpen(DialogDemo.SignOut) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DialogRow(
                title = "Backup complete",
                subtitle = "A single OK button, no dismissText",
                icon = Icons.Outlined.CloudDone,
                tint = TileTeal,
                onClick = { onOpen(DialogDemo.BackupDone) },
            )
        }
    }
}

@Composable
private fun UIKitOptionsSection(onOpen: (DialogDemo) -> Unit) {
    DemoSection(
        title = "AdaptiveBasicAlertDialog",
        footer = "The full UIAlertController setup on iOS: text fields, keyboards, action styles, severity " +
            "and the action sheet style. On other platforms it shows the Material content you pass.",
    ) {
        DemoListCard {
            DialogRow(
                title = "Rename file",
                subtitle = "Text field, Rename is off until you type",
                icon = Icons.Outlined.DriveFileRenameOutline,
                tint = TileBlue,
                onClick = { onOpen(DialogDemo.Rename) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DialogRow(
                title = "Remove work account",
                subtitle = "Email and passcode fields, critical severity",
                icon = Icons.Outlined.PersonRemove,
                tint = TileAmber,
                onClick = { onOpen(DialogDemo.RemoveAccount) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DialogRow(
                title = "Photo options",
                subtitle = "Action sheet with a disabled action",
                icon = Icons.Outlined.PhotoLibrary,
                tint = TileViolet,
                onClick = { onOpen(DialogDemo.PhotoActions) },
            )
        }
    }
}

@Composable
private fun DialogRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    DemoRow(
        title = title,
        subtitle = subtitle,
        leading = { IconTile(icon = icon, color = tint) },
        onClick = onClick,
    )
}

@Composable
private fun DeleteDraftDialog(onResult: (String) -> Unit) {
    AdaptiveAlertDialog(
        onConfirm = { onResult("Draft deleted") },
        onDismiss = { onResult("Draft kept") },
        confirmText = "Delete",
        dismissText = "Cancel",
        title = "Delete this draft?",
        text = "“Weekend in Porto” will be deleted from all your devices. This can't be undone.",
        // Material: the same destructive meaning, through the Material slots.
        materialIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
        materialConfirmButton = {
            TextButton(
                onClick = { onResult("Draft deleted") },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Delete")
            }
        },
        iosConfirmButtonStyle = AlertDialogIosActionStyle.Destructive,
        iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
    )
}

@Composable
private fun SignOutDialog(onResult: (String) -> Unit) {
    AdaptiveAlertDialog(
        onConfirm = { onResult("Signed out") },
        onDismiss = { onResult("Stayed signed in") },
        confirmText = "Sign Out",
        dismissText = "Cancel",
        title = "Sign out?",
        text = "Your notes stay on this device. Sign in again to sync them.",
        iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
        iosConfirmButtonIsPreferred = true,
    )
}

@Composable
private fun BackupDoneDialog(onResult: (String) -> Unit) {
    AdaptiveAlertDialog(
        onConfirm = { onResult("Backup notice closed") },
        onDismiss = { onResult("Backup notice closed") },
        confirmText = "OK",
        title = "Backup complete",
        text = "248 photos and 12 videos are backed up.",
    )
}
