package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import kotlin.concurrent.AtomicInt

/**
 * A bookmark of the location the user picked. iOS needs no bookmark flags for this: the
 * security-scope flags exist only on macOS.
 */
@OptIn(ExperimentalForeignApi::class, InternalCalfApi::class)
actual fun KmpFile.createBookmark(context: PlatformContext): String? {
    val data = originalUrl.withSecurityScopedAccess {
        originalUrl.bookmarkDataWithOptions(
            options = 0u,
            includingResourceValuesForKeys = null,
            relativeToURL = null,
            error = null,
        )
    } ?: return null
    return data.base64EncodedStringWithOptions(0u)
}

/**
 * The reopened file starts its security-scoped access at once and holds it until
 * [releaseBookmark]. Starting access per operation on paths rebuilt from the URL is not enough
 * after a device restart, while access held on the resolved URL covers the files inside a folder.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun KmpFile.Companion.fromBookmark(bookmark: String, context: PlatformContext): KmpFile? {
    if (bookmark.isEmpty()) return null
    val data = NSData.create(base64EncodedString = bookmark, options = 0u) ?: return null
    val url = NSURL.URLByResolvingBookmarkData(
        bookmarkData = data,
        options = 0u,
        relativeToURL = null,
        bookmarkDataIsStale = null,
        error = null,
    ) ?: return null
    return KmpFile(url, SecurityScopedAccess(url))
}

actual fun KmpFile.releaseBookmark(context: PlatformContext) {
    bookmarkAccess?.release()
}

/** Security-scoped access started once on [url] and stopped at most once. */
internal class SecurityScopedAccess(private val url: NSURL) {
    private val held = AtomicInt(if (url.startAccessingSecurityScopedResource()) 1 else 0)

    fun release() {
        if (held.compareAndSet(1, 0)) url.stopAccessingSecurityScopedResource()
    }
}
