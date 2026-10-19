package com.dusrel.shrinkit.presentation.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.domain.model.CompressMode
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.components.SymbolIcon
import com.dusrel.shrinkit.presentation.theme.LocalButtonContainer
import java.io.File

/**
 * Home screen — body is a same-to-same replica of the approved Minimal
 * mockup: centered hero + full-width CTA + flat quick-action rows +
 * collapsible How-it-works. Header stays logo-free per user instruction.
 */
@Composable
fun HomeScreen(
    toolViewModel: ShrinkItViewModel,
    onOpenTool: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTools: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Resolve toast messages in composable scope (configuration-aware)
    val cameraFailedMsg = stringResource(R.string.error_camera_failed)
    val cameraPermissionMsg = stringResource(R.string.error_camera_permission)

    // Camera capture: permission -> temp file -> TakePicture -> add to tool.
    // The pending URI lives in the activity-scoped ViewModel so a config
    // change while the camera app is open does not lose the capture.
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            toolViewModel.pendingCameraUri?.let { uri ->
                toolViewModel.pendingCameraUri = null
                toolViewModel.addImages(listOf(uri))
                onOpenTool()
            }
        } else {
            toolViewModel.pendingCameraUri = null
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera(
                context = context,
                onUriReady = { uri -> toolViewModel.pendingCameraUri = uri },
                onError = {
                    Toast.makeText(
                        context,
                        cameraFailedMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                },
                launch = takePictureLauncher::launch
            )
        } else {
            Toast.makeText(
                context,
                cameraPermissionMsg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val onCameraClick = {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera(
                context = context,
                onUriReady = { uri -> toolViewModel.pendingCameraUri = uri },
                onError = {
                    Toast.makeText(
                        context,
                        cameraFailedMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                },
                launch = takePictureLauncher::launch
            )
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var howOpen by remember { mutableStateOf(true) }
    val howChevronRotation by animateFloatAsState(
        targetValue = if (howOpen) 90f else 0f,
        label = "howChevron"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(title = stringResource(R.string.app_name))
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            // ---- Hero (mockup: padding 30px 22px 0, centered) ----
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, top = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.home_hero_title),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp,
                        lineHeight = 31.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.home_hero_subtitle),
                        fontSize = 13.5.sp,
                        lineHeight = 21.6.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            toolViewModel.clearAllImages()
                            onOpenTool()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalButtonContainer.current,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.home_cta_select_photos),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.home_cta_caption),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ---- Quick-action rows (mockup: padding 0 22px, margin-top 30px) ----
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 0.dp)
                        .padding(top = 30.dp)
                ) {
                    HomeRow(
                        iconName = "camera_alt",
                        title = stringResource(R.string.home_row_camera_title),
                        subtitle = stringResource(R.string.home_row_camera_sub),
                        onClick = onCameraClick
                    )
                    Spacer(Modifier.height(14.dp))
                    HomeRow(
                        iconName = "picture_as_pdf",
                        title = stringResource(R.string.home_row_exam_title),
                        subtitle = stringResource(R.string.home_row_exam_sub),
                        badge = stringResource(R.string.home_row_exam_badge),
                        onClick = {
                            toolViewModel.clearAllImages()
                            toolViewModel.updateCompressMode(CompressMode.TARGET_KB)
                            toolViewModel.updateTargetKb(20)
                            onOpenTool()
                        }
                    )
                    Spacer(Modifier.height(14.dp))
                    HomeRow(
                        iconName = "history",
                        title = stringResource(R.string.home_row_history_title),
                        subtitle = stringResource(R.string.home_action_history_sub),
                        onClick = onOpenHistory
                    )
                    Spacer(Modifier.height(14.dp))
                    HomeRow(
                        iconName = "auto_awesome",
                        title = stringResource(R.string.home_row_tools_title),
                        subtitle = stringResource(R.string.home_row_tools_sub),
                        onClick = onOpenTools
                    )
                }
            }

            // ---- How it works (collapsible, mockup: padding 0 22px, margin-top 26px) ----
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp)
                        .padding(top = 26.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { howOpen = !howOpen }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_how_it_works),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        SymbolIcon(
                            name = "chevron_right",
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(howChevronRotation)
                        )
                    }
                    if (howOpen) {
                        Text(
                            text = stringResource(R.string.home_how_step1) + "\n" +
                                stringResource(R.string.home_how_step2) + "\n" +
                                stringResource(R.string.home_how_step3),
                            fontSize = 13.sp,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

/**
 * Flat quick-action row — mockup `.rowbtn`: 42dp accent-soft tile,
 * 14.5sp/600 title (+ optional badge), 12.5sp muted subtitle, 16dp chevron.
 */
@Composable
private fun HomeRow(
    iconName: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            SymbolIcon(
                name = iconName,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (badge != null) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SymbolIcon(
            name = "chevron_right",
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun launchCamera(
    context: Context,
    onUriReady: (Uri) -> Unit,
    onError: () -> Unit,
    launch: (Uri) -> Unit
) {
    try {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        onUriReady(uri)
        launch(uri)
    } catch (_: Exception) {
        onError()
    }
}
