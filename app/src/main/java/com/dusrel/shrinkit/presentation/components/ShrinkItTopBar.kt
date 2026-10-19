package com.dusrel.shrinkit.presentation.components

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.presentation.util.BrowserLauncher

/**
 * Tool workspace header — compact Minimal bar (same spec as [ScreenTopBar]:
 * 10dp/14dp padding, 16sp title, page-color bg + hairline border, no logo).
 * Keeps the language selector and overflow menu from the original app.
 */
@Composable
fun ShrinkItTopBar(
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }

    // Resolve URLs in composable scope (configuration-aware); used in onClick lambdas below
    val websiteUrl = stringResource(R.string.url_website)
    val privacyUrl = stringResource(R.string.url_privacy)
    val termsUrl = stringResource(R.string.url_terms)

    ScreenTopBar(
        title = stringResource(R.string.app_name),
        onBackClick = onBackClick,
        actions = {
            // Language selector button
            Box {
                HeaderIconButton(onClick = { showLangMenu = true }) {
                    SymbolIcon(
                        name = "language",
                        contentDescription = stringResource(R.string.content_desc_language),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showLangMenu,
                    onDismissRequest = { showLangMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.lang_english)) },
                        onClick = {
                            setAppLocale(context, "en")
                            showLangMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.lang_hindi)) },
                        onClick = {
                            setAppLocale(context, "hi")
                            showLangMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.lang_assamese)) },
                        onClick = {
                            setAppLocale(context, "as")
                            showLangMenu = false
                        }
                    )
                }
            }

            // Overflow Menu
            Box {
                HeaderIconButton(onClick = { showMenu = true }) {
                    SymbolIcon(
                        name = "more_vert",
                        contentDescription = stringResource(R.string.content_desc_menu),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.link_website)) },
                        leadingIcon = {
                            SymbolIcon(
                                name = "open_in_browser",
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            BrowserLauncher.openUrl(context, websiteUrl)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.link_privacy_policy)) },
                        leadingIcon = {
                            SymbolIcon(
                                name = "privacy_tip",
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            BrowserLauncher.openUrl(context, privacyUrl)
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.link_terms)) },
                        onClick = {
                            BrowserLauncher.openUrl(context, termsUrl)
                            showMenu = false
                        }
                    )
                }
            }
        },
        modifier = modifier
    )
}

private fun setAppLocale(context: android.content.Context, languageTag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager?.applicationLocales = LocaleList.forLanguageTags(languageTag)
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}
