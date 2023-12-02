package com.mohamedrejeb.calf.geo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

actual class LocationTracker(
    interval: Long = 1000,
    priority: Int = LocationRequest.PRIORITY_HIGH_ACCURACY
) : LocationCallback() {
    private var locationProviderClient: FusedLocationProviderClient? = null
    private var isStarted: Boolean = false
    private val locationRequest = LocationRequest().also {
        it.interval = interval
        it.priority = priority
    }
    private val locationsChannel = Channel<LatLng>(Channel.CONFLATED)
    private val extendedLocationsChannel = Channel<ExtendedLocation>(Channel.CONFLATED)
    private val trackerScope = CoroutineScope(Dispatchers.Main)

    fun bind(lifecycle: Lifecycle, context: Context, fragmentManager: FragmentManager) {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        @SuppressLint("MissingPermission")
        if (isStarted) {
            locationProviderClient?.requestLocationUpdates(locationRequest, this, null)
        }

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                locationProviderClient?.removeLocationUpdates(this@LocationTracker)
                locationProviderClient = null
            }
        })
    }

    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)

        val lastLocation = locationResult.lastLocation ?: return

        val speedAccuracy = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) null
        else lastLocation.speedAccuracyMetersPerSecond.toDouble()

        val bearingAccuracy = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) null
        else lastLocation.bearingAccuracyDegrees.toDouble()

        val verticalAccuracy = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) null
        else lastLocation.verticalAccuracyMeters.toDouble()

        val latLng = LatLng(
            lastLocation.latitude,
            lastLocation.longitude
        )

        val locationPoint = Location(
            coordinates = latLng,
            coordinatesAccuracyMeters = lastLocation.accuracy.toDouble()
        )

        val speed = Speed(
            speedMps = lastLocation.speed.toDouble(),
            speedAccuracyMps = speedAccuracy
        )

        val azimuth = Azimuth(
            azimuthDegrees = lastLocation.bearing.toDouble(),
            azimuthAccuracyDegrees = bearingAccuracy
        )

        val altitude = Altitude(
            altitudeMeters = lastLocation.altitude,
            altitudeAccuracyMeters = verticalAccuracy
        )

        val extendedLocation = ExtendedLocation(
            location = locationPoint,
            azimuth = azimuth,
            speed = speed,
            altitude = altitude,
            timestampMs = lastLocation.time
        )

        trackerScope.launch {
            extendedLocationsChannel.send(extendedLocation)
            locationsChannel.send(latLng)
        }
    }

    @SuppressLint("MissingPermission")
    actual suspend fun startTracking() {
        // if permissions request failed - execution stops here

        isStarted = true
        locationProviderClient?.requestLocationUpdates(locationRequest, this, null)
    }

    actual fun stopTracking() {
        isStarted = false
        locationProviderClient?.removeLocationUpdates(this)
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