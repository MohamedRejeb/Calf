package com.mohamedrejeb.calf.io

/**
 * An typealias representing a file in the platform specific implementation
 */
expect class KmpFile

expect fun createKmpFile(path: String): KmpFile?

expect fun KmpFile.exists(): Boolean

expect fun KmpFile.readByteArray(): ByteArray

expect val KmpFile.name: String?

expect val KmpFile.path: String?

expect val KmpFile.isDirectory: Boolean

val KmpFile.isFile: Boolean
    get() = !this.isDirectory