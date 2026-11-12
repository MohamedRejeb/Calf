import com.mohamedrejeb.calf.sample.App
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveBarHostUIViewController
import platform.UIKit.UIViewController

/**
 * The bar host: screens on plain Compose navigation get their `AdaptiveScaffold` bars as UIKit
 * container bars. The calf-navigation shell is unaffected (no hosted screen, no hosted bars).
 */
@OptIn(ExperimentalCalfUiApi::class)
fun MainViewController(): UIViewController =
    AdaptiveBarHostUIViewController {
        App()
    }
