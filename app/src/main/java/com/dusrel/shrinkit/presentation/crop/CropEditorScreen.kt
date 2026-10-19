package com.dusrel.shrinkit.presentation.crop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.domain.model.RelativeCropRect
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer

private data class CropAspect(val labelRes: Int, val ratio: Float?)

/** Draggable corner handle for Free-mode crop window. */
@Composable
private fun CornerHandle(
    xPx: Float,
    yPx: Float,
    onDrag: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { androidx.compose.ui.unit.IntOffset(xPx.toInt() - 20, yPx.toInt() - 20) }
            .size(40.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.White, CircleShape)
        )
    }
}

/**
 * Full-screen crop editor.
 *
 * The crop window is fixed and centered; the image pans/zooms underneath it.
 * On Apply, the window is mapped back to relative bitmap coordinates and
 * stored on the [ImageItem], applied during processing.
 *
 * @param lockedAspectRatio when non-null, the aspect picker is hidden and the
 * window is locked to this ratio (passport/social wizards).
 * @param lockedAspectLabel label shown for the locked ratio (e.g. "35:45").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(
    imageId: String,
    toolViewModel: ShrinkItViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    lockedAspectRatio: Float? = null,
    lockedAspectLabel: String? = null,
    modifier: Modifier = Modifier
) {
    val item = remember(imageId) { toolViewModel.getImage(imageId) }
    if (item == null) {
        // Unknown image (e.g. deep link): leave without composing the editor.
        LaunchedEffect(Unit) { onCancel() }
        return
    }

    // 2026-verified preset set (Exa): standard ratios + free + custom.
    // ratio = null means: Free (draggable window) or Original (bitmap aspect).
    val aspects = remember(lockedAspectRatio) {
        if (lockedAspectRatio != null) {
            listOf(CropAspect(R.string.crop_ratio_locked, lockedAspectRatio))
        } else {
            listOf(
                CropAspect(R.string.crop_ratio_free, null),
                CropAspect(R.string.crop_ratio_1_1, 1f),
                CropAspect(R.string.crop_ratio_4_5, 4f / 5f),
                CropAspect(R.string.crop_ratio_3_4, 3f / 4f),
                CropAspect(R.string.crop_ratio_3_2, 3f / 2f),
                CropAspect(R.string.crop_ratio_16_9, 16f / 9f),
                CropAspect(R.string.crop_ratio_9_16, 9f / 16f),
                CropAspect(R.string.crop_ratio_2_3, 2f / 3f),
                CropAspect(R.string.crop_ratio_4_1, 4f),
                CropAspect(R.string.crop_ratio_custom, -1f),
                CropAspect(R.string.crop_ratio_original, -2f)
            )
        }
    }
    var selectedAspect by remember(aspects) { mutableStateOf(aspects[0]) }
    // Custom W:H inputs (only when Custom selected).
    var customW by remember { mutableStateOf("3") }
    var customH by remember { mutableStateOf("2") }

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
    // Resolve the effective aspect: Free = null (draggable window),
    // Custom = W/H inputs, Original = bitmap aspect.
    val customRatio = remember(customW, customH) {
        val w = customW.toFloatOrNull()
        val h = customH.toFloatOrNull()
        if (w != null && h != null && w > 0 && h > 0) w / h else null
    }
    val isFreeMode = selectedAspect.ratio == null && lockedAspectRatio == null
    val aspectRatio: Float? = when {
        lockedAspectRatio != null -> lockedAspectRatio
        selectedAspect.ratio == null -> null // Free
        selectedAspect.ratio == -1f -> customRatio // Custom
        selectedAspect.ratio == -2f -> bmpAspect // Original
        else -> selectedAspect.ratio
    }

    // Free-mode draggable window (box px, mutable). Initialized to 92% centered.
    var freeLeft by remember { mutableFloatStateOf(0f) }
    var freeTop by remember { mutableFloatStateOf(0f) }
    var freeRight by remember { mutableFloatStateOf(0f) }
    var freeBottom by remember { mutableFloatStateOf(0f) }
    var freeInit by remember { mutableStateOf(false) }

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

    // Init free window once the box is measured.
    LaunchedEffect(boxW, boxH, isFreeMode) {
        if (isFreeMode && boxW > 0 && boxH > 0 && !freeInit) {
            val w = boxW * 0.92f
            val h = boxH * 0.92f
            freeLeft = (boxW - w) / 2f
            freeTop = (boxH - h) / 2f
            freeRight = freeLeft + w
            freeBottom = freeTop + h
            freeInit = true
        }
        if (!isFreeMode) freeInit = false
    }

    // Crop window: fixed-ratio (derived, centered) or free (mutable).
    val effAspect = aspectRatio ?: 1f
    val winW: Float
    val winH: Float
    val winLeft: Float
    val winTop: Float
    if (isFreeMode && freeInit) {
        winLeft = freeLeft
        winTop = freeTop
        winW = freeRight - freeLeft
        winH = freeBottom - freeTop
    } else {
        winW = minOf(boxW * 0.92f, boxH * 0.92f * effAspect)
        winH = winW / effAspect
        winLeft = (boxW - winW) / 2f
        winTop = (boxH - winH) / 2f
    }
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

    // Free-mode: drag a window corner. Keeps the window inside the box
    // with a 48dp minimum size.
    fun dragCorner(corner: Int, dx: Float, dy: Float) {
        val minSize = 48f * 2.5f // ~48dp at ~2.5x density, px-approx
        when (corner) {
            0 -> { // top-left
                freeLeft = (freeLeft + dx).coerceIn(0f, freeRight - minSize)
                freeTop = (freeTop + dy).coerceIn(0f, freeBottom - minSize)
            }
            1 -> { // top-right
                freeRight = (freeRight + dx).coerceIn(freeLeft + minSize, boxW)
                freeTop = (freeTop + dy).coerceIn(0f, freeBottom - minSize)
            }
            2 -> { // bottom-left
                freeLeft = (freeLeft + dx).coerceIn(0f, freeRight - minSize)
                freeBottom = (freeBottom + dy).coerceIn(freeTop + minSize, boxH)
            }
            else -> { // bottom-right
                freeRight = (freeRight + dx).coerceIn(freeLeft + minSize, boxW)
                freeBottom = (freeBottom + dy).coerceIn(freeTop + minSize, boxH)
            }
        }
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
                if (lockedAspectRatio != null) {
                    // Locked ratio (passport/social wizards): no picker, just a lock chip.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SymbolIcon(
                            name = "lock",
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = lockedAspectLabel ?: stringResource(R.string.crop_ratio_locked),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
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
                    // Custom W:H inputs.
                    if (selectedAspect.ratio == -1f) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customW,
                                onValueChange = { customW = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text(stringResource(R.string.label_width)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Text(":", style = MaterialTheme.typography.titleLarge)
                            OutlinedTextField(
                                value = customH,
                                onValueChange = { customH = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text(stringResource(R.string.label_height)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
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
                // Free-mode corner handles.
                if (isFreeMode && freeInit) {
                    CornerHandle(winLeft, winTop, onDrag = { dx, dy -> dragCorner(0, dx, dy) })
                    CornerHandle(winRight, winTop, onDrag = { dx, dy -> dragCorner(1, dx, dy) })
                    CornerHandle(winLeft, winBottom, onDrag = { dx, dy -> dragCorner(2, dx, dy) })
                    CornerHandle(winRight, winBottom, onDrag = { dx, dy -> dragCorner(3, dx, dy) })
                }
            }
        }
    }
}
