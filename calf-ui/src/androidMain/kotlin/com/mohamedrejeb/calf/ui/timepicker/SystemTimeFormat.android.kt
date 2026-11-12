package com.mohamedrejeb.calf.ui.timepicker

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun systemIs24HourFormat(): Boolean = DateFormat.is24HourFormat(LocalContext.current)
