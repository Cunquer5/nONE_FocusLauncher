package com.example.nonelauncher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.HazeGlassMorphism.rememberHazeState
import dev.chrisbanes.haze.*

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 20.dp,
    tintAlpha: Float = 0.6f,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = rememberHazeState()

    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius))
    ) {
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(blurRadius = blurRadius)
                ),
            content = content
        )
    }
}