package com.mohamedrejeb.calf.navigation.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Per-entry plumbing for the three "owner" CompositionLocals that Compose
 * Multiplatform composables expect:
 *
 * - [LifecycleOwner] — drives `LaunchedEffect` cleanup, lifecycle observers,
 *   `repeatOnLifecycle`, etc.
 * - [ViewModelStoreOwner] — backs `viewModel<X>()` lookups; the store is
 *   cleared exactly when the entry leaves the back-stack permanently
 *   (i.e., not during a cancelled swipe-back).
 * - [SavedStateRegistryOwner] — for save/restore of `rememberSaveable` state.
 *
 * The state-machine is driven from `EntryHostingController`'s view-lifecycle
 * callbacks via [markStarted] / [markStopped] / [markDestroyed]. The class
 * itself implements all three owner interfaces so it can be provided directly
 * via `CompositionLocalProvider`.
 */
internal class NavEntryScope :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    private val savedStateRegistryController = SavedStateRegistryController.create(this).also {
        it.performAttach()
        it.performRestore(null)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore = ViewModelStore()

    override val savedStateRegistry: SavedStateRegistry get() =
        savedStateRegistryController.savedStateRegistry

    /** Called from `EntryHostingController.viewDidAppear`. */
    fun markStarted() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    /** Called from `EntryHostingController.viewDidDisappear` when the VC is *not* permanently popped. */
    fun markStopped() {
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
    }

    /** Called from `EntryHostingController.viewDidDisappear` when `isMovingFromParentViewController()` is true. */
    fun markDestroyed() {
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
        viewModelStore.clear()
    }
}
