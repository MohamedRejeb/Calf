package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDown
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun DropDownScreen(
    navigateBack: () -> Unit,
) {
    var selectedAction by remember { mutableStateOf("Tap a button to open a drop-down") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = navigateBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Adaptive Drop Down",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // Status text
            Text(
                text = selectedAction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simple drop-down
            var simpleExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simple Drop Down",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Tap the button to see Copy, Share, Delete options",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Box {
                        IconButton(onClick = { simpleExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }

                        val items = listOf(
                            AdaptiveDropDownItem(
                                title = "Copy",
                                onClick = { selectedAction = "Copied!" },
                            ),
                            AdaptiveDropDownItem(
                                title = "Share",
                                onClick = { selectedAction = "Shared!" },
                            ),
                            AdaptiveDropDownItem(
                                title = "Delete",
                                isDestructive = true,
                                onClick = { selectedAction = "Deleted!" },
                            ),
                        )

                        AdaptiveDropDown(
                            expanded = simpleExpanded,
                            onDismissRequest = { simpleExpanded = false },
                            iosItems = items,
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    enabled = !item.isDisabled,
                                    onClick = {
                                        simpleExpanded = false
                                        item.onClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Drop-down with sections
            var sectionedExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sectioned Drop Down",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Tap the button to see grouped menu with sections",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Box {
                        IconButton(onClick = { sectionedExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }

                        val items = listOf(
                            AdaptiveDropDownItem(
                                title = "Open",
                                onClick = { selectedAction = "Opened!" },
                            ),
                            AdaptiveDropDownItem(
                                title = "Edit",
                                onClick = { selectedAction = "Editing!" },
                            ),
                        )

                        val sections = listOf(
                            AdaptiveDropDownSection(
                                title = "Danger Zone",
                                items = listOf(
                                    AdaptiveDropDownItem(
                                        title = "Archive",
                                        onClick = { selectedAction = "Archived!" },
                                    ),
                                    AdaptiveDropDownItem(
                                        title = "Delete",
                                        isDestructive = true,
                                        onClick = { selectedAction = "Deleted!" },
                                    ),
                                ),
                            ),
                        )

                        AdaptiveDropDown(
                            expanded = sectionedExpanded,
                            onDismissRequest = { sectionedExpanded = false },
                            iosItems = items,
                            iosSections = sections,
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    enabled = !item.isDisabled,
                                    onClick = {
                                        sectionedExpanded = false
                                        item.onClick()
                                    },
                                )
                            }

                            sections.forEach { section ->
                                HorizontalDivider()
                                section.items.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.title) },
                                        enabled = !item.isDisabled,
                                        onClick = {
                                            sectionedExpanded = false
                                            item.onClick()
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
