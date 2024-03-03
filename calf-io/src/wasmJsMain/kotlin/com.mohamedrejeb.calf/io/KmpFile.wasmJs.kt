package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Represents a file in a platform-agnostic way.
 */
actual class KmpFile(
    val file: File,
)

actual fun KmpFile.exists(context: PlatformContext) = true

actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray =
    suspendCoroutine { continuation ->
        val fileReader = FileReader()
        fileReader.readAsArrayBuffer(file)
        fileReader.onloadend = { event ->
            val readyState = getReadyState(event)
            if (readyState == FileReader.DONE) {
                val arrayBuffer: ArrayBuffer = getResult(event)
                val array = Uint8Array(arrayBuffer)
                val byteArray =
                    ByteArray(array.length) { index ->
                        array[index]
                    }
                continuation.resume(byteArray)
            } else {
                continuation.resume(ByteArray(0))
            }
        }
    }

actual fun KmpFile.getName(context: PlatformContext): String? = file.name

actual fun KmpFile.getPath(context: PlatformContext): String? = file.name

actual fun KmpFile.isDirectory(context: PlatformContext): Boolean = !file.name.contains(".")

private fun getReadyState(event: Event): Short = js("event.target.readyState")

private fun getResult(event: Event): ArrayBuffer = js("event.target.result")
