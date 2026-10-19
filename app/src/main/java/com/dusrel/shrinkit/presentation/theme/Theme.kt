package com.dusrel.shrinkit.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Web parity: solid buttons use --t-btn-a (#016df0 light / #0071e3 dark),
 * which differs from --t-accent (#0a84ff) in dark mode. M3's primary maps
 * to --t-accent, so buttons need their own theme-aware container color.
 */
val LocalButtonContainer = compositionLocalOf { ButtonLight }

private val DarkColorScheme = darkColorScheme(
    primary = AccentDark,            // #0a84ff (web --t-accent)
    onPrimary = Color.White,
    primaryContainer = AccentSoftDark,
    onPrimaryContainer = AccentDark,
    secondary = TealDark,            // #2dd4bf (web --t-teal dark)
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF14351F),
    onSecondaryContainer = SuccessDark,
    tertiary = SuccessDark,          // #34d399 (web --t-success-text dark)
    onTertiary = Color.Black,
    background = PageDark,           // #121214 (web --t-page)
    onBackground = InkDark,          // #e5e5ea (web --t-ink)
    surface = SurfaceDark,           // #1c1c1e (web --t-surface)
    onSurface = InkDark,
    surfaceVariant = SurfaceCardDark, // #232326 (web --t-surface-2)
    onSurfaceVariant = MutedDark,    // #98989f (web --t-muted)
    outline = BorderDark,
    outlineVariant = InputBorderDark,
    error = ErrorDark,               // #ff7a7a (web --t-error dark)
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AccentLight,           // #016df0 (web --t-accent)
    onPrimary = Color.White,
    primaryContainer = AccentSoftLight,
    onPrimaryContainer = AccentLight,
    secondary = TealLight,           // #42c3ac (web --t-teal)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F4EA),
    onSecondaryContainer = SuccessLight,
    tertiary = SuccessLight,         // #0a7d4f (web --t-success-text)
    onTertiary = Color.White,
    background = PageLight,          // #ffffff (web --t-page)
    onBackground = InkLight,         // #000000 (web --t-ink)
    surface = SurfaceLight,          // #ffffff (web --t-surface)
    onSurface = InkLight,
    surfaceVariant = Surface2Light,  // #f6faff (web --t-surface-2)
    onSurfaceVariant = MutedLight,   // #667085 (web --t-muted)
    outline = BorderLight,           // #e2e8f0 (web --t-border)
    outlineVariant = InputBorderLight, // #cbd5e1 (web --t-input-border)
    error = ErrorLight,              // #d33 (web --t-error)
    onError = Color.White
)

@Composable
fun ShrinkItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false to preserve strong ShrinkIt brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            CompositionLocalProvider(
                LocalButtonContainer provides if (darkTheme) ButtonDark else ButtonLight
            ) {
                content()
            }
        }
    )
}
