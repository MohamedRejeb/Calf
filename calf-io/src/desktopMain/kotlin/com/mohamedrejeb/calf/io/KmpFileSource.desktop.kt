package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

actual suspend fun KmpFile.source(context: PlatformContext): RawSource =
    SystemFileSystem.source(Path(file.absolutePath))
