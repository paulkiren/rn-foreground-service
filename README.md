# React Native Foreground Service

A modern React Native module to run services in foreground for Android. Supports Android 5.0+ through Android 14, including proper notification permission handling for Android 13+.

[![npm version](https://badge.fury.io/js/@kirenpaul%2Frn-foreground-service.svg)](https://badge.fury.io/js/@kirenpaul%2Frn-foreground-service)

## Features

- Run background tasks while the app is closed
- Create persistent notifications that keep your service alive
- Support for all modern Android versions (5.0-14+)
- Proper notification channel management
- Background task scheduling and management
- Typings support (TypeScript)
- Support for the new React Native architecture
- Configurable foreground service types for Android 10+
- Proper notification permission handling for Android 13+

## Installation

```bash
npm install @kirenpaul/rn-foreground-service --save
# or
yarn add @kirenpaul/rn-foreground-service
```

### Android Setup

1. Add the following permissions to your `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Basic foreground service permission (required for all Android versions) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    
    <!-- Required for Android 13+ -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <!-- Choose relevant permissions based on your foreground service type -->
    <!-- For location-based foreground service (Android 10+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <!-- For camera-based foreground service (Android 10+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
    <!-- For microphone-based foreground service (Android 10+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <!-- For media playback foreground service (Android 10+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <!-- For data sync foreground service (Android 10+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
</manifest>
```

2. Make sure your app is using the latest React Native modules:

```gradle
// android/app/build.gradle
android {
    compileSdkVersion 34
    buildToolsVersion "34.0.0"
    
    defaultConfig {
        // Your settings...
        minSdkVersion 21
        targetSdkVersion 34
        // ...
    }
    // ...
}
```

## Usage

### Basic Example

```javascript
import React, { useEffect } from 'react';
import { View, Button, PermissionsAndroid, Platform } from 'react-native';
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

const App = () => {
  
  // Request notification permissions (required for Android 13+)
  const requestPermissions = async () => {
    if (Platform.OS !== 'android') return true;
    
    if (Platform.Version >= 33) {
      const permission = PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS;
      const hasPermission = await PermissionsAndroid.check(permission);
      
      if (!hasPermission) {
        const status = await PermissionsAndroid.request(permission);
        return status === 'granted';
      }
      return true;
    }
    return true;
  };
  
  useEffect(() => {
    // Register the service
    ReactNativeForegroundService.register();
    
    return () => {
      // Optional: Stop service when component unmounts
      ReactNativeForegroundService.stop();
    };
  }, []);

  const startService = async () => {
    // Ensure we have notification permissions
    const hasPermission = await requestPermissions();
    if (!hasPermission) {
      console.log('Notification permission denied');
      return;
    }
    
    // Create a task
    ReactNativeForegroundService.add_task(
      () => {
        console.log('Task running...');
        // Your background code...
      },
      {
        delay: 1000,
        onLoop: true,
        taskId: 'myTaskId',
        onError: (error) => console.log('Error:', error),
      },
    );

    // Start the service
    await ReactNativeForegroundService.start({
      id: 1444,
      title: 'Foreground Service',
      message: 'Running in background...',
      icon: 'ic_launcher',
      button: true,
      buttonText: 'Click me',
      buttonOnPress: 'button_press_event',
      color: '#4caf50',
      visibility: 'public',
      importance: 'max',
      serviceType: 'datasynch', // Choose from: 'default', 'location', 'camera', 'microphone', 'mediaplayback', 'datasynch'
    });
  };

  const stopService = async () => {
    await ReactNativeForegroundService.stop();
  };

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <Button title="Start Service" onPress={startService} />
      <Button title="Stop Service" onPress={stopService} />
    </View>
  );
};

export default App;
```

## API Reference

### Notification Configuration

| Property | Type | Description | Default |
|----------|------|-------------|---------|
| id | number | Unique notification ID | Required |
| title | string | Notification title | Required |
| message | string | Notification message | Required |
| vibration | boolean | Whether to vibrate | true |
| visibility | 'private' \| 'public' \| 'secret' | Notification visibility level | 'private' |
| icon | string | Small icon name (resource) | 'ic_launcher' |
| largeIcon | string | Large icon name (resource) | 'ic_launcher' |
| importance | 'min' \| 'low' \| 'default' \| 'high' \| 'max' | Notification importance | 'high' |
| number | string | Badge number | undefined |
| button | boolean | Show action button | false |
| buttonText | string | Text for action button | 'Button' |
| buttonOnPress | string | Event name when button pressed | undefined |
| button2 | boolean | Show second action button | false |
| button2Text | string | Text for second action button | 'Button' |
| button2OnPress | string | Event name when second button pressed | undefined |
| mainOnPress | string | Event name when notification pressed | undefined |
| progressBar | boolean | Show progress bar | false |
| progressBarMax | number | Progress bar maximum value | undefined |
| progressBarCurr | number | Progress bar current value | undefined |
| color | string | Notification color (hex) | undefined |
| channelId | string | Custom notification channel ID | 'foreground.service.channel' |
| channelName | string | Custom notification channel name | 'Foreground Service' |
| serviceType | string | The foreground service type (Android 10+) | 'default' |

### Methods

#### `register()`
Registers the foreground service task runner.

#### `start(config: NotificationConfig): Promise<boolean>`
Starts a foreground service with the given notification config.

#### `update(config: NotificationConfig): Promise<boolean>`
Updates an existing foreground service notification.

#### `stop(): Promise<boolean>`
Stops the foreground service.

#### `stopAll(): Promise<boolean>`
Stops all foreground services.

#### `isRunning(): Promise<boolean>`
Checks if the foreground service is running.

#### `addTask(task: Function, options: TaskOptions): string`
Adds a task to be executed in the background. Returns the task ID.

#### `updateTask(task: Function, options: TaskOptions): string`
Updates an existing task. Returns the task ID.

#### `removeTask(taskId: string): void`
Removes a task by ID.

#### `isTaskRunning(taskId: string): boolean`
Checks if a task is currently running.

#### `removeAllTasks(): void`
Removes all background tasks.

#### `getTask(taskId: string): Task | undefined`
Gets a task by ID.

#### `getAllTasks(): Record<string, Task>`
Gets all running tasks.

#### `cancelNotification(id: number): Promise<boolean>`
Cancels a notification by ID.

#### `eventListener(callback: (data: NotificationClickData) => void): Function`
Adds an event listener for notification clicks. Returns a function to remove the listener.

#### `checkNotificationPermission(): Promise<boolean>`
Checks if the app has notification permission (Android 13+ requirement).

## Task Options

| Property | Type | Description | Default |
|----------|------|-------------|---------|
| delay | number | Delay before task execution (ms) | 5000 |
| onLoop | boolean | Whether to repeat the task | false |
| taskId | string | Custom ID for the task | Auto-generated |
| loopDelay | number | Delay between repeat executions | Same as delay |
| onSuccess | Function | Callback for successful execution | undefined |
| onError | Function | Callback for execution errors | undefined |

## Handling Permissions

For Android 13+ (API level 33), you need to request POST_NOTIFICATIONS permission at runtime:

```javascript
import { PermissionsAndroid, Platform } from 'react-native';

const requestNotificationPermission = async () => {
  if (Platform.OS === 'android' && Platform.Version >= 33) {
    const permission = PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS;
    const hasPermission = await PermissionsAndroid.check(permission);
    
    if (!hasPermission) {
      const status = await PermissionsAndroid.request(permission);
      return status === 'granted';
    }
    return true;
  }
  return true;
};

// Check permission before starting service
const startMyService = async () => {
  const hasPermission = await requestNotificationPermission();
  if (hasPermission) {
    await ReactNativeForegroundService.start({...});
  } else {
    console.log('Notification permission denied');
  }
};
```

## Common Issues & Troubleshooting

1. **Service stops when app is killed**  
   This is expected behavior on some Android devices with aggressive battery optimization. Ask users to exclude your app from battery optimization.

2. **Android 13+ doesn't show notifications**  
   Ensure you've requested POST_NOTIFICATIONS permission at runtime.

3. **Android 12+ limitations**  
   Android 12+ has further restrictions on background service starts. Ensure you're starting services from a foreground state.

## Compatibility

- React Native: >=0.60.0
- Android: API 21+ (Android 5.0+)

## License

MIT

## Credits

Original author: Raja Osama (supersami)  
Maintained by: Kiren Paul