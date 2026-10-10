package com.mohamedrejeb.calf.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import com.mohamedrejeb.calf.navigation.internal.AppForegroundState
import com.mohamedrejeb.calf.navigation.internal.NavEntryScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavEntryScopeTest {
    private class TrackedViewModel : ViewModel() {
        var cleared = false
            private set

        override fun onCleared() {
            cleared = true
        }
    }

    @Test
    fun `started scope is destroyed and its store cleared`() {
        val scope = NavEntryScope()
        val viewModel = TrackedViewModel()
        scope.viewModelStore.put("vm", viewModel)
        scope.markStarted()

        scope.markDestroyed()

        assertEquals(Lifecycle.State.DESTROYED, scope.lifecycle.currentState)
        assertTrue(viewModel.cleared)
    }

    @Test
    fun `scope that never appeared can still be destroyed`() {
        // A wrapper pushed and immediately covered by the next push never
        // reaches viewDidAppear, so its lifecycle is still INITIALIZED when a
        // pop-to-root drops it from the middle of the stack.
        val scope = NavEntryScope()
        val viewModel = TrackedViewModel()
        scope.viewModelStore.put("vm", viewModel)

        scope.markDestroyed()

        assertEquals(Lifecycle.State.DESTROYED, scope.lifecycle.currentState)
        assertTrue(viewModel.cleared)
    }

    @Test
    fun `destroying twice is a no-op`() {
        val scope = NavEntryScope()
        scope.markStarted()
        scope.markDestroyed()

        scope.markDestroyed()

        assertEquals(Lifecycle.State.DESTROYED, scope.lifecycle.currentState)
    }

    @Test
    fun `stopping a destroyed scope keeps it destroyed`() {
        val scope = NavEntryScope()
        scope.markStarted()
        scope.markDestroyed()

        scope.markStopped()

        assertEquals(Lifecycle.State.DESTROYED, scope.lifecycle.currentState)
    }

    @Test
    fun `starting a destroyed scope is ignored`() {
        // UIKit can still send viewDidAppear to a controller whose entry was torn down in the same
        // turn; the lifecycle registry throws on DESTROYED -> RESUMED.
        val scope = NavEntryScope()
        scope.markStarted()
        scope.markDestroyed()

        scope.markStarted()

        assertEquals(Lifecycle.State.DESTROYED, scope.lifecycle.currentState)
    }

    @Test
    fun `visible entry in the active app is resumed`() {
        val scope = NavEntryScope()
        scope.markStarted()
        scope.setAppState(AppForegroundState.Active)

        assertEquals(Lifecycle.State.RESUMED, scope.lifecycle.currentState)
    }

    @Test
    fun `visible entry is only started while the app is inactive`() {
        // Control Centre, an incoming call, the app switcher: Android's ON_PAUSE.
        val scope = NavEntryScope()
        scope.markStarted()

        scope.setAppState(AppForegroundState.Inactive)

        assertEquals(Lifecycle.State.STARTED, scope.lifecycle.currentState)
    }

    @Test
    fun `visible entry stops while the app is in the background and resumes with it`() {
        val scope = NavEntryScope()
        scope.markStarted()

        scope.setAppState(AppForegroundState.Background)
        assertEquals(Lifecycle.State.CREATED, scope.lifecycle.currentState)

        scope.setAppState(AppForegroundState.Active)
        assertEquals(Lifecycle.State.RESUMED, scope.lifecycle.currentState)
    }

    @Test
    fun `covered entry stays stopped whatever the app does`() {
        val scope = NavEntryScope()
        scope.markStarted()
        scope.markStopped()

        scope.setAppState(AppForegroundState.Active)

        assertEquals(Lifecycle.State.CREATED, scope.lifecycle.currentState)
    }

    @Test
    fun `entry shown while the app is in the background waits for the foreground`() {
        val scope = NavEntryScope()
        scope.setAppState(AppForegroundState.Background)

        scope.markStarted()
        assertEquals(Lifecycle.State.CREATED, scope.lifecycle.currentState)

        scope.setAppState(AppForegroundState.Active)
        assertEquals(Lifecycle.State.RESUMED, scope.lifecycle.currentState)
    }

    @Test
    fun `app state is ignored once destroyed`() {
        val scope = NavEntryScope()
        scope.markStarted()
        scope.markDestroyed()

        scope.setAppState(AppForegroundState.Active)

        assertEquals(Lifecycle.State.DESTROYED, scope.lifecycle.currentState)
    }
}
