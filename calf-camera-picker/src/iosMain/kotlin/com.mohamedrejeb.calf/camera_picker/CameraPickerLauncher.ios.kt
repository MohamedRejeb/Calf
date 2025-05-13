package com.mohamedrejeb.calf.camera_picker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@Composable
actual fun rememberCameraPickerLauncher(onResult: (KmpFile) -> Unit): CameraPickerLauncher {
    val viewController = getUIViewController() // You'll need a helper function for this

    return object : CameraPickerLauncher {
        override fun launch() {
            val pickerController = UIImagePickerController()
            pickerController.sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            pickerController.delegate =
                object : NSObject(), UIImagePickerControllerDelegateProtocol,
                    UINavigationControllerDelegateProtocol {
                    override fun imagePickerController(
                        picker: UIImagePickerController,
                        didFinishPickingMediaWithInfo: Map<Any?, Any?>
                    ) {
                        val imageUrl =
                            didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
                        if (imageUrl != null) {
                            val kmpFile = KmpFile(imageUrl) // Use file path accordingly
                            onResult(kmpFile)
                        }
                        picker.dismissViewControllerAnimated(true, null)
                    }

                    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                        picker.dismissViewControllerAnimated(true, null)
                    }
                }

            viewController.presentViewController(pickerController, true, null)
        }
    }
}

private fun getUIViewController(): UIViewController {
    // You need to obtain the current `UIViewController` in some way.
    // A common approach is to have a helper function that accesses it through the app's window or other means.
    val window = UIApplication.sharedApplication.keyWindow
    return window?.rootViewController
        ?: throw IllegalStateException("No root view controller available.")
}
