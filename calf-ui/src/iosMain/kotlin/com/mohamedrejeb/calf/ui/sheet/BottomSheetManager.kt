package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedrejeb.calf.ui.utils.applyTheme
import com.mohamedrejeb.calf.ui.utils.toUIColor
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIModalTransitionStyleCoverVertical
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.UIKit.presentationController
import platform.UIKit.sheetPresentationController
import platform.darwin.NSObject

/**
 * A helper class that is used to present UIKit bottom sheet with Compose content.
 *
 * @param onDismiss The callback that is called when the bottom sheet is dismissed.
 * @param content The Compose content of the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class BottomSheetManager(
    private val parentUIViewController: UIViewController,
    private var isDark: Boolean,
    private var containerColor: Color,
    private var onDismiss: () -> Unit,
    private val confirmValueChange: (SheetValue) -> Boolean,
    private val content: @Composable () -> Unit
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
     * The presentation controller delegate that is used to detect when the bottom sheet is dismissed.
     */
    private val presentationControllerDelegate by lazy {
        BottomSheetControllerDelegate(
            onDismiss = {
                isPresented = false
                onDismiss()
            },
            confirmValueChange = confirmValueChange,
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
     * Shows the bottom sheet.
     */
    fun show(
        skipPartiallyExpanded: Boolean,
        showDragHandle: Boolean = true,
    ) {
        if (isPresented || isAnimating) return
        isAnimating = true

        bottomSheetUIViewController.sheetPresentationController?.setDetents(
            if (skipPartiallyExpanded)
                listOf(
                    UISheetPresentationControllerDetent.largeDetent()
                )
            else
                listOf(
                    UISheetPresentationControllerDetent.largeDetent(),
                    UISheetPresentationControllerDetent.mediumDetent(),
                )
        )
        bottomSheetUIViewController.sheetPresentationController?.prefersGrabberVisible = showDragHandle

        applyTheme(isDark)
        applyContainerColor(containerColor)

        parentUIViewController.presentViewController(
            viewControllerToPresent = bottomSheetUIViewController,
            animated = true,
            completion = {
                isPresented = true
                isAnimating = false
            }
        )
    }

    /**
     * Hides the bottom sheet.
     */
    fun hide(
        completion: (() -> Unit)? = null
    ) {
        if (!isPresented || isAnimating) return
        isAnimating = true

        bottomSheetUIViewController.dismissViewControllerAnimated(
            flag = true,
            completion = {
                isPresented = false
                isAnimating = false
                completion?.invoke()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetControllerDelegate(
    private val onDismiss: () -> Unit,
    private val confirmValueChange: (SheetValue) -> Boolean,
) : NSObject(), UIAdaptivePresentationControllerDelegateProtocol {

    override fun presentationControllerShouldDismiss(presentationController: UIPresentationController): Boolean {
        return confirmValueChange(SheetValue.Hidden)
    }

    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        onDismiss()
    }

}
