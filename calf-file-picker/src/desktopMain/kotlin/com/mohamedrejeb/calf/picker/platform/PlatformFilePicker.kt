package com.mohamedrejeb.calf.picker.platform

import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.platform.awt.AwtFilePicker
import com.mohamedrejeb.calf.picker.platform.mac.MacOSFilePicker
import com.mohamedrejeb.calf.picker.platform.util.Platform
import com.mohamedrejeb.calf.picker.platform.util.PlatformUtil
import com.mohamedrejeb.calf.picker.platform.windows.WindowsFilePicker
import java.awt.Window
import java.io.File

interface PlatformFilePicker {

    suspend fun launchFilePicker(
        initialDirectory: String?,
        type: FilePickerFileType,
        selectionMode: FilePickerSelectionMode,
        title: String?,
        parentWindow: Window?,
        onResult: (List<File>) -> Unit,
    )

    suspend fun launchDirectoryPicker(
        initialDirectory: String?,
        title: String?,
        parentWindow: Window?,
        onResult: (File?) -> Unit,
    )

    companion object {
        val current: PlatformFilePicker by lazy { createPlatformFilePicker() }

        private fun createPlatformFilePicker(): PlatformFilePicker {
            return when (PlatformUtil.current) {
                Platform.Windows ->
                    WindowsFilePicker()

                Platform.MacOS ->
                    MacOSFilePicker()

                Platform.Linux ->
                    AwtFilePicker()
            }
        }
    }

}