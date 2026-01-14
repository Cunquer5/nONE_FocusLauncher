package com.example.focuslauncher.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import com.example.focuslauncher.data.model.AppModel
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
            val bitmap = drawableToBitmap(drawable, iconSize, scale = 1.25f)
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
        
        // Calculate bounds to scale content from center
        val scaledSize = (size * scale).toInt()
        val offset = (size - scaledSize) / 2
        
        drawable.setBounds(offset, offset, offset + scaledSize, offset + scaledSize)
        drawable.draw(canvas)
        return bitmap
    }
}
