package com.example.nonelauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.LocalHazeState
import com.example.nonelauncher.ui.theme.liquidGlass

/**
 * A full-bleed glass sheet. Samples the window's wallpaper through [LocalHazeState]; the radius
 * and tint stay per-sheet because a taller surface has more ground behind it to settle.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 20.dp,
    tintAlpha: Float = 0.6f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlass(
                hazeState = LocalHazeState.current,
                shape = RoundedCornerShape(cornerRadius),
                style = LiquidGlassStyle.Regular.copy(
                    blurRadius = blurRadius,
                    tint = Color(0xFF0A0A0B).copy(alpha = tintAlpha),
                ),
            ),
        content = content,
    )
}
