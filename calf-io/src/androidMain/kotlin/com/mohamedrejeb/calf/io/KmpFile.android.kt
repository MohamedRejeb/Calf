package com.mohamedrejeb.calf.io

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mohamedrejeb.calf.core.PlatformContext
import java.io.FileNotFoundException

actual class KmpFile(
    val uri: Uri,
)

actual fun KmpFile.exists(context: PlatformContext): Boolean =
    try {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        inputStream.close()

        true
    } catch (e: Exception) {
        false
    }

/**
 * Reads the content of the file as a byte array
 *
 * @param context the context to use to open the file
 * @throws FileNotFoundException if the file is not found
 * @return the content of the file as a byte array
 */
@Throws(FileNotFoundException::class)
actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray {
    val inputStream =
        context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("File not found")

    return inputStream.readBytes().also {
        inputStream.close()
    }
}

actual fun KmpFile.getName(context: PlatformContext): String? {
    val documentFile = DocumentFile.fromSingleUri(context, uri)

    return documentFile?.name
}

actual fun KmpFile.getPath(context: PlatformContext): String? = uri.toString()

actual fun KmpFile.isDirectory(context: PlatformContext): Boolean {
    val documentFile = DocumentFile.fromSingleUri(context, uri)

    return documentFile?.isDirectory == true
}
