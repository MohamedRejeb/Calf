import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mohamedrejeb.calf.picker.ProvideFilePickerParentWindow
import com.mohamedrejeb.calf.sample.App


fun main() {
    System.setProperty("compose.interop.blending", "true")

    application {
        Window(
            title = "Calf",
            icon = painterResource("app-icon.png"),
            // Wide enough for the home list and a demo side by side.
            state = rememberWindowState(size = DpSize(1200.dp, 800.dp)),
            onCloseRequest = ::exitApplication,
        ) {
            ProvideFilePickerParentWindow {
                App()
            }
        }
    }
}
