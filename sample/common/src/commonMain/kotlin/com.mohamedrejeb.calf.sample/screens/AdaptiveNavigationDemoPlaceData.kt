package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Kayaking
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Water
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet

// The Adaptive Navigation demo is a small travel app: saved places on the Home tab (this file) and
// a mailbox on the Mail tab (AdaptiveNavigationDemoMailData.kt).

/** A saved place of the Home tab. Its coloured tile stands in for a photo. */
internal data class NavDemoPlace(
    val name: String,
    val country: String,
    val tagline: String,
    val about: String,
    val bestTime: String,
    val stay: String,
    val icon: ImageVector,
    val tint: Color,
)

/** The place behind a Home item. The + button can add more items than there are places, so they repeat. */
internal fun navDemoPlace(itemId: Int): NavDemoPlace = NavDemoPlaces[itemId.mod(NavDemoPlaces.size)]

private val NavDemoPlaces = listOf(
    NavDemoPlace(
        name = "Lisbon",
        country = "Portugal",
        tagline = "Trams, tiles and seven hills",
        about = "Yellow trams climb from the river to the castle, and every viewpoint has a café. " +
            "Stay in Alfama or Graça and walk everywhere else.",
        bestTime = "Apr – Jun",
        stay = "4 days",
        icon = Icons.Filled.LocationCity,
        tint = TileCoral,
    ),
    NavDemoPlace(
        name = "Dolomites",
        country = "Italy",
        tagline = "Pale peaks above green valleys",
        about = "Limestone towers that glow at sunset, huts that serve lunch at 2,000 metres and " +
            "cable cars that make the high trails easy to reach.",
        bestTime = "Jun – Sep",
        stay = "6 days",
        icon = Icons.Filled.Terrain,
        tint = TileTeal,
    ),
    NavDemoPlace(
        name = "Kyoto",
        country = "Japan",
        tagline = "Temples, gardens and quiet lanes",
        about = "More than a thousand temples, moss gardens and wooden streets. Start early: " +
            "Fushimi Inari and Arashiyama are calm before eight.",
        bestTime = "Mar – May",
        stay = "5 days",
        icon = Icons.Filled.Park,
        tint = TileAmber,
    ),
    NavDemoPlace(
        name = "Lofoten",
        country = "Norway",
        tagline = "Fishing villages under steep walls",
        about = "Red cabins on stilts, white beaches and mountains straight out of the sea. " +
            "The midnight sun lasts from late May to mid July.",
        bestTime = "Jun – Aug",
        stay = "5 days",
        icon = Icons.Filled.Sailing,
        tint = TileBlue,
    ),
    NavDemoPlace(
        name = "Marrakesh",
        country = "Morocco",
        tagline = "Souks, riads and the Atlas",
        about = "Get lost in the medina, then rest in a riad courtyard. The Atlas mountains are " +
            "a day trip away, with snow on the peaks until April.",
        bestTime = "Mar – May",
        stay = "4 days",
        icon = Icons.Filled.Castle,
        tint = TileViolet,
    ),
    NavDemoPlace(
        name = "Banff",
        country = "Canada",
        tagline = "Turquoise lakes and pine forests",
        about = "Lake Louise and Moraine Lake are at their bluest in July. Book the park shuttle " +
            "ahead, the lake roads close to cars in summer.",
        bestTime = "Jul – Sep",
        stay = "5 days",
        icon = Icons.Filled.Forest,
        tint = TileIndigo,
    ),
    NavDemoPlace(
        name = "Algarve",
        country = "Portugal",
        tagline = "Sea caves and golden cliffs",
        about = "Kayak into the Benagil cave at low tide and walk the Seven Hanging Valleys trail " +
            "along the cliffs in the late afternoon.",
        bestTime = "May – Oct",
        stay = "7 days",
        icon = Icons.Filled.BeachAccess,
        tint = TileAmber,
    ),
    NavDemoPlace(
        name = "Reykjavík",
        country = "Iceland",
        tagline = "Northern lights and hot springs",
        about = "A small, colourful capital and the start of the Golden Circle. From September, " +
            "clear nights bring the northern lights.",
        bestTime = "Sep – Mar",
        stay = "4 days",
        icon = Icons.Filled.AcUnit,
        tint = TileBlue,
    ),
    NavDemoPlace(
        name = "Patagonia",
        country = "Chile",
        tagline = "The W trek in Torres del Paine",
        about = "Five days of granite towers, glaciers and wind. Refugios along the trail need " +
            "to be booked months ahead for the summer season.",
        bestTime = "Nov – Mar",
        stay = "8 days",
        icon = Icons.Filled.Hiking,
        tint = TileTeal,
    ),
    NavDemoPlace(
        name = "Ha Long Bay",
        country = "Vietnam",
        tagline = "Limestone islands in green water",
        about = "Spend a night on a boat and paddle between the islands at dawn. Lan Ha Bay, " +
            "next door, is quieter and just as beautiful.",
        bestTime = "Oct – Apr",
        stay = "3 days",
        icon = Icons.Filled.Kayaking,
        tint = TileCoral,
    ),
    NavDemoPlace(
        name = "Lake Bled",
        country = "Slovenia",
        tagline = "A church on an island",
        about = "Row to the island, climb to the castle and walk the six kilometres around the " +
            "lake. Vintgar gorge is a short bus ride away.",
        bestTime = "May – Sep",
        stay = "3 days",
        icon = Icons.Filled.Water,
        tint = TileIndigo,
    ),
    NavDemoPlace(
        name = "Cappadocia",
        country = "Türkiye",
        tagline = "Balloons over fairy chimneys",
        about = "Balloons rise at sunrise over valleys of carved rock. Sleep in a cave hotel in " +
            "Göreme and hike the Rose Valley in the evening.",
        bestTime = "Apr – Jun",
        stay = "3 days",
        icon = Icons.Filled.Landscape,
        tint = TileViolet,
    ),
)
