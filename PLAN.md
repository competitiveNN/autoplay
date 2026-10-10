Building an app that auto-resumes playback when headphones are plugged back in on **Android 8.0 Oreo (API 26)** requires navigating strict background execution limits and implicit broadcast restrictions.

Because Android 8.0 prevents background apps from running background services or registering static broadcast receivers for system events, the solution relies on a **Foreground Service** combined with a **Dynamic Broadcast Receiver**.

---

### Core Architecture Overview

To ensure 100% reliability on Android 8.0 Oreo, the core logic relies on these components:

1. **Foreground Service (`HeadphoneMonitorService`)**: Keeps your app alive in the background by displaying a persistent notification, bypassing Oreo's background service restrictions.
2. **Dynamic Broadcast Receiver**: Listens for `Intent.ACTION_HEADSET_PLUG` (which cannot be declared in the Manifest on Android 8.0+).
3. **Smart State Flag (`wasPlayingBeforeUnplug`)**: Ensures audio only auto-resumes if it was actively playing *when* the headphones were unplugged, preventing random playback when plugging in idle headphones.

---

### Step-by-Step Core Logic Plan

#### Step 1: Implement the Foreground Service (Oreo Compliance)

Standard background services are killed shortly after your app leaves the screen on Android 8.0. A Foreground Service solves this.

* **Triggering**: Start the service from your UI using `Context.startForegroundService(intent)`.
* **The 5-Second Rule**: Within 5 seconds of starting, the service *must* call `startForeground(notificationId, notification)` to show a persistent notification, otherwise the system will crash the app with an `AppNotResponding` (ANR) exception.

#### Step 2: Dynamically Register the Broadcast Receiver

Since Android 8.0 blocks manifest-declared (static) receivers for most implicit system intents, you must register your receiver programmatically inside the Foreground Service.

* **Registration**: Inside the service's `onCreate()` or `onStartCommand()`, register the receiver:
```kotlin
val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
registerReceiver(headsetReceiver, filter)

```


* **Unregistration**: Inside the service's `onDestroy()`, always unregister the receiver to prevent memory leaks:
```kotlin
unregisterReceiver(headsetReceiver)

```



#### Step 3: Implement the Headphone State and Resume Logic

Inside your `BroadcastReceiver`'s `onReceive()` method, intercept the `ACTION_HEADSET_PLUG` intent and evaluate the state extras:

* **Intent Extras**:
* `state`: `0` means unplugged, `1` means plugged in.


* **The Logic Flow**:
1. **When Unplugged (`state == 0`)**:
* Check if your media player is currently playing.
* If **Yes**: Pause the media player immediately, and set a tracking flag: `wasPlayingBeforeUnplug = true`.
* If **No**: Set `wasPlayingBeforeUnplug = false`.


2. **When Plugged Back In (`state == 1`)**:
* Check if `wasPlayingBeforeUnplug == true`.
* If **Yes**: Send a command to resume/play your media player, then reset `wasPlayingBeforeUnplug = false`.
* If **No**: Do nothing (the user plugged in headphones while music was already stopped).





#### Step 4: Handle Media Playback Execution

* Safely interact with your app's media player instance (e.g., `MediaPlayer` or `ExoPlayer`).
* Ensure you request **Audio Focus** (`AudioManager.requestAudioFocus`) before resuming playback to cooperate correctly with other system audio streams on Android 8.0.

---

### Key Takeaways for 100% Android 8.0 Compatibility

* **Never use static Manifest receivers** for `ACTION_HEADSET_PLUG`. Android 8.0 will silently ignore them.
* **Always use `startForegroundService()**` followed quickly by `startForeground()`. Failing to promote the service will trigger a crash on Oreo.
* **Always unregister** the dynamic receiver in `onDestroy()` of your service to avoid memory leaks.
