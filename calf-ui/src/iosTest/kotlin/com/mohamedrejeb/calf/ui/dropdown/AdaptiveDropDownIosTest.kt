package com.mohamedrejeb.calf.ui.dropdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import platform.UIKit.UIViewController
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCalfUiApi::class)
class AdaptiveDropDownIosTest {

    @Test
    fun `swiping pages with lists of different lengths does not crash`() = runComposeUiTest {
        val pageCount = 10

        setContent {
            CompositionLocalProvider(LocalUIViewController provides UIViewController()) {
                HorizontalPager(
                    state = rememberPagerState { pageCount },
                    modifier = Modifier.fillMaxSize().testTag("pager"),
                ) { page ->
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(30 - page * 3) { index ->
                            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                                Text("Page $page, item $index")
                                AdaptiveDropDown(
                                    expanded = false,
                                    onDismissRequest = {},
                                    iosItems = emptyList(),
                                    materialContent = {},
                                )
                            }
                        }
                    }
                }
            }
        }

        repeat(pageCount - 1) {
            onNodeWithTag("pager").performTouchInput { swipeLeft() }
            waitForIdle()
        }
        repeat(pageCount - 1) {
            onNodeWithTag("pager").performTouchInput { swipeRight() }
            waitForIdle()
        }
    }
}
