# @kirenpaul/rn-foreground-service

A powerful React Native library for running Android foreground services with headless task management, customizable notifications, and notification interactions.

[![NPM Version](https://img.shields.io/npm/v/@kirenpaul/rn-foreground-service.svg)](https://www.npmjs.com/package/@kirenpaul/rn-foreground-service)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)

## Overview

This library enables React Native applications to run foreground services on Android with persistent notifications. It's perfect for applications that need to perform background operations while keeping the user informed through a persistent notification.

### Key Features

- Run persistent foreground services on Android
- Execute multiple headless tasks concurrently
- Customizable notifications with up to 2 action buttons
- Progress bar support in notifications
- Custom notification colors and icons
- Handle notification and button click events
- Comprehensive task management API
- Automatic setup via postinstall script
- TypeScript support included

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

The postinstall script will automatically configure your Android files, but you may need to verify the setup manually.

## Setup

### 1. AndroidManifest.xml

Add the required permissions and service declarations to `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest
  xmlns:android="http://schemas.android.com/apk/res/android"
  package="com.your.app"
>
  <!-- Required Permissions -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
  <uses-permission android:name="android.permission.WAKE_LOCK" />

  <application>
    <!-- Notification Channel Configuration -->
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

    <!-- Service Declarations -->
    <service android:name="com.supersami.foregroundservice.ForegroundService" />
    <service android:name="com.supersami.foregroundservice.ForegroundServiceTask" />
  </application>
</manifest>
```

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

// Basic usage
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Service Running',
  message: 'Your app is running in the background',
});

// With custom configuration
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Download Manager',
  message: 'Downloading files...',
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

## Troubleshooting

### Service Not Starting
- Ensure you called `register()` before `start()`
- Verify AndroidManifest.xml has all required permissions and services
- Check that notification channel is properly configured

### Tasks Not Executing
- Confirm the service is running with `is_running()`
- Check task delay is set correctly (minimum effective interval is 500ms)
- Verify task function doesn't throw unhandled errors

### Notification Not Appearing
- Check notification importance level
- Verify icon resources exist in your Android project
- Ensure colors.xml is properly configured

### App Crashes on Install
- Make sure postinstall script ran successfully
- Manually verify AndroidManifest.xml configuration
- Check that MainActivity.java is properly updated

## Platform Support

- Android: 5.0 (API 21) and above
- iOS: Not supported (foreground services are an Android-specific feature)

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

## Resources

- [Medium Article: React Native Foreground Service](https://medium.com/javascript-in-plain-english/react-native-foreground-service-f7fc8e617fba)
- [Example Project](https://github.com/Raja0sama/ForegroundSerivceExample)
- [NPM Package](https://www.npmjs.com/package/@kirenpaul/rn-foreground-service)
- [GitHub Repository](https://github.com/paulkiren/rn-foreground-service)
