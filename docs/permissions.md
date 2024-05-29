# Permissions

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-permissions)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-permissions%22)

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
implementation("com.mohamedrejeb.calf:calf-permissions:0.4.1")
```

## Usage

`rememberPermissionState` and `rememberMultiplePermissionsState` APIs

The `rememberPermissionState(permission: Permission)` API allows you to request a certain permission to the user and check for the status of the permission. 
`rememberMultiplePermissionsState(permissions: List<Permission>)` offers the same but for multiple permissions at the same time.

You can use the `launchPermissionRequest()` method to launch the permission request dialog.

You can check the status of the permission using the `status` property of the `PermissionState` object.

example: `cameraPermissionState.status.isGranted`

| Android                                                      | iOS                                                  |
|--------------------------------------------------------------|------------------------------------------------------|
| ![Permission Dialog Android](images/Permissions-android.png) | ![Permission Diqlog iOS](images/Permissions-ios.png) |

```kotlin
// Camera permission state
val cameraPermissionState = rememberPermissionState(
    Permission.Camera
)

if (cameraPermissionState.status.isGranted) {
    Text("Camera permission Granted")
} else {
    Button(
        onClick = { cameraPermissionState.launchPermissionRequest() }
    ) {
        Text("Request permission")
    }
}
```

## Permissions

### Camera Permission

To request the camera permission, use `Permission.Camera`.

#### Android

On Android you need to add the following permission to your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

#### iOS

On iOS you need to add the following key to your `Info.plist` file:

```xml
<key>NSCameraUsageDescription</key>
<string>Camera permission is required to take pictures</string>
```

The string value is the message that will be displayed to the user when the permission is requested.

### Gallery Permission

To request the gallery permission, use `Permission.Gallery`.

#### Android

This permission is always granted on Android.

#### iOS

On iOS you need to add the following key to your `Info.plist` file:

```xml
<key>NSPhotoLibraryUsageDescription</key>
<string>Gallery permission is required to pick images</string>
```

The string value is the message that will be displayed to the user when the permission is requested.

### Read Storage Permission

To request the read storage permission, use `Permission.ReadStorage`.

#### Android

On Android you need to add the following permission to your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

#### iOS

This permission is always granted on iOS.

### Write Storage Permission

To request the write storage permission, use `Permission.WriteStorage`.

#### Android

On Android you need to add the following permission to your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
    tools:ignore="ScopedStorage" />
```

#### iOS

This permission is always granted on iOS.

### Location Permission

To request the location permission, use `Permission.FineLocation` or `Permission.CoarseLocation`.

#### Android

On Android you need to add the following permission to your `AndroidManifest.xml` file:

```xml
<!-- For fine location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- For coarse location -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

#### iOS

On iOS you need to add the following key to your `Info.plist` file:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>Location permission is required to get your location</string>
```

The string value is the message that will be displayed to the user when the permission is requested.

### Remote Notification Permission

To request the remote notification permission, use `Permission.RemoteNotification`.

#### Android

This permission is always granted on Android.

#### iOS

There is no need to add anything to your `Info.plist` file to request this permission.

### Record Audio Permission

To request the record audio permission, use `Permission.RecordAudio`.

#### Android

On Android you need to add the following permission to your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

#### iOS

On iOS you need to add the following key to your `Info.plist` file:

```xml
<key>NSMicrophoneUsageDescription</key>
<string>Record audio permission is required to record audio</string>
```

The string value is the message that will be displayed to the user when the permission is requested.

### Bluetooth Permission

To request the bluetooth permission, use `Permission.BluetoothLe` or `Permission.BluetoothScan` or `Permission.BluetoothConnect` or `Permission.BluetoothAdvertise`.

#### Android

On Android you need to add the following permission to your `AndroidManifest.xml` file:

```xml
<!-- For Bluetooth LE -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<!-- For Bluetooth Scan -->
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<!-- For Bluetooth Connect -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<!-- For Bluetooth Advertise -->
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
```

#### iOS

On iOS you need to add the following key to your `Info.plist` file:

```xml
<key>NSBluetoothAlwaysUsageDescription</key>
<string>Bluetooth permission is required to use Bluetooth</string>
```

The string value is the message that will be displayed to the user when the permission is requested.
