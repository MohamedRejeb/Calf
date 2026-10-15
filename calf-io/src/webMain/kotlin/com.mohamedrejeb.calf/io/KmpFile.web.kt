package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import org.w3c.files.File

actual class KmpFile(
    val file: File,
) {
    actual companion object
}

actual fun KmpFile.exists(context: PlatformContext) = true

actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray =
    readFileAsBytes(file)

actual fun KmpFile.getName(context: PlatformContext): String? = file.name

/** Browsers do not expose file system paths. */
actual fun KmpFile.getPath(context: PlatformContext): String? = null

/** Browser files have no URI. */
actual fun KmpFile.toUriString(): String? = null

/** Browser files have no URI to restore them from. */
actual fun KmpFile.Companion.fromUriString(uriString: String): KmpFile? = null

actual fun KmpFile.getSize(context: PlatformContext): Long? = file.size.toDouble().toLong()

/** The browser's type for the file; it is empty when the browser does not know it. */
actual fun KmpFile.getMimeType(context: PlatformContext): String? = file.type.ifEmpty { null }

actual fun KmpFile.isDirectory(context: PlatformContext): Boolean = !file.name.contains(".")

internal expect suspend fun readFileAsBytes(file: File): ByteArray

/**
 * Converts this [org.w3c.files.File] to a [KmpFile].
 */
fun File.toKmpFile(): KmpFile = KmpFile(this)
