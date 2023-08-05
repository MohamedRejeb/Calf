package com.mohamedrejeb.calf.ui.dialog

import androidx.compose.runtime.Composable

@Composable
expect fun AdaptiveAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String,
    dismissText: String,
    title: String,
    text: String,
    properties: DialogProperties = DialogProperties(),
)