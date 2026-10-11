package com.example.nonelauncher.ui.modern.components

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.components.GlassMorphismButton
import androidx.core.graphics.drawable.toBitmap
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.foundation.border

/**
 * Wallpaper Selection Dialog for Modern UI
 * Provides options for:
 * 1. Default saturated orange color
 * 2. System wallpaper picker for custom selection
 */
@Composable
fun WallpaperSelectionDialog(
    onDismiss: () -> Unit,
    onOrangeColorSelected: () -> Unit,
    onCustomWallpaperClick: () -> Unit
) {
    val context = LocalContext.current
    val wallpaperManager = WallpaperManager.getInstance(context)
    val hazeState = HazeGlassMorphism.rememberHazeState()
    
    // Get current wallpaper drawable
    val currentWallpaper = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wallpaperManager.drawable
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .hazeChild(state = hazeState, blurRadius = 16.dp)
                .background(Color.Black.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Text(
                    text = "Select Wallpaper",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Option 1: Default Orange Color
                WallpaperOption(
                    title = "Default Saturated Orange",
                    description = "Use the launcher's signature orange color",
                    backgroundColor = OrangePrimary,
                    onClick = {
                        onOrangeColorSelected()
                        onDismiss()
                    },
                    hazeState = hazeState
                )

                // Divider
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                // Option 2: Custom Wallpaper
                WallpaperOption(
                    title = "Choose Custom Wallpaper",
                    description = "Select from your device storage",
                    backgroundColor = Color(0xFF2A2A2A),
                    preview = currentWallpaper,
                    onClick = {
                        onCustomWallpaperClick()
                        onDismiss()
                    },
                    hazeState = hazeState
                )

                // Close Button
                GlassMorphismButton(
                    onClick = onDismiss,
                    text = "Cancel",
                    hazeState = hazeState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }
        }
    }
}

/**
 * Individual wallpaper option card
 */
@Composable
private fun WallpaperOption(
    title: String,
    description: String,
    backgroundColor: Color,
    preview: android.graphics.drawable.Drawable? = null,
    onClick: () -> Unit,
    hazeState: HazeState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .hazeChild(state = hazeState, blurRadius = 12.dp)
            .background(backgroundColor.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Preview swatch: solid fill for the orange option, the live wallpaper for custom
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
            ) {
                if (preview != null) {
                    Image(
                        bitmap = remember(preview) { preview.toBitmap(128, 128).asImageBitmap() },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}