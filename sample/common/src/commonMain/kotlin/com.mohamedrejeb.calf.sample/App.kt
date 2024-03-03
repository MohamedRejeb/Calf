package com.mohamedrejeb.calf.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.navigation.rememberNavController
import com.mohamedrejeb.calf.sample.navigation.AppNavGraph
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme

@Composable
fun App() =
    CalfTheme {
        val navController = rememberNavController()

        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            AppNavGraph(navController = navController)
        }
    }
