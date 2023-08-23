package com.mohamedrejeb.calf.io

import java.io.File

/**
 * An typealias representing a file in the platform specific implementation
 */
actual typealias KmpFile = File

actual fun KmpFile.exists() = this.exists()

actual fun KmpFile.readByteArray(): ByteArray = this.readBytes()