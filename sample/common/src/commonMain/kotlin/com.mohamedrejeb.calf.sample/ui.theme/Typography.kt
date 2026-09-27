package com.mohamedrejeb.calf.sample.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Base = Typography()

/** Material's scale with heavier, tighter titles; the body sizes are unchanged. */
val Typography =
    Typography(
        displayLarge = Base.displayLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        displayMedium = Base.displayMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        displaySmall = Base.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineLarge = Base.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
        headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = Base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = Base.bodyLarge,
        bodyMedium = Base.bodyMedium,
        bodySmall = Base.bodySmall,
        labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        labelMedium = Base.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = Base.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
    )
