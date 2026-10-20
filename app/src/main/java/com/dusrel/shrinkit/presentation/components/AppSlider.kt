package com.dusrel.shrinkit.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ShrinkIt slider — one consistent design for every screen (Minimal style,
 * M3 slider guidelines):
 * - Label row with the value ALWAYS visible (accent, semibold)
 * - 6dp rounded track: accent active, soft neutral inactive
 * - Flat thumb: surface circle, 2dp accent ring, subtle shadow;
 *   grows slightly while pressed (interaction feedback)
 * - Optional min/max hints under the track ends
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    label: String? = null,
    valueText: String? = null,
    onValueChangeFinished: (() -> Unit)? = null,
    minHint: String? = null,
    maxHint: String? = null
) {
    val accent = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val thumbSize by animateDpAsState(
        targetValue = if (pressed) 26.dp else 22.dp,
        label = "thumbSize"
    )

    Column(modifier = modifier) {
        if (label != null || valueText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (valueText != null) {
                    Text(
                        text = valueText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            interactionSource = interactionSource,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .shadow(3.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(2.dp, accent, CircleShape)
                )
            },
            track = { sliderState ->
                val range = sliderState.valueRange
                val fraction = if (range.endInclusive > range.start) {
                    ((sliderState.value - range.start) /
                        (range.endInclusive - range.start)).coerceIn(0f, 1f)
                } else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(inactive)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(accent)
                    )
                }
            }
        )
        if (minHint != null || maxHint != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = minHint ?: "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = maxHint ?: "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
