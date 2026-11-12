package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.sample.components.BarOverflowMenu
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import kotlinx.datetime.LocalDate

private const val SCREEN_TITLE = "Showcase"
private const val TAB_FADE_MILLIS = 250

/**
 * A small trip planner built only from Calf's adaptive components: a native tab bar and top bar
 * with a menu, switches, a slider, buttons, a progress indicator, date and time pickers, a drop
 * down, an alert dialog and a bottom sheet. On iOS every one of them is UIKit.
 */
@Composable
fun ShowcaseScreen(navigateBack: () -> Unit) {
    ShowcaseApp(navigateBack = navigateBack)
}

/** [ShowcaseScreen], opened on [initialTab]; the screenshot test renders each tab through it. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
internal fun ShowcaseApp(
    navigateBack: () -> Unit,
    initialTab: ShowcaseTab = ShowcaseTab.Plan,
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var dialog by rememberSaveable { mutableStateOf<ShowcaseDialog?>(null) }
    var showShareSheet by rememberSaveable { mutableStateOf(false) }

    // Shared by the tabs: the schedule picks the departure the plan shows.
    val datePickerState = rememberAdaptiveDatePickerState(
        initialSelectedDateMillis = TripInitialDeparture.toEpochMillis(),
    )
    val timePickerState = rememberAdaptiveTimePickerState(
        initialHour = TRIP_INITIAL_HOUR,
        initialMinute = TRIP_INITIAL_MINUTE,
        is24Hour = true,
    )
    val departure = departureDateOf(datePickerState.selectedDateMillis)

    // Keeps each tab's scroll position and switches while another tab is shown.
    val tabStateHolder = rememberSaveableStateHolder()

    AdaptiveScaffold(
        topBar = {
            ShowcaseTopBar(
                navigateBack = navigateBack,
                onShare = { showShareSheet = true },
                onShowDialog = { dialog = it },
            )
        },
        bottomBar = {
            ShowcaseTabBar(selectedTab = selectedTab, onSelect = { selectedTab = it })
        },
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn(tween(TAB_FADE_MILLIS)) togetherWith fadeOut(tween(TAB_FADE_MILLIS)) },
            modifier = Modifier.fillMaxSize(),
        ) { tab ->
            tabStateHolder.SaveableStateProvider(tab.name) {
                ShowcaseTabContent(
                    tab = tab,
                    padding = padding,
                    departure = departure,
                    datePickerState = datePickerState,
                    timePickerState = timePickerState,
                    onShare = { showShareSheet = true },
                    onShowDialog = { dialog = it },
                )
            }
        }
    }

    dialog?.let { shown ->
        ShowcaseDialogHost(dialog = shown, onDismiss = { dialog = null })
    }
    if (showShareSheet) {
        ShareTripSheet(onDismiss = { showShareSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowcaseTabContent(
    tab: ShowcaseTab,
    padding: PaddingValues,
    departure: LocalDate,
    datePickerState: AdaptiveDatePickerState,
    timePickerState: AdaptiveTimePickerState,
    onShare: () -> Unit,
    onShowDialog: (ShowcaseDialog) -> Unit,
) {
    when (tab) {
        ShowcaseTab.Plan -> PlanTab(
            padding = padding,
            departure = departure,
            travellerCount = TripTravellers.size,
        )
        ShowcaseTab.Schedule -> ScheduleTab(
            padding = padding,
            datePickerState = datePickerState,
            timePickerState = timePickerState,
        )
        ShowcaseTab.People -> PeopleTab(
            padding = padding,
            onInvite = onShare,
            onShowDialog = onShowDialog,
        )
    }
}

/**
 * The bar: back, a share action and a menu. Material draws the menu with [BarOverflowMenu]; iOS
 * gets a `UIBarButtonItem` whose `UIMenu` holds the same items.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
private fun ShowcaseTopBar(
    navigateBack: () -> Unit,
    onShare: () -> Unit,
    onShowDialog: (ShowcaseDialog) -> Unit,
) {
    val menuItems = remember(onShare, onShowDialog) { topBarMenuItems(onShare, onShowDialog) }

    AdaptiveTopBar(
        title = { Text(SCREEN_TITLE) },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            AdaptiveIconButton(onClick = onShare) {
                Icon(Icons.Outlined.IosShare, contentDescription = "Share trip")
            }
            BarOverflowMenu(items = menuItems)
        },
        iosTitle = SCREEN_TITLE,
        iosLeadingItems = listOf(
            UIKitUIBarButtonItem.back(onClick = navigateBack),
        ),
        iosTrailingItems = listOf(
            UIKitUIBarButtonItem.image(
                image = UIKitImage.SystemName(SFSymbol.squareAndArrowUp),
                label = "Share trip",
                onClick = onShare,
            ),
            UIKitUIBarButtonItem.withMenu(
                image = UIKitImage.SystemName(SFSymbol.ellipsisCircle),
                label = "More options",
                menuItems = menuItems,
            ),
        ),
    )
}

private fun topBarMenuItems(
    onShare: () -> Unit,
    onShowDialog: (ShowcaseDialog) -> Unit,
): List<AdaptiveDropDownItem> = listOf(
    AdaptiveDropDownItem(
        title = "Invite travellers",
        iosIcon = UIKitImage.SystemName(SFSymbol.personBadgePlus),
        onClick = onShare,
    ),
    AdaptiveDropDownItem(
        title = "View source",
        iosIcon = UIKitImage.SystemName(SFSymbol.chevronLeftForwardslashChevronRight),
        onClick = { onShowDialog(ShowcaseDialog.Source) },
    ),
    AdaptiveDropDownItem(
        title = "Leave trip",
        iosIcon = UIKitImage.SystemName(SFSymbol.rectanglePortraitAndArrowRight),
        isDestructive = true,
        onClick = { onShowDialog(ShowcaseDialog.Leave) },
    ),
)

/** A `UITabBar` with SF Symbols on iOS, a Material `NavigationBar` elsewhere. */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
private fun ShowcaseTabBar(
    selectedTab: ShowcaseTab,
    onSelect: (ShowcaseTab) -> Unit,
) {
    val tabs = ShowcaseTab.entries
    AdaptiveNavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        iosItems = remember {
            tabs.map { UIKitUITabBarItem(title = it.title, image = UIKitImage.SystemName(it.iosSymbol)) }
        },
        iosSelectedIndex = selectedTab.ordinal,
        iosOnItemSelected = { onSelect(tabs[it]) },
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.title) },
            )
        }
    }
}
