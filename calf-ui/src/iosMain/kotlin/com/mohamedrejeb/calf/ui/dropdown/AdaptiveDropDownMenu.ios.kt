package com.mohamedrejeb.calf.ui.dropdown


import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPoint
import platform.UIKit.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BoxScope.AdaptiveDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier
) {
    val contextMenu = remember {
        CupertinoContextMenu()
    }

    UIKitView(
        factory = {
            val interaction = UIContextMenuInteraction(delegate = contextMenu)
            val button = UIButton.buttonWithType(UIButtonTypeSystem)

            button.setTitle("More", forState = UIControlStateNormal)
            button.addInteraction(interaction)

            button
        },
        modifier = Modifier
            .matchParentSize()
    )
}

internal class CupertinoContextMenu() : NSObject(), UIContextMenuInteractionDelegateProtocol {
    fun show() {

    }

    @OptIn(ExperimentalForeignApi::class)
    override fun contextMenuInteraction(
        interaction: UIContextMenuInteraction,
        configurationForMenuAtLocation: CValue<CGPoint>
    ): UIContextMenuConfiguration? {
        return UIContextMenuConfiguration.configurationWithIdentifier(
            identifier = null,
            previewProvider = null,
            actionProvider = {
                val removeRatingAction = makeRemoveRatingAction()
                UIMenu.menuWithTitle(
                    title = "",
                    children = listOf<UIMenuElement>(
                        removeRatingAction,
                    )
                )
            }
        )
    }

    fun makeRemoveRatingAction(): UIAction {
        // 1
        val removeRatingAttributes = UIMenuElementAttributesDestructive

        // 2
//        if (currentUserRating == 0) {
//            removeRatingAttributes.insert(.disabled)
//        }

        // 3
        val deleteImage = UIImage.systemImageNamed("delete.left")

        // 4
        return UIAction.actionWithTitle(
            title = "Remove rating",
            image = deleteImage,
            identifier = null,
        ) {
            println("Action rating")
        }.apply {
            // 5
            attributes = removeRatingAttributes
        }
    }
}