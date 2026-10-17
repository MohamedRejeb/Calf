package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext

/**
 * A wrapper class for a file in the platform specific implementation.
 */
expect class KmpFile {
    companion object
}

/**
 * Checks if the KmpFile exists in the specified platform context.
 *
 * @param context The platform context in which to check the existence of the file.
 * @return True if the file exists, false otherwise.
 */
expect fun KmpFile.exists(context: PlatformContext): Boolean

/**
 * Reads the content of the KmpFile as a byte array.
 *
 * @param context The platform context.
 * @return The content of the file as a byte array.
 */
expect suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray

/**
 * Reads the name of the KmpFile.
 *
 * @param context The platform context.
 * @return The name of the file as a string.
 */
expect fun KmpFile.getName(context: PlatformContext): String?

/**
 * Reads the file system path of the KmpFile.
 *
 * The result is a decoded path that can be opened directly, for example with kotlinx-io's `Path`
 * or `java.io.File`, or `null` when the file has no file system path:
 * - **Android**: the path of a `file:` URI; `null` for `content:` URIs, which is what the system
 *   pickers return. Read those with [readByteArray] or `source()`, or use [toUriString].
 * - **iOS**: the path of the file the app can read. For a picked file that is the app's copy,
 *   which keeps the picked file's name; the original location is security scoped.
 * - **Desktop**: the path of the file.
 * - **JS** and **Wasm**: always `null`, browsers do not expose file paths.
 *
 * @param context The platform context.
 * @return The file system path of the file, or `null` if it has none.
 * @see toUriString
 */
expect fun KmpFile.getPath(context: PlatformContext): String?

/**
 * Returns the URI of the KmpFile as a string, such as `content://…` on Android or `file:///…` on
 * iOS and Desktop.
 *
 * Use it to persist a reference to the file or to hand it back to the platform, for example as
 * the `initialDirectory` of the Android folder picker. Use [getPath] when a file system path is
 * needed.
 *
 * @return The URI of the file, or `null` on JS and Wasm, where files have no URI.
 * @see fromUriString
 */
expect fun KmpFile.toUriString(): String?

/**
 * Creates a [KmpFile] from a string returned by [toUriString], for example one the app stored to
 * reopen a file or folder later. An absolute file system path is accepted too.
 *
 * Only the reference is restored, not access to it: a `content:` URI on Android needs a
 * persisted URI permission, and a picked location on iOS needs a security-scoped bookmark, to stay
 * readable after the app restarts.
 *
 * @param uriString A `content:` or `file:` URI on Android, a `file:` URL on iOS and Desktop, or an
 * absolute path.
 * @return The file, or `null` if [uriString] is not a file reference, and always `null` on JS and
 * Wasm, where files have no URI.
 */
expect fun KmpFile.Companion.fromUriString(uriString: String): KmpFile?

/**
 * Reads the size of the KmpFile in bytes.
 *
 * @param context The platform context.
 * @return The size in bytes, or `null` if it is unknown, the file does not exist, or it is a
 * directory.
 */
expect fun KmpFile.getSize(context: PlatformContext): Long?

/**
 * Reads the MIME type of the KmpFile, such as `application/pdf`.
 *
 * Android asks the content provider and falls back to the file extension; the other platforms use
 * the extension, and browsers the type of the picked file.
 *
 * @param context The platform context.
 * @return The MIME type, or `null` if it is unknown.
 */
expect fun KmpFile.getMimeType(context: PlatformContext): String?

/**
 * Checks if the KmpFile is a directory.
 *
 * @param context The platform context.
 * @return True if the file is a directory, false otherwise.
 */
expect fun KmpFile.isDirectory(context: PlatformContext): Boolean

/**
 * Checks if the KmpFile is a file.
 *
 * @param context The platform context.
 * @return True if the file is a file, false otherwise.
 */
fun KmpFile.isFile(context: PlatformContext): Boolean = !isDirectory(context)

// --- Context-free overloads ---
// On Android, these use the application context stored internally by Calf composables.
// On all other platforms, context is not needed and these work out of the box.

/**
 * Checks if the KmpFile exists.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 *
 * @param context The platform context in which to check the existence of the file.
 * @return True if the file exists, false otherwise.
 */
fun KmpFile.exists(): Boolean = exists(resolveContext())

/**
 * Reads the content of the KmpFile as a byte array.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 *
 * @param context The platform context.
 * @return The content of the file as a byte array.
 */
suspend fun KmpFile.readByteArray(): ByteArray = readByteArray(resolveContext())

/**
 * Reads the name of the KmpFile.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 *
 * @param context The platform context.
 * @return The name of the file as a string.
 */
fun KmpFile.getName(): String? = getName(resolveContext())

/**
 * Reads the file system path of the KmpFile, or `null` if it has none; see the overload that
 * accepts a [PlatformContext] for the per-platform behaviour.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 *
 * @return The file system path of the file, or `null` if it has none.
 */
fun KmpFile.getPath(): String? = getPath(resolveContext())

/**
 * Reads the size of the KmpFile in bytes, or `null` if it is unknown.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 */
fun KmpFile.getSize(): Long? = getSize(resolveContext())

/**
 * Reads the MIME type of the KmpFile, or `null` if it is unknown.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 */
fun KmpFile.getMimeType(): String? = getMimeType(resolveContext())

/**
 * Checks if the KmpFile is a directory.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 *
 * @param context The platform context.
 * @return True if the file is a directory, false otherwise.
 */
fun KmpFile.isDirectory(): Boolean = isDirectory(resolveContext())

/**
 * Checks if the KmpFile is a file (not a directory).
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 *
 * @param context The platform context.
 * @return True if the file is a file, false otherwise.
 */
fun KmpFile.isFile(): Boolean = !isDirectory()
