package com.example.nonelauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.theme.OrangePrimary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

/**
 * Enhanced glass morphism button component for the nONE Launcher app
 * Uses the Haze library for true glass effects with backdrop blur
 */
@Composable
fun GlassMorphismButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    hazeState: HazeState,
    enabled: Boolean = true,
    color: Color = OrangePrimary,
    textColor: Color = Color.White,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    height: Dp = 56.dp,
    glassAlpha: Float = 0.4f,
    blurIntensity: HazeGlassMorphism.BlurIntensity = HazeGlassMorphism.BlurIntensity.MEDIUM,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = rememberRipple(bounded = true)
    
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple,
                    onClick = onClick
                ) else Modifier
            )
            .hazeChild(
                state = hazeState,
                style = HazeStyle(
                    blurRadius = when (blurIntensity) {
                        HazeGlassMorphism.BlurIntensity.SUBTLE -> 8.dp
                        HazeGlassMorphism.BlurIntensity.MEDIUM -> 16.dp
                        HazeGlassMorphism.BlurIntensity.STRONG -> 24.dp
                    }
                )
            )
            .background(Color.Black.copy(alpha = glassAlpha))
            .border(
                width = borderWidth,
                color = if (enabled) borderColor else borderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Gradient glass morphism button with stronger visual emphasis
 */
@Composable
fun GradientGlassMorphismButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    hazeState: HazeState,
    enabled: Boolean = true,
    color: Color = OrangePrimary,
    textColor: Color = Color.White,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    height: Dp = 56.dp,
    glassAlpha: Float = 0.6f,
    blurIntensity: HazeGlassMorphism.BlurIntensity = HazeGlassMorphism.BlurIntensity.MEDIUM,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = rememberRipple(bounded = true)
    
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple,
                    onClick = onClick
                ) else Modifier
            )
            .hazeChild(
                state = hazeState,
                style = HazeStyle(
                    blurRadius = when (blurIntensity) {
                        HazeGlassMorphism.BlurIntensity.SUBTLE -> 8.dp
                        HazeGlassMorphism.BlurIntensity.MEDIUM -> 16.dp
                        HazeGlassMorphism.BlurIntensity.STRONG -> 24.dp
                    }
                )
            )
            .background(color.copy(alpha = glassAlpha))
            .border(
                width = borderWidth,
                color = if (enabled) borderColor else borderColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Outline glass morphism button - transparent with border
 */
@Composable
fun OutlineGlassMorphismButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    hazeState: HazeState,
    enabled: Boolean = true,
    textColor: Color = Color.White,
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    height: Dp = 56.dp,
    glassAlpha: Float = 0.2f,
    blurIntensity: HazeGlassMorphism.BlurIntensity = HazeGlassMorphism.BlurIntensity.MEDIUM,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = rememberRipple(bounded = true)
    
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple,
                    onClick = onClick
                ) else Modifier
            )
            .hazeChild(
                state = hazeState,
                style = HazeStyle(
                    blurRadius = when (blurIntensity) {
                        HazeGlassMorphism.BlurIntensity.SUBTLE -> 8.dp
                        HazeGlassMorphism.BlurIntensity.MEDIUM -> 16.dp
                        HazeGlassMorphism.BlurIntensity.STRONG -> 24.dp
                    }
                )
            )
            .background(Color.Transparent)
            .border(
                width = borderWidth,
                color = if (enabled) borderColor else borderColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}