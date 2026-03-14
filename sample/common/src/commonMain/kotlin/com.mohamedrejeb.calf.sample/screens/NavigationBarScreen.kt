package com.mohamedrejeb.calf.sample.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.AdaptiveTopBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

@Composable
private fun HomeTabContent(
    paddingValues: PaddingValues
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    var isMaterialDialogVisible by remember { mutableStateOf(false) }
    var isAdaptiveDialogVisible by remember { mutableStateOf(false) }

    if (isMaterialDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                isMaterialDialogVisible = false
            },
            title = {
                Text(text = "Material Dialog")
            },
            text = {
                Text(text = "This is a Material 3 AlertDialog.")
            },
            confirmButton = {}
        )
    }

    if (isAdaptiveDialogVisible) {
        AdaptiveAlertDialog(
            onConfirm = {
                isAdaptiveDialogVisible = false
            },
            onDismiss = {
                isAdaptiveDialogVisible = false
            },
            confirmText = "Confirm",
            dismissText = "Close",
            title = "Adaptive Dialog",
            text = "This is an Adaptive AlertDialog."
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = 16.dp + paddingValues.calculateTopPadding(),
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp + paddingValues.calculateBottomPadding()
        ),
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            // iOS-style header
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            // iOS-style card with rounded corners and subtle shadow
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is a modern iOS-style UI demo for the Adaptive Navigation Bar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // iOS-style button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        item {
            // Section title in iOS style
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariantColor,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        item {
            // iOS-style grouped list
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    // First item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMaterialDialogVisible = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(surfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dashboard",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View your activity",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceVariantColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(180f) // Rotate to point right
                        )
                    }

                    // Divider in iOS style
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 68.dp) // Inset divider
                    )

                    // Second item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isAdaptiveDialogVisible = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(surfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF2D55), // iOS red
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Favorites",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Your saved items",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceVariantColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(180f) // Rotate to point right
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "ITEMS LIST",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariantColor,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        items(30) { index ->
            Surface(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(surfaceVariantColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Item ${index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "This is item number ${index + 1} for testing padding",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariantColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteTabContent(
    paddingValues: PaddingValues,
) {
    val iosRed = Color(0xFFFF2D55)
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
        , // iOS background color
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = 16.dp + paddingValues.calculateTopPadding(),
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp + paddingValues.calculateBottomPadding()
        )
    ) {
        item {
            // iOS-style header
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            // iOS-style search bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = surfaceVariantColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = onSurfaceVariantColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColor
                    )
                }
            }
        }

        item {
            // Section title in iOS style
            Text(
                text = "FAVORITES",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariantColor,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        item {
            // iOS-style grouped list
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    // First favorite item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(surfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = iosRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Favorite Item 1",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Added on May 10, 2023",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceVariantColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(180f)
                        )
                    }

                    // Divider in iOS style
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 72.dp) // Inset divider
                    )

                    // Second favorite item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(surfaceVariantColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = iosRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Favorite Item 2",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Added on June 15, 2023",
                                style = MaterialTheme.typography.bodySmall,
                                color = onSurfaceVariantColor
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(180f)
                        )
                    }
                }
            }
        }

        item {
            // iOS-style button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = iosRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Favorites",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = iosRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTabContent(
    paddingValues: PaddingValues
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iosGreen = Color(0xFF34C759)
    val iosRed = Color(0xFFFF2D55)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = 16.dp + paddingValues.calculateTopPadding(),
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp + paddingValues.calculateBottomPadding()
        ),
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            // iOS-style header
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            // iOS-style profile card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile picture
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(surfaceVariantColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Name
                    Text(
                        text = "John Doe",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Email
                    Text(
                        text = "john.doe@example.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit Profile button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor
                    ) {
                        Text(
                            text = "Edit Profile",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        item {
            // Section title in iOS style
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariantColor,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        item {
            // iOS-style settings list
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    // Dark Mode setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icon container
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.inverseSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // iOS-style switch
                        AdaptiveSwitch(
                            checked = true,
                            onCheckedChange = { }
                        )
                    }

                    // Divider in iOS style
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 64.dp) // Inset divider
                    )

                    // Notifications setting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icon container
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(iosRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // iOS-style switch
                        AdaptiveSwitch(
                            checked = false,
                            onCheckedChange = { }
                        )
                    }
                }
            }
        }

        item {
            // Section title in iOS style
            Text(
                text = "ACCOUNT",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariantColor,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        item {
            // iOS-style logout button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalCalfUiApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NavigationBarScreen(
    navigateBack: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(1) }
    val items = listOf("Home", "Favorite", "Profile")
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Favorite,
        Icons.Outlined.Person
    )
    val selectedIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Favorite,
        Icons.Filled.Person
    )

    AdaptiveScaffold(
        topBar = {
            AdaptiveTopBar(
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { navigateBack() }
                        ) {
                            Icon(
                                Icons.Filled.ArrowBackIosNew,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Back",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                iosTitle = items[selectedIndex],
                iosLeadingItems = listOf(
                    UIKitUIBarButtonItem(
                        title = "Back",
                        image = UIKitImage.SystemName("chevron.left"),
                        onClick = { navigateBack() },
                    ),
                ),
                iosTrailingItems = listOf(
                    UIKitUIBarButtonItem(
                        image = UIKitImage.SystemName("magnifyingglass"),
                        onClick = { /* search */ },
                    ),
                ),
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        },
        bottomBar = {
            AdaptiveNavigationBar(
                iosItems = listOf(
                    UIKitUITabBarItem(
                        title = "Home",
                        image = UIKitImage.SystemName("house.fill"),
                    ),
                    UIKitUITabBarItem(
                        title = "Favorite",
                        image = UIKitImage.Vector(Icons.Rounded.Favorite),
                    ),
                    UIKitUITabBarItem(
                        title = "Profile",
                        image = UIKitImage.Vector(Icons.Rounded.Person),
                    ),
                ),
                iosSelectedIndex = selectedIndex,
                iosOnItemSelected = { selectedIndex = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                if (selectedIndex == index) selectedIcons[index] else icons[index],
                                contentDescription = item,
                            )
                        },
                        label = { Text(item) },
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        // Content — padding is automatically correct on both platforms
        AnimatedContent(
            targetState = items[selectedIndex],
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier
                .fillMaxSize()
        ) { currentTab ->
            when (currentTab) {
                "Home" -> HomeTabContent(paddingValues)
                "Favorite" -> FavoriteTabContent(paddingValues)
                "Profile" -> ProfileTabContent(paddingValues)
            }
        }
    }
}
