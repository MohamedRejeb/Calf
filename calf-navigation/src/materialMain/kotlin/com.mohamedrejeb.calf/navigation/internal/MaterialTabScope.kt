package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlin.random.Random

/**
 * What one tab keeps alive while another tab is selected on Material platforms:
 * a [SaveableStateHolder] for its entries' `rememberSaveable` state and a
 * [ViewModelStoreOwner] for its entries' ViewModels. Both outlive the tab's
 * `NavDisplay`, which leaves the composition when the tab is not selected, so
 * switching tabs never counts as popping the tab's entries.
 *
 * Scoping ViewModels per tab also means two tabs that show the same key do not
 * share a ViewModel, matching the per-stack scopes UIKit gives each tab.
 */
internal class MaterialTabScope(
    val entryStateHolder: SaveableStateHolder,
    val viewModelStoreOwner: ViewModelStoreOwner,
)

private class TabViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

/**
 * Owns the per-tab ViewModel stores. Lives in the ViewModel store that hosts
 * the tab display, so the tabs' ViewModels survive configuration changes on
 * Android and are cleared exactly when the host's store is cleared (activity
 * finished, or the outer entry hosting the tabs popped).
 */
internal class TabViewModelStoresViewModel : ViewModel() {
    private val owners = HashMap<Any, TabViewModelStoreOwner>()

    fun ownerFor(tab: Any): ViewModelStoreOwner = owners.getOrPut(tab) { TabViewModelStoreOwner() }

    override fun onCleared() {
        owners.values.forEach { it.viewModelStore.clear() }
        owners.clear()
    }
}

private const val TAB_STORES_KEY_PREFIX = "com.mohamedrejeb.calf.navigation.tab-stores:"

/** One [MaterialTabScope] per tab, created once and kept for as long as the display is composed. */
@Composable
internal fun <T : NavKey> rememberMaterialTabScopes(tabs: List<T>): Map<T, MaterialTabScope> {
    // One store holder per display: two displays under the same owner may use equal tab keys.
    // Saved, so the display finds its ViewModels again after an Android configuration change.
    val displayId = rememberSaveable { Random.nextLong().toString() }
    val stores = viewModel(key = TAB_STORES_KEY_PREFIX + displayId) { TabViewModelStoresViewModel() }
    return tabs.associateWith { tab ->
        key(tab) {
            val entryStateHolder = rememberSaveableStateHolder()
            remember(entryStateHolder, stores) {
                MaterialTabScope(entryStateHolder, stores.ownerFor(tab))
            }
        }
    }
}
