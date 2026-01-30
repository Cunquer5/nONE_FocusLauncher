package com.example.nonelauncher.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

/**
 * Modern glass morphism using Chris Banes Haze library v0.5.0+
 */
object HazeGlassMorphism {
    
    // Glass colors
    val GlassBlack = Color(0xFF0F0F0F).copy(alpha = 0.42f)
    val GlassBorder = Color(0x33FFFFFF)
    
    enum class BlurIntensity {
        SUBTLE,    // Minimal blur - for dock
        MEDIUM,    // Standard blur - for settings
        STRONG     // Heavy blur - for app drawer
    }
    
    /**
     * Remember a HazeState for composables that need glass effects
     */
    @Composable
    fun rememberHazeState() = remember { HazeState() }
    
    /**
     * Apply haze effect to background (for use with Box containers)
     */
    fun Modifier.hazeBackground(
        hazeState: HazeState,
        backgroundColor: Color = Color.Transparent
    ): Modifier = this
        .fillMaxSize()
        .haze(
            state = hazeState,
            backgroundColor = backgroundColor
        )
    
    /**
     * Glass background layer - FOR BLUR ONLY
     */
    fun Modifier.hazeBackgroundLayer(
        hazeState: HazeState,
        backgroundColor: Color = Color.Transparent
    ): Modifier = this
        .fillMaxSize()
        .haze(
            state = hazeState,
            backgroundColor = backgroundColor
        )
    
    /**
     * Glass surface layer - SHARP CONTENT WITH TINT
     */
    fun Modifier.glassSurfaceLayer(
        hazeState: HazeState,
        color: Color = GlassBlack,
        cornerRadius: Dp = 16.dp,
        alpha: Float = 0.5f,
        intensity: BlurIntensity = BlurIntensity.MEDIUM
    ): Modifier = this
        .fillMaxSize()
        .clip(RoundedCornerShape(cornerRadius))
        .hazeChild(
            state = hazeState,
            blurRadius = when (intensity) {
                BlurIntensity.SUBTLE -> 8.dp
                BlurIntensity.MEDIUM -> 16.dp
                BlurIntensity.STRONG -> 24.dp
            }
        )
        .background(color.copy(alpha = alpha))
        .border(
            width = 1.dp,
            color = GlassBorder,
            shape = RoundedCornerShape(cornerRadius)
        )
    
    /**
     * Legacy glass background method
     */
    fun Modifier.glassBackground(
        hazeState: HazeState,
        intensity: BlurIntensity = BlurIntensity.MEDIUM,
        color: Color = GlassBlack,
        cornerRadius: Dp = 16.dp,
        border: Boolean = true
    ): Modifier = this
        .clip(RoundedCornerShape(cornerRadius))
        .hazeChild(
            state = hazeState,
            blurRadius = when (intensity) {
                BlurIntensity.SUBTLE -> 8.dp
                BlurIntensity.MEDIUM -> 16.dp
                BlurIntensity.STRONG -> 24.dp
            }
        )
        .background(color)
        .then(
            if (border) {
                Modifier.border(
                    width = 1.dp,
                    color = GlassBorder,
                    shape = RoundedCornerShape(cornerRadius)
                )
            } else {
                Modifier
            }
        )
}