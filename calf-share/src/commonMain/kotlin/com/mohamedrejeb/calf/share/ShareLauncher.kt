package com.mohamedrejeb.calf.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.mohamedrejeb.calf.core.ExperimentalCalfApi

/**
 * Creates and remembers a [ShareLauncher] for sharing content using the platform's native
 * share mechanism.
 *
 * On Android, this opens `Intent.createChooser()`. On iOS, it presents a
 * `UIActivityViewController`. On Desktop, it copies to clipboard or opens URLs in the browser.
 * On Web, it uses the `navigator.share()` API with a clipboard fallback.
 *
 * @param onResult Callback invoked with the [ShareResult] after the share operation completes.
 */
@ExperimentalCalfApi
@Composable
expect fun rememberShareLauncher(
    onResult: (ShareResult) -> Unit,
): ShareLauncher

/**
 * A launcher that presents the platform's native share sheet for sharing content.
 *
 * Create an instance using [rememberShareLauncher].
 *
 * @param onLaunch Called when [launch] is invoked, with the [ShareContent] to share.
 */
@ExperimentalCalfApi
@Stable
expect class ShareLauncher(
    onLaunch: (ShareContent) -> Unit,
) {
    /**
     * Launch the share sheet with the given [content].
     *
     * @param content The content to share. See [ShareContent] for supported types.
     */
    fun launch(content: ShareContent)
}
