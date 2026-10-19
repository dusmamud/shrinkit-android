package com.dusrel.shrinkit.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.components.ScreenTopBar
import com.dusrel.shrinkit.presentation.util.BrowserLauncher

/** Dedicated About page: logo, version, privacy / terms / website rows. */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0)?.versionName
        }.getOrNull() ?: "1.0.0"
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.settings_about),
                onBackClick = onBack
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.align(Alignment.Start),
                horizontalAlignment = Alignment.Start
            ) {
                SettingsRow(
                    iconName = "shield",
                    title = stringResource(R.string.about_privacy),
                    subtitle = null,
                    onClick = {
                        BrowserLauncher.openUrl(
                            context,
                            context.getString(R.string.url_privacy)
                        )
                    }
                )
                SettingsRow(
                    iconName = "info",
                    title = stringResource(R.string.link_terms),
                    subtitle = null,
                    onClick = {
                        BrowserLauncher.openUrl(
                            context,
                            context.getString(R.string.url_terms)
                        )
                    }
                )
                SettingsRow(
                    iconName = "language",
                    title = stringResource(R.string.link_website),
                    subtitle = "shrinkit.dusrel.com",
                    onClick = {
                        BrowserLauncher.openUrl(
                            context,
                            context.getString(R.string.url_website)
                        )
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.about_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
