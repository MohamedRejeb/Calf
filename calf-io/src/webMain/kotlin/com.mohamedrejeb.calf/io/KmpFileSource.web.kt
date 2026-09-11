package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.io.Buffer
import kotlinx.io.RawSource

actual suspend fun KmpFile.source(context: PlatformContext): RawSource =
    Buffer().apply { write(readFileAsBytes(file)) }
