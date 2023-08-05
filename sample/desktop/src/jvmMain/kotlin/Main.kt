import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mohamedrejeb.calf.sample.App


fun main() = application {
    Window(
        title = "Calf",
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
