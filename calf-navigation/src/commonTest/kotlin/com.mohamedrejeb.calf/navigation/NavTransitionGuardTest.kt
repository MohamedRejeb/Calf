package com.mohamedrejeb.calf.navigation

import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.internal.NavChangeDecision
import com.mohamedrejeb.calf.navigation.internal.decideNavChange
import kotlin.test.Test
import kotlin.test.assertEquals

class NavTransitionGuardTest {
    private data object Home : NavKey
    private data class Detail(val id: Int) : NavKey

    private val accepted: List<NavKey> = listOf(Home, Detail(1))

    @Test
    fun `idle navigator accepts any change`() {
        assertEquals(NavChangeDecision.Accept, decideNavChange(accepted, accepted + Detail(2), isTransitioning = false))
        assertEquals(NavChangeDecision.Accept, decideNavChange(accepted, listOf(Home), isTransitioning = false))
    }

    @Test
    fun `unchanged stack is accepted even mid-transition`() {
        assertEquals(NavChangeDecision.Accept, decideNavChange(accepted, accepted.toList(), isTransitioning = true))
    }

    @Test
    fun `push during a transition is deferred until it ends`() {
        val decision = decideNavChange(accepted, accepted + Detail(2), isTransitioning = true)
        assertEquals(NavChangeDecision.Defer, decision)
    }

    @Test
    fun `replace during a transition is deferred`() {
        val decision = decideNavChange(accepted, listOf(Home, Detail(9)), isTransitioning = true)
        assertEquals(NavChangeDecision.Defer, decision)
    }

    @Test
    fun `pop during a transition is deferred`() {
        val decision = decideNavChange(accepted, listOf(Home), isTransitioning = true)
        assertEquals(NavChangeDecision.Defer, decision)
    }

    @Test
    fun `pushing the top entry again during its own transition is dropped as a double tap`() {
        val decision = decideNavChange(accepted, accepted + Detail(1), isTransitioning = true)
        assertEquals(NavChangeDecision.Revert(accepted), decision)
    }

    @Test
    fun `pushing the top entry again once idle is a real push`() {
        assertEquals(NavChangeDecision.Accept, decideNavChange(accepted, accepted + Detail(1), isTransitioning = false))
    }

    @Test
    fun `pushing the top entry twice during a transition is dropped too`() {
        val decision = decideNavChange(accepted, accepted + Detail(1) + Detail(1), isTransitioning = true)
        assertEquals(NavChangeDecision.Revert(accepted), decision)
    }
}
