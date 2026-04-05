package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.toolbar.AdaptiveToolbar
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import com.mohamedrejeb.calf.ui.web.LoadingState
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.rememberWebViewNavigator
import com.mohamedrejeb.calf.ui.web.rememberWebViewState

private val INITIAL_URL =
    if (currentPlatform.isWeb)
        "https://en.wikipedia.org/wiki/Main_Page/"
    else
        "https://github.com/MohamedRejeb"

private val DUMMY_HTML = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Calf WebView Demo</title>
        <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                margin: 0;
                padding: 24px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: #fff;
                min-height: 100vh;
            }
            h1 { margin-top: 0; }
            .card {
                background: rgba(255,255,255,0.15);
                border-radius: 12px;
                padding: 16px;
                margin: 12px 0;
                backdrop-filter: blur(10px);
            }
            a { color: #ffd700; }
            ul { padding-left: 20px; }
            li { margin: 6px 0; }
        </style>
    </head>
    <body>
        <h1>🐄 Calf WebView</h1>
        <div class="card">
            <h2>Hello from loadHtml!</h2>
            <p>This page was loaded using <strong>navigator.loadHtml()</strong> with inline HTML content.</p>
        </div>
        <div class="card">
            <h3>Navigation Test</h3>
            <p>Try using the navigation buttons below:</p>
            <ul>
                <li><strong>Back / Forward</strong> – navigate through history</li>
                <li><strong>Reload</strong> – reload this page</li>
                <li><strong>Stop</strong> – stop loading</li>
                <li><strong>Load URL</strong> – load a remote URL</li>
                <li><strong>Load HTML</strong> – load this page again</li>
            </ul>
        </div>
        <div class="card">
            <p>Visit <a href="https://github.com/nickel-lang/nickel">a link</a> to test in-page navigation.</p>
        </div>
    </body>
    </html>
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCalfUiApi::class)
@Composable
fun WebViewScreen(
    navigateBack: () -> Unit
) {
    val state = rememberWebViewState(url = INITIAL_URL)
    val navigator = rememberWebViewNavigator()

    val iosItems by remember(navigator.canGoBack, navigator.canGoForward) {
        mutableStateOf(
            listOf(
                UIKitUIBarButtonItem.image(
                    image = UIKitImage.SystemName(SFSymbol.chevronLeft),
                    enabled = navigator.canGoBack,
                    onClick = { navigator.navigateBack() },
                ),
                UIKitUIBarButtonItem.image(
                    image = UIKitImage.SystemName(SFSymbol.chevronRight),
                    enabled = navigator.canGoForward,
                    onClick = { navigator.navigateForward() },
                ),
                UIKitUIBarButtonItem.flexibleSpace(),
                UIKitUIBarButtonItem.image(
                    image = UIKitImage.SystemName(SFSymbol.arrowClockwise),
                    onClick = { navigator.reload() },
                ),
                UIKitUIBarButtonItem.flexibleSpace(),
                UIKitUIBarButtonItem.image(
                    image = UIKitImage.SystemName(SFSymbol.xmark),
                    onClick = { navigator.stopLoading() },
                ),
                UIKitUIBarButtonItem.flexibleSpace(),
                UIKitUIBarButtonItem.image(
                    image = UIKitImage.SystemName(SFSymbol.globe),
                    onClick = { navigator.loadUrl(INITIAL_URL) },
                ),
                UIKitUIBarButtonItem.image(
                    image = UIKitImage.SystemName(SFSymbol.chevronLeftForwardslashChevronRight),
                    onClick = { navigator.loadHtml(DUMMY_HTML) },
                ),
            )
        )
    }

    LaunchedEffect(Unit) {
        state.settings.javaScriptEnabled = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    AdaptiveIconButton(onClick = { navigateBack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = state.pageTitle ?: "Web View",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        state.lastLoadedUrl?.let { url ->
                            Text(
                                text = url,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .windowInsetsPadding(WindowInsets.ime)
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            WebView(
                state = state,
                navigator = navigator,
                modifier = Modifier.fillMaxSize(),
            )

            val loadingState = state.loadingState
            if (loadingState is LoadingState.Loading) {
                val progress = loadingState.progress
                if (progress in 0.0f..1.0f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }

            AdaptiveToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                content = {
                    AdaptiveIconButton(
                        onClick = { navigator.navigateBack() },
                        enabled = navigator.canGoBack,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Back")
                    }
                    AdaptiveIconButton(
                        onClick = { navigator.navigateForward() },
                        enabled = navigator.canGoForward,
                    ) {
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
                    AdaptiveIconButton(onClick = { navigator.loadHtml(DUMMY_HTML) }) {
                        Icon(Icons.Filled.Code, contentDescription = "Load HTML")
                    }
                },
                iosItems = iosItems,
            )
        }
    }
}