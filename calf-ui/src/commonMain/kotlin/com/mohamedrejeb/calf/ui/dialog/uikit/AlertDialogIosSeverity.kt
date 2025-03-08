package com.mohamedrejeb.calf.ui.dialog.uikit

/**
 * This enumeration defines the severity options used by the severity property of `UIAlertController`. In apps built with Mac Catalyst, the severity determines the style of the presented alert. A `UIAlertControllerSeverityCritical` alert appears with a caution icon, and an alert with a `UIAlertControllerSeverityDefault` severity doesn’t. UIKit ignores the alert severity on iOS.
 *
 * You should only use the UIAlertControllerSeverityCritical severity if an alert truly requires special attention from the user. For more information, see the [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/macos/windows-and-views/alerts/) on alerts
 *
 * See [UIAlertControllerSeverity](https://developer.apple.com/documentation/uikit/uialertcontrollerseverity?language=objc)
 */
enum class AlertDialogIosSeverity {
    /**
     * Indicates that the system should present the alert using the standard alert style.
     */
    Default,
    /**
     * Indicates that the system should present the alert using the critical, or caution, style.
     */
    Critical
}