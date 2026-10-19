package com.dusrel.shrinkit.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.domain.model.ProcessingStatus
import com.dusrel.shrinkit.presentation.components.ImagePickerSection
import com.dusrel.shrinkit.presentation.components.PresetChipsRow
import com.dusrel.shrinkit.presentation.components.ResultCard
import com.dusrel.shrinkit.presentation.components.SettingsPanel
import com.dusrel.shrinkit.presentation.components.ShrinkItTopBar
import com.dusrel.shrinkit.presentation.theme.BrandBlue
import com.dusrel.shrinkit.presentation.util.InAppReviewManager
import com.dusrel.shrinkit.presentation.util.InAppUpdateManager

@Composable
fun ShrinkItApp(
    viewModel: ShrinkItViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        activity?.let { InAppUpdateManager.checkForUpdates(it) }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    // Trigger review flow when batch successfully finishes
    LaunchedEffect(uiState.isProcessing) {
        if (!uiState.isProcessing && uiState.images.isNotEmpty() && uiState.images.all { it.status == ProcessingStatus.COMPLETED }) {
            activity?.let { InAppReviewManager.requestReview(it) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ShrinkItTopBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(visible = uiState.images.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearAllImages() },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isProcessing
                        ) {
                            Icon(imageVector = Icons.Default.ClearAll, contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(stringResource(R.string.action_clear_all))
                        }

                        Button(
                            onClick = { viewModel.processBatch() },
                            enabled = !uiState.isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isProcessing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(R.string.status_processing, uiState.currentProgress, uiState.totalProgress))
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "${stringResource(R.string.action_process_all)} (${uiState.images.size})",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hero / Picker Card
            item {
                ImagePickerSection(
                    onImagesSelected = { uris -> viewModel.addImages(uris) }
                )
            }

            // Quick Presets Row
            item {
                PresetChipsRow(
                    selectedPresetId = uiState.selectedPresetId,
                    onPresetSelected = { preset -> viewModel.applyPreset(preset) }
                )
            }

            // Detailed Settings Panel
            item {
                SettingsPanel(
                    options = uiState.options,
                    onWidthChanged = { viewModel.updateWidth(it) },
                    onHeightChanged = { viewModel.updateHeight(it) },
                    onUnitChanged = { viewModel.updateUnit(it) },
                    onToggleAspectLock = { viewModel.toggleKeepAspectRatio() },
                    onResizeModeChanged = { viewModel.updateResizeMode(it) },
                    onFormatChanged = { viewModel.updateFormat(it) },
                    onCompressModeChanged = { viewModel.updateCompressMode(it) },
                    onQualityChanged = { viewModel.updateQuality(it) },
                    onTargetKbChanged = { viewModel.updateTargetKb(it) },
                    onBackgroundColorChanged = { viewModel.updateBackgroundColor(it) },
                    onToggleStripExif = { viewModel.toggleStripExif() }
                )
            }

            // Selected & Processed Images Section
            if (uiState.images.isNotEmpty()) {
                item {
                    Text(
                        text = "${stringResource(R.string.title_results)} (${uiState.images.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                items(
                    items = uiState.images,
                    key = { it.id }
                ) { item ->
                    ResultCard(
                        item = item,
                        onRemove = { viewModel.removeImage(item.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
