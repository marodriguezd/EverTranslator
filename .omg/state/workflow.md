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
   - Upgrade Material design library dependency in `main/build.gradle`.
   - Implement M3 theme parents (`Theme.Material3.DayNight.NoActionBar` / `Theme.Material3.DayNight`).
   - Setup Material 3 dynamic color tokens (Light & Dark mode palettes) in XML color resources.
   - Apply dynamic colors dynamically on Android 12+ (API 31+) in `CoreApplication` / `LaunchActivity`.
3. **Source Code Optimization & Refactoring**:
   - Refactor Kotlin singletons / repositories / utils using modern idiomatic Kotlin features.
   - Optimize BitmapCache & coroutine scopes.
   - Clean up deprecated APIs and unused imports.
4. **Verification & CI**:
   - Commit & push changes to remote repository `exp/material3-2026-optimization`.
   - Verify build status via `gh run list` / `gh run view`.
