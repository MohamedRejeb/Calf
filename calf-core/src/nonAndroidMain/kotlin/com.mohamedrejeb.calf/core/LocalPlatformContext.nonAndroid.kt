package com.mohamedrejeb.calf.core

import androidx.compose.runtime.staticCompositionLocalOf

actual val LocalPlatformContext =
    staticCompositionLocalOf {
        PlatformContext.INSTANCE
    }
