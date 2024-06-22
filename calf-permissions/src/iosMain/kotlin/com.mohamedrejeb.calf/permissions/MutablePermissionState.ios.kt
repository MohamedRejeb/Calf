package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * Creates a [MutablePermissionState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permission the permission to control and observe.
 * @param onPermissionResult will be called with whether or not the user granted the permission
 *  after [PermissionState.launchPermissionRequest] is called.
 */
@ExperimentalPermissionsApi
@Composable
internal actual fun rememberMutablePermissionState(
    permission: Permission,
    onPermissionResult: (Boolean) -> Unit,
): MutablePermissionState {
    val permissionState =
        remember(permission) {
            MutablePermissionState(
                permission = permission,
                onPermissionResult = onPermissionResult,
            )
        }

    return permissionState
}

/**
 * A mutable state object that can be used to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberMutablePermissionState].
 *
 * @param permission the permission to control and observe.
 */
@ExperimentalPermissionsApi
@Stable
internal actual class MutablePermissionState(
    actual override val permission: Permission,
    private val onPermissionResult: (Boolean) -> Unit,
) : PermissionState {
    actual constructor(
        permission: Permission,
    ) : this(permission, {})

    private val permissionDelegate = permission.getPermissionDelegate()

    actual override var status: PermissionStatus by mutableStateOf(PermissionStatus.Denied(false))

    init {
        refreshPermissionStatus()
    }

    actual override fun launchPermissionRequest() {
        permissionDelegate.launchPermissionRequest(
            onPermissionResult = {
                onPermissionResult(it)
                refreshPermissionStatus()
            },
        )
    }

    actual override fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(settingsUrl)
    }

    internal actual fun refreshPermissionStatus() {
        permissionDelegate.getPermissionStatus { status ->
            this.status = status
        }
    }
}
