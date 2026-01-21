package com.example.nonelauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.AppModel

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
    isUninstallable: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Logic for size based on columns
    val iconSize = when (gridColumns) {
        4 -> 56.dp
        5 -> 48.dp
        6 -> 40.dp
        else -> 56.dp
    }
    
    val blurRadius = when (gridColumns) {
        4 -> 8.dp
        5 -> 6.dp
        6 -> 4.dp
        else -> 8.dp
    }

    val iconShape = RoundedCornerShape(22)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(iconShape)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = Color.Gray),
                onClick = { onAppClick(app) },
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
        
        val colorFilter = remember(isGrayscale) {
            if (isGrayscale) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            } else {
                null
            }
        }

        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(iconShape)
                .background(Color.Black.copy(alpha = 0.3f))
                .graphicsLayer {
                    shape = iconShape
                    clip = true
                    if (isGrayscale) {
                        alpha = 0.5f
                    }
                }
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                colorFilter = colorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isBlurred) Modifier.blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        else Modifier
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = app.label,
            fontSize = if (gridColumns > 5) 10.sp else 12.sp,
            lineHeight = if (gridColumns > 5) 12.sp else 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (isGrayscale) Color.White.copy(alpha = 0.6f) else Color.White // Force white for Modern Dark theme
        )
    }
}
