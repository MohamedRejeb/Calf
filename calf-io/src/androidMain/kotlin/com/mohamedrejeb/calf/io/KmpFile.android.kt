package com.mohamedrejeb.calf.io

import java.io.File

/**
 * An typealias representing a file in the platform specific implementation
 */
actual typealias KmpFile = File

actual fun createKmpFile(path: String): KmpFile? = File(path)

actual fun KmpFile.exists() = this.exists()

actual fun KmpFile.readByteArray(): ByteArray = this.readBytes()

actual val KmpFile.name: String?
    get() = this.name

actual val KmpFile.path: String?
    get() = this.path

actual val KmpFile.isDirectory: Boolean
    get() = this.isDirectory