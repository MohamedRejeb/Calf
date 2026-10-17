package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File

/**
 * Desktop apps can reopen any path they could open before, so the bookmark is the absolute path.
 * Sandboxed macOS apps would need security-scoped bookmarks, which Calf does not create.
 */
actual fun KmpFile.createBookmark(context: PlatformContext): String? =
    file.absoluteFile.takeIf { it.exists() }?.path

actual fun KmpFile.Companion.fromBookmark(bookmark: String, context: PlatformContext): KmpFile? =
    File(bookmark).takeIf { it.isAbsolute && it.exists() }?.let(::KmpFile)

/** Nothing is held on Desktop. */
actual fun KmpFile.releaseBookmark(context: PlatformContext) = Unit
