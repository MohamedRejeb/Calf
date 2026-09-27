package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.SheetHeader
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.coroutines.delay

/** How long "Link copied" shows before the share sheet closes itself. */
private const val COPIED_FEEDBACK_MILLIS = 900L
private val ButtonIconSize = 18.dp

/** The alerts of the Showcase. Each one is a `UIAlertController` on iOS. */
internal enum class ShowcaseDialog { Export, Leave, About, Source }

/** Shows [dialog] as an adaptive alert; every button closes it. */
@Composable
internal fun ShowcaseDialogHost(
    dialog: ShowcaseDialog,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        // One button: an alert that only informs.
        ShowcaseDialog.Export -> AdaptiveAlertDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
            confirmText = "Done",
            title = "Itinerary exported",
            text = "Lisbon-weekend.pdf, 3 days and 9 stops, is in your Downloads folder.",
        )
        // A destructive confirm: red on iOS, with a regular Cancel next to it.
        ShowcaseDialog.Leave -> AdaptiveAlertDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
            confirmText = "Leave",
            dismissText = "Cancel",
            title = "Leave $TRIP_NAME?",
            text = "You will lose access to the plan and the shared budget. Inès can invite you again.",
            iosConfirmButtonStyle = AlertDialogIosActionStyle.Destructive,
            iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
        )
        ShowcaseDialog.About -> AdaptiveAlertDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
            confirmText = "OK",
            title = "About this demo",
            text = "Every control on this screen is a Calf adaptive component: UIKit on iOS, " +
                "Material 3 on Android, desktop and the web.",
            iosConfirmButtonIsPreferred = true,
        )
        ShowcaseDialog.Source -> AdaptiveAlertDialog(
            onConfirm = onDismiss,
            onDismiss = onDismiss,
            confirmText = "OK",
            dismissText = "Cancel",
            title = "View source",
            text = "This screen lives in the sample app of $CALF_SOURCE_URL.",
            iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
        )
    }
}

/**
 * The share sheet: the invite link, who may edit, and a button that copies the link and closes
 * the sheet through its state, so the native sheet animates out before it leaves composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareTripSheet(onDismiss: () -> Unit) {
    val sheetState = rememberAdaptiveSheetState()
    var allowEditing by rememberSaveable { mutableStateOf(true) }
    var copied by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(COPIED_FEEDBACK_MILLIS)
            sheetState.hide()
            onDismiss()
        }
    }

    AdaptiveBottomSheet(
        onDismissRequest = onDismiss,
        adaptiveSheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.pageMargin)
                .padding(bottom = Spacing.xl),
        ) {
            SheetHeader(
                title = "Share trip",
                subtitle = "$TRIP_NAME · ${TripTravellers.size} travellers",
            )
            DemoListCard {
                DemoRow(
                    title = "Invite link",
                    subtitle = TRIP_INVITE_LINK,
                    leading = { IconTile(icon = Icons.Outlined.Link, color = TileBlue) },
                )
                DemoDivider(startIndent = DividerIndentWithIcon)
                DemoRow(
                    title = "Allow editing",
                    subtitle = if (allowEditing) "Travellers can change dates and budget" else "View only",
                    leading = { IconTile(icon = Icons.Outlined.EditNote, color = TileViolet) },
                    trailing = { AdaptiveSwitch(checked = allowEditing, onCheckedChange = { allowEditing = it }) },
                )
            }
            AdaptiveButton(
                onClick = { copied = true },
                enabled = !copied,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonIconSize),
                )
                Spacer(Modifier.width(Spacing.s))
                Text(if (copied) "Link copied" else "Copy link")
            }
        }
    }
}
