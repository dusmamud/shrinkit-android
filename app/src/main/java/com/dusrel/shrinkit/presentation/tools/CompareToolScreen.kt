package com.dusrel.shrinkit.presentation.tools

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.domain.engine.BinarySearchCompressor
import com.dusrel.shrinkit.domain.engine.SafeBitmapDecoder
import com.dusrel.shrinkit.domain.model.TargetFormat
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Before / After Compare (web parity: /tools/compare-images).
 * Select → immediately shows the comparison slider.
 * Mobile UX rule: only the divider HANDLE is draggable (scroll always wins).
 */
@Composable
fun CompareToolScreen(
    onBack: () -> Unit,
    onSaveBytes: (bytes: ByteArray, filename: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var originalBytes by remember { mutableStateOf(0L) }
    var quality by remember { mutableFloatStateOf(65f) }
    var dividerPos by remember { mutableFloatStateOf(0.5f) } // 0..1
    var working by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> imageUri = uri }

    // Decode original when picked.
    LaunchedEffect(imageUri) {
        val uri = imageUri ?: return@LaunchedEffect
        working = true
        withContext(Dispatchers.IO) {
            try {
                val decoded = SafeBitmapDecoder.decode(context, uri)
                originalBitmap = decoded.bitmap
                originalBytes = context.contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
            } catch (_: Exception) {
                originalBitmap = null
            }
        }
        working = false
    }

    // Re-compress on quality change (debounced by LaunchedEffect key).
    LaunchedEffect(originalBitmap, quality) {
        val bmp = originalBitmap ?: return@LaunchedEffect
        working = true
        withContext(Dispatchers.IO) {
            try {
                val q = quality.toInt().coerceIn(10, 95)
                compressedBytes = BinarySearchCompressor.compressWithQuality(
                    bmp, TargetFormat.JPEG, q
                ).data
            } catch (_: Exception) {
            }
        }
        working = false
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.tool_compare_title),
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
                    text = stringResource(R.string.tool_compare_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (imageUri == null) {
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
                                name = "compare",
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
                // Quality slider (web parity: 10–95, default 65, live re-compress).
                item {
                    ToolSection(title = stringResource(R.string.tool_compare_quality, quality.toInt())) {
                        Slider(
                            value = quality,
                            onValueChange = { quality = it },
                            valueRange = 10f..95f
                        )
                    }
                }

                // Comparison canvas.
                item {
                    val bmp = originalBitmap
                    val cmp = compressedBytes
                    if (bmp != null && cmp != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                val fullW = maxWidth
                                val fullH = maxHeight
                                // Compressed = background (right side).
                                AsyncImage(
                                    model = cmp,
                                    contentDescription = stringResource(R.string.tool_compare_after),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Original = clipped to left of divider.
                                Box(
                                    modifier = Modifier
                                        .width(fullW * dividerPos)
                                        .fillMaxHeight()
                                        .clipToBounds()
                                ) {
                                    AsyncImage(
                                        model = bmp,
                                        contentDescription = stringResource(R.string.tool_compare_before),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(fullW, fullH)
                                    )
                                }
                                // Divider line.
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                (fullW * dividerPos).toPx().toInt() - 1,
                                                0
                                            )
                                        }
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(Color.White)
                                )
                                // Draggable handle (ONLY the handle drags).
                                val density = LocalDensity.current
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                (fullW * dividerPos).toPx().toInt() - with(density) { 24.dp.toPx().toInt() },
                                                0
                                            )
                                        }
                                        .fillMaxHeight()
                                        .width(48.dp)
                                        .pointerInput(Unit) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val dxFrac = with(density) {
                                                    dragAmount.x.toDp() / fullW.toDp()
                                                }
                                                dividerPos = (dividerPos + dxFrac).coerceIn(0.02f, 0.98f)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        SymbolIcon(
                                            name = "compare_arrows",
                                            contentDescription = null,
                                            tint = Color.White,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                                // Corner labels.
                                Text(
                                    text = stringResource(R.string.tool_compare_before),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.55f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                Text(
                                    text = stringResource(R.string.tool_compare_after),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.55f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                if (working) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Size badges + save.
                item {
                    val cmp = compressedBytes
                    if (cmp != null) {
                        val saved = if (originalBytes > 0) {
                            ((1 - cmp.size.toFloat() / originalBytes) * 100).toInt().coerceAtLeast(0)
                        } else 0
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${stringResource(R.string.tool_compare_before)}: ${formatKb(originalBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${stringResource(R.string.tool_compare_after)}: ${formatKb(cmp.size.toLong())} (−$saved%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = {
                                    onSaveBytes(cmp, "shrinkit-compare-q${quality.toInt()}.jpg")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LocalButtonContainer.current
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.tool_compare_save),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Button(
                                onClick = {
                                    imageUri = null
                                    originalBitmap = null
                                    compressedBytes = null
                                    dividerPos = 0.5f
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(stringResource(R.string.tool_compare_change))
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
