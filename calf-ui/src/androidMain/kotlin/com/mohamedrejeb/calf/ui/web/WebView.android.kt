package com.mohamedrejeb.calf.ui.web

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val client = remember { AccompanistWebViewClient() }
    val chromeClient = remember { AccompanistWebChromeClient() }

    BoxWithConstraints(modifier) {
        // WebView changes it's layout strategy based on
        // it's layoutParams. We convert from Compose Modifier to
        // layout params here.
        val width =
            if (constraints.hasFixedWidth)
                LayoutParams.MATCH_PARENT
            else
                LayoutParams.WRAP_CONTENT
        val height =
            if (constraints.hasFixedHeight)
                LayoutParams.MATCH_PARENT
            else
                LayoutParams.WRAP_CONTENT

        val layoutParams = FrameLayout.LayoutParams(
            width,
            height
        )

        WebView(
            state,
            layoutParams,
            Modifier,
            captureBackPresses,
            navigator,
            {
                it.settings.standardFontFamily = "sans-serif"
                it.settings.defaultFontSize = 16
                it.settings.minimumFontSize = 8
                it.settings.minimumLogicalFontSize = 8
                it.settings.textZoom = 100
                it.settings.allowContentAccess = true
                it.settings.allowFileAccess = true
                it.settings.blockNetworkImage = false
                it.settings.blockNetworkLoads = false
                it.settings.databaseEnabled = true
                it.settings.domStorageEnabled = true
                it.settings.loadsImagesAutomatically = true
                it.settings.useWideViewPort = true
                it.settings.loadWithOverviewMode = true
                it.settings.javaScriptCanOpenWindowsAutomatically = true
                it.settings.mediaPlaybackRequiresUserGesture = false
                it.settings.setSupportMultipleWindows(true)
                it.settings.setSupportZoom(true)
                it.settings.builtInZoomControls = true
                it.settings.displayZoomControls = false
                it.settings.setGeolocationEnabled(true)
                onCreated() },
            { onDispose() },
            client,
            chromeClient,
            null
        )
    }
}

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
 * @param layoutParams A FrameLayout.LayoutParams object to custom size the underlying WebView.
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
 */
@Composable
internal fun WebView(
    state: WebViewState,
    layoutParams: FrameLayout.LayoutParams,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    val webView = state.webView

    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            navigator.handleNavigationEvents(wv)
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.content }.collect { content ->
                when (content) {
                    is WebContent.Url -> {
                        wv.loadUrl(content.url, content.additionalHttpHeaders)
                    }

                    is WebContent.Data -> {
                        wv.loadDataWithBaseURL(
                            content.baseUrl,
                            content.data,
                            content.mimeType,
                            content.encoding,
                            content.historyUrl
                        )
                    }

                    is WebContent.NavigatorOnly -> {
                        // NO-OP
                    }

                    else -> {}
                }
            }
        }
    }

    // Set the state of the client and chrome client
    // This is done internally to ensure they always are the same instance as the
    // parent Web composable
    client.state = state
    client.navigator = navigator
    chromeClient.state = state

    AndroidView(
        factory = { context ->
            (factory?.invoke(context) ?: WebView(context)).apply {
                onCreated(this)

                applySettings(state.settings)

                this.layoutParams = layoutParams

                state.viewState?.let {
                    this.restoreState(it)
                }

                webChromeClient = chromeClient
                webViewClient = client
            }.also { state.webView = it }
        },
        modifier = modifier,
        onRelease = {
            onDispose(it)
        }
    )
}

/**
 * Applies the settings from the WebSettings object to the WebView
 */
private fun WebView.applySettings(webSettings: WebSettings) {
    settings.javaScriptEnabled = webSettings.javaScriptEnabled
    settings.javaScriptCanOpenWindowsAutomatically = webSettings.javaScriptCanOpenWindowsAutomatically
    settings.allowFileAccess = webSettings.androidSettings.allowFileAccess
    settings.allowContentAccess = webSettings.androidSettings.allowContentAccess
    settings.blockNetworkImage = webSettings.androidSettings.blockNetworkImage
    settings.blockNetworkLoads = webSettings.androidSettings.blockNetworkLoads
    settings.databaseEnabled = webSettings.androidSettings.databaseEnabled
    settings.domStorageEnabled = webSettings.androidSettings.domStorageEnabled
    settings.loadsImagesAutomatically = webSettings.androidSettings.loadsImagesAutomatically
    settings.useWideViewPort = webSettings.androidSettings.useWideViewPort
    settings.loadWithOverviewMode = webSettings.androidSettings.loadWithOverviewMode
    settings.mediaPlaybackRequiresUserGesture = webSettings.androidSettings.mediaPlaybackRequiresUserGesture
    settings.setSupportMultipleWindows(webSettings.androidSettings.supportMultipleWindows)
    settings.setSupportZoom(webSettings.androidSettings.supportZoom)
    settings.displayZoomControls = webSettings.androidSettings.displayZoomControls
    settings.setGeolocationEnabled(webSettings.androidSettings.setGeolocationEnabled)
    settings.textZoom = webSettings.androidSettings.textZoom
    settings.builtInZoomControls = webSettings.androidSettings.builtInZoomControls
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        settings.safeBrowsingEnabled = webSettings.androidSettings.safeBrowsingEnabled
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        settings.isAlgorithmicDarkeningAllowed = webSettings.androidSettings.isAlgorithmicDarkeningAllowed
    }
}

/**
 * AccompanistWebViewClient
 *
 * A parent class implementation of WebViewClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overriden if further custom behaviour is required.
 */
public open class AccompanistWebViewClient : WebViewClient() {
    public open lateinit var state: WebViewState
        internal set
    public open lateinit var navigator: WebViewNavigator
        internal set

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        state.loadingState = LoadingState.Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageTitle = null
        state.pageIcon = null

        state.lastLoadedUrl = url
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = LoadingState.Finished
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)

        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (error != null) {
            state.errorsForCurrentRequest.add(WebViewError(request, error))
        }
    }
}

/**
 * AccompanistWebChromeClient
 *
 * A parent class implementation of WebChromeClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overriden if further custom behaviour is required.
 */
public open class AccompanistWebChromeClient : WebChromeClient() {
    public open lateinit var state: WebViewState
        internal set

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)
        state.pageTitle = title
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        state.pageIcon = icon
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is LoadingState.Finished) return
        state.loadingState = LoadingState.Loading(newProgress / 100.0f)
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
            applySettings()
        }
    )

    private fun applySettings() {
        webView?.applySettings(settings)
    }

    @JavascriptInterface
    actual fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        webView?.evaluateJavascript(script, callback)
    }

    /**
     * the favicon received from the loaded content of the current page
     */
    public var pageIcon: Bitmap? by mutableStateOf(null)
        internal set

    /**
     * A list for errors captured in the last load. Reset when a new page is loaded.
     * Errors could be from any resource (iframe, image, etc.), not just for the main page.
     * For more fine grained control use the OnError callback of the WebView.
     */
    val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()

    /**
     * The saved view state from when the view was destroyed last. To restore state,
     * use the navigator and only call loadUrl if the bundle is null.
     * See WebViewSaveStateSample.
     */
    public var viewState: Bundle? = null
        internal set

    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
    var webView by mutableStateOf<WebView?>(null)
        internal set
}

// Use Dispatchers.Main to ensure that the webview methods are called on UI thread
internal suspend fun WebViewNavigator.handleNavigationEvents(
    webView: WebView
): Nothing = withContext(Dispatchers.Main) {
    navigationEvents.collect { event ->
        when (event) {
            is WebViewNavigator.NavigationEvent.Back -> webView.goBack()
            is WebViewNavigator.NavigationEvent.Forward -> webView.goForward()
            is WebViewNavigator.NavigationEvent.Reload -> webView.reload()
            is WebViewNavigator.NavigationEvent.StopLoading -> webView.stopLoading()
            is WebViewNavigator.NavigationEvent.LoadHtml -> webView.loadDataWithBaseURL(
                event.baseUrl,
                event.html,
                event.mimeType,
                event.encoding,
                event.historyUrl
            )

            is WebViewNavigator.NavigationEvent.LoadUrl -> {
                loadUrl(event.url, event.additionalHttpHeaders)
            }
        }
    }
}

/**
 * A wrapper class to hold errors from the WebView.
 */
@Immutable
public data class WebViewError(
    /**
     * The request the error came from.
     */
    val request: WebResourceRequest?,
    /**
     * The error that was reported.
     */
    val error: WebResourceError
)

actual val WebStateSaver: Saver<WebViewState, Any> = run {
    val pageTitleKey = "pagetitle"
    val lastLoadedUrlKey = "lastloaded"
    val stateBundle = "bundle"

    mapSaver(
        save = {
            val viewState = Bundle().apply { it.webView?.saveState(this) }
            mapOf(
                pageTitleKey to it.pageTitle,
                lastLoadedUrlKey to it.lastLoadedUrl,
                stateBundle to viewState
            )
        },
        restore = {
            WebViewState(WebContent.NavigatorOnly).apply {
                this.pageTitle = it[pageTitleKey] as String?
                this.lastLoadedUrl = it[lastLoadedUrlKey] as String?
                this.viewState = it[stateBundle] as Bundle?
            }
        }
    )
}