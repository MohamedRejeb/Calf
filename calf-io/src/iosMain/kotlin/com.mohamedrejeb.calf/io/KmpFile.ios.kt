package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSFileSize
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeRegular
import platform.Foundation.NSNumber
import platform.Foundation.NSURLIsDirectoryKey
import platform.UniformTypeIdentifiers.UTType
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

/**
 * A wrapper class for a file in the platform-specific implementation.
 *
 * @property url A persistent copy of the file that remains accessible after the picker callback.
 * It keeps the picked file's name, and [getPath] returns its path.
 * @property originalUrl The original URL returned by the file picker. May become inaccessible
 * due to iOS security scoping — use for metadata only ([getName], [toUriString]).
 */
actual class KmpFile @InternalCalfApi constructor(
    val url: NSURL,
    val originalUrl: NSURL,
) {
    @OptIn(InternalCalfApi::class)
    constructor(url: NSURL) : this(originalUrl = url, url = url)

    actual companion object
}

actual fun KmpFile.exists(context: PlatformContext): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(url.path ?: return false)
}

@OptIn(ExperimentalForeignApi::class, InternalCalfApi::class)
actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray {
    val data = url.withSecurityScopedAccess { NSData.dataWithContentsOfURL(url) } ?: return ByteArray(0)
    val byteArraySize: Int = if (data.length > Int.MAX_VALUE.toUInt()) Int.MAX_VALUE else data.length.toInt()
    return ByteArray(byteArraySize).apply {
        usePinned {
            memcpy(it.addressOf(0), data.bytes, data.length)
        }
    }
}

actual fun KmpFile.getName(context: PlatformContext): String? =
    originalUrl.lastPathComponent

/**
 * The path of [KmpFile.url], the copy the app can read, so the path can be opened directly; a
 * picked file's copy keeps the picked file's name. Only a `file:` URL has a path, and
 * [NSURL.path] is already percent-decoded.
 */
actual fun KmpFile.getPath(context: PlatformContext): String? =
    url.takeIf { it.isFileURL() }?.path

/** The URL of the original file, for example to reopen a picked folder later. */
actual fun KmpFile.toUriString(): String? = originalUrl.absoluteString

actual fun KmpFile.Companion.fromUriString(uriString: String): KmpFile? {
    val url = when {
        uriString.startsWith(FILE_URL_PREFIX) -> NSURL(string = uriString)
        uriString.startsWith(PATH_SEPARATOR) -> NSURL.fileURLWithPath(uriString)
        else -> null
    }
    return url?.takeIf { it.isFileURL() }?.let(::KmpFile)
}

/** The size of the readable file; a directory has none. */
@OptIn(ExperimentalForeignApi::class)
actual fun KmpFile.getSize(context: PlatformContext): Long? {
    val path = url.path ?: return null
    val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null) ?: return null
    if (attributes[NSFileType] != NSFileTypeRegular) return null
    return (attributes[NSFileSize] as? NSNumber)?.longLongValue
}

/** The MIME type registered for the original file's extension. */
@OptIn(ExperimentalForeignApi::class)
actual fun KmpFile.getMimeType(context: PlatformContext): String? {
    val extension = originalUrl.pathExtension?.lowercase()?.takeIf { it.isNotEmpty() } ?: return null
    return UTType.typeWithFilenameExtension(extension)?.preferredMIMEType
}

private const val FILE_URL_PREFIX = "file:"
private const val PATH_SEPARATOR = "/"

@OptIn(ExperimentalForeignApi::class)
actual fun KmpFile.isDirectory(context: PlatformContext): Boolean {
    val result = originalUrl.resourceValuesForKeys(listOf(NSURLIsDirectoryKey), error = null)
    return result?.get(NSURLIsDirectoryKey) == true
}

/**
 * Runs [block] with access to this URL's security-scoped resource, so a [KmpFile] built from a
 * picker URL by hand can still be read after the picker callback. A URL that is not security
 * scoped is accessed as is.
 */
@InternalCalfApi
inline fun <T> NSURL.withSecurityScopedAccess(block: () -> T): T {
    val accessing = startAccessingSecurityScopedResource()
    try {
        return block()
    } finally {
        if (accessing) stopAccessingSecurityScopedResource()
    }
}

/**
 * Converts this [NSURL] to a [KmpFile].
 */
fun NSURL.toKmpFile(): KmpFile = KmpFile(this)
