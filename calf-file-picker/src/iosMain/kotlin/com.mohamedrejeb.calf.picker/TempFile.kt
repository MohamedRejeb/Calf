package com.mohamedrejeb.calf.picker

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID

@OptIn(ExperimentalForeignApi::class)
internal fun NSURL.createTempFile(): NSURL? {
    val extension = absoluteString
        ?.substringAfterLast('/')
        ?.substringAfterLast('.', "") ?: return null
    val tempUrl = NSURL.fileURLWithPath(
        "${NSTemporaryDirectory().removeSuffix("/")}/${NSUUID().UUIDString}.$extension"
    )
    val success = NSFileManager.defaultManager.copyItemAtURL(
        srcURL = this@createTempFile,
        toURL = tempUrl,
        error = null,
    )
    return if (success) tempUrl else null
}
