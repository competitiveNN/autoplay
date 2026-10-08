# AutoPlay

AutoPlay is an Android app that automatically resumes media playback when headphones are connected and media is stopped (paused or stopped), mimicking the behavior of pressing the play button.

## Demo

1. Start your favourite music/podcast app, then pause it.
2. Open AutoPlay and toggle **ON** (status shows "Service running").
3. Plug in wired, Bluetooth, or USB headphones — playback resumes automatically.
4. Use **Test Resume** in notification or Settings to verify without re-plugging.
5. Try the **widget** or **Quick Settings tile** for one-tap ON/OFF.

> Tip: after toggling OFF and ON, the service re-checks already-connected headphones and resumes immediately.

## Features

- Runs in the background as a persistent foreground service (never killed)
- Single ON/OFF toggle button in the UI
- Detects headphone connection via:
  - `AudioDeviceCallback` (API 23+, modern path)
  - `ACTION_HEADSET_PLUG` broadcast (fallback)
  - Supports wired, Bluetooth (A2DP/LE), USB, digital, and hearing aid headphones
- Resumes stopped media playback through `MediaSessionManager`
- Fallback to media-button injection when permission denied
- Restarts automatically after device boot
- **Settings** with preferences for:
  - Resume delay (0-3000ms)
  - Headphone type filter
  - Battery optimization reminder
  - App exclusion list (exclude specific apps from auto-resume)
  - Smart Resume learning (adapts delay to your usage patterns)
  - Theme (system / light / dark)
  - Backup / restore settings via clipboard
- **Home screen widget** for quick ON/OFF toggle
- **Diagnostics (opt-in, local-only)** — when enabled, uncaught crashes append to `files/diagnostics_crash.log` (no network); share manually if filing a bug
- **Quick Settings tile** (Android 7+) — add via Edit tiles in the shade
- **Wear OS** extended actions on the foreground notification (Stop / Test Resume)
- **Localization**: English and German (values-de)
- **Persistent notification** with Test Resume and Stop actions
- **Periodic battery optimization checks** via WorkManager (every 4 hours)

## Requirements

- Android 8.0 (API 26) minimum
- Android 11+ supported (target SDK 34)
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permission (Android 11+)

## Permissions used

| Permission | Purpose |
|---|---|
| `FOREGROUND_SERVICE` | Persistent service |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Foreground service type |
| `RECEIVE_BOOT_COMPLETED` | Restart on boot |
| `MODIFY_AUDIO_SETTINGS` | Audio device monitoring |
| `POST_NOTIFICATIONS` | Foreground + battery warnings (Android 13+) |
| `READ_PHONE_STATE` | Reserved for future use |

No `INTERNET` permission — fully offline. See Privacy Policy in the app under Settings → About & Licenses.

## Project structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/fra/autoplay/
│   │   ├── MainActivity.kt                 # Toggle UI
│   │   ├── MediaPlaybackService.kt         # Foreground service + headphone detection
│   │   ├── HeadphoneReceiver.kt            # ACTION_HEADSET_PLUG broadcast receiver
│   │   ├── BootReceiver.kt                 # Boot completed receiver
│   │   ├── QuickSettingsTileService.kt     # QS tile (TileService)
│   │   ├── SettingsActivity.kt             # Settings screen
│   │   ├── SettingsFragment.kt             # Preferences fragment + theme/backup/rename
│   │   ├── PreferencesHelper.kt            # SharedPreferences wrapper + migration
│   │   ├── BatteryOptimizationWorker.kt    # WorkManager worker for battery checks
│   │   ├── AutoPlayWidgetProvider.kt       # Home screen widget
│   │   ├── AboutActivity.kt                # About / Privacy / Licenses
│   │   └── AutoPlayApplication.kt          # Application class (migration + theme + WorkManager)
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_settings.xml / activity_about.xml
│   │   │   ├── fragment_settings.xml       # ScrollView-wrapped, a11y labelled
│   │   │   ├── dialog_delay.xml
│   │   │   └── widget_autoplay.xml
│   │   ├── values/strings.xml              # en
│   │   ├── values-de/strings.xml           # de
│   │   ├── values/themes.xml               # Material 3 DayNight
│   │   ├── xml/
│   │   │   ├── settings_preferences.xml
│   │   │   └── widget_autoplay_info.xml
│   │   └── drawable/ic_launcher*.xml
│   ├── test/java/com/fra/autoplay/         # Unit tests (Robolectric)
│   │   ├── MediaPlaybackServiceTest.kt
│   │   ├── HeadphoneReceiverTest.kt
│   │   ├── BootReceiverTest.kt
│   │   ├── PreferencesHelperTest.kt
│   │   └── QuickSettingsTileServiceTest.kt
│   └── androidTest/java/com/fra/autoplay/  # Integration tests (Espresso)
│       ├── MainActivityTest.kt
│       ├── SettingsActivityTest.kt
│       └── PreferencesHelperIntegrationTest.kt
├── compose/                              # Shared Compose UI components module
│   ├── src/main/java/com/fra/autoplay/compose/
│   │   ├── SettingComponents.kt           # Reusable setting items (SwitchSettingItem, etc.)
│   │   └── AutoPlaySettingsScreen.kt      # Compose-based settings screen
│   └── src/main/res/values/strings.xml    # Compose module strings
├── fastlane/metadata/android/               # Play Store / F-Droid (en-US, de-DE)
└── JETPACK_COMPOSE_MIGRATION_PLAN.md
```

## Building

```bash
./gradlew assembleDebug
```

Requires Android SDK 34 and Java 21 (Temurin). CI builds debug **and** release (R8 fullMode + shrinkResources) with JDK 21 + `android-actions/setup-android@v3` and caches Gradle.

## Testing

```bash
./gradlew testDebugUnitTest      # Robolectric unit tests
./gradlew recordPaparazziDebug   # screenshot tests (main/settings/about/widget + Compose UI)
./gradlew :app:jacocoTestReport  # aggregated JaCoCo coverage (CSV/HTML/XML)
./gradlew lintDebug              # Android Lint (baseline in lint-baseline.xml)
./gradlew connectedDebugAndroidTest  # Espresso (needs device/emulator)
./gradlew :benchmark:connectedDebugAndroidTest  # Macrobenchmark startup (needs device/emulator)
./gradlew :benchmark:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.fra.autoplay.benchmark.FrameTimingBenchmarkTest  # Frame timing (needs device/emulator)
```

Tests cover:
- `MediaPlaybackService.isHeadphone()` for all headphone device types
- `PreferencesHelper` JSON export/import, migration, schema versioning
- `QuickSettingsTileService` state mapping
- `HeadphoneReceiver` ignoring non-headset and unplug events
- `BootReceiver` ignoring non-boot events
- Paparazzi screenshots of `activity_main`, `fragment_settings`, `activity_about`, `widget_autoplay`
- Paparazzi screenshots of Compose `AutoPlaySettingsScreen`, `SwitchSettingItem`, `ButtonSettingItem`, `DropdownSettingItem`
- Compose benchmark metrics: `composeSettingsScrollPerformance()`, `composeSettingsStartup()`

Coverage badge: `.github/badges/jacoco.svg` (regenerated on every CI push to `master`/`main`).

## Diagnostics

Settings → General → Diagnostics toggle (off by default). When enabled:

- **Local file logging**: `AutoPlayApplication` installs a file-only `UncaughtExceptionHandler` that appends to `files/diagnostics_crash.log`. Clear or share via any file manager.
- **Firebase Crashlytics** (optional): If `google-services.json` is present, crashes are also sent to Firebase Crashlytics. No network permission is required by the app; Crashlytics uses its own. Disable the toggle to stop both local and remote logging.

## Store listing

Play Store / F-Droid copy lives in `fastlane/metadata/android/{en-US,de-DE}/` (title, short/full description, changelogs). Add screenshots under `fastlane/metadata/android/<locale>/images/phoneScreenshots/`.

## License

MIT — see `LICENSE` or About → Licenses in the app.

## Troubleshooting

### Media doesn't resume when headphones connect
1. **Check service is running** — The ON/OFF toggle in the app must show "Service running"
2. **Disable battery optimization** — Go to Settings → Apps → AutoPlay → Battery → Unrestricted
3. **Verify headphone type** — In Settings, ensure "Filter by Headphone Type" matches your headphones (or disable filter for all types)
4. **Grant notification access** — Some Android versions require notification access for MediaSessionManager
5. **Check POST_NOTIFICATIONS** — On Android 13+, allow notifications when prompted; otherwise the foreground notification is silent
6. **Grant notification access** — Some launchers hide the persistent notification on Android 13+ without it

### Service keeps getting killed (OEM FAQ)

| OEM | Where to allow AutoPlay |
|-----|------------------------|
| **Samsung One UI** | Settings → Apps → AutoPlay → Battery → Unrestricted; Battery → Background usage limits → Never sleeping apps → add AutoPlay |
| **Xiaomi MIUI / HyperOS** | Settings → Apps → Manage apps → AutoPlay → Battery saver → No restrictions; enable Autostart; lock the app in Recents |
| **OnePlus OxygenOS** | Settings → Apps → AutoPlay → Battery → Don't optimize; Battery → Battery optimization → Don't optimize |
| **Huawei EMUI** | Settings → Battery → App launch → AutoPlay → Manage manually → enable all toggles |
| **OPPO / Realme ColorOS** | Settings → Apps → AutoPlay → Battery → Allow background activity + Allow auto-start |
| **Vivo Funtouch** | Settings → Battery → Background high power consumption → AutoPlay → allow |
| **Stock Android (Pixel)** | Settings → Apps → AutoPlay → App battery usage → Unrestricted |

General: also exempt AutoPlay from any "Adaptive battery" / "Battery saver" feature and keep the foreground notification visible — some OEMs aggressively kill apps without a visible notification.

### Settings don't take effect immediately
- Resume delay and filter changes apply on next headphone connection (or via Test Resume)
- Use **Test Resume** in Settings or the notification to verify without replugging

### Localization not showing
- Locale follows system language (en/de). Change system language and reopen the app.

### Build fails with Java version error
- Use Java 21 (Temurin). CI enforces it.
- Set `JAVA_HOME=/path/to/jdk-21` before running `./gradlew`

### "SDK location not found" error
- Set `ANDROID_HOME` environment variable to your Android SDK path
- Or create `local.properties` with `sdk.dir=/path/to/android/sdk`

### Lint fails on NewApi / NotificationPermission
- See `lint.xml` for severity downgrades that are warnings on older API levels. Real errors (WrongViewCast, MissingClass, UnspecifiedRegisterReceiverFlag) still break the build.
