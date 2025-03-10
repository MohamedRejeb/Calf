package com.mohamedrejeb.calf.permissions.helper

import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.PermissionStatus
import platform.Contacts.CNAuthorizationStatus
import platform.Contacts.CNAuthorizationStatusAuthorized
import platform.Contacts.CNAuthorizationStatusNotDetermined
import platform.Contacts.CNContactStore
import platform.Contacts.CNEntityType

internal class ContactPermissionHelper : PermissionHelper {
    override fun launchPermissionRequest(onPermissionResult: (Boolean) -> Unit) {
        handlePermissionRequest(
            onPermissionResult = onPermissionResult,
            launchPermissionRequest = {
                CNContactStore().requestAccessForEntityType(CNEntityType.CNEntityTypeContacts) { granted, _ ->
                    onPermissionResult(granted)
                }
            }
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun getPermissionStatus(onPermissionResult: (PermissionStatus) -> Unit) {
        val permissionStatus = when (getCurrentAuthorizationStatus()) {
            CNAuthorizationStatusAuthorized -> PermissionStatus.Granted
            CNAuthorizationStatusNotDetermined ->
                PermissionStatus.Denied(shouldShowRationale = true)

            else -> PermissionStatus.Denied(shouldShowRationale = false)
        }
        onPermissionResult(permissionStatus)
    }

    private fun getCurrentAuthorizationStatus(): CNAuthorizationStatus {
        return CNContactStore.authorizationStatusForEntityType(CNEntityType.CNEntityTypeContacts)
    }
}
