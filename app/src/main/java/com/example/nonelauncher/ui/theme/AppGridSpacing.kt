package com.example.nonelauncher.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppGridSpacing {
    fun getSpacingForColumns(columns: Int): Pair<Dp, Dp> {
        val iconSize = when (columns) {
            4 -> 56.dp
            5 -> 48.dp
            6 -> 40.dp
            else -> 56.dp
        }
        
        val columnSpacing = iconSize * 0.33f
        val rowSpacing = iconSize * 0.45f
        
        return Pair(columnSpacing, rowSpacing)
    }
}