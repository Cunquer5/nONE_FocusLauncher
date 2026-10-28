package com.example.nonelauncher.ui.modern.components

import android.app.WallpaperManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.nonelauncher.ui.components.GlassMorphismButton
import com.example.nonelauncher.ui.theme.LiquidGlassPanel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.liquidGlass

/**
 * Wallpaper selection, drawn in the home window like the intention gate so its glass samples
 * the same wallpaper source as every other surface.
 */
@Composable
fun WallpaperSelectionDialog(
    onDismiss: () -> Unit,
    onOrangeColorSelected: () -> Unit,
    onCustomWallpaperClick: () -> Unit
) {
    val context = LocalContext.current
    val wallpaperManager = WallpaperManager.getInstance(context)

    val currentWallpaper = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wallpaperManager.drawable
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    BackHandler { onDismiss() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
        )

        LiquidGlassPanel(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                // Keep taps on the panel from reaching the dismissing scrim behind it.
                .pointerInput(Unit) { detectTapGestures { } },
            shape = RoundedCornerShape(28.dp),
            style = LiquidGlassStyle.Heavy.copy(
                blurRadius = 26.dp,
                tint = Color(0xFF0A0A0B).copy(alpha = 0.52f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Select Wallpaper",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                WallpaperOption(
                    title = "Default Saturated Orange",
                    description = "Use the launcher's signature orange color",
                    backgroundColor = OrangePrimary,
                    onClick = {
                        onOrangeColorSelected()
                        onDismiss()
                    }
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                WallpaperOption(
                    title = "Choose Custom Wallpaper",
                    description = "Select from your device storage",
                    backgroundColor = Color(0xFF2A2A2A),
                    preview = currentWallpaper,
                    onClick = {
                        onCustomWallpaperClick()
                        onDismiss()
                    }
                )

                GlassMorphismButton(
                    onClick = onDismiss,
                    text = "Cancel",
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
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .liquidGlass(
                hazeState = null,
                shape = shape,
                style = LiquidGlassStyle.Thin.copy(
                    tint = Color(0xFF0A0A0B).copy(alpha = 0.38f),
                    rimAlpha = 0.14f,
                    sheenAlpha = 0.03f,
                    shadowElevation = 0.dp,
                ),
                sampling = false,
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
