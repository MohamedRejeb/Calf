package com.mohamedrejeb.calf.permissions

/**
 * Configuration for mapping a [Permission] to its Android permission string.
 *
 * @param permissionString the Android manifest permission string (e.g. [android.Manifest.permission.CAMERA]).
 * @param minSdkVersion the minimum Android SDK version where this permission applies (inclusive).
 * @param maxSdkVersion the maximum Android SDK version where this permission applies (inclusive).
 * @param alwaysGranted whether this permission is always considered granted on Android.
 */
class AndroidPermissionMapping(
    val permissionString: String,
    val minSdkVersion: Int = 0,
    val maxSdkVersion: Int = Int.MAX_VALUE,
    val alwaysGranted: Boolean = false,
)

/**
 * Registry for Android permission mappings.
 *
 * Each permission module registers its Android permission mappings here
 * via the actual implementation of its register function in androidMain.
 */
object AndroidPermissionRegistry {
    private val mappings = mutableMapOf<Permission, AndroidPermissionMapping>()

    fun register(permission: Permission, mapping: AndroidPermissionMapping) {
        mappings[permission] = mapping
    }

    internal fun getMapping(permission: Permission): AndroidPermissionMapping? =
        mappings[permission]

    internal fun findByAndroidString(androidPermission: String, sdkInt: Int): Permission? =
        mappings.entries.firstOrNull { (_, mapping) ->
            mapping.permissionString == androidPermission &&
                sdkInt in mapping.minSdkVersion..mapping.maxSdkVersion
        }?.key
}
