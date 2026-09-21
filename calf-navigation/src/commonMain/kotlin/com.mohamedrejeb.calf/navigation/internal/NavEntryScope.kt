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

    // Unsafe = no main-thread enforcement; UIKit drives it from the main thread anyway.
    private val lifecycleRegistry = LifecycleRegistry.createUnsafe(this)

    private val savedStateRegistryController = SavedStateRegistryController.create(this).also {
        it.performAttach()
        it.performRestore(null)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore = ViewModelStore()

    override val savedStateRegistry: SavedStateRegistry get() =
        savedStateRegistryController.savedStateRegistry

    /**
     * Called from `EntryHostingController.viewDidAppear`. Ignored once destroyed: UIKit can still
     * report an appearance for a controller whose entry was torn down in the same turn, and the
     * registry throws on `DESTROYED` → `RESUMED`.
     */
    fun markStarted() {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) return
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    /** Called from `EntryHostingController.viewDidDisappear`: covered, or on its way out. Ignored once destroyed. */
    fun markStopped() {
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
    }

    /**
     * Called when the entry leaves the stack for good. Idempotent. A scope that
     * never appeared (pushed and covered before `viewDidAppear`) is still
     * `INITIALIZED`, and the lifecycle registry only allows `DESTROYED` from
     * `CREATED` or above, so it is stepped through `CREATED` first.
     */
    fun markDestroyed() {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) return
        if (lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}
