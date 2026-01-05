# Migration Guide: v2.x → v3.0

**Library:** @kirenpaul/rn-foreground-service
**Migration Difficulty:** Medium
**Estimated Time:** 15-30 minutes
**Required for:** Android 14+ compatibility

---

## Overview

Version 3.0 brings critical updates for Android 14+ compatibility. While the API remains largely the same, there are new setup requirements that **must** be completed.

### What's New in v3.0?
- ✅ Android 14+ foreground service type support
- ✅ Android 13+ POST_NOTIFICATIONS permission handling
- ✅ Enhanced security with `exported="false"`
- ✅ Improved postinstall script with better error messages
- ✅ Better TypeScript support (coming in v3.1)

### Breaking Changes
- ⚠️ **AndroidManifest.xml requires new permissions** (critical)
- ⚠️ **Runtime permission request required on Android 13+** (critical)
- ⚠️ **targetSdkVersion 34 recommended** (important)

---

## Prerequisites

Before starting, ensure:
- ✅ You have backup of your code
- ✅ Your React Native version is ≥ 0.59.0
- ✅ You can test on Android 13+ and 14+ devices

---

## Step-by-Step Migration

### Step 1: Update the Library

```bash
npm install @kirenpaul/rn-foreground-service@latest
# or
yarn upgrade @kirenpaul/rn-foreground-service@latest
```

The postinstall script will run automatically and:
- Update your service declarations with `foregroundServiceType`
- Add `exported="false"` for security
- Show warnings if you have old format

**Expected Output:**
```
📱 @kirenpaul/rn-foreground-service - Post-install setup
================================================

✅ Successfully configured AndroidManifest.xml
   - Added ForegroundService with types: dataSync|location|mediaPlayback
   - Added ForegroundServiceTask
   - Added notification metadata

⚠️  IMPORTANT: Android 14+ Requirements
   You must add these permissions to your AndroidManifest.xml...
```

---

### Step 2: Update AndroidManifest.xml Permissions

**Location:** `android/app/src/main/AndroidManifest.xml`

#### 2a. Add POST_NOTIFICATIONS Permission (Android 13+)

Add this permission after existing `FOREGROUND_SERVICE` permission:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

  <!-- Existing permissions -->
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
  <uses-permission android:name="android.permission.WAKE_LOCK" />

  <!-- NEW: Add this for Android 13+ -->
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

  <!-- ... rest of manifest -->
</manifest>
```

#### 2b. Add Type-Specific Permission (Android 14+)

Add the permission that matches your use case:

**For data sync / file operations / network requests:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

**For location tracking:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

**For media playback:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
```

**Need multiple types?** Add all required permissions:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

#### 2c. Verify Service Declarations

Check that your service declarations look like this (should be auto-updated by postinstall):

```xml
<application>
  <!-- Your existing meta-data and config... -->

  <!-- Should look like this (NEW format) -->
  <service
      android:name="com.supersami.foregroundservice.ForegroundService"
      android:foregroundServiceType="dataSync|location|mediaPlayback"
      android:exported="false" />
  <service
      android:name="com.supersami.foregroundservice.ForegroundServiceTask"
      android:exported="false" />
</application>
```

**⚠️ If your services still look like this (OLD format):**
```xml
<service android:name="com.supersami.foregroundservice.ForegroundService"></service>
<service android:name="com.supersami.foregroundservice.ForegroundServiceTask"></service>
```

**You must manually update them!** Delete the old lines and add the new format above.

---

### Step 3: Request Runtime Permission (Android 13+)

Add this code to request notification permission **before** starting the service:

#### 3a. Create Permission Helper

Add this helper function to your app:

```javascript
// utils/permissions.js
import { PermissionsAndroid, Platform } from 'react-native';

export async function requestNotificationPermission() {
  if (Platform.OS !== 'android') {
    return true; // iOS doesn't need this
  }

  // Android 13+ requires runtime permission
  if (Platform.Version >= 33) {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
        {
          title: 'Notification Permission',
          message: 'This app needs permission to show service notifications.',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        }
      );

      return granted === PermissionsAndroid.RESULTS.GRANTED;
    } catch (err) {
      console.warn('Permission request error:', err);
      return false;
    }
  }

  // Android 12 and below don't need runtime permission
  return true;
}
```

#### 3b. Update Your Service Start Code

**BEFORE (v2.x):**
```javascript
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';

// Started service directly
await ReactNativeForegroundService.start({
  id: 144,
  title: 'Service Running',
  message: 'Doing background work...',
});
```

**AFTER (v3.0):**
```javascript
import ReactNativeForegroundService from '@kirenpaul/rn-foreground-service';
import { requestNotificationPermission } from './utils/permissions';

async function startMyService() {
  // NEW: Request permission first on Android 13+
  const hasPermission = await requestNotificationPermission();

  if (!hasPermission) {
    console.log('Notification permission denied. Cannot start service.');
    // Optionally show alert to user
    return;
  }

  // Now safe to start service
  await ReactNativeForegroundService.start({
    id: 144,
    title: 'Service Running',
    message: 'Doing background work...',
  });
}
```

---

### Step 4: Update build.gradle (Recommended)

**Location:** `android/app/build.gradle`

Update your target SDK to 34 for full Android 14 compatibility:

```gradle
android {
    compileSdkVersion 34  // Update from 31 or 33

    defaultConfig {
        applicationId "com.your.app"
        minSdkVersion 21
        targetSdkVersion 34  // Update from 31 or 33
        // ... other config
    }
}
```

**Note:** If you can't update to SDK 34 immediately, the library will still work, but you won't get full Android 14 benefits.

---

### Step 5: Test Your Changes

#### Test Checklist

Run your app on different Android versions and test:

**On Android 14+ Device:**
- [ ] Service starts without SecurityException
- [ ] Notification appears correctly
- [ ] All tasks execute as expected
- [ ] Service can be stopped

**On Android 13 Device:**
- [ ] Permission dialog appears before service start
- [ ] Notification appears after granting permission
- [ ] No notification if permission denied

**On Android 12 Device:**
- [ ] Service starts without permission dialog (no runtime permission needed)
- [ ] Service works from foreground
- [ ] Service fails gracefully when started from background (if applicable)

**On Android 9-11 Devices:**
- [ ] Service works as before (backward compatibility)

---

## API Changes

### No Breaking API Changes!

The JavaScript API remains the same:

```javascript
// All these methods work exactly as before
ReactNativeForegroundService.register();
ReactNativeForegroundService.start(config);
ReactNativeForegroundService.update(config);
ReactNativeForegroundService.stop();
ReactNativeForegroundService.add_task(fn, config);
// ... etc
```

### New Helper Methods (Optional)

While not required, v3.0 may add helper methods in future updates:

```javascript
// Check if notification permission is granted (coming soon)
const hasPermission = await ReactNativeForegroundService.checkNotificationPermission();

// Check if can start service from current state (coming soon)
const canStart = await ReactNativeForegroundService.canStartForegroundService();
```

---

## Common Issues During Migration

### Issue 1: SecurityException on Android 14+

**Error:**
```
SecurityException: Starting FGS with type dataSync requires permission
android.permission.FOREGROUND_SERVICE_DATA_SYNC
```

**Solution:**
Add the missing permission to your AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

---

### Issue 2: Notification Doesn't Appear on Android 13+

**Symptom:** Service starts but no notification visible

**Solution:**
1. Check you added `POST_NOTIFICATIONS` permission to manifest
2. Check you're requesting runtime permission before starting service
3. Use this code to verify:
   ```javascript
   const result = await PermissionsAndroid.check(
     PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
   );
   console.log('Has permission:', result);
   ```

---

### Issue 3: Postinstall Didn't Update Services

**Symptom:** Services still use old format without `foregroundServiceType`

**Solution:**
Manually update your service declarations in `android/app/src/main/AndroidManifest.xml`:

```xml
<!-- Delete old format -->
<service android:name="com.supersami.foregroundservice.ForegroundService"></service>

<!-- Add new format -->
<service
    android:name="com.supersami.foregroundservice.ForegroundService"
    android:foregroundServiceType="dataSync|location|mediaPlayback"
    android:exported="false" />
```

---

### Issue 4: Build Errors After SDK Update

**Error:** Build fails after updating targetSdkVersion to 34

**Solution:**
1. Update other dependencies that may be incompatible
2. Clean and rebuild:
   ```bash
   cd android
   ./gradlew clean
   cd ..
   npx react-native run-android
   ```

---

## Rollback Plan

If you encounter critical issues, you can rollback:

### Quick Rollback to v2.x

```bash
npm install @kirenpaul/rn-foreground-service@2.2.0
# or
yarn add @kirenpaul/rn-foreground-service@2.2.0
```

**⚠️ Warning:** Rollback means your app won't work on Android 14+. Only use temporarily while debugging.

### Restore Old Manifest

If you need to revert changes to AndroidManifest.xml, use git:

```bash
git checkout android/app/src/main/AndroidManifest.xml
```

---

## Getting Help

### Before Asking for Help

1. Check the [Troubleshooting section](./README.md#troubleshooting) in README
2. Verify you completed all migration steps
3. Test on a physical Android device (emulators may behave differently)
4. Check Android Studio Logcat for specific error messages

### Where to Get Help

- **GitHub Issues:** [github.com/paulkiren/rn-foreground-service/issues](https://github.com/paulkiren/rn-foreground-service/issues)
- **Stack Overflow:** Tag with `react-native` and `android-foreground-service`

### When Reporting Issues

Please include:
- Library version: `3.0.0`
- React Native version
- Android version where issue occurs
- Complete error message from Logcat
- Relevant code snippets
- AndroidManifest.xml permissions section

---

## Migration Checklist

Use this checklist to ensure you've completed everything:

### Required Steps
- [ ] Updated library to v3.0.0
- [ ] Added `POST_NOTIFICATIONS` permission to AndroidManifest.xml
- [ ] Added type-specific permission (e.g., `FOREGROUND_SERVICE_DATA_SYNC`)
- [ ] Verified service declarations have `foregroundServiceType` attribute
- [ ] Verified services have `android:exported="false"`
- [ ] Added runtime permission request before starting service
- [ ] Tested on Android 14+ device
- [ ] Tested on Android 13 device (permission flow)

### Optional but Recommended
- [ ] Updated targetSdkVersion to 34
- [ ] Updated compileSdkVersion to 34
- [ ] Added permission helper function
- [ ] Tested on Android 12 device (background restrictions)
- [ ] Tested on older Android devices (9-11)
- [ ] Reviewed all service type options

---

## Summary

**Time to Complete:** 15-30 minutes
**Difficulty:** Medium (mostly manifest updates)
**Breaking Changes:** Manifest permissions and runtime permission request required

### Key Changes
1. **Manifest:** Add POST_NOTIFICATIONS and type-specific permissions
2. **Code:** Request runtime permission before starting service on Android 13+
3. **Build:** Update to targetSdkVersion 34 (recommended)

### No Code Changes Required For
- Existing service start/stop logic
- Task management
- Notification updates
- Event listeners

---

**Questions?** Open an issue on GitHub or check the README for more examples!

**Ready for v3.0?** Follow the steps above and you'll be Android 14+ compatible in no time! 🚀
