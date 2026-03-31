package com.mohamedrejeb.calf.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember

/**
 * Settings for the file picker dialog.
 *
 * Common properties ([title], [initialDirectory]) are available on all platforms.
 * Desktop adds a [parentWindow][FilePickerSettings] overload in its source set.
 *
 * Create instances using the [FilePickerSettings] factory function or
 * [rememberFilePickerSettings] composable:
 * ```
 * // commonMain
 * val settings = FilePickerSettings(
 *     title = "Pick an image",
 *     initialDirectory = "/home/user/Pictures",
 * )
 *
 * // or inside a @Composable
 * val settings = rememberFilePickerSettings(
 *     title = "Pick an image",
 *     initialDirectory = "/home/user/Pictures",
 * )
 * ```
 */
@Immutable
interface FilePickerSettings {
    val title: String?
    val initialDirectory: String?
}

internal data class FilePickerSettingsImpl(
    override val title: String? = null,
    override val initialDirectory: String? = null,
) : FilePickerSettings

/**
 * Creates a [FilePickerSettings] with the given [title] and [initialDirectory].
 *
 * On Desktop, use the overload that also accepts `parentWindow`.
 */
fun FilePickerSettings(
    title: String? = null,
    initialDirectory: String? = null,
): FilePickerSettings = FilePickerSettingsImpl(
    title = title,
    initialDirectory = initialDirectory,
)

/**
 * Default [FilePickerSettings] with no customization.
 */
fun defaultFilePickerSettings(): FilePickerSettings = FilePickerSettings()

/**
 * Remembers a [FilePickerSettings] instance that updates when [title] or [initialDirectory] change.
 *
 * On Desktop, use the overload that also accepts `parentWindow`.
 */
@Composable
fun rememberFilePickerSettings(
    title: String? = null,
    initialDirectory: String? = null,
): FilePickerSettings = remember(title, initialDirectory) {
    FilePickerSettings(title = title, initialDirectory = initialDirectory)
}
