import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.window.ComposeViewport
import com.mohamedrejeb.calf.sample.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport("composeApp") {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            App()
        }
    }
}
