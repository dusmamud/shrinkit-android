package com.dusrel.shrinkit.presentation.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.domain.model.ProcessingStatus
import com.dusrel.shrinkit.domain.model.BackgroundColor
import com.dusrel.shrinkit.domain.model.TargetFormat
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.components.FullscreenImageViewer
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer

/**
 * Compress to 50KB / 20KB tool (web parity: /tools/compress-to-50kb).
 * Single image → target size + EXIF + format + optional crop (aspect lock) → compress.
 */
@Composable
fun KbToolScreen(
    toolId: String,
    toolViewModel: ShrinkItViewModel,
    onBack: () -> Unit,
    onOpenCrop: (String) -> Unit,
    onProcessComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val defaultKb = if (toolId == "kb20") 20 else 50
    val is20 = toolId == "kb20"

    val images by toolViewModel.uiState.collectAsStateWithLifecycle()
    val item = images.images.firstOrNull()
    val options = images.options

    // NOTE: fresh tool state is prepared at navigation time
    // (prepareKbTool in NavHost). No clearing here — the screen is
    // recreated when returning from the crop editor, and clearing
    // would wipe the selected (cropped) image.

    // Navigate to the result screen once, on the completing transition.
    // (Keyed on isProcessing with a wasProcessing guard so returning
    // from the result screen doesn't re-trigger navigation.)
    var wasProcessing by remember { mutableStateOf(false) }
    val isProcessingNow = images.isProcessing
    LaunchedEffect(isProcessingNow) {
        if (wasProcessing && !isProcessingNow &&
            item?.status == ProcessingStatus.COMPLETED
        ) {
            onProcessComplete()
        }
        wasProcessing = isProcessingNow
    }

    // Cropped preview (decoded + cropped bitmap) when a crop is set.
    val croppedPreview by androidx.compose.runtime.produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = item?.id,
        key2 = item?.cropRect
    ) {
        value = if (item?.cropRect != null) {
            toolViewModel.getCroppedPreview(item)
        } else {
            null
        }
    }

    var showFullscreen by remember { mutableStateOf(false) }
    if (showFullscreen && item != null) {
        FullscreenImageViewer(
            uri = item.uri,
            onDismiss = { showFullscreen = false }
        )
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            toolViewModel.clearAllImages()
            toolViewModel.addImages(listOf(uri))
        }
    }

    var customKb by remember(defaultKb) { mutableStateOf(defaultKb.toString()) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                title = stringResource(if (is20) R.string.tool_kb20_title else R.string.tool_kb50_title),
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(if (is20) R.string.tool_kb20_desc else R.string.tool_kb50_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (item == null) {
                item {
                    Card(
                        onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SymbolIcon(
                                name = "add_photo_alternate",
                                contentDescription = null,
                                fontSize = 48.sp,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.tool_select_image),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // Selected image: cropped preview when a crop is set,
                // tap for fullscreen, clear + crop actions.
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Thumbnail shows the CROPPED image after crop apply.
                        val thumbModel: Any = croppedPreview ?: item.uri
                        AsyncImage(
                            model = thumbModel,
                            contentDescription = item.filename,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showFullscreen = true }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.filename,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Text(
                                text = "${item.originalWidth}×${item.originalHeight} · ${formatKb(item.originalSizeBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.cropRect != null) {
                                Text(
                                    text = stringResource(R.string.tool_cropped_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = { onOpenCrop(item.id) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            SymbolIcon(
                                name = "crop",
                                contentDescription = stringResource(R.string.action_crop),
                                tint = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp
                            )
                        }
                        IconButton(
                            onClick = { toolViewModel.clearAllImages() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            SymbolIcon(
                                name = "close",
                                contentDescription = stringResource(R.string.action_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                // Target size.
                item {
                    ToolSection(title = stringResource(R.string.tool_target_size)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(20, 50, 100, 200, 500).forEach { kb ->
                                FilterChip(
                                    selected = options.targetKb == kb,
                                    onClick = {
                                        toolViewModel.updateTargetKb(kb)
                                        customKb = kb.toString()
                                    },
                                    label = { Text("$kb") }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customKb,
                            onValueChange = { str ->
                                customKb = str.filter { it.isDigit() }.take(5)
                                customKb.toIntOrNull()?.let { toolViewModel.updateTargetKb(it) }
                            },
                            label = { Text(stringResource(R.string.tool_target_size_kb)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            suffix = { Text("KB") }
                        )
                    }
                }

                // EXIF remover (default ON).
                item {
                    ToolSection(title = stringResource(R.string.tool_privacy)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.opt_strip_exif),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = options.stripExif,
                                onCheckedChange = { toolViewModel.toggleStripExif() }
                            )
                        }
                    }
                }

                // Output format.
                item {
                    ToolSection(title = stringResource(R.string.section_format)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(TargetFormat.JPEG, TargetFormat.PNG, TargetFormat.WEBP).forEach { format ->
                                val label = when (format) {
                                    TargetFormat.JPEG -> stringResource(R.string.format_jpg)
                                    TargetFormat.PNG -> stringResource(R.string.format_png)
                                    else -> stringResource(R.string.format_webp)
                                }
                                FilterChip(
                                    selected = options.format == format,
                                    onClick = { toolViewModel.updateFormat(format) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                // Background color — only when the input may be transparent
                // (PNG/WebP) and the output drops alpha (JPEG).
                val needsBg = item != null &&
                    (item.mimeType.contains("png", ignoreCase = true) ||
                        item.mimeType.contains("webp", ignoreCase = true)) &&
                    options.format == TargetFormat.JPEG
                if (needsBg) {
                    item {
                        ToolSection(title = stringResource(R.string.section_background)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    BackgroundColor.WHITE to stringResource(R.string.bg_white),
                                    BackgroundColor.BLACK to stringResource(R.string.bg_black)
                                ).forEach { (bg, label) ->
                                    FilterChip(
                                        selected = options.backgroundColor == bg,
                                        onClick = { toolViewModel.updateBackgroundColor(bg) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Black & White (20KB only, web parity).
                if (is20) {
                    item {
                        ToolSection(title = stringResource(R.string.tool_bw_title)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.tool_bw_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = options.blackWhite,
                                    onCheckedChange = { toolViewModel.setBlackWhite(it) }
                                )
                            }
                        }
                    }
                }

                // Compress button / result.
                item {
                    val isProcessing = images.isProcessing
                    val done = item.status == ProcessingStatus.COMPLETED
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { toolViewModel.processBatch() },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LocalButtonContainer.current
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(stringResource(R.string.status_processing_simple))
                            } else {
                                Text(
                                    stringResource(R.string.tool_compress_action, options.targetKb),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (done) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${formatKb(item.originalSizeBytes)} → ${formatKb(item.processedSizeBytes)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.tool_saved_percent,
                                                item.reductionPercent
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    SymbolIcon(
                                        name = "check_circle",
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        fontSize = 32.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

/** Shared flat section wrapper for tool screens (Minimal style). */
@Composable
fun ToolSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

private fun formatKb(bytes: Long): String {
    return if (bytes < 1024) "$bytes B"
    else String.format("%.1f KB", bytes / 1024f)
}
