package com.dusrel.shrinkit.presentation.tools

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.ShrinkItViewModel

/**
 * Passport Photo Maker (web parity: /tools/passport-photo-maker).
 * 2026-verified specs, all 300 DPI.
 */
@Composable
fun PassportToolScreen(
    toolViewModel: ShrinkItViewModel,
    onBack: () -> Unit,
    onOpenCropLocked: (imageId: String, ratio: Float, label: String) -> Unit,
    onProcessComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        WizardPreset(
            id = "india",
            label = "India 35×45mm",
            ratioLabel = "35:45",
            ratio = 35f / 45f,
            defaultWidth = 413,
            defaultHeight = 531
        ),
        WizardPreset(
            id = "us",
            label = "US 2×2in",
            ratioLabel = "1:1",
            ratio = 1f,
            defaultWidth = 600,
            defaultHeight = 600
        ),
        WizardPreset(
            id = "schengen",
            label = "Schengen 35×45mm",
            ratioLabel = "35:45",
            ratio = 35f / 45f,
            defaultWidth = 413,
            defaultHeight = 531
        ),
        WizardPreset(
            id = "uk",
            label = "UK 35×45mm",
            ratioLabel = "35:45",
            ratio = 35f / 45f,
            defaultWidth = 413,
            defaultHeight = 531
        )
    )
    CropWizardScreen(
        title = stringResource(R.string.tool_passport_title),
        description = stringResource(R.string.tool_passport_desc),
        presets = presets,
        showTargetSize = true,
        toolViewModel = toolViewModel,
        onBack = onBack,
        onOpenCropLocked = onOpenCropLocked,
        onProcessComplete = onProcessComplete,
        modifier = modifier
    )
}

/**
 * Social Media Resizer (web parity: /tools/social-media-resizer).
 * 2026-verified platform presets.
 */
@Composable
fun SocialToolScreen(
    toolViewModel: ShrinkItViewModel,
    onBack: () -> Unit,
    onOpenCropLocked: (imageId: String, ratio: Float, label: String) -> Unit,
    onProcessComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        WizardPreset(
            id = "ig-square",
            label = "IG Square",
            ratioLabel = "1:1",
            ratio = 1f,
            defaultWidth = 1080,
            defaultHeight = 1080
        ),
        WizardPreset(
            id = "ig-story",
            label = "IG Story",
            ratioLabel = "9:16",
            ratio = 9f / 16f,
            defaultWidth = 1080,
            defaultHeight = 1920
        ),
        WizardPreset(
            id = "ig-portrait",
            label = "IG Portrait",
            ratioLabel = "4:5",
            ratio = 4f / 5f,
            defaultWidth = 1080,
            defaultHeight = 1350
        ),
        WizardPreset(
            id = "yt-thumb",
            label = "YT Thumbnail",
            ratioLabel = "16:9",
            ratio = 16f / 9f,
            defaultWidth = 1280,
            defaultHeight = 720
        ),
        WizardPreset(
            id = "yt-banner",
            label = "YT Banner",
            ratioLabel = "16:9",
            ratio = 16f / 9f,
            defaultWidth = 2560,
            defaultHeight = 1440
        ),
        WizardPreset(
            id = "tw-post",
            label = "X Post",
            ratioLabel = "16:9",
            ratio = 16f / 9f,
            defaultWidth = 1200,
            defaultHeight = 675
        ),
        WizardPreset(
            id = "li-banner",
            label = "LinkedIn",
            ratioLabel = "4:1",
            ratio = 4f,
            defaultWidth = 1584,
            defaultHeight = 396
        )
    )
    CropWizardScreen(
        title = stringResource(R.string.tool_social_title),
        description = stringResource(R.string.tool_social_desc),
        presets = presets,
        showTargetSize = false,
        toolViewModel = toolViewModel,
        onBack = onBack,
        onOpenCropLocked = onOpenCropLocked,
        onProcessComplete = onProcessComplete,
        modifier = modifier
    )
}
