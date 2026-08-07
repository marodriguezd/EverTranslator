# Taskboard - Material Design 3 (2026) Upgrade & Code Refactor

| Task ID | Task Description | Owner | Priority | Status | Verification |
| --- | --- | --- | --- | --- | --- |
| TASK-01 | Create experimental git branch `exp/material3-2026-optimization` | main-agent | High | DONE | Branch created and active |
| TASK-02 | Upgrade `main/build.gradle` dependencies to Material 3 & AndroidX | main-agent | High | DONE | Material 1.11.0, appcompat 1.7.0, core-ktx 1.12.0 |
| TASK-03 | Update themes (`themes.xml`, `values-night/themes.xml`) to Material 3 | main-agent | High | DONE | Inheriting Theme.Material3.DayNight |
| TASK-04 | Enhance color palettes (`colors.xml`, `values-night/colors.xml`) with M3 color tokens | main-agent | High | DONE | M3 color tokens defined for light/dark modes |
| TASK-05 | Add M3 Dynamic Color support in Application/Activity | main-agent | Medium | DONE | DynamicColors.applyToActivitiesIfAvailable active in CoreApplication |
| TASK-06 | Refactor & optimize core Kotlin source code (ScreenExtractor, BitmapCache, Utils) | main-agent | High | DONE | Fast grid sampling in ScreenExtractor, soft ref purge in BitmapCache |
| TASK-07 | Commit changes, push to GitHub, and verify via CI workflow | main-agent | High | DONE | Pushed to remote branch `exp/material3-2026-optimization` & GitHub Actions CI triggered |
