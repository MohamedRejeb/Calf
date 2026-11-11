package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLConnection
import java.nio.file.Files

actual class KmpFile(
    val file: File,
) {
    actual companion object
}

actual fun KmpFile.exists(context: PlatformContext) = file.exists()

actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray = file.readBytes()

actual fun KmpFile.getName(context: PlatformContext): String? = file.name

actual fun KmpFile.getPath(context: PlatformContext): String? = file.path

actual fun KmpFile.toUriString(): String? = file.toPath().toUri().toString()

actual fun KmpFile.Companion.fromUriString(uriString: String): KmpFile? = when {
    uriString.startsWith(FILE_URI_PREFIX) -> fileFromUri(uriString)?.let(::KmpFile)
    File(uriString).isAbsolute -> KmpFile(File(uriString))
    else -> null
}

private fun fileFromUri(uriString: String): File? =
    try {
        File(URI(uriString))
    } catch (e: URISyntaxException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }

actual fun KmpFile.getSize(context: PlatformContext): Long? = file.takeIf { it.isFile }?.length()

actual fun KmpFile.getMimeType(context: PlatformContext): String? =
    URLConnection.guessContentTypeFromName(file.name.lowercase()) ?: probeContentType()

/** Asks the operating system, which also knows types the JDK's own table lacks. */
private fun KmpFile.probeContentType(): String? =
    try {
        Files.probeContentType(file.toPath())
    } catch (e: IOException) {
        null
    }

private const val FILE_URI_PREFIX = "file:"

actual fun KmpFile.isDirectory(context: PlatformContext): Boolean = file.isDirectory

/**
 * Converts this [java.io.File] to a [KmpFile].
 */
fun File.toKmpFile(): KmpFile = KmpFile(this)
