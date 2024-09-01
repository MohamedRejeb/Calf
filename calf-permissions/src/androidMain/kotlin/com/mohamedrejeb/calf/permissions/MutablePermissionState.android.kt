package com.mohamedrejeb.calf.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current
    val permissionState = remember(permission) {
        MutablePermissionStateImpl(permission, context, context.findActivity(), onPermissionResult)
    }

    // Refresh the permission status when the lifecycle is resumed
    PermissionLifecycleCheckerEffect(permissionState)

    // Remember RequestPermission launcher and assign it to permissionState
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionState.refreshPermissionStatus()
        onPermissionResult(it)
    }
    DisposableEffect(permissionState, launcher) {
        permissionState.launcher = launcher
        onDispose {
            permissionState.launcher = null
        }
    }

    return permissionState
}

/**
 * A mutable state object that can be used to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberMutablePermissionState].
 *
 * @param permission the permission to control and observe.
 * @param context to check the status of the [permission].
 * @param activity to check if the user should be presented with a rationale for [permission].
 */
@ExperimentalPermissionsApi
@Stable
internal class MutablePermissionStateImpl(
    override val permission: Permission,
    private val context: Context,
    private val activity: Activity,
    private val onPermissionResult: (Boolean) -> Unit,
) : MutablePermissionState {

    private val androidPermission = permission.toAndroidPermission()

    override var status: PermissionStatus by mutableStateOf(getPermissionStatus())

    override fun launchPermissionRequest() {
        if (androidPermission.isEmpty() || permission.isAlwaysGranted()) {
            refreshPermissionStatus()
            onPermissionResult(true)
            return
        }

        launcher?.launch(
            androidPermission
        ) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
    }

    internal var launcher: ActivityResultLauncher<String>? = null

    override fun openAppSettings() {
        val intent =
            if (permission == Permission.Notification && supportsNotificationSettings())
                createAppNotificationsIntent(context)
            else
                createAppSettingsIntent(context)

        context.startActivity(intent)
    }

    override fun refreshPermissionStatus() {
        status = getPermissionStatus()
    }

    private fun getPermissionStatus(): PermissionStatus {
        if (androidPermission.isEmpty() || permission.isAlwaysGranted())
            return PermissionStatus.Granted

        val hasPermission = context.checkPermission(androidPermission)
        return if (hasPermission)
            PermissionStatus.Granted
        else
            PermissionStatus.Denied(activity.shouldShowRationale(androidPermission))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAppNotificationsIntent(context: Context) =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

    private fun supportsNotificationSettings() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun createAppSettingsIntent(context: Context) =
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
}