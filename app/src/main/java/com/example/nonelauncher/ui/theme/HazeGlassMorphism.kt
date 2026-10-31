package com.example.nonelauncher.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Press feedback for translucent dark glass: additive light (14% white wash + 3% scale)
 * on the tappable's own painted node. Standard ripples vanish on black glass over the
 * wallpaper. Pass the SAME interactionSource to the clickable, with indication = null.
 */
@Composable
fun Modifier.glassPress(
    interactionSource: MutableInteractionSource,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "glassPressScale")
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .background(
            if (pressed) Color.White.copy(alpha = 0.14f) else Color.Transparent,
            shape
        )
}
