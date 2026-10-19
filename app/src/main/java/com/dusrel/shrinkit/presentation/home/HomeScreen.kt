package com.dusrel.shrinkit.presentation.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.domain.model.CompressMode
import com.dusrel.shrinkit.presentation.ShrinkItViewModel
import com.dusrel.shrinkit.presentation.theme.BrandBlue
import java.io.File

/**
 * Branded landing screen: quick actions into the Tool, plus product education.
 */
@Composable
fun HomeScreen(
    toolViewModel: ShrinkItViewModel,
    onOpenTool: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                        context.getString(R.string.error_camera_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                launch = takePictureLauncher::launch
            )
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.error_camera_permission),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Brand hero
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandBlue
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_quick_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    icon = Icons.Filled.PhotoLibrary,
                    title = stringResource(R.string.home_action_compress_title),
                    subtitle = stringResource(R.string.home_action_compress_sub),
                    onClick = {
                        toolViewModel.clearAllImages()
                        onOpenTool()
                    }
                )
                QuickActionCard(
                    icon = Icons.Filled.CameraAlt,
                    title = stringResource(R.string.home_action_camera_title),
                    subtitle = stringResource(R.string.home_action_camera_sub),
                    onClick = {
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
                                        context.getString(R.string.error_camera_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                launch = takePictureLauncher::launch
                            )
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
                QuickActionCard(
                    icon = Icons.Filled.PictureAsPdf,
                    title = stringResource(R.string.home_action_20kb_title),
                    subtitle = stringResource(R.string.home_action_20kb_sub),
                    onClick = {
                        toolViewModel.clearAllImages()
                        toolViewModel.updateCompressMode(CompressMode.TARGET_KB)
                        toolViewModel.updateTargetKb(20)
                        onOpenTool()
                    }
                )
                QuickActionCard(
                    icon = Icons.Filled.History,
                    title = stringResource(R.string.home_action_history_title),
                    subtitle = stringResource(R.string.home_action_history_sub),
                    onClick = onOpenHistory
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_how_it_works),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HowItWorksRow(
                    step = "1",
                    title = stringResource(R.string.home_step1_title),
                    body = stringResource(R.string.home_step1_body)
                )
                HowItWorksRow(
                    step = "2",
                    title = stringResource(R.string.home_step2_title),
                    body = stringResource(R.string.home_step2_body)
                )
                HowItWorksRow(
                    step = "3",
                    title = stringResource(R.string.home_step3_title),
                    body = stringResource(R.string.home_step3_body)
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_why_shrinkit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureRow(
                    Icons.Filled.Shield,
                    stringResource(R.string.home_feat_privacy_title),
                    stringResource(R.string.home_feat_privacy_body)
                )
                FeatureRow(
                    Icons.Filled.Speed,
                    stringResource(R.string.home_feat_exact_title),
                    stringResource(R.string.home_feat_exact_body)
                )
                FeatureRow(
                    Icons.Filled.Translate,
                    stringResource(R.string.home_feat_lang_title),
                    stringResource(R.string.home_feat_lang_body)
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
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

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BrandBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HowItWorksRow(step: String, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = RoundedCornerShape(50),
            color = BrandBlue,
            modifier = Modifier.size(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = step,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
