package com.example.nonelauncher.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import com.example.nonelauncher.data.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private var cachedApps: List<AppModel>? = null

    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        cachedApps?.let { return@withContext it }

        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val activities = pm.queryIntentActivities(intent, 0)
        val iconSize = (56 * context.resources.displayMetrics.density).toInt()

        val apps = activities.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo
            if (activityInfo.packageName == context.packageName) return@mapNotNull null

            val label = resolveInfo.loadLabel(pm).toString()
            val drawable = resolveInfo.loadIcon(pm)
            // Pre-process the icon: Scale up to 1.25x to ensure it fills the circle (removing adaptive icon corners)
            // This moves the heavy lifting from UI scroll to one-time load.
            val bitmap = drawableToBitmap(drawable, iconSize, scale = 1.0f)
            val imageBitmap = bitmap.asImageBitmap()

            AppModel(
                packageName = activityInfo.packageName,
                label = label,
                icon = imageBitmap,
                isEssential = isEssentialApp(activityInfo.packageName, label)
            )
        }.sortedBy { it.label.lowercase() }

        cachedApps = apps
        apps
    }

    private fun isEssentialApp(packageName: String, label: String): Boolean {
        val essentialKeywords = listOf("bank", "pay", "card", "finance", "wallet", "wallet", "contacts", "phone", "call", "dialer", "message", "sms")
        val essentialPackagePatterns = listOf(
            "com.android.dialer", "com.google.android.dialer",
            "com.android.contacts", "com.google.android.contacts",
            "com.android.messaging", "com.google.android.apps.messaging"
        )

        val normalizedLabel = label.lowercase()
        val normalizedPackage = packageName.lowercase()

        return essentialKeywords.any { normalizedLabel.contains(it) } ||
               essentialPackagePatterns.any { normalizedPackage.contains(it) }
    }

    private fun drawableToBitmap(drawable: Drawable, size: Int, scale: Float = 1.0f): Bitmap {
        if (drawable is BitmapDrawable && scale == 1.0f) {
            val bitmap = drawable.bitmap
            if (bitmap.width == size && bitmap.height == size) {
                return bitmap
            }
        }
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        if (drawable is AdaptiveIconDrawable) {
            // Scale adaptive icons to zoom into the visual viewport (standard is ~1.5x, using 1.3x for safety)
            val adaptiveScale = scale * 1.3f
            val scaledSize = (size * adaptiveScale).toInt()
            val offset = (size - scaledSize) / 2
            val bounds = Rect(offset, offset, offset + scaledSize, offset + scaledSize)
            
            // Draw layers separately to bypass system mask
            drawable.background?.let { 
                it.bounds = bounds
                it.draw(canvas) 
            }
            drawable.foreground?.let { 
                it.bounds = bounds
                it.draw(canvas) 
            }
        } else {
            // Legacy icons: use provided scale (usually 1.0)
            val scaledSize = (size * scale).toInt()
            val offset = (size - scaledSize) / 2
            val bounds = Rect(offset, offset, offset + scaledSize, offset + scaledSize)
            
            drawable.bounds = bounds
            drawable.draw(canvas)
        }
        
        return bitmap
    }
}
