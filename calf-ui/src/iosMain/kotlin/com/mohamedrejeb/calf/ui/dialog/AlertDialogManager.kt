package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.*
import com.mohamedrejeb.calf.core.*
import com.mohamedrejeb.calf.ui.dialog.uikit.*
import com.mohamedrejeb.calf.ui.utils.*
import kotlinx.cinterop.*
import platform.CoreGraphics.CGRectMake
import platform.UIKit.*
import platform.darwin.NSObject
import platform.objc.sel_registerName

/**
 * Manages the [UIAlertController] and its actions.
 *
 * @param onDismissRequest Lambda that is invoked when the dialog is dismissed.
 * @param iosProperties The iOS properties of the dialog.
 * @param properties The properties of the dialog.
 * @param parentUIViewController The [UIViewController] that will present the dialog.
 * @constructor Creates an [AlertDialogManager] instance.
 * @see [UIAlertController]
 */
@InternalCalfApi
class AlertDialogManager internal constructor(
    private val parentUIViewController: UIViewController,
    internal var onDismissRequest: () -> Unit,
    internal var iosProperties: AlertDialogIosProperties,
    internal var properties: DialogProperties,
    internal var layoutDirection: LayoutDirection = LayoutDirection.Ltr,
) : NSObject() {

    /**
     * Indicates whether the dialog is presented or not.
     */
    private var isPresented = false

    /**
     * Indicates whether the dialog is animating or not.
     */
    private var isAnimating = false

    /**
     * Strong references to text field delegate objects.
     * UIKit's addTarget holds targets weakly, so without these
     * the GC would collect the delegates and text change events
     * would stop firing.
     */
    private val textFieldDelegates = mutableListOf<NSObject>()

    /**
     * The horizontal extent of the pane that raised the dialog, in the presenting view's points,
     * when one Compose surface holds several panes; `null` to use the presenting view's column.
     */
    internal var anchorPane: () -> ClosedFloatingPointRange<Double>? = { null }

    /**
     * The ui view controller that is used to present the dialog.
     */
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private val dialogUIViewController: UIViewController by lazy {
        UIAlertController.alertControllerWithTitle(
            title = iosProperties.title,
            message = iosProperties.text,
            preferredStyle = iosProperties.style.toUIAlertControllerStyle()
        ).apply {
            iosProperties.actions.forEach { action ->
                val uiAlertAction = action.toUIAlertAction()
                uiAlertAction.enabled = action.enabled
                addAction(uiAlertAction)

                if (action.isPreferred) {
                    setPreferredAction(uiAlertAction)
                }
            }

            if (iosProperties.style == AlertDialogIosStyle.Alert) {
                iosProperties.textFields.forEach { textFieldState ->
                    addTextFieldWithConfigurationHandler { textField ->
                        if (textField == null)
                            return@addTextFieldWithConfigurationHandler

                        val textChangedDelegate = object : NSObject() {
                            @ObjCAction
                            fun textFieldDidChange(sender: UITextField) {
                                textFieldState.onValueChange(sender.text.orEmpty())
                            }
                        }

                        // Keep a strong reference so GC doesn't collect the delegate
                        textFieldDelegates.add(textChangedDelegate)

                        textField.text = textFieldState.initialValue
                        textField.placeholder = textFieldState.placeholder
                        textField.keyboardType = textFieldState.keyboardType.toUIKeyboardType()
                        textField.setSecureTextEntry(textFieldState.isSecure)
                        textField.addTarget(
                            target = textChangedDelegate,
                            action = sel_registerName("textFieldDidChange:"),
                            forControlEvents = UIControlEventEditingChanged
                        )
                    }
                }
            }

            if (iosProperties.severity == AlertDialogIosSeverity.Critical)
                setSeverity(UIAlertControllerSeverityCritical)
        }
    }

    /**
     * Updates the properties of the already-presented [UIAlertController]
     * without dismissing and recreating it.
     *
     * Updatable properties:
     * - [UIAlertController.title] and [UIAlertController.message]
     * - [UIAlertAction.enabled] for each action
     */
    internal fun updateDialogProperties() {
        val controller = dialogUIViewController as? UIAlertController ?: return

        controller.title = iosProperties.title
        controller.message = iosProperties.text

        val actions = iosProperties.actions
        for (i in actions.indices) {
            if (i < controller.actions.size) {
                (controller.actions[i] as? UIAlertAction)?.enabled = actions[i].enabled
            }
        }
    }

    /**
     * Lambda that dismisses the dialog.
     */
    private val onDismissLambda: (() -> Unit) = {
        dialogUIViewController.dismissViewControllerAnimated(
            flag = true,
            completion = {
                isPresented = false
                isAnimating = false
                onDismissRequest()
            }
        )
    }

    /**
     * Pointer to the [dismiss] method.
     */
    @OptIn(ExperimentalForeignApi::class)
    private val dismissPointer = sel_registerName("dismiss")

    /**
     * Dismisses the dialog.
     */
    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun dismiss() {
        if (!isPresented || isAnimating) return
        isAnimating = true
        onDismissLambda.invoke()
    }

    /**
     * Shows the dialog.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun showAlertDialog() {
        if (isPresented || isAnimating) return
        isAnimating = true

        dialogUIViewController.applyLayoutDirection(layoutDirection)
        anchorActionSheet()

        parentUIViewController.presentViewController(
            viewControllerToPresent = dialogUIViewController,
            animated = iosProperties.animated,
            completion = {
                isPresented = true
                isAnimating = false

                if (properties.dismissOnClickOutside) {
                    dialogUIViewController.view.superview?.setUserInteractionEnabled(true)
                    dialogUIViewController.view.superview?.addGestureRecognizer(
                        UITapGestureRecognizer(
                            target = this,
                            action = dismissPointer
                        )
                    )
                }
            }
        )
    }

    /**
     * UIKit shows the action-sheet style as a popover on regular width (iPad, an open iPhone Duo),
     * and without a source it floats in the middle of the window, over another column. Centred on
     * the raising column instead, without an arrow: there is no single control to point at.
     * On compact width UIKit ignores it and slides the sheet up from the bottom.
     */
    @OptIn(ExperimentalForeignApi::class)
    private fun anchorActionSheet() {
        if (iosProperties.style != AlertDialogIosStyle.ActionSheet) return
        val popover = dialogUIViewController.popoverPresentationController ?: return
        val view = parentUIViewController.view
        val presenter = view.safeAreaLayoutGuide.layoutFrame.useContents {
            AnchorRect(
                left = origin.x,
                top = origin.y,
                right = origin.x + size.width,
                bottom = origin.y + size.height,
            )
        }
        val anchor = actionSheetAnchor(presenter, anchorPane())
        popover.sourceView = view
        popover.sourceRect = CGRectMake(anchor.x, anchor.y, 1.0, 1.0)
        popover.permittedArrowDirections = 0u
    }

    companion object {
        fun AlertDialogIosActionStyle.toUIAlertActionStyle(): UIAlertActionStyle =
            when (this) {
                AlertDialogIosActionStyle.Default ->
                    UIAlertActionStyleDefault

                AlertDialogIosActionStyle.Cancel ->
                    UIAlertActionStyleCancel

                AlertDialogIosActionStyle.Destructive ->
                    UIAlertActionStyleDestructive
            }

        fun AlertDialogIosStyle.toUIAlertControllerStyle(): UIAlertControllerStyle =
            when (this) {
                AlertDialogIosStyle.ActionSheet ->
                    UIAlertControllerStyleActionSheet

                AlertDialogIosStyle.Alert ->
                    UIAlertControllerStyleAlert
            }

        fun AlertDialogIosAction.toUIAlertAction(): UIAlertAction {
            return UIAlertAction.actionWithTitle(
                title = title,
                style = style.toUIAlertActionStyle(),
                handler = {
                    onClick()
                }
            )
        }
    }
}