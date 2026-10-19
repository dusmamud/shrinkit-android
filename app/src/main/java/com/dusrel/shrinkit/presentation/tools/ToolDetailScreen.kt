package com.dusrel.shrinkit.presentation.tools

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dusrel.shrinkit.presentation.ShrinkItViewModel

/**
 * Router for the 5 dedicated tool screens (Phase 2).
 * Each tool has its own flow; the generic workspace is no longer reused here.
 */
@Composable
fun ToolDetailScreen(
    toolId: String,
    toolViewModel: ShrinkItViewModel,
    onBack: () -> Unit,
    onOpenCrop: (imageId: String) -> Unit,
    onOpenCropLocked: (imageId: String, ratio: Float, label: String) -> Unit,
    onProcessComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (toolId) {
        "kb50", "kb20" -> KbToolScreen(
            toolId = toolId,
            toolViewModel = toolViewModel,
            onBack = onBack,
            onOpenCrop = onOpenCrop,
            onProcessComplete = onProcessComplete,
            modifier = modifier
        )
        "passport" -> PassportToolScreen(
            toolViewModel = toolViewModel,
            onBack = onBack,
            onOpenCropLocked = onOpenCropLocked,
            onProcessComplete = onProcessComplete,
            modifier = modifier
        )
        "social" -> SocialToolScreen(
            toolViewModel = toolViewModel,
            onBack = onBack,
            onOpenCropLocked = onOpenCropLocked,
            onProcessComplete = onProcessComplete,
            modifier = modifier
        )
        "compare" -> CompareToolScreen(
            toolViewModel = toolViewModel,
            onBack = onBack,
            onProcessComplete = onProcessComplete,
            modifier = modifier
        )
        else -> KbToolScreen(
            toolId = "kb50",
            toolViewModel = toolViewModel,
            onBack = onBack,
            onOpenCrop = onOpenCrop,
            modifier = modifier
        )
    }
}
