package com.mohamedrejeb.calf.picker

actual class FilePickerSettings(
    actual val title: String? = null,
    actual val initialDirectory: String? = null,
)

actual fun defaultFilePickerSettings(): FilePickerSettings = FilePickerSettings()
