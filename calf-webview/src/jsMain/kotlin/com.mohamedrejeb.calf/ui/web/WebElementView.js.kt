package com.mohamedrejeb.calf.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.WebElementView
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun <T : HTMLElement> CalfWebElementView(
    factory: () -> T,
    modifier: Modifier,
    update: (T) -> Unit,
) {
    WebElementView(
        factory = factory,
        modifier = modifier,
        update = update,
    )
}
