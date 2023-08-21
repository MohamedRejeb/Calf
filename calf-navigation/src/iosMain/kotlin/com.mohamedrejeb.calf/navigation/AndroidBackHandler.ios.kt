package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun AndroidBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // This is a no-op on iOS
}