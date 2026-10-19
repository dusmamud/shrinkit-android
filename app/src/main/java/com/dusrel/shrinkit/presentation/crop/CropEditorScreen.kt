package com.dusrel.shrinkit.presentation.crop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.domain.model.RelativeCropRect
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.theme.BrandBlue
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer

private data class CropAspect(val labelRes: Int, val ratio: Float?)

/**
 * Full-screen crop editor.
 *
 * The crop window is fixed and centered; the image pans/zooms underneath it.
 * On Apply, the window is mapped back to relative bitmap coordinates and
 * stored on the [ImageItem], applied during processing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(
    imageId: String,
    toolViewModel: ShrinkItViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = remember(imageId) { toolViewModel.getImage(imageId) }
    if (item == null) {
        // Unknown image (e.g. deep link): leave without composing the editor.
        LaunchedEffect(Unit) { onCancel() }
        return
    }

    val aspects = listOf(
        CropAspect(R.string.crop_ratio_1_1, 1f),
        CropAspect(R.string.crop_ratio_4_5, 4f / 5f),
        CropAspect(R.string.crop_ratio_3_4, 3f / 4f),
        CropAspect(R.string.crop_ratio_16_9, 16f / 9f),
        CropAspect(R.string.crop_ratio_original, null)
    )
    var selectedAspect by remember { mutableStateOf(aspects[0]) }

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Bitmap aspect (fallback to square if unknown).
    val bmpAspect = remember(item) {
        if (item.originalWidth > 0 && item.originalHeight > 0) {
            item.originalWidth.toFloat() / item.originalHeight.toFloat()
        } else 1f
    }
    val aspectRatio = selectedAspect.ratio ?: bmpAspect

    // Geometry, all in px. Computed from box + aspect only (stable during gestures).
    val boxW = boxSize.width.toFloat()
    val boxH = boxSize.height.toFloat()
    // Displayed (ContentScale.Fit) image rect inside the box, untransformed.
    val dispW: Float
    val dispH: Float
    if (boxW > 0 && boxH > 0 && bmpAspect > 0) {
        dispH = minOf(boxH, boxW / bmpAspect)
        dispW = dispH * bmpAspect
    } else {
        dispW = 0f
        dispH = 0f
    }
    val dispLeft = (boxW - dispW) / 2f
    val dispTop = (boxH - dispH) / 2f
    // Crop window: centered, 92% of the constraining box dimension.
    val winW = minOf(boxW * 0.92f, boxH * 0.92f * aspectRatio)
    val winH = winW / aspectRatio
    val winLeft = (boxW - winW) / 2f
    val winTop = (boxH - winH) / 2f
    val winRight = winLeft + winW
    val winBottom = winTop + winH
    // Minimum scale so the image always covers the window.
    val minScale = if (dispW > 0 && dispH > 0) {
        maxOf(winW / dispW, winH / dispH, 1f)
    } else 1f
    val latestMinScale by rememberUpdatedState(minScale)

    // Reset pan/zoom whenever the canvas or aspect ratio changes.
    LaunchedEffect(boxSize, aspectRatio) {
        scale = minScale
        offsetX = 0f
        offsetY = 0f
    }

    fun clampOffsets() {
        if (dispW <= 0 || dispH <= 0) return
        val cx = boxW / 2f
        // offsetX in [winRight - cx - dispW*scale/2, winLeft - cx + dispW*scale/2]
        val minOx = winRight - cx - dispW * scale / 2f
        val maxOx = winLeft - cx + dispW * scale / 2f
        val minOy = winBottom - boxH / 2f - dispH * scale / 2f
        val maxOy = winTop - boxH / 2f + dispH * scale / 2f
        offsetX = if (minOx <= maxOx) offsetX.coerceIn(minOx, maxOx) else (minOx + maxOx) / 2f
        offsetY = if (minOy <= maxOy) offsetY.coerceIn(minOy, maxOy) else (minOy + maxOy) / 2f
    }

    fun computeCropRect(): RelativeCropRect {
        val cx = boxW / 2f
        val cy = boxH / 2f
        fun fracX(px: Float): Float {
            val qx = cx + (px - cx - offsetX) / scale
            return ((qx - dispLeft) / dispW).coerceIn(0f, 1f)
        }
        fun fracY(py: Float): Float {
            val qy = cy + (py - cy - offsetY) / scale
            return ((qy - dispTop) / dispH).coerceIn(0f, 1f)
        }
        return RelativeCropRect(
            left = fracX(winLeft),
            top = fracY(winTop),
            right = fracX(winRight),
            bottom = fracY(winBottom)
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crop_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        SymbolIcon(name = "close", contentDescription = stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            toolViewModel.setCropRect(imageId, computeCropRect())
                            onDone()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LocalButtonContainer.current),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        SymbolIcon(name = "check", contentDescription = null, filled = true)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.crop_apply))
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.crop_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aspects.forEach { aspect ->
                        FilterChip(
                            selected = selectedAspect == aspect,
                            onClick = { selectedAspect = aspect },
                            label = { Text(stringResource(aspect.labelRes)) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            toolViewModel.setCropRect(imageId, null)
                            onDone()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.crop_reset))
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .onSizeChanged { newSize ->
                    if (newSize != boxSize) {
                        boxSize = newSize
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(latestMinScale, 6f)
                        offsetX += pan.x
                        offsetY += pan.y
                        clampOffsets()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (boxW > 0 && boxH > 0) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.filename,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dim = Color.Black.copy(alpha = 0.55f)
                    // Dim everything outside the window (4 rects).
                    drawRect(dim, Offset.Zero, Size(winLeft, boxH))
                    drawRect(dim, Offset(winRight, 0f), Size(boxW - winRight, boxH))
                    drawRect(dim, Offset(winLeft, 0f), Size(winW, winTop))
                    drawRect(
                        dim,
                        Offset(winLeft, winBottom),
                        Size(winW, boxH - winBottom)
                    )
                    // Window border + thirds grid.
                    drawRect(
                        Color.White,
                        Offset(winLeft, winTop),
                        Size(winW, winH),
                        style = Stroke(width = 3f)
                    )
                    val thirdW = winW / 3f
                    val thirdH = winH / 3f
                    for (i in 1..2) {
                        drawLine(
                            Color.White.copy(alpha = 0.5f),
                            Offset(winLeft + thirdW * i, winTop),
                            Offset(winLeft + thirdW * i, winBottom),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            Color.White.copy(alpha = 0.5f),
                            Offset(winLeft, winTop + thirdH * i),
                            Offset(winRight, winTop + thirdH * i),
                            strokeWidth = 1.5f
                        )
                    }
                }
            }
        }
    }
}
