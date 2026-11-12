package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sf.symbols.SFSymbol
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

// The trip the Showcase plans. Fixed content so the screen looks the same in every recording.

internal const val TRIP_NAME = "Weekend in Lisbon"
internal const val TRIP_NIGHTS = 2
internal const val TRIP_INVITE_LINK = "lisbon-weekend.trip/j/8KQ2"
internal const val CALF_SOURCE_URL = "github.com/MohamedRejeb/Calf"

/** Friday, October 16, 2026: the departure the date picker starts on. */
internal val TripInitialDeparture = LocalDate(year = 2026, month = 10, day = 16)
internal const val TRIP_INITIAL_HOUR = 9
internal const val TRIP_INITIAL_MINUTE = 30

/** The per-person budget slider, in euros. */
internal const val BUDGET_MIN = 150f
internal const val BUDGET_MAX = 900f
internal const val BUDGET_ROUNDING = 10
internal const val BUDGET_INITIAL = 450f

/** The three tabs of the Showcase, with their Material icon and iOS SF Symbol. */
internal enum class ShowcaseTab(
    val title: String,
    val icon: ImageVector,
    val iosSymbol: String,
) {
    Plan("Plan", Icons.Outlined.Luggage, SFSymbol.suitcase),
    Schedule("Schedule", Icons.Outlined.CalendarMonth, SFSymbol.calendar),
    People("People", Icons.Outlined.Group, SFSymbol.person2),
}

/** A person on the trip. [status] is the second line of their row. */
internal data class Traveller(
    val name: String,
    val status: String,
    val color: Color,
    val isPending: Boolean = false,
)

internal val TripTravellers: List<Traveller> = listOf(
    Traveller(name = "Inès Duarte", status = "Organiser", color = TileIndigo),
    Traveller(name = "Sam Carter", status = "Flying from London", color = TileTeal),
    Traveller(name = "Yuki Tanaka", status = "Flying from Paris", color = TileAmber),
    Traveller(name = "Omar Haddad", status = "Invited on Sep 24", color = TileCoral, isPending = true),
)

/** When the departure reminder fires. [Off] is the destructive entry of the menu. */
internal enum class ReminderOption(val label: String) {
    AtDeparture("At departure"),
    OneHourBefore("1 hour before"),
    OneDayBefore("1 day before"),
    Off("Off"),
}

// Formatting. Plain English names instead of a locale formatter: the sample shows the same text
// on every platform.

private val WeekdayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val MonthNames =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private const val THOUSANDS = 1000

internal fun LocalDate.toEpochMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

/** The picker's selection (UTC midnight) as a date, or the initial departure when cleared. */
internal fun departureDateOf(selectedDateMillis: Long?): LocalDate =
    selectedDateMillis
        ?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date }
        ?: TripInitialDeparture

internal fun LocalDate.returnDate(): LocalDate = plus(TRIP_NIGHTS, DateTimeUnit.DAY)

/** "Fri, Oct 16". */
internal fun LocalDate.shortLabel(): String =
    "${WeekdayNames[dayOfWeek.ordinal]}, ${MonthNames[month.ordinal]} $day"

/** "Oct 16 – 18", or "Oct 30 – Nov 1" across months. */
internal fun tripRangeLabel(departure: LocalDate): String {
    val back = departure.returnDate()
    val start = "${MonthNames[departure.month.ordinal]} ${departure.day}"
    val end = if (back.month == departure.month) "${back.day}" else "${MonthNames[back.month.ordinal]} ${back.day}"
    return "$start – $end"
}

/** "09:30". */
internal fun timeLabel(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

/** "€1,800". */
internal fun euros(amount: Int): String {
    val thousands = amount / THOUSANDS
    val rest = amount % THOUSANDS
    return if (thousands == 0) "€$rest" else "€$thousands,${rest.toString().padStart(3, '0')}"
}
