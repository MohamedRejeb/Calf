package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Immutable

@Immutable
actual data class FilePickerSettings(
    actual val title: String? = null,
    actual val initialDirectory: String? = null,
)

actual fun defaultFilePickerSettings(): FilePickerSettings = FilePickerSettings()
