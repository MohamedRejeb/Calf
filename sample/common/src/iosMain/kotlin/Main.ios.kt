import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedrejeb.calf.sample.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
//    lateinit var uiViewController: UIViewController
//    val uiNavigationController: UINavigationController by lazy {
//        UINavigationController(rootViewController = uiViewController)
//    }

//    uiViewController = ComposeUIViewController {
//        App(
//            navigate = {
//                println("kotlin navigationController")
//                println("kotlin navigationController")
//                println(uiViewController)
//                println(uiViewController.navigationController)
////                navigate()
//                uiViewController.navigationController?.pushViewController(
//                    SecondViewController(),
//                    true
//                )
//            }
//        )
//    }

//    savedUiViewController = uiViewController

    return ComposeUIViewController {
        App()
    }
}
