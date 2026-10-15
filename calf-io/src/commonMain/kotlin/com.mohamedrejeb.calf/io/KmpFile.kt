package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext

/**
 * A wrapper class for a file in the platform specific implementation.
 */
expect class KmpFile

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
 * The result is a decoded path, usable with kotlinx-io's `Path` or `java.io.File`, or `null` when
 * the file has no file system path:
 * - **Android**: the path of a `file:` URI; `null` for `content:` URIs, which is what the system
 *   pickers return. Read those with [readByteArray] or `source()`, or use [toUriString].
 * - **iOS**: the path of the original `file:` URL; `null` for any other URL. For a picked file
 *   that location is security scoped, so read the content with [readByteArray] or `source()`,
 *   which use the app's own copy.
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
 */
expect fun KmpFile.toUriString(): String?

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
