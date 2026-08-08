
# EverTranslator 
[![GitHub version](https://badge.fury.io/gh/firemaples%2FEverTranslator.svg)](https://badge.fury.io/gh/firemaples%2FEverTranslator) 
[![CI](https://github.com/firemaples/EverTranslator/actions/workflows/ci.yml/badge.svg)](https://github.com/firemaples/EverTranslator/actions/workflows/ci.yml)

<a href="https://play.google.com/store/apps/details?id=tw.firemaples.onscreenocr">
  <img src="https://play.google.com/intl/en_us/badges/images/badge_new.png" alt="Get it on google play" />
  <img src="https://PlayBadges.pavi2410.me/badge/downloads?id=tw.firemaples.onscreenocr" />
  <img src="https://PlayBadges.pavi2410.me/badge/ratings?id=tw.firemaples.onscreenocr" />
</a>

<img src="materials/mipmap-xxhdpi/icon.png" alt="app icon" align="right" />

Translate any text on screen, even in games!

<a href="https://www.youtube.com/watch?v=Y0OjF-luuDE">Watch usage guide on Youtube</a>

<p>
  <img src="materials/PlayStore/device-2016-12-08-204259.jpg" width="200px" />
  <img src="materials/PlayStore/device-2016-12-08-205120.jpg" width="200px" />
  <img src="materials/PlayStore/device-2016-12-08-205741.jpg" width="200px" />
</p>

## Features

 - **Recognize** any text on the screen.
 - **Translate** the recognized text.
 - **Copy** the recognized text or translated text.
 - **Read out** the text. (temporarily removed)

## Requirement

- Android 5.0 (API level 21) or above.
- Permission of display over other apps. (not suitable for Android Go)

## Get the app

### Download it on Google Play

<a href="https://play.google.com/store/apps/details?id=tw.firemaples.onscreenocr">
  <img src="https://play.google.com/intl/en_us/badges/images/badge_new.png" alt="Get it on google play" />
</a>

### Build from source

#### Environment requirements

- Android SDK
- Android Studio (optional)

#### Produce APK by command line

1. Create a local file `./local.properties` and put your Android SDK path to it as `sdk.dir=path to SDK`, or simplily <a href='#open-project-in-android-stuido'>open the project with Android Studio</a>, it will automatically create the required file for you.
1. Assemble APK file by ```./gradlew clean assembleDevDebug```
1. You can find the APK file on `main/build/outputs/apk/dev/debug/main-dev-debug.apk`
1. Install debug APK to your phone by ```adb install -r -t main/build/outputs/apk/dev/debug/main-dev-debug.apk```

#### Open project in Android Stuido

1. Open the project's root folder by Android Studio, the application module is the `main` folder.
1. You can simplily build and run application by the built-in buttons in Android Studio.

#### Flavors

- **Dev** for development and rapid CI/CD testing
- **Prod** for official releases

#### Build & Release Signing (CI/CD)

The GitHub Actions CI/CD pipeline (`.github/workflows/ci.yml` and `.github/workflows/release.yml`) automatically builds both **Debug** and **Release** APKs.

1. **Automated CI Debug Builds**:
   Every push to `master` or pull request automatically builds a debug APK (`main-dev-debug.apk`) available in the GitHub Actions workflow run artifacts.

2. **Automated Signed Releases**:
   When triggering a release workflow (or publishing a release tag `v*`), GitHub Actions builds signed APKs attached directly to the GitHub Release.

3. **Custom Keystore Configuration (GitHub Secrets)**:
   To sign releases with your own custom keystore, add the following Repository Secrets in **GitHub Settings -> Secrets and variables -> Actions**:
   - `KEYSTORE_BASE64`: Base64 encoded `.keystore` / `.jks` file. Convert your keystore file with:
     ```bash
     base64 -w 0 /path/to/your.keystore   # Linux
     base64 -i /path/to/your.keystore     # macOS
     ```
   - `KEYSTORE_PASSWORD`: Keystore store password
   - `KEY_ALIAS`: Key alias name
   - `KEY_PASSWORD`: Key password

   *Note: If no custom `KEYSTORE_BASE64` secret is set, GitHub Actions automatically generates a self-signed release keystore so that released APKs are always signed and installable.*

## Version History

[Github Page](https://firemaples.github.io/EverTranslator/version_history.html) (big changes)

[Release](https://github.com/firemaples/EverTranslator/releases)

## Contact

If you encounter a bug, please [raise an issue here](https://github.com/firemaples/EverTranslator/issues/new/choose).  

For general questions, you can [raise an issue](https://github.com/firemaples/EverTranslator/issues/new/choose) or send an email firemaples@gmail.com.
