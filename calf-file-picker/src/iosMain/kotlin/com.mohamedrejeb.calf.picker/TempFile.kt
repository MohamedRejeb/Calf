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
internal suspend fun NSURL.createTempFile(): NSURL? = withContext(Dispatchers.IO) {
    val extension = absoluteString
        ?.substringAfterLast('/')
        ?.substringAfterLast('.', "") ?: return@withContext null
    val tempUrl = NSURL.fileURLWithPath(
        "${NSTemporaryDirectory().removeSuffix("/")}/${NSUUID().UUIDString}.$extension"
    )
    val success = NSFileManager.defaultManager.copyItemAtURL(
        srcURL = this@createTempFile,
        toURL = tempUrl,
        error = null,
    )
    if (success) tempUrl else null
}
