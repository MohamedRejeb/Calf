package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCalfNavigationApi::class)
class AdaptiveTabStateTest {
    private data object Home : NavKey
    private data object Search : NavKey
    private data object Profile : NavKey
    private data class Detail(val id: Int) : NavKey

    private fun state(
        tabs: List<NavKey> = listOf(Home, Search, Profile),
        startTab: NavKey = Home,
        selectedIndex: Int = tabs.indexOf(startTab),
    ) = AdaptiveTabState(
        tabs = tabs,
        startTab = startTab,
        selectedIndexState = mutableIntStateOf(selectedIndex),
        backStacks = tabs.associateWith { NavBackStack<NavKey>(it) },
    )

    @Test
    fun startsOnStartTabWithRootOnlyStacks() {
        val state = state()

        assertEquals(Home, state.selectedTab)
        assertEquals(listOf<NavKey>(Home), state.backStack.toList())
        assertEquals(listOf<NavKey>(Search), state.backStackOf(Search).toList())
    }

    @Test
    fun eachTabOwnsItsOwnStack() {
        val state = state()

        assertNotSame(state.backStackOf(Home), state.backStackOf(Search))
        state.backStackOf(Search).add(Detail(1))
        assertEquals(listOf<NavKey>(Home), state.backStackOf(Home).toList())
        assertEquals(listOf<NavKey>(Search, Detail(1)), state.backStackOf(Search).toList())
    }

    @Test
    fun selectSwitchesTabAndKeepsOtherStacks() {
        val state = state()
        state.backStack.add(Detail(1))

        state.select(Search)

        assertEquals(Search, state.selectedTab)
        assertEquals(listOf<NavKey>(Search), state.backStack.toList())
        assertEquals(listOf<NavKey>(Home, Detail(1)), state.backStackOf(Home).toList())
    }

    @Test
    fun reselectingTheCurrentTabPopsItToRoot() {
        val state = state()
        state.backStack.add(Detail(1))
        state.backStack.add(Detail(2))

        state.select(Home)

        assertEquals(Home, state.selectedTab)
        assertEquals(listOf<NavKey>(Home), state.backStack.toList())
    }

    @Test
    fun selectFromPlatformNeverPops() {
        val state = state()
        state.backStack.add(Detail(1))

        state.selectFromPlatform(Home)

        assertEquals(listOf<NavKey>(Home, Detail(1)), state.backStack.toList())
    }

    @Test
    fun popToRootOfAnotherTab() {
        val state = state()
        state.backStackOf(Profile).add(Detail(3))

        state.popToRoot(Profile)

        assertEquals(listOf<NavKey>(Profile), state.backStackOf(Profile).toList())
        assertEquals(Home, state.selectedTab)
    }

    @Test
    fun unknownTabIsRejected() {
        val state = state()

        assertFailsWith<IllegalArgumentException> { state.select(Detail(1)) }
        assertFailsWith<IllegalArgumentException> { state.backStackOf(Detail(1)) }
    }

    @Test
    fun invalidConstructionIsRejected() {
        assertFailsWith<IllegalArgumentException> { state(tabs = emptyList(), startTab = Home, selectedIndex = 0) }
        assertFailsWith<IllegalArgumentException> { state(tabs = listOf(Home, Home)) }
        assertFailsWith<IllegalArgumentException> { state(startTab = Detail(1)) }
        assertFailsWith<IllegalArgumentException> { state(selectedIndex = 5) }
    }

    @Test
    fun backReturnsToStartTabOnlyFromAnotherTabsRoot() {
        val state = state()
        assertFalse(state.isAtNonStartTabRoot, "start tab root: system back should exit")

        state.select(Search)
        assertTrue(state.isAtNonStartTabRoot, "other tab root: back should return to start tab")

        state.backStack.add(Detail(1))
        assertFalse(state.isAtNonStartTabRoot, "pushed entry: back should pop it first")
    }

    @Test
    fun displayTabsMustMatchStateTabsInOrder() {
        val state = state()
        val matching = listOf(
            adaptiveTab<NavKey>(Home, icon = {}),
            adaptiveTab<NavKey>(Search, icon = {}),
            adaptiveTab<NavKey>(Profile, icon = {}),
        )
        state.requireMatching(matching)

        val reordered = listOf(matching[1], matching[0], matching[2])
        assertFailsWith<IllegalArgumentException> { state.requireMatching(reordered) }
        assertFailsWith<IllegalArgumentException> { state.requireMatching(matching.dropLast(1)) }
    }
}
