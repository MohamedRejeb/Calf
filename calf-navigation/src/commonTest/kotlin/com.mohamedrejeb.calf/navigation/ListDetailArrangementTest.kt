package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.PaneRoleKind
import com.mohamedrejeb.calf.navigation.internal.PaneSide
import com.mohamedrejeb.calf.navigation.internal.arrangeListDetail
import com.mohamedrejeb.calf.navigation.internal.paneSidesOf
import com.mohamedrejeb.calf.navigation.internal.partitionListDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the pure back stack → panes logic behind `AdaptiveListDetail`. Keys are strings here:
 * a name starting with "List" is a list entry, one starting with "Detail" a detail entry.
 */
class ListDetailArrangementTest {

    private fun roleOf(key: String): PaneRoleKind =
        when {
            key.startsWith("List") -> PaneRoleKind.List
            key.startsWith("Detail") -> PaneRoleKind.Detail
            else -> PaneRoleKind.None
        }

    // ── partition ──

    @Test
    fun noListEntry_everythingStaysPrimary() {
        val partition = partitionListDetail(listOf("Home", "Detail1", "Child"), ::roleOf)

        assertEquals(listOf("Home", "Detail1", "Child"), partition.primary)
        assertTrue(partition.secondary.isEmpty())
        assertNull(partition.list)
    }

    @Test
    fun listOnTop_secondaryIsEmpty() {
        val partition = partitionListDetail(listOf("Home", "List"), ::roleOf)

        assertEquals(listOf("Home", "List"), partition.primary)
        assertTrue(partition.secondary.isEmpty())
        assertEquals("List", partition.list)
    }

    @Test
    fun detailAndEverythingAfterIt_goSecondary() {
        val partition = partitionListDetail(listOf("Home", "List", "Detail1", "Child"), ::roleOf)

        assertEquals(listOf("Home", "List"), partition.primary)
        assertEquals(listOf("Detail1", "Child"), partition.secondary)
    }

    @Test
    fun plainEntryBetweenListAndDetail_staysPrimary() {
        val partition = partitionListDetail(listOf("List", "Filter", "Detail1"), ::roleOf)

        assertEquals(listOf("List", "Filter"), partition.primary)
        assertEquals(listOf("Detail1"), partition.secondary)
    }

    @Test
    fun plainEntryAboveTheList_withoutDetail_staysPrimary() {
        val partition = partitionListDetail(listOf("List", "Filter"), ::roleOf)

        assertEquals(listOf("List", "Filter"), partition.primary)
        assertTrue(partition.secondary.isEmpty())
    }

    @Test
    fun nestedLists_bothStayPrimary_andTheLastOneOwnsThePlaceholder() {
        val partition = partitionListDetail(listOf("ListMailboxes", "ListInbox"), ::roleOf)

        assertEquals(listOf("ListMailboxes", "ListInbox"), partition.primary)
        assertEquals("ListInbox", partition.list)
    }

    @Test
    fun listPushedAfterADetail_staysOnTheDetailSide() {
        val partition = partitionListDetail(listOf("List", "Detail1", "ListNested"), ::roleOf)

        assertEquals(listOf("List"), partition.primary)
        assertEquals(listOf("Detail1", "ListNested"), partition.secondary)
        assertEquals("List", partition.list)
    }

    @Test
    fun detailBelowTheFirstList_isIgnored() {
        val partition = partitionListDetail(listOf("Detail0", "List", "Detail1"), ::roleOf)

        assertEquals(listOf("Detail0", "List"), partition.primary)
        assertEquals(listOf("Detail1"), partition.secondary)
    }

    @Test
    fun duplicateKeys_arePartitionedByPosition() {
        val partition = partitionListDetail(listOf("List", "Detail1", "Detail1"), ::roleOf)

        assertEquals(listOf("List"), partition.primary)
        assertEquals(listOf("Detail1", "Detail1"), partition.secondary)
    }

    @Test
    fun emptyStack_isEmptyEverywhere() {
        val partition = partitionListDetail(emptyList<String>(), ::roleOf)

        assertTrue(partition.primary.isEmpty())
        assertTrue(partition.secondary.isEmpty())
        assertNull(partition.list)
    }

    @Test
    fun itemsKeepTheirIdentity() {
        class Host(val key: String)
        val hosts = listOf(Host("List"), Host("Detail1"))

        val partition = partitionListDetail(hosts) { roleOf(it.key) }

        assertSame(hosts[0], partition.primary.single())
        assertSame(hosts[1], partition.secondary.single())
    }

    // ── arrangement ──

    @Test
    fun onePane_everythingIsInTheCompactStack_inBackStackOrder() {
        val stack = listOf("Home", "List", "Detail1", "Child")

        val arrangement = arrangeListDetail(stack, ::roleOf, paneCount = 1)

        assertEquals(stack, arrangement.compact)
        assertTrue(arrangement.primary.isEmpty())
        assertTrue(arrangement.secondary.isEmpty())
        assertNull(arrangement.placeholderList)
    }

    @Test
    fun onePane_topIsTheDetailSideWhenOneIsOpen_elseTheList() {
        assertEquals("Child", arrangeListDetail(listOf("List", "Detail1", "Child"), ::roleOf, paneCount = 1).compact.last())
        assertEquals("List", arrangeListDetail(listOf("Home", "List"), ::roleOf, paneCount = 1).compact.last())
    }

    @Test
    fun twoPanes_splitAtTheDetail() {
        val arrangement = arrangeListDetail(listOf("Home", "List", "Detail1", "Child"), ::roleOf, paneCount = 2)

        assertEquals(listOf("Home", "List"), arrangement.primary)
        assertEquals(listOf("Detail1", "Child"), arrangement.secondary)
        assertTrue(arrangement.compact.isEmpty())
        assertNull(arrangement.placeholderList)
    }

    @Test
    fun twoPanes_withoutADetail_showThePlaceholderOfTheList() {
        val arrangement = arrangeListDetail(listOf("Home", "List"), ::roleOf, paneCount = 2)

        assertEquals("List", arrangement.placeholderList)
        assertTrue(arrangement.secondary.isEmpty())
    }

    @Test
    fun twoPanes_withoutAList_haveNoPlaceholder() {
        val arrangement = arrangeListDetail(listOf("Home"), ::roleOf, paneCount = 2)

        assertEquals(listOf("Home"), arrangement.primary)
        assertNull(arrangement.placeholderList)
    }

    @Test
    fun poppingTheLastDetailInTwoPanes_bringsThePlaceholderBack() {
        val open = arrangeListDetail(listOf("List", "Detail1"), ::roleOf, paneCount = 2)
        val popped = arrangeListDetail(listOf("List"), ::roleOf, paneCount = 2)

        assertNull(open.placeholderList)
        assertEquals("List", popped.placeholderList)
    }

    @Test
    fun paneCountChange_neverChangesWhichEntriesExist() {
        val stack = listOf("Home", "List", "Detail1", "Child")

        val one = arrangeListDetail(stack, ::roleOf, paneCount = 1)
        val two = arrangeListDetail(stack, ::roleOf, paneCount = 2)

        assertEquals(one.compact, two.primary + two.secondary)
    }

    @Test
    fun moreThanTwoPanes_isTreatedAsTwo() {
        val arrangement = arrangeListDetail(listOf("List", "Detail1"), ::roleOf, paneCount = 3)

        assertEquals(listOf("List"), arrangement.primary)
        assertEquals(listOf("Detail1"), arrangement.secondary)
        assertFalse(arrangement.compact.isNotEmpty())
    }

    // ── sides (what the Material scene is told about each key) ──

    @Test
    fun sides_entriesBelowTheList_haveNone_soTheyStayFullScreen() {
        val sides = paneSidesOf(listOf("Home", "List", "Detail1", "Child"), ::roleOf)

        assertNull(sides["Home"])
        assertEquals(PaneSide.List, sides["List"])
        assertEquals(PaneSide.Detail, sides["Detail1"])
        assertEquals(PaneSide.Detail, sides["Child"])
    }

    @Test
    fun sides_plainEntryOverTheList_isOnTheListSide() {
        assertEquals(PaneSide.List, paneSidesOf(listOf("List", "Filter"), ::roleOf)["Filter"])
    }

    @Test
    fun sides_withoutAList_areEmpty() {
        assertTrue(paneSidesOf(listOf("Home", "Detail1"), ::roleOf).isEmpty())
    }

    @Test
    fun sides_keyOnBothSides_countsAsDetail() {
        val sides = paneSidesOf(listOf("List", "Shared", "Detail1", "Shared"), ::roleOf)

        assertEquals(PaneSide.Detail, sides["Shared"])
    }
}
