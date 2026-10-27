package com.example.nonelauncher.ui.modern.components

import android.app.WallpaperManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardQuietButton
import com.example.nonelauncher.ui.theme.FlapWordmark

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
        BoardPanel(
            modifier = Modifier
                .fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlapWordmark(text = "Wallpaper", cellHeight = 24.dp, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(2.dp))

                WallpaperOption(
                    title = "Orange",
                    description = "The launcher's signature saturated field",
                    swatch = OrangeSwatch,
                    preview = null,
                    onClick = {
                        onOrangeColorSelected()
                        onDismiss()
                    }
                )

                WallpaperOption(
                    title = "Custom",
                    description = "Pick an image from your device",
                    swatch = Board.InkSoft,
                    preview = currentWallpaper,
                    onClick = {
                        onCustomWallpaperClick()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                BoardQuietButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private val OrangeSwatch = com.example.nonelauncher.ui.theme.Board.Action

/**
 * Individual wallpaper option cell
 */
@Composable
private fun WallpaperOption(
    title: String,
    description: String,
    swatch: Color,
    preview: android.graphics.drawable.Drawable?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .clip(Board.cell)
            .background(Board.InkSoft)
            .border(1.dp, Board.HairFaint, Board.cell)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = BoardBodyStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = Board.Paint
            )
            Text(
                text = description,
                style = BoardBodyStyle.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = Board.PaintDim
            )
        }

        // Preview swatch: solid fill for the orange option, the live wallpaper for custom
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(Board.cell)
                .background(swatch)
                .border(1.dp, Board.Hair, Board.cell)
        ) {
            if (preview != null) {
                Image(
                    bitmap = remember(preview) { preview.toBitmap(112, 112).asImageBitmap() },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
