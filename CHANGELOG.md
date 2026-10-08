# Changelog

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