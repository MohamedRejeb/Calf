package com.mohamedrejeb.calf.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.share.platform.NativeShareBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

@ExperimentalCalfApi
@Composable
actual fun rememberShareLauncher(
    onResult: (ShareResult) -> Unit,
): ShareLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        ShareLauncher(
            onLaunch = { content ->
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        handleShare(content)
                    }
                    currentOnResult(result)
                }
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

private fun handleShare(content: ShareContent): ShareResult {
    // Try native share sheet (macOS — uses NSApp.keyWindow automatically)
    try {
        if (NativeShareBridge.isNativeShareSupported()) {
            val nativeResult = showNativeShare(content)
            if (nativeResult != null) return nativeResult
        }
    } catch (_: Throwable) {
        // Native lib failed to load or call failed — fall through to fallback
    }

    // Fallback for non-macOS or if native share is unavailable
    return handleFallbackShare(content)
}

private fun showNativeShare(content: ShareContent): ShareResult? {
    val (text, url, filePaths) = when (content) {
        is ShareContent.Text -> Triple(content.text, null, null)
        is ShareContent.Url -> Triple(null, content.url, null)
        is ShareContent.File -> Triple(null, null, arrayOf(content.file.file.absolutePath))
        is ShareContent.Files -> Triple(null, null, content.files.map { it.file.absolutePath }.toTypedArray())
    }

    return try {
        val result = NativeShareBridge.showShareSheet(
            text = text,
            url = url,
            filePaths = filePaths,
            parentWindow = 0L, // Rust gets the focused window via NSApp.keyWindow
        )
        when (result) {
            NativeShareBridge.RESULT_SUCCESS -> ShareResult.Success
            NativeShareBridge.RESULT_DISMISSED -> ShareResult.Dismissed
            else -> null // Fall through to fallback
        }
    } catch (_: Exception) {
        null // Fall through to fallback
    }
}

private fun handleFallbackShare(content: ShareContent): ShareResult =
    when (content) {
        is ShareContent.Text -> shareTextFallback(content.text)
        is ShareContent.Url -> shareUrlFallback(content.url)
        is ShareContent.File -> shareFileFallback(content)
        is ShareContent.Files -> shareFilesFallback(content)
    }

private fun shareTextFallback(text: String): ShareResult {
    return try {
        copyToClipboard(text)
        ShareResult.Success
    } catch (_: Exception) {
        ShareResult.Unavailable
    }
}

private fun shareUrlFallback(url: String): ShareResult {
    return try {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(url))
            ShareResult.Success
        } else {
            copyToClipboard(url)
            ShareResult.Success
        }
    } catch (_: Exception) {
        try {
            copyToClipboard(url)
            ShareResult.Success
        } catch (_: Exception) {
            ShareResult.Unavailable
        }
    }
}

private fun shareFileFallback(content: ShareContent.File): ShareResult {
    return try {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(content.file.file)
            ShareResult.Success
        } else {
            ShareResult.Unavailable
        }
    } catch (_: Exception) {
        ShareResult.Unavailable
    }
}

private fun shareFilesFallback(content: ShareContent.Files): ShareResult {
    return try {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            content.files.forEach { kmpFile ->
                desktop.open(kmpFile.file)
            }
            ShareResult.Success
        } else {
            ShareResult.Unavailable
        }
    } catch (_: Exception) {
        ShareResult.Unavailable
    }
}

private fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, selection)
}
