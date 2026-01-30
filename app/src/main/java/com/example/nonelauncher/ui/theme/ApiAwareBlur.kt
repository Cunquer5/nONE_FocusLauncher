package com.example.nonelauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * API-aware blur modifier with fallback for older Android versions
 */
fun Modifier.apiAwareBlur(
    radius: Dp,
    fallbackAlpha: Float = 0.8f
): Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    this.blur(radius)
} else {
    // Fallback: Increase background opacity for older devices
    this.background(Color.Black.copy(alpha = fallbackAlpha))
}

/**
 * Glass morphism modifier with API-aware blur
 */
fun Modifier.glassEffect(
    blurRadius: Dp = 15.dp,
    backgroundColor: Color = Color.Black.copy(alpha = 0.4f),
    fallbackAlpha: Float = 0.7f
): Modifier = this
    .background(backgroundColor)
    .apiAwareBlur(blurRadius, fallbackAlpha)