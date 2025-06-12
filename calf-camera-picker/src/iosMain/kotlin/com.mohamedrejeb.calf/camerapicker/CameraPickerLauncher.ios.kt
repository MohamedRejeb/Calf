package com.mohamedrejeb.calf.camerapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.io.KmpFile
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

internal class CameraPickerLauncherIosImpl(
    private val viewController: UIViewController,
    private val onResult: (KmpFile) -> Unit,
) : CameraPickerLauncher {
    override fun launch() {
        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
            throw IllegalStateException("Camera is not available on this device.")
        }

        val pickerController = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, Any?>
                ) {
                    val image =
                        didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                    if (image != null) {
                        val data = UIImageJPEGRepresentation(image, 1.0)
                        if (data != null) {
                            val tempDir = NSTemporaryDirectory()
                            val fileName = "captured_${NSDate().timeIntervalSince1970}.jpg"
                            val filePath = tempDir + fileName
                            val fileUrl = NSURL.fileURLWithPath(filePath)
                            val written = data.writeToURL(fileUrl, true)
                            if (written) {
                                onResult(KmpFile(fileUrl))
                            }
                        }
                    }
                    NSOperationQueue.mainQueue.addOperationWithBlock {
                        picker.dismissViewControllerAnimated(true, null)
                    }
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    NSOperationQueue.mainQueue.addOperationWithBlock {
                        picker.dismissViewControllerAnimated(true, null)
                    }
                }
            }
        }

        NSOperationQueue.mainQueue.addOperationWithBlock {
            viewController.presentViewController(pickerController, true, null)
        }
    }

}

@Composable
actual fun rememberCameraPickerLauncher(onResult: (KmpFile) -> Unit): CameraPickerLauncher {
    val viewController = LocalUIViewController.current

    return remember(
        viewController,
        onResult,
    ) {
        CameraPickerLauncherIosImpl(
            viewController = viewController,
            onResult = onResult,
        )
    }
}
