# Development Guide - miataru Android App

## Quick Start for Developers

### Prerequisites
- Android Studio Koala+ (Electric Eel or newer recommended)
- Android SDK Platform 35 (Android 15) and build tools installed
- JDK 17
- An Android 8.0+ device or emulator (minSdk 24; targetSdk 35)

### Setup
1. Clone the repository
2. Open the project root in Android Studio
3. Ensure the Android SDK 35 is installed and select the matching JDK 17
4. Sync Gradle and build the `app` module
5. Create and run an Android 15 emulator or use a physical device

## Project Structure Deep Dive

### Core Architecture
```
app/
├── src/main/
│   ├── java/com/miataru/
│   │   ├── ui/                    # Compose UI (screens, components)
│   │   ├── location/              # FusedLocation + Foreground Service
│   │   ├── settings/              # DataStore + settings models
│   │   ├── data/                  # Room/DataStore, repositories, entities
│   │   ├── network/               # Miataru API client (Retrofit/Ktor)
│   │   ├── di/                    # Hilt modules
│   │   └── util/                  # Utilities
│   └── res/                       # Resources & localization
└── build.gradle.kts               # Module Gradle config
```

### Key Dependencies
- Jetpack Compose (Material 3, Navigation, Lifecycle)
- Google Play Services Location (FusedLocationProvider)
- Hilt for dependency injection
- Retrofit or Ktor + Kotlinx Serialization for API client
- Room or DataStore for persistence
- ML Kit or ZXing for QR scanning and ZXing for QR generation
- WorkManager for background tasks

## Development Workflow

### Code Organization
- Composables should be stateless when possible; hoist state to ViewModels
- ViewModels expose immutable UI state via `StateFlow`/`UiState`
- Repositories coordinate network and local storage
- Keep platform concerns (permissions, services) outside composables

### Testing Strategy
- Unit tests for repositories/managers with JUnit and coroutines-test
- UI tests with Compose UI testing
- Instrumentation tests for location service and QR scanning
- Test on multiple device sizes and Android versions

### Debugging Tips
- Use emulator location tools (route playback) for testing
- Monitor battery and background restrictions (Settings > Battery)
- Trace foreground service notifications and WorkManager jobs
- Inspect network with Android Studio Network Inspector

## Common Development Tasks

### Adding New Features
1. Create a screen in `ui/feature/` and a `FeatureViewModel`
2. Add repository/manager if business logic is needed
3. Update DataStore/Room schemas if required
4. Add localized strings in `res/values/strings.xml`
5. Add navigation route and preview

### Location Permission Handling
- Check `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` prior to use
- Request `ACCESS_BACKGROUND_LOCATION` only when necessary with clear rationale
- Show UI rationale and handle permanently denied states
- Respect user choices and provide in-app toggles

### Settings Management
- Use DataStore for preferences (server URL, tracking options)
- Expose data as `Flow`; collect with lifecycle-aware APIs in UI
- Provide sensible defaults and validate input

## Performance Considerations

### Battery Optimization
- Use Foreground Service only while tracking; show persistent notification
- Adjust update intervals and accuracy based on foreground/background
- Throttle server updates when offline; batch when back online
- Use WorkManager for periodic sync rather than tight loops

### Memory Management
- Stop location updates when not needed
- Use `viewModelScope` and cancel when ViewModel is cleared
- Avoid leaking `Context` into long-lived objects

## Privacy & Security

### Data Handling
- Never store sensitive location data unencrypted
- Provide clear data retention controls
- Give users full control over sharing and server configuration
- Follow GDPR and Google Play privacy requirements

### Permission Management
- Request only necessary permissions at the moment of need
- Explain why permissions are needed and how to change them later
- Handle background limits and OEM battery policies

## Troubleshooting

### Common Issues
- Location not updating: verify permissions, location services, and service running
- QR code not scanning: check camera permission and ML Kit model availability
- Settings not persisting: confirm DataStore read/write and coroutine scope
- Background tracking stops: check OEM battery optimizations and Foreground Service

### Debug Tools
- Emulator Extended Controls > Location
- Logcat and Network Inspector
- Background Task Inspector (WorkManager)
- Developer options: Keep activities, background restrictions

## Contributing Guidelines

### Code Style
- Kotlin style guide; idiomatic coroutines/flows
- Descriptive names; small focused functions
- Comment complex logic concisely (English)

### Git Workflow
- Feature branches per task
- Descriptive commit messages
- Run tests and lint before pushing
- Update documentation as needed

## Resources

### Documentation
- Jetpack Compose: `https://developer.android.com/jetpack/compose`
- Android UX guidelines: `https://developer.android.com/design`
- Fused Location Provider: `https://developers.google.com/location-context/fused-location-provider`
- DataStore: `https://developer.android.com/topic/libraries/architecture/datastore`
- WorkManager: `https://developer.android.com/topic/libraries/architecture/workmanager`

### Tools
- Android Studio profilers (CPU, Memory, Energy)
- Emulator for multi-device testing
- Play Console and internal app sharing for distribution
