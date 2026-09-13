package com.mohamedrejeb.calf.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
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
}
