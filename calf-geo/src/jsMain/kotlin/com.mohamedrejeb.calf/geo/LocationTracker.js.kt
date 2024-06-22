package com.mohamedrejeb.calf.geo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

actual class LocationTracker {
    actual suspend fun startTracking() {
        println("startTracking")
    }

    actual fun stopTracking() {
        println("stopTracking")
    }

    private val _locationState: MutableState<LatLng> = mutableStateOf(LatLng(0.0, 0.0))
    actual val locationState: State<LatLng> = _locationState

    private val _extendedLocationState = mutableStateOf(
        ExtendedLocation(
            location = Location(
                coordinates = LatLng(0.0, 0.0),
                coordinatesAccuracyMeters = 0.0
            ),
            azimuth = Azimuth(
                azimuthDegrees = 0.0,
                azimuthAccuracyDegrees = null
            ),
            speed = Speed(
                speedMps = 0.0,
                speedAccuracyMps = null
            ),
            altitude = Altitude(
                altitudeMeters = 0.0,
                altitudeAccuracyMeters = null
            ),
            timestampMs = 0L
        )
    )
    actual val extendedLocationState: State<ExtendedLocation> = _extendedLocationState
}