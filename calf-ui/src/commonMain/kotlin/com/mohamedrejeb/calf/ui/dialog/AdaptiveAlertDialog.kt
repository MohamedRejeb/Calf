package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosProperties
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosStyle
import com.mohamedrejeb.calf.ui.dialog.uikit.rememberAlertDialogIosProperties

/**
 * Displays an adaptive alert dialog, which is a dialog that adapts to the platform it is running on.
 *
 * On iOS, it uses a native `UIAlertController` and on other platforms, it uses a material3 `AlertDialog`.
 *
 * Non-iOS platforms is a shortcut for Android, Desktop, and Web.
 *
 * The styling parameters are only used for non-iOS platforms.
 *
 * @param onConfirm Lambda that is invoked when the confirm button is clicked.
 * If materialConfirmButton is provided, this lambda will not be used for non-iOS platforms.
 * @param onDismiss Lambda that is invoked when the dismiss button is clicked.
 * If materialDismissButton is provided, this lambda will not be used for non-iOS platforms.
 * @param confirmText The text of the confirm button.
 * if materialConfirmButton is provided, this text will not be used for non-iOS platforms.
 * @param dismissText The text of the dismiss button.
 * if materialDismissButton is provided, this text will not be used for non-iOS platforms.
 * @param title The title of the dialog.
 * if materialTitle is provided, this text will not be used for non-iOS platforms.
 * @param text The text of the dialog.
 * if materialText is provided, this text will not be used for non-iOS platforms.
 * @param materialConfirmButton The composable that represents the confirm button for non-iOS platforms.
 * If not provided, a default button with the confirm text and the onConfirm lambda will be used.
 * @param materialDismissButton The composable that represents the dismiss button for non-iOS platforms.
 * If not provided, a default button with the dismiss text and the onDismiss lambda will be used.
 * @param materialIcon The composable that represents the icon of the dialog for non-iOS platforms.
 * If not provided, no icon will be used.
 * @param materialTitle The composable that represents the title of the dialog for non-iOS platforms.
 * If not provided, a default text with the title text will be used.
 * @param materialText The composable that represents the text of the dialog for non-iOS platforms.
 * If not provided, a default text with the text text will be used.
 * @param shape The shape of the dialog.
 * @param containerColor The color of the dialog container.
 * @param iconContentColor The color of the icon content.
 * @param titleContentColor The color of the title content.
 * @param textContentColor The color of the text content.
 * @param tonalElevation The tonal elevation of the dialog.
 * @param iosDialogStyle The style of the iOS dialog.
 * @param iosConfirmButtonStyle The style of the iOS confirm button.
 * @param iosDismissButtonStyle The style of the iOS dismiss button.
 * @param iosConfirmButtonIsPreferred Whether the confirm button is the preferred action on iOS.
 * When true, the confirm button will be bold and triggered by the Enter key on a hardware keyboard.
 * Only works with [AlertDialogIosStyle.Alert] style.
 * @param properties The properties of the dialog.
 */
@Composable
expect fun AdaptiveAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String,
    dismissText: String,
    title: String,
    text: String,

    materialConfirmButton: @Composable (() -> Unit)? = null,
    materialDismissButton: @Composable (() -> Unit)? = null,
    materialIcon: @Composable (() -> Unit)? = null,
    materialTitle: @Composable (() -> Unit)? = null,
    materialText: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,

    iosDialogStyle: AlertDialogIosStyle = AlertDialogIosStyle.Alert,
    iosConfirmButtonStyle: AlertDialogIosActionStyle = AlertDialogIosActionStyle.Default,
    iosDismissButtonStyle: AlertDialogIosActionStyle = AlertDialogIosActionStyle.Destructive,
    iosConfirmButtonIsPreferred: Boolean = false,

    properties: DialogProperties = DialogProperties(),
    modifier: Modifier = Modifier,
)

/**
 * Displays an adaptive alert dialog, which is a dialog that adapts to the platform it is running on.
 *
 * On iOS, it uses a native `UIAlertController` and on other platforms, it uses a material3 `BasicAlertDialog`.
 *
 * Non-iOS platforms is a shortcut for Android, Desktop, and Web.
 *
 * The styling parameters are only used for non-iOS platforms.
 *
 * @param onDismissRequest Lambda that is invoked when the dialog is dismissed.
 * @param modifier The modifier of the dialog.
 * @param iosProperties The iOS properties of the dialog.
 * This is used to configure the iOS dialog.
 * @param properties The properties of the dialog.
 * @param materialContent The composable that represents the confirm button for non-iOS platforms.
 * @see [AlertDialogIosProperties]
 */
@ExperimentalCalfUiApi
@Composable
expect fun AdaptiveBasicAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,

    iosProperties: AlertDialogIosProperties = rememberAlertDialogIosProperties(),

    properties: DialogProperties = DialogProperties(),

    materialContent: @Composable () -> Unit,
)