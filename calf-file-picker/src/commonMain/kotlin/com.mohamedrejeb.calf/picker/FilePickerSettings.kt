package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Immutable

/**
 * Platform-specific settings for the file picker dialog.
 *
 * Common properties (title, initialDirectory) are available on all platforms.
 * Platform-specific properties (e.g. parentWindow on desktop) are only
 * available in the corresponding source set.
 *
 * Usage:
 * ```
 * // commonMain
 * val settings = FilePickerSettings(
 *     title = "Pick an image",
 *     initialDirectory = "/home/user/Pictures",
 * )
 *
 * // desktopMain — additional options available
 * val settings = FilePickerSettings(
 *     title = "Pick an image",
 *     parentWindow = window,
 * )
 * ```
 */
@Immutable
expect class FilePickerSettings {
    val title: String?
    val initialDirectory: String?
}

/**
 * Default [FilePickerSettings] with no customization.
 */
expect fun defaultFilePickerSettings(): FilePickerSettings
