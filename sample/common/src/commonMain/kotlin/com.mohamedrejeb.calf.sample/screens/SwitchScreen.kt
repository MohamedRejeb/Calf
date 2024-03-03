package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchScreen(
    navigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        IconButton(
            onClick = {
                navigateBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            val firstSwitchState = remember { mutableStateOf(false) }
            AdaptiveSwitch(
                checked = firstSwitchState.value,
                onCheckedChange = { firstSwitchState.value = it },
                modifier = Modifier
                    .padding(16.dp)
            )
            val secondSwitchState = remember { mutableStateOf(true) }
            AdaptiveSwitch(
                checked = secondSwitchState.value,
                onCheckedChange = { secondSwitchState.value = it },
                modifier = Modifier
                    .padding(16.dp)
            )
        }
    }
}