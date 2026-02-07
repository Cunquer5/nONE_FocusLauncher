package com.example.nonelauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.theme.OrangePrimary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

/**
 * Enhanced glass container with improved glass morphism effects
 * Uses the Haze library for true backdrop blur effects
 */
@Composable
fun EnhancedGlassContainer(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 20.dp,
    tintAlpha: Float = 0.6f,
    backgroundColor: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius))
    ) {
        // Background layer with blur effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(
                    state = hazeState,
                    style = HazeStyle(
                        tint = Color.Black.copy(alpha = tintAlpha),
                        blurRadius = blurRadius
                    )
                )
        )

        // Content layer that stays sharp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = blurRadius
                    )
                ),
            content = content
        )
    }
}

/**
 * Glass card component with enhanced styling
 */
@Composable
fun EnhancedGlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() },
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 16.dp,
    glassAlpha: Float = 0.4f,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Background with blur effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .haze(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = blurRadius
                    )
                )
        )

        // Glass surface with border
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(blurRadius = blurRadius)
                )
                .background(Color.Black.copy(alpha = glassAlpha))
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(cornerRadius)
                ),
            content = content
        )
    }
}

/**
 * Glass dialog component for modal presentations
 */
@Composable
fun GlassDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 20.dp,
    glassAlpha: Float = 0.5f,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = HazeGlassMorphism.rememberHazeState()
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        EnhancedGlassContainer(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            hazeState = hazeState,
            cornerRadius = cornerRadius,
            blurRadius = blurRadius,
            tintAlpha = glassAlpha
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                content = content
            )
        }
    }
}

/**
 * Glass toggle switch component with glass morphism effect
 */
@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val hazeState = HazeGlassMorphism.rememberHazeState()
    
    EnhancedGlassContainer(
        modifier = modifier
            .width(64.dp)
            .height(32.dp),
        hazeState = hazeState,
        cornerRadius = 16.dp,
        tintAlpha = if (checked) 0.8f else 0.4f
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
        ) {
            // Empty box to handle clicks
        }
        
        androidx.compose.animation.AnimatedVisibility(
            visible = checked,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideIn { IntOffset(40, 0) },
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOut { IntOffset(-40, 0) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(32.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangePrimary),
                contentAlignment = Alignment.Center
            ) {
                // Inner content for checked indicator
            }
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(24.dp)
                .offset(x = (-4).dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (checked) OrangePrimary 
                    else Color.White.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner content for toggle knob
        }
    }
}