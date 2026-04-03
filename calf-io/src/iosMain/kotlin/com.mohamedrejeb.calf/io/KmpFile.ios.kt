package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsDirectoryKey
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

/**
 * A wrapper class for a file in the platform-specific implementation.
 *
 * @property url A persistent copy of the file that remains accessible after the picker callback.
 * @property originalUrl The original URL returned by the file picker. May become inaccessible
 * due to iOS security scoping — use for metadata only (file name, path).
 */
actual class KmpFile @InternalCalfApi constructor(
    val url: NSURL,
    val originalUrl: NSURL,
) {
    @OptIn(InternalCalfApi::class)
    constructor(url: NSURL) : this(originalUrl = url, url = url)
}

actual fun KmpFile.exists(context: PlatformContext): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(url.path ?: return false)
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray {
    val data = NSData.dataWithContentsOfURL(url) ?: return ByteArray(0)
    val byteArraySize: Int = if (data.length > Int.MAX_VALUE.toUInt()) Int.MAX_VALUE else data.length.toInt()
    return ByteArray(byteArraySize).apply {
        usePinned {
            memcpy(it.addressOf(0), data.bytes, data.length)
        }
    }
}

actual fun KmpFile.getName(context: PlatformContext): String? =
    originalUrl.absoluteString
        ?.removeSuffix("/")
        ?.split('/')
        ?.lastOrNull()

actual fun KmpFile.getPath(context: PlatformContext): String? =
    originalUrl.absoluteString

@OptIn(ExperimentalForeignApi::class)
actual fun KmpFile.isDirectory(context: PlatformContext): Boolean {
    val result = originalUrl.resourceValuesForKeys(listOf(NSURLIsDirectoryKey), error = null)
    return result?.get(NSURLIsDirectoryKey) == true
}
