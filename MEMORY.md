# Repository Memory - EverTranslator

## Active Experimental Branches
- **`exp/material3-2026-optimization`**: Experimental branch created on 2026-08-07.
  - Upgraded to Material Design 3 (Material You 2026 specs) & `compileSdk 34`.
  - Implemented M3 themes (`Theme.Material3.DayNight`) and dynamic color palettes (`CoreApplication.kt`).
  - Performance optimizations in `ScreenExtractor.kt` (fast 32x grid sampling for screen capture) and `BitmapCache.kt` (soft reference recycling).

## CI/CD Pipeline
- `.github/workflows/ci.yml` triggers on `master`, `dep/**`, and `exp/**`.
- Generates `main-dev-debug.apk` artifact on GitHub Actions.
- Environmental rule: Heavy local Gradle builds are prohibited on mobile/embedded host ARM64; verification is strictly executed on GitHub Actions via `gh` CLI.
