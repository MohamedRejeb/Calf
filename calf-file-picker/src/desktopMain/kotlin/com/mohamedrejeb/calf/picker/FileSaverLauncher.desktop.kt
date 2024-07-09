package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.awt.AwtFileSaver
import kotlinx.coroutines.launch

@Composable
fun rememberFileSaverLauncher(
    bytes: ByteArray?,
    baseName: String,
    extension: String,
    initialDirectory: String?,
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()

    val currentBytes by rememberUpdatedState(bytes)
    val currentBaseName by rememberUpdatedState(baseName)
    val currentExtension by rememberUpdatedState(extension)
    val currentInitialDirectory by rememberUpdatedState(initialDirectory)
    val currentOnResult by rememberUpdatedState(onResult)

    return FileSaverLauncher(
        onLaunch = {
            scope.launch {
                val file = AwtFileSaver.saveFile(
                    bytes = currentBytes,
                    baseName = currentBaseName,
                    extension = currentExtension,
                    initialDirectory = currentInitialDirectory,
                    parentWindow = null,
                )

                currentOnResult(file)
            }
        }
    )
}

class FileSaverLauncher(
    private val onLaunch: () -> Unit,
) {
    fun launch() {
        onLaunch()
    }
}
