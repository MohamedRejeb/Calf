package com.mohamedrejeb.calf.io

import org.khronos.webgl.ArrayBuffer
import org.w3c.files.File
import org.w3c.files.FileReader

/**
 * A typealias representing a file in the platform specific implementation
 */
actual typealias KmpFile = File

actual fun createKmpFile(path: String): KmpFile? = null

actual fun KmpFile.exists() = true

actual fun KmpFile.readByteArray(): ByteArray {
    val fileReader = FileReader()
    fileReader.onload = {
        val arrayBuffer = it.target.asDynamic().result as String
        ByteArray(arrayBuffer.length) { index ->
            arrayBuffer[index].code.toByte()
        }
    }
    fileReader.readAsText(this)
    return fileReader.result as ByteArray
}

actual val KmpFile.name: String?
    get() = this.name

actual val KmpFile.path: String?
    get() = this.name

actual val KmpFile.isDirectory: Boolean
    get() = !this.name.contains(".")