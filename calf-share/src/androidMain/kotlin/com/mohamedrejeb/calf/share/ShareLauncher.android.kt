package com.mohamedrejeb.calf.share

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.mohamedrejeb.calf.core.ExperimentalCalfApi

@ExperimentalCalfApi
@Composable
actual fun rememberShareLauncher(
    onResult: (ShareResult) -> Unit,
): ShareLauncher {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        ShareLauncher(
            onLaunch = { content ->
                val intent = content.toShareIntent()
                val chooser = Intent.createChooser(intent, null)

                try {
                    context.startActivity(chooser)
                    currentOnResult(ShareResult.Success)
                } catch (_: Exception) {
                    currentOnResult(ShareResult.Unavailable)
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

private fun ShareContent.toShareIntent(): Intent =
    when (this) {
        is ShareContent.Text -> createTextShareIntent(text, subject)
        is ShareContent.Url -> createTextShareIntent(url, subject)
        is ShareContent.File -> createFileShareIntent(this)
        is ShareContent.Files -> createMultipleFilesShareIntent(this)
    }

private fun createTextShareIntent(
    text: String,
    subject: String?,
): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    }

private fun createFileShareIntent(
    content: ShareContent.File,
): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = content.mimeType
        putExtra(Intent.EXTRA_STREAM, content.file.uri)
        content.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

private fun createMultipleFilesShareIntent(
    content: ShareContent.Files,
): Intent =
    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = content.mimeType
        putParcelableArrayListExtra(
            Intent.EXTRA_STREAM,
            ArrayList(content.files.map { it.uri }),
        )
        content.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
