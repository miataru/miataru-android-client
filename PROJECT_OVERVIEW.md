# miataru Android App - Project Overview

## 🎯 Project Purpose
miataru is a privacy-focused Android application that enables users to track their device location, share it securely with trusted parties, and manage multiple devices and groups. The app prioritizes user privacy, battery efficiency, and cross-platform compatibility.

## 🏗️ Architecture Overview

### Technology Stack
- **UI**: Jetpack Compose (Material 3)
- **State Management**: ViewModel + Kotlin Coroutines/StateFlow
- **Data Persistence**: DataStore (preferences) and Room (entities) as needed
- **Location Services**: FusedLocationProvider + Foreground Service
- **QR Code**: ML Kit or ZXing scanning; ZXing generation
- **Background Work**: WorkManager
- **DI**: Hilt
- **Target**: minSdk 24, targetSdk 35 (Android 15)

### Core Components
```
MainActivity (Entry Point)
├── Navigation (Compose Navigation)
├── Onboarding (Permissions, device setup)
├── location/
│   ├── LocationTrackingManager
│   └── LocationTrackingService (Foreground Service)
├── settings/
│   └── SettingsRepository (DataStore)
├── data/
│   ├── KnownDeviceStore (Room/DataStore)
│   └── DeviceGroupStore (optional)
├── network/
│   └── MiataruApiClient (Retrofit/Ktor + Serialization)
└── ui/
    ├── screens/ (Map, Devices, Settings, Onboarding)
    └── components/ (common UI elements)
```

## 🔑 Key Features

### Location Management
- **Privacy-First**: User controls all location sharing
- **Battery Optimized**: Smart location updates and batching
- **Permission Handling**: Foreground/background location with rationale
- **Background Support**: Foreground service with persistent notification

### Device Management
- **Multi-Device**: Track multiple devices
- **Group Support**: Organize devices into groups
- **QR Code Sharing**: QR onboarding using `miataru://` codes
- **Cross-Platform**: Android-first with server compatibility

### User Experience
- **Onboarding Flow**: Guided permission and setup process
- **Settings Control**: Preferences for server, tracking, and privacy
- **Error Handling**: User-friendly messages and retry mechanisms
- **Localization**: English, German, and Japanese support

## 📱 Platform-Specific Considerations (Android)
- Background execution limits and Doze/App Standby
- Foreground service requirements and notification channels
- OEM battery optimizations (whitelisting guidance)
- Privacy dashboard and runtime permission flows

## 🛠️ Development Guidelines

### Code Organization
- **UI**: Minimal logic in composables; delegate to ViewModels
- **Managers/Repos**: Business logic and data orchestration
- **Models**: Immutable UI state and sealed events
- **Resources**: Organized under `res/` with proper localization

### State Management
- ViewModels expose `StateFlow` and receive events from UI
- Handle app lifecycle and process death resilience
- Keep dependencies minimal and injected via Hilt

### Testing Strategy
- Unit tests for repositories/managers
- Compose UI tests for key flows
- Instrumentation tests for services and permissions
- Multi-device and version testing (API 24–35)

## 🔒 Privacy & Security

### Data Protection
- Encrypt sensitive location data at rest when stored
- Obtain explicit consent for sharing and background tracking
- Provide data retention and deletion controls
- Ensure GDPR and Google Play compliance

### Permission Management
- Request minimal permissions at time of need
- Provide clear explanations and in-app guidance
- Handle denial gracefully with alternatives

## 📊 Performance Considerations

### Battery Optimization
- Adaptive update intervals and accuracy
- WorkManager for periodic sync
- Offline caching and backoff/retry policies

### Memory Management
- Clean up listeners and stop updates when unused
- Avoid leaking `Context` and keep services lean

## 🚀 Deployment & Distribution

### Build Requirements
- Android Studio (Koala or newer)
- SDK Platform 35 and build tools
- JDK 17

### Distribution
- Google Play (internal/app bundle)
- Closed testing tracks and internal app sharing
- Versioning and signing via Gradle

## 🔧 Common Development Tasks

### Adding Features
1. Add screen and ViewModel
2. Wire repository/manager and DI module
3. Add strings and resources
4. Add navigation route and preview
5. Write tests

### Debugging
- Emulator route playback and sensors
- Permission status and background restrictions
- Network and background task inspectors

### Performance Tuning
- Profiler (CPU/Memory/Energy)
- Trace foreground service behavior
- Optimize recompositions and state hoisting

## 📚 Resources & References

### Documentation
- Compose: `https://developer.android.com/jetpack/compose`
- Location: `https://developers.google.com/location-context/fused-location-provider`
- WorkManager: `https://developer.android.com/topic/libraries/architecture/workmanager`
- Hilt: `https://developer.android.com/training/dependency-injection/hilt-android`
- Material 3: `https://m3.material.io/`

### Dependencies
- ZXing/ML Kit for QR
- Retrofit/Ktor + Kotlinx Serialization
- Room/DataStore

## 🎯 Future Roadmap

### Planned Features
- Enhanced privacy controls
- Advanced device grouping
- Improved battery optimization
- Offline-first caching and sync

### Technical Improvements
- Performance optimizations
- Enhanced error handling
- Expanded test coverage
- Documentation updates

---

This overview provides context for the miataru Android port. See `doc/AndroidPortingPlan.md` for detailed steps.
