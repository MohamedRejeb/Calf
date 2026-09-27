package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.toolbar.AdaptiveToolbar
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.ui.web.LoadingState
import com.mohamedrejeb.calf.ui.web.WebViewNavigator
import com.mohamedrejeb.calf.ui.web.WebViewState
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.rememberWebViewNavigator
import com.mohamedrejeb.calf.ui.web.rememberWebViewState

// Most sites refuse to be framed, and the web actual is an iframe, so it opens a page that allows it.
private val INITIAL_URL =
    if (currentPlatform.isWeb) "https://en.wikipedia.org/wiki/Main_Page/"
    else "https://github.com/MohamedRejeb/Calf"

/** The page of the "Load HTML" button: plain inline HTML that follows the system theme. */
private val INLINE_HTML = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Calf WebView</title>
        <style>
            :root { color-scheme: light dark; }
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                margin: 0;
                padding: 24px 20px 120px;
                background: #F3F3F7;
                color: #16161C;
                line-height: 1.5;
            }
            h1 { font-size: 28px; margin: 8px 0 4px; }
            .lead { color: #5E5E6A; margin: 0 0 20px; }
            .card { background: #FFFFFF; border-radius: 16px; padding: 4px 16px; margin: 0 0 16px; }
            .row { display: flex; justify-content: space-between; padding: 12px 0; border-top: 1px solid #E0E0E8; }
            .row:first-child { border-top: none; }
            .row span:last-child { color: #5E5E6A; }
            code { font-family: ui-monospace, Menlo, monospace; font-size: 14px; }
            a { color: #3F3FCF; }
            @media (prefers-color-scheme: dark) {
                body { background: #0E0E12; color: #E6E5EE; }
                .lead, .row span:last-child { color: #A9A8B6; }
                .card { background: #1B1B21; }
                .row { border-top-color: #2C2C35; }
                a { color: #BDBCFF; }
            }
        </style>
    </head>
    <body>
        <h1>Inline HTML</h1>
        <p class="lead">This page is a string, loaded with <code>navigator.loadHtml()</code>.
        The toolbar below drives the same <code>WebViewNavigator</code>.</p>
        <div class="card">
            <div class="row"><span>Back, Forward</span><span>History</span></div>
            <div class="row"><span>Reload</span><span>Load the page again</span></div>
            <div class="row"><span>Stop</span><span>Cancel loading</span></div>
            <div class="row"><span>Globe</span><span>Load a URL</span></div>
            <div class="row"><span>Code</span><span>Load this HTML</span></div>
        </div>
        <p>Follow <a href="https://github.com/MohamedRejeb/Calf">the Calf repository</a>,
        then come back with the back button.</p>
    </body>
    </html>
""".trimIndent()

/**
 * A small browser: the page title and address in the bar, a floating toolbar for history,
 * reload, stop and the URL or inline HTML content. It draws its own bar, so it applies the
 * system bar and keyboard insets itself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(navigateBack: () -> Unit) {
    val state = rememberWebViewState(url = INITIAL_URL)
    val navigator = rememberWebViewNavigator()

    LaunchedEffect(Unit) {
        state.settings.javaScriptEnabled = true
    }

    Scaffold(
        topBar = { PageTitleBar(state = state, navigateBack = navigateBack) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
        ) {
            WebView(
                state = state,
                navigator = navigator,
                modifier = Modifier.fillMaxSize(),
            )
            LoadingBar(state.loadingState, Modifier.align(Alignment.TopCenter))
            BrowserToolbar(
                navigator = navigator,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.l),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageTitleBar(state: WebViewState, navigateBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            AdaptiveIconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        title = {
            Column {
                Text(
                    text = state.pageTitle ?: Screen.WebView.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                state.lastLoadedUrl?.let { url ->
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

/** A determinate bar while the page reports progress, an indeterminate one otherwise. */
@Composable
private fun LoadingBar(loadingState: LoadingState, modifier: Modifier = Modifier) {
    if (loadingState !is LoadingState.Loading) return
    val progress = loadingState.progress
    if (progress in 0f..1f) {
        LinearProgressIndicator(progress = { progress }, modifier = modifier.fillMaxWidth())
    } else {
        LinearProgressIndicator(modifier = modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
private fun BrowserToolbar(navigator: WebViewNavigator, modifier: Modifier = Modifier) {
    val canGoBack = navigator.canGoBack
    val canGoForward = navigator.canGoForward
    val iosItems = remember(navigator, canGoBack, canGoForward) {
        browserBarItems(navigator, canGoBack, canGoForward)
    }
    AdaptiveToolbar(
        expanded = true,
        modifier = modifier,
        content = {
            AdaptiveIconButton(onClick = { navigator.navigateBack() }, enabled = canGoBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Back")
            }
            AdaptiveIconButton(onClick = { navigator.navigateForward() }, enabled = canGoForward) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Navigate Forward")
            }
            AdaptiveIconButton(onClick = { navigator.reload() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reload")
            }
            AdaptiveIconButton(onClick = { navigator.stopLoading() }) {
                Icon(Icons.Filled.Close, contentDescription = "Stop Loading")
            }
            AdaptiveIconButton(onClick = { navigator.loadUrl(INITIAL_URL) }) {
                Icon(Icons.Filled.Language, contentDescription = "Load URL")
            }
            AdaptiveIconButton(onClick = { navigator.loadHtml(INLINE_HTML) }) {
                Icon(Icons.Filled.Code, contentDescription = "Load HTML")
            }
        },
        iosItems = iosItems,
    )
}

/** The same actions as SF Symbols bar items, for the native toolbar on iOS. */
@OptIn(ExperimentalCalfUiApi::class)
private fun browserBarItems(
    navigator: WebViewNavigator,
    canGoBack: Boolean,
    canGoForward: Boolean,
): List<UIKitUIBarButtonItem> = listOf(
    UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName(SFSymbol.chevronLeft),
        enabled = canGoBack,
        label = "Navigate Back",
        onClick = { navigator.navigateBack() },
    ),
    UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName(SFSymbol.chevronRight),
        enabled = canGoForward,
        label = "Navigate Forward",
        onClick = { navigator.navigateForward() },
    ),
    UIKitUIBarButtonItem.flexibleSpace(),
    UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName(SFSymbol.arrowClockwise),
        label = "Reload",
        onClick = { navigator.reload() },
    ),
    UIKitUIBarButtonItem.flexibleSpace(),
    UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName(SFSymbol.xmark),
        label = "Stop Loading",
        onClick = { navigator.stopLoading() },
    ),
    UIKitUIBarButtonItem.flexibleSpace(),
    UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName(SFSymbol.globe),
        label = "Load URL",
        onClick = { navigator.loadUrl(INITIAL_URL) },
    ),
    UIKitUIBarButtonItem.image(
        image = UIKitImage.SystemName(SFSymbol.chevronLeftForwardslashChevronRight),
        label = "Load HTML",
        onClick = { navigator.loadHtml(INLINE_HTML) },
    ),
)
