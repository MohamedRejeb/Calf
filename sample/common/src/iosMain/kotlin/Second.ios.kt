//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.interop.ComposeUITextField
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.ComposeUIViewController
//import kotlinx.cinterop.*
//import platform.CoreGraphics.*
//import platform.UIKit.*
//import platform.darwin.NSObject
//import platform.objc.sel_registerName
//import platform.posix.free
//
//fun SecondViewController(): UIViewController {
//    val uiViewController = MySecondViewController().uiViewController
//
//    return uiViewController
//}
//
//class MySecondViewController {
//
//    @OptIn(ExperimentalForeignApi::class)
//    private val dismissPointer get() = sel_registerName("dismiss")
//
//    private val onDismiss: (() -> Unit) = {
//        println("dismiss")
//        UIApplication.sharedApplication.keyWindow?.rootViewController?.dismissViewControllerAnimated(
//            flag = true,
//            completion = null
//        )
//    }
//
//    @OptIn(BetaInteropApi::class)
//    @ObjCAction
//    fun dismiss() {
//        onDismiss.invoke()
//    }
//
//    @OptIn(ExperimentalForeignApi::class)
//    fun showAlertDialog() {
//        val alertController = UIAlertController.alertControllerWithTitle(
//            title = "Alert",
//            message = "This is a native alert from Compose.",
//            preferredStyle = UIAlertControllerStyleAlert
//        )
//
//        val dismissAction = UIAlertAction.actionWithTitle(
//            title = "OK",
//            style = UIAlertActionStyleDefault,
//            handler = {
//                println("OK")
//            }
//        )
//        alertController.addAction(dismissAction)
//
//        val cancelAction = UIAlertAction.actionWithTitle(
//            title = "Cancel",
//            style = UIAlertActionStyleDestructive,
//            handler = {
//                println("Cancel")
//            }
//        )
//        alertController.addAction(cancelAction)
//
//        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
//            viewControllerToPresent = alertController,
//            animated = true,
//            completion = {
//                println("completion")
//                println((UIApplication.sharedApplication.keyWindow?.rootViewController as? UINavigationController?))
//                println((UIApplication.sharedApplication.keyWindow?.rootViewController as? UINavigationController?)?.viewControllers)
//                println((UIApplication.sharedApplication.keyWindow?.rootViewController as? UINavigationController?)?.topViewController)
//                println((UIApplication.sharedApplication.keyWindow?.rootViewController as? UINavigationController?)?.visibleViewController)
//
//                alertController.view.superview?.setUserInteractionEnabled(true)
//                alertController.view.superview?.addGestureRecognizer(
//                    UITapGestureRecognizer(
//                        target = this,
////        target = (UIApplication.sharedApplication.keyWindow?.rootViewController as? UINavigationController?)?.visibleViewController,
//                        action = sel_registerName("dismiss")
//                    )
//                )
//            }
//        )
//    }
//
//    val uiViewController = ComposeUIViewController {
////        SecondScreen(
////            showAlertDialog = {
////                showAlertDialog()
////            }
////        )
//    }
//
//}
//
