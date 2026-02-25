# iOS -> Android Parity Matrix

| iOS Flow | Android Target | Priority | Android UX Adaptation | Test ID |
|---|---|---:|---|---|
| Root view onboarding gate | `MainViewModel` + `MiataruApp` start destination | P0 | Deterministic nav route gate in Compose | PAR-001 |
| `miataru://` deep link existing device | `DeepLinkRouter` -> DeviceMap route | P0 | Uses `onNewIntent` + route navigation | PAR-002 |
| `miataru://` deep link unknown device | `DeepLinkRouter` -> DeviceEditor prefill | P0 | Opens add screen with query arg | PAR-003 |
| iOS onboarding welcome | `OnboardingScreen` step 1 | P0 | Stepper UX instead of paged cards | PAR-010 |
| Location permissions onboarding | `OnboardingScreen` step 2 | P0 | Android runtime permission launcher | PAR-011 |
| Server selection onboarding | `OnboardingScreen` step 3 | P0 | Editable URL + persisted DataStore | PAR-012 |
| History config onboarding | `OnboardingScreen` step 4 | P0 | Switch controls | PAR-013 |
| Allowed list onboarding | `OnboardingScreen` step 4 | P0 | Switch controls | PAR-014 |
| Onboarding completion | `OnboardingViewModel.completeOnboarding` | P0 | Explicit finish CTA | PAR-015 |
| Device list | `DevicesScreen` | P0 | Material cards with action icons | PAR-020 |
| Add device manual | `DeviceEditorScreen` add mode | P0 | Single form screen | PAR-021 |
| Add via QR payload | `DeviceEditorScreen` + ZXing + `QrPayloadParser` | P0 | Scanner launcher + strict parser | PAR-022 |
| Edit device | `DeviceEditorScreen` edit mode | P0 | Route by local device ID | PAR-023 |
| Delete device | `DevicesViewModel.deleteDevice` | P0 | Confirmation dialog | PAR-024 |
| ACL flags per device | `DeviceEditorScreen` switches | P0 | Toggle controls, sync after save | PAR-025 |
| ACL sync/rollback semantics | `AclSyncServiceImpl` | P0 | Local-first with rollback on fail | PAR-026 |
| Device map view | `DeviceMapScreen` | P0 | GoogleMap Compose + marker/circle | PAR-030 |
| Group list | `GroupsScreen` | P0 | Dedicated tab section | PAR-031 |
| Group member assignment | `MembersDialog` | P0 | Checkbox dialog | PAR-032 |
| Group map view | `GroupMapScreen` | P0 | Multi-marker map and list summary | PAR-033 |
| Settings core toggles | `SettingsScreen` | P0 | Android switch/field patterns | PAR-040 |
| Device key update | `SettingsScreen` + `DeviceKeyServiceImpl` | P0 | Explicit key update action | PAR-041 |
| Visitor history list | `VisitorHistoryScreen` | P0 | List with ignore toggle | PAR-042 |
| Background tracking enable | `TrackingCoordinatorImpl` | P0 | Foreground service + periodic work | PAR-050 |
| Location upload worker | `LocationUploadWorker` | P0 | Last-known-location best effort | PAR-051 |
| API gateway unification | `MiataruGateway` + impl | P0 | Error classification and normalization | PAR-060 |
| Data persistence | Room + DataStore modules | P0 | Repository boundary and flows | PAR-061 |
| Map provider policy gate | `SettingsState.mapProvider` | P0 | Google default, MapLibre fallback mode | PAR-070 |
| Advanced navigation stack | Deferred | P1 | Requires route engine and nav overlays | PAR-100 |
| Device history timeline playback | Deferred | P1 | Requires timeline UI + cache model | PAR-101 |
| Widgets parity | Deferred | P2 | Requires App Widget + update service | PAR-200 |
