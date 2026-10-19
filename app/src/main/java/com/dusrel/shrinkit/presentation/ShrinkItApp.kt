package com.dusrel.shrinkit.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.domain.model.ProcessingStatus
import com.dusrel.shrinkit.presentation.components.ImagePickerSection
import com.dusrel.shrinkit.presentation.components.ResultCard
import com.dusrel.shrinkit.presentation.components.SettingsPanel
import com.dusrel.shrinkit.presentation.components.ShrinkItTopBar
import com.dusrel.shrinkit.presentation.util.InAppReviewManager
import com.dusrel.shrinkit.presentation.util.InAppUpdateManager
import com.dusrel.shrinkit.domain.model.ProcessingOptions

@Composable
fun ShrinkItApp(
    viewModel: ShrinkItViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onProcessComplete: (() -> Unit)? = null,
    defaultQuality: Int? = null,
    defaultFormatName: String? = null,
    /** Focused tool id (kb50/kb20/passport/social/compare) — applies its config on entry. */
    initialToolId: String? = null,
    /** Override the top bar (tool detail screens show the tool title). */
    topBar: @Composable () -> Unit = { ShrinkItTopBar(onBackClick = onNavigateBack) },
    /** Extra header content rendered above the picker (tool badge/description). */
    headerContent: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val images by remember { viewModel.uiState.map { it.images }.distinctUntilChanged() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val isProcessing by remember { viewModel.uiState.map { it.isProcessing }.distinctUntilChanged() }.collectAsStateWithLifecycle(initialValue = false)
    val options by remember { viewModel.uiState.map { it.options }.distinctUntilChanged() }.collectAsStateWithLifecycle(initialValue = ProcessingOptions())
    val currentProgress by remember { viewModel.uiState.map { it.currentProgress }.distinctUntilChanged() }.collectAsStateWithLifecycle(initialValue = 0)
    val totalProgress by remember { viewModel.uiState.map { it.totalProgress }.distinctUntilChanged() }.collectAsStateWithLifecycle(initialValue = 0)
    val userMessage by remember { viewModel.uiState.map { it.userMessage }.distinctUntilChanged() }.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }

    // Apply Settings defaults when the tool opens (or when defaults change).
    LaunchedEffect(defaultQuality, defaultFormatName) {
        if (defaultQuality != null && defaultFormatName != null) {
            viewModel.applyPreferenceDefaults(defaultQuality, defaultFormatName)
        }
    }

    // Focused tool entry (Tools hub): apply the tool's locked config once.
    LaunchedEffect(initialToolId) {
        initialToolId?.let { viewModel.applyToolConfig(it) }
    }

    LaunchedEffect(Unit) {
        activity?.let { InAppUpdateManager.checkForUpdates(it) }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    // Trigger review flow + results navigation when batch successfully finishes.
    // Results get their own screen (best practice) — they never replace inputs.
    LaunchedEffect(isProcessing) {
        if (!isProcessing && images.isNotEmpty() && images.all { it.status == ProcessingStatus.COMPLETED }) {
            activity?.let { InAppReviewManager.requestReview(it) }
            onProcessComplete?.invoke()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(visible = images.isNotEmpty()) {
                Surface(
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(0.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.clearAllImages() },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            SymbolIcon(name = "clear_all", contentDescription = null)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(stringResource(R.string.action_clear_all))
                        }

                        Button(
                            onClick = { viewModel.processBatch() },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LocalButtonContainer.current,
                                contentColor = androidx.compose.ui.graphics.Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(stringResource(R.string.status_processing, currentProgress, totalProgress))
                            } else {
                                SymbolIcon(name = "auto_awesome", contentDescription = null, filled = true)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "${stringResource(R.string.action_process_all)} (${images.size})",
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
            item { headerContent() }

            // Hero / Picker Card
            item {
                ImagePickerSection(
                    onImagesSelected = { uris -> viewModel.addImages(uris) }
                )
            }

            // Selected & Processed Images Section — web parity: selected
            // images show at the TOP, above the settings panel.
            // (No crop action here: crop is against resize.)
            if (images.isNotEmpty()) {
                item {
                    Text(
                        text = "${stringResource(R.string.title_selected_images)} (${images.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                items(
                    items = images,
                    key = { it.id }
                ) { item ->
                    ResultCard(
                        item = item,
                        onRemove = { viewModel.removeImage(item.id) },
                        onRotate = { viewModel.rotateImage(item.id) },
                        modifier = Modifier.animateItem()
                    )
                }

                // Detailed Settings Panel — web parity: only visible once
                // images are selected, below the image list.
                item {
                    SettingsPanel(
                        options = options,
                        onWidthChanged = { viewModel.updateWidth(it) },
                        onHeightChanged = { viewModel.updateHeight(it) },
                        onUnitChanged = { viewModel.updateUnit(it) },
                        onToggleAspectLock = { viewModel.toggleKeepAspectRatio() },
                        onResizeModeChanged = { viewModel.updateResizeMode(it) },
                        onDpiChanged = { viewModel.updateDpi(it) },
                        onFormatChanged = { viewModel.updateFormat(it) },
                        onQualityChanged = { viewModel.updateQuality(it) },
                        onBackgroundColorChanged = { viewModel.updateBackgroundColor(it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
