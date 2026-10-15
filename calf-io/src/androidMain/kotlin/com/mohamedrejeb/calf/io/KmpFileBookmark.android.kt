package com.mohamedrejeb.calf.io

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File

/** Persists read and write access where the grant allows both, and read access otherwise. */
actual fun KmpFile.createBookmark(context: PlatformContext): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT ->
        if (context.persist(uri, READ_WRITE) || context.persist(uri, READ)) uri.toString() else null
    ContentResolver.SCHEME_FILE -> uri.toString().takeIf { uri.existingFile() != null }
    else -> null
}

actual fun KmpFile.Companion.fromBookmark(bookmark: String, context: PlatformContext): KmpFile? {
    val uri = Uri.parse(bookmark)
    val stillAccessible = when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT ->
            context.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
        ContentResolver.SCHEME_FILE -> uri.existingFile() != null
        else -> false
    }
    return if (stillAccessible) KmpFile(uri) else null
}

actual fun KmpFile.releaseBookmark(context: PlatformContext) {
    if (uri.scheme != ContentResolver.SCHEME_CONTENT) return
    val persisted = context.contentResolver.persistedUriPermissions.firstOrNull { it.uri == uri } ?: return
    val flags = (if (persisted.isReadPermission) READ else 0) or (if (persisted.isWritePermission) WRITE else 0)
    try {
        context.contentResolver.releasePersistableUriPermission(uri, flags)
    } catch (e: SecurityException) {
        // The permission is already gone.
    }
}

/** A grant that was not made persistable, such as one from GetContent, cannot be persisted. */
private fun PlatformContext.persist(uri: Uri, flags: Int): Boolean =
    try {
        contentResolver.takePersistableUriPermission(uri, flags)
        true
    } catch (e: SecurityException) {
        false
    }

private fun Uri.existingFile(): File? = path?.let(::File)?.takeIf { it.exists() }

private const val READ = Intent.FLAG_GRANT_READ_URI_PERMISSION
private const val WRITE = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
private const val READ_WRITE = READ or WRITE
