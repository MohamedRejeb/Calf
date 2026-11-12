package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dialog.AdaptiveBasicAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosAction
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosProperties
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosSeverity
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosTextField
import com.mohamedrejeb.calf.ui.dialog.uikit.rememberAlertDialogIosProperties
import com.mohamedrejeb.calf.ui.uikit.IosKeyboardType

private const val FILE_NAME = "Q3 report.pdf"
private const val WORK_EMAIL = "maya@studio.dev"

// The dialogs of the AdaptiveBasicAlertDialog section. Each one describes the UIAlertController
// once (AlertDialogIosProperties) and draws the Material side from the same description, so both
// platforms offer the same fields and actions.

@OptIn(ExperimentalCalfUiApi::class)
@Composable
internal fun RenameFileDialog(onResult: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val properties = rememberAlertDialogIosProperties(
        title = "Rename file",
        text = "Choose a new name for “$FILE_NAME”.",
        actions = listOf(
            AlertDialogIosAction(
                title = "Rename",
                style = AlertDialogIosActionStyle.Default,
                isPreferred = true,
                // UIKit updates `enabled` on the presented alert, so Rename turns on as you type.
                enabled = name.isNotBlank(),
                onClick = { onResult("Renamed to “${name.trim()}”") },
            ),
            AlertDialogIosAction(
                title = "Cancel",
                style = AlertDialogIosActionStyle.Cancel,
                onClick = { onResult("Rename cancelled") },
            ),
        ),
        textFields = listOf(
            AlertDialogIosTextField(
                placeholder = "File name",
                onValueChange = { name = it },
            ),
        ),
    )
    AdaptiveBasicAlertDialog(
        onDismissRequest = { onResult("Rename cancelled") },
        iosProperties = properties,
        materialContent = { MaterialAlertContent(properties) },
    )
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
internal fun RemoveAccountDialog(onResult: (String) -> Unit) {
    var passcode by remember { mutableStateOf("") }
    val properties = rememberAlertDialogIosProperties(
        title = "Remove work account?",
        text = "Mail, contacts and calendars of this account are removed from this device.",
        // Only Mac Catalyst draws the caution icon; UIKit ignores severity on iPhone and iPad.
        severity = AlertDialogIosSeverity.Critical,
        actions = listOf(
            AlertDialogIosAction(
                title = "Remove Account",
                style = AlertDialogIosActionStyle.Destructive,
                enabled = passcode.isNotEmpty(),
                onClick = { onResult("Work account removed") },
            ),
            AlertDialogIosAction(
                title = "Sign Out Only",
                style = AlertDialogIosActionStyle.Default,
                onClick = { onResult("Signed out of $WORK_EMAIL") },
            ),
            AlertDialogIosAction(
                title = "Cancel",
                style = AlertDialogIosActionStyle.Cancel,
                onClick = { onResult("Account kept") },
            ),
        ),
        textFields = listOf(
            AlertDialogIosTextField(
                placeholder = "Email",
                initialValue = WORK_EMAIL,
                keyboardType = IosKeyboardType.EmailAddress,
                onValueChange = {},
            ),
            AlertDialogIosTextField(
                placeholder = "Device passcode",
                keyboardType = IosKeyboardType.NumberPad,
                isSecure = true,
                onValueChange = { passcode = it },
            ),
        ),
    )
    AdaptiveBasicAlertDialog(
        onDismissRequest = { onResult("Account kept") },
        iosProperties = properties,
        materialContent = { MaterialAlertContent(properties) },
    )
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
internal fun PhotoActionSheet(onResult: (String) -> Unit) {
    val properties = rememberAlertDialogIosProperties(
        title = "IMG_2041.HEIC",
        text = "Shared by Sara. Only she can delete it.",
        style = AlertDialogIosStyle.ActionSheet,
        actions = listOf(
            AlertDialogIosAction(
                title = "Save to Files",
                style = AlertDialogIosActionStyle.Default,
                onClick = { onResult("Photo saved to Files") },
            ),
            AlertDialogIosAction(
                title = "Duplicate",
                style = AlertDialogIosActionStyle.Default,
                onClick = { onResult("Photo duplicated") },
            ),
            AlertDialogIosAction(
                title = "Delete Photo",
                style = AlertDialogIosActionStyle.Destructive,
                enabled = false,
                onClick = { onResult("Photo deleted") },
            ),
            AlertDialogIosAction(
                title = "Cancel",
                style = AlertDialogIosActionStyle.Cancel,
                onClick = { onResult("Photo options closed") },
            ),
        ),
    )
    AdaptiveBasicAlertDialog(
        onDismissRequest = { onResult("Photo options closed") },
        iosProperties = properties,
        materialContent = { MaterialAlertContent(properties) },
    )
}

/**
 * The Material side of an [AdaptiveBasicAlertDialog], drawn from the same [properties] as the
 * UIAlertController: title, message, text fields and actions, with destructive actions in the
 * error colour and disabled ones greyed out.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialAlertContent(properties: AlertDialogIosProperties) {
    Surface(
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            modifier = Modifier.padding(Spacing.xl),
        ) {
            Text(
                text = properties.title,
                style = MaterialTheme.typography.headlineSmall,
                color = AlertDialogDefaults.titleContentColor,
            )
            if (properties.text.isNotEmpty()) {
                Text(
                    text = properties.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlertDialogDefaults.textContentColor,
                )
            }
            properties.textFields.forEach { field -> MaterialAlertTextField(field) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s, alignment = Alignment.End),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Material puts the dismissive action first, before the confirming one.
                properties.actions
                    .sortedByDescending { it.style == AlertDialogIosActionStyle.Cancel }
                    .forEach { action -> MaterialAlertAction(action) }
            }
        }
    }
}

@Composable
private fun MaterialAlertTextField(field: AlertDialogIosTextField) {
    var value by remember(field.placeholder) { mutableStateOf(field.initialValue) }
    OutlinedTextField(
        value = value,
        onValueChange = {
            value = it
            field.onValueChange(it)
        },
        label = { Text(field.placeholder) },
        singleLine = true,
        visualTransformation = if (field.isSecure) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType.toMaterial()),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MaterialAlertAction(action: AlertDialogIosAction) {
    val color = if (action.style == AlertDialogIosActionStyle.Destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    TextButton(
        onClick = action.onClick,
        enabled = action.enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
    ) {
        Text(action.title)
    }
}

private fun IosKeyboardType.toMaterial(): KeyboardType =
    when (this) {
        IosKeyboardType.EmailAddress -> KeyboardType.Email
        IosKeyboardType.NumberPad, IosKeyboardType.AsciiCapableNumberPad -> KeyboardType.NumberPassword
        IosKeyboardType.DecimalPad -> KeyboardType.Decimal
        IosKeyboardType.PhonePad, IosKeyboardType.NamePhonePad -> KeyboardType.Phone
        IosKeyboardType.URL -> KeyboardType.Uri
        else -> KeyboardType.Text
    }
