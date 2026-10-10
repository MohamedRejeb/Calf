package com.mohamedrejeb.calf.navigation.internal

import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillResignActiveNotification

/**
 * The app's foreground state for every entry host, read from `UIApplication` and followed through
 * its notifications. One observer set for the process; entries subscribe while they live and are
 * told the current state at once.
 */
internal object IosAppForegroundState {
    private val listeners = mutableSetOf<(AppForegroundState) -> Unit>()
    private var isObserving = false

    var current: AppForegroundState = readApplicationState()
        private set

    fun subscribe(listener: (AppForegroundState) -> Unit) {
        startObserving()
        listeners += listener
        listener(current)
    }

    fun unsubscribe(listener: (AppForegroundState) -> Unit) {
        listeners -= listener
    }

    private fun startObserving() {
        if (isObserving) return
        isObserving = true
        current = readApplicationState()
        observe(UIApplicationDidBecomeActiveNotification, AppForegroundState.Active)
        observe(UIApplicationWillResignActiveNotification, AppForegroundState.Inactive)
        observe(UIApplicationDidEnterBackgroundNotification, AppForegroundState.Background)
        // Followed by didBecomeActive once the app really is active again.
        observe(UIApplicationWillEnterForegroundNotification, AppForegroundState.Inactive)
    }

    private fun observe(name: String?, state: AppForegroundState) {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = name,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> update(state) }
    }

    private fun update(state: AppForegroundState) {
        if (state == current) return
        current = state
        listeners.toList().forEach { it(state) }
    }

    private fun readApplicationState(): AppForegroundState =
        when (UIApplication.sharedApplication.applicationState) {
            UIApplicationState.UIApplicationStateActive -> AppForegroundState.Active
            UIApplicationState.UIApplicationStateInactive -> AppForegroundState.Inactive
            else -> AppForegroundState.Background
        }
}
