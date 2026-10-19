package com.dusrel.shrinkit.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.domain.model.BackgroundColor
import com.dusrel.shrinkit.domain.model.ProcessingOptions
import com.dusrel.shrinkit.domain.model.ResizeMode
import com.dusrel.shrinkit.domain.model.SizeUnit
import com.dusrel.shrinkit.domain.model.TargetFormat

/**
 * Web parity (/resize SettingsPanel): width/height + aspect lock + unit,
 * DPI, resize mode (stretch/crop/fit), format, quality (JPEG/WebP only),
 * background. No compress-mode toggle, no target-KB, no EXIF switch —
 * those live on the dedicated tool screens (Phase 2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    options: ProcessingOptions,
    onWidthChanged: (Float) -> Unit,
    onHeightChanged: (Float) -> Unit,
    onUnitChanged: (SizeUnit) -> Unit,
    onToggleAspectLock: () -> Unit,
    onResizeModeChanged: (ResizeMode) -> Unit,
    onDpiChanged: (Int) -> Unit,
    onFormatChanged: (TargetFormat) -> Unit,
    onQualityChanged: (Int) -> Unit,
    onBackgroundColorChanged: (BackgroundColor) -> Unit,
    modifier: Modifier = Modifier
) {
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    // Web rule: resize mode row only matters when unlocked and W != H.
    val showResizeMode = !options.keepAspectRatio && options.width != options.height
    // Web rule: quality slider only applies to JPEG/WebP (PNG/GIF force 100).
    val showQuality = options.format == TargetFormat.JPEG || options.format == TargetFormat.WEBP

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Dimensions & Unit
            Text(
                text = stringResource(R.string.section_dimensions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (options.width % 1 == 0f) options.width.toInt().toString() else options.width.toString(),
                    onValueChange = { str ->
                        str.toFloatOrNull()?.let { onWidthChanged(it) }
                    },
                    label = { Text(stringResource(R.string.label_width)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(
                    onClick = onToggleAspectLock,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    SymbolIcon(
                        name = if (options.keepAspectRatio) "lock" else "lock_open",
                        contentDescription = stringResource(R.string.label_aspect_lock),
                        tint = if (options.keepAspectRatio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = if (options.height % 1 == 0f) options.height.toInt().toString() else options.height.toString(),
                    onValueChange = { str ->
                        str.toFloatOrNull()?.let { onHeightChanged(it) }
                    },
                    label = { Text(stringResource(R.string.label_height)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Unit Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = unitDropdownExpanded,
                onExpandedChange = { unitDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = when (options.unit) {
                        SizeUnit.PERCENT -> stringResource(R.string.unit_percent)
                        SizeUnit.PIXELS -> stringResource(R.string.unit_pixels)
                        SizeUnit.CM -> stringResource(R.string.unit_cm)
                        SizeUnit.INCHES -> stringResource(R.string.unit_inches)
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_unit)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = unitDropdownExpanded,
                    onDismissRequest = { unitDropdownExpanded = false }
                ) {
                    SizeUnit.entries.forEach { unit ->
                        val label = when (unit) {
                            SizeUnit.PERCENT -> stringResource(R.string.unit_percent)
                            SizeUnit.PIXELS -> stringResource(R.string.unit_pixels)
                            SizeUnit.CM -> stringResource(R.string.unit_cm)
                            SizeUnit.INCHES -> stringResource(R.string.unit_inches)
                        }
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onUnitChanged(unit)
                                unitDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Section 2: DPI (web parity: 72/150/300)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.label_dpi),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(72, 150, 300).forEach { dpi ->
                        FilterChip(
                            selected = options.dpi == dpi,
                            onClick = { onDpiChanged(dpi) },
                            label = { Text(dpi.toString()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 3: Resize Mode — web rule: only when unlocked and W != H
            if (showResizeMode) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.section_mode),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResizeMode.entries.forEach { mode ->
                            val isSelected = options.resizeMode == mode
                            val label = when (mode) {
                                ResizeMode.STRETCH -> stringResource(R.string.mode_stretch)
                                ResizeMode.CROP -> stringResource(R.string.mode_crop)
                                ResizeMode.FIT -> stringResource(R.string.mode_fit)
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { onResizeModeChanged(mode) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section 4: Output Format
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.section_format),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // AVIF has no Android framework encoder (Bitmap.compress), so it is
                    // not offered as an output format. GIF needs a custom encoder —
                    // deferred (web parity gap, tracked separately).
                    TargetFormat.entries.filter { it != TargetFormat.AVIF }.forEach { format ->
                        val isSelected = options.format == format
                        val label = when (format) {
                            TargetFormat.ORIGINAL -> stringResource(R.string.format_original)
                            TargetFormat.JPEG -> stringResource(R.string.format_jpg)
                            TargetFormat.PNG -> stringResource(R.string.format_png)
                            TargetFormat.WEBP -> stringResource(R.string.format_webp)
                            TargetFormat.AVIF -> stringResource(R.string.format_avif)
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFormatChanged(format) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Section 5: Quality — web rule: JPEG/WebP only
            if (showQuality) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(R.string.label_quality)}: ${options.quality}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Slider(
                        value = options.quality.toFloat(),
                        onValueChange = { onQualityChanged(it.toInt()) },
                        valueRange = 1f..100f
                    )
                }
            }

            // Section 6: Background Color
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.section_background),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = options.backgroundColor == BackgroundColor.WHITE,
                        onClick = { onBackgroundColorChanged(BackgroundColor.WHITE) },
                        label = { Text(stringResource(R.string.bg_white)) }
                    )
                    FilterChip(
                        selected = options.backgroundColor == BackgroundColor.BLACK,
                        onClick = { onBackgroundColorChanged(BackgroundColor.BLACK) },
                        label = { Text(stringResource(R.string.bg_black)) }
                    )
                }
            }
        }
    }
}
