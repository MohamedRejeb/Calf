package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File

actual class KmpFile(
    val file: File,
)

actual fun KmpFile.exists(context: PlatformContext) = file.exists()

actual suspend fun KmpFile.readByteArray(context: PlatformContext): ByteArray = file.readBytes()

actual fun KmpFile.getName(context: PlatformContext): String? = file.name

actual fun KmpFile.getPath(context: PlatformContext): String? = file.path

actual fun KmpFile.isDirectory(context: PlatformContext): Boolean = file.isDirectory
