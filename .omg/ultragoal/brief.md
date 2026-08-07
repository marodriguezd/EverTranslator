# Ultragoal Brief: Fix Android 14 MediaProjection Crash on Experimental Branch

## Objective
Investigate and resolve the force-close crash occurring when requesting screen capture (MediaProjection) permissions on the experimental Material Design 3 / targetSdk 34 branch (`exp/material3-2026-optimization`).

## Root Cause Analysis
1. In `exp/material3-2026-optimization`, `compileSdk` and `targetSdkVersion` were updated to `34` for Material 3.
2. Android 14 (API level 34 / targetSdk 34) introduced strict Foreground Service MediaProjection rules:
   - `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />` MUST be declared in `AndroidManifest.xml`.
   - `ViewHolderService` must be started via `ContextCompat.startForegroundService(...)` and call `startForeground()` with `ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION` immediately upon startup before `MediaProjectionManager.getMediaProjection(...)` is called.
3. Without this permission and proper service start on targetSdk 34, Android throws a fatal `SecurityException`, resulting in the reported force-close crash.

## Plan
1. Add `FOREGROUND_SERVICE_MEDIA_PROJECTION` permission to `AndroidManifest.xml`.
2. Update `ViewHolderService` start logic to use `ContextCompat.startForegroundService` and guarantee immediate `startForeground` execution.
3. Verify Android 14 FGS compatibility, commit changes to `exp/material3-2026-optimization`, and trigger CI build on GitHub Actions.
