package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedrejeb.calf.ui.utils.applyTheme
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIApplication
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
internal class BottomSheetManager(
    dark: Boolean,
    private val onDismiss: () -> Unit,
    private val content: @Composable () -> Unit
) {
    /**
     * Indicates whether the bottom sheet is currently presented.
     */
    private var isPresented = false

    /**
     * Indicates whether the bottom sheet is currently animating.
     */
    private var isAnimating = false

    /**
     * The ui view controller that is used to present the bottom sheet.
     */
    private var bottomSheetUIViewController: UIViewController? = null

    private var isDark = dark

    private fun initBottomSheetUIViewController(): UIViewController {
        val uiViewController = ComposeUIViewController(content).apply {
            modalPresentationStyle = UIModalPresentationPageSheet
            modalTransitionStyle = UIModalTransitionStyleCoverVertical
            presentationController?.delegate = BottomSheetControllerDelegate(
                onDismiss = {
                    isPresented = false
                    onDismiss()
                    disposeBottomSheetUIViewController()
                }
            )
            applyTheme(isDark)
        }

        bottomSheetUIViewController = uiViewController

        return uiViewController
    }

    private fun disposeBottomSheetUIViewController() {
        bottomSheetUIViewController = null
    }

    fun applyTheme(dark: Boolean) {
        isDark = dark
        bottomSheetUIViewController?.applyTheme(dark)
    }

    /**
     * Shows the bottom sheet.
     */
    fun show(
        skipPartiallyExpanded: Boolean,
    ) {
        val uiViewController = initBottomSheetUIViewController()

        if (isPresented || isAnimating) return
        isAnimating = true

        uiViewController.sheetPresentationController?.setDetents(
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
        uiViewController.sheetPresentationController?.prefersGrabberVisible = true

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = uiViewController,
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

        bottomSheetUIViewController?.dismissViewControllerAnimated(
            flag = true,
            completion = {
                isPresented = false
                isAnimating = false
                completion?.invoke()
                disposeBottomSheetUIViewController()
            }
        )
    }
}

class BottomSheetControllerDelegate(
    private val onDismiss: () -> Unit
) : NSObject(), UIAdaptivePresentationControllerDelegateProtocol {

    override fun presentationControllerShouldDismiss(presentationController: UIPresentationController): Boolean {
        return true
    }

    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        onDismiss()
    }

}
