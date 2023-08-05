package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

@JvmInline
@Immutable
value class UIKitDisplayMode internal constructor(internal val value: Int) {
    companion object {
        /** Date picker mode */
        val Picker = UIKitDisplayMode(0)

        /** Date text input mode */
        val Wheels = UIKitDisplayMode(1)
    }

    override fun toString() = when (this) {
        Picker -> "Picker"
        Wheels -> "Wheels"
        else -> "Unknown"
    }
}