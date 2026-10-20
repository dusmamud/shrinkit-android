package com.dusrel.shrinkit.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.domain.model.TargetFormat
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.AppSlider
import kotlinx.coroutines.launch

/** Dedicated Defaults page: default quality slider + default format chips. */
@Composable
fun DefaultsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val prefs by viewModel.prefs.preferences.collectAsState(
        initial = com.dusrel.shrinkit.data.local.UserPreferences()
    )

    var qualitySliderValue by remember(prefs.defaultQuality) {
        mutableFloatStateOf(prefs.defaultQuality.toFloat())
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.settings_defaults),
                onBackClick = onBack
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            AppSlider(
                label = stringResource(R.string.settings_default_quality_label),
                valueText = "${qualitySliderValue.toInt()}%",
                value = qualitySliderValue,
                onValueChange = { qualitySliderValue = it },
                onValueChangeFinished = {
                    scope.launch {
                        viewModel.prefs.setDefaultQuality(qualitySliderValue.toInt())
                    }
                },
                valueRange = 1f..100f,
                minHint = "1",
                maxHint = "100"
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_default_format),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_default_format_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
