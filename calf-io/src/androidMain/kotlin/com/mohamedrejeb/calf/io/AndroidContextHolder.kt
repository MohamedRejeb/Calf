package com.mohamedrejeb.calf.io

import android.annotation.SuppressLint
import android.content.Context
import com.mohamedrejeb.calf.core.InternalCalfApi

/**
 * Internal holder for the Android application context.
 * Set once automatically from Calf composables. Used by context-free
 * [KmpFile] extension functions so callers don't need to pass context.
 */
@SuppressLint("StaticFieldLeak")
internal object AndroidContextHolder {

    @Volatile
    private var _context: Context? = null

    val context: Context
        get() = _context
            ?: error(
                "Android context is not available. " +
                    "Use a Calf composable (e.g. rememberFilePickerLauncher) first, " +
                    "or call the overload that accepts a PlatformContext parameter."
            )

    fun initialize(context: Context) {
        if (_context == null) {
            _context = context.applicationContext
        }
    }
}

/**
 * Initializes the internal Android application context used by context-free [KmpFile] extensions.
 * Called automatically by Calf composables — not intended for external use.
 */
@InternalCalfApi
fun initializeKmpFileContext(context: Context) {
    AndroidContextHolder.initialize(context)
}
