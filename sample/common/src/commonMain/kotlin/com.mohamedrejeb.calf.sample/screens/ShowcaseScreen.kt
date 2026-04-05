package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDown
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import com.mohamedrejeb.calf.ui.slider.AdaptiveSlider
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.sf.symbols.SFSymbol

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalCalfUiApi::class)
@Composable
fun ShowcaseScreen(
    navigateBack: () -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var showAlertDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }
    val sheetState = rememberAdaptiveSheetState()

    var topBarMenuExpanded by remember { mutableStateOf(false) }

    val topBarMenuItems = listOf(
        AdaptiveDropDownItem(
            title = "About Calf",
            iosIcon = UIKitImage.SystemName(SFSymbol.infoCircle),
            onClick = { showAboutDialog = true },
        ),
        AdaptiveDropDownItem(
            title = "View Source",
            iosIcon = UIKitImage.SystemName(SFSymbol.chevronLeftForwardslashChevronRight),
            onClick = { showSourceDialog = true },
        ),
    )

    val tabLabels = listOf("Controls", "Pickers", "Overlays")
    val tabIcons = listOf(
        Icons.Outlined.Tune,
        Icons.Outlined.CalendarMonth,
        Icons.Outlined.Layers,
    )

    AdaptiveScaffold(
        topBar = {
            AdaptiveTopBar(
                title = { Text("Component Showcase") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    AdaptiveIconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                    Box {
                        AdaptiveIconButton(onClick = { topBarMenuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = topBarMenuExpanded,
                            onDismissRequest = { topBarMenuExpanded = false },
                        ) {
                            topBarMenuItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    onClick = {
                                        topBarMenuExpanded = false
                                        item.onClick()
                                    },
                                )
                            }
                        }
                    }
                },
                iosTitle = "Component Showcase",
                iosLeadingItems = listOf(
                    UIKitUIBarButtonItem(
                        title = "Back",
                        onClick = navigateBack,
                    ),
                ),
                iosTrailingItems = listOf(
                    UIKitUIBarButtonItem.image(
                        image = UIKitImage.SystemName(SFSymbol.squareAndArrowUp),
                        onClick = { showShareDialog = true },
                    ),
                    UIKitUIBarButtonItem.withMenu(
                        image = UIKitImage.SystemName(SFSymbol.ellipsisCircle),
                        menuItems = topBarMenuItems,
                    ),
                ),
            )
        },
        bottomBar = {
            AdaptiveNavigationBar(
                iosItems = listOf(
                    UIKitUITabBarItem(
                        title = "Controls",
                        image = UIKitImage.SystemName(SFSymbol.sliderHorizontal3),
                    ),
                    UIKitUITabBarItem(
                        title = "Pickers",
                        image = UIKitImage.SystemName(SFSymbol.calendar),
                    ),
                    UIKitUITabBarItem(
                        title = "Overlays",
                        image = UIKitImage.SystemName(SFSymbol.squareOnSquare),
                    ),
                ),
                iosSelectedIndex = selectedTabIndex,
                iosOnItemSelected = { selectedTabIndex = it },
            ) {
                tabLabels.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                tabIcons[index],
                                contentDescription = label,
                            )
                        },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier.fillMaxSize(),
        ) { tabIndex ->
            when (tabIndex) {
                0 -> ShowcaseControlsTab(paddingValues)
                1 -> ShowcasePickersTab(paddingValues)
                2 -> ShowcaseOverlaysTab(
                    paddingValues = paddingValues,
                    onShowAlertDialog = { showAlertDialog = true },
                    onShowBottomSheet = { showBottomSheet = true },
                )
            }
        }
    }

    // -- Top bar action dialogs --

    if (showShareDialog) {
        AdaptiveAlertDialog(
            onConfirm = { showShareDialog = false },
            onDismiss = { showShareDialog = false },
            confirmText = "Ok",
            dismissText = "Cancel",
            title = "Share",
            text = "Link copied to clipboard!",
        )
    }

    if (showAboutDialog) {
        AdaptiveAlertDialog(
            onConfirm = { showAboutDialog = false },
            onDismiss = { showAboutDialog = false },
            confirmText = "Ok",
            dismissText = "Cancel",
            title = "About Calf",
            text = "Calf (Compose Adaptive Look & Feel) is a Kotlin Multiplatform library providing adaptive UI components with native look & feel on each platform.",
        )
    }

    if (showSourceDialog) {
        AdaptiveAlertDialog(
            onConfirm = { showSourceDialog = false },
            onDismiss = { showSourceDialog = false },
            confirmText = "Ok",
            dismissText = "Cancel",
            title = "View Source",
            text = "github.com/MohamedRejeb/Calf",
        )
    }

    // -- Component showcase dialogs --

    if (showAlertDialog) {
        AdaptiveAlertDialog(
            onConfirm = { showAlertDialog = false },
            onDismiss = { showAlertDialog = false },
            confirmText = "Ok",
            dismissText = "Cancel",
            title = "Showcase Dialog",
            text = "This is an adaptive alert dialog rendered natively on each platform.",
        )
    }

    if (showBottomSheet) {
        AdaptiveBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            adaptiveSheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(
                    text = "Adaptive Bottom Sheet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This sheet uses native iOS presentation APIs on iOS and Material3 ModalBottomSheet on other platforms.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var sheetSwitch by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Example Toggle",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "A switch inside a bottom sheet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AdaptiveSwitch(
                        checked = sheetSwitch,
                        onCheckedChange = { sheetSwitch = it },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                AdaptiveButton(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Dismiss")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tab: Controls (Buttons, Switch, Slider, Progress)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShowcaseControlsTab(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Interactive controls and visual indicators.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // -- Buttons --
        ShowcaseSectionHeader(title = "Buttons")

        ShowcaseCard(label = "AdaptiveButton") {
            var clickCount by remember { mutableIntStateOf(0) }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AdaptiveButton(onClick = { clickCount++ }) {
                    Text("Clicked $clickCount")
                }
                AdaptiveButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create")
                }
                AdaptiveButton(onClick = {}, enabled = false) {
                    Text("Disabled")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ShowcaseCard(label = "AdaptiveIconButton") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AdaptiveIconButton(onClick = {}) {
                    Icon(Icons.Outlined.Favorite, contentDescription = "Favorite")
                }
                AdaptiveIconButton(onClick = {}) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share")
                }
                AdaptiveIconButton(onClick = {}) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add")
                }
                AdaptiveIconButton(onClick = {}, enabled = false) {
                    Icon(Icons.Outlined.Favorite, contentDescription = "Disabled")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -- Toggle & Slider --
        ShowcaseSectionHeader(title = "Controls")

        ShowcaseCard(label = "AdaptiveSwitch") {
            var switchA by remember { mutableStateOf(true) }
            var switchB by remember { mutableStateOf(false) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (switchA) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AdaptiveSwitch(
                    checked = switchA,
                    onCheckedChange = { switchA = it },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (switchB) "On" else "Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AdaptiveSwitch(
                    checked = switchB,
                    onCheckedChange = { switchB = it },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ShowcaseCard(label = "AdaptiveSlider") {
            var brightness by remember { mutableFloatStateOf(0.5f) }
            var volume by remember { mutableFloatStateOf(0.75f) }

            Text(
                text = "Brightness \u2014 ${(brightness * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AdaptiveSlider(
                value = brightness,
                onValueChange = { brightness = it },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Volume \u2014 ${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AdaptiveSlider(
                value = volume,
                onValueChange = { volume = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -- Progress Indicator --
        ShowcaseSectionHeader(title = "Progress Indicators")

        ShowcaseCard(label = "AdaptiveCircularProgressIndicator") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AdaptiveCircularProgressIndicator()
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AdaptiveCircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "48dp",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AdaptiveCircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Colored",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tab: Pickers (Dropdown, DatePicker, TimePicker)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
private fun ShowcasePickersTab(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selection and input components.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // -- Dropdown Menu --
        ShowcaseSectionHeader(title = "Dropdown Menu")

        ShowcaseCard(label = "AdaptiveDropDown") {
            var dropdownExpanded by remember { mutableStateOf(false) }
            var selectedAction by remember { mutableStateOf("Tap the button to open the menu") }

            val items = listOf(
                AdaptiveDropDownItem(
                    title = "Copy",
                    iosIcon = UIKitImage.SystemName(SFSymbol.documentOnDocument),
                    onClick = { selectedAction = "Copied!" },
                ),
                AdaptiveDropDownItem(
                    title = "Share",
                    iosIcon = UIKitImage.SystemName(SFSymbol.squareAndArrowUp),
                    onClick = { selectedAction = "Shared!" },
                ),
                AdaptiveDropDownItem(
                    title = "Delete",
                    iosIcon = UIKitImage.SystemName(SFSymbol.trash),
                    isDestructive = true,
                    onClick = { selectedAction = "Deleted!" },
                ),
            )

            Text(
                text = "Uses iOS UIMenu pull-down menu on iOS, Material3 DropdownMenu elsewhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedAction,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Context actions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    AdaptiveIconButton(onClick = { dropdownExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    AdaptiveDropDown(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        iosItems = items,
                    ) {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.title) },
                                enabled = !item.isDisabled,
                                onClick = {
                                    dropdownExpanded = false
                                    item.onClick()
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -- Date & Time Pickers --
        ShowcaseSectionHeader(title = "Date & Time Pickers")

        ShowcaseCard(label = "AdaptiveDatePicker") {
            val datePickerState = rememberAdaptiveDatePickerState()

            Text(
                text = "Selected: ${datePickerState.selectedDateMillis?.let { "${it}ms" } ?: "None"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AdaptiveDatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ShowcaseCard(label = "AdaptiveTimePicker") {
            val timePickerState = rememberAdaptiveTimePickerState()

            Text(
                text = "Selected: ${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AdaptiveTimePicker(
                state = timePickerState,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tab: Overlays (AlertDialog, BottomSheet)
// ---------------------------------------------------------------------------

@Composable
private fun ShowcaseOverlaysTab(
    paddingValues: PaddingValues,
    onShowAlertDialog: () -> Unit,
    onShowBottomSheet: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Dialogs and modal surfaces.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // -- Dialogs & Sheets --
        ShowcaseSectionHeader(title = "Dialogs & Sheets")

        ShowcaseCard(label = "AdaptiveAlertDialog") {
            Text(
                text = "Uses native UIAlertController on iOS, Material3 AlertDialog elsewhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdaptiveButton(onClick = onShowAlertDialog) {
                Text("Show Alert Dialog")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ShowcaseCard(label = "AdaptiveBottomSheet") {
            Text(
                text = "Native iOS sheet presentation on iOS, Material3 ModalBottomSheet elsewhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdaptiveButton(onClick = onShowBottomSheet) {
                Text("Show Bottom Sheet")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

/**
 * Section header for grouping showcase items.
 */
@Composable
private fun ShowcaseSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

/**
 * Card wrapper for each component showcase section.
 *
 * @param label The component name displayed as the card title.
 * @param content The composable content demonstrating the component.
 */
@Composable
private fun ShowcaseCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
