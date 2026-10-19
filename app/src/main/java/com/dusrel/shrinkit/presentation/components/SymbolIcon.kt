package com.dusrel.shrinkit.presentation.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.dusrel.shrinkit.R

/**
 * Material Symbols Rounded icons via the bundled icon fonts
 * (res/font/material_symbols_rounded[_filled].ttf, subset to the icons used).
 *
 * Replaces the unmaintained material-icons-extended artifact (Google: "no longer
 * maintained or recommended"). Filled vs outlined follows M3: filled for the
 * active/selected destination and primary emphasis, outlined everywhere else.
 *
 * Codepoints from the official MaterialSymbolsRounded codepoints file, verified
 * present in both font subsets.
 */
private val SymbolCodepoints = mapOf(
    "add_photo_alternate" to "\uE43E",
    "arrow_back" to "\uE5C4",
    "auto_awesome" to "\uE65F",
    "bolt" to "\uEA0B",
    "camera_alt" to "\uE412",
    "check" to "\uE668",
    "check_circle" to "\uE86C",
    "chevron_right" to "\uE5CC",
    "clear_all" to "\uE0B8",
    "compare" to "\uE3B9",
    "compare_arrows" to "\uE9CF",
    "crop" to "\uE3BE",
    "close" to "\uE5CD",
    "delete" to "\uE92E",
    "download" to "\uE884",
    "error" to "\uF8B6",
    "expand_less" to "\uE5CE",
    "expand_more" to "\uE5CF",
    "history" to "\uE8B3",
    "home" to "\uE9B2",
    "info" to "\uE88E",
    "language" to "\uEA07",
    "lock" to "\uE899",
    "lock_open" to "\uE898",
    "more_vert" to "\uE5D4",
    "view_list" to "\uE8EF",
    "grid_view" to "\uE9B0",
    "notifications" to "\uE7F4",
    "rotate_right" to "\uE41A",
    "open_in_browser" to "\uE89D",
    "palette" to "\uE40A",
    "privacy_tip" to "\uF0DC",
    "photo_library" to "\uE413",
    "photo_size_select_large" to "\uE433",
    "picture_as_pdf" to "\uE415",
    "security" to "\uE32A",
    "settings" to "\uE8B8",
    "share" to "\uE80D",
    "shield" to "\uE9E0",
    "speed" to "\uE9E4",
    "star" to "\uF09A",
    "translate" to "\uE8E2",
)

private val SymbolRounded = FontFamily(Font(R.font.material_symbols_rounded))
private val SymbolRoundedFilled = FontFamily(Font(R.font.material_symbols_rounded_filled))

@Composable
fun SymbolIcon(
    name: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    filled: Boolean = false,
    fontSize: TextUnit = 24.sp
) {
    val glyph = SymbolCodepoints[name] ?: return
    BasicText(
        text = glyph,
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        },
        style = TextStyle(
            fontFamily = if (filled) SymbolRoundedFilled else SymbolRounded,
            fontSize = fontSize,
            color = tint
        )
    )
}
