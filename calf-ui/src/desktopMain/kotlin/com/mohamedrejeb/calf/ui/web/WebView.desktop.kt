package com.mohamedrejeb.calf.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.web.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param factory An optional WebView factory for using a custom subclass of WebView
 * @sample com.google.accompanist.sample.webview.BasicWebViewSample
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
    val jfxPanel = remember { JFXPanel() }

    LaunchedEffect(Unit) {
        Platform.runLater {
            val wv = WebView().apply {
                applySettings(state.settings)
            }
            jfxPanel.scene = Scene(wv)
            state.webView = wv
            onCreated()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    val webView = state.webView

    webView?.let { wv ->
        LaunchedEffect(navigator, wv) {
            navigator.handleNavigationEvents(wv)
        }

        LaunchedEffect(state, wv) {
            snapshotFlow { state.content }.collect { content ->
                when (content) {
                    is WebContent.Url -> {
                        Platform.runLater {
                            wv.engine.load(content.url)
                        }
                    }

                    is WebContent.Data -> {
                        Platform.runLater {
                            if (content.mimeType.isNullOrBlank())
                                wv.engine.loadContent(content.data)
                            else
                                wv.engine.loadContent(content.data, content.mimeType)
                        }
                    }

                    is WebContent.NavigatorOnly -> {
                        // NO-OP
                    }

                    else -> {}
                }
            }
        }

        SwingPanel(
            factory = {
                jfxPanel
            },
            update = {

            },
            modifier = modifier
        )
    }
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
     * Whether the WebView is currently [LoadingState.Loading] data in its desktopMain frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    actual var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the webview is currently loading data in its desktopMain frame
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
            applySetting()
        }
    )

    private fun applySetting() {
        webView?.applySettings(settings)
    }

    actual fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        Platform.runLater {
            webView?.engine?.executeScript(script).let {
                callback?.invoke(it?.toString())
            }
        }
    }

    var webView by mutableStateOf<WebView?>(null)
        internal set
}

private fun WebView.applySettings(webSettings: WebSettings) {
    engine.isJavaScriptEnabled = webSettings.javaScriptEnabled
}

// Use Dispatchers.Main to ensure that the webview methods are called on UI thread
internal suspend fun WebViewNavigator.handleNavigationEvents(
    webView: WebView
): Nothing = withContext(Dispatchers.Main) {
    navigationEvents.collect { event ->
        with(webView.engine) {
            when (event) {
                is WebViewNavigator.NavigationEvent.Back ->
                    if (history.currentIndex > 0) history.go(-1)
                is WebViewNavigator.NavigationEvent.Forward ->
                    if (history.currentIndex < history.maxSize - 1) history.go(1)
                is WebViewNavigator.NavigationEvent.Reload ->
                    reload()
                is WebViewNavigator.NavigationEvent.StopLoading ->
                    stopLoading()
                is WebViewNavigator.NavigationEvent.LoadHtml ->
                    loadContent(event.html, event.mimeType)
                is WebViewNavigator.NavigationEvent.LoadUrl ->
                    loadUrl(event.url, event.additionalHttpHeaders)
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