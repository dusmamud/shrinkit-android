package com.dusrel.shrinkit.presentation.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.theme.LocalDropzoneBg
import com.dusrel.shrinkit.presentation.theme.LocalSuccessBg
import com.dusrel.shrinkit.domain.model.ImageItem
import com.dusrel.shrinkit.domain.model.ProcessingStatus
import java.text.DecimalFormat

@Composable
fun ResultCard(
    item: ImageItem,
    onRemove: () -> Unit,
    onRotate: (() -> Unit)? = null,
    onCrop: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCompare by remember(item.id) { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalDropzoneBg.current)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.processedUri ?: item.uri)
                            .size(Size(200, 200))
                            .build(),
                        contentDescription = item.filename,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(68.dp)
                    )

                    if (item.status == ProcessingStatus.PROCESSING) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Metadata & Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.filename,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val origFormatted = formatBytes(item.originalSizeBytes)
                    val origDim = "${item.originalWidth}x${item.originalHeight}"

                    if (item.status == ProcessingStatus.COMPLETED) {
                        val procFormatted = formatBytes(item.processedSizeBytes)
                        val procDim = "${item.processedWidth}x${item.processedHeight}"

                        Text(
                            text = "$origFormatted → $procFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$origDim → $procDim",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "$origFormatted · $origDim",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Edit badges
                    if (item.rotationDegrees != 0 || item.cropRect != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (item.rotationDegrees != 0) {
                                EditBadge(text = "${item.rotationDegrees}°")
                            }
                            if (item.cropRect != null) {
                                EditBadge(text = stringResource(R.string.badge_cropped))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badges & Actions
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.status == ProcessingStatus.COMPLETED && item.reductionPercent > 0) {
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

                        if (item.status == ProcessingStatus.COMPLETED) {
                            IconButton(
                                onClick = { showCompare = !showCompare },
                                modifier = Modifier.size(36.dp)
                            ) {
                                SymbolIcon(
                                    name = "compare",
                                    contentDescription = stringResource(R.string.action_compare),
                                    tint = if (showCompare) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 20.sp,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { shareImage(context, item) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                SymbolIcon(
                                    name = "share",
                                    contentDescription = stringResource(R.string.action_share),
                                    tint = MaterialTheme.colorScheme.primary,
                                    fontSize = 20.sp,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Per-image edits (before processing)
                        if (item.status == ProcessingStatus.IDLE) {
                            if (onRotate != null) {
                                IconButton(
                                    onClick = onRotate,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    SymbolIcon(
                                        name = "rotate_right",
                                        contentDescription = stringResource(R.string.action_rotate),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 20.sp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (onCrop != null) {
                                IconButton(
                                    onClick = onCrop,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    SymbolIcon(
                                        name = "crop",
                                        contentDescription = stringResource(R.string.action_crop),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 20.sp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(36.dp)
                        ) {
                            SymbolIcon(
                                name = "close",
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (item.status == ProcessingStatus.ERROR) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SymbolIcon(
                                name = "error",
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = item.errorMessage ?: "Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Before / After comparison (expandable)
            AnimatedVisibility(
                visible = showCompare && item.status == ProcessingStatus.COMPLETED
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.compare_before),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(item.uri)
                                .size(Size(600, 420))
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = formatBytes(item.originalSizeBytes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.compare_after),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        AsyncImage(
                            model = item.processedUri?.let {
                                ImageRequest.Builder(context)
                                    .data(it)
                                    .size(Size(600, 420))
                                    .build()
                            },
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = formatBytes(item.processedSizeBytes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun shareImage(context: Context, item: ImageItem) {
    val uri = item.processedUri ?: item.uri
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = item.mimeType.ifEmpty { "image/*" }
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val df = DecimalFormat("#.##")
    return when {
        bytes >= 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
        else -> "${df.format(bytes / 1024.0)} KB"
    }
}
