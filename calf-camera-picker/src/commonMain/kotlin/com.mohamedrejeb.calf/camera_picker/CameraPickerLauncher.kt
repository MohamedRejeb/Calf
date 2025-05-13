package com.mohamedrejeb.calf.camera_picker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile


interface CameraPickerLauncher {
    fun launch()
}

@Composable
expect fun rememberCameraPickerLauncher(
    onResult: (KmpFile) -> Unit,
): CameraPickerLauncher


