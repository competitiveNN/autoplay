package com.fra.autoplay.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Compose-based settings screen that mirrors the original SettingsFragment.
 * Uses shared UI components from the :compose module.
 *
 * All preference operations are delegated to the host activity via callbacks,
 * keeping the :compose module decoupled from app internals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoPlaySettingsScreen(
    onAboutClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    delayEnabled: Boolean,
    delaySubtitle: String,
    filterEnabled: Boolean,
    filterSubtitle: String,
    batteryEnabled: Boolean,
    smartResumeEnabled: Boolean,
    smartResumeSubtitle: String,
    diagnosticsEnabled: Boolean,
    diagnosticsSubtitle: String,
    themeOptions: List<String>,
    selectedTheme: String,
    onDelayToggle: (Boolean) -> Unit,
    onFilterToggle: (Boolean) -> Unit,
    onBatteryToggle: (Boolean) -> Unit,
    onSmartResumeToggle: (Boolean) -> Unit,
    onDiagnosticsToggle: (Boolean) -> Unit,
    onThemeSelected: (String) -> Unit,
    settingsTitle: String,
    resumeDelayTitle: String,
    filterHeadphonesTitle: String,
    batteryReminderTitle: String,
    smartResumeTitle: String,
    themeTitle: String,
    diagnosticsTitle: String,
    backupTitle: String,
    restoreTitle: String,
    aboutTitle: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(settingsTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingSectionHeader(title = "Media Playback")
            }
            item {
                SettingsCard {
                    SwitchSettingItem(
                        title = resumeDelayTitle,
                        subtitle = delaySubtitle,
                        checked = delayEnabled,
                        onCheckedChange = onDelayToggle
                    )
                    SwitchSettingItem(
                        title = filterHeadphonesTitle,
                        subtitle = filterSubtitle,
                        checked = filterEnabled,
                        onCheckedChange = onFilterToggle
                    )
                    SwitchSettingItem(
                        title = batteryReminderTitle,
                        checked = batteryEnabled,
                        onCheckedChange = onBatteryToggle
                    )
                    SwitchSettingItem(
                        title = smartResumeTitle,
                        subtitle = smartResumeSubtitle,
                        checked = smartResumeEnabled,
                        onCheckedChange = onSmartResumeToggle
                    )
                }
            }

            item {
                SettingSectionHeader(title = "Appearance")
            }
            item {
                SettingsCard {
                    DropdownSettingItem(
                        title = themeTitle,
                        options = themeOptions,
                        selectedOption = selectedTheme,
                        onOptionSelected = onThemeSelected
                    )
                }
            }

            item {
                SettingSectionHeader(title = "Diagnostics")
            }
            item {
                SettingsCard {
                    SwitchSettingItem(
                        title = diagnosticsTitle,
                        subtitle = diagnosticsSubtitle,
                        checked = diagnosticsEnabled,
                        onCheckedChange = onDiagnosticsToggle
                    )
                }
            }

            item {
                SettingSectionHeader(title = "Backup & Restore")
            }
            item {
                SettingsCard {
                    ButtonSettingItem(
                        title = backupTitle,
                        subtitle = "Export preferences to JSON",
                        onClick = onBackupClick
                    )
                    ButtonSettingItem(
                        title = restoreTitle,
                        subtitle = "Import preferences from JSON",
                        onClick = onRestoreClick
                    )
                }
            }

            item {
                SettingSectionHeader(title = "About")
            }
            item {
                SettingsCard {
                    ButtonSettingItem(
                        title = aboutTitle,
                        subtitle = "Version, license, and information",
                        onClick = onAboutClick
                    )
                }
            }
        }
    }
}