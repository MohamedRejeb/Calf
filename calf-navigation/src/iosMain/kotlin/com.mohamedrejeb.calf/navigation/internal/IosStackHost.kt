package com.mohamedrejeb.calf.navigation.internal

import androidx.navigation3.runtime.NavKey
import platform.UIKit.UIViewController

/**
 * A native container that shows one Compose back stack: a navigation controller
 * ([IosNavController]) or a split view of navigation controllers ([IosSplitController]). A tab
 * bar controller holds either as a tab's root.
 */
internal interface IosStackHost {
    /** The controller to embed: in a Compose interop view, or as a tab of a `UITabBarController`. */
    val rootViewController: UIViewController

    /** Makes the native container match [snapshot], the current Compose back stack. */
    fun syncToUiStack(snapshot: List<NavKey>)

    /** Tears down every entry. The host is not used afterwards. */
    fun release()
}
