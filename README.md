# EverTranslator

<div align="center">

<img src="materials/mipmap-xxhdpi/icon.png" alt="EverTranslator Icon" width="128" height="128" />

### On-Screen OCR & Real-Time Translation for Android

*An independent, actively maintained open-source fork bringing screen text recognition and instant translation to modern Android devices.*

[![Latest Release](https://img.shields.io/github/v/release/marodriguezd/EverTranslator?style=for-the-badge&color=007ACC&logo=github)](https://github.com/marodriguezd/EverTranslator/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/marodriguezd/EverTranslator/ci.yml?branch=master&style=for-the-badge&logo=github-actions&logoColor=white)](https://github.com/marodriguezd/EverTranslator/actions/workflows/ci.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPL_v3-blue.svg?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%205.0%2B-green.svg?style=for-the-badge&logo=android)](https://developer.android.com)

---

### 📥 [Download Latest Release](https://github.com/marodriguezd/EverTranslator/releases/latest)

</div>

---

## 📖 Overview

**EverTranslator** is a lightweight, floating screen translator for Android. It allows you to select any region of your screen, perform Optical Character Recognition (OCR), and translate text instantly—without switching apps. 

Whether you are playing imported games, reading webtoons, browsing foreign social media, or working with non-selectable text, EverTranslator provides immediate translations right over your active app.

> **Note on Fork & Distribution**:  
> This repository is an independent open-source fork maintained by [@marodriguezd](https://github.com/marodriguezd). It incorporates ongoing enhancements, performance optimizations, and updates to the original codebase. **Releases are distributed exclusively via GitHub Releases**.

---

## ⚡ Features

- 🎯 **On-Screen Area Selection**: Drag and crop any area of your screen for instant OCR extraction.
- 🌐 **Instant Text Translation**: Fast translation powered by reliable OCR engines and translation services.
- 🎈 **Floating Overlay Bubble**: Single-tap floating control for effortless activation across any app.
- 📋 **Clipboard Integration**: Easily copy recognized source text or translated output with a single tap.
- 🎨 **Modern Android UI**: Designed for clean usability, low memory overhead, and smooth gesture controls.
- 🔓 **Fully Open Source & Privacy-Focused**: No ads, no telemetry, and no third-party app store requirements.

---

## 📥 Getting the App

All official builds are generated and published directly via GitHub Actions CI/CD.

### 👉 [Click Here to Access the Latest Release](https://github.com/marodriguezd/EverTranslator/releases/latest)

#### Package Options:
- **`EverTranslator-vX.Y.Z-dev-release.apk`**: Recommended production release build.
- **`EverTranslator-vX.Y.Z-dev-debug.apk`**: Debug build useful for testing or troubleshooting.

> **Installation Tip**: Ensure your Android device allows installing APKs from your browser or file manager ("Install unknown apps" permission).

---

## 📱 System Requirements

- **Operating System**: Android 5.0 (API level 21) or higher.
- **Permissions Required**:
  - `Display over other apps` (`SYSTEM_ALERT_WINDOW`) – Enables the floating overlay interface.
  - `Screen Capture` – Granted on-demand when capturing selected screen regions.

---

## 🖼️ Gallery

<div align="center">

<img src="materials/PlayStore/device-2016-12-08-204259.jpg" width="240px" alt="Screenshot 1" />
<img src="materials/PlayStore/device-2016-12-08-205120.jpg" width="240px" alt="Screenshot 2" />
<img src="materials/PlayStore/device-2016-12-08-205741.jpg" width="240px" alt="Screenshot 3" />

</div>

---

## 🛠️ Building from Source

### Prerequisites
- Java Development Kit (JDK 17)
- Android SDK (API Level 34 / compileSdk 34)
- Gradle 8.x+ (handled via included `./gradlew` wrapper)

### Build Steps (Command Line)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/marodriguezd/EverTranslator.git
   cd EverTranslator
   ```

2. **Configure SDK Path**:
   Create a `local.properties` file in the root directory (or open in Android Studio to auto-generate):
   ```properties
   sdk.dir=/path/to/your/android-sdk
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew clean assembleDevDebug
   ```
   *Output APK location*: `main/build/outputs/apk/dev/debug/main-dev-debug.apk`

4. **Build Release APK**:
   ```bash
   ./gradlew assembleDevRelease
   ```
   *Output APK location*: `main/build/outputs/apk/dev/release/main-dev-release.apk`

---

## 🤖 CI/CD Pipeline & Automated Releases

This project utilizes GitHub Actions for continuous integration and automated releases:

- **Continuous Integration (`.github/workflows/ci.yml`)**:  
  Automatically builds and tests every push or pull request to `master`. Debug APKs are preserved as workflow artifacts.

- **Automated Releases (`.github/workflows/release.yml`)**:  
  Triggered automatically on new version tags (`v*`) or manually via **Workflow Dispatch**. Generates signed, installable APKs directly attached to the corresponding [GitHub Release](https://github.com/marodriguezd/EverTranslator/releases).

- **Custom Release Keystore (GitHub Secrets)**:  
  Configure `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` in repository secrets for custom signing. If unconfigured, GitHub Actions generates a valid self-signed release keystore automatically.

---

## 🤝 Support & Issues

If you encounter bugs, have suggestions, or want to contribute:
- Check existing issues or submit a new report on the [GitHub Issue Tracker](https://github.com/marodriguezd/EverTranslator/issues).

---

## 📜 License

EverTranslator is open-source software licensed under the **[GNU General Public License v3.0 (GPL-3.0)](LICENSE)**.
