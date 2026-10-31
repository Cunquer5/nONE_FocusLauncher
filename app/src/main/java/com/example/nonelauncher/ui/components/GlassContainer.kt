package com.example.nonelauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.LocalHazeState
import com.example.nonelauncher.ui.theme.liquidGlass

/**
 * A glass sheet welded to the bottom edge. Samples the window's wallpaper through
 * [LocalHazeState]; the radius and tint stay per-sheet because a taller surface has more ground
 * behind it to settle.
 *
 * Square at the bottom because the sheet runs off the display there; rounded at the top, where it
 * stops short of the screen's top edge and the wallpaper frames it.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 20.dp,
    tintAlpha: Float = 0.6f,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = remember(cornerRadius) {
        RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
    }
    Box(
        modifier = modifier
            .liquidGlass(
                hazeState = LocalHazeState.current,
                shape = shape,
                style = LiquidGlassStyle.Regular.copy(
                    blurRadius = blurRadius,
                    tint = Color(0xFF0A0A0B).copy(alpha = tintAlpha),
                ),
            ),
        content = content,
    )
}
