package com.mohamedrejeb.calf.geo

data class ExtendedLocation(
    val location: Location,
    val azimuth: Azimuth,
    val speed: Speed,
    val altitude: Altitude,
    val timestampMs: Long
)