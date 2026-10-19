package com.dusrel.shrinkit.presentation.results

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.data.storage.MediaStoreExporter
import com.dusrel.shrinkit.domain.model.ImageItem
import com.dusrel.shrinkit.domain.model.ProcessingStatus
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.components.FullscreenImageViewer
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer
import com.dusrel.shrinkit.presentation.theme.LocalSuccessBg
import java.text.DecimalFormat

/**
 * Dedicated results screen (Exa best practice: results get their own
 * screen — they never replace the input list).
 *
 * Layouts: 1 = hero, 2 = stacked cards, 3+ = grid.
 * 16dp margins, staggered fade-in, proper back navigation.
 */
@Composable
fun ResultScreen(
    toolViewModel: ShrinkItViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by toolViewModel.uiState.collectAsStateWithLifecycle()
    val results = uiState.images.filter { it.status == ProcessingStatus.COMPLETED }
    val totalSaved = results.sumOf { it.originalSizeBytes - it.processedSizeBytes }

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.results_title, results.size),
                onBackClick = onBack
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = {
                            toolViewModel.clearAllImages()
                            onDone()
                        }
                    ) {
                        Text(stringResource(R.string.results_new_batch))
                    }
                    Button(
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalButtonContainer.current,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(R.string.action_done),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary bar.
            if (results.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.results_summary,
                        results.size,
                        formatBytes(totalSaved)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when {
                results.size == 1 -> {
                    // Hero layout for a single result.
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(300))
                            ) {
                                HeroResultCard(item = results[0])
                            }
                        }
                    }
                }
                results.size == 2 -> {
                    // Two stacked cards.
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(results, key = { it.id }) { item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(300))
                            ) {
                                CompactResultCard(item = item)
                            }
                        }
                    }
                }
                else -> {
                    // Grid for 3+.
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(results, key = { _, it -> it.id }) { index, item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(300, delayMillis = index * 60))
                            ) {
                                GridResultCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Hero card: large image + full stats + Save/Share. */
@Composable
private fun HeroResultCard(item: ImageItem) {
    val context = LocalContext.current
    var showFullscreen by remember(item.id) { mutableStateOf(false) }
    if (showFullscreen && item.processedUri != null) {
        FullscreenImageViewer(uri = item.processedUri!!, onDismiss = { showFullscreen = false })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.processedUri)
                    .size(Size(1200, 1200))
                    .build(),
                contentDescription = item.filename,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showFullscreen = true }
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.filename,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                ResultStatsRow(item = item)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { saveResult(context, item) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalButtonContainer.current,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        SymbolIcon(name = "download", contentDescription = null, fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_save_gallery))
                    }
                    Button(
                        onClick = { shareResult(context, item) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        SymbolIcon(name = "share", contentDescription = null, fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_share))
                    }
                }
            }
        }
    }
}

/** Stacked card for the 2-image layout. */
@Composable
private fun CompactResultCard(item: ImageItem) {
    val context = LocalContext.current
    var showFullscreen by remember(item.id) { mutableStateOf(false) }
    if (showFullscreen && item.processedUri != null) {
        FullscreenImageViewer(uri = item.processedUri!!, onDismiss = { showFullscreen = false })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.processedUri)
                    .size(Size(400, 400))
                    .build(),
                contentDescription = item.filename,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFullscreen = true }
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.filename,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                ResultStatsRow(item = item, compact = true)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { saveResult(context, item) }) {
                    SymbolIcon(
                        name = "download",
                        contentDescription = stringResource(R.string.action_save_gallery),
                        tint = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp
                    )
                }
                IconButton(onClick = { shareResult(context, item) }) {
                    SymbolIcon(
                        name = "share",
                        contentDescription = stringResource(R.string.action_share),
                        tint = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

/** Grid cell for 3+ results. Tap opens fullscreen. */
@Composable
private fun GridResultCard(item: ImageItem) {
    val context = LocalContext.current
    var showFullscreen by remember(item.id) { mutableStateOf(false) }
    if (showFullscreen && item.processedUri != null) {
        FullscreenImageViewer(uri = item.processedUri!!, onDismiss = { showFullscreen = false })
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable { showFullscreen = true }
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.processedUri)
                        .size(Size(600, 600))
                        .build(),
                    contentDescription = item.filename,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                if (item.reductionPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LocalSuccessBg.current,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "-${item.reductionPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.filename,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatBytes(item.originalSizeBytes)} → ${formatBytes(item.processedSizeBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Original → compressed stats with reduction badge. */
@Composable
private fun ResultStatsRow(item: ImageItem, compact: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
            Text(
                text = "${formatBytes(item.originalSizeBytes)} → ${formatBytes(item.processedSizeBytes)}",
                style = if (compact) MaterialTheme.typography.bodySmall
                else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${item.originalWidth}x${item.originalHeight} → ${item.processedWidth}x${item.processedHeight}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (item.reductionPercent > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = LocalSuccessBg.current
            ) {
                Text(
                    text = "-${item.reductionPercent}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun saveResult(context: Context, item: ImageItem) {
    val uri: Uri = item.processedUri ?: return
    try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        val ext = when {
            item.mimeType.contains("png") -> "png"
            item.mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        MediaStoreExporter.saveToPictures(
            context = context,
            data = bytes,
            filename = "shrinkit_${item.filename.substringBeforeLast(".")}.$ext",
            mimeType = item.mimeType.ifEmpty { "image/jpeg" }
        )
        android.widget.Toast.makeText(
            context,
            context.getString(R.string.toast_saved_gallery),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    } catch (_: Exception) {
    }
}

private fun shareResult(context: Context, item: ImageItem) {
    val uri: Uri = item.processedUri ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = item.mimeType.ifEmpty { "image/*" }
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Image"))
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val df = DecimalFormat("#.##")
    return when {
        bytes >= 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
        else -> "${df.format(bytes / 1024.0)} KB"
    }
}
