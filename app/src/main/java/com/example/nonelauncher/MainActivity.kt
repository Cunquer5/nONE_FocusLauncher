package com.example.nonelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.nonelauncher.ui.LauncherScreen
import com.example.nonelauncher.ui.theme.NoneLauncherTheme

class MainActivity : ComponentActivity() {
    private var appWidgetHost: android.appwidget.AppWidgetHost? = null
    private var appWidgetManager: android.appwidget.AppWidgetManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        appWidgetManager = android.appwidget.AppWidgetManager.getInstance(this)
        appWidgetHost = android.appwidget.AppWidgetHost(this, APP_WIDGET_HOST_ID)
        appWidgetHost?.startListening()

        enableEdgeToEdge()
        
        setContent {
            NoneLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
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

    companion object {
        const val APP_WIDGET_HOST_ID = 1024
    }
}
