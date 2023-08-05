package com.mohamedrejeb.calf.ui.dialog

/**
 * Policy on setting [WindowManager.LayoutParams.FLAG_SECURE] on a window.
 */
enum class SecureFlagPolicy {
    /**
     * Inherit [WindowManager.LayoutParams.FLAG_SECURE] from the parent window and pass it on the
     * window that is using this policy.
     */
    Inherit,

    /**
     * Forces [WindowManager.LayoutParams.FLAG_SECURE] to be set on the window that is using this
     * policy.
     */
    SecureOn,
    /**
     * No [WindowManager.LayoutParams.FLAG_SECURE] will be set on the window that is using this
     * policy.
     */
    SecureOff
}

internal fun SecureFlagPolicy.shouldApplySecureFlag(isSecureFlagSetOnParent: Boolean): Boolean {
    return when (this) {
        SecureFlagPolicy.SecureOff -> false
        SecureFlagPolicy.SecureOn -> true
        SecureFlagPolicy.Inherit -> isSecureFlagSetOnParent
    }
}