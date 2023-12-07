package com.mohamedrejeb.calf.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.web.WebView
import com.mohamedrejeb.calf.web.rememberWebViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    navigateBack: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navigateBack() }
                    ) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                },
                title = {
                    Text(
                        text = Screen.WebView.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val state = rememberWebViewState(
                url = "https://github.com/MohamedRejeb"
            )

            LaunchedEffect(state.isLoading) {
                if (state.isLoading) return@LaunchedEffect

                state.settings.apply {
                    javaScriptEnabled = true
                    androidSettings.supportZoom = true
                }

                state.evaluateJavascript(
                    """
                    "Hello World!";
                """.trimIndent()
                ) {
                    println("JS Response: $it")
                }
            }

            WebView(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
            )

            if (state.isLoading)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
        }
    }
}