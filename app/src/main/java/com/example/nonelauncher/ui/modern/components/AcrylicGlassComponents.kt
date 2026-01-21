package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.ui.theme.AcrylicGlass.acrylicGlassBackgroundLayer
import com.example.nonelauncher.ui.theme.OrangePrimary

/**
 * CORRECT ACRYLIC GLASS IMPLEMENTATION
 *
 * This composable demonstrates the proper layer structure for acrylic glass:
 * 1. Background layer (Box) with RenderEffect blur - blurs wallpaper behind
 * 2. Content layer (Icon + Text) with NO blur - stays sharp
 *
 * Why this works:
 * - RenderEffect.createBlurRenderEffect() blurs pixels BEHIND the layer
 * - Content drawn ON TOP of the layer is not affected by the blur
 * - Result: Blurred background visible through semi-transparent tint, sharp content on top
 */
@Composable
fun AcrylicGlassCard(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    blurRadius: Float = 25f,
    tintColor: Color = Color.White.copy(alpha = 0.15f),
    cornerRadius: Dp = 16.dp,
    icon: ImageVector = Icons.Default.Apps,
    label: String = "App",
    onIconClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        // LAYER 1: Blurred background
        // This Box sits BEHIND the content and gets the blur effect applied
        // The blur affects the wallpaper/content BEHIND this layer, not the layer itself
        Box(
            modifier = Modifier
                .fillMaxSize()
                .acrylicGlassBackgroundLayer(
                    blurRadius = blurRadius,
                    tintColor = tintColor,
                    cornerRadius = 0.dp  // No corner radius here, outer Box handles it
                )
        )
        
        // LAYER 2: Sharp content on top
        // This layer sits ON TOP of the blurred background
        // The blur does not affect this layer or its children
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon stays sharp - not affected by blur
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Text stays sharp - not affected by blur
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

/**
 * ACRYLIC GLASS DOCK ITEM
 *
 * Example of applying acrylic glass to dock/navbar buttons
 * Same principle: blur background layer, sharp content layer
 */
@Composable
fun AcrylicGlassDockItem(
    modifier: Modifier = Modifier,
    label: String = "Item",
    icon: @Composable () -> Unit = { }
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Blurred background layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .acrylicGlassBackgroundLayer(
                    blurRadius = 15f,
                    tintColor = Color.Black.copy(alpha = 0.25f),
                    cornerRadius = 0.dp
                )
        )
        
        // Sharp content layer
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * ACRYLIC GLASS SEARCH BAR
 *
 * Example showing acrylic glass applied to input field background
 */
@Composable
fun AcrylicGlassSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Blurred background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .acrylicGlassBackgroundLayer(
                    blurRadius = 20f,
                    tintColor = Color.Black.copy(alpha = 0.2f),
                    cornerRadius = 0.dp
                )
        )
        
        // Sharp content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}
