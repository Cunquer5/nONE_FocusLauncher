package com.example.nonelauncher

import android.os.Bundle
import android.view.WindowManager
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.nonelauncher.ui.LauncherScreen
import com.example.nonelauncher.ui.theme.NoneLauncherTheme
import com.example.nonelauncher.ui.theme.Transparent

class MainActivity : ComponentActivity() {
    private var appWidgetHost: android.appwidget.AppWidgetHost? = null
    private var appWidgetManager: android.appwidget.AppWidgetManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        
        appWidgetManager = android.appwidget.AppWidgetManager.getInstance(this)
        appWidgetHost = android.appwidget.AppWidgetHost(this, APP_WIDGET_HOST_ID)
        appWidgetHost?.startListening()

        // Handle back button to stay on home screen
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - stay on home screen
            }
        })

        enableEdgeToEdge()
        
        // Properly show system wallpaper
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawableResource(android.R.color.transparent)


        setContent {
            NoneLauncherTheme {
                Box(modifier= Modifier.fillMaxSize()) {
                    LauncherScreen(
                        appWidgetHost = appWidgetHost,
                        appWidgetManager = appWidgetManager
                    )
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        appWidgetHost?.stopListening()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle home button press and recent tasks navigation
        if (intent?.action == Intent.ACTION_MAIN && 
            intent.hasCategory(Intent.CATEGORY_HOME)) {
            // Properly restore launcher state from recents
            // Don't call moveTaskToBack - let the system handle it naturally
        }
    }

    companion object {
        const val APP_WIDGET_HOST_ID = 1024
    }
}
