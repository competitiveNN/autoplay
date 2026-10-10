# AutoPlay - R8 fullMode rules
# Keep app entry points referenced from AndroidManifest / system
-keep class com.fra.autoplay.MediaPlaybackService { *; }
-keep class com.fra.autoplay.BootReceiver { *; }
-keep class com.fra.autoplay.QuickSettingsTileService { *; }
-keep class com.fra.autoplay.AutoPlayWidgetProvider { *; }
-keep class com.fra.autoplay.BatteryOptimizationWorker { *; }
# Keep PreferencesHelper data class used via reflection/JSON
-keep class com.fra.autoplay.PreferencesHelper* { *; }
# WorkManager
-keep class androidx.work.Worker { *; }
