package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.io.RawSource
import kotlinx.io.asSource
import java.io.FileNotFoundException

actual suspend fun KmpFile.source(context: PlatformContext): RawSource =
    context.contentResolver.openInputStream(uri)?.asSource()
        ?: throw FileNotFoundException("Unable to open $uri")
