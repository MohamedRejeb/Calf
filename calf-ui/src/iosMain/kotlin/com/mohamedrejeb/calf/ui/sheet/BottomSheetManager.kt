package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedrejeb.calf.ui.navigation.IosBarEdgeInsets
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection
import com.mohamedrejeb.calf.ui.utils.applyTheme
import com.mohamedrejeb.calf.ui.utils.toUIColor
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIModalTransitionStyleCoverVertical
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationController
import platform.UIKit.UISheetPresentationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.presentationController
import platform.UIKit.sheetPresentationController
import platform.UIKit.setModalInPresentation
import platform.darwin.NSObject

/**
 * A helper class that is used to present UIKit bottom sheet with Compose content.
 *
 * @param onDismiss The callback that is called when the user dismisses the bottom sheet.
 * @param confirmValueChange Asked whether the user may swipe the sheet away.
 * @param onDetentChange The callback that is called when the user drags the sheet to another detent.
 * @param onSafeAreaChange Receives the sheet's own safe area whenever UIKit changes it.
 * @param content The Compose content of the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetManager(
    private val parentUIViewController: UIViewController,
    private var isDark: Boolean,
    private var containerColor: Color,
    private var onDismiss: () -> Unit,
    private val confirmValueChange: (SheetValue) -> Boolean,
    private val onDetentChange: (SheetValue) -> Unit,
    private val onSafeAreaChange: (IosBarEdgeInsets) -> Unit,
    private val content: @Composable () -> Unit,
    internal var layoutDirection: LayoutDirection = LayoutDirection.Ltr,
) {
    private var isInitialized = false

    /**
     * Indicates whether the bottom sheet is currently presented.
     */
    private var isPresented = false

    /**
     * Indicates whether the bottom sheet is currently animating.
     */
    private var isAnimating = false

    /**
     * What the sheet is presented with; applied live while it is presented.
     */
    private var options = SheetPresentationOptions()

    /**
     * The presentation controller delegate that is used to detect when the bottom sheet is dismissed
     * or dragged to another detent.
     */
    private val presentationControllerDelegate by lazy {
        BottomSheetControllerDelegate(
            onDismiss = {
                isPresented = false
                onDismiss()
            },
            confirmValueChange = confirmValueChange,
            onDetentChange = { identifier ->
                onDetentChange(sheetValueOfDetent(identifier, options.hasPartiallyExpandedState))
            },
        )
    }

    /**
     * The ui view controller that is used to present the bottom sheet.
     */
    private val bottomSheetUIViewController: UIViewController by lazy {
        ComposeUIViewController(content).apply {
            modalPresentationStyle = UIModalPresentationPageSheet
            modalTransitionStyle = UIModalTransitionStyleCoverVertical
            presentationController?.delegate = presentationControllerDelegate
            SheetSafeAreaProbe(onSafeAreaChange).install(view)
            isInitialized = true
        }
    }

    fun applyTheme(dark: Boolean) {
        isDark = dark

        if (isInitialized)
            bottomSheetUIViewController.applyTheme(dark)
    }

    fun applyContainerColor(color: Color) {
        containerColor = color

        if (isInitialized)
            bottomSheetUIViewController.view.backgroundColor = color.toUIColor()
    }

    /**
     * Applies [newOptions] now if the sheet exists, animated if it is on screen, and keeps them for
     * the next presentation.
     */
    fun applyPresentationOptions(newOptions: SheetPresentationOptions) {
        options = newOptions

        if (!isInitialized) return
        val sheet = bottomSheetUIViewController.sheetPresentationController
        if (isPresented && sheet != null) {
            sheet.animateChanges { applyOptionsToController() }
        } else {
            applyOptionsToController()
        }
    }

    private fun applyOptionsToController() {
        bottomSheetUIViewController.setModalInPresentation(options.preventsDismissal)
        bottomSheetUIViewController.sheetPresentationController?.apply {
            setDetents(sheetDetentValues(options.hasPartiallyExpandedState).map { it.toDetent() })
            prefersGrabberVisible = options.showsGrabber
        }
    }

    /**
     * Brings the sheet to [value]: presents it there when it is not on screen, else moves it to
     * that detent. Returns false when nothing was started, because an animation is running.
     *
     * [completion] runs once the sheet is presented; a detent change completes at once, UIKit
     * animates it without telling when it is done.
     */
    fun present(
        value: SheetValue,
        completion: (() -> Unit)? = null,
    ): Boolean {
        require(value != SheetValue.Hidden) { "Use hide() to dismiss the sheet." }
        if (isAnimating) return false
        if (isPresented) return selectDetent(value, completion)

        isAnimating = true

        applyOptionsToController()
        bottomSheetUIViewController.sheetPresentationController?.selectedDetentIdentifier =
            value.toDetentIdentifier()

        applyTheme(isDark)
        applyContainerColor(containerColor)
        bottomSheetUIViewController.applyLayoutDirection(layoutDirection)

        parentUIViewController.presentViewController(
            viewControllerToPresent = bottomSheetUIViewController,
            animated = true,
            completion = {
                isPresented = true
                isAnimating = false
                completion?.invoke()
            }
        )

        return true
    }

    private fun selectDetent(
        value: SheetValue,
        completion: (() -> Unit)?,
    ): Boolean {
        val sheet = bottomSheetUIViewController.sheetPresentationController ?: return false

        sheet.animateChanges {
            sheet.selectedDetentIdentifier = value.toDetentIdentifier()
        }
        completion?.invoke()

        return true
    }

    /**
     * Hides the bottom sheet.
     */
    fun hide(
        completion: (() -> Unit)? = null
    ): Boolean {
        if (!isPresented || isAnimating) return false

        isAnimating = true

        bottomSheetUIViewController.dismissViewControllerAnimated(
            flag = true,
            completion = {
                isPresented = false
                isAnimating = false
                completion?.invoke()
            }
        )

        return true
    }
}

/**
 * Listens to the sheet's presentation controller: the swipe down, which [confirmValueChange] may
 * refuse, and the detent the user drags the sheet to.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetControllerDelegate(
    private val onDismiss: () -> Unit,
    private val confirmValueChange: (SheetValue) -> Boolean,
    private val onDetentChange: (String?) -> Unit,
) : NSObject(), UISheetPresentationControllerDelegateProtocol {

    override fun presentationControllerShouldDismiss(presentationController: UIPresentationController): Boolean {
        return confirmValueChange(SheetValue.Hidden)
    }

    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        onDismiss()
    }

    override fun sheetPresentationControllerDidChangeSelectedDetentIdentifier(
        sheetPresentationController: UISheetPresentationController,
    ) {
        onDetentChange(sheetPresentationController.selectedDetentIdentifier)
    }
}
