# Jetpack Compose Migration Plan

## Overview
This document outlines the plan to migrate AutoPlay from traditional XML/View-based UI to Jetpack Compose. The migration will be done incrementally to maintain app stability.

## Current State (v1.1.0)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **UI Framework**: XML layouts + ViewBinding
- **Theme**: Material 3 via XML themes
- **Dependencies**: androidx.compose.material3 already added for future use

## Migration Strategy: Incremental (Recommended)

### Phase 1: Foundation (Week 1-2)
- [ ] Enable Compose in `build.gradle.kts`:
  ```kotlin
  buildFeatures {
      compose = true
  }
  composeOptions {
      kotlinCompilerExtensionVersion = "1.5.11"
  }
  ```
- [ ] Add Compose BOM for version management
- [ ] Create `ComposeTheme.kt` with Material 3 color scheme (using existing colors)
- [ ] Set up `Preview` annotations for design system components

### Phase 2: Shared Components (Week 2-3)
- [ ] Create reusable Compose components:
  - `AutoPlaySwitch` (replaces MaterialSwitch)
  - `AutoPlayButton` (replaces MaterialButton)
  - `AutoPlayDialog` (replaces MaterialAlertDialogBuilder)
  - `AutoPlayText` (typography system)
- [ ] Add `@Preview` for each component in light/dark themes

### Phase 3: Settings Screen (Week 3-4)
- [ ] Migrate `SettingsFragment` → `SettingsScreen` Composable
- [ ] Use `ComposeView` in existing Fragment for gradual migration
- [ ] Implement preferences using `remember` + `SharedPreferences`
- [ ] Add `ExcludedAppsDialog` as Composable
- [ ] Test with Espresso + Compose testing rules

### Phase 4: Main Screen (Week 4-5)
- [ ] Migrate `MainActivity` to `ComponentActivity` + `setContent`
- [ ] Convert `activity_main.xml` → `MainScreen` Composable
- [ ] Add `Widget` state observation via `collectAsStateWithLifecycle()`

### Phase 5: About Screen (Week 5)
- [ ] Migrate `AboutActivity` → `AboutScreen` Composable
- [ ] Use `LazyColumn` for scrollable content

### Phase 6: Cleanup (Week 6)
- [ ] Remove XML layouts no longer used
- [ ] Remove unused ViewBinding code
- [ ] Update theme to pure Compose (`Theme.Material3.DayNight.NoActionBar`)
- [ ] Remove AppCompat dependency if no longer needed

## Dependencies to Add
```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.02.00"))

// Core Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.9.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

// Testing
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
```

## Architecture Considerations

### State Management
- Use `StateFlow` / `SharedFlow` for service state observation
- Convert `PreferencesHelper` to use `DataStore` for type-safe preferences
- Use `remember` + `mutableStateOf` for UI state

### Navigation
- Replace Activity/Fragment navigation with `Navigation Compose`
- Single Activity architecture with Compose navigation graph

### Theme Migration
```kotlin
// AutoPlayTheme.kt
@Composable
fun AutoPlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## Testing Strategy
- Unit tests: No change needed (business logic unchanged)
- Integration tests: Migrate Espresso → Compose Testing APIs
- Screenshot tests: Add `compose-test-rule` for visual regression

## Risks & Mitigation
| Risk | Mitigation |
|------|------------|
| Widget breakage | Keep widget as RemoteViews (not Compose) |
| Performance on older devices | Test on API 26+; use `ComposeView` incrementally |
| Learning curve | Pair programming; use Compose previews extensively |

## Timeline
- **Total**: ~6 weeks (part-time)
- **Milestone 1** (2 weeks): Foundation + shared components
- **Milestone 2** (2 weeks): Settings screen
- **Milestone 3** (2 weeks): Main + About screens + cleanup

## Rollback Plan
- Each phase committed to separate branch
- Feature flag to toggle Compose screens
- XML layouts preserved until full migration verified

## Success Criteria
- [ ] All screens render correctly in light/dark themes
- [ ] All existing tests pass
- [ ] No performance regression on API 26
- [ ] APK size not significantly increased
- [ ] Dynamic color works on Android 12+

---
*Generated: 2026-10-08*
*Target: AutoPlay v2.0.0*