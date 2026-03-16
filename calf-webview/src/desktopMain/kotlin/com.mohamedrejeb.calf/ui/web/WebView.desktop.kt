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
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.beans.value.ChangeListener
import javafx.concurrent.Worker
import javafx.scene.web.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A wrapper around the JavaFX [javafx.scene.web.WebView] to provide a basic WebView composable
 * for Desktop (JVM).
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Not applicable on Desktop. Reserved for API consistency.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created. The underlying JavaFX WebView
 * can be accessed via [WebViewState.webView] for additional configuration.
 * @param onDispose Called when the WebView is destroyed.
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

    DisposableEffect(Unit) {
        Platform.setImplicitExit(false)
        Platform.runLater {
            val wv = WebView().apply {
                applySettings(state.settings)
            }
            val engine = wv.engine

            engine.loadWorker.stateProperty().addListener(ChangeListener { _, _, newState ->
                when (newState) {
                    Worker.State.RUNNING -> {
                        state.loadingState = LoadingState.Loading(0f)
                        state.pageTitle = null
                        state.lastLoadedUrl = engine.location
                    }
                    Worker.State.SUCCEEDED -> {
                        state.loadingState = LoadingState.Finished
                        state.pageTitle = engine.title
                        state.lastLoadedUrl = engine.location
                        navigator.canGoBack = engine.history.currentIndex > 0
                        navigator.canGoForward = engine.history.currentIndex < engine.history.entries.size - 1
                    }
                    Worker.State.FAILED -> {
                        state.loadingState = LoadingState.Finished
                        val exception = engine.loadWorker.exception
                        if (exception != null) {
                            state.errorsForCurrentRequest = listOf(
                                WebViewError(
                                    code = -1,
                                    description = exception.message ?: "Unknown error"
                                )
                            )
                        }
                    }
                    Worker.State.CANCELLED -> {
                        state.loadingState = LoadingState.Finished
                    }
                    else -> {}
                }
            })

            engine.loadWorker.progressProperty().addListener(ChangeListener { _, _, newValue ->
                if (state.loadingState is LoadingState.Loading) {
                    val progress = newValue.toFloat().coerceIn(0f, 1f)
                    state.loadingState = LoadingState.Loading(progress)
                }
            })

            jfxPanel.scene = Scene(wv)
            state.webView = wv
            onCreated()
        }

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
                }
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

    // TODO: Cookie manager is not working properly on Desktop, we need to check it
    // https://stackoverflow.com/questions/14385233/setting-a-cookie-using-javafxs-webengine-webview
    actual val cookieManager: CookieManager = CookieManager()

    actual var errorsForCurrentRequest: List<WebViewError> by mutableStateOf(emptyList())
        internal set

    private fun applySetting() {
        webView?.applySettings(settings)
    }

    actual fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        Platform.runLater {
            val result = webView?.engine?.executeScript(script)
            callback?.invoke(result?.toString())
        }
    }

    var webView by mutableStateOf<WebView?>(null)
        internal set
}

private fun WebView.applySettings(webSettings: WebSettings) {
    Platform.runLater {
        engine.isJavaScriptEnabled = webSettings.javaScriptEnabled
        webSettings.desktopSettings.customUserAgent?.let {
            engine.userAgent = it
        }
    }
}

// Use Dispatchers.Main to ensure that the webview methods are called on UI thread
internal suspend fun WebViewNavigator.handleNavigationEvents(
    webView: WebView
) {
    navigationEvents.collect { event ->
        Platform.runLater {
            with(webView.engine) {
                when (event) {
                    is WebViewNavigator.NavigationEvent.Back ->
                        if (history.currentIndex > 0) history.go(-1)
                    is WebViewNavigator.NavigationEvent.Forward ->
                        if (history.currentIndex < history.entries.size - 1) history.go(1)
                    is WebViewNavigator.NavigationEvent.Reload ->
                        reload()
                    is WebViewNavigator.NavigationEvent.StopLoading ->
                        stopLoading()
                    is WebViewNavigator.NavigationEvent.LoadHtml ->
                        loadContent(event.html, event.mimeType)
                    is WebViewNavigator.NavigationEvent.LoadUrl -> {
                        this.load(event.url)
                    }
                }
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