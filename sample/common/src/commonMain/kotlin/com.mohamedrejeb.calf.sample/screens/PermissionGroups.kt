package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.BluetoothSearching
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.SettingsInputAntenna
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiFind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.permissions.BackgroundLocation
import com.mohamedrejeb.calf.permissions.BluetoothAdvertise
import com.mohamedrejeb.calf.permissions.BluetoothConnect
import com.mohamedrejeb.calf.permissions.BluetoothLe
import com.mohamedrejeb.calf.permissions.BluetoothScan
import com.mohamedrejeb.calf.permissions.Call
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.CoarseLocation
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
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet

/** A permission as the demo lists it: a readable name and the tile of its row. */
internal data class PermissionItem(
    val permission: Permission,
    val title: String,
    val icon: ImageVector,
    val tint: Color,
)

/** A titled group of permissions, like a section of the system settings. */
internal data class PermissionGroup(
    val title: String,
    val items: List<PermissionItem>,
    val footer: String? = null,
    val showsAppSettings: Boolean = false,
)

/** Every permission the Calf modules define, grouped by what an app would use them for. */
internal val PermissionGroups: List<PermissionGroup> = listOf(
    PermissionGroup(
        title = "Camera and media",
        footer = "Tap a permission to ask for it; the small print is its Permission name. Desktop and the web " +
            "have no runtime permissions: every state reads as not allowed and a request does nothing.",
        items = listOf(
            PermissionItem(Permission.Camera, "Camera", Icons.Outlined.PhotoCamera, TileIndigo),
            PermissionItem(Permission.RecordAudio, "Microphone", Icons.Outlined.Mic, TileCoral),
            PermissionItem(Permission.Gallery, "Photo library", Icons.Outlined.PhotoLibrary, TileBlue),
            PermissionItem(Permission.ReadImage, "Read images", Icons.Outlined.Image, TileBlue),
            PermissionItem(Permission.ReadVideo, "Read videos", Icons.Outlined.VideoLibrary, TileViolet),
            PermissionItem(Permission.ReadAudio, "Read audio", Icons.Outlined.LibraryMusic, TileCoral),
            PermissionItem(Permission.ReadStorage, "Read storage", Icons.Outlined.SdStorage, TileTeal),
            PermissionItem(Permission.WriteStorage, "Write storage", Icons.Outlined.SaveAlt, TileTeal),
        ),
    ),
    PermissionGroup(
        title = "Location",
        items = listOf(
            PermissionItem(Permission.FineLocation, "Precise location", Icons.Outlined.MyLocation, TileBlue),
            PermissionItem(Permission.CoarseLocation, "Approximate location", Icons.Outlined.LocationOn, TileBlue),
            PermissionItem(Permission.BackgroundLocation, "Background location", Icons.Outlined.NearMe, TileIndigo),
        ),
    ),
    PermissionGroup(
        title = "Notifications",
        showsAppSettings = true,
        items = listOf(
            PermissionItem(Permission.Notification, "Notifications", Icons.Outlined.Notifications, TileCoral),
            PermissionItem(Permission.RemoteNotification, "Push notifications", Icons.Outlined.NotificationsActive, TileCoral),
        ),
    ),
    PermissionGroup(
        title = "Nearby devices",
        items = listOf(
            PermissionItem(Permission.BluetoothLe, "Bluetooth", Icons.Outlined.Bluetooth, TileBlue),
            PermissionItem(Permission.BluetoothScan, "Scan for devices", Icons.AutoMirrored.Outlined.BluetoothSearching, TileBlue),
            PermissionItem(Permission.BluetoothConnect, "Connect to devices", Icons.Outlined.BluetoothConnected, TileBlue),
            PermissionItem(Permission.BluetoothAdvertise, "Advertise", Icons.Outlined.SettingsInputAntenna, TileBlue),
            PermissionItem(Permission.WifiAccessState, "Wi-Fi state", Icons.Outlined.Wifi, TileTeal),
            PermissionItem(Permission.WifiChangeState, "Change Wi-Fi", Icons.Outlined.Router, TileTeal),
            PermissionItem(Permission.WifiNearbyDevices, "Nearby Wi-Fi devices", Icons.Outlined.WifiFind, TileTeal),
        ),
    ),
    PermissionGroup(
        title = "Contacts, calendar and calls",
        items = listOf(
            PermissionItem(Permission.ReadContacts, "Read contacts", Icons.Outlined.Contacts, TileAmber),
            PermissionItem(Permission.WriteContacts, "Edit contacts", Icons.Outlined.PersonAdd, TileAmber),
            PermissionItem(Permission.ReadCalendar, "Read calendar", Icons.Outlined.CalendarMonth, TileCoral),
            PermissionItem(Permission.WriteCalendar, "Edit calendar", Icons.Outlined.EditCalendar, TileCoral),
            PermissionItem(Permission.Call, "Phone calls", Icons.Outlined.Call, TileTeal),
        ),
    ),
)
