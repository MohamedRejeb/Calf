package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Effect that updates the `hasPermission` state of a revoked [MutablePermissionState] permission
 * when the lifecycle gets called with [lifecycleEvent].
 */
@ExperimentalPermissionsApi
@Composable
internal fun PermissionLifecycleCheckerEffect(
    permissionState: MutablePermissionState,
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
) {
    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission.
    val permissionCheckerObserver =
        remember(permissionState) {
            LifecycleEventObserver { _, event ->
                if (event == lifecycleEvent) {
                    // If the permission is revoked, check again.
                    // We don't check if the permission was denied as that triggers a process restart.
                    if (permissionState.status != PermissionStatus.Granted) {
                        permissionState.refreshPermissionStatus()
                    }
                }
            }
        }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, permissionCheckerObserver) {
        lifecycle.addObserver(permissionCheckerObserver)
        onDispose { lifecycle.removeObserver(permissionCheckerObserver) }
    }
}

/**
 * Effect that updates the `hasPermission` state of a list of permissions
 * when the lifecycle gets called with [lifecycleEvent] and the permission is revoked.
 */
@ExperimentalPermissionsApi
@Composable
internal fun PermissionsLifecycleCheckerEffect(
    permissions: List<MutablePermissionState>,
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
) {
    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission.
    val permissionsCheckerObserver =
        remember(permissions) {
            LifecycleEventObserver { _, event ->
                if (event == lifecycleEvent) {
                    for (permission in permissions) {
                        // If the permission is revoked, check again. We don't check if the permission
                        // was denied as that triggers a process restart.
                        if (permission.status != PermissionStatus.Granted) {
                            permission.refreshPermissionStatus()
                        }
                    }
                }
            }
        }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, permissionsCheckerObserver) {
        lifecycle.addObserver(permissionsCheckerObserver)
        onDispose { lifecycle.removeObserver(permissionsCheckerObserver) }
    }
}
