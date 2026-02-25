# iOS Flow Inventory -> Android Port Scope

This inventory captures functional and UI flows identified in `miataru-ios` and maps them to Android implementation scope.

## 1. App Bootstrap and Deep Links
- App launch -> root route resolution by onboarding state.
- Deep link `miataru://<deviceId>`.
  - Existing device: open device map.
  - Unknown device: open add device with prefilled ID.
- Foreground relaunch / `onNewIntent` deep link handling.

## 2. Onboarding Flows
- Welcome and feature intro.
- Location permissions and tracking toggle.
- Server URL selection.
- Location history setting.
- Allowed device list setting.
- Map provider policy step (Google/MapLibre).
- Completion and transition into main app.

## 3. Device Management Flows
- Device list with refresh state and error state.
- Add device (manual).
- Add device by QR payload / scanner.
- Edit device.
- Delete device.
- Device access policy flags (current location/history).
- ACL sync trigger after device mutations.

## 4. Group Flows
- Group list.
- Add group.
- Delete group.
- Edit group members (assign/remove devices).
- Open group map with member markers.

## 5. Map Flows
- Device map open.
- Device map refresh.
- Group map refresh.
- Marker and accuracy circle rendering (Google mode).
- Fallback rendering in MapLibre policy mode.

## 6. Tracking and Background
- Tracking toggle from onboarding/settings.
- Foreground service lifecycle.
- Periodic WorkManager upload.
- Immediate upload on enable.
- Permission-degraded behavior (retry / no upload).

## 7. Settings Flows
- Track/report location toggle.
- Location history toggle.
- Allowed device list toggle.
- Server URL update.
- Device key update.
- Map provider switch.
- Rerun onboarding.

## 8. Visitor History Flows
- Visitor history refresh.
- Visitor list render.
- Ignore/unignore visitor event.

## 9. API/Sync Flows
- UpdateLocation.
- GetLocation.
- GetLocationHistory (gateway support).
- GetVisitorHistory.
- setDeviceKey.
- setAllowedDeviceList.
- setDeviceSlogan/getDeviceSlogan (gateway support).

## 10. Error and Edge Handling
- API/network errors surfaced to UI.
- Duplicate device ID validation.
- Invalid QR payload handling.
- Missing own device key for ACL sync.
- Missing permissions for background upload.

## 11. Deferred (Next Waves)
- Advanced navigation parity (mutual navigation, haptics, route overlays).
- Device history timeline playback UI.
- Widgets and AppIntent parity.
- Full localization parity beyond `de/en`.
- Tablet-specific layouts.
