package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.Composable

@Composable
internal actual fun systemIs24HourFormat(): Boolean = isTwentyFourHourFromHour12Flag(resolvedHour12Flag())

/**
 * The `hour12` option the browser resolves for the user's locale when an hour is formatted:
 * [HOUR12_TRUE], [HOUR12_FALSE], or [HOUR12_UNKNOWN] when the browser does not report it.
 */
@Suppress("UnsafeCastFromDynamic")
private fun resolvedHour12Flag(): Int =
    js(
        """{
            try {
                var hour12 = new Intl.DateTimeFormat(undefined, { hour: 'numeric' }).resolvedOptions().hour12;
                if (hour12 === true) return 1;
                if (hour12 === false) return 0;
                return -1;
            } catch (error) {
                return -1;
            }
        }"""
    )
