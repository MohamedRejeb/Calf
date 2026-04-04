package com.mohamedrejeb.calf.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
@ExperimentalCalfApi
@Composable
actual fun rememberShareLauncher(
    onResult: (ShareResult) -> Unit,
): ShareLauncher {
    val currentUIViewController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)

    return remember(currentUIViewController) {
        ShareLauncher(
            onLaunch = { content ->
                presentShareSheet(
                    viewController = currentUIViewController,
                    content = content,
                    onResult = { currentOnResult(it) },
                )
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

@OptIn(ExperimentalForeignApi::class)
private fun presentShareSheet(
    viewController: UIViewController,
    content: ShareContent,
    onResult: (ShareResult) -> Unit,
) {
    val activityItems = content.toActivityItems()
    val activityViewController = UIActivityViewController(
        activityItems = activityItems,
        applicationActivities = null,
    )

    // Set subject for email clients if available
    val subject = when (content) {
        is ShareContent.Text -> content.subject
        is ShareContent.Url -> content.subject
        is ShareContent.File -> content.subject
        is ShareContent.Files -> content.subject
    }
    if (subject != null) {
        activityViewController.title = subject
    }

    // Handle completion
    activityViewController.completionWithItemsHandler = { _, completed, _, _ ->
        if (completed) {
            onResult(ShareResult.Success)
        } else {
            onResult(ShareResult.Dismissed)
        }
    }

    // iPad safety: configure popover presentation controller to prevent crash
    activityViewController.popoverPresentationController?.let { popover ->
        popover.sourceView = viewController.view
        popover.sourceRect = viewController.view.bounds.useContents {
            CGRectMake(
                x = origin.x + size.width / 2.0,
                y = origin.y + size.height / 2.0,
                width = 0.0,
                height = 0.0,
            )
        }
        popover.permittedArrowDirections = 0uL // No arrow
    }

    viewController.presentViewController(
        activityViewController,
        animated = true,
        completion = null,
    )
}
