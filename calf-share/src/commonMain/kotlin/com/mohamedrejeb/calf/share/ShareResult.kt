package com.mohamedrejeb.calf.share

import androidx.compose.runtime.Immutable

/**
 * The result of a share operation initiated by [ShareLauncher].
 */
@Immutable
sealed interface ShareResult {

    /**
     * The content was shared successfully.
     */
    data object Success : ShareResult

    /**
     * The user dismissed the share sheet without sharing.
     */
    data object Dismissed : ShareResult

    /**
     * Sharing is not available on this platform or in this context.
     * The launcher may have used a fallback (e.g., clipboard copy).
     */
    data object Unavailable : ShareResult
}
