# Android Porting Analysis and Implementation Plan
This document tracks progress of the Android port. Mark tasks as complete once implemented.

**Android Studio project root**: Open the folder that contains `settings.gradle.kts` (the `src/` directory) in Android Studio. That gives a single project that builds the app and the miataru-client library, runs app and library tests, and supports debugging the app (including library code).

## Miataru client library – API 1.1 alignment

- **Canonical spec**: [Miataru.yaml](../ios-app-for-reference-during-porting/miataru/Libraries/MiataruClientSwift/Definitions/Miataru.yaml) (API version 1.1.0).
- **Relationship**: The Android client in `Libraries/MiataruAndroidClient/miataru-client` implements the original five endpoints; the iOS client in `ios-app-for-reference-during-porting/miataru/Libraries/MiataruClientSwift` is the reference implementation for API 1.1 and is mirrored on Android.
- **API 1.1 breaking and additive changes**:
  - **GetLocation / GetLocationHistory**: `RequestMiataruDeviceID` is mandatory; `RequestMiataruDeviceKey` is optional (strict requester validation when key is set).
  - **UpdateLocation / GetVisitorHistory / DeleteLocation**: Optional `DeviceKey` in request when the device has a key set (403 if missing or wrong).
  - **GetLocationGeoJSON**: GET (and POST) return 401 when the device has a DeviceKey; use GetLocation instead for key-protected devices.
  - **New endpoints**: DeleteLocation, setDeviceKey, setAllowedDeviceList, setDeviceSlogan, getDeviceSlogan.

### Building, testing, and debugging with Android Studio

- **Unified build**: One Gradle sync builds both `app` and `miataru-client` (library is included as a subproject).
- **Run/Debug app**: Use the default run configuration for the `app` module; breakpoints in app and library code are supported.
- **Run library unit tests**: Right-click `miataru-client` test source set or a test class and **Run Tests**, or run the Gradle task `:miataru-client:test`.
- **Run all tests**: Run the Gradle `test` task from the Gradle tool window or use a run configuration for the full test suite.

## iOS App Feature Overview
- **Background and foreground tracking** – A shared `LocationManager` switches between high-accuracy updates in the foreground and significant-change monitoring in the background while observing settings and network reachability.
- **Device management** – `KnownDeviceStore` persists all known devices, automatically inserts the current device if missing, and saves the list whenever it changes.
- **QR-based onboarding** – Users can add devices by scanning QR codes with `CodeScanner`; the app validates that codes use the `miataru://` prefix before saving the device ID.
- **Shareable QR code for this device** – A dedicated view generates a QR code containing the local device ID and offers sharing via clipboard, email, or the iOS share sheet.
- **Map UI with off-screen indicators** – The map draws custom arrows for devices located outside the visible region, rotating and snapping intelligently to screen edges for smooth guidance.

## Android Porting Strategy
1. **Platform & Architecture**
   - Use Kotlin with Jetpack Compose to mirror SwiftUI's reactive style.
   - Implement managers as `ViewModel` + `StateFlow` equivalents for location, settings, and device stores.
   - Store preferences in DataStore and persistent entities in Room or serialized files.
2. **Location Tracking**
   - Employ Google's FusedLocationProvider for high-accuracy foreground tracking and a Foreground Service with `ACCESS_BACKGROUND_LOCATION` for background updates.
   - Switch between frequent updates and significant-change requests using distance thresholds or `PendingIntent` triggers.
   - Monitor connectivity via `ConnectivityManager` and throttle server updates when offline.
3. **Miataru Server Communication**
   - Recreate the Swift `MiataruAPIClient` with Retrofit and Kotlinx Serialization (or Ktor client).
   - Provide suspend functions for `GetLocation`, `GetLocationHistory`, and `UpdateLocation` endpoints with authentication and server configuration.
4. **Device ID & Settings**
   - Persist a unique device ID in app-private storage.
   - Build `KnownDeviceStore` and `DeviceGroupStore` equivalents with Room and expose reactive flows.
5. **Map and Navigation Features**
   - Use Google Maps Compose to render device markers, compass, and off-screen arrow indicators.
   - Implement custom composables to draw arrows and compute edge intersections analogous to the Swift implementation.
   - Optionally integrate a Directions API to render route polylines.
6. **QR Code Scanning and Generation**
   - Integrate ML Kit Barcode Scanning or ZXing for scanning `miataru://` codes during onboarding.
   - Generate QR codes with ZXing's encoder and share via Android's `Intent.ACTION_SEND`.
7. **Background Behavior and Battery Optimization**
   - Respect background execution limits by displaying a persistent notification when tracking.
   - Provide user controls to adjust location sensitivity and update intervals.
   - Use WorkManager for periodic sync or cleanup tasks.
8. **Reverse Geocoding & Caching**
   - Utilize Android's `Geocoder` or a third-party API for placemark lookup and cache results until the device moves beyond a threshold.
9. **Onboarding and Permissions**
   - Compose-based onboarding screens guide users through camera, location (foreground & background), and notification permissions.
10. **Testing & Distribution**
   - Unit-test managers and data stores with JUnit; UI-test flows with Espresso.
   - Target API 24+ and configure CI for builds and tests.

## Implementation Checklist
- [ ] **Miataru client library – API 1.1**
  - [ ] Add RequestMiataruDeviceID/RequestMiataruDeviceKey to GetLocation/GetLocationHistory requests; ensure all call sites send requesting device ID (and key when set).
  - [ ] Add DeviceKey to UpdateLocation, GetVisitorHistory, and DeleteLocation where the device uses a key.
  - [ ] Implement and use new endpoints: DeleteLocation, setDeviceKey, setAllowedDeviceList, setDeviceSlogan, getDeviceSlogan.
  - [ ] Use GetLocation (or POST GetLocationGeoJSON with config) for key-protected devices instead of GET GetLocationGeoJSON.
- [ ] **Miataru client library – tests**
  - [ ] Add unit tests for all request/response model JSON (Moshi) round-trips and optional-field behavior.
  - [ ] Add tests using MockWebServer for all endpoints (existing + new) to verify HTTP method, path, and body and to parse success (and optionally error) responses.
  - [ ] Add test for `MiataruAndroidClient` builder and base URL wiring.
- [ ] **Project and dependency setup**
  - [ ] Integrate Hilt for dependency injection.
  - [ ] Replace custom HTTP logic with Retrofit and Kotlinx Serialization.
  - [ ] Add coroutine and Flow dependencies.
- [ ] **Device identification and persistence**
  - [ ] Implement a persistent device ID manager.
  - [ ] Migrate `KnownDeviceStore` to Room or DataStore with reactive updates.
  - [ ] Add device grouping and CRUD operations.
- [ ] **Location tracking**
  - [ ] Convert `LocationService` into a Foreground Service with notification and background permission handling.
  - [ ] Switch between high-accuracy foreground updates and significant-change background updates.
- [ ] **Miataru API client** (app use of the miataru-client library)
  - [ ] Support `GetLocationHistory`, `UpdateLocation`, and server configuration via the miataru-client library (updated for API 1.1).
  - [ ] Add error handling, retries, and offline caching of pending updates.
- [ ] **QR code onboarding**
  - [ ] Integrate ML Kit or ZXing for scanning and generating `miataru://` codes.
  - [ ] Build Compose screens to scan codes, validate IDs, and generate shareable QR codes.
- [ ] **Map and UI**
  - [ ] Introduce Google Maps Compose for marker rendering, compass, and off-screen indicators.
  - [ ] Create screens for device list management, map view, and settings.
- [ ] **Settings and permissions**
  - [ ] Store user preferences (server URL, tracking options) with DataStore.
  - [ ] Implement onboarding flow requesting camera, location, and notification permissions.
- [ ] **Background behavior and battery management**
  - [ ] Handle Doze/App Standby with WorkManager for periodic sync.
  - [ ] Allow users to toggle tracking and adjust update intervals.
- [ ] **Testing and CI**
  - [ ] Add unit tests for stores and API client.
  - [ ] Add instrumentation tests for location service and QR scanning.
  - [ ] Configure GitHub Actions to run Gradle builds and tests.

