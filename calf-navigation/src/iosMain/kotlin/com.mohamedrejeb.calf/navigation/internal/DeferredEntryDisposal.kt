package com.mohamedrejeb.calf.navigation.internal

/**
 * Tears down entries that left the back stack while they were on screen, once UIKit has let go
 * of them.
 *
 * Such an entry cannot be disposed at once, UIKit still animates it out, and an entry never
 * disposes itself (see [EntryHostingController]). This is how every pop made on the Compose back
 * stack ends for the screen that was visible; without it its composition and ViewModels live on.
 */
internal class DeferredEntryDisposal {
    private var pending: List<EntryHostingController> = emptyList()

    fun defer(controllers: List<EntryHostingController>) {
        pending = pending + controllers
    }

    /**
     * Disposes the deferred entries UIKit no longer holds and returns them. Call it when a
     * transition has finished, or right after a change that was not animated.
     */
    fun disposeReleased(): List<EntryHostingController> {
        val (released, held) = pending.partition { it.parentViewController == null }
        released.forEach { it.disposeEntry() }
        pending = held
        return released
    }

    fun disposeAll() {
        pending.forEach { it.disposeEntry() }
        pending = emptyList()
    }
}
