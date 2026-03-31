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
 * Reads the path of the KmpFile.
 *
 * @param context The platform context.
 * @return The path of the file as a string.
 */
expect fun KmpFile.getPath(context: PlatformContext): String?

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
 * Reads the path of the KmpFile.
 *
 * On Android, requires that a Calf `rememberFilePickerLauncher` composable has been used first
 * to initialize the internal context. Otherwise, use the overload that accepts a [PlatformContext].
 *
 * @param context The platform context.
 * @return The path of the file as a string.
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
