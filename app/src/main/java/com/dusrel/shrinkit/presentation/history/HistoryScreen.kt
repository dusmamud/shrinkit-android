package com.dusrel.shrinkit.presentation.history

import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.data.local.ProcessedImageEntity
import com.dusrel.shrinkit.presentation.components.HeaderIconButton
import com.dusrel.shrinkit.presentation.components.FullscreenImageViewer
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.theme.LocalSuccessBg
import java.util.Date

private enum class HistoryView { LIST, GRID }

/**
 * Past processed images — Minimal flat design (no cards):
 * flat rows in list view, flat rounded thumbnails in grid view.
 * Per-item actions live in a ⋮ overflow menu; the header ⋮ menu holds
 * List/Grid view toggle and Clear all.
 */
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }
    var fullscreenEntity by remember { mutableStateOf<ProcessedImageEntity?>(null) }
    var showHeaderMenu by remember { mutableStateOf(false) }
    var view by rememberSaveable { mutableStateOf(HistoryView.LIST) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    val countSubtitle = if (history.isEmpty()) {
        null
    } else {
        stringResource(R.string.history_count, history.size)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.history_title),
                subtitle = countSubtitle,
                actions = {
                    if (history.isNotEmpty()) {
                        Box {
                            HeaderIconButton(onClick = { showHeaderMenu = true }) {
                                SymbolIcon(
                                    name = "more_vert",
                                    contentDescription = stringResource(R.string.history_options),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showHeaderMenu,
                                onDismissRequest = { showHeaderMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.history_view_list)) },
                                    onClick = {
                                        view = HistoryView.LIST
                                        showHeaderMenu = false
                                    },
                                    leadingIcon = {
                                        SymbolIcon(
                                            name = "view_list",
                                            contentDescription = null,
                                            tint = if (view == HistoryView.LIST)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = {
                                        if (view == HistoryView.LIST) {
                                            SymbolIcon(
                                                name = "check",
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.history_view_grid)) },
                                    onClick = {
                                        view = HistoryView.GRID
                                        showHeaderMenu = false
                                    },
                                    leadingIcon = {
                                        SymbolIcon(
                                            name = "grid_view",
                                            contentDescription = null,
                                            tint = if (view == HistoryView.GRID)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = {
                                        if (view == HistoryView.GRID) {
                                            SymbolIcon(
                                                name = "check",
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.history_clear_all)) },
                                    onClick = {
                                        showHeaderMenu = false
                                        showClearConfirm = true
                                    },
                                    leadingIcon = {
                                        SymbolIcon(
                                            name = "delete",
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SymbolIcon(
                    name = "history",
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    fontSize = 64.sp,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.history_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.history_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (view == HistoryView.LIST) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 12.dp, top = 8.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    history,
                    key = { it.id },
                    contentType = { "history_item" }
                ) { entity ->
                    HistoryRow(
                        entity = entity,
                        onOpen = { fullscreenEntity = entity },
                        onShare = { viewModel.share(entity) },
                        onDelete = { viewModel.delete(entity) }
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    history,
                    key = { it.id },
                    contentType = { "history_item" }
                ) { entity ->
                    HistoryTile(
                        entity = entity,
                        onOpen = { fullscreenEntity = entity },
                        onShare = { viewModel.share(entity) },
                        onDelete = { viewModel.delete(entity) }
                    )
                }
            }
        }
    }

    fullscreenEntity?.let { entity ->
        FullscreenImageViewer(
            uri = Uri.parse(entity.contentUri),
            onDismiss = { fullscreenEntity = null },
            onShare = { viewModel.share(entity) }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = { Text(stringResource(R.string.history_clear_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearConfirm = false
                }) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

/** Flat list row — no card, spacing separates rows. */
@Composable
private fun HistoryRow(
    entity: ProcessedImageEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val request = remember(entity.contentUri) {
        ImageRequest.Builder(context)
            .data(Uri.parse(entity.contentUri))
            .size(Size(200, 200))
            .build()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entity.filename,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = historyStatsLine(entity),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        ReductionBadge(reductionPercent(entity))
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                SymbolIcon(
                    name = "more_vert",
                    contentDescription = stringResource(R.string.history_options)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_share)) },
                    onClick = {
                        showMenu = false
                        onShare()
                    },
                    leadingIcon = {
                        SymbolIcon(name = "share", contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_delete)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        SymbolIcon(
                            name = "delete",
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

/** Flat grid tile — rounded thumbnail, text below, ⋮ overlay. */
@Composable
private fun HistoryTile(
    entity: ProcessedImageEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val request = remember(entity.contentUri) {
        ImageRequest.Builder(context)
            .data(Uri.parse(entity.contentUri))
            .size(Size(600, 360))
            .build()
    }
    Column(modifier = modifier) {
        Box {
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onOpen() }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                        )
                ) {
                    SymbolIcon(
                        name = "more_vert",
                        contentDescription = stringResource(R.string.history_options)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_share)) },
                        onClick = {
                            showMenu = false
                            onShare()
                        },
                        leadingIcon = {
                            SymbolIcon(name = "share", contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            SymbolIcon(
                                name = "delete",
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = entity.filename,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ReductionBadge(reductionPercent(entity))
            Spacer(Modifier.width(6.dp))
            Text(
                text = formatBytes(entity.processedBytes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReductionBadge(percent: Int) {
    Text(
        text = "-$percent%",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(LocalSuccessBg.current.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun historyStatsLine(entity: ProcessedImageEntity): String {
    val context = LocalContext.current
    val date = remember(entity.createdAt) {
        DateFormat.getMediumDateFormat(context).format(Date(entity.createdAt))
    }
    return "${formatBytes(entity.originalBytes)} → ${formatBytes(entity.processedBytes)} · " +
        "${entity.width}×${entity.height} · $date"
}

private fun reductionPercent(entity: ProcessedImageEntity): Int {
    if (entity.originalBytes <= 0) return 0
    return (((entity.originalBytes - entity.processedBytes).toDouble()
        / entity.originalBytes.toDouble()) * 100).toInt().coerceAtLeast(0)
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    return when {
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.0f KB", bytes / 1024.0)
    }
}
