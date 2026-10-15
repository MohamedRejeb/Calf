package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.isLiquidGlassAvailable
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.button.LiquidGlassButtonColors

private const val USAGE = """
AdaptiveButton(onClick = { addToBag() }) {
    Text("Add to bag")
}

AdaptiveIconButton(onClick = { isFavourite = !isFavourite }) {
    Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Favourite")
}
"""

private const val UNIT_PRICE_EUROS = 89
private const val MAX_QUANTITY = 5
private const val DISABLED_GLASS_ALPHA = 0.4f

/** Closes the Variants footer: the rows that pass Material-only parameters. */
private const val MATERIAL_ONLY_FOOTER =
    "Outlined, elevated and squared pass border, elevation and shape, which are Material only."
private val ThumbnailSize = 56.dp
private val ButtonIconSize = 18.dp
private val QuantityWidth = 24.dp

/** Space between an icon button's edge and its icon; offset so the glyphs line up with the card. */
private val IconButtonInset = 12.dp

@Composable
fun ButtonScreen(navigateBack: () -> Unit) {
    SampleScreenScaffold(title = Screen.Button.title, navigateBack = navigateBack) { padding ->
        DemoPage(contentPadding = padding) {
            DemoHeader(Screen.Button)
            ProductSection()
            VariantsSection()
            if (isLiquidGlassAvailable) LiquidGlassSection()
            DemoSection(title = "Usage") {
                CodeSnippet(USAGE.trimIndent())
            }
        }
    }
}

@Composable
private fun ProductSection() {
    var quantity by rememberSaveable { mutableIntStateOf(1) }
    var favourite by rememberSaveable { mutableStateOf(false) }
    var inBag by rememberSaveable { mutableIntStateOf(0) }
    var lastTap by rememberSaveable { mutableStateOf("Nothing yet") }

    DemoSection(
        title = "Product",
        footer = "The stepper passes enabled = false at 1 and at $MAX_QUANTITY items.",
    ) {
        DemoCard {
            ProductSummary()
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(
                    quantity = quantity,
                    onQuantityChange = { quantity = it },
                    modifier = Modifier.offset(x = -IconButtonInset),
                )
                Spacer(Modifier.weight(1f))
                AdaptiveIconButton(
                    onClick = {
                        favourite = !favourite
                        lastTap = if (favourite) "Saved to favourites" else "Removed from favourites"
                    },
                    modifier = Modifier.offset(x = IconButtonInset),
                ) {
                    Icon(
                        imageVector = if (favourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favourite",
                    )
                }
                AdaptiveIconButton(
                    onClick = { lastTap = "Share sheet opened" },
                    modifier = Modifier.offset(x = IconButtonInset),
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share")
                }
            }
            AdaptiveButton(
                onClick = {
                    inBag += quantity
                    lastTap = "Added $quantity to bag"
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                ButtonIcon(Icons.Outlined.ShoppingBag)
                Text("Add to bag · €${UNIT_PRICE_EUROS * quantity}")
            }
        }
        DemoCard {
            ValueRow(label = "In your bag", value = if (inBag == 1) "1 item" else "$inBag items")
            ValueRow(label = "Last tap", value = lastTap)
        }
    }
}

@Composable
private fun ProductSummary() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(ThumbnailSize)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.medium),
        ) {
            Icon(
                imageVector = Icons.Outlined.Checkroom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Linen overshirt",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Sand · Size M · In stock",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "€$UNIT_PRICE_EUROS",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        AdaptiveIconButton(
            onClick = { onQuantityChange(quantity - 1) },
            enabled = quantity > 1,
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = "One less")
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = QuantityWidth),
        )
        AdaptiveIconButton(
            onClick = { onQuantityChange(quantity + 1) },
            enabled = quantity < MAX_QUANTITY,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "One more")
        }
    }
}

@Composable
private fun VariantsSection() {
    DemoSection(
        title = "Variants",
        footer = if (isLiquidGlassAvailable) {
            MATERIAL_ONLY_FOOTER
        } else {
            "On iOS 26 these are Liquid Glass buttons, and liquidGlassColors sets their glass colours. " +
                MATERIAL_ONLY_FOOTER
        },
    ) {
        DemoListCard {
            DemoRow(
                title = "Filled",
                trailing = { AdaptiveButton(onClick = {}) { Text("Checkout") } },
            )
            DemoDivider()
            DemoRow(title = "Outlined", trailing = { OutlinedCheckout() })
            DemoDivider()
            DemoRow(title = "Elevated", trailing = { ElevatedCheckout() })
            DemoDivider()
            DemoRow(
                title = "Disabled",
                trailing = { AdaptiveButton(onClick = {}, enabled = false) { Text("Checkout") } },
            )
            DemoDivider()
            DemoRow(
                title = "Icon and label",
                trailing = {
                    AdaptiveButton(onClick = {}) {
                        ButtonIcon(Icons.Outlined.Add)
                        Text("New list")
                    }
                },
            )
            DemoDivider()
            DemoRow(
                title = "Icon buttons",
                trailing = {
                    Row {
                        IconOnlyButton(Icons.Outlined.FavoriteBorder, "Favourite")
                        IconOnlyButton(Icons.Outlined.Share, "Share")
                        IconOnlyButton(Icons.Outlined.Add, "Add")
                    }
                },
            )
            DemoDivider()
            DemoRow(title = "Tonal, squared", trailing = { TonalSquaredIconButtons() })
            DemoDivider()
            DemoRow(
                title = "Icon button, disabled",
                trailing = { IconOnlyButton(Icons.Outlined.FavoriteBorder, "Favourite", enabled = false) },
            )
        }
    }
}

/** Material's outlined button: transparent container, `border` around it and no shadow. */
@Composable
private fun OutlinedCheckout() {
    AdaptiveButton(
        onClick = {},
        colors = ButtonDefaults.outlinedButtonColors(),
        elevation = null,
        border = ButtonDefaults.outlinedButtonBorder(),
    ) {
        Text("Checkout")
    }
}

/** Material's elevated button: a surface container lifted by `elevation`. */
@Composable
private fun ElevatedCheckout() {
    AdaptiveButton(
        onClick = {},
        colors = ButtonDefaults.elevatedButtonColors(),
        elevation = ButtonDefaults.elevatedButtonElevation(),
    ) {
        Text("Checkout")
    }
}

/** Tonal containers squared off through `shape`. */
@Composable
private fun TonalSquaredIconButtons() {
    val tonal = IconButtonDefaults.filledTonalIconButtonColors()
    val squared = MaterialTheme.shapes.small
    Row {
        IconOnlyButton(Icons.Outlined.FavoriteBorder, "Favourite", colors = tonal, shape = squared)
        IconOnlyButton(Icons.Outlined.Share, "Share", colors = tonal, shape = squared)
        IconOnlyButton(Icons.Outlined.Add, "Add", colors = tonal, shape = squared)
    }
}

/** Only shown where [isLiquidGlassAvailable]: clear glass through `liquidGlassColors`. */
@Composable
private fun LiquidGlassSection() {
    val onSurface = MaterialTheme.colorScheme.onSurface
    // Unspecified tint and surface: an untinted glass button instead of one tinted with `colors`.
    val clearGlass = LiquidGlassButtonColors(
        tintColor = Color.Unspecified,
        surfaceColor = Color.Unspecified,
        contentColor = onSurface,
        disabledContentColor = onSurface.copy(alpha = DISABLED_GLASS_ALPHA),
    )
    DemoSection(
        title = "Liquid Glass",
        footer = "liquidGlassColors with an unspecified tint and surface gives a clear glass button.",
    ) {
        DemoListCard {
            DemoRow(
                title = "Clear glass",
                trailing = {
                    AdaptiveButton(onClick = {}, liquidGlassColors = clearGlass) { Text("Save for later") }
                },
            )
            DemoDivider()
            DemoRow(
                title = "Clear glass, disabled",
                trailing = {
                    AdaptiveButton(onClick = {}, enabled = false, liquidGlassColors = clearGlass) {
                        Text("Save for later")
                    }
                },
            )
        }
    }
}

@Composable
private fun IconOnlyButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    shape: Shape = IconButtonDefaults.standardShape,
) {
    AdaptiveIconButton(onClick = {}, enabled = enabled, colors = colors, shape = shape) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

/** The leading icon of a button with a label, sized and spaced like Material's. */
@Composable
private fun ButtonIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
    Spacer(Modifier.width(Spacing.s))
}
