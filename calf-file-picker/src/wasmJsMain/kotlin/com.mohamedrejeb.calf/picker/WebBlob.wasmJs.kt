@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalWasmJsInterop::class)

package com.mohamedrejeb.calf.picker

import org.khronos.webgl.toUint8Array
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

internal actual fun createBlob(bytes: ByteArray, mimeType: String): Blob {
    val uint8 = bytes.toUByteArray().toUint8Array()
    return Blob(
        blobParts = arrayOf<JsAny?>(uint8).toJsArray(),
        options = BlobPropertyBag(type = mimeType),
    )
}
