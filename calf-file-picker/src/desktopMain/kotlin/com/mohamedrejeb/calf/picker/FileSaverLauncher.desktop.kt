package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.platform.awt.AwtFileSaver
import kotlinx.coroutines.launch

@ExperimentalCalfApi
@Composable
fun rememberFileSaverLauncher(
    onResult: (KmpFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()

    val currentOnResult by rememberUpdatedState(onResult)

    return FileSaverLauncher(
        onLaunch = { bytes, baseName, extension, initialDirectory ->
            scope.launch {
                val file = AwtFileSaver.saveFile(
                    bytes = bytes,
                    baseName = baseName,
                    extension = extension,
                    initialDirectory = initialDirectory,
                    parentWindow = null,
                )

                currentOnResult(file)
            }
        }
    )
}

@ExperimentalCalfApi
class FileSaverLauncher(
    private val onLaunch: (
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    fun launch(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) {
        onLaunch(
            bytes,
            baseName,
            extension,
            initialDirectory,
        )
    }
}
