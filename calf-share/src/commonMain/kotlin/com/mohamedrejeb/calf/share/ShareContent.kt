package com.mohamedrejeb.calf.share

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.io.KmpFile

/**
 * Represents content that can be shared using [ShareLauncher].
 */
@Immutable
sealed interface ShareContent {

    /**
     * Share plain text.
     *
     * @param text The text to share.
     * @param subject Optional subject line (used by email clients).
     */
    data class Text(
        val text: String,
        val subject: String? = null,
    ) : ShareContent

    /**
     * Share a URL. On iOS, this may show a rich link preview.
     *
     * @param url The URL string to share.
     * @param subject Optional subject line (used by email clients).
     */
    data class Url(
        val url: String,
        val subject: String? = null,
    ) : ShareContent

    /**
     * Share a single file.
     *
     * @param file The file to share.
     * @param mimeType The MIME type of the file (e.g., "image/png", "application/pdf").
     * @param subject Optional subject line (used by email clients).
     */
    data class File(
        val file: KmpFile,
        val mimeType: String,
        val subject: String? = null,
    ) : ShareContent

    /**
     * Share multiple files.
     *
     * @param files The files to share.
     * @param mimeType The MIME type of the files (e.g., `image/\*`).
     * @param subject Optional subject line (used by email clients).
     */
    data class Files(
        val files: List<KmpFile>,
        val mimeType: String,
        val subject: String? = null,
    ) : ShareContent
}
