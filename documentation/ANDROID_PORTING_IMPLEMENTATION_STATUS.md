# Android Miataru Porting – Implementation Status

## Summary
This repository now contains a substantial Android-native implementation of Miataru based on Jetpack Compose, Hilt, Room, DataStore, WorkManager and a gateway layer around `miataru-client`.

The implementation is aligned with the staged parity plan (P0 core first), with key onboarding, device management, map flows, settings, background tracking foundations and deep-link handling in place.

## Implemented Architecture
- UI: Jetpack Compose + Navigation Compose + Material 3
- State: ViewModel + Flow/StateFlow
- DI: Hilt
- Local storage:
  - DataStore for app settings
  - Room for devices, groups, location snapshots, visitor events
- Network: `MiataruGateway` abstraction on top of `miataru-client`
- Background:
  - Foreground service for runtime location-tracking orchestration
  - WorkManager for immediate/periodic upload execution and recovery

## Implemented Functional Areas
- Onboarding flow with permission handling and tracking bootstrap
- Device CRUD and list/map flows
- Group list/map base flows
- Settings flow (server, tracking toggles, map provider, own device key update)
- Deep-link routing (`miataru://...`)
- Visitor history base flow
- DeviceKey/ACL service foundations
- Background tracking foundations with foreground service + worker upload

## Tracking and Upload Behavior (Current)
- Tracking activation ensures own device ID availability before starting runtime tracking.
- Foreground tracking service:
  - Registers `LocationManager` listeners (`GPS`, `NETWORK`, `PASSIVE`)
  - Applies significant-change style filtering and throttling to reduce battery usage
  - Enqueues immediate upload work on accepted location changes
  - Bootstraps with newest last-known location on service start
- Worker upload path:
  - Prefers explicit location payload from service callbacks
  - Falls back to best last-known location if callback payload is unavailable
  - Uses Miataru-compatible timestamp conversion (epoch seconds for upload)

## Timestamp Compatibility Fixes
Miataru timestamp handling was made robust for both upload and parsing:
- Upload now uses epoch seconds string for `Timestamp`.
- Parsing accepts:
  - epoch seconds
  - epoch milliseconds
  - ISO-8601 as fallback

This fixes silent failures caused by strict ISO parsing assumptions.

## Server URL Policy
Default server URL in app settings is:
- `https://service.miataru.com`

No mandatory `/v1` suffix is auto-appended at settings layer; runtime URL normalization only ensures trailing slash consistency for client construction.

## Documentation Added for Parity/Analysis
- `documentation/iOS_Flow_Inventory.md`
- `documentation/iOS_Android_Parity_Matrix.md`
- `documentation/Android_UX_Adaptation_Decisions.md`

These capture analyzed iOS flows, Android mapping/priority, and explicit UX adaptation choices.

## Build/Verification
Recent verification commands used:
- `./gradlew :app:assembleDebug`
- `./gradlew :app:installDebug`
- targeted unit validation for timestamp conversion logic

## Known Practical Notes
- Real-world update frequency depends on Android location subsystem behavior, provider availability, movement profile, and throttling gates.
- Background behavior requires active tracking, granted location/background permissions, and foreground-service notification viability.
- Existing non-critical deprecation warnings in Gradle/Kotlin remain and can be cleaned up in a follow-up maintenance pass.
