import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.CanvasBasedWindow
import com.mohamedrejeb.calf.sample.App
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow(
            title = "Calf",
            canvasElementId = "ComposeTarget",
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                App()
            }
        }
    }
}
