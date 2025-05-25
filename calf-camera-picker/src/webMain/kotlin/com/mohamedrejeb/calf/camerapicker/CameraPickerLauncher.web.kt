package com.mohamedrejeb.calf.camerapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.io.KmpFile

@Stable
internal class CameraPickerLauncherWebImpl() : CameraPickerLauncher {
    override fun launch() = Unit
}

@Composable
actual fun rememberCameraPickerLauncher(
    onResult: (KmpFile) -> Unit,
): CameraPickerLauncher {
    return remember {
        CameraPickerLauncherWebImpl()
    }
}