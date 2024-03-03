package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext

/**
 * Represents a file in a platform-agnostic way.
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
expect fun KmpFile.readByteArray(context: PlatformContext): ByteArray

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
