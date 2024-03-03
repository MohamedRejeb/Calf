package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import org.w3c.files.File
import org.w3c.files.FileReader

/**
 * A typealias representing a file in the platform specific implementation
 */
actual class KmpFile(
    val file: File,
)

actual fun KmpFile.exists(context: PlatformContext) = true

actual fun KmpFile.readByteArray(context: PlatformContext): ByteArray {
    val fileReader = FileReader()
    fileReader.onload = {
        val arrayBuffer = it.target.asDynamic().result as String
        ByteArray(arrayBuffer.length) { index ->
            arrayBuffer[index].code.toByte()
        }
    }
    fileReader.readAsText(file)
    return fileReader.result as ByteArray
}

actual fun KmpFile.getName(context: PlatformContext): String? = file.name

actual fun KmpFile.getPath(context: PlatformContext): String? = file.name

actual fun KmpFile.isDirectory(context: PlatformContext): Boolean = !file.name.contains(".")
