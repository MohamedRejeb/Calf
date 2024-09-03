package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.permissions.shouldShowRationale

@Composable
fun PermissionScreen(navigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime),
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            items(Permission.entries) { permission ->
                PermissionItem(permission = permission)
            }
        }

        IconButton(
            onClick = {
                navigateBack()
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground,
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
        ) {
            Icon(
                Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionItem(permission: Permission) {
    val permissionState = rememberPermissionState(permission = permission)

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = permission.name,
    )

    Text(
        text = "Is permission granted: ${permissionState.status.isGranted}",
    )

    LaunchedEffect(permissionState.status) {
        println("${permission.name}: ${permissionState.status}")
        if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale) {
            println("${permission.name}: Show Rationale")
        }
    }

    Button(
        onClick = {
            println("Click")
            permissionState.launchPermissionRequest()
        },
    ) {
        Text(
            text = "Request permission",
        )
    }



    Spacer(modifier = Modifier.height(16.dp))

    if (permission == Permission.Notification) {
        Button(
            onClick = {
                permissionState.openAppSettings()
            },
        ) {
            Text(
                text = "Open Notification Settings",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }


    HorizontalDivider()
}
