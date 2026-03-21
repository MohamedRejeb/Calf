package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScreen(
    navigateBack: () -> Unit
) {
    var allowHide by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberAdaptiveSheetState(
        confirmValueChange = {
            it != SheetValue.Hidden || allowHide
        }
    )
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    SampleScreenScaffold(
        title = "Adaptive Bottom Sheet",
        navigateBack = navigateBack,
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Modal bottom sheets with native iOS presentation style.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            AdaptiveButton(
                onClick = { openBottomSheet = true },
            ) {
                Text("Show Bottom Sheet")
            }
        }

        if (openBottomSheet) {
            AdaptiveBottomSheet(
                onDismissRequest = {
                    openBottomSheet = false
                },
                adaptiveSheetState = sheetState,
            ) {
                var showDialog by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier
                ) {
                    item {
                        Text("Bottom Sheet")
                        Spacer(modifier = Modifier.height(16.dp))
                        AdaptiveButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        openBottomSheet = false
                                    }
                                }
                            }
                        ) {
                            Text("Close")
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AdaptiveSwitch(
                                checked = allowHide,
                                onCheckedChange = {
                                    allowHide = it
                                }
                            )
                            Text("Allow swipe to hide")
                        }
                    }

                    item {
                        AdaptiveButton(
                            onClick = {
                                showDialog = true
                            },
                        ) {
                            Text("Show Alert Dialog")
                        }

                        if (showDialog) {
                            AdaptiveAlertDialog(
                                onConfirm = {
                                    showDialog = false
                                },
                                onDismiss = {
                                    showDialog = false
                                },
                                confirmText = "Ok",
                                dismissText = "Cancel",
                                title = "Alert Dialog",
                                text = "This is a native alert dialog from Calf",
                            )
                        }
                    }

                    items(100) {
                        Text(
                            text = "Item $it",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}