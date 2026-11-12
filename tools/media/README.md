# Media capture kit

Reproduces the screenshots and GIFs in `docs/images` from the sample app, one rig at a time.

| Rig | Device | Suffix |
|---|---|---|
| `ios26` | iPhone 17 Pro simulator, iOS 26 (Liquid Glass) | `ios-liquid-glass` |
| `ios18` | iPhone 16 Pro simulator, iOS 18 (Cupertino) | `ios` |
| `android` | Pixel 9 emulator, API 35 (Material 3) | `material` |

1. Build and install the sample on the device (`sample/ios` with `xcodebuild`, `./gradlew :sample:android:installDebug`),
   put the device ids in `capture.sh`, and have [Maestro](https://maestro.mobile.dev) on the PATH.
2. `tools/media/capture.sh <rig> <Screen>` for a screen of `MANIFEST.md` (flow dir names), or loop over them.
   Stills land in `build/media/<rig>/<Screen>/stills`, the GIF next to them.
3. Copy the files you keep to `build/media/final/<rig>/` with the manifest's names, then
   `python3 tools/media/assemble_media.py` to place them in `docs/images` and rewrite the docs tables
   (`--dry` lists what would change).

Web and desktop stills are taken by hand: the wasm sample in Chrome at 1280×820 with the light colour scheme,
the desktop app's window with `screencapture -l <window id>`. The iPhone Duo simulator is driven through Device
Hub's accessibility tree; see `plans/DUO_RELEASE_ROADMAP.md` (S23) for the recipe.
Flows under `flows/<rig>` tap by text and, where UIKit controls expose none, by point: they match the device sizes above.
