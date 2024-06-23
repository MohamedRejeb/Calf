package com.mohamedrejeb.calf.picker

import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.Foundation.writeToURL

internal fun NSURL.createTempFile(): NSURL? {
    val name = lastPathComponent ?: return null
    val data = NSData.dataWithContentsOfURL(this) ?: return null
    return NSURL.fileURLWithPath("${NSTemporaryDirectory()}/${NSUUID().UUIDString}/$name").apply {
        data.writeToURL(this, true)
    }
}