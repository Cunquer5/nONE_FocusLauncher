package com.example.nonelauncher.ui.modern.components

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nonelauncher.ui.theme.OrangePrimary

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
                .background(Color(0xFF1E1E1E), RoundedCornerShape(24.dp))
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
                    isSelected = false,
                    onClick = {
                        onOrangeColorSelected()
                        onDismiss()
                    }
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
                    isSelected = false,
                    onClick = {
                        onCustomWallpaperClick()
                        onDismiss()
                    }
                )

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color.White)
                }
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
    isSelected: Boolean = false,
    preview: android.graphics.drawable.Drawable? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor.copy(alpha = 0.3f))
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

            // Selection Indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
