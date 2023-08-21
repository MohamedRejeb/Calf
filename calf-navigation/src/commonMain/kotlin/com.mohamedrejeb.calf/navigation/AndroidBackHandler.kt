package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun AndroidBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)