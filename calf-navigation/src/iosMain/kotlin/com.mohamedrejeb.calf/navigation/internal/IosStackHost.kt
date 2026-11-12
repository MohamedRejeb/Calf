package com.mohamedrejeb.calf.navigation.internal

import androidx.navigation3.runtime.NavEntry
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

    /**
     * The display composed again: every hosted entry is built anew through [entryProvider], so
     * content lambdas see fresh captures and bars follow changed titles and items, as Navigation 3
     * rebuilds entries on every recomposition on the Material platforms.
     */
    fun refreshEntries(entryProvider: (NavKey) -> NavEntry<NavKey>)

    /** Tears down every entry. The host is not used afterwards. */
    fun release()
}
