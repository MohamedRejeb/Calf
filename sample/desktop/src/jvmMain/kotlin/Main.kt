import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mohamedrejeb.calf.sample.App


fun main() {
    System.setProperty("compose.interop.blending", "true")

    application {
        Window(
            title = "Calf",
            onCloseRequest = ::exitApplication
        ) {
            App()
        }
    }
}
