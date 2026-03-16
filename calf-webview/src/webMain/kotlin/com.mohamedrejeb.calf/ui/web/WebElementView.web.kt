package com.mohamedrejeb.calf.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.w3c.dom.HTMLElement

@Composable
internal expect fun <T : HTMLElement> CalfWebElementView(
    factory: () -> T,
    modifier: Modifier,
    update: (T) -> Unit,
)
