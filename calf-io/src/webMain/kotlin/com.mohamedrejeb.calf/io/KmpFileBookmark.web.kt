package com.mohamedrejeb.calf.io

import com.mohamedrejeb.calf.core.PlatformContext

/** A browser file cannot be reopened after the page reloads. */
actual fun KmpFile.createBookmark(context: PlatformContext): String? = null

actual fun KmpFile.Companion.fromBookmark(bookmark: String, context: PlatformContext): KmpFile? = null

/** Nothing is held in the browser. */
actual fun KmpFile.releaseBookmark(context: PlatformContext) = Unit
