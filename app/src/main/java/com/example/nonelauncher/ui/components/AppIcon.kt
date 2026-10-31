package com.example.nonelauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.SquircleShapes
import com.example.nonelauncher.ui.theme.glassPress

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppModel,
    isGrayscale: Boolean = false,
    isBlurred: Boolean = false,
    gridColumns: Int = 4,
    onAppClick: (AppModel) -> Unit,
    onUninstall: () -> Unit = {},
    onAppInfo: () -> Unit = {},
    isUninstallable: Boolean = false,
    onTogglePinned: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current
    
    // Pre-calculate sizes outside of composition to avoid recompositions
    val iconSize = remember(gridColumns) {
        when (gridColumns) {
            4 -> 56.dp
            5 -> 48.dp
            6 -> 40.dp
            else -> 56.dp
        }
    }
    
    val blurRadius = remember(gridColumns) {
        when (gridColumns) {
            4 -> 8.dp
            5 -> 6.dp
            6 -> 4.dp
            else -> 8.dp
        }
    }

    val iconShape = remember(gridColumns) {
        when (gridColumns) {
            4 -> SquircleShapes.Icon56dp
            5 -> SquircleShapes.Icon48dp
            6 -> SquircleShapes.Icon40dp
            else -> SquircleShapes.Icon56dp
        }
    }
    
    // Pre-calculate color filter only when isGrayscale changes
    val colorFilter = remember(isGrayscale) {
        if (isGrayscale) {
            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        } else {
            null
        }
    }
    
    // Pre-calculate text properties
    val (fontSize, lineHeight) = remember(gridColumns) {
        if (gridColumns > 5) 10.sp to 12.sp else 12.sp to 14.sp
    }
    
    val textColor = remember(isGrayscale) {
        if (isGrayscale) Color.White.copy(alpha = 0.6f) else Color.White
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(iconShape)
            .combinedClickable(
                interactionSource = interactionSource,
                // Gray ripple on black glass was invisible AND occluded by the icon well;
                // feedback now rides the icon Box itself via glassPress + haptic
                indication = null,
                onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    onAppClick(app)
                },
                onLongClick = { showMenu = true }
            )
            .padding(4.dp)
    ) {
        Box {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color.Black.copy(alpha = 0.7f))
            ) {
                DropdownMenuItem(
                    text = { Text("App Info", color = Color.White) },
                    onClick = { 
                        showMenu = false
                        onAppInfo() 
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            if (app.isPinned) "Remove from Essentials" else "Add to Essentials",
                            color = Color.White
                        )
                    },
                    onClick = {
                        showMenu = false
                        onTogglePinned()
                    }
                )
                if (isUninstallable) {
                    DropdownMenuItem(
                        text = { Text("Uninstall", color = Color.White) },
                        onClick = { 
                            showMenu = false
                            onUninstall() 
                        }
                    )
                }
            }
        }

        // Static well: a grid of blurred icon wells costs a backdrop pass per icon.
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(iconShape)
                .background(Color.Black.copy(alpha = 0.3f))
                .glassPress(interactionSource, iconShape)
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                colorFilter = colorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(iconShape)
                    .then(
                        if (isBlurred) Modifier.blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        else Modifier
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(5.dp))
        
        Text(
            text = app.label,
            fontSize = fontSize,
            lineHeight = lineHeight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}