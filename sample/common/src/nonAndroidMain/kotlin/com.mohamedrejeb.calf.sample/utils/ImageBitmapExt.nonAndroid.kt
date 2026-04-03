package com.mohamedrejeb.calf.sample.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.toByteArray(): ByteArray {
    return Image
        .makeFromBitmap(this.asSkiaBitmap())
        .encodeToData(
            format = EncodedImageFormat.PNG,
            quality = 100,
        )!!
        .bytes
}