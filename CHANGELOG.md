# Changelog

## v1.2.0 (2026-10-08)

### Added
- Dark/light/system theme toggle (AppCompatDelegate, preference, spinner in Settings)
- Quick Settings Tile (TileService) to toggle service from the notification shade
- Test Resume button wired in SettingsFragment
- Backup/restore settings via clipboard JSON export/import
- Wear OS notification wearable extender (Stop / Test Resume on watch)
- German localization (`values-de/strings.xml`) — English base updated to 1.2.0
- Play Store / F-Droid `fastlane` metadata for `en-US` and `de-DE`
- SharedPreferences schema versioning (`schema_version = 3`) with auto-migration in `AutoPlayApplication`
- Unit tests for `PreferencesHelper` migration/export and `QuickSettingsTileService` state
- Benchmark placeholder (`benchmark/README.md`) for future Macrobenchmark

### Improved
- Accessibility: contentDescription on switches, ScrollView wrapping for large text, liveRegion on status, heading on title
- About screen now injects real `versionName` and copies it to clipboard
- Settings: theme spinner, backup/restore buttons, smart-resume ordering fixed

### Fixed
- Theme `forceDarkAllowed` lint `NewApi` — now `tools:targetApi="q"` on the item
- `HeadphoneReceiver` manifest name mismatch (`HeadphoneReceiver` → `HeadphoneConnectionReceiver`)
- Notification posting lint on Android 13+: runtime `POST_NOTIFICATIONS` check + `@SuppressLint`
- Receiver registration now uses `ContextCompat.registerReceiver(RECEIVER_NOT_EXPORTED)`
- Quick Settings Tile corrected to `Tile`/`TileService` + `action.QS_TILE`
- Added `POST_NOTIFICATIONS` permission and `<queries>` for `getInstalledApplications`

## v1.1.0 (2026-10-08)


### Added
- **Integration tests** for SettingsActivity and PreferencesHelper using Espresso
- **WorkManager-based periodic battery optimization health checks** (every 4 hours)
  - Shows notification when battery optimization is enabled for the app
  - Respects user preference to enable/disable reminders
- **"Test Resume" action** in persistent notification for quick testing
- **App exclusion list** preference to exclude specific apps from auto-resume
- **Home screen widget** for quick ON/OFF toggle without opening the app
  - Shows current service status
  - Tap to toggle service on/off
  - Tap icon to open app

### Improved
- Updated MediaPlaybackService to respect app exclusion list
- Widget updates automatically when service state changes
- Added broadcast for service state changes to keep UI in sync

## v1.0.1 (2026-10-07)

### Bug Fixes
- Fixed `MediaController.FLAG_HANDLES_TRANSPORT_CONTROLS` compilation error (constant moved to `MediaSession`)
- Fixed Python `#` comments in Kotlin source (MediaPlaybackService.kt)
- Fixed malformed string in MainActivity.kt
- Fixed `AudioDeviceCallback` method names for API 34 (`onAudioDevicesAdded`/`onAudioDevicesRemoved`)
- Fixed `AudioDeviceInfo` constants for API 34 (`TYPE_BLE_HEADSET`, `TYPE_USB_HEADSET`, `TYPE_HEARING_AID`)
- Fixed `PreferencesHelper.kt` SharedPreferences typo (`contextSharedPreferences` → `getSharedPreferences`)
- Fixed `ic_stop_24` drawable reference in notification action
- Removed unused `ContextCompat` import in HeadphoneReceiver.kt

### Added
- SettingsActivity with PreferencesFragment for user preferences
- Resume delay preference (0-3000ms)
- Headphone type filter preference
- Battery optimization reminder preference
- Test Resume button in settings
- Settings menu action in MainActivity toolbar

### Improved
- Simplified MainActivity UI to single ON/OFF toggle per spec
- Updated MediaPlaybackService to use preferences for resume delay and headphone filtering
- Deprecated `FLAG_HANDLES_TRANSPORT_CONTROLS` replaced with `transportControls != null` check

## v1.0 (2026-10-07)

### Initial Release
- Auto-resume media playback when headphones connect and media is stopped
- Supports wired, Bluetooth (A2DP), USB, digital, and hearing aid headphones
- Foreground service with persistent notification
- Boot receiver to restart service after reboot
- Battery optimization detection and guidance
- Unit tests (Robolectric) + Espresso integration tests
- GitHub Actions CI/CD workflow

### Improvements
- Smart session selection: prioritizes transport-control sessions, most recent first
- Only resumes the most relevant session to avoid conflicts
- Toast feedback for all user actions
- Test Resume and Battery Optimization buttons in UI
- Accessibility labels and content descriptions