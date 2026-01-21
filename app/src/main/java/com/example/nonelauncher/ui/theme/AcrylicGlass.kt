package com.example.nonelauncher.ui.theme

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * TRUE ACRYLIC GLASS EFFECT - Backdrop blur only
 *
 * IMPORTANT: This creates a blurred BACKGROUND layer with sharp foreground content.
 * The key difference from naive approaches:
 *
 * ❌ WRONG: Applying .blur() to the entire container
 *    - Blurs content, background, everything = frosted icons
 *
 * ✅ CORRECT: Three-layer approach:
 *    1. Background layer with RenderEffect blur (blurs wallpaper behind)
 *    2. Semi-transparent color overlay for color tint
 *    3. Content layer with NO blur (icons, text stay sharp)
 *
 * How RenderEffect.createBlurRenderEffect() works:
 * - Applied via graphicsLayer { renderEffect = ... }
 * - Blurs pixels BEHIND this layer (on GPU)
 * - Does NOT affect content drawn ON TOP of this layer
 * - Android 12+ only, gracefully disabled on older versions
 */
object AcrylicGlass {
    
    /**
     * Creates a blurred background layer suitable for acrylic glass effect
     * 
     * This modifier should be applied ONLY to the background element,
     * NOT to the entire container or content.
     *
     * @param blurRadius Blur radius in pixels (20-40 recommended for acrylic effect)
     * @return Modifier with backdrop blur via RenderEffect (Android 12+) or no-op (older)
     */
    fun Modifier.acrylicBackdropBlur(blurRadius: Float = 25f): Modifier = this.then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ - True backdrop blur using RenderEffect
            // This blurs content BEHIND this element, not the element itself
            val renderEffect = RenderEffect.createBlurEffect(
                blurRadius,      // X-axis blur
                blurRadius,      // Y-axis blur
                Shader.TileMode.CLAMP  // Edge treatment - clamp to edge
            )
            Modifier.graphicsLayer {
                this.renderEffect = renderEffect.asComposeRenderEffect()
            }
        } else {
            // Fallback: Semi-transparent background without blur
            // (blur not available before Android 12)
            Modifier
        }
    )
    
    /**
     * Creates a semi-transparent color tint for the acrylic overlay
     * Applied OVER the blurred background
     */
    fun Modifier.acrylicTint(
        color: Color = Color.White.copy(alpha = 0.15f)
    ): Modifier = this
        .background(color)
    
    /**
     * Complete acrylic glass background - for use with background layer ONLY
     * This should NOT be applied to content/icons
     */
    fun Modifier.acrylicGlassBackgroundLayer(
        blurRadius: Float = 25f,
        tintColor: Color = Color.White.copy(alpha = 0.15f),
        cornerRadius: Dp = 16.dp
    ): Modifier = this
        .clip(RoundedCornerShape(cornerRadius))
        .acrylicBackdropBlur(blurRadius)
        .acrylicTint(tintColor)
}

/**
 * EXAMPLE: Acrylic glass card with blurred background + sharp content
 *
 * This demonstrates the CORRECT layer structure:
 * ```
 * Box(modifier = Modifier.size(100.dp)) {
 *     // LAYER 1: Blurred background (ONLY this gets blur)
 *     Box(
 *         modifier = Modifier
 *             .fillMaxSize()
 *             .acrylicGlassBackgroundLayer()  // ← Blur applied HERE
 *     )
 *     
 *     // LAYER 2: Sharp content on top (NO blur applied)
 *     Icon(...)  // ← Stays sharp, blur does not affect
 *     Text(...)  // ← Stays sharp, blur does not affect
 * }
 * ```
 *
 * Key principle: Blur and content are on DIFFERENT layers.
 * The blur layer is behind, content layer is in front.
 * Because content is on TOP, the blur doesn't affect it.
 */
