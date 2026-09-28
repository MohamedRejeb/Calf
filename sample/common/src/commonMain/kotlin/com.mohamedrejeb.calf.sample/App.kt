package com.mohamedrejeb.calf.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.sample.navigation.AppNavGraph
import com.mohamedrejeb.calf.sample.navigation.ComposeNavGraph
import com.mohamedrejeb.calf.sample.navigation.SampleNavigation
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme

@Composable
fun App() =
    CalfTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            // Two separate shells, switched from the home screen; they are never nested.
            var navigation by rememberSaveable { mutableStateOf(SampleNavigation.Native) }
            when (navigation) {
                SampleNavigation.Native -> AppNavGraph(onNavigationChange = { navigation = it })
                SampleNavigation.Compose -> ComposeNavGraph(onNavigationChange = { navigation = it })
            }
        }
    }
