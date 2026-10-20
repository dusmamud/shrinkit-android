package com.dusrel.shrinkit.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.data.local.ThemeMode
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.navigation.Destinations
import kotlinx.coroutines.launch

/**
 * Settings hub — Minimal flat rows (no cards). Appearance, Defaults and
 * About open dedicated drill-down screens; Notifications is an inline
 * toggle; Language and Rate open system / Play Store.
 */
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by viewModel.prefs.preferences.collectAsState(
        initial = com.dusrel.shrinkit.data.local.UserPreferences()
    )

    // Permission grant must be observable Compose state. Reading
    // checkSelfPermission once in composition never refreshes after the
    // permission dialog — the toggle then stays OFF even after Allow
    // (especially when the stored pref was already true, so DataStore
    // emits nothing new). Best practice: keep it in state and re-check
    // on ON_RESUME (covers grants made in system settings too).
    fun notifGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    var notificationsGranted by remember { mutableStateOf(notifGranted()) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                notificationsGranted = notifGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
        scope.launch { viewModel.prefs.setNotificationsEnabled(granted) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(title = stringResource(R.string.tab_settings))
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            SettingsRow(
                iconName = "palette",
                title = stringResource(R.string.settings_appearance),
                subtitle = stringResource(
                    when (prefs.themeMode) {
                        ThemeMode.SYSTEM -> R.string.theme_system
                        ThemeMode.LIGHT -> R.string.theme_light
                        ThemeMode.DARK -> R.string.theme_dark
                    }
                ),
                onClick = { onNavigate(Destinations.SETTINGS_APPEARANCE) }
            )
            SettingsRow(
                iconName = "settings",
                title = stringResource(R.string.settings_defaults),
                subtitle = stringResource(
                    R.string.settings_defaults_sub,
                    prefs.defaultQuality
                ),
                onClick = { onNavigate(Destinations.SETTINGS_DEFAULTS) }
            )
            Spacer(Modifier.height(10.dp))
            SettingsRow(
                iconName = "notifications",
                title = stringResource(R.string.settings_notifications),
                subtitle = stringResource(R.string.settings_notifications_sub),
                trailing = {
                    Switch(
                        checked = prefs.notificationsEnabled && notificationsGranted,
                        onCheckedChange = { checked ->
                            if (!checked) {
                                scope.launch {
                                    viewModel.prefs.setNotificationsEnabled(false)
                                }
                            } else if (notificationsGranted) {
                                scope.launch {
                                    viewModel.prefs.setNotificationsEnabled(true)
                                }
                            } else {
                                notifPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        }
                    )
                }
            )
            Spacer(Modifier.height(10.dp))
            SettingsRow(
                iconName = "language",
                title = stringResource(R.string.settings_language),
                subtitle = stringResource(R.string.settings_language_sub),
                onClick = { openLanguageSettings(context) }
            )
            SettingsRow(
                iconName = "star",
                title = stringResource(R.string.settings_rate),
                subtitle = stringResource(R.string.settings_rate_sub),
                onClick = { openPlayStore(context) }
            )
            SettingsRow(
                iconName = "info",
                title = stringResource(R.string.settings_about),
                subtitle = stringResource(R.string.settings_about_sub),
                onClick = { onNavigate(Destinations.SETTINGS_ABOUT) }
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.settings_privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private fun openLanguageSettings(context: android.content.Context) {
    try {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_LOCALE_SETTINGS)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

private fun openPlayStore(context: android.content.Context) {
    val pkg = context.packageName
    val launched = try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
        )
        true
    } catch (_: Exception) {
        false
    }
    if (!launched) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                )
            )
        } catch (_: Exception) {
        }
    }
}
