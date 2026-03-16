package com.mohamedrejeb.calf.permissions

import androidx.compose.runtime.Stable

@RequiresOptIn(message = "Calf Permissions is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalPermissionsApi

/**
 * Model of the status of a permission. It can be granted or denied.
 * If denied, the user might need to be presented with a rationale.
 */
@ExperimentalPermissionsApi
@Stable
sealed interface PermissionStatus {
    data object Granted : PermissionStatus
    data class Denied(
        val shouldShowRationale: Boolean,
    ) : PermissionStatus
}

/**
 * `true` if the permission is granted.
 */
@ExperimentalPermissionsApi
val PermissionStatus.isGranted: Boolean
    get() = this == PermissionStatus.Granted

/**
 * `true` if the permission is not granted.
 */
@ExperimentalPermissionsApi
val PermissionStatus.isNotGranted: Boolean
    get() = !isGranted

/**
 * `true` if the permission is denied.
 */
@ExperimentalPermissionsApi
val PermissionStatus.isDenied: Boolean
    get() = this is PermissionStatus.Denied

/**
 * `true` if a rationale should be presented to the user.
 */
@ExperimentalPermissionsApi
val PermissionStatus.shouldShowRationale: Boolean
    get() = when (this) {
        is PermissionStatus.Granted ->
            false

        is PermissionStatus.Denied ->
            shouldShowRationale
    }