package com.mohamedrejeb.calf.foldable

import kotlin.math.PI

internal const val HINGE_DEGREES_SHUT = 0f
internal const val HINGE_DEGREES_FLAT = 180f

/**
 * Converts `UIHinge.angle` (radians, 0 = shut, π = flat) to degrees within 0..180. iOS gives no
 * guarantee about the range, so anything outside it (and NaN) is pulled back in.
 */
internal fun hingeDegreesOf(radians: Double): Float {
    if (radians.isNaN()) return HINGE_DEGREES_SHUT
    val degrees = radians * 180.0 / PI
    return degrees.coerceIn(HINGE_DEGREES_SHUT.toDouble(), HINGE_DEGREES_FLAT.toDouble()).toFloat()
}
