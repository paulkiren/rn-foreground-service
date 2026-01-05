# Changelog

All notable changes to this project will be documented in this file.

# 3.0.0 (2026-01-05)

## 🚀 Major Features

### Android 14+ Compatibility
* **Full Android 14+ support with foreground service types** - Critical update for apps targeting API 34+
* Support for all 11 Android foreground service types:
  - `dataSync` (default)
  - `location`
  - `mediaPlayback`
  - `camera`
  - `connectedDevice`
  - `health`
  - `mediaProjection`
  - `microphone`
  - `phoneCall`
  - `remoteMessaging`
  - `shortService`
  - `specialUse`
  - `systemExempted`

### New API Features
* **`serviceType` parameter** - Specify foreground service type when starting service
* **`checkNotificationPermission()`** - Check POST_NOTIFICATIONS permission (Android 13+)
* **`canStartForegroundService()`** - Check if service can be started from current state (Android 12+)
* **Enhanced error messages** - Version-specific error messages with actionable guidance

### Developer Experience
* **Full TypeScript support** - Complete type definitions with `ForegroundServiceType` enum
* **Automatic service type injection** - postinstall script configures Android 14+ compatible service declarations
* **Comprehensive documentation** - README, migration guide, and architecture docs

## 🔧 Improvements

### Android Native Layer
* Enhanced `ForegroundService.java`:
  - `getServiceType()` method for mapping service types to Android constants
  - Version-specific `startForeground()` calls (Android 9-14+)
  - Better error handling and logging
* Enhanced `ForegroundServiceModule.java`:
  - POST_NOTIFICATIONS permission check for Android 13+
  - Improved IllegalStateException handling for Android 12+ background restrictions
  - Improved SecurityException handling for Android 14+ service type requirements
  - Two new React Native methods: `checkNotificationPermission()` and `canStartForegroundService()`

### JavaScript/TypeScript Layer
* Updated `index.js`:
  - `serviceType` parameter with default value "dataSync"
  - Exposed new helper methods
  - Backward compatible - no breaking changes
* Updated `index.d.ts`:
  - `ForegroundServiceType` type for autocomplete
  - Full JSDoc documentation
  - Type-safe API

### Build & Installation
* Enhanced `postinstall.js`:
  - Automatic injection of Android 14+ service declarations
  - Service type support in manifest
  - `android:exported="false"` for security
  - Better console output and error messages

## 📚 Documentation

* **MIGRATION_v2_to_v3.md** - Step-by-step upgrade guide for existing users
* **ARCHITECTURE.md** - Complete library architecture documentation
* **GAP_ANALYSIS.md** - Detailed analysis of Android requirements vs implementation
* **GAP_ANALYSIS_REVIEW.md** - Verification of gap analysis accuracy
* **UPGRADE_PLAN.md** - 4-phase implementation plan
* **PHASE_2_SUMMARY.md** - Detailed Phase 2 implementation documentation
* **UPGRADE_SUMMARY.md** - Overall upgrade summary
* **README.md updates**:
  - Android 14+ requirements section
  - Runtime permission examples
  - All 11 service types documented
  - Android version-specific troubleshooting
  - build.gradle update instructions

## ⚠️ Breaking Changes

**None** - Fully backward compatible!

* `serviceType` parameter is optional with default value "dataSync"
* Existing code works without modification
* New helper methods are additive
* All Android versions still supported (API 28+)

## 📋 Migration Required

While the API is backward compatible, users must make manifest changes:

1. **Add to AndroidManifest.xml:**
   ```xml
   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
   ```

2. **Request permission at runtime (Android 13+):**
   ```javascript
   await PermissionsAndroid.request(
     PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
   );
   ```

3. **Update build.gradle:**
   ```gradle
   targetSdkVersion 34
   ```

See [MIGRATION_v2_to_v3.md](MIGRATION_v2_to_v3.md) for complete migration instructions.

## 🔒 Security

* Services now marked with `android:exported="false"` for better security
* Runtime permission checks prevent permission-related crashes
* Proactive background state validation

## 🐛 Bug Fixes

* Fixed crash on Android 14+ due to missing service type declarations
* Fixed notification permission issues on Android 13+
* Fixed background start restrictions on Android 12+
* Improved error messages for all Android version-specific issues

## 📊 Compatibility

| Android Version | Status |
|----------------|--------|
| Android 14+ (API 34) | ✅ Fully supported |
| Android 13 (API 33) | ✅ Fully supported |
| Android 12 (API 31-32) | ✅ Fully supported |
| Android 9-11 (API 28-30) | ✅ Fully supported |

## 🙏 Acknowledgments

This release was made possible by comprehensive analysis of Android documentation:
- [Foreground Service Types](https://developer.android.com/develop/background-work/services/fg-service-types)
- [Background Start Restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- [Troubleshooting Foreground Services](https://developer.android.com/develop/background-work/services/fgs/troubleshoot)

---

# 2.2.0 (2025-12-17)


### Bug Fixes

* update package-lock.json to sync with package.json dependencies ([4e92261](https://github.com/paulkiren/rn-foreground-service/commit/4e92261a526d66fbc03dfaf17776e90b898edf66))


### Features

* Adding feature to change the color of the icon ([0ea1d02](https://github.com/paulkiren/rn-foreground-service/commit/0ea1d02cf9372b4ee7ae39011b6b69bc8a15440b))
* Progress Bar Support added ([7d21bdd](https://github.com/paulkiren/rn-foreground-service/commit/7d21bdde5ae301389ba34afb107f7a972b2c661b))
