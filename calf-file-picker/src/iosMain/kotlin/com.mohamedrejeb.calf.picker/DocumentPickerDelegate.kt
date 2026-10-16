package com.mohamedrejeb.calf.picker

import platform.Foundation.NSURL
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
import platform.UIKit.presentationController
import platform.darwin.NSObject
import kotlin.concurrent.AtomicInt

/**
 * Delegate of a presented [UIDocumentPickerViewController] that reports exactly one outcome per
 * presentation.
 *
 * A dismissal can arrive through `documentPickerWasCancelled`, through
 * `presentationControllerDidDismiss` alone (a swipe that starts before the presentation animation
 * ends fires only that one), or through both, so every callback goes through one guard.
 */
internal class DocumentPickerDelegate(
    private val onPicked: (List<NSURL>) -> Unit,
    private val onCancelled: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol, UIAdaptivePresentationControllerDelegateProtocol {
    private val finished = AtomicInt(0)

    /** Arms this delegate for a new presentation of [picker]; call it before presenting. */
    fun attachTo(picker: UIDocumentPickerViewController) {
        finished.value = 0
        picker.delegate = this
        picker.presentationController?.delegate = this
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        if (finishOnce()) onPicked(didPickDocumentsAtURLs.filterIsInstance<NSURL>())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        if (finishOnce()) onCancelled()
    }

    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        if (finishOnce()) onCancelled()
    }

    private fun finishOnce(): Boolean = finished.compareAndSet(0, 1)
}
