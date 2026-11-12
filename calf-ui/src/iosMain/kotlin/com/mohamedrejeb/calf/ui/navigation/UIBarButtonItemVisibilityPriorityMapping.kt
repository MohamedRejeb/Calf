@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.navigation

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.Foundation.NSNumber
import platform.Foundation.setValue
import platform.UIKit.UIBarButtonItem
import platform.objc.sel_registerName
import platform.posix.RTLD_DEFAULT
import platform.posix.dlsym

/**
 * Applies a [UIKitUIBarButtonItemVisibilityPriority] to a native item.
 *
 * `UIBarButtonItem.visibilityPriority` is an iOS 27 property that the Kotlin/Native
 * platform libraries (built from the iOS 26 SDK) do not expose. It is set through
 * key-value coding, guarded by `respondsToSelector`, so the same binary runs on every
 * iOS version and builds with Xcode 26 and 27 alike: before iOS 27 this is a no-op.
 * The value comes from the constant UIKit exports at runtime, with the value measured on
 * iOS 27.1 as a fallback (also used when the exported value is not a small integer).
 */
internal fun UIBarButtonItem.applyVisibilityPriority(priority: UIKitUIBarButtonItemVisibilityPriority) {
    if (priority == UIKitUIBarButtonItemVisibilityPriority.Standard) return
    if (!respondsToSelector(sel_registerName(VISIBILITY_PRIORITY_SETTER))) return
    setValue(NSNumber(long = priority.uikitValue), forKey = VISIBILITY_PRIORITY_KEY)
}

private val UIKitUIBarButtonItemVisibilityPriority.uikitValue: Long
    get() = exportedConstants[this] ?: uikitFallbackValue.toLong()

/** The `UIBarButtonItemVisibilityPriority*` constants of the running UIKit, read once. */
private val exportedConstants: Map<UIKitUIBarButtonItemVisibilityPriority, Long> by lazy {
    buildMap {
        exportedLong("UIBarButtonItemVisibilityPriorityLow")?.let { put(UIKitUIBarButtonItemVisibilityPriority.Low, it) }
        exportedLong("UIBarButtonItemVisibilityPriorityStandard")?.let { put(UIKitUIBarButtonItemVisibilityPriority.Standard, it) }
        exportedLong("UIBarButtonItemVisibilityPriorityHigh")?.let { put(UIKitUIBarButtonItemVisibilityPriority.High, it) }
    }
}

/** The constant behind [symbol], or null when it is missing or not a small integer as the priorities are. */
private fun exportedLong(symbol: String): Long? {
    val address: COpaquePointer = dlsym(RTLD_DEFAULT, symbol) ?: return null
    return address.reinterpret<LongVar>().pointed.value.takeIf { it in PLAUSIBLE_PRIORITY_RANGE }
}

private val PLAUSIBLE_PRIORITY_RANGE = -10_000L..10_000L

private const val VISIBILITY_PRIORITY_KEY = "visibilityPriority"
private const val VISIBILITY_PRIORITY_SETTER = "setVisibilityPriority:"
