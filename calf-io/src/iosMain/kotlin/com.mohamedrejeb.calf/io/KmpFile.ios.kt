package com.mohamedrejeb.calf.io

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.posix.memcpy

/**
 * A typealias representing a file in the platform specific implementation
 */
@Suppress("CONFLICTING_OVERLOADS")
actual typealias KmpFile = NSURL

actual fun createKmpFile(path: String): KmpFile? = NSURL(fileURLWithPath = path)

actual fun KmpFile.exists(): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(this.path ?: return false)
}

@OptIn(ExperimentalForeignApi::class)
actual fun KmpFile.readByteArray(): ByteArray {
    val data = NSData.dataWithContentsOfURL(this) ?: return ByteArray(0)
    val byteArraySize: Int = if (data.length > Int.MAX_VALUE.toUInt()) Int.MAX_VALUE else data.length .toInt()
    return ByteArray(byteArraySize).apply {
        usePinned {
            memcpy(it.addressOf(0), data.bytes, data.length)
        }
    }
}

actual val KmpFile.name: String?
    get() = this.lastPathComponent

actual val KmpFile.path: String?
    get() = this.path

actual val KmpFile.isDirectory: Boolean
    get() = !this.path.orEmpty().contains(".")