package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.core.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSLock
import platform.Foundation.NSURL
import platform.Foundation.URLByStandardizingPath
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
 * The reopened file holds security-scoped access to its location until [releaseBookmark].
 * Starting access per operation on paths rebuilt from the URL is not enough after a device
 * restart, while access held on the resolved URL covers the files inside a folder. Files reopened
 * from the same location share one access, which stops when the last of them is released.
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
    return KmpFile(url, SecurityScopedAccessRegistry.Shared.acquire(url))
}

actual fun KmpFile.releaseBookmark(context: PlatformContext) {
    bookmarkAccess?.release()
}

/**
 * One file's claim on the access to its location, given up at most once.
 */
internal class SecurityScopedAccess(private val onRelease: () -> Unit) {
    private val held = AtomicInt(1)

    fun release() {
        if (held.compareAndSet(1, 0)) onRelease()
    }
}

/**
 * Counts the files holding access to each location, so access starts once per location and stops
 * when the last file is released. Every start must be balanced by a stop, or the app leaks kernel
 * resources and eventually cannot open more security-scoped locations until it is relaunched.
 */
internal class SecurityScopedAccessRegistry(
    private val start: (NSURL) -> Boolean,
    private val stop: (NSURL) -> Unit,
) {
    private class Location(val url: NSURL, val isAccessing: Boolean, val holders: Int)

    private val lock = NSLock()
    private var locations: Map<String, Location> = emptyMap()

    fun acquire(url: NSURL): SecurityScopedAccess {
        val key = url.locationKey()
        locked {
            val location = locations[key]
            locations = locations + (key to when (location) {
                null -> Location(url, isAccessing = start(url), holders = 1)
                // A start that failed earlier is retried, so one failure does not stick to the location.
                else -> Location(location.url, location.isAccessing || start(location.url), location.holders + 1)
            })
        }
        return SecurityScopedAccess { release(key) }
    }

    private fun release(key: String) {
        val stopped = locked {
            val location = locations[key] ?: return@locked null
            if (location.holders > 1) {
                locations = locations + (key to Location(location.url, location.isAccessing, location.holders - 1))
                null
            } else {
                locations = locations - key
                location.takeIf { it.isAccessing }
            }
        }
        stopped?.let { stop(it.url) }
    }

    private inline fun <T> locked(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }

    private fun NSURL.locationKey(): String = (URLByStandardizingPath ?: this).absoluteString ?: toString()

    companion object {
        val Shared = SecurityScopedAccessRegistry(
            start = { it.startAccessingSecurityScopedResource() },
            stop = { it.stopAccessingSecurityScopedResource() },
        )
    }
}
