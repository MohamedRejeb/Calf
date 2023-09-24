package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.CoreGraphics.*
import platform.UIKit.*
import platform.darwin.NSObject

/**
 * A helper class that is used to present UIKit bottom sheet with Compose content.
 *
 * @param onDismiss The callback that is called when the bottom sheet is dismissed.
 * @param content The Compose content of the bottom sheet.
 */
internal class BottomSheetManager(
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
    private val bottomSheetUIViewController = ComposeUIViewController {
        content()
    }

    private val bottomSheetTransitioningDelegate = BottomSheetTransitioningDelegate()

    /**
     * The presentation controller delegate that is used to detect when the bottom sheet is dismissed.
     */
    private val presentationControllerDelegate = BottomSheetControllerDelegate(
        onDismiss = {
            isPresented = false
            onDismiss()
        }
    )

    /**
     * Shows the bottom sheet.
     */
    fun show() {
        if (isPresented || isAnimating) return
        isAnimating = true

        bottomSheetUIViewController.modalPresentationStyle = UIModalPresentationPopover
        bottomSheetUIViewController.transitioningDelegate = bottomSheetTransitioningDelegate
        bottomSheetUIViewController.presentationController?.setDelegate(presentationControllerDelegate)

        bottomSheetUIViewController.sheetPresentationController?.setDetents(
            listOf(
                UISheetPresentationControllerDetentIdentifierMedium,
                UISheetPresentationControllerDetentIdentifierLarge
            )
        )
        bottomSheetUIViewController.sheetPresentationController?.prefersGrabberVisible = true

        println(bottomSheetUIViewController.sheetPresentationController)
        println(bottomSheetUIViewController.sheetPresentationController?.detents)

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
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

class BottomSheetTransitioningDelegate : NSObject(), UIViewControllerTransitioningDelegateProtocol {
    override fun presentationControllerForPresentedViewController(
        presented: UIViewController,
        presentingViewController: UIViewController?,
        sourceViewController: UIViewController
    ): UIPresentationController {
        return BottomSheetPresentationController(
            presented,
            presentingViewController,
        )
    }
}

class BottomSheetPresentationController(
    presentedViewController: UIViewController,
    presentingViewController: UIViewController?
): UIPresentationController(
    presentedViewController = presentedViewController,
    presentingViewController = presentingViewController,
) {
    @OptIn(ExperimentalForeignApi::class)
    override fun frameOfPresentedViewInContainerView(): CValue<CGRect> {
        val containerView = containerView ?: return cValue { CGRectZero }
        val safeAreaInsets = containerView.safeAreaInsets

        val safeAreaFrame = UIEdgeInsetsInsetRect(containerView.bounds, safeAreaInsets)
        val presentedHeight: CGFloat = 300.0

        return cValue {
            CGRectMake(
                x = CGRectGetMinX(safeAreaFrame),
                y = CGRectGetMaxY(safeAreaFrame) - presentedHeight,
                width = CGRectGetWidth(safeAreaFrame),
                height = presentedHeight
            )
        }
    }
}