package com.mohamedrejeb.calf.camerapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.io.KmpFile
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.random.Random

internal class CameraPickerLauncherIosImpl(
    private val viewController: UIViewController,
    private val onResult: (KmpFile) -> Unit,
) : CameraPickerLauncher {

    // Strong reference to prevent delegate from being deallocated
    private var pickerDelegate: NSObject? = null

    override fun launch() {
        val isSimulator =
            platform.Foundation.NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null

        val pickerController = UIImagePickerController().apply {
            sourceType = if (!isSimulator &&
                UIImagePickerController.isSourceTypeAvailable(
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                )
            ) {
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            } else {
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            }

            allowsEditing = false

            // Create and store the delegate instance
            val delegateInstance = object : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {

                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, Any?>
                ) {
                    val image =
                        didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                    if (image != null) {
                        val imageData = UIImagePNGRepresentation(image)
                        if (imageData != null) {
                            val tempPath = saveTempImageFile(imageData)
                            if (tempPath != null) {
                                onResult(KmpFile(NSURL.fileURLWithPath(tempPath)))
                            }
                        }
                    }
                    picker.dismissViewControllerAnimated(true, null)
                    // Clear reference after use
                    pickerDelegate = null
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    picker.dismissViewControllerAnimated(true, null)
                    // Clear reference on cancel
                    pickerDelegate = null
                }

                private fun saveTempImageFile(data: NSData): String? {
                    val tempDir = NSTemporaryDirectory()
                    val fileName = "captured_image_${Random.nextInt()}.png"
                    val filePath = tempDir + fileName
                    return if (data.writeToFile(filePath, true)) filePath else null
                }
            }

            // Assign delegate and store strong reference
            pickerDelegate = delegateInstance
            delegate = delegateInstance
        }

        // Always present controller on main thread
        dispatch_async(dispatch_get_main_queue()) {
            viewController.presentViewController(pickerController, true, null)
        }
    }
}


@Composable
actual fun rememberCameraPickerLauncher(onResult: (KmpFile) -> Unit): CameraPickerLauncher {
    val viewController = LocalUIViewController.current
    return remember(viewController, onResult) {
        CameraPickerLauncherIosImpl(viewController, onResult)
    }
}
