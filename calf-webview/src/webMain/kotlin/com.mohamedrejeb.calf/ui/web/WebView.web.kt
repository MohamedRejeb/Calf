package com.mohamedrejeb.calf.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.events.Event

/**
 * A wrapper around an HTML iframe to provide a basic WebView composable for Web (JS/WasmJS).
 *
 * Uses [WebElementView] to embed an [HTMLIFrameElement] inside the Compose canvas.
 * Note: Cross-origin restrictions apply — features like JavaScript evaluation, page title,
 * and navigation history only work for same-origin content.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Not applicable on Web. Reserved for API consistency.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the iframe element is first created.
 * @param onDispose Called when the iframe element is destroyed.
 */
@Composable
actual fun WebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    val iframe = state.iframe

    iframe?.let { iframeEl ->
        LaunchedEffect(iframeEl, navigator) {
            navigator.handleNavigationEvents(iframeEl)
        }

        LaunchedEffect(iframeEl, state) {
            snapshotFlow { state.content }.collect { content ->
                when (content) {
                    is WebContent.Url -> {
                        iframeEl.src = content.url
                    }

                    is WebContent.Data -> {
                        iframeEl.srcdoc = content.data
                    }

                    is WebContent.NavigatorOnly -> {
                        // NO-OP
                    }
                }
            }
        }
    }

    CalfWebElementView(
        factory = {
            (document.createElement("iframe") as HTMLIFrameElement).apply {
                style.border = "none"
                width = "100%"
                height = "100%"

                addEventListener("load", { _: Event ->
                    state.loadingState = LoadingState.Finished
                    // Try to read title (only works same-origin)
                    try {
                        state.pageTitle = contentDocument?.title
                        state.lastLoadedUrl = contentWindow?.location?.href
                    } catch (_: Throwable) {
                        // Cross-origin: cannot access contentDocument/contentWindow.location
                    }
                    navigator.canGoBack = false
                    navigator.canGoForward = false
                })

                addEventListener("error", { _: Event ->
                    state.loadingState = LoadingState.Finished
                    state.errorsForCurrentRequest += WebViewError(
                        code = -1,
                        description = "Failed to load content in iframe"
                    )
                })

                state.iframe = this
                onCreated()
            }
        },
        modifier = modifier,
        update = { iframeEl ->
            iframeEl.applySettings(state.settings)
        },
    )
}

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
@Stable
actual class WebViewState actual constructor(webContent: WebContent) {
    actual var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    /**
     *  The content being loaded by the WebView
     */
    actual var content: WebContent by mutableStateOf(webContent)

    /**
     * Whether the WebView is currently [LoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    actual var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the webview is currently loading data in its main frame
     */
    actual val isLoading: Boolean
        get() = loadingState !is LoadingState.Finished

    /**
     * The title received from the loaded content of the current page
     */
    actual var pageTitle: String? by mutableStateOf(null)
        internal set

    actual val settings: WebSettings = WebSettings(
        onSettingsChanged = {
            iframe?.applySettings(settings)
        }
    )

    actual val cookieManager: CookieManager = CookieManager()

    actual var errorsForCurrentRequest: List<WebViewError> by mutableStateOf(emptyList())
        internal set

    actual fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        try {
            val result = iframe?.contentWindow?.evaluateScript(script)
            callback?.invoke(result?.toString())
        } catch (e: Throwable) {
            // Cross-origin: cannot access contentWindow
            callback?.invoke(null)
        }
    }

    /**
     * The underlying iframe element. Access this for additional configuration if needed.
     */
    var iframe by mutableStateOf<HTMLIFrameElement?>(null)
        internal set
}

private fun HTMLIFrameElement.applySettings(webSettings: WebSettings) {
    if (!webSettings.javaScriptEnabled) {
        // Use sandbox to disable scripts; allow everything else
        setAttribute("sandbox", "allow-forms allow-popups allow-same-origin")
    } else {
        // No sandbox — iframe has full capabilities
        removeAttribute("sandbox")
    }
}

// Use Dispatchers.Main to ensure that the webview methods are called on UI thread
internal suspend fun WebViewNavigator.handleNavigationEvents(
    iframe: HTMLIFrameElement,
): Nothing = withContext(Dispatchers.Main) {
    navigationEvents.collect { event ->
        when (event) {
            is WebViewNavigator.NavigationEvent.Back -> {
                try {
                    iframe.contentWindow?.history?.back()
                } catch (_: Throwable) {
                    // Cross-origin restriction
                }
            }

            is WebViewNavigator.NavigationEvent.Forward -> {
                try {
                    iframe.contentWindow?.history?.forward()
                } catch (_: Throwable) {
                    // Cross-origin restriction
                }
            }

            is WebViewNavigator.NavigationEvent.Reload -> {
                try {
                    iframe.contentWindow?.location?.reload()
                } catch (_: Throwable) {
                    // Cross-origin: fall back to resetting src
                    val currentSrc = iframe.src
                    iframe.src = ""
                    iframe.src = currentSrc
                }
            }

            is WebViewNavigator.NavigationEvent.StopLoading -> {
                try {
                    iframe.contentWindow?.stop()
                } catch (_: Throwable) {
                    // Cross-origin restriction
                }
            }

            is WebViewNavigator.NavigationEvent.LoadUrl -> {
                iframe.removeAttribute("srcdoc")
                iframe.src = event.url
            }

            is WebViewNavigator.NavigationEvent.LoadHtml -> {
                iframe.srcdoc = event.html
            }
        }
    }
}

actual val WebStateSaver: Saver<WebViewState, Any> = run {
    val pageTitleKey = "pagetitle"
    val lastLoadedUrlKey = "lastloaded"

    mapSaver(
        save = {
            mapOf(
                pageTitleKey to it.pageTitle,
                lastLoadedUrlKey to it.lastLoadedUrl,
            )
        },
        restore = {
            WebViewState(WebContent.NavigatorOnly).apply {
                this.pageTitle = it[pageTitleKey] as String?
                this.lastLoadedUrl = it[lastLoadedUrlKey] as String?
            }
        }
    )
}
