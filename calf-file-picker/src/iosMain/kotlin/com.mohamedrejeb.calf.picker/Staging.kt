package com.mohamedrejeb.calf.picker

import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.io.withSecurityScopedAccess
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.URLByDeletingLastPathComponent
import platform.Foundation.writeToURL

/**
 * Files Calf copies to or from a system sheet are staged in the app's temporary directory, each in
 * a directory of its own, so they keep their real name without colliding with each other.
 */
@OptIn(ExperimentalForeignApi::class)
private fun stagingUrl(area: String, fileName: String): NSURL {
    val directory = "${NSTemporaryDirectory().removeSuffix("/")}/$area/${NSUUID().UUIDString}"
    NSFileManager.defaultManager.createDirectoryAtPath(
        directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    return NSURL.fileURLWithPath("$directory/$fileName")
}

/**
 * Copies a picked file while the picker still grants access to it. The copy keeps the picked
 * file's name, so `KmpFile.url` is readable for as long as the app needs it and its path still
 * ends in the real name.
 *
 * @return The copy, or `null` if [source] could not be copied.
 */
internal fun stagePickedFile(source: NSURL): NSURL? {
    val fileName = source.lastPathComponent ?: return null
    return copyToStaging(source, stagingUrl(PICKER_STAGING_AREA, fileName))
}

/**
 * Writes [data] to a fresh export staging directory as [fileName]. The export picker shows, and
 * saves under, the staged file's own name, which is why the name is the one the caller asked for.
 *
 * @return The staged file, or `null` if it could not be written.
 */
internal fun stageExportData(data: NSData, fileName: String): NSURL? {
    val url = stagingUrl(SAVER_STAGING_AREA, fileName)
    if (data.writeToURL(url, atomically = true)) return url
    removeStaging(url)
    return null
}

/**
 * Copies [source] into a fresh export staging directory as [fileName]; the source is left in place.
 *
 * @return The staged copy, or `null` if [source] could not be copied.
 */
internal fun stageExportFile(source: NSURL, fileName: String): NSURL? =
    copyToStaging(source, stagingUrl(SAVER_STAGING_AREA, fileName))

@OptIn(ExperimentalForeignApi::class, InternalCalfApi::class)
private fun copyToStaging(source: NSURL, destination: NSURL): NSURL? {
    val copied = source.withSecurityScopedAccess {
        NSFileManager.defaultManager.copyItemAtURL(source, toURL = destination, error = null)
    }
    if (copied) return destination
    removeStaging(destination)
    return null
}

/** Removes a staged file together with its private directory. */
@OptIn(ExperimentalForeignApi::class)
internal fun removeStaging(stagedUrl: NSURL) {
    val directory = stagedUrl.URLByDeletingLastPathComponent ?: return
    NSFileManager.defaultManager.removeItemAtURL(directory, error = null)
}

private const val PICKER_STAGING_AREA = "calf-file-picker"
private const val SAVER_STAGING_AREA = "calf-file-saver"
