package com.dusrel.shrinkit.presentation.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.domain.model.ImagePreset
import com.dusrel.shrinkit.presentation.ShrinkItApp
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.components.ScreenTopBar

/**
 * Focused single-purpose tool page (web parity: /tools/*).
 * Reuses the full workspace engine with the tool's config pre-applied.
 */
@Composable
fun ToolDetailScreen(
    toolId: String,
    toolViewModel: ShrinkItViewModel,
    onBack: () -> Unit,
    onOpenCropEditor: (String) -> Unit,
    defaultQuality: Int,
    defaultFormatName: String,
    modifier: Modifier = Modifier
) {
    val tool = ALL_TOOLS.firstOrNull { it.id == toolId }

    ShrinkItApp(
        viewModel = toolViewModel,
        onNavigateBack = onBack,
        onOpenCropEditor = onOpenCropEditor,
        defaultQuality = defaultQuality,
        defaultFormatName = defaultFormatName,
        initialToolId = toolId,
        topBar = {
            ScreenTopBar(
                title = tool?.let { stringResource(it.titleRes) }
                    ?: stringResource(R.string.tools_title),
                onBackClick = onBack
            )
        },
        headerContent = {
            if (tool != null) {
                ToolHeader(
                    tool = tool,
                    viewModel = toolViewModel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ToolHeader(
    tool: ToolEntry,
    viewModel: ShrinkItViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(tool.descRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        when (tool.id) {
            "kb50", "kb20" -> {
                val kb = if (tool.id == "kb50") 50 else 20
                Text(
                    text = stringResource(R.string.tool_target_upto, kb),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            "passport" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("passport_in", "passport_us").forEach { presetId ->
                        val preset = ImagePreset.ALL.firstOrNull { it.id == presetId }
                            ?: return@forEach
                        FilterChip(
                            selected = uiState.selectedPresetId == presetId,
                            onClick = { viewModel.applyPreset(preset) },
                            label = {
                                Text(
                                    stringResource(
                                        if (presetId == "passport_in")
                                            R.string.preset_passport
                                        else R.string.preset_us_visa
                                    )
                                )
                            }
                        )
                    }
                }
            }
            "social" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("instagram_post", "instagram_story", "youtube_thumb")
                        .forEach { presetId ->
                            val preset = ImagePreset.ALL.firstOrNull { it.id == presetId }
                                ?: return@forEach
                            FilterChip(
                                selected = uiState.selectedPresetId == presetId,
                                onClick = { viewModel.applyPreset(preset) },
                                label = {
                                    Text(
                                        stringResource(
                                            when (presetId) {
                                                "instagram_post" -> R.string.preset_insta_square
                                                "instagram_story" -> R.string.preset_insta_story
                                                else -> R.string.preset_yt_thumb
                                            }
                                        )
                                    )
                                }
                            )
                        }
                }
            }
            // "compare": no extra config — compare toggle lives on each result.
        }
        Spacer(Modifier.height(4.dp))
    }
}
