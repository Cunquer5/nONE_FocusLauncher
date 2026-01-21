package com.example.nonelauncher.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.os.Build
import android.graphics.Shader
import android.graphics.RenderEffect

/**
 * DEPRECATED: Use AcrylicGlass.kt instead for true acrylic glass effects
 *
 * This module provides glass morphism modifiers with semi-transparent backgrounds.
 * Note: These do NOT include backdrop blur. For true frosted glass with blurred
 * backgrounds, use AcrylicGlass.kt which implements proper RenderEffect-based blur.
 */
object GlassMorphism {
    
    /**
     * Apply black glass effect to a Composable (semi-transparent, no blur)
     * Creates a semi-transparent dark background with frosted glass appearance
     *
     * @param opacity The opacity of the glass effect (0f to 1f). Default: GLASS_OPACITY_MEDIUM
     * @param backgroundColor The background color of the glass. Default: BlackGlass (0xFF1E1E1E)
     * @param cornerRadius The corner radius for rounded glass effect. Default: 32.dp
     */
    fun Modifier.blackGlassBackground(
        opacity: Float = GLASS_OPACITY_MEDIUM,
        backgroundColor: Color = BlackGlass,
        cornerRadius: Dp = 32.dp
    ): Modifier = this
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor.copy(alpha = opacity))
    
    /**
     * Apply black glass effect to dock/navbar elements
     * Higher opacity for better contrast in bottom navbar
     */
    fun Modifier.dockGlassBackground(
        cornerRadius: Dp = 40.dp
    ): Modifier = this
        .clip(RoundedCornerShape(cornerRadius))
        .background(Black.copy(alpha = 0.6f))
    
    /**
     * Apply black glass effect to card/sheet elements
     * Medium opacity for balanced transparency
     */
    fun Modifier.cardGlassBackground(
        cornerRadius: Dp = 32.dp
    ): Modifier = this
        .clip(RoundedCornerShape(cornerRadius))
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.graphicsLayer {
                    renderEffect = RenderEffect
                        .createBlurEffect(
                            25f,
                            25f,
                            Shader.TileMode.CLAMP
                        )
                        .asComposeRenderEffect()
                }
            } else {
                Modifier // No blur on older versions
            }
        )
        .background(BlackGlass.copy(alpha = GLASS_OPACITY_MEDIUM))
    
    /**
     * Apply black glass effect to input fields and interactive elements
     * Lower opacity for subtle effect on interactive surfaces
     */
    fun Modifier.inputGlassBackground(
        cornerRadius: Dp = 16.dp
    ): Modifier = this
        .clip(RoundedCornerShape(cornerRadius))
        .background(DarkerBrown.copy(alpha = GLASS_OPACITY_MEDIUM))
}
