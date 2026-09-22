package com.mohamedrejeb.calf.sample.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleHostTest {

    @Test
    fun homeIsTheList() {
        assertEquals(SampleHost.List, Screen.Home.host())
    }

    @Test
    fun demosThatOwnTheWholeScreenArePushedFromTheOuterDisplay() {
        val fullScreen = Screen.entries.filter { it.host() == SampleHost.FullScreen }.toSet()

        assertEquals(
            setOf(Screen.Showcase, Screen.NavigationBar, Screen.ScaffoldDemo, Screen.AdaptiveNavigation),
            fullScreen,
        )
    }

    @Test
    fun demosThatAreTheirOwnBarKeepItInsideTheDetailPane() {
        val ownBar = Screen.entries.filter { it.host() == SampleHost.DetailOwnBar }.toSet()

        assertEquals(setOf(Screen.DropDown, Screen.TopBarDemo, Screen.WebView), ownBar)
    }

    @Test
    fun everyOtherDemoIsADetailWithTheNavigatorsBar() {
        val details = Screen.entries.filter { it.host() == SampleHost.Detail }

        assertEquals(Screen.entries.size - 1 - 4 - 3, details.size)
        assertEquals(emptyList(), details.filter { it == Screen.Home })
    }

    @Test
    fun detailHostsAreTheOnesShownInsideTheListDetailDisplay() {
        assertEquals(true, SampleHost.Detail.isDetail)
        assertEquals(true, SampleHost.DetailOwnBar.isDetail)
        assertEquals(false, SampleHost.FullScreen.isDetail)
        assertEquals(false, SampleHost.List.isDetail)
    }

    @Test
    fun oneKeyPerScreenComparesByScreen() {
        assertEquals(SampleScreen(Screen.Dialog), SampleScreen(Screen.Dialog))
    }
}
