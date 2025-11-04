package com.mohamedrejeb.calf.ui.progress

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.calf.ui.cupertino.CupertinoActivityIndicator

@Composable
actual fun AdaptiveCircularProgressIndicator(
    modifier: Modifier,
    color: Color,
    strokeWidth: Dp,
    trackColor: Color,
    strokeCap: StrokeCap,
    gapSize: Dp,
) {
    CupertinoActivityIndicator(
        modifier = modifier,
        color = color,
    )
}