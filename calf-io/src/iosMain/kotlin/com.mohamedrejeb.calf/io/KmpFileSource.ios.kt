package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.io.RawSource
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/** The file is opened inside its security-scoped access; an open file stays readable after it. */
@OptIn(InternalCalfApi::class)
actual suspend fun KmpFile.source(context: PlatformContext): RawSource {
    val path = url.path ?: throw FileNotFoundException("No file system path for $url")
    return url.withSecurityScopedAccess { SystemFileSystem.source(Path(path)) }
}
