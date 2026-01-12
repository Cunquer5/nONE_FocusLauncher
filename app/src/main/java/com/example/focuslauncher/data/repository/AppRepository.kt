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

    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val activities = pm.queryIntentActivities(intent, 0)
        val iconSize = (56 * context.resources.displayMetrics.density).toInt()

        activities.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo
            if (activityInfo.packageName == context.packageName) return@mapNotNull null

            val label = resolveInfo.loadLabel(pm).toString()
            val drawable = resolveInfo.loadIcon(pm)
            val bitmap = drawableToBitmap(drawable, iconSize)
            val imageBitmap = bitmap.asImageBitmap()

            AppModel(
                packageName = activityInfo.packageName,
                label = label,
                icon = imageBitmap,
                isEssential = isEssentialApp(activityInfo.packageName, label)
            )
        }.sortedBy { it.label.lowercase() }
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

    private fun drawableToBitmap(drawable: Drawable, size: Int): Bitmap {
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            if (bitmap.width > size || bitmap.height > size) {
                return Bitmap.createScaledBitmap(bitmap, size, size, true)
            }
            return bitmap
        }
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }
}
