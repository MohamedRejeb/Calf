package com.mohamedrejeb.calf.share

import platform.Foundation.NSURL

/**
 * Converts [ShareContent] to a list of activity items suitable for
 * [platform.UIKit.UIActivityViewController].
 */
internal fun ShareContent.toActivityItems(): List<Any> =
    when (this) {
        is ShareContent.Text ->
            listOf(text)

        is ShareContent.Url -> {
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl != null) listOf(nsUrl) else listOf(url)
        }

        is ShareContent.File ->
            listOf(file.url)

        is ShareContent.Files ->
            files.map { it.url }
    }
