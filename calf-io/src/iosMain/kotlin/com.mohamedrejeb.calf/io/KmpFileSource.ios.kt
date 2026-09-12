package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.io.RawSource
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

actual suspend fun KmpFile.source(context: PlatformContext): RawSource {
    val path = url.path ?: throw FileNotFoundException("No file system path for $url")
    return SystemFileSystem.source(Path(path))
}
