package com.mohamedrejeb.calf.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.internal.AdaptiveNavEntryMetadata
import com.mohamedrejeb.calf.navigation.internal.AdaptiveNavEntryMetadataKey
import com.mohamedrejeb.calf.navigation.internal.adaptiveMetadata
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies the data [adaptiveNavEntry] hands to the platform hosts through
 * `NavEntry.metadata`. The composable content itself needs a Compose runtime
 * and is covered by the desktop UI tests.
 */
@OptIn(ExperimentalCalfNavigationApi::class)
class AdaptiveNavEntryTest {
    private data object Home : NavKey
    private data class Detail(val id: Int) : NavKey

    @Test
    fun entryWithDefaultsCarriesDefaultMetadata() {
        val entry = adaptiveNavEntry(Home) {}

        assertEquals(AdaptiveNavEntryMetadata.Default, entry.adaptiveMetadata)
        assertEquals(Home.toString(), entry.contentKey)
    }

    @Test
    fun entryCapturesEveryOption() {
        val save = UIKitUIBarButtonItem.title("Save") {}
        val cancel = UIKitUIBarButtonItem.title("Cancel") {}
        val configuration = UIKitNavigationBarConfiguration(isTranslucent = false)

        val entry = adaptiveNavEntry(
            key = Detail(7),
            largeTitle = true,
            iosTitle = "Item 7",
            iosBackTitle = "Items",
            iosLeadingItems = listOf(cancel),
            iosTrailingItems = listOf(save),
            iosConfiguration = configuration,
            barShown = false,
            tabBarShown = false,
            iosGestureEnabled = false,
        ) {}

        val metadata = entry.adaptiveMetadata
        assertEquals("Item 7", metadata.iosTitle)
        assertEquals("Items", metadata.iosBackTitle)
        assertEquals(listOf(cancel), metadata.iosLeadingItems)
        assertEquals(listOf(save), metadata.iosTrailingItems)
        assertEquals(configuration, metadata.iosConfiguration)
        assertTrue(metadata.largeTitle)
        assertFalse(metadata.barShown)
        assertFalse(metadata.tabBarShown)
        assertFalse(metadata.iosGestureEnabled)
    }

    @Test
    fun plainNavEntryFallsBackToDefaultMetadata() {
        val entry = NavEntry<NavKey>(Home) {}

        assertEquals(AdaptiveNavEntryMetadata.Default, entry.adaptiveMetadata)
        assertNull(entry.metadata[AdaptiveNavEntryMetadataKey.toString()])
    }

    @Test
    fun metadataKeyIsStable() {
        // The key is the string Nav3 stores metadata under; renaming it would
        // silently disconnect hosts from entries built by an older version.
        assertEquals("com.mohamedrejeb.calf.navigation.entry", AdaptiveNavEntryMetadataKey.toString())
    }
}
