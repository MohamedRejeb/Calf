package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.InitialsAvatar
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable

private val FilePreviewSize = 96.dp
private const val PREVIEW_MAX_LINES = 2

/** One message of the inbox: avatar, sender and time, subject, and the start of the text. */
@Composable
internal fun NavDemoMailRow(
    mail: NavDemoMail,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        modifier = Modifier
            .fillMaxWidth()
            .adaptiveClickable(onClick = onClick)
            .background(background)
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
    ) {
        InitialsAvatar(
            name = mail.sender,
            color = mail.tint,
            size = NavDemoThumbnailSize,
            textStyle = MaterialTheme.typography.titleSmall,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mail.sender,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = mail.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = mail.subject,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = mail.paragraphs.first(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = PREVIEW_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A message: subject, sender, the text, its attachment, and the tab's state probe. */
@Composable
internal fun NavDemoMessageContent(
    messageId: Int,
    contentPadding: PaddingValues,
    onOpenAttachment: () -> Unit,
) {
    val mail = navDemoMail(messageId)

    DemoPage(contentPadding = contentPadding) {
        Text(
            text = mail.subject,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.xs),
        )
        DemoCard {
            NavDemoSenderRow(mail)
            DemoDivider(startIndent = 0.dp)
            mail.paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        DemoSection(title = "Attachment") {
            DemoListCard {
                DemoRow(
                    title = mail.attachment.name,
                    subtitle = "${mail.attachment.kind} · ${mail.attachment.size}",
                    leading = { NavDemoThumbnail(icon = mail.attachment.icon, color = mail.attachment.tint) },
                    onClick = onOpenAttachment,
                )
            }
        }
        NavDemoMailStateCheck()
    }
}

@Composable
private fun NavDemoSenderRow(mail: NavDemoMail) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        InitialsAvatar(
            name = mail.sender,
            color = mail.tint,
            size = NavDemoThumbnailSize,
            textStyle = MaterialTheme.typography.titleSmall,
        )
        Column {
            Text(
                text = mail.sender,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "To me · ${mail.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The attachment of a message, pushed from the message; in two panes it replaces the detail pane. */
@Composable
internal fun NavDemoAttachmentContent(
    messageId: Int,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    val mail = navDemoMail(messageId)
    val file = mail.attachment

    DemoPage(contentPadding = contentPadding) {
        DemoCard(horizontalAlignment = Alignment.CenterHorizontally) {
            NavDemoThumbnail(icon = file.icon, color = file.tint, size = FilePreviewSize)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${file.kind} · ${file.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AdaptiveButton(onClick = onBack) { Text("Back to the message") }
        }
        DemoSection(title = "Details") {
            DemoListCard {
                NavDemoInfoRow(label = "From", value = mail.sender)
                DemoDivider()
                NavDemoInfoRow(label = "Message", value = mail.subject)
                DemoDivider()
                NavDemoInfoRow(label = "Received", value = mail.date)
            }
        }
        NavDemoMailStateCheck()
    }
}
