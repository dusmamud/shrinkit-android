package com.dusrel.shrinkit.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object BrowserLauncher {

    /**
     * Launches external web links in the user's default browser using AndroidX Custom Tabs,
     * with graceful fallback to standard ACTION_VIEW Intent.
     */
    fun openUrl(context: Context, url: String) {
        val uri = Uri.parse(url)
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(false)
                .build()
            customTabsIntent.launchUrl(context, uri)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
                // Ignore if no web browser is installed on device
            }
        }
    }
}
