package com.mohamedrejeb.calf.io

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

/**
 * A typealias representing a file in the platform specific implementation
 */
actual typealias KmpFile = NSData

actual fun KmpFile.exists() = true

@OptIn(ExperimentalForeignApi::class)
actual fun KmpFile.readByteArray(): ByteArray = ByteArray(this@readByteArray.length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), this@readByteArray.bytes, this@readByteArray.length)
    }
}