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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.dusrel.shrinkit.domain.model.CompressMode
import com.dusrel.shrinkit.domain.model.ProcessingStatus
import com.dusrel.shrinkit.domain.model.SizeUnit
import com.dusrel.shrinkit.domain.model.TargetFormat
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.components.FullscreenImageViewer
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer

/**
 * A crop preset for the wizard: ratio lock + default output pixels.
 */
data class WizardPreset(
    val id: String,
    val label: String,
    val ratioLabel: String,
    val ratio: Float,
    val defaultWidth: Int,
    val defaultHeight: Int
)

/**
 * Shared crop-first wizard for Passport and Social tools.
 *
 * Flow: preset select → pick image → crop screen (ratio locked) →
 * editor (W/H locked-ratio + optional target size) → process.
 * Width/height edits never break the locked aspect ratio.
 */
@Composable
fun CropWizardScreen(
    title: String,
    description: String,
    presets: List<WizardPreset>,
    showTargetSize: Boolean,
    toolViewModel: ShrinkItViewModel,
    onBack: () -> Unit,
    onOpenCropLocked: (imageId: String, ratio: Float, label: String) -> Unit,
    onProcessComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by toolViewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.images.firstOrNull()
    val options = uiState.options

    var selectedPreset by remember(presets) { mutableStateOf(presets.first()) }
    var targetKbText by remember { mutableStateOf("200") }

    // NOTE: fresh state is prepared at navigation time (prepareWizardTool).
    // No clearing here — the screen is recreated on crop return.

    // Auto-open the crop screen right after an image is picked (user spec:
    // "image select karte hi turant crop screen show hona chahiye").
    var pendingAutoCrop by remember { mutableStateOf(false) }
    LaunchedEffect(item?.id) {
        if (pendingAutoCrop && item != null) {
            pendingAutoCrop = false
            onOpenCropLocked(item.id, selectedPreset.ratio, selectedPreset.ratioLabel)
        }
    }

    // Navigate to the result screen once, on the completing transition.
    var wasProcessing by remember { mutableStateOf(false) }
    val isProcessingNow = uiState.isProcessing
    LaunchedEffect(isProcessingNow) {
        if (wasProcessing && !isProcessingNow &&
            item?.status == ProcessingStatus.COMPLETED
        ) {
            onProcessComplete()
        }
        wasProcessing = isProcessingNow
    }

    // Cropped preview when a crop is set.
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
            // Apply the preset's default output size in pixels.
            toolViewModel.updateUnit(SizeUnit.PIXELS)
            toolViewModel.updateWidth(selectedPreset.defaultWidth.toFloat())
            toolViewModel.updateHeight(selectedPreset.defaultHeight.toFloat())
            toolViewModel.updateFormat(TargetFormat.JPEG)
            // Crop opens automatically once the item is decoded.
            pendingAutoCrop = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { ScreenTopBar(title = title, onBackClick = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Preset chips.
            item {
                ToolSection(title = stringResource(R.string.wizard_choose_preset)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.chunked(2).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { preset ->
                                    FilterChip(
                                        selected = selectedPreset.id == preset.id,
                                        onClick = {
                                            selectedPreset = preset
                                            // Refresh defaults if an image is already picked.
                                            if (item != null) {
                                                toolViewModel.updateWidth(preset.defaultWidth.toFloat())
                                                toolViewModel.updateHeight(preset.defaultHeight.toFloat())
                                            }
                                        },
                                        label = { Text(preset.label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
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
                // Picked image → crop CTA (opens locked-ratio crop screen).
                // Thumbnail shows the CROPPED image after crop apply.
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            if (item.cropRect != null) {
                                Text(
                                    text = stringResource(
                                        R.string.wizard_cropped_ratio,
                                        selectedPreset.ratioLabel
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.wizard_crop_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                onOpenCropLocked(
                                    item.id,
                                    selectedPreset.ratio,
                                    selectedPreset.ratioLabel
                                )
                            }
                        ) {
                            SymbolIcon(name = "crop", contentDescription = null)
                            Spacer(Modifier.size(4.dp))
                            Text(stringResource(R.string.action_crop))
                        }
                    }
                }

                // Editor: W/H (locked ratio) + optional target size.
                // Shown after crop; W/H edits keep the locked ratio.
                if (item.cropRect != null) {
                    item {
                        ToolSection(title = stringResource(R.string.wizard_output_size)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = if (options.width % 1 == 0f) options.width.toInt().toString() else options.width.toString(),
                                    onValueChange = { str ->
                                        str.toFloatOrNull()?.let { toolViewModel.updateWidth(it) }
                                    },
                                    label = { Text(stringResource(R.string.label_width)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                SymbolIcon(
                                    name = "lock",
                                    contentDescription = stringResource(R.string.crop_ratio_locked),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                OutlinedTextField(
                                    value = if (options.height % 1 == 0f) options.height.toInt().toString() else options.height.toString(),
                                    onValueChange = { str ->
                                        str.toFloatOrNull()?.let { toolViewModel.updateHeight(it) }
                                    },
                                    label = { Text(stringResource(R.string.label_height)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    if (showTargetSize) {
                        item {
                            ToolSection(title = stringResource(R.string.tool_target_size)) {
                                OutlinedTextField(
                                    value = targetKbText,
                                    onValueChange = { str ->
                                        targetKbText = str.filter { it.isDigit() }.take(5)
                                    },
                                    label = { Text(stringResource(R.string.tool_target_size_kb)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    suffix = { Text("KB") }
                                )
                            }
                        }
                    }

                    item {
                        val isProcessing = uiState.isProcessing
                        val done = item.status == ProcessingStatus.COMPLETED
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (showTargetSize) {
                                        val kb = targetKbText.toIntOrNull()?.coerceIn(1, 2000) ?: 200
                                        toolViewModel.updateTargetKb(kb)
                                        toolViewModel.updateCompressMode(CompressMode.TARGET_KB)
                                    } else {
                                        toolViewModel.updateCompressMode(CompressMode.QUALITY)
                                        toolViewModel.updateQuality(90)
                                    }
                                    toolViewModel.processBatch()
                                },
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
                                        stringResource(R.string.wizard_process_action),
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
                                                text = "${item.processedWidth}×${item.processedHeight} · ${formatKb(item.processedSizeBytes)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = stringResource(R.string.wizard_saved),
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
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

private fun formatKb(bytes: Long): String {
    return if (bytes < 1024) "$bytes B"
    else String.format("%.1f KB", bytes / 1024f)
}
