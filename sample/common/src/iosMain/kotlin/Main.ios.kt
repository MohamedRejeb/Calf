import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedrejeb.calf.sample.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        App()
    }
}
