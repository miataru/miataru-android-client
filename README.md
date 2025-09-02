<<<<<<< HEAD
# miataru Android App

miataru is a privacy-focused Android application for location tracking and secure sharing with trusted parties. The project prioritizes user privacy, battery efficiency, and a clean, modern Android experience.

- **Target platform**: Android 8.0+ (minSdk 24), targetSdk 35 (Android 15)
- **Tech stack**: Kotlin, Jetpack Compose (Material 3), Hilt, Coroutines/Flow, DataStore/Room, Retrofit/Ktor, WorkManager, FusedLocationProvider, ZXing/ML Kit, Google Maps Compose
- **IDE**: Android Studio

## Status
The Android port is in progress. See the detailed plan in `doc/AndroidPortingPlan.md`.

## Features (planned/implemented)
- **Privacy-first tracking**: Foreground and background location with user control
- **Device management**: Known devices and optional groups
- **QR onboarding**: `miataru://` QR scan and share
- **Map UI**: Device markers and off-screen indicators (Google Maps Compose)
- **Settings**: Configurable server and tracking options via DataStore
- **Localization**: English, German, Japanese

## Project structure (expected)
```
app/
├── src/main/
│   ├── java/com/miataru/
│   │   ├── ui/            # Compose screens and components
│   │   ├── location/      # FusedLocation + Foreground Service
│   │   ├── settings/      # DataStore + settings models
│   │   ├── data/          # Room/DataStore, repositories, entities
│   │   ├── network/       # Miataru API client (Retrofit/Ktor)
│   │   ├── di/            # Hilt modules
│   │   └── util/          # Utilities
│   └── res/               # Resources & localization
└── build.gradle.kts
```

Refer to `.cursorrules`, `DEVELOPMENT.md`, and `PROJECT_OVERVIEW.md` for architecture and standards.

## Requirements
- **Android Studio** Koala or newer
- **Android SDK Platform 35** and Build Tools
- **JDK 17**

## Getting started
1. Clone the repository
2. Open the project root in Android Studio
3. Ensure SDK 35 and JDK 17 are configured
4. Sync Gradle and run the `app` configuration on an emulator or device

Command line (once the Android module is present):
```
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

## Configuration
- **Permissions** (requested as needed):
  - `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
  - `ACCESS_BACKGROUND_LOCATION` (for background tracking)
  - `FOREGROUND_SERVICE` and notification channel
  - `POST_NOTIFICATIONS` (Android 13+)
  - `CAMERA` (QR scanning)
  - `INTERNET`
- **Settings**:
  - Server URL and tracking options stored in DataStore
  - Device ID persisted in app-private storage
- **API Client**:
  - Retrofit or Ktor with Kotlinx Serialization for Miataru endpoints

## Development guidelines
- **Architecture**: MVVM with `ViewModel` + `StateFlow`; composables stay lightweight
- **DI**: Hilt modules for repositories/managers/clients
- **Persistence**: DataStore for preferences; Room or serialized files for entities
- **UI**: Use `collectAsStateWithLifecycle` to observe flows in Compose
- **Permissions**: Clear rationale for foreground/background location; handle denial gracefully
- **Localization**: All user-facing text in `res/values/strings.xml` (and `values-de`, `values-ja`)
- **Headers**: New Kotlin files must include the required copyright header

See `.cursorrules` for complete standards and checklists.

## Privacy & security
- Do not store sensitive location data unencrypted
- Provide clear controls for sharing and data retention
- Comply with GDPR and Google Play policies

## Testing
- **Unit tests**: Repositories/managers (JUnit, coroutines-test)
- **UI tests**: Compose UI testing for critical flows
- **Instrumentation**: Services, permissions, and QR scanning

Run tests:
```
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

## Contributing
- Read `doc/AndroidPortingPlan.md` and `.cursorrules`
- Create feature branches and write descriptive commits
- Add tests for new behavior
- Ensure strings are localized and permissions handled gracefully
- Open a PR with a clear description and screenshots for UI changes

## License
See `LICENSE` for details.

## Resources
- Jetpack Compose: `https://developer.android.com/jetpack/compose`
- Material 3: `https://m3.material.io/`
- Fused Location Provider: `https://developers.google.com/location-context/fused-location-provider`
- WorkManager: `https://developer.android.com/topic/libraries/architecture/workmanager`
- Hilt: `https://developer.android.com/training/dependency-injection/hilt-android`
- Retrofit: `https://square.github.io/retrofit/`
- Kotlin Coroutines: `https://kotlinlang.org/docs/coroutines-overview.html`

---
For detailed porting steps and status, see `doc/AndroidPortingPlan.md`.
=======
miataru-android-client
======================

the Miataru Android Client
>>>>>>> master
