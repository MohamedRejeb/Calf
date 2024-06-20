package com.mohamedrejeb.calf.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.ui.cupertino.CupertinoActivityIndicator
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import com.mohamedrejeb.calf.ui.sheet.AdaptiveBottomSheet
import com.mohamedrejeb.calf.ui.sheet.rememberAdaptiveSheetState
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch
import com.mohamedrejeb.calf.ui.toggle.CupertinoSwitch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchScreen(
    navigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navigateBack() }
                    ) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                },
                title = {
                    Text(
                        text = Screen.Switch.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) { paddingValues ->

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .windowInsetsPadding(WindowInsets.ime)
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