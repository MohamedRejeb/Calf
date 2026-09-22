@file:OptIn(
    com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi::class,
    com.mohamedrejeb.calf.core.CalfIosOnlyApi::class,
)

package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptiveNavDisplay
import com.mohamedrejeb.calf.navigation.AdaptivePaneRole
import com.mohamedrejeb.calf.navigation.IosSplitViewConfiguration
import com.mohamedrejeb.calf.navigation.IosSplitViewDisplayMode
import com.mohamedrejeb.calf.navigation.adaptiveNavEntry
import com.mohamedrejeb.calf.navigation.rememberAdaptiveListDetailSettings
import com.mohamedrejeb.calf.navigation.rememberAdaptiveNavBackStack
import com.mohamedrejeb.calf.navigation.selectDetail
import com.mohamedrejeb.calf.sample.components.LocalSampleEntryPadding
import com.mohamedrejeb.calf.sample.screens.AdaptiveClickableScreen
import com.mohamedrejeb.calf.sample.screens.AdaptiveNavigationDemoScreen
import com.mohamedrejeb.calf.sample.screens.AlertDialogScreen
import com.mohamedrejeb.calf.sample.screens.BottomSheetScreen
import com.mohamedrejeb.calf.sample.screens.ButtonScreen
import com.mohamedrejeb.calf.sample.screens.CameraPickerScreen
import com.mohamedrejeb.calf.sample.screens.CupertinoIconsCatalogScreen
import com.mohamedrejeb.calf.sample.screens.DatePickerScreen
import com.mohamedrejeb.calf.sample.screens.DropDownScreen
import com.mohamedrejeb.calf.sample.screens.FilePickerScreen
import com.mohamedrejeb.calf.sample.screens.FileSaverScreen
import com.mohamedrejeb.calf.sample.screens.FoldableScreen
import com.mohamedrejeb.calf.sample.screens.HomeDetailPlaceholder
import com.mohamedrejeb.calf.sample.screens.HomeScreen
import com.mohamedrejeb.calf.sample.screens.ImagePickerScreen
import com.mohamedrejeb.calf.sample.screens.NavigationBarScreen
import com.mohamedrejeb.calf.sample.screens.PermissionScreen
import com.mohamedrejeb.calf.sample.screens.ProgressBarScreen
import com.mohamedrejeb.calf.sample.screens.ScaffoldDemoScreen
import com.mohamedrejeb.calf.sample.screens.ShareScreen
import com.mohamedrejeb.calf.sample.screens.ShowcaseScreen
import com.mohamedrejeb.calf.sample.screens.SliderScreen
import com.mohamedrejeb.calf.sample.screens.SwitchScreen
import com.mohamedrejeb.calf.sample.screens.TimePickerScreen
import com.mohamedrejeb.calf.sample.screens.ToolbarScreen
import com.mohamedrejeb.calf.sample.screens.TopBarDemoScreen
import com.mohamedrejeb.calf.sample.screens.WebViewScreen

private val HomeKey = SampleScreen(Screen.Home)

/**
 * The sample's navigation, in two layers:
 *
 *  - The **outer** display pushes the demos that own the whole screen (their own tabs or their
 *    own navigation display, see [SampleHost.FullScreen]) over everything, full screen.
 *  - Its root entry is the **home list-detail**: the component list beside the selected demo when
 *    the window is wide (open iPhone Duo, iPad, desktop, Android tablet), a normal push when it is
 *    narrow. Opening the sample on a foldable is the demo.
 *
 * Both stacks are persisted with [SampleSavedState], so folding an Android device (which
 * recreates the Activity) keeps the selected demo.
 */
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val outerStack = rememberAdaptiveNavBackStack(SampleRoot, configuration = SampleSavedState)

    AdaptiveNavDisplay(
        backStack = outerStack,
        modifier = modifier.fillMaxSize(),
    ) { key ->
        when (key) {
            is SampleRoot -> adaptiveNavEntry(key = key, barShown = false) {
                HomeListDetail(openFullScreen = { screen -> outerStack.add(SampleScreen(screen)) })
            }

            is SampleScreen -> adaptiveNavEntry(key = key, barShown = false) {
                DemoScreen(screen = key.screen, navigateBack = { outerStack.removeLastOrNull() })
            }

            else -> error("Unexpected outer key: $key")
        }
    }
}

@Composable
private fun HomeListDetail(openFullScreen: (Screen) -> Unit) {
    val stack = rememberAdaptiveNavBackStack(HomeKey, configuration = SampleSavedState)
    // Both columns stay visible on a portrait iPad instead of the list hiding behind a bar button.
    val listDetail = rememberAdaptiveListDetailSettings(
        iosSplitView = remember {
            IosSplitViewConfiguration(preferredDisplayMode = IosSplitViewDisplayMode.OneBesideSecondary)
        },
    )

    AdaptiveNavDisplay(
        backStack = stack,
        listDetail = listDetail,
        modifier = Modifier.fillMaxSize(),
    ) { key ->
        val screen = (key as? SampleScreen)?.screen ?: error("Unexpected home key: $key")
        homeEntry(
            key = key,
            screen = screen,
            stack = stack,
            openFullScreen = openFullScreen,
        )
    }
}

private fun homeEntry(
    key: SampleScreen,
    screen: Screen,
    stack: NavBackStack<NavKey>,
    openFullScreen: (Screen) -> Unit,
): NavEntry<NavKey> =
    when (screen.host()) {
        SampleHost.List -> adaptiveNavEntry(
            key = key,
            barShown = false,
            paneRole = AdaptivePaneRole.List(detailPlaceholder = { HomeDetailPlaceholder() }),
        ) { padding ->
            HomeScreen(
                contentPadding = padding,
                // Read in the composition, so the highlight follows the stack on every platform.
                selectedScreen = stack.selectedDemo(),
                navigate = { target ->
                    if (target.host().isDetail) {
                        stack.selectDetail(list = HomeKey, detail = SampleScreen(target))
                    } else {
                        openFullScreen(target)
                    }
                },
            )
        }

        SampleHost.Detail -> adaptiveNavEntry(
            key = key,
            title = { Text(screen.title) },
            navigationIcon = {
                IconButton(onClick = { stack.removeLastOrNull() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            iosTitle = screen.title,
            paneRole = AdaptivePaneRole.Detail,
        ) { padding ->
            // The navigator paints the bar; SampleScreenScaffold renders only the body.
            CompositionLocalProvider(LocalSampleEntryPadding provides padding) {
                DemoScreen(screen = screen, navigateBack = { stack.removeLastOrNull() })
            }
        }

        SampleHost.DetailOwnBar -> adaptiveNavEntry(
            key = key,
            barShown = false,
            paneRole = AdaptivePaneRole.Detail,
        ) {
            DemoScreen(screen = screen, navigateBack = { stack.removeLastOrNull() })
        }

        SampleHost.FullScreen -> error("$screen is pushed from the outer display")
    }

/** The demo open in the detail pane, or `null` when the placeholder is showing. */
private fun NavBackStack<NavKey>.selectedDemo(): Screen? =
    filterIsInstance<SampleScreen>().lastOrNull { it.screen != Screen.Home }?.screen

/** The demo for a [Screen]. Every entry of the enum except [Screen.Home] has one. */
@Composable
private fun DemoScreen(screen: Screen, navigateBack: () -> Unit) {
    when (screen) {
        Screen.Home -> error("Home is the list, not a demo")
        Screen.Showcase -> ShowcaseScreen(navigateBack = navigateBack)
        Screen.Dialog -> AlertDialogScreen(navigateBack = navigateBack)
        Screen.BottomSheet -> BottomSheetScreen(navigateBack = navigateBack)
        Screen.ProgressBar -> ProgressBarScreen(navigateBack = navigateBack)
        Screen.Switch -> SwitchScreen(navigateBack = navigateBack)
        Screen.Slider -> SliderScreen(navigateBack = navigateBack)
        Screen.Button -> ButtonScreen(navigateBack = navigateBack)
        Screen.AdaptiveClickable -> AdaptiveClickableScreen(navigateBack = navigateBack)
        Screen.DropDown -> DropDownScreen(navigateBack = navigateBack)
        Screen.DatePicker -> DatePickerScreen(navigateBack = navigateBack)
        Screen.TimePicker -> TimePickerScreen(navigateBack = navigateBack)
        Screen.FilePicker -> FilePickerScreen(navigateBack = navigateBack)
        Screen.ImagePicker -> ImagePickerScreen(navigateBack = navigateBack)
        Screen.ProfilePicture -> FileSaverScreen(navigateBack = navigateBack)
        Screen.CameraPickerScreen -> CameraPickerScreen(navigateBack = navigateBack)
        Screen.CupertinoIconsCatalog -> CupertinoIconsCatalogScreen(navigateBack = navigateBack)
        Screen.Share -> ShareScreen(navigateBack = navigateBack)
        Screen.Foldable -> FoldableScreen(navigateBack = navigateBack)
        Screen.WebView -> WebViewScreen(navigateBack = navigateBack)
        Screen.Permission -> PermissionScreen(navigateBack = navigateBack)
        Screen.NavigationBar -> NavigationBarScreen(navigateBack = navigateBack)
        Screen.ScaffoldDemo -> ScaffoldDemoScreen(navigateBack = navigateBack)
        Screen.TopBarDemo -> TopBarDemoScreen(navigateBack = navigateBack)
        Screen.Toolbar -> ToolbarScreen(navigateBack = navigateBack)
        Screen.AdaptiveNavigation -> AdaptiveNavigationDemoScreen(navigateBack = navigateBack)
    }
}
