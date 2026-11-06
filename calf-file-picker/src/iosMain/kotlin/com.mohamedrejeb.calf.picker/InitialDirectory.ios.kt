package com.mohamedrejeb.calf.picker

import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.fromUriString
import com.mohamedrejeb.calf.io.toUriString
import platform.Foundation.NSURL

/** The document picker opens at a URL; the original URL is the folder the user picked. */
internal actual fun KmpFile.toInitialDirectory(): String? = toUriString()

/**
 * Resolves an `initialDirectory` setting to the document picker's `directoryURL`. It accepts what
 * [KmpFile.Companion.fromUriString] does: the URL that a picked folder gives, and an absolute path,
 * as code shared with Desktop would pass.
 */
internal fun initialDirectoryUrl(initialDirectory: String?): NSURL? =
    initialDirectory?.let { KmpFile.fromUriString(it) }?.originalUrl
