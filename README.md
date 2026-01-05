# @kirenpaul/rn-foreground-service

A powerful React Native library for running Android foreground services with headless task management, customizable notifications, and notification interactions.

[![NPM Version](https://img.shields.io/npm/v/@kirenpaul/rn-foreground-service.svg)](https://www.npmjs.com/package/@kirenpaul/rn-foreground-service)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)

## 🎉 What's New in v3.0.0

- 🚀 **Full Android 14+ Support** - Complete compatibility with Android 14's foreground service requirements
- 🎯 **Dynamic Service Types** - Pass `serviceType` parameter to specify service type (dataSync, location, etc.)
- ✅ **New Helper Methods** - `checkNotificationPermission()` and `canStartForegroundService()`
- 📘 **Full TypeScript Support** - Complete type definitions with autocomplete for all 11 service types
- 🔒 **Enhanced Error Messages** - Clear, version-specific error messages with actionable solutions
- ⚡ **Zero Breaking Changes** - Fully backward compatible with v2.x

See [CHANGELOG.md](CHANGELOG.md) for complete release notes.

## Overview

This library enables React Native applications to run foreground services on Android with persistent notifications. It's perfect for applications that need to perform background operations while keeping the user informed through a persistent notification.

### Key Features

- ✅ **Android 14+ Compatible** - Full support for foreground service types
- 🎯 **11 Service Types** - Specify service type (dataSync, location, mediaPlayback, etc.)
- 🔔 **Smart Permission Handling** - Built-in helpers for Android 13+ notifications
- 🚀 **Run persistent foreground services** on Android
- 📋 **Execute multiple headless tasks** concurrently
- 🎨 **Customizable notifications** with up to 2 action buttons
- 📊 **Progress bar support** in notifications
- 🎨 **Custom notification colors** and icons
- 🖱️ **Handle notification** and button click events
- 📚 **Comprehensive task management** API
- ⚙️ **Automatic setup** via postinstall script
- 📘 **Full TypeScript support** with type definitions

### When to Use This Library

Use this library when you need to:
- Run background tasks that must continue even when the app is minimized
- Display persistent notifications for ongoing operations
- Execute periodic tasks with custom intervals
- Handle user interactions through notification buttons
- Track progress of long-running operations

## Installation

```bash
npm install @kirenpaul/rn-foreground-service
```

or

```bash
yarn add @kirenpaul/rn-foreground-service
```

The postinstall script will automatically configure your Android files with Android 14+ compatible service declarations.

### 📋 Upgrading from v2.x?

If you're upgrading from v2.x, see the [Migration Guide](MIGRATION_v2_to_v3.md) for step-by-step instructions. Your JavaScript code will work as-is, but you'll need to update your AndroidManifest.xml.

## 🔴 Important: Android 14+ Requirements

Starting with **Android 14 (API 34)**, foreground services have new requirements. **Version 3.0.0 makes this easy** with automatic configuration and new helper methods.

### What's Handled Automatically (v3.0.0)
- ✅ **Service declarations** - postinstall script configures your AndroidManifest.xml
- ✅ **Dynamic service types** - Pass `serviceType` parameter when starting service
- ✅ **Permission checks** - New helper methods: `checkNotificationPermission()` and `canStartForegroundService()`

### What You Must Configure
- ❌ **Type-specific permissions** - Add to your AndroidManifest.xml (see below)
- ❌ **Runtime permission request** - Request POST_NOTIFICATIONS on Android 13+ (code provided below)
- ❌ **targetSdkVersion** - Update to 34 in build.gradle

**Good news:** With v3.0.0, you can dynamically specify service types without editing the manifest each time! See the [Usage](#start-the-foreground-service) section.

## Setup

### 1. AndroidManifest.xml

The postinstall script automatically adds service declarations, but you **must add these permissions** to `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest
  xmlns:android="http://schemas.android.com/apk/res/android"
  package="com.your.app"
>
  <!-- ✅ REQUIRED: Basic Permissions -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
  <uses-permission android:name="android.permission.WAKE_LOCK" />

  <!-- ✅ REQUIRED: Android 13+ Notification Permission -->
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

  <!-- ✅ REQUIRED: Android 14+ Foreground Service Type Permission -->
  <!-- Add the permission(s) that match your use case: -->

  <!-- For data sync, file operations, network requests -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

  <!-- For location tracking (GPS, geofencing) -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

  <!-- For audio/video playback -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

  <!-- See "Service Types" section below for all available types -->

  <application>
    <!-- Notification Channel Configuration (optional, can customize) -->
    <meta-data
      android:name="com.supersami.foregroundservice.notification_channel_name"
      android:value="Sticky Title"
    />
    <meta-data
      android:name="com.supersami.foregroundservice.notification_channel_description"
      android:value="Sticky Description."
    />
    <meta-data
      android:name="com.supersami.foregroundservice.notification_color"
      android:resource="@color/blue"
    />

    <!-- Service Declarations (auto-added by postinstall script) -->
    <service
        android:name="com.supersami.foregroundservice.ForegroundService"
        android:foregroundServiceType="dataSync|location|mediaPlayback"
        android:exported="false" />
    <service
        android:name="com.supersami.foregroundservice.ForegroundServiceTask"
        android:exported="false" />
  </application>
</manifest>
```

**Note:** The postinstall script configures services with `dataSync|location|mediaPlayback` types by default.

**NEW in v3.0.0:** You can now **dynamically specify** the service type in your JavaScript code:
```javascript
// No manifest editing needed!
await ReactNativeForegroundService.start({
  id: 1,
  title: 'Camera Service',
  message: 'Recording...',
  serviceType: 'camera' // Just pass the type you need
});
```

If you prefer static configuration or need a type not in the default list, you can manually edit the service declaration in your manifest.

### 2. MainActivity.java

Update your `MainActivity.java` to handle notification interactions:

```java
package com.your.app;

import android.os.Bundle;
import com.facebook.react.ReactActivity;
import android.content.Intent;
import android.util.Log;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class MainActivity extends ReactActivity {

  public boolean isOnNewIntent = false;

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    isOnNewIntent = true;
    ForegroundEmitter();
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!isOnNewIntent) {
      ForegroundEmitter();
    }
  }

  public void ForegroundEmitter() {
    // Emit notification click events to JavaScript
    String main = getIntent().getStringExtra("mainOnPress");
    String btn = getIntent().getStringExtra("buttonOnPress");
    String btn2 = getIntent().getStringExtra("button2OnPress");

    WritableMap map = Arguments.createMap();
    if (main != null) {
      map.putString("main", main);
    }
    if (btn != null) {
      map.putString("button", btn);
    }
    if (btn2 != null) {
      map.putString("button2", btn2);
    }

    try {
      getReactInstanceManager().getCurrentReactContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("notificationClickHandle", map);
    } catch (Exception e) {
      Log.e("ForegroundService", "Error emitting event: " + e.getMessage());
    }
  }
}
```

### 3. colors.xml

Create or update `android/app/src/main/res/values/colors.xml`:

```xml
<resources>
  <item name="blue" type="color">#00C4D1</item>
  <integer-array name="androidcolors">
    <item>@color/blue</item>
  </integer-array>
</resources>
```

### 4. Request Runtime Permissions (Android 13+)

**Critical:** On Android 13+, you must request the `POST_NOTIFICATIONS` permission at runtime before starting the service.

**Easy way (NEW in v3.0.0)** - Use the built-in helper:

```javascript
import { PermissionsAndroid } from 'react-native';
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

async function requestNotificationPermission() {
  // Check if permission is already granted (works on all Android versions)
  const hasPermission = await ReactNativeForegroundService.checkNotificationPermission();

  if (!hasPermission) {
    // Request permission
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
      {
        title: 'Notification Permission',
        message: 'This app needs notification permission to show service status.',
        buttonNeutral: 'Ask Me Later',
        buttonNegative: 'Cancel',
        buttonPositive: 'OK',
      }
    );

    return granted === PermissionsAndroid.RESULTS.GRANTED;
  }

  return true;
}

// Use before starting service
async function startMyService() {
  // 1. Check notification permission (Android 13+)
  const hasPermission = await requestNotificationPermission();
  if (!hasPermission) {
    console.log('Cannot start service without notification permission');
    return;
  }

  // 2. Check if can start from background (Android 12+) - NEW in v3.0.0
  const canStart = await ReactNativeForegroundService.canStartForegroundService();
  if (!canStart) {
    console.log('Cannot start service from background. Please open the app.');
    // You might want to show a notification or alert to the user
    return;
  }

  // 3. Now safe to start the service!
  await ReactNativeForegroundService.start({
    id: 144,
    title: 'Service Running',
    message: 'Performing background tasks...',
    serviceType: 'dataSync', // Specify service type (NEW in v3.0.0)
  });
}
```

### 5. Update build.gradle (Android 14+)

Ensure your app targets Android 14 or higher. Update `android/app/build.gradle`:

```gradle
android {
    compileSdkVersion 34

    defaultConfig {
        targetSdkVersion 34
        // ... other config
    }
}
```

---

### ✨ v3.0.0 Makes Android 14+ Easy!

**What used to be hard:**
- ❌ Manually editing AndroidManifest.xml for different service types
- ❌ Complex permission checking logic
- ❌ Unclear error messages when things went wrong

**What's easy now with v3.0.0:**
- ✅ **Dynamic service types** - Just pass `serviceType: 'location'` in your code
- ✅ **Built-in helpers** - `checkNotificationPermission()` and `canStartForegroundService()`
- ✅ **Clear errors** - Version-specific error messages that tell you exactly what to fix
- ✅ **Auto-configuration** - postinstall script handles the AndroidManifest.xml setup

See complete examples below! 👇

---

## Usage

### Register the Service

In your main app entry file (e.g., `index.js`):

```javascript
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';
import { AppRegistry } from 'react-native';
import { name as appName } from './app.json';
import App from './src/App';

// Register the foreground service
ReactNativeForegroundService.register();

AppRegistry.registerComponent(appName, () => App);
```

### Start the Foreground Service

```javascript
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

// Basic usage (defaults to dataSync service type)
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Service Running',
  message: 'Your app is running in the background',
});

// Specify service type (NEW in v3.0.0)
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Location Tracker',
  message: 'Tracking your location',
  serviceType: 'location', // Specify the service type
});

// With custom configuration
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Download Manager',
  message: 'Downloading files...',
  serviceType: 'dataSync', // NEW: Specify service type
  icon: 'ic_download',
  largeIcon: 'ic_launcher',
  importance: 'high',
  visibility: 'public',
  number: '1',
  button: true,
  buttonText: 'Pause',
  buttonOnPress: 'pause',
  button2: true,
  button2Text: 'Cancel',
  button2OnPress: 'cancel',
  mainOnPress: 'open',
  color: '#00ff00',
  progress: {
    max: 100,
    curr: 45
  },
  setOnlyAlertOnce: true
});
```

### Add and Manage Tasks

```javascript
// Add a one-time task
const taskId = ReactNativeForegroundService.add_task(
  () => console.log('Task executed'),
  {
    delay: 1000,
    onLoop: false,
    taskId: 'one-time-task',
    onSuccess: () => console.log('Task completed'),
    onError: (error) => console.error('Task failed:', error)
  }
);

// Add a recurring task
ReactNativeForegroundService.add_task(
  async () => {
    // Your background work here
    const data = await fetchData();
    processData(data);
  },
  {
    delay: 5000, // Run every 5 seconds
    onLoop: true,
    taskId: 'recurring-task'
  }
);

// Update an existing task
ReactNativeForegroundService.update_task(
  () => console.log('Updated task'),
  {
    taskId: 'recurring-task',
    delay: 10000 // Change to 10 seconds
  }
);

// Remove a task
ReactNativeForegroundService.remove_task('recurring-task');

// Check if a task is running
const isRunning = ReactNativeForegroundService.is_task_running('recurring-task');

// Get task details
const task = ReactNativeForegroundService.get_task('recurring-task');

// Get all tasks
const allTasks = ReactNativeForegroundService.get_all_tasks();

// Remove all tasks
ReactNativeForegroundService.remove_all_tasks();
```

### Update Notification

```javascript
await ReactNativeForegroundService.update({
  id: 144,
  title: 'Download Progress',
  message: 'Downloaded 50 of 100 files',
  progress: {
    max: 100,
    curr: 50
  }
});
```

### Handle Notification Interactions

```javascript
import { useEffect } from 'react';
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

function MyComponent() {
  useEffect(() => {
    const cleanup = ReactNativeForegroundService.eventListener((data) => {
      console.log('Notification clicked:', data);

      if (data.main === 'open') {
        // Handle main notification click
        navigateToScreen('Home');
      }

      if (data.button === 'pause') {
        // Handle pause button click
        pauseDownload();
      }

      if (data.button === 'cancel') {
        // Handle cancel button click
        cancelDownload();
        ReactNativeForegroundService.stop();
      }
    });

    return cleanup; // Cleanup listener on unmount
  }, []);

  return <YourComponent />;
}
```

### Stop the Service

```javascript
// Stop the service (decrements internal counter)
await ReactNativeForegroundService.stop();

// Force stop regardless of start counter
await ReactNativeForegroundService.stopAll();
```

### Check Service Status

```javascript
const isRunning = ReactNativeForegroundService.is_running();
console.log('Service running:', isRunning);
```

### Cancel Individual Notifications

```javascript
// Cancel a specific notification by ID
await ReactNativeForegroundService.cancel_notification(144);
```

### Check Permissions and Service State (NEW in v3.0.0)

```javascript
// Check if POST_NOTIFICATIONS permission is granted (Android 13+)
const hasNotificationPermission = await ReactNativeForegroundService.checkNotificationPermission();

if (!hasNotificationPermission) {
  // Request permission
  await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
  );
}

// Check if app can start foreground service (Android 12+)
// Returns false if app is in background on Android 12+
const canStart = await ReactNativeForegroundService.canStartForegroundService();

if (!canStart) {
  Alert.alert(
    'Cannot Start Service',
    'Please open the app to start the background service'
  );
  return;
}

// Now safe to start the service
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Service',
  message: 'Running...'
});
```

## API Reference

### Methods

#### `register()`
Registers the foreground service headless task. Must be called before any other methods.

#### `start(config)`
Starts the foreground service with a notification.

**Parameters:**
- `id` (number, required): Unique notification ID
- `title` (string): Notification title
- `message` (string): Notification message
- `serviceType` (string, **NEW in v3.0.0**): Service type - one of: `'dataSync'` (default), `'location'`, `'mediaPlayback'`, `'camera'`, `'microphone'`, `'phoneCall'`, `'connectedDevice'`, `'health'`, `'mediaProjection'`, `'remoteMessaging'`, `'shortService'`, `'specialUse'`, `'systemExempted'`
- `icon` (string): Small icon resource name (default: 'ic_notification')
- `largeIcon` (string): Large icon resource name (default: 'ic_launcher')
- `importance` (string): 'none' | 'min' | 'low' | 'default' | 'high' | 'max'
- `visibility` (string): 'private' | 'public' | 'secret'
- `number` (string): Badge counter for supported devices
- `button` (boolean): Show first action button
- `buttonText` (string): First button text
- `buttonOnPress` (string): First button event identifier
- `button2` (boolean): Show second action button
- `button2Text` (string): Second button text
- `button2OnPress` (string): Second button event identifier
- `mainOnPress` (string): Main notification click event identifier
- `progress` (object): `{ max: number, curr: number }` for progress bar
- `color` (string): Notification color (hex)
- `setOnlyAlertOnce` (boolean): Alert only once for this notification

#### `update(config)`
Updates the notification of a running service. Same parameters as `start()`.

#### `stop()`
Stops the foreground service (decrements internal counter).

#### `stopAll()`
Force stops the service regardless of start counter.

#### `is_running()`
Returns boolean indicating if the service is running.

#### `add_task(task, config)`
Adds a new task to be executed by the service.

**Parameters:**
- `task` (function): Function to execute
- `config.delay` (number): Delay before first execution (ms)
- `config.onLoop` (boolean): Whether to repeat the task
- `config.taskId` (string): Unique task identifier
- `config.onSuccess` (function): Success callback
- `config.onError` (function): Error callback

**Returns:** Task ID string

#### `update_task(task, config)`
Updates an existing task. Same parameters as `add_task()`.

#### `remove_task(taskId)`
Removes a task by ID.

#### `is_task_running(taskId)`
Returns boolean indicating if a task exists.

#### `get_task(taskId)`
Returns task object for given ID.

#### `get_all_tasks()`
Returns object containing all tasks.

#### `remove_all_tasks()`
Removes all tasks.

#### `cancel_notification(id)`
Cancels a notification by ID.

#### `eventListener(callback)`
Listens for notification interaction events.

**Returns:** Cleanup function to remove the listener

#### `checkNotificationPermission()` (**NEW in v3.0.0**)
Checks if POST_NOTIFICATIONS permission is granted (Android 13+).

**Returns:** Promise<boolean> - true if permission granted or not required (Android 12 and below), false otherwise

**Example:**
```javascript
const hasPermission = await ReactNativeForegroundService.checkNotificationPermission();
if (!hasPermission) {
  // Request permission
  await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
}
```

#### `canStartForegroundService()` (**NEW in v3.0.0**)
Checks if app can currently start a foreground service (Android 12+). On Android 12+, apps cannot start foreground services from the background.

**Returns:** Promise<boolean> - true if can start, false if app is in background (Android 12+) or other restriction applies

**Example:**
```javascript
const canStart = await ReactNativeForegroundService.canStartForegroundService();
if (!canStart) {
  Alert.alert('Cannot start service from background');
  return;
}
await ReactNativeForegroundService.start({...});
```

## Examples

### Download Manager

```javascript
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

class DownloadManager {
  constructor() {
    this.downloads = [];
  }

  async startDownload(url, filename) {
    // Start foreground service
    await ReactNativeForegroundService.start({
      id: 1,
      title: 'Download Manager',
      message: `Downloading ${filename}`,
      serviceType: 'dataSync', // Specify service type for data operations
      button: true,
      buttonText: 'Cancel',
      buttonOnPress: 'cancel_download',
      progress: { max: 100, curr: 0 }
    });

    // Add download task
    ReactNativeForegroundService.add_task(
      async () => {
        const progress = await this.downloadChunk(url);

        // Update notification with progress
        await ReactNativeForegroundService.update({
          id: 1,
          title: 'Download Manager',
          message: `Downloading ${filename}`,
          progress: { max: 100, curr: progress }
        });

        if (progress >= 100) {
          ReactNativeForegroundService.remove_task('download_task');
          await ReactNativeForegroundService.stop();
        }
      },
      {
        delay: 1000,
        onLoop: true,
        taskId: 'download_task'
      }
    );
  }

  async downloadChunk(url) {
    // Your download logic here
    return progress;
  }
}
```

### Location Tracker

```javascript
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';
import Geolocation from '@react-native-community/geolocation';

async function startLocationTracking() {
  await ReactNativeForegroundService.start({
    id: 2,
    title: 'Location Tracking',
    message: 'Tracking your location',
    serviceType: 'location', // Specify location service type
    icon: 'ic_location',
    importance: 'low',
    button: true,
    buttonText: 'Stop Tracking',
    buttonOnPress: 'stop_tracking'
  });

  ReactNativeForegroundService.add_task(
    async () => {
      Geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          console.log('Location:', latitude, longitude);
          // Send to your backend
          sendLocationToServer(latitude, longitude);
        },
        (error) => console.error('Location error:', error)
      );
    },
    {
      delay: 30000, // Every 30 seconds
      onLoop: true,
      taskId: 'location_tracker'
    }
  );
}
```

## Service Types (Android 14+)

Starting with Android 14, you must specify what type of work your foreground service performs.

### How Service Types Work (v3.0.0+)

**Two ways to configure service types:**

1. **Dynamic (Recommended)** - Pass `serviceType` parameter when starting the service:
   ```javascript
   await ReactNativeForegroundService.start({
     id: 1,
     title: 'Service',
     message: 'Running...',
     serviceType: 'location' // Dynamically specify type
   });
   ```

2. **Static** - Configure in AndroidManifest.xml (for apps that only use one type):
   ```xml
   <service
       android:name="com.supersami.foregroundservice.ForegroundService"
       android:foregroundServiceType="location"
       android:exported="false" />
   ```

The postinstall script configures your app with `dataSync|location|mediaPlayback` by default, which covers most use cases. You can override this by passing the `serviceType` parameter.

### Available Service Types

| Type | Permission Required | Use Case |
|------|---------------------|----------|
| **dataSync** | `FOREGROUND_SERVICE_DATA_SYNC` | File downloads, data backup, sync operations |
| **location** | `FOREGROUND_SERVICE_LOCATION` | GPS tracking, geofencing, navigation |
| **mediaPlayback** | `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Music/video players |
| **camera** | `FOREGROUND_SERVICE_CAMERA` | Video recording, camera streaming |
| **microphone** | `FOREGROUND_SERVICE_MICROPHONE` | Audio recording |
| **phoneCall** | `FOREGROUND_SERVICE_PHONE_CALL` | VoIP calls |
| **connectedDevice** | `FOREGROUND_SERVICE_CONNECTED_DEVICE` | Bluetooth, NFC, USB connections |
| **health** | `FOREGROUND_SERVICE_HEALTH` | Fitness tracking, health monitoring |
| **mediaProjection** | `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Screen recording, casting |
| **remoteMessaging** | `FOREGROUND_SERVICE_REMOTE_MESSAGING` | Messaging apps |
| **shortService** | None | Short-duration tasks (< 3 minutes) |
| **specialUse** | `FOREGROUND_SERVICE_SPECIAL_USE` | Special use cases approved by Google |

### Changing Service Type

#### Option 1: Dynamic (Recommended - NEW in v3.0.0)

Simply pass the `serviceType` parameter when starting the service:

```javascript
// For camera usage
await ReactNativeForegroundService.start({
  id: 1,
  title: 'Camera Service',
  message: 'Recording...',
  serviceType: 'camera' // Just pass the type!
});

// For microphone usage
await ReactNativeForegroundService.start({
  id: 2,
  title: 'Audio Recorder',
  message: 'Recording audio...',
  serviceType: 'microphone'
});
```

**Don't forget to add the corresponding permission to AndroidManifest.xml:**

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
<uses-permission android:name="android.permission.CAMERA" /> <!-- Runtime permission also needed -->
```

#### Option 2: Static (Legacy)

Manually edit the service declaration in your `android/app/src/main/AndroidManifest.xml`:

```xml
<service
    android:name="com.supersami.foregroundservice.ForegroundService"
    android:foregroundServiceType="camera"  <!-- Change to your type -->
    android:exported="false" />
```

**Note:** With v3.0.0, you can use **both** approaches. The `serviceType` parameter overrides the manifest declaration, allowing you to dynamically change service types at runtime.

## Troubleshooting

### Service Crashes on Android 14+ with SecurityException

**Error:** `SecurityException: Starting FGS with type dataSync requires permission...`

**Solution:**
1. Add the type-specific permission to your `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
   ```
2. Ensure your app's `targetSdkVersion` is set to 34 in `build.gradle`

### Notification Not Appearing on Android 13+

**Error:** Service starts but no notification appears

**Solution:**
1. Add POST_NOTIFICATIONS permission to AndroidManifest.xml
2. Request runtime permission before starting service (see Setup section 4)

### ForegroundServiceStartNotAllowedException (Android 12+)

**Error:** Cannot start foreground service from background

**Solution:**
- Start the service only when your app is in the foreground
- Or use one of the [exemptions from background restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start#background-start-restriction-exemptions)

### Service Not Starting
- Ensure you called `register()` before `start()`
- Verify AndroidManifest.xml has all required permissions and services
- Check that notification channel is properly configured
- Check logs for specific error messages

### Tasks Not Executing
- Confirm the service is running with `is_running()`
- Check task delay is set correctly (minimum effective interval is 500ms)
- Verify task function doesn't throw unhandled errors

### Old Service Declaration Format

If you're upgrading from v2.x and see warnings during install, update your service declarations:

**Old format (will NOT work on Android 14+):**
```xml
<service android:name="com.supersami.foregroundservice.ForegroundService"></service>
```

**New format (required for Android 14+):**
```xml
<service
    android:name="com.supersami.foregroundservice.ForegroundService"
    android:foregroundServiceType="dataSync|location|mediaPlayback"
    android:exported="false" />
```

## Platform Support

| Platform | Supported Versions | Notes |
|----------|-------------------|-------|
| **Android** | 9.0+ (API 28+) | Full support for all Android versions |
| **Android 14+** | API 34+ | Full service type support |
| **Android 13** | API 33 | POST_NOTIFICATIONS permission required |
| **Android 12** | API 31-32 | Background start restrictions apply |
| **iOS** | Not supported | Foreground services are Android-specific |

**Compatibility Notes:**
- **Recommended:** Target Android 14 (API 34) for best experience
- **Minimum:** Android 9 (API 28) required
- **React Native:** 0.59.0 or higher

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

MIT © Raja Osama (supersami)

Modified by Kiren Paul for internal use.

## Credits

This library is based on and adapted from:
- [react-native-foreground-service](https://github.com/voximplant/react-native-foreground-service) by Voximplant
- [react-native-push-notification](https://github.com/zo0r/react-native-push-notification) by zo0r

Original implementation by [Raja Osama](https://github.com/raja0sama).

## Documentation

- 📖 [Complete API Reference](#api-reference) - All methods and parameters
- 🔄 [Migration Guide](MIGRATION_v2_to_v3.md) - Upgrading from v2.x to v3.0.0
- 📋 [CHANGELOG](CHANGELOG.md) - Version history and release notes
- 🏗️ [Architecture Documentation](ARCHITECTURE.md) - Library architecture and design
- 🚀 [Release Notes](RELEASE_NOTES_v3.0.0.md) - v3.0.0 release details

## Resources

- [Medium Article: React Native Foreground Service](https://medium.com/javascript-in-plain-english/react-native-foreground-service-f7fc8e617fba)
- [Example Project](https://github.com/Raja0sama/ForegroundSerivceExample)
- [NPM Package](https://www.npmjs.com/package/@kirenpaul/rn-foreground-service)
- [GitHub Repository](https://github.com/paulkiren/rn-foreground-service)
- [Android Foreground Services Documentation](https://developer.android.com/develop/background-work/services/foreground-services)
