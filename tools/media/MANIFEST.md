# Media capture manifest (S23) — 2026-10-12

Rigs (capture.sh <rig> <screen>): ios26 = iPhone 17 Pro 7289CAAF… (Liquid Glass) → suffix `ios-liquid-glass`;
ios18 = iPhone 16 Pro 5356D66E… (Cupertino) → suffix `ios`; android = emulator-5554 Pixel 9 API 35 → suffix `material`.
Flows: $S/flows/<Screen>/open.yaml (launch, scroll to the row, open, takeScreenshot <Name>[-variant]) and
optional action.yaml (interaction recorded to a GIF). Stills land in $S/media/<rig>/<Screen>/stills, GIF in
$S/media/<rig>/<Screen>/<Screen>.gif. Final names (docs/images or docs/images/gif):

| Screen (flow dir) | Component name for files | Still(s) | GIF | Doc that shows it |
|---|---|---|---|---|
| Button | AdaptiveButton | AdaptiveButton-<rig>.png | AdaptiveButton-<rig>.gif | docs/ui/adaptive-button.md |
| Switch | AdaptiveSwitch | AdaptiveSwitch-<rig>.png | AdaptiveSwitch-<rig>.gif | docs/ui/adaptive-switch.md |
| Slider | AdaptiveSlider | AdaptiveSlider-<rig>.png | AdaptiveSlider-<rig>.gif | docs/ui/adaptive-slider.md |
| ProgressBar | AdaptiveCircularProgressIndicator | …-<rig>.png | — | docs/ui/adaptive-circular-progress-indicator.md |
| Clickable | AdaptiveClickable | AdaptiveClickable-<rig>.png | AdaptiveClickable-<rig>.gif | docs/ui/adaptive-clickable.md |
| Toolbar | AdaptiveToolbar | AdaptiveToolbar-<rig>.png | AdaptiveToolbar-<rig>.gif | docs/ui/adaptive-toolbar.md |
| CupertinoIcons | CupertinoIcons | CupertinoIcons-<rig>.png | — | docs/sf-symbols.md |
| Dialog | AdaptiveAlertDialog | AdaptiveAlertDialog-<rig>.png (+ -action-sheet, -text-field variants) | AdaptiveAlertDialog-<rig>.gif | docs/ui/adaptive-alert-dialog.md |
| BottomSheet | AdaptiveBottomSheet | AdaptiveBottomSheet-<rig>.png | AdaptiveBottomSheet-<rig>.gif | docs/ui/adaptive-bottom-sheet.md |
| DropDown | AdaptiveDropdownMenu | AdaptiveDropdownMenu-<rig>.png | AdaptiveDropdownMenu-<rig>.gif | docs/ui/adaptive-drop-down.md |
| DatePicker | AdaptiveDatePicker | AdaptiveDatePicker-<rig>.png | AdaptiveDatePicker-<rig>.gif | docs/ui/adaptive-date-picker.md |
| TimePicker | AdaptiveTimePicker | AdaptiveTimePicker-<rig>.png | — | docs/ui/adaptive-time-picker.md |
| FilePicker | AdaptiveFilePicker | AdaptiveFilePicker-<rig>.png | — | docs/filepicker.md |
| ImagePicker | AdaptiveImagePicker | AdaptiveImagePicker-<rig>.png | — | docs/filepicker.md |
| Camera | CameraPicker | CameraPicker-<rig>.png | — | README |
| FileSaver | FileSaver | FileSaver-<rig>.png | — | docs/filepicker.md |
| NavigationBar | AdaptiveNavigationBar | AdaptiveNavigationBar-<rig>.png | AdaptiveNavigationBar-<rig>.gif | docs/ui/adaptive-navigation-bar.md |
| Scaffold | AdaptiveScaffold | AdaptiveScaffold-<rig>.png | — | docs/ui.md |
| TopBar | AdaptiveTopAppBar | AdaptiveTopAppBar-<rig>.png | AdaptiveTopAppBar-<rig>.gif | docs/ui/adaptive-top-bar.md |
| Share | Share | Share-<rig>.png | — | docs/share.md |
| WebView | WebView | WebView-<rig>.png | — | docs/webview.md |
| Permissions | Permissions | Permissions-<rig>.png | — | docs/permissions.md |
| AdaptiveNavigation | AdaptiveNavigation | AdaptiveNavigation-<rig>.png (+ -mail, -message) | AdaptiveNavigation-<rig>.gif | docs/navigation.md |
| Foldable | Foldable | Foldable-<rig>.png | — | docs/foldable.md |
| Showcase | Showcase | Showcase-<rig>.png | Showcase-<rig>.gif | README hero |
| Home | Home | Home-<rig>.png | — | README |

Rules: stills ≤ 1080 px tall (capture.sh does `sips -Z 1080`); GIFs 360 px wide, 15 fps, ≤ ~400 KB; a GIF
must show the interaction fully (start on the settled screen, end settled); a still must show the screen with
the bar and content, no Maestro overlay, status bar fine. System pickers (file, image, camera, share) on
simulators: capture the sheet/picker as it appears; if it cannot open (no camera on sim), capture the demo
screen itself. iOS 26 simulator: Maestro's `hierarchy` command and failed `scrollUntilVisible` can crash
SpringBoard (app sent to the home screen, no app crash) — relaunch (`launchApp` does) and continue; prefer
`scrollUntilVisible` with the row's description text, taps by the exact text on screen.
