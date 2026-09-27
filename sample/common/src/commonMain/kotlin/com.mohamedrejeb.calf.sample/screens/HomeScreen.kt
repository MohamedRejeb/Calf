package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.NativeBadge
import com.mohamedrejeb.calf.sample.components.SectionTitle
import com.mohamedrejeb.calf.sample.isLiquidGlassAvailable
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.navigation.ScreenCategory
import com.mohamedrejeb.calf.sample.resources.Res
import com.mohamedrejeb.calf.sample.resources.calf_logo
import com.mohamedrejeb.calf.sample.ui.theme.PageMaxWidth
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import org.jetbrains.compose.resources.painterResource

private val HeaderLogoSize = 64.dp
private val PlaceholderLogoSize = 120.dp
private const val PLACEHOLDER_LOGO_ALPHA = 0.9f

/** The home list's demos, grouped by category in display order. */
private val demosByCategory: List<Pair<ScreenCategory, List<Screen>>> =
    ScreenCategory.entries.map { category ->
        category to Screen.entries.filter { it != Screen.Home && it.category == category }
    }.filter { (_, screens) -> screens.isNotEmpty() }

/**
 * The component list. It is the list pane of the sample's list-detail display, so beside an open
 * demo it highlights [selectedScreen]; on a narrow window it is the screen the demos are pushed
 * over.
 *
 * @param contentPadding the insets of the navigation entry: the pane's own safe area, not the
 *   window's (in a split view the two differ).
 */
@Composable
fun HomeScreen(
    navigate: (Screen) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    selectedScreen: Screen? = null,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        item(key = "header") {
            HomeHeader(modifier = Modifier.homeColumn().padding(top = Spacing.l, bottom = Spacing.s))
        }

        demosByCategory.forEach { (category, screens) ->
            item(key = category.name) {
                Column(modifier = Modifier.homeColumn().padding(top = Spacing.xl)) {
                    SectionTitle(category.title)
                    DemoListCard(modifier = Modifier.padding(top = Spacing.s)) {
                        screens.forEachIndexed { index, screen ->
                            if (index > 0) DemoDivider(startIndent = DividerIndentWithIcon)
                            DemoRow(
                                title = screen.title,
                                subtitle = screen.description,
                                leading = { IconTile(icon = screen.icon, color = category.tileColor) },
                                selected = screen == selectedScreen,
                                onClick = { navigate(screen) },
                            )
                        }
                    }
                }
            }
        }

        item(key = "footer") {
            Text(
                text = "Every screen here is the same Kotlin code on Android, iOS, desktop and web.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.homeColumn().padding(vertical = Spacing.xl),
            )
        }
    }
}

/** A home list item: full width up to [PageMaxWidth], with the page margins. */
private fun Modifier.homeColumn(): Modifier =
    widthIn(max = PageMaxWidth)
        .fillMaxWidth()
        .padding(horizontal = Spacing.pageMargin)

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.l),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Image(
                painter = painterResource(Res.drawable.calf_logo),
                contentDescription = null,
                modifier = Modifier.size(HeaderLogoSize),
            )
            Column {
                Text(
                    text = Screen.Home.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = Screen.Home.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        NativeBadge(platform = currentPlatform, implementation = platformLook())
    }
}

/** The look the adaptive components take on this platform. */
private fun platformLook(): String =
    when {
        !currentPlatform.isIOS -> "Material 3"
        isLiquidGlassAvailable -> "Liquid Glass"
        else -> "UIKit"
    }

private val ScreenCategory.tileColor: Color
    get() = when (this) {
        ScreenCategory.StartHere -> TileIndigo
        ScreenCategory.Components -> TileBlue
        ScreenCategory.Presentation -> TileViolet
        ScreenCategory.Pickers -> TileAmber
        ScreenCategory.Navigation -> TileTeal
        ScreenCategory.PlatformAPIs -> TileCoral
    }

/** Fills the detail pane while two panes are shown and no demo is open. */
@Composable
fun HomeDetailPlaceholder() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier.padding(Spacing.xxl),
        ) {
            Image(
                painter = painterResource(Res.drawable.calf_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(PlaceholderLogoSize)
                    .alpha(PLACEHOLDER_LOGO_ALPHA),
            )
            Text(
                text = "Select a component",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = Spacing.l),
            )
            Text(
                text = "Pick a demo from the list. It opens here, next to it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
