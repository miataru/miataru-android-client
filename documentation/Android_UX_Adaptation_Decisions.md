# Android UX Adaptation Decisions

## Objective
Implement iOS function parity with Android-native interaction patterns and platform constraints.

## Decisions

1. Navigation model
- iOS tab and stack model is translated to Navigation Compose routes.
- Bottom destinations: `Devices`, `Groups`, `Settings`.
- Secondary flows (`map`, `visitor history`, `editor`) are stacked routes.

2. Onboarding UX
- iOS page set is implemented as a deterministic stepper flow.
- Android permission prompts are explicit actions, not auto-triggered on page display.

3. Device add/edit
- iOS sheets are converted to full-screen Compose routes.
- QR handling uses scanner launcher + parser and supports strict `miataru://` validation.

4. Deep links
- iOS URL handling becomes Android intent-filter + runtime router.
- Existing vs unknown device behavior remains functionally equivalent.

5. Background behavior
- iOS background location lifecycle maps to Android ForegroundService + WorkManager.
- Upload worker uses last known location and retries on recoverable conditions.

6. Data model and state
- iOS `UserDefaults`/plist-like stores are mapped to DataStore + Room.
- UI uses Flow/StateFlow with ViewModel ownership.

7. Error handling
- iOS alert-heavy UX is adapted into inline error cards and dismiss actions.
- Critical actions still use confirmation dialogs.

8. Map provider policy
- Google rendering is default implementation.
- If cost policy requires no paid dependency, app can switch provider mode to MapLibre fallback path.

9. Access control and device key
- ACL/device-key server actions are centralized in dedicated services.
- ACL writes use rollback behavior when remote sync fails.

10. Deferred adaptations
- iOS advanced navigation overlays, haptics, and mutual navigation detection are postponed to P1.
- Widget and tablet specialization are postponed to P2.

## Out-of-Scope for this wave
- Turn-by-turn parity and route progress overlays.
- Widget timeline parity.
- Full localization parity beyond initial core languages.
