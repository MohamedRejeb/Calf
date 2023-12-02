package com.mohamedrejeb.calf.geo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State

expect class LocationTracker {
//    val permissionsController: PermissionsController

    suspend fun startTracking() // can be suspended for request permission
    fun stopTracking()

    val locationState: State<LatLng>

    val extendedLocationState: State<ExtendedLocation>
}