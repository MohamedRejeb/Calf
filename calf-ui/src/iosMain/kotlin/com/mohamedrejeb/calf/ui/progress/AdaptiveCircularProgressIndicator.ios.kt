package com.mohamedrejeb.calf.ui.progress

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.utils.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIActivityIndicatorView

@Composable
actual fun AdaptiveCircularProgressIndicator(
    modifier: Modifier,
    color: Color,
    strokeWidth: Dp,
    trackColor: Color,
    strokeCap: StrokeCap,
) {
    CupertinoActivityIndicator(
        modifier = modifier,
        color = color,
    )
}