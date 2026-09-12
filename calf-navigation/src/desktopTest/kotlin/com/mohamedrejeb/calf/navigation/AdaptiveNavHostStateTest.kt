package com.mohamedrejeb.calf.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

private const val HOME = "home"
private const val DETAIL = "detail"

@OptIn(ExperimentalTestApi::class)
class AdaptiveNavHostStateTest {


    @Test
    fun `saveable state of a destination survives navigating away and back`() = runComposeUiTest {
        lateinit var navController: AdaptiveNavHostController
        setContent {
            navController = rememberNavController()
            AdaptiveNavHost(navController = navController, startDestination = HOME) {
                composable(HOME) { Counter(label = HOME) }
                composable(DETAIL) { Text(DETAIL) }
            }
        }
        waitForIdle()

        onNodeWithText("$HOME 0").performClick()
        onNodeWithText("$HOME 1").assertIsDisplayed()

        navController.navigate(DETAIL)
        waitForIdle()
        onNodeWithText(DETAIL).assertIsDisplayed()

        navController.popBackStack()
        waitForIdle()

        onNodeWithText("$HOME 1").assertIsDisplayed()
    }

    @Test
    fun `saveable state of a popped destination is dropped`() = runComposeUiTest {
        lateinit var navController: AdaptiveNavHostController
        setContent {
            navController = rememberNavController()
            AdaptiveNavHost(navController = navController, startDestination = HOME) {
                composable(HOME) { Text(HOME) }
                composable(DETAIL) { Counter(label = DETAIL) }
            }
        }
        waitForIdle()

        navController.navigate(DETAIL)
        waitForIdle()
        onNodeWithText("$DETAIL 0").performClick()
        onNodeWithText("$DETAIL 1").assertIsDisplayed()

        navController.popBackStack()
        waitForIdle()
        navController.navigate(DETAIL)
        waitForIdle()

        onNodeWithText("$DETAIL 0").assertIsDisplayed()
    }
}

@androidx.compose.runtime.Composable
private fun Counter(label: String) {
    var count by rememberSaveable { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("$label $count")
    }
}
