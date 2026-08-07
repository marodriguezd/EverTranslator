# Workflow State - EverTranslator Material Design 3 (2026) Upgrade & Refactoring

## Mode
- Mode: `goal`
- Max Cycles: 5
- Current Cycle: 1
- Assumed Approval: Routine non-destructive refactoring and Material 3 migration on experimental branch `exp/material3-2026-optimization`.

## Strategic Objective
Create an experimental branch (`exp/material3-2026-optimization`), refactor core codebase (modern Kotlin coroutines, clean architecture, performance optimizations), upgrade dependencies and themes to Material Design 3 (2026 specifications), and verify CI workflow status.

## Workflow Execution Steps
1. **Branch Setup**: Branch `exp/material3-2026-optimization` created and checked out.
2. **Material 3 Theme & Palette Upgrade**:
   - Upgraded Material design library dependency in `main/build.gradle` (v1.11.0).
   - Implemented M3 theme parents (`Theme.Material3.DayNight.NoActionBar` / `Theme.Material3.DayNight`).
   - Setup Material 3 dynamic color tokens (Light & Dark mode palettes) in XML color resources.
   - Applied dynamic colors dynamically on Android 12+ (`DynamicColors.applyToActivitiesIfAvailable`) in `CoreApplication.kt`.
3. **Source Code Optimization & Refactoring**:
   - Refactored `ScreenExtractor.kt` to use fast 32x grid sampling and row-buffer `getPixels` for black screen detection (eliminates JNI overhead).
   - Cleaned up `BitmapCache.kt` to purge garbage-collected / recycled `SoftReference` instances during lookup.
   - Updated `Utils.kt` with `PackageManager.PackageInfoFlags` compatibility for Android 13+ (API 33+).
   - Upgraded layout buttons to `MaterialButton`.
4. **Verification & CI**:
   - Committed and pushed changes to remote branch `exp/material3-2026-optimization`.
   - Updated `.github/workflows/ci.yml` to run CI on `exp/**` branches.
   - GitHub Actions CI workflow triggered and active.
