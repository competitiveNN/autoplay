# AutoPlay

AutoPlay is an Android app that automatically resumes media playback when headphones are connected and media is stopped (paused or stopped), mimicking the behavior of pressing the play button.

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
| `READ_PHONE_STATE` | Reserved for future use |

## Project structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/fra/autoplay/
│   │   ├── MainActivity.kt              # Toggle UI
│   │   ├── MediaPlaybackService.kt      # Foreground service + headphone detection
│   │   ├── HeadphoneReceiver.kt         # ACTION_HEADSET_PLUG broadcast receiver
│   │   └── BootReceiver.kt              # Boot completed receiver
│   ├── res/
│   │   ├── layout/activity_main.xml
│   │   ├── values/strings.xml
│   │   └── drawable/ic_launcher_foreground.xml
│   └── test/java/com/fra/autoplay/      # Unit tests
│       ├── MediaPlaybackServiceTest.kt
│       ├── HeadphoneReceiverTest.kt
│       └── BootReceiverTest.kt
```

## Building

```bash
./gradlew assembleDebug
```

Requires Android SDK 34 and Android Gradle Plugin 8.2.

## Testing

```bash
./gradlew testDebugUnitTest
```

Tests cover:
- `MediaPlaybackService.isHeadphone()` for all headphone device types
- `HeadphoneReceiver` ignoring non-headset and unplug events
- `BootReceiver` ignoring non-boot events

## License

MIT