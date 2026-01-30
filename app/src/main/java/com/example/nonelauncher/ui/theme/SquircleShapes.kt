package com.example.nonelauncher.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SquircleShapes {
    fun squircle(dimension: Dp): RoundedCornerShape {
        return RoundedCornerShape(dimension * 0.2237f)
    }
    
    // Pre-defined common sizes
    val Icon56dp = RoundedCornerShape(12.5.dp) // 56dp * 0.2237
    val Icon48dp = RoundedCornerShape(10.7.dp) // 48dp * 0.2237
    val Icon40dp = RoundedCornerShape(9.dp)    // 40dp * 0.2237
    val Button56dp = RoundedCornerShape(12.5.dp)
    val DockItem63dp = RoundedCornerShape(14.1.dp) // 63dp * 0.2237
}