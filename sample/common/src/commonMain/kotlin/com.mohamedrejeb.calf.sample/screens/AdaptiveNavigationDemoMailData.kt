package com.mohamedrejeb.calf.sample.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileCoral
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet

// The mailbox of the Adaptive Navigation demo's Mail tab.

/** A file attached to a message of the Mail tab. */
internal data class NavDemoFile(
    val name: String,
    val kind: String,
    val size: String,
    val icon: ImageVector,
    val tint: Color,
)

/** A message of the Mail tab. The first paragraph is the inbox preview. */
internal data class NavDemoMail(
    val sender: String,
    val email: String,
    val subject: String,
    val time: String,
    val date: String,
    val paragraphs: List<String>,
    val attachment: NavDemoFile,
    val tint: Color,
)

/** The ids of the inbox's messages, as the Mail keys carry them. */
internal val navDemoMailIds: IntRange get() = 1..NavDemoMails.size

/** The message behind a Mail key; ids start at 1. */
internal fun navDemoMail(messageId: Int): NavDemoMail = NavDemoMails[(messageId - 1).mod(NavDemoMails.size)]

private const val PDF = "PDF document"
private const val PICTURE = "Image"

private val NavDemoMails: List<NavDemoMail> = listOf(
    NavDemoMail(
        sender = "Priya Shah",
        email = "priya.shah@example.com",
        subject = "Lisbon itinerary, final version",
        time = "09:41",
        date = "Sep 27, 2026 at 09:41",
        paragraphs = listOf(
            "Here is the final plan for the long weekend. I moved the Sintra day to Saturday so we avoid " +
                "the Sunday crowds, and booked the 10:15 train from Rossio.",
            "Friday evening is free. Marta suggested dinner in Alfama, and the fado place near the " +
                "cathedral takes walk-ins before eight.",
            "Everything is in the PDF, with the addresses and the booking numbers. Tell me if anything " +
                "looks off before I pay the deposit on Monday.",
            "Priya",
        ),
        attachment = NavDemoFile("Lisbon-itinerary.pdf", PDF, "412 KB", Icons.Filled.PictureAsPdf, TileCoral),
        tint = TileCoral,
    ),
    NavDemoMail(
        sender = "Harbour Rentals",
        email = "bookings@harbour-rentals.example",
        subject = "Your booking is confirmed",
        time = "08:15",
        date = "Sep 27, 2026 at 08:15",
        paragraphs = listOf(
            "Thank you for booking with us. Your apartment in Graça is reserved from Friday, October 9 " +
                "to Monday, October 12, for two guests.",
            "Check-in is from 15:00. The key box code will be sent the day before you arrive, together " +
                "with the Wi-Fi password and the building's door code.",
            "Your booking reference is HR-4821. Keep the attached confirmation with you.",
            "Harbour Rentals",
        ),
        attachment = NavDemoFile("Booking-HR4821.pdf", PDF, "96 KB", Icons.Filled.PictureAsPdf, TileBlue),
        tint = TileBlue,
    ),
    NavDemoMail(
        sender = "Tom Becker",
        email = "tom.becker@example.com",
        subject = "Photos from the Dolomites",
        time = "Yesterday",
        date = "Sep 26, 2026 at 19:02",
        paragraphs = listOf(
            "Finally sorted the photos from the trip. The sunrise at Seceda was worth the 4:30 alarm, " +
                "even if nobody believed me at the time.",
            "I attached my favourite one. The full album is shared with you, around 300 pictures, " +
                "so maybe look at it on Wi-Fi.",
            "Same trip next year? I was thinking about the Alta Via 1 in early September.",
            "Tom",
        ),
        attachment = NavDemoFile("Seceda-sunrise.jpg", PICTURE, "3.2 MB", Icons.Filled.Image, TileTeal),
        tint = TileTeal,
    ),
    NavDemoMail(
        sender = "Marta Silva",
        email = "marta.silva@example.com",
        subject = "Dinner on Friday?",
        time = "Yesterday",
        date = "Sep 26, 2026 at 12:30",
        paragraphs = listOf(
            "Priya told me you land on Friday afternoon. Would you like to have dinner with us? " +
                "There is a small place in Alfama we go to every time.",
            "They only cook what they bought at the market that morning, so the menu changes every " +
                "day. I attached last week's so you get an idea.",
            "We could meet at the Portas do Sol viewpoint at seven and walk down together.",
            "Beijinhos, Marta",
        ),
        attachment = NavDemoFile("Menu.pdf", PDF, "180 KB", Icons.Filled.PictureAsPdf, TileAmber),
        tint = TileAmber,
    ),
    NavDemoMail(
        sender = "Kenji Watanabe",
        email = "kenji.watanabe@example.com",
        subject = "Kyoto guide draft for review",
        time = "Thu",
        date = "Sep 24, 2026 at 16:48",
        paragraphs = listOf(
            "The second draft of the Kyoto guide is ready. I rewrote the Higashiyama walk and added " +
                "a section on the smaller temples in the north.",
            "Could you check the opening hours in the practical section? Several gardens changed " +
                "them after the summer.",
            "Comments by Tuesday would be perfect, the designer starts on the layout on Wednesday.",
            "Thanks, Kenji",
        ),
        attachment = NavDemoFile("Kyoto-guide-v2.docx", "Document", "1.1 MB", Icons.Filled.Description, TileIndigo),
        tint = TileIndigo,
    ),
    NavDemoMail(
        sender = "Ingrid Olsen",
        email = "ingrid@reine-cabins.example",
        subject = "Check-in details for Reine",
        time = "Wed",
        date = "Sep 23, 2026 at 10:05",
        paragraphs = listOf(
            "Your cabin is number 4, the red one closest to the water. The door will be open and the " +
                "key is on the kitchen table.",
            "The last ferry from Bodø to Moskenes leaves at 17:30. If you miss it, call me and I will " +
                "keep the heating on for a late arrival.",
            "Directions from the ferry are in the attachment. It is a short, beautiful drive.",
            "Ingrid",
        ),
        attachment = NavDemoFile("Directions.pdf", PDF, "240 KB", Icons.Filled.PictureAsPdf, TileBlue),
        tint = TileViolet,
    ),
    NavDemoMail(
        sender = "Sam Rivera",
        email = "sam.rivera@example.com",
        subject = "Budget sheet for the trip",
        time = "Tue",
        date = "Sep 22, 2026 at 21:17",
        paragraphs = listOf(
            "I put all the bookings in one sheet so we can split the costs. Flights and the apartment " +
                "are paid, the rest are estimates.",
            "Add anything I missed in the last tab. The totals per person update on their own.",
            "Let's settle up once we are back, no rush.",
            "Sam",
        ),
        attachment = NavDemoFile("Trip-budget.xlsx", "Spreadsheet", "58 KB", Icons.Filled.TableChart, TileTeal),
        tint = TileTeal,
    ),
    NavDemoMail(
        sender = "Nora Haddad",
        email = "nora.haddad@example.com",
        subject = "Riads in Marrakesh",
        time = "Mon",
        date = "Sep 21, 2026 at 18:40",
        paragraphs = listOf(
            "You asked for places to stay in Marrakesh, so here is my short list. All three are inside " +
                "the medina and within walking distance of the square.",
            "The second one has a rooftop terrace with a view of the Atlas. Ask for the room on the " +
                "top floor, it is the quietest.",
            "Taxis cannot reach most riads, so the owners will send someone to meet you at the gate.",
            "Nora",
        ),
        attachment = NavDemoFile("Riads.pdf", PDF, "1.4 MB", Icons.Filled.PictureAsPdf, TileViolet),
        tint = TileCoral,
    ),
    NavDemoMail(
        sender = "Lucas Moreau",
        email = "lucas.moreau@example.com",
        subject = "Slides for Thursday",
        time = "Sep 21",
        date = "Sep 21, 2026 at 09:12",
        paragraphs = listOf(
            "Attached are the slides for Thursday's review. I kept it to twelve slides and moved the " +
                "detailed numbers to the appendix.",
            "Could you present the roadmap part? You know the timeline better than anyone.",
            "We have the big room from 14:00 to 15:00, with a call link for the remote team.",
            "Lucas",
        ),
        attachment = NavDemoFile("Q3-review.key", "Presentation", "8.4 MB", Icons.Filled.Slideshow, TileAmber),
        tint = TileIndigo,
    ),
    NavDemoMail(
        sender = "Hana Kim",
        email = "hana.kim@example.com",
        subject = "Packing list",
        time = "Sep 20",
        date = "Sep 20, 2026 at 15:26",
        paragraphs = listOf(
            "Here is the list I use for every hiking trip. It has saved me more than once, especially " +
                "the part about spare socks.",
            "October can be cold at night even in the south, so bring a warm layer and a light rain " +
                "jacket.",
            "Have a wonderful trip and send pictures.",
            "Hana",
        ),
        attachment = NavDemoFile("Packing-list.txt", "Plain text", "2 KB", Icons.AutoMirrored.Filled.InsertDriveFile, TileIndigo),
        tint = TileViolet,
    ),
    NavDemoMail(
        sender = "Owen Price",
        email = "owen.price@example.com",
        subject = "Trail map for Torres del Paine",
        time = "Sep 19",
        date = "Sep 19, 2026 at 11:03",
        paragraphs = listOf(
            "As promised, the map of the W trek with the refugios marked. We did it west to east, " +
                "which keeps the towers for the last morning.",
            "Day three through the French Valley is the longest. Leave early, the wind picks up after " +
                "lunch.",
            "Happy to go through it on a call if you want.",
            "Owen",
        ),
        attachment = NavDemoFile("W-trek-map.png", PICTURE, "2.7 MB", Icons.Filled.Image, TileTeal),
        tint = TileBlue,
    ),
    NavDemoMail(
        sender = "Elena Rossi",
        email = "elena.rossi@example.com",
        subject = "Re: Lake Bled in October",
        time = "Sep 18",
        date = "Sep 18, 2026 at 08:57",
        paragraphs = listOf(
            "October is lovely there, the forests turn gold and the crowds are gone. The boats to the " +
                "island run until the end of the month.",
            "We stayed in a small cabin above the lake. I attached their price list, they have a " +
                "discount for stays of three nights or more.",
            "Try the cream cake at the hotel on the shore, it is famous for a reason.",
            "Elena",
        ),
        attachment = NavDemoFile("Bled-cabins.pdf", PDF, "310 KB", Icons.Filled.PictureAsPdf, TileIndigo),
        tint = TileAmber,
    ),
)
