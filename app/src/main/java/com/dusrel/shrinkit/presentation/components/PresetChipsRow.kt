package com.dusrel.shrinkit.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.domain.model.ImagePreset

@Composable
fun PresetChipsRow(
    selectedPresetId: String?,
    onPresetSelected: (ImagePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.preset_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImagePreset.ALL.forEach { preset ->
                val isSelected = preset.id == selectedPresetId
                val label = when (preset.id) {
                    "passport_in" -> stringResource(R.string.preset_passport)
                    "signature_in" -> stringResource(R.string.preset_exam_signature)
                    "passport_us" -> stringResource(R.string.preset_us_visa)
                    "instagram_post" -> stringResource(R.string.preset_insta_square)
                    "instagram_story" -> stringResource(R.string.preset_insta_story)
                    "youtube_thumb" -> stringResource(R.string.preset_yt_thumb)
                    else -> preset.description
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onPresetSelected(preset) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            SymbolIcon(
                                name = "check",
                                contentDescription = null,
                                filled = true,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
