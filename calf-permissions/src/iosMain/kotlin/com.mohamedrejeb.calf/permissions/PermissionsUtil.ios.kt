package com.mohamedrejeb.calf.permissions

import com.mohamedrejeb.calf.permissions.helper.AVCapturePermissionHelper
import com.mohamedrejeb.calf.permissions.helper.BluetoothPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.CalendarPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.ContactPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.GalleryPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.GrantedPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.LocalNotificationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.LocationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.PermissionHelper
import com.mohamedrejeb.calf.permissions.helper.RemoteNotificationPermissionHelper
import com.mohamedrejeb.calf.permissions.helper.WifiPermissionHelper
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo

internal fun Permission.getPermissionDelegate(): PermissionHelper {
    return when (this) {
        Permission.Camera ->
            AVCapturePermissionHelper(AVMediaTypeVideo)

        Permission.Gallery,
        Permission.ReadImage,
        Permission.ReadVideo,
            ->
            GalleryPermissionHelper()

        Permission.ReadStorage,
        Permission.WriteStorage,
        Permission.ReadAudio,
        Permission.Call,
            ->
            GrantedPermissionHelper()

        Permission.FineLocation,
        Permission.CoarseLocation,
        Permission.BackgroundLocation,
            ->
            LocationPermissionHelper()

        Permission.Notification ->
            LocalNotificationPermissionHelper()

        Permission.RemoteNotification ->
            RemoteNotificationPermissionHelper()

        Permission.RecordAudio ->
            AVCapturePermissionHelper(AVMediaTypeAudio)

        Permission.BluetoothLe,
        Permission.BluetoothScan,
        Permission.BluetoothConnect,
        Permission.BluetoothAdvertise,
            ->
            BluetoothPermissionHelper()

        Permission.ReadContacts -> ContactPermissionHelper()
        Permission.ReadCalendar -> CalendarPermissionHelper()
        Permission.WriteCalendar -> CalendarPermissionHelper()

        Permission.WifiAccessState,
        Permission.WifiChangeState,
        Permission.WifiNearbyDevices,
            ->
            WifiPermissionHelper()
    }
}
