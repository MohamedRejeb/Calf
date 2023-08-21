package com.mohamedrejeb.calf.io

/**
 * An typealias representing a file in the platform specific implementation
 */
expect class KmpFile

expect fun KmpFile.exists(): Boolean

expect fun KmpFile.readByteArray(): ByteArray