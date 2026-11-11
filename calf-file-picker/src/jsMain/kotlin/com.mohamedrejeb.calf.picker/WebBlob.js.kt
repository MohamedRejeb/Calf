package com.mohamedrejeb.calf.picker

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FilePropertyBag

internal actual fun createBlob(bytes: ByteArray, mimeType: String): Blob {
    val jsArray = Int8Array(bytes.size)
    for (i in bytes.indices) {
        jsArray.asDynamic()[i] = bytes[i]
    }
    val uint8 = Uint8Array(jsArray.buffer)
    return Blob(
        blobParts = arrayOf(uint8),
        options = BlobPropertyBag(type = mimeType),
    )
}

internal actual fun createFile(blob: Blob, fileName: String, mimeType: String): File =
    File(fileBits = arrayOf(blob), fileName = fileName, options = FilePropertyBag(type = mimeType))
