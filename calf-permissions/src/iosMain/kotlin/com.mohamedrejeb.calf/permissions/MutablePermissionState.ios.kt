package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    onPermissionResult: (PermissionStatus) -> Unit,
): MutablePermissionState {
    val scope = rememberCoroutineScope()

    val permissionState =
        remember(permission) {
            MutablePermissionStateImpl(
                permission = permission,
                onPermissionResult = onPermissionResult,
                scope = scope,
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
internal class MutablePermissionStateImpl(
    override val permission: Permission,
    private val onPermissionResult: (PermissionStatus) -> Unit,
    private val scope: CoroutineScope,
) : MutablePermissionState {

    private val permissionDelegate = permission.getPermissionDelegate()

    override var status: PermissionStatus by mutableStateOf(PermissionStatus.Denied(shouldShowRationale = false))

    init {
        refreshPermissionStatus()
    }

    override fun launchPermissionRequest() {
        permissionDelegate.launchPermissionRequest(
            onPermissionResult = {
                onPermissionResult(it)
                refreshPermissionStatus()
            },
        )
    }

    override fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(settingsUrl)
    }

    override fun refreshPermissionStatus() {
        permissionDelegate.getPermissionStatus { status ->
            scope.launch(Dispatchers.Main) {
                this@MutablePermissionStateImpl.status = status
            }
        }
    }
}
