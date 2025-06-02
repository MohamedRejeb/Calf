package com.mohamedrejeb.calf.camerapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.io.KmpFile
import platform.Foundation.NSURL
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

internal class CameraPickerLauncherIosImpl(
    private val viewController: UIViewController,
    private val onResult: (KmpFile) -> Unit,
) : CameraPickerLauncher {
    override fun launch() {
        if (!UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            )
        ) {
            throw IllegalStateException("Camera is not available on this device")
        }
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
                        didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL

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
