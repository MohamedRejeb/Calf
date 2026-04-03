package com.mohamedrejeb.calf.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal actual suspend fun readFileAsBytes(file: File): ByteArray =
    suspendCoroutine { continuation ->
        val fileReader = FileReader()
        fileReader.readAsArrayBuffer(file)
        fileReader.onloadend = { event ->
            if (event.target.asDynamic().readyState == FileReader.DONE) {
                val arrayBuffer: ArrayBuffer = event.target.asDynamic().result
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
