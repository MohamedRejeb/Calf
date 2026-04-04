package com.mohamedrejeb.calf.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.core.ExperimentalCalfApi

@ExperimentalCalfApi
@Composable
actual fun rememberShareLauncher(
    onResult: (ShareResult) -> Unit,
): ShareLauncher {
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        ShareLauncher(
            onLaunch = { content ->
                handleShare(content, currentOnResult)
            },
        )
    }
}

@ExperimentalCalfApi
@Stable
actual class ShareLauncher actual constructor(
    private val onLaunch: (ShareContent) -> Unit,
) {
    actual fun launch(content: ShareContent) {
        onLaunch(content)
    }
}

private fun handleShare(content: ShareContent, onResult: (ShareResult) -> Unit) {
    when (content) {
        is ShareContent.Text -> shareText(content.text, content.subject, onResult)
        is ShareContent.Url -> shareUrl(content.url, content.subject, onResult)
        is ShareContent.File -> shareFile(content, onResult)
        is ShareContent.Files -> shareFiles(content, onResult)
    }
}

/**
 * Maps the Web Share API promise result to [ShareResult].
 * - `true` → Success (share completed)
 * - `false` → Dismissed (AbortError — user cancelled)
 * - `null` → error (e.g., InvalidStateError from a share already in progress)
 */
private fun mapWebShareResult(result: Boolean?): ShareResult =
    when (result) {
        true -> ShareResult.Success
        false -> ShareResult.Dismissed
        null -> ShareResult.Unavailable
    }

private fun shareText(text: String, subject: String?, onResult: (ShareResult) -> Unit) {
    if (isWebShareSupported()) {
        webShareText(text, subject) { result ->
            onResult(mapWebShareResult(result))
        }
        return
    }

    // Fall back to clipboard copy
    onResult(
        try {
            copyToClipboard(text)
            ShareResult.Success
        } catch (_: Exception) {
            ShareResult.Unavailable
        }
    )
}

private fun shareUrl(url: String, subject: String?, onResult: (ShareResult) -> Unit) {
    if (isWebShareSupported()) {
        webShareUrl(url, subject) { result ->
            onResult(mapWebShareResult(result))
        }
        return
    }

    // Fall back to clipboard copy
    onResult(
        try {
            copyToClipboard(url)
            ShareResult.Success
        } catch (_: Exception) {
            ShareResult.Unavailable
        }
    )
}

private fun shareFile(content: ShareContent.File, onResult: (ShareResult) -> Unit) {
    if (isWebShareSupported()) {
        webShareFile(content.file.file) { result ->
            onResult(mapWebShareResult(result))
        }
        return
    }
    onResult(ShareResult.Unavailable)
}

private fun shareFiles(content: ShareContent.Files, onResult: (ShareResult) -> Unit) {
    if (isWebShareSupported()) {
        webShareFiles(content.files.map { it.file }) { result ->
            onResult(mapWebShareResult(result))
        }
        return
    }
    onResult(ShareResult.Unavailable)
}
