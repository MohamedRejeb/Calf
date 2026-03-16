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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.permissions.BackgroundLocation
import com.mohamedrejeb.calf.permissions.BluetoothAdvertise
import com.mohamedrejeb.calf.permissions.BluetoothConnect
import com.mohamedrejeb.calf.permissions.BluetoothLe
import com.mohamedrejeb.calf.permissions.BluetoothScan
import com.mohamedrejeb.calf.permissions.Call
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.CoarseLocation
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.FineLocation
import com.mohamedrejeb.calf.permissions.Gallery
import com.mohamedrejeb.calf.permissions.Notification
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.ReadAudio
import com.mohamedrejeb.calf.permissions.ReadCalendar
import com.mohamedrejeb.calf.permissions.ReadContacts
import com.mohamedrejeb.calf.permissions.ReadImage
import com.mohamedrejeb.calf.permissions.ReadStorage
import com.mohamedrejeb.calf.permissions.ReadVideo
import com.mohamedrejeb.calf.permissions.RecordAudio
import com.mohamedrejeb.calf.permissions.RemoteNotification
import com.mohamedrejeb.calf.permissions.WifiAccessState
import com.mohamedrejeb.calf.permissions.WifiChangeState
import com.mohamedrejeb.calf.permissions.WifiNearbyDevices
import com.mohamedrejeb.calf.permissions.WriteCalendar
import com.mohamedrejeb.calf.permissions.WriteContacts
import com.mohamedrejeb.calf.permissions.WriteStorage
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import com.mohamedrejeb.calf.permissions.shouldShowRationale

@Composable
fun PermissionScreen(navigateBack: () -> Unit) {
    val permissions = remember {
        listOf(
            Permission.Call,
            Permission.Camera,
            Permission.Gallery,
            Permission.ReadStorage,
            Permission.WriteStorage,
            Permission.ReadImage,
            Permission.ReadVideo,
            Permission.ReadAudio,
            Permission.FineLocation,
            Permission.CoarseLocation,
            Permission.BackgroundLocation,
            Permission.Notification,
            Permission.RemoteNotification,
            Permission.RecordAudio,
            Permission.BluetoothLe,
            Permission.BluetoothScan,
            Permission.BluetoothConnect,
            Permission.BluetoothAdvertise,
            Permission.ReadContacts,
            Permission.WriteContacts,
            Permission.ReadCalendar,
            Permission.WriteCalendar,
            Permission.WifiAccessState,
            Permission.WifiChangeState,
            Permission.WifiNearbyDevices,
        )
    }

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
            items(permissions) { permission ->
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
            permissionState.launchPermissionRequest()
        },
    ) {
        Text(
            text = "Request permission",
        )
    }



    Spacer(modifier = Modifier.height(16.dp))

    if (permission.name == Permission.Notification.name) {
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
