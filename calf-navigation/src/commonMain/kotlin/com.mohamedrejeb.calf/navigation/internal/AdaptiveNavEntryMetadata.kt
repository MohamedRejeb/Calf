package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem

/**
 * Everything `adaptiveNavEntry` captures besides the content lambda, stored in
 * [NavEntry.metadata] under [AdaptiveNavEntryMetadataKey].
 *
 * It lives in metadata rather than inside the content lambda because two
 * hosts read it *outside* the entry's own composition:
 *  - the iOS host populates `UINavigationItem` and `hidesBottomBarWhenPushed`
 *    before the child controller is pushed, so the native transition animates
 *    with the right title, items and tab-bar state;
 *  - the Material tab host reads [tabBarShown] of the top entry to animate the
 *    navigation bar in and out.
 */
@Immutable
internal data class AdaptiveNavEntryMetadata(
    val iosTitle: String,
    val iosBackTitle: String?,
    val iosLeadingItems: List<UIKitUIBarButtonItem>,
    val iosTrailingItems: List<UIKitUIBarButtonItem>,
    val iosConfiguration: UIKitNavigationBarConfiguration,
    val largeTitle: Boolean,
    val barShown: Boolean,
    val tabBarShown: Boolean,
    val iosGestureEnabled: Boolean,
) {
    companion object {
        /** Used for entries that were not created through `adaptiveNavEntry`. */
        val Default = AdaptiveNavEntryMetadata(
            iosTitle = "",
            iosBackTitle = null,
            iosLeadingItems = emptyList(),
            iosTrailingItems = emptyList(),
            iosConfiguration = UIKitNavigationBarConfiguration(),
            largeTitle = false,
            barShown = true,
            tabBarShown = true,
            iosGestureEnabled = true,
        )
    }
}

/** Typed Nav3 metadata key. Nav3 stores metadata by the key's `toString()`. */
internal object AdaptiveNavEntryMetadataKey : NavMetadataKey<AdaptiveNavEntryMetadata> {
    override fun toString(): String = "com.mohamedrejeb.calf.navigation.entry"
}

/** This entry's Calf metadata, or [AdaptiveNavEntryMetadata.Default] for a plain Nav3 entry. */
internal val NavEntry<*>.adaptiveMetadata: AdaptiveNavEntryMetadata
    get() = metadata[AdaptiveNavEntryMetadataKey] ?: AdaptiveNavEntryMetadata.Default
