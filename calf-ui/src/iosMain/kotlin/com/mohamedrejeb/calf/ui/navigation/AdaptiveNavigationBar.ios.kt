package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.systemBlueColor
import platform.UIKit.systemGreenColor
import platform.UIKit.tabBarItem
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
fun createTabBarContainerView(
    parent: UIViewController
): UITabBar {
    val tabBar = UITabBar()

    tabBar.delegate = object: NSObject(),  UITabBarDelegateProtocol {
        override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
            tabBar.selectedItem = didSelectItem
        }
    }
    tabBar.translatesAutoresizingMaskIntoConstraints = false

    val homeItem = UITabBarItem(
        title = "Home",
        image = UIImage.systemImageNamed("house.fill"),
        tag = 0
    )
    val favoriteItem = UITabBarItem(
        title = "Favorite",
        image = UIImage.systemImageNamed("heart.fill"),
        tag = 1
    )
    val profileItem = UITabBarItem(
        title = "Profile",
        image = UIImage.systemImageNamed("person.circle.fill"),
        tag = 2
    )

    tabBar.setItems(listOf(homeItem, favoriteItem, profileItem))
    tabBar.selectedItem = homeItem

//    NSLayoutConstraint.activateConstraints(listOf(
//        tabBar.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
//    tabBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
//    tabBar.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor) // Pin to safe area bottom
//    ))

//    parent.view.addSubview(tabBar)

    // Return the view of the tab bar controller
    return tabBar
}

/**
 * iOS implementation of [AdaptiveNavigationBar].
 *
 * This implementation uses UIKit's UITabBarController to create a native iOS tab bar.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@ExperimentalCalfUiApi
@Composable
actual fun AdaptiveNavigationBar(
    onItemChanged: (String) -> Unit,
    iosPaddingValues: (PaddingValues) -> Unit,
    iosItems: List<UIKitUITabBarItem>,
    iosSelectedIndex: Int,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    windowInsets: WindowInsets,
    content: @Composable RowScope.() -> Unit
) {
    val density = LocalDensity.current
    val viewController = LocalUIViewController.current
    val tabBarView = remember {
        UITabBar()
    }

    val onItemChangedState by rememberUpdatedState(onItemChanged)

    val tabBarManager = remember(tabBarView) {
        TabBarManager(
            tabBar = tabBarView,
            onItemChanged = {
                onItemChangedState(it)
            },
        )
    }

    LaunchedEffect(iosItems, iosSelectedIndex) {
        tabBarManager.setItems(iosItems, iosSelectedIndex)
    }
    
    var topLeft by remember {
        mutableStateOf(DpOffset.Zero)
    }

    var positionInRoot by remember { 
        mutableStateOf(DpOffset.Zero)
    }

    var tabBarWidth by remember {
        mutableStateOf(0.dp)
    }

    var tabBarHeight by remember {
        mutableStateOf(0.dp)
    }

    DisposableEffect(tabBarView, viewController) {
        viewController.view.addSubview(tabBarView)

        NSLayoutConstraint.activateConstraints(
            listOf(
                tabBarView.leadingAnchor.constraintEqualToAnchor(viewController.view.leadingAnchor),
                tabBarView.trailingAnchor.constraintEqualToAnchor(viewController.view.trailingAnchor),
                tabBarView.bottomAnchor.constraintEqualToAnchor(viewController.view.bottomAnchor),
            )
        )

        onDispose {
            tabBarView.removeFromSuperview()
        }
    }

    LaunchedEffect(Unit) {
        var tabBarHeightConsistencyCounter = 0

        while(true) {
            withFrameMillis {  }
            println("TabBar width: ${tabBarView.frame.useContents { size.width }}")
            println("TabBar height: ${tabBarView.frame.useContents { size.height }}")
            println("Tab bar y: ${tabBarView.frame.useContents { origin.y }}")
            println("viewController.view.height: ${viewController.view.frame.useContents { size.height }}")

            tabBarView.frame.useContents {
                topLeft = DpOffset(
                    x = origin.x.dp,
                    y = origin.y.dp,
                )
                tabBarWidth = size.width.dp
                val newTabBarHeight = size.height.dp
                if (tabBarHeight != newTabBarHeight) {
                    tabBarHeight = newTabBarHeight
                } else {
                    tabBarHeightConsistencyCounter++
                }
            }

            if (tabBarHeight.value > 0f)
                iosPaddingValues(
                    PaddingValues(
                        bottom = tabBarHeight,
                    )
                )

            if (tabBarHeight.value > 0f && tabBarHeightConsistencyCounter > 10)
                break
        }
    }

    Box(
        modifier = Modifier
//            .fillMaxWidth()
            .onPlaced {
                val positionInRootPx = it.positionInRoot()
                positionInRoot = with(density) {
                    DpOffset(
                        x = positionInRootPx.x.toDp(),
                        y = positionInRootPx.y.toDp(),
                    )
                }
            }
            .offset(x = topLeft.x - positionInRoot.x, y = topLeft.y - positionInRoot.y)
            .width(tabBarWidth)
            .height(tabBarHeight)
//            .background(containerColor)
            .background(Color.Red)
    )

//    UIKitView(
//        factory = {
//            tabBarView
//        },
//        properties = UIKitInteropProperties(
//            interactionMode = UIKitInteropInteractionMode.NonCooperative,
//        ),
//        modifier = modifier
//            .fillMaxWidth()
//            .then(
//                if (tabBarManager.aspectRatio.isFinite() && tabBarManager.aspectRatio > 0f)
//                    Modifier
//                        .aspectRatio(tabBarManager.aspectRatio)
//                else
//                    Modifier
//            ),
//    )
}
