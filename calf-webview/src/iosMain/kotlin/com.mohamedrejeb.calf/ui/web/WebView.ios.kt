package com.mohamedrejeb.calf.ui.web

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UISemanticContentAttributeForceLeftToRight
import platform.UIKit.UISemanticContentAttributeForceRightToLeft
import com.mohamedrejeb.calf.ui.web.helper.NSKeyValueObservingProtocol
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.setValue
import platform.Foundation.dataUsingEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSError
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.WebKit.*
import platform.darwin.NSObject

/**
 * A wrapper around WKWebView to provide a basic WebView composable for iOS.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back. On iOS this controls the back/forward navigation swipe gesture.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created. The underlying WKWebView can be
 * accessed via [WebViewState.webView] for additional configuration.
 * @param onDispose Called when the WebView is destroyed.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun WebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val webView = state.webView

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            state.navigator = navigator
            navigator.handleNavigationEvents(wv)
        }

        // Observe WKWebView properties using KVO
        DisposableEffect(wv) {
            val observer = WKWebViewObserver(state)
            observer.startObserving(wv)
            onDispose {
                observer.stopObserving(wv)
            }
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.content }.collect { content ->
                when (content) {
                    is WebContent.Url -> {
                        val url = NSURL(string = content.url)
                        val urlRequest = NSMutableURLRequest()
                        urlRequest.setURL(url)
                        content.additionalHttpHeaders.forEach { (key, value) ->
                            urlRequest.setValue(value = value, forHTTPHeaderField = key)
                        }
                        wv.loadRequest(urlRequest)
                        wv.allowsBackForwardNavigationGestures = true
                    }

                    is WebContent.Data -> {
                        val baseUrl = content.baseUrl?.let { NSURL(string = it) }

                        wv.loadHTMLString(
                            content.data,
                            baseUrl
                        )
                    }

                    is WebContent.NavigatorOnly -> {
                        // NO-OP
                    }
                }
            }
        }
    }

    UIKitView(
        factory = {
            WKWebView().apply {
                onCreated()
                setUserInteractionEnabled(true)
                allowsBackForwardNavigationGestures = captureBackPresses
                semanticContentAttribute = when (layoutDirection) {
                    LayoutDirection.Rtl -> UISemanticContentAttributeForceRightToLeft
                    else -> UISemanticContentAttributeForceLeftToRight
                }
                applySettings(state.settings)
                state.navigator = navigator
                state.webView = this
                navigationDelegate = state
            }
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
        ),
        onRelease = {
            onDispose()
            state.webView = null
        },
        modifier = modifier,
    )
}

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
@Stable
actual class WebViewState actual constructor(
    webContent: WebContent
) : NSObject(), WKNavigationDelegateProtocol {
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
            applySetting()
        }
    )

    actual val cookieManager: CookieManager = CookieManager()

    actual var errorsForCurrentRequest: List<WebViewError> by mutableStateOf(emptyList())
        internal set

    private fun applySetting() {
        webView?.applySettings(settings)
    }

    actual fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        webView?.evaluateJavaScript(script) { result, error ->
            if (error != null) {
                callback?.invoke(error.localizedDescription())
            } else {
                callback?.invoke(result?.toString())
            }
        }
    }

    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
    var webView by mutableStateOf<WKWebView?>(null)
        internal set

    // Navigator reference for updating canGoBack/canGoForward
    internal var navigator: WebViewNavigator? = null

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        loadingState = LoadingState.Finished
        pageTitle = webView.title
        navigator?.canGoBack = webView.canGoBack
        navigator?.canGoForward = webView.canGoForward
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didCommitNavigation: WKNavigation?) {
        loadingState = LoadingState.Loading(webView.estimatedProgress.toFloat())
        lastLoadedUrl = webView.URL?.absoluteString
        errorsForCurrentRequest = emptyList()
        pageTitle = webView.title
        navigator?.canGoBack = webView.canGoBack
        navigator?.canGoForward = webView.canGoForward
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) {
        loadingState = LoadingState.Finished
        errorsForCurrentRequest = errorsForCurrentRequest + WebViewError(
            code = withError.code.toInt(),
            description = withError.localizedDescription
        )
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError
    ) {
        loadingState = LoadingState.Finished
        errorsForCurrentRequest = errorsForCurrentRequest + WebViewError(
            code = withError.code.toInt(),
            description = withError.localizedDescription
        )
    }
}

private fun WKWebView.applySettings(webSettings: WebSettings) {
    configuration.defaultWebpagePreferences.allowsContentJavaScript = webSettings.javaScriptEnabled
    configuration.preferences.javaScriptEnabled = webSettings.javaScriptEnabled
    configuration.preferences.javaScriptCanOpenWindowsAutomatically =
        webSettings.javaScriptCanOpenWindowsAutomatically
    allowsBackForwardNavigationGestures =
        webSettings.iosSettings.allowsBackForwardNavigationGestures
    configuration.allowsInlineMediaPlayback = webSettings.iosSettings.allowsInlineMediaPlayback
}

// Use Dispatchers.Main to ensure that the webview methods are called on UI thread
@OptIn(BetaInteropApi::class)
internal suspend fun WebViewNavigator.handleNavigationEvents(
    webView: WKWebView
): Nothing = withContext(Dispatchers.Main) {
    navigationEvents.collect { event ->
        when (event) {
            is WebViewNavigator.NavigationEvent.Back -> webView.goBack()
            is WebViewNavigator.NavigationEvent.Forward -> webView.goForward()
            is WebViewNavigator.NavigationEvent.Reload -> webView.reload()
            is WebViewNavigator.NavigationEvent.StopLoading -> webView.stopLoading()
            is WebViewNavigator.NavigationEvent.LoadHtml -> {
                val data = NSString
                    .create(string = event.html)
                    .dataUsingEncoding(NSUTF8StringEncoding) ?: return@collect
                val baseUrl = event.baseUrl?.let { NSURL(string = it) }

                if (baseUrl != null) {
                    webView.loadData(
                        data,
                        event.mimeType ?: "text/html",
                        event.encoding ?: "utf-8",
                        baseUrl
                    )
                } else {
                    webView.loadHTMLString(
                        event.html,
                        null
                    )
                }
            }

            is WebViewNavigator.NavigationEvent.LoadUrl -> {
                val url = NSURL(string = event.url)
                val urlRequest = NSMutableURLRequest().apply {
                    setURL(url)
                    event.additionalHttpHeaders.forEach { (key, value) ->
                        setValue(value = value, forHTTPHeaderField = key)
                    }
                }
                webView.loadRequest(urlRequest)
            }
        }
    }
}

/**
 * KVO observer for WKWebView properties.
 * Observes estimatedProgress, title, URL, canGoBack, and canGoForward.
 */
@OptIn(ExperimentalForeignApi::class)
private class WKWebViewObserver(
    private val state: WebViewState,
) : NSObject(), NSKeyValueObservingProtocol {

    private val observedKeyPaths = listOf(
        "estimatedProgress",
        "loading",
        "title",
        "URL",
        "canGoBack",
        "canGoForward",
    )

    fun startObserving(webView: WKWebView) {
        observedKeyPaths.forEach { keyPath ->
            webView.addObserver(
                observer = this,
                forKeyPath = keyPath,
                options = NSKeyValueObservingOptionNew,
                context = null,
            )
        }
    }

    fun stopObserving(webView: WKWebView) {
        observedKeyPaths.forEach { keyPath ->
            webView.removeObserver(
                observer = this,
                forKeyPath = keyPath,
            )
        }
    }

    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: COpaquePointer?,
    ) {
        val webView = ofObject as? WKWebView ?: return

        when (keyPath) {
            "estimatedProgress" -> {
                val progress = webView.estimatedProgress.toFloat()
                if (state.isLoading) {
                    state.loadingState = LoadingState.Loading(progress)
                }
            }

            "loading" -> {
                if (webView.loading) {
                    state.loadingState = LoadingState.Loading(webView.estimatedProgress.toFloat())
                } else {
                    state.loadingState = LoadingState.Finished
                }
            }

            "title" -> {
                state.pageTitle = webView.title
            }

            "URL" -> {
                state.lastLoadedUrl = webView.URL?.absoluteString
            }

            "canGoBack" -> {
                state.navigator?.canGoBack = webView.canGoBack
            }

            "canGoForward" -> {
                state.navigator?.canGoForward = webView.canGoForward
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