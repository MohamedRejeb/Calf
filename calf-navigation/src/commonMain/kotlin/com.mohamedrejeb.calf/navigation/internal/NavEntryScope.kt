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
 * The state machine is driven from `EntryHostingController`'s view-lifecycle
 * callbacks via [markStarted] / [markStopped] / [markDestroyed], and from the
 * app's foreground state via [setAppState]: UIKit sends no appearance callback
 * when the app goes to the background, so without it an entry on screen would
 * stay `RESUMED` there and `repeatOnLifecycle` / `collectAsStateWithLifecycle`
 * would never pause, unlike on Android. The class itself implements all three
 * owner interfaces so it can be provided directly via `CompositionLocalProvider`.
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

    private var isVisible: Boolean = false
    private var appState: AppForegroundState = AppForegroundState.Active

    /**
     * Called from `EntryHostingController.viewDidAppear`. Ignored once destroyed: UIKit can still
     * report an appearance for a controller whose entry was torn down in the same turn, and the
     * registry throws on `DESTROYED` → `RESUMED`.
     */
    fun markStarted() {
        isVisible = true
        applyState()
    }

    /** Called from `EntryHostingController.viewDidDisappear`: covered, or on its way out. Ignored once destroyed. */
    fun markStopped() {
        isVisible = false
        applyState()
    }

    /** The app moved between active, inactive and background; see [AppForegroundState]. */
    fun setAppState(state: AppForegroundState) {
        appState = state
        applyState()
    }

    /**
     * Visible in the active app: `RESUMED`. Visible while the app is inactive (Control Centre, a
     * call): `STARTED`, Android's paused. Covered, or the app in the background: `CREATED`.
     */
    private fun applyState() {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) return
        lifecycleRegistry.currentState = when {
            !isVisible -> Lifecycle.State.CREATED
            appState == AppForegroundState.Background -> Lifecycle.State.CREATED
            appState == AppForegroundState.Inactive -> Lifecycle.State.STARTED
            else -> Lifecycle.State.RESUMED
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

/** The app's foreground state, as iOS reports it; the entry lifecycle is capped by it. */
internal enum class AppForegroundState {
    /** In the foreground and receiving events. */
    Active,

    /** In the foreground but not receiving events: Control Centre, a call, the app switcher. */
    Inactive,

    /** Not on screen. */
    Background,
}
