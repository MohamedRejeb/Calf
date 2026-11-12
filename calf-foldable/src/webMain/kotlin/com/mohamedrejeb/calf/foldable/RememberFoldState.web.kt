package com.mohamedrejeb.calf.foldable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** No fold API on this platform: always [FoldSnapshot.NoFold]. */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
internal actual fun rememberPlatformFoldState(): FoldState =
    remember { nonIosFoldState() }
