package com.dusrel.shrinkit.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// ShrinkIt Web Design Tokens — EXACT parity with shrinkit.dusrel.com
// (src/styles/global.css). :root = Light, [data-theme="dark"] = Dark 2 Graphite.
// Do NOT approximate: every hex below matches the web token exactly.
// ============================================================================

// Brand (shared)
val BrandBlue = Color(0xFF007BFF)
val BrandBlueDark = Color(0xFF0257BF)

// ---- Dark 2 Graphite (exact web tokens) ----
val PageDark = Color(0xFF121214)          // --t-page
val SurfaceDark = Color(0xFF1C1C1E)       // --t-surface
val SurfaceCardDark = Color(0xFF232326)   // --t-surface-2
val InkDark = Color(0xFFE5E5EA)           // --t-ink
val Ink2Dark = Color(0xFFD1D1D6)          // --t-ink-2
val MutedDark = Color(0xFF98989F)         // --t-muted
val BorderDark = Color(0xFF2A2A2E)        // rgba(255,255,255,0.10) on #121214
val BorderSoftDark = Color(0xFF232326)    // rgba(255,255,255,0.07) on #121214
val InputBgDark = Color(0xFF2C2C2E)        // --t-input-bg
val InputBorderDark = Color(0xFF3A3A3E)    // rgba(255,255,255,0.16) on #121214
val AccentDark = Color(0xFF0A84FF)        // --t-accent
val ButtonDark = Color(0xFF0071E3)        // --t-btn-a/b (solid)
val AccentSoftDark = Color(0x290A84FF)    // rgba(10,132,255,0.16)
val DropzoneDark = Color(0xFF1A1A1C)      // --t-dropzone-bg
val ThumbBgDark = Color(0xFF1A1A1C)       // --t-thumb-bg
val DashDark = Color(0xFF0A84FF)          // --t-dash (dark)
val SuccessBgDark = Color(0xFF14351F)     // --t-success-bg (dark)
val ErrorDark = Color(0xFFFF7A7A)         // --t-error
val SuccessDark = Color(0xFF34D399)       // --t-success-text
val SuccessBorderDark = Color(0xFF34D399) // --t-success-border
val WarningDark = Color(0xFFE6B84D)       // --t-warning-text
val TealDark = Color(0xFF2DD4BF)          // --t-teal (dark)

// ---- Light (exact web tokens) ----
val PageLight = Color(0xFFFFFFFF)         // --t-page
val BandLight = Color(0xFFFAFCFF)         // --t-band
val SurfaceLight = Color(0xFFFFFFFF)      // --t-surface
val Surface2Light = Color(0xFFF6FAFF)     // --t-surface-2
val InkLight = Color(0xFF000000)          // --t-ink
val Ink2Light = Color(0xFF1D2939)         // --t-ink-2
val MutedLight = Color(0xFF667085)        // --t-muted
val BorderLight = Color(0xFFE2E8F0)       // --t-border
val BorderSoftLight = Color(0xFFF1F5F9)  // --t-border-soft
val InputBorderLight = Color(0xFFCBD5E1)  // --t-input-border
val AccentLight = Color(0xFF016DF0)       // --t-accent
val Accent2Light = Color(0xFF1A7AF7)      // --t-accent-2
val ButtonLight = Color(0xFF016DF0)       // --t-btn-a (solid)
val AccentSoftLight = Color(0x1A016DF0)   // rgba(1,109,240,0.10)
val DropzoneLight = Color(0xFFFAFCFF)     // --t-dropzone-bg
val ThumbBgLight = Color(0xFFFAFCFF)      // --t-thumb-bg
val DashLight = Color(0xFF52A0FF)        // --t-dash
val SuccessBgLight = Color(0xFFE6F4EA)   // --t-success-bg
val ErrorLight = Color(0xFFDD3333)        // --t-error (#d33)
val SuccessLight = Color(0xFF0A7D4F)      // --t-success-text
val SuccessBorderLight = Color(0xFF0A7D4F)
val WarningLight = Color(0xFFB7791F)      // --t-warning-text
val TealLight = Color(0xFF42C3AC)         // --t-teal
