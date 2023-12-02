package com.mohamedrejeb.calf.geo

/**
 * A class representing the coordinates of a location, stored as a [LatLng] and an accuracy in meters.
 *
 * @property coordinates The coordinates of the location.
 * @property coordinatesAccuracyMeters The accuracy of the coordinates in meters.
 */
data class Location(
    val coordinates: LatLng,
    val coordinatesAccuracyMeters: Double
)