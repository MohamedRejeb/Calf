package com.mohamedrejeb.calf.navigation

import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.navigation.internal.PaneRoleKind
import com.mohamedrejeb.calf.navigation.internal.adaptiveMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** The data side of the list-detail API: what an entry declares and what the settings carry. */
@OptIn(ExperimentalCalfNavigationApi::class, CalfIosOnlyApi::class)
class AdaptiveListDetailApiTest {
    private data object Inbox : NavKey
    private data class Message(val id: Int) : NavKey

    @Test
    fun entryHasNoPaneRoleByDefault() {
        assertSame(AdaptivePaneRole.None, adaptiveNavEntry(Inbox) {}.adaptiveMetadata.paneRole)
    }

    @Test
    fun plainNavEntryHasNoPaneRole() {
        assertSame(AdaptivePaneRole.None, NavEntry<NavKey>(Inbox) {}.adaptiveMetadata.paneRole)
    }

    @Test
    fun entryCarriesItsPaneRole() {
        val list = AdaptivePaneRole.List {}

        assertSame(list, adaptiveNavEntry(Inbox, paneRole = list) {}.adaptiveMetadata.paneRole)
        assertSame(
            AdaptivePaneRole.Detail,
            adaptiveNavEntry(Message(1), paneRole = AdaptivePaneRole.Detail) {}.adaptiveMetadata.paneRole,
        )
    }

    @Test
    fun everyRoleMapsToItsKind() {
        assertEquals(PaneRoleKind.None, AdaptivePaneRole.None.kind)
        assertEquals(PaneRoleKind.List, AdaptivePaneRole.List().kind)
        assertEquals(PaneRoleKind.Detail, AdaptivePaneRole.Detail.kind)
    }

    @Test
    fun defaultSettingsAreEqual_soTheyAreAStableRememberKey() {
        assertEquals(AdaptiveListDetailSettings(), AdaptiveListDetailSettings())
        assertEquals(AdaptiveListDetailSettings().hashCode(), AdaptiveListDetailSettings().hashCode())
    }

    @Test
    fun defaultIosSplitViewLeavesEverythingToTheSystem() {
        val configuration = AdaptiveListDetailSettings().iosSplitView

        assertEquals(IosSplitViewDisplayMode.Automatic, configuration.preferredDisplayMode)
        assertEquals(IosSplitViewBehavior.Automatic, configuration.splitBehavior)
        assertEquals(IosSplitViewPrimaryEdge.Leading, configuration.primaryEdge)
        assertNull(configuration.primaryColumnWidth)
        assertNull(configuration.minPrimaryColumnWidth)
        assertNull(configuration.maxPrimaryColumnWidth)
        assertEquals(true, configuration.presentsWithGesture)
        assertEquals(false, configuration.showsSecondaryOnlyButton)
    }

    @Test
    fun settingsCarryTheIosSplitViewConfiguration() {
        val configuration = IosSplitViewConfiguration(
            preferredDisplayMode = IosSplitViewDisplayMode.OneBesideSecondary,
            splitBehavior = IosSplitViewBehavior.Tile,
            primaryColumnWidth = 360.dp,
            primaryEdge = IosSplitViewPrimaryEdge.Trailing,
        )

        val settings = AdaptiveListDetailSettings(iosSplitView = configuration)

        assertEquals(configuration, settings.iosSplitView)
        assertEquals(360.dp, settings.iosSplitView.primaryColumnWidth)
        assertNotEquals(AdaptiveListDetailSettings(), settings)
    }
}
