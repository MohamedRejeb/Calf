package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.IosNavBarData
import com.mohamedrejeb.calf.navigation.internal.IosNavBarDataKey
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests covering the data-flow boundary of [adaptiveNavEntry].
 *
 * Composable invocation of `adaptiveNavEntry(...)` itself requires a Compose
 * runtime and is exercised in the iOS simulator integration tests. Here we
 * verify the underlying [IosNavBarData] semantics that the composable's
 * iOS adapter reads from `NavEntry.metadata[IosNavBarDataKey]`.
 */
@OptIn(ExperimentalCalfNavigationApi::class)
class AdaptiveNavEntryTest {

    @Test
    fun defaultIosNavBarData_hasSensibleDefaults() {
        val data = IosNavBarData.Default

        assertEquals("", data.title)
        assertNull(data.backTitle)
        assertTrue(data.leadingItems.isEmpty())
        assertTrue(data.trailingItems.isEmpty())
        assertEquals(UIKitNavigationBarConfiguration(), data.configuration)
        assertFalse(data.largeTitle)
        assertTrue(data.barShown)
        assertTrue(data.gestureEnabled)
        assertEquals(NavPresentation.Card, data.presentation)
    }

    @Test
    fun iosNavBarData_copyPreservesUnchangedFields() {
        val original = IosNavBarData.Default.copy(
            title = "Detail",
            largeTitle = true,
        )
        assertEquals("Detail", original.title)
        assertTrue(original.largeTitle)

        val copied = original.copy(title = "Settings")
        assertEquals("Settings", copied.title)
        assertTrue(copied.largeTitle, "largeTitle should survive copy")
    }

    @Test
    fun iosNavBarData_equality() {
        val a = IosNavBarData(
            title = "Home",
            backTitle = "Back",
            leadingItems = emptyList(),
            trailingItems = emptyList(),
            configuration = UIKitNavigationBarConfiguration(),
            largeTitle = true,
            barShown = true,
            gestureEnabled = true,
            presentation = NavPresentation.Card,
        )
        val b = a.copy()
        val differingTitle = a.copy(title = "Settings")

        assertEquals(a, b, "structurally equal IosNavBarData should be ==")
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, differingTitle)
    }

    @Test
    fun iosNavBarDataKey_matchesPublishedConstant() {
        // Sanity: the metadata key the iOS adapter reads is the one we write.
        // Hard-coded literal here to detect accidental rename.
        assertEquals("calf.nav.ios-bar-data", IosNavBarDataKey)
    }

    @Test
    fun iosNavBarData_carriesArbitraryItems() {
        val saveItem = UIKitUIBarButtonItem.title("Save") {}
        val cancelItem = UIKitUIBarButtonItem.title("Cancel") {}
        val data = IosNavBarData.Default.copy(
            title = "Compose",
            leadingItems = listOf(cancelItem),
            trailingItems = listOf(saveItem),
        )

        assertEquals(1, data.leadingItems.size)
        assertEquals("Cancel", data.leadingItems.single().title)
        assertEquals(1, data.trailingItems.size)
        assertEquals("Save", data.trailingItems.single().title)
        assertNotNull(data.leadingItems.single().onClick)
    }
}
