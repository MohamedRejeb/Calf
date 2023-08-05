import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import com.mohamedrejeb.calf.sample.App
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        Window("Calf") {
            Box(Modifier.fillMaxSize()) {
                App()
            }
        }
    }
}