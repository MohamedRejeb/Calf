package com.mohamedrejeb.calf.sample.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.toImageBitmap
import com.mohamedrejeb.calf.sample.utils.toByteArray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * Reads [file], crops the centre square of the image to a circle and encodes it as a PNG, whose
 * transparent corners keep the circle. Fails when the file cannot be read or decoded.
 */
internal suspend fun circleCropPng(file: KmpFile): Result<ByteArray> =
    try {
        val bytes = file.readByteArray()
        Result.success(withContext(Dispatchers.Default) { circleCrop(bytes) })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

private fun circleCrop(bytes: ByteArray): ByteArray {
    val source = bytes.toImageBitmap()
    val side = min(source.width, source.height)
    val output = ImageBitmap(side, side)
    val canvas = Canvas(output)

    canvas.clipPath(
        Path().apply {
            addOval(Rect(offset = Offset.Zero, size = Size(side.toFloat(), side.toFloat())))
        },
    )
    // Centre crop: the middle square of a landscape or portrait photo.
    canvas.drawImageRect(
        image = source,
        srcOffset = IntOffset((source.width - side) / 2, (source.height - side) / 2),
        srcSize = IntSize(side, side),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(side, side),
        paint = Paint(),
    )
    return output.toByteArray()
}
