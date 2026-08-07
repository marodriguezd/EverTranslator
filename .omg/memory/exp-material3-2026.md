# Memory Note: Material Design 3 (2026) Upgrade & Performance Optimization

- Date: 2026-08-07
- Branch: `exp/material3-2026-optimization`
- GitHub Actions CI Run: `31162496090` (SUCCESS - `main-dev-debug.apk`)

## Applied Changes
1. **Material Design 3**:
   - `main/build.gradle`: Upgraded `com.google.android.material:material:1.11.0`, `core-ktx:1.12.0`, `appcompat:1.7.0`. Updated `compileSdk` to 34 and `targetSdkVersion` to 34.
   - `themes.xml` & `values-night/themes.xml`: Inherits `Theme.Material3.DayNight` and `Theme.Material3.DayNight.NoActionBar`.
   - `colors.xml` & `values-night/colors.xml`: Full M3 light/dark color tokens.
   - `CoreApplication.kt`: Added `DynamicColors.applyToActivitiesIfAvailable(this)`.

2. **Performance Optimization**:
   - `ScreenExtractor.kt`: `Bitmap.isWholeBlack()` uses 32x step grid sampling to detect non-black pixels in O(1) time without full JNI bitmap scan overhead.
   - `BitmapCache.kt`: `getBitmapFromReusableSet()` purges stale or null `SoftReference` entries during set traversal.
   - `Utils.kt`: `getPackageInfo` modern SDK 33+ compatibility check.

3. **CI Pipeline**:
   - `.github/workflows/ci.yml`: Added `exp/**` pattern to push/pull_request triggers.
