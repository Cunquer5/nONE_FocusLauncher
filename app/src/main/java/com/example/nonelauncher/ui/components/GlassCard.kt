package com.example.nonelauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    glassAlpha: Float = 0.6f,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = remember { HazeState() }

    Box(modifier = modifier) {
        // Layer 1: Background with Haze blur
        Box(
            modifier = Modifier
                .matchParentSize()
                .haze(hazeState, backgroundColor = Color.Transparent)
        )

        // Layer 2: Glass surface
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .hazeChild(hazeState)
                .background(Color.Black.copy(alpha = glassAlpha))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(cornerRadius)
                ),
            content = content
        )
    }
}