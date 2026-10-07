# Changelog

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