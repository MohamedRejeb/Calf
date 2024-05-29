package com.mohamedrejeb.calf.picker

import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL

internal fun NSURL.createTempFile(): NSURL? {
    val extension = absoluteString?.substringAfterLast('.', "") ?: return null
    val data = NSData.dataWithContentsOfURL(this) ?: return null
    return NSURL.fileURLWithPath("${NSTemporaryDirectory()}/${NSUUID().UUIDString}.$extension").apply {
        data.writeToURL(this, true)
    }
}