# Release Notes - v3.0.0

**Release Date:** January 5, 2026
**Package:** `@kirenpaul/rn-foreground-service@3.0.0`

---

## 🎉 What's New

### 🚨 Critical: Android 14+ Support

Version 3.0.0 brings **full Android 14+ compatibility** with foreground service types. If you're targeting Android 14 (API 34) or planning to in 2024, **this update is essential**.

### ✨ Major Features

#### 1. **Foreground Service Types** 🎯
Specify the type of work your service performs. Supports all 11 Android service types:

```javascript
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

// Location tracking
await ReactNativeForegroundService.start({
  id: 1,
  title: "Location Tracking",
  message: "Tracking your route...",
  serviceType: "location" // NEW!
});

// Media playback
await ReactNativeForegroundService.start({
  id: 2,
  title: "Music Player",
  message: "Playing audio...",
  serviceType: "mediaPlayback" // NEW!
});
```

**Available Types:**
- `dataSync` (default) - Background data synchronization
- `location` - Location tracking
- `mediaPlayback` - Audio/video playback
- `camera` - Camera usage
- `microphone` - Audio recording
- `phoneCall` - VoIP calling
- `health` - Health/fitness tracking
- `connectedDevice` - Bluetooth/USB device interaction
- `mediaProjection` - Screen recording
- `remoteMessaging` - Push notifications
- `shortService` - Quick tasks (<3 minutes)
- `specialUse` - Special use cases
- `systemExempted` - System-level services

#### 2. **New Helper Methods** 🛠️

```javascript
// Check notification permission (Android 13+)
const hasPermission = await ReactNativeForegroundService.checkNotificationPermission();
if (!hasPermission) {
  await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
  );
}

// Check if can start service from background (Android 12+)
const canStart = await ReactNativeForegroundService.canStartForegroundService();
if (!canStart) {
  Alert.alert("Please open the app to start the service");
}
```

#### 3. **Full TypeScript Support** 📘

```typescript
import ReactNativeForegroundService, { ForegroundServiceType } from '@kirenpaul/rn-foreground-service';

// Type-safe service type selection
const startService = async (type: ForegroundServiceType) => {
  await ReactNativeForegroundService.start({
    id: 1,
    title: "Service",
    message: "Running...",
    serviceType: type // Autocomplete works!
  });
};
```

#### 4. **Enhanced Error Messages** 📝

Get clear, actionable error messages:

```
❌ Before:
SecurityException: Permission denial

✅ After:
SecurityException: Missing required permissions. On Android 14+, you must declare
the correct foreground service type and request type-specific permissions
(e.g., FOREGROUND_SERVICE_DATA_SYNC). Check your AndroidManifest.xml and
runtime permissions.
```

---

## 📦 Installation

```bash
npm install @kirenpaul/rn-foreground-service@3.0.0
```

or

```bash
yarn add @kirenpaul/rn-foreground-service@3.0.0
```

---

## 🔄 Upgrading from v2.x

### Is This a Breaking Change?

**API Level: NO** - Your JavaScript code works as-is
**Manifest Level: YES** - You must update AndroidManifest.xml

### Quick Migration (3 steps):

**1. Add permissions to AndroidManifest.xml:**
```xml
<manifest>
  <!-- Android 13+ notification permission -->
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

  <!-- Android 14+ service type permission (choose based on your use case) -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
  <!-- OR -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
  <!-- See docs for all types -->
</manifest>
```

**2. Request permission at runtime (Android 13+):**
```javascript
if (Platform.Version >= 33) {
  await PermissionsAndroid.request(
    PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
  );
}
```

**3. Update targetSdkVersion in build.gradle:**
```gradle
android {
    compileSdkVersion 34
    targetSdkVersion 34  // Update to 34
}
```

**📚 Full migration guide:** [MIGRATION_v2_to_v3.md](MIGRATION_v2_to_v3.md)

---

## 🎯 Why This Update Matters

### The Problem
- **Android 14 enforcement:** Apps targeting API 34+ MUST declare service types
- **Crashes on Android 14:** Missing service types cause SecurityException
- **Silent failures:** Services fail to start with unclear errors
- **Poor user experience:** Apps can't use background services effectively

### The Solution (v3.0.0)
- ✅ Automatic service type configuration via postinstall
- ✅ Clear error messages with fixes
- ✅ Proactive permission checks
- ✅ Full Android 9-14+ compatibility
- ✅ TypeScript support for type safety

---

## 🔒 Security Improvements

- Services now marked `android:exported="false"` by default
- Runtime permission validation prevents crashes
- Proactive background state checks
- Version-specific error guidance

---

## 📊 Compatibility

| Android Version | v2.2.0 Status | v3.0.0 Status |
|----------------|---------------|---------------|
| Android 14 (API 34) | ❌ Crashes | ✅ Fully supported |
| Android 13 (API 33) | ⚠️ No notifications | ✅ Fully supported |
| Android 12 (API 31-32) | ✅ Works | ✅ Works (improved) |
| Android 9-11 (API 28-30) | ✅ Works | ✅ Works |

---

## 📚 Documentation

- **[README.md](README.md)** - Complete setup and usage guide
- **[MIGRATION_v2_to_v3.md](MIGRATION_v2_to_v3.md)** - Step-by-step upgrade guide
- **[CHANGELOG.md](CHANGELOG.md)** - Detailed changelog
- **[PHASE_2_SUMMARY.md](PHASE_2_SUMMARY.md)** - Technical implementation details
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Library architecture

---

## 🐛 Bug Fixes

- Fixed crash on Android 14+ due to missing service type declarations
- Fixed notification permission issues on Android 13+
- Fixed background start restrictions on Android 12+
- Improved error messages for all Android version-specific issues

---

## 🙏 Thank You

Special thanks to the Android documentation team and the React Native community for their support in making this release possible.

This release involved:
- 📄 Analysis of 11 Android documentation files (1,239 lines)
- 💻 Implementation across 4 files (Java, JavaScript, TypeScript)
- 📖 Creation of 7 comprehensive documentation files
- ✅ Full backward compatibility maintained

---

## 🚀 What's Next

**Phase 3 (Future):**
- Service type validation (automatic permission checking)
- Timeout handling (6-hour limit tracking)
- Memory leak fixes
- Additional helper methods

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/paulkiren/rn-foreground-service/issues)
- **Documentation:** [README.md](README.md)
- **Migration Help:** [MIGRATION_v2_to_v3.md](MIGRATION_v2_to_v3.md)

---

## ⚡ Quick Start Example

```javascript
import React, { useEffect } from 'react';
import { PermissionsAndroid, Platform } from 'react-native';
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

const App = () => {
  useEffect(() => {
    startForegroundService();
  }, []);

  const startForegroundService = async () => {
    try {
      // 1. Check notification permission (Android 13+)
      if (Platform.Version >= 33) {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
        );
        if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
          console.log('Notification permission denied');
          return;
        }
      }

      // 2. Register task
      ReactNativeForegroundService.register();

      // 3. Start service with type
      await ReactNativeForegroundService.start({
        id: 1,
        title: 'Background Service',
        message: 'Running in background...',
        serviceType: 'dataSync', // Specify service type
        importance: 'high'
      });

      console.log('Service started successfully!');
    } catch (error) {
      console.error('Failed to start service:', error);
    }
  };

  return <YourApp />;
};

export default App;
```

---

**Version:** 3.0.0
**License:** MIT
**Maintainer:** @kirenpaul

**Enjoy the update! 🎉**
