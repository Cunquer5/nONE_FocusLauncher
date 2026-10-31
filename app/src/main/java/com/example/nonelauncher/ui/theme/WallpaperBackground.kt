package com.example.nonelauncher.ui.theme

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The home's ground: the device wallpaper drawn by the app itself.
 *
 * The window used to be transparent and the system painted the wallpaper behind it, which put the
 * wallpaper outside the composition — nothing glass could sample, so every "blur" was really just a
 * tint. Drawing the wallpaper in-tree gives Haze a real source and keeps the ground under app control
 * (dimming, cropping, fallback) on every ROM.
 */
@Composable
fun WallpaperBackground(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    refreshKey: Int = 0,
    onLoadResult: (Boolean) -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // Everything inside — wallpaper and scrim — is the source glass samples.
            .hazeSource(hazeState)
    ) {
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight
        val wallpaper = rememberWallpaperBitmap(widthPx, heightPx, refreshKey, onLoadResult)
        val wallpaperAlpha by animateFloatAsState(
            targetValue = if (wallpaper != null) 1f else 0f,
            animationSpec = tween(durationMillis = 260),
            label = "wallpaperFade"
        )

        // No stand-in ground is painted: until the static wallpaper can be read, the window is
        // transparent over the system's own wallpaper (see MainActivity). Painting an opaque
        // fallback here would hide the user's wallpaper instead of showing it.
        if (wallpaper != null) {
            Image(
                bitmap = wallpaper,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(wallpaperAlpha)
            )
        }

        // Dimming sits inside the haze source so glass samples the same ground the eye sees.
        // Darker at both chrome edges where type lives, nearly open through the middle.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.34f),
                        0.18f to Color.Black.copy(alpha = 0.08f),
                        0.55f to Color.Black.copy(alpha = 0.04f),
                        1f to Color.Black.copy(alpha = 0.42f)
                    )
                )
        )
    }
}

@Composable
private fun rememberWallpaperBitmap(
    widthPx: Int,
    heightPx: Int,
    refreshKey: Int,
    onLoadResult: (Boolean) -> Unit,
): ImageBitmap? {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    var version by remember { mutableIntStateOf(wallpaperVersion(context)) }

    DisposableEffect(context, lifecycleOwner) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                version = wallpaperVersion(context)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_WALLPAPER_CHANGED),
            // Sent by the system, so a not-exported receiver never sees it and the launcher
            // would keep drawing the old wallpaper until the next resume.
            ContextCompat.RECEIVER_EXPORTED
        )
        // The picker can return without the broadcast reaching us first; resume re-reads the id.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) version = wallpaperVersion(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            context.unregisterReceiver(receiver)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val bitmap by produceState<ImageBitmap?>(initialValue = null, version, refreshKey, widthPx, heightPx) {
        val loaded = withContext(Dispatchers.IO) {
            loadWallpaperBitmap(context, widthPx, heightPx)
        }
        value = loaded
        onLoadResult(loaded != null)
    }
    return bitmap
}

private fun wallpaperVersion(context: Context): Int = runCatching {
    WallpaperManager.getInstance(context).getWallpaperId(WallpaperManager.FLAG_SYSTEM)
}.getOrDefault(0)

private fun loadWallpaperBitmap(context: Context, widthPx: Int, heightPx: Int): ImageBitmap? {
    if (widthPx <= 0 || heightPx <= 0) return null
    return try {
        val manager = WallpaperManager.getInstance(context)
        // getDrawable() asks the wallpaper service for READ_EXTERNAL_STORAGE, which a target-34
        // app can no longer hold on some ROMs. The wallpaper file descriptor is the second door.
        val drawable = runCatching { manager.drawable }.getOrNull()
        val source = (drawable as? BitmapDrawable)?.bitmap
            ?: runCatching {
                manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)?.use { file ->
                    android.graphics.BitmapFactory.decodeFileDescriptor(file.fileDescriptor)
                }
            }.getOrNull()
            ?: return drawable?.let { renderDrawable(it, widthPx, heightPx) }

        if (source.width <= 0 || source.height <= 0) return null
        val target = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(target)
        // Centre-crop the wallpaper to the window instead of stretching it.
        val scale = max(widthPx / source.width.toFloat(), heightPx / source.height.toFloat())
        val srcWidth = widthPx / scale
        val srcHeight = heightPx / scale
        val left = (source.width - srcWidth) / 2f
        val top = (source.height - srcHeight) / 2f
        canvas.drawBitmap(
            source,
            Rect(
                left.roundToInt(),
                top.roundToInt(),
                (left + srcWidth).roundToInt(),
                (top + srcHeight).roundToInt()
            ),
            RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()),
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        target.asImageBitmap()
    } catch (t: Throwable) {
        // Live wallpapers and locked-down ROMs may refuse a static bitmap; the fallback ground shows.
        android.util.Log.w("WallpaperBackground", "static wallpaper unavailable", t)
        null
    }
}

private fun renderDrawable(
    drawable: android.graphics.drawable.Drawable,
    widthPx: Int,
    heightPx: Int,
): ImageBitmap {
    val target = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(target)
    drawable.setBounds(0, 0, widthPx, heightPx)
    drawable.draw(canvas)
    return target.asImageBitmap()
}
