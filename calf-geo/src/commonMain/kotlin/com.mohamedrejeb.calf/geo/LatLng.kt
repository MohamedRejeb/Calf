package com.mohamedrejeb.calf.geo

import kotlin.math.*

/**
 * A class representing a pair of latitude and longitude coordinates, stored as degrees.
 *
 * @property latitude The latitude in degrees.
 * @property longitude The longitude in degrees.
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
) {

    /**
     * Returns the distance to the given [LatLng] in kilometers.
     *
     * @param latLng The [LatLng] to calculate the distance to.
     * @return The distance to the given [LatLng] in kilometers.
     */
    fun distanceTo(latLng: LatLng): Double {
        val lat1 = latitude
        val lon1 = longitude
        val lat2 = latLng.latitude
        val lon2 = latLng.longitude

        val r = EarthRadius
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * asin(sqrt(a))
        return r * c
    }

    /**
     * Returns the angle to the given [LatLng] in degrees.
     *
     * @param latLng The [LatLng] to calculate the angle to.
     * @param rounded Whether the angle should be rounded to the nearest 10 degrees.
     * @return The angle to the given [LatLng] in degrees.
     */
    fun getAngleTo(latLng: LatLng, rounded: Boolean = true): Double {
        val lat1 = toRadians(this.latitude)
        val lat2 = toRadians(latLng.latitude)
        val lon1 = toRadians(this.longitude)
        val lon2 = toRadians(latLng.longitude)

        val dLon = lon2 - lon1

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                (sin(lat1) * cos(lat2) * cos(dLon))

        var brng = atan2(y, x)

        brng = toDegree(brng)
        brng = (brng + 360) % 360

        var angle = brng
        if (rounded) {
            angle = (round(brng / 10) * 10)
        }
        if (angle == 360.0) angle = 0.0

        return angle
    }

    private fun toRadians(angle: Double): Double {
        return angle * PI / 180.0
    }

    private fun toDegree(angle: Double): Double {
        return angle * 180.0 / PI
    }

    private companion object {
        const val EarthRadius = 6371.00 // in Kilometers
    }
}