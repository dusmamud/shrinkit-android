package com.dusrel.shrinkit.presentation.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.data.local.ThemeMode
import com.dusrel.shrinkit.data.local.UserPreferencesRepository
import com.dusrel.shrinkit.domain.model.TargetFormat
import com.dusrel.shrinkit.presentation.util.BrowserLauncher
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val prefs: UserPreferencesRepository
) : ViewModel()

/**
 * App settings: appearance, processing defaults, language, about.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by viewModel.prefs.preferences.collectAsState(
        initial = com.dusrel.shrinkit.data.local.UserPreferences()
    )
    var showAbout by remember { mutableStateOf(false) }

    // Hoisted out of the LazyColumn item: survives item recomposition/disposal.
    var qualitySliderValue by remember(prefs.defaultQuality) {
        mutableFloatStateOf(prefs.defaultQuality.toFloat())
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { viewModel.prefs.setThemeMode(mode) }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = prefs.themeMode == mode,
                            onClick = {
                                scope.launch { viewModel.prefs.setThemeMode(mode) }
                            }
                        )
                        Text(
                            text = stringResource(
                                when (mode) {
                                    ThemeMode.SYSTEM -> R.string.theme_system
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                }
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_defaults)) {
                Text(
                    text = stringResource(R.string.settings_default_quality, prefs.defaultQuality),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = qualitySliderValue,
                    onValueChange = { qualitySliderValue = it },
                    onValueChangeFinished = {
                        scope.launch {
                            viewModel.prefs.setDefaultQuality(qualitySliderValue.toInt())
                        }
                    },
                    valueRange = 1f..100f,
                    steps = 98
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_default_format),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TargetFormat.ORIGINAL,
                        TargetFormat.JPEG,
                        TargetFormat.PNG,
                        TargetFormat.WEBP
                    ).forEach { format ->
                        val selected = prefs.defaultFormatName == format.name
                        FilterChip(
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    viewModel.prefs.setDefaultFormatName(format.name)
                                }
                            },
                            label = {
                                Text(
                                    stringResource(
                                        when (format) {
                                            TargetFormat.ORIGINAL -> R.string.format_original
                                            TargetFormat.JPEG -> R.string.format_jpg
                                            TargetFormat.PNG -> R.string.format_png
                                            else -> R.string.format_webp
                                        }
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_general)) {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(R.string.settings_language_sub),
                    onClick = {
                        try {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                            } else {
                                Intent(Settings.ACTION_LOCALE_SETTINGS)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SettingsRow(
                    icon = Icons.Filled.Star,
                    title = stringResource(R.string.settings_rate),
                    subtitle = stringResource(R.string.settings_rate_sub),
                    onClick = {
                        val pkg = context.packageName
                        val launched = try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=$pkg")
                                )
                            )
                            true
                        } catch (_: Exception) {
                            false
                        }
                        if (!launched) {
                            // Play Store app missing: fall back to the web listing.
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        android.net.Uri.parse(
                                            "https://play.google.com/store/apps/details?id=$pkg"
                                        )
                                    )
                                )
                            } catch (_: Exception) {
                            }
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.settings_about),
                    subtitle = stringResource(R.string.settings_about_sub),
                    onClick = { showAbout = true }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (showAbout) {
        val packageInfo = remember {
            runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.getOrNull()
        }
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.app_name)) },
            text = {
                Column {
                    Text(
                        stringResource(
                            R.string.about_version,
                            packageInfo?.versionName ?: "1.0.0"
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.about_body))
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        BrowserLauncher.openUrl(
                            context,
                            "https://shrinkit.dusrel.com/privacy"
                        )
                    }) {
                        Text(stringResource(R.string.about_privacy))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
