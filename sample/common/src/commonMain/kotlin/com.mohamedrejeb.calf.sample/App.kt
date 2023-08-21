package com.mohamedrejeb.calf.sample

import androidx.compose.runtime.*
import com.mohamedrejeb.calf.navigation.rememberNavController
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme

@Composable
fun App() = CalfTheme {
    val navController = rememberNavController()

    AppNavGraph(navController = navController)
}