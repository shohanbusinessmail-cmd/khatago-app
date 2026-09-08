# Build and test

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK Platform 35 and Build Tools supplied by Android Studio
- An Android 8.0/API 26 or newer device/emulator

## Commands

```bash
./gradlew test
./gradlew connectedDebugAndroidTest
./gradlew assembleDebug
./gradlew assembleRelease
```

The APKs are produced under `app/build/outputs/apk/`. The app requests no internet permission. If testing reminders on Android 13+, grant notification permission from More; the worker will otherwise skip notification delivery without affecting records.

## Android Studio

Import the repository root, allow Gradle sync, select the `app` configuration and run on a device. For a clean install during QA, clear app data or uninstall/reinstall. The first launch starts onboarding and production data starts empty.

## Release configuration

The checked-in release build is unsigned by design. For a real distribution build, place signing values in `~/.gradle/gradle.properties` or CI secrets, add a `signingConfig` locally/through a private convention plugin, and never commit a keystore or passwords. See `docs/RELEASE.md`.
