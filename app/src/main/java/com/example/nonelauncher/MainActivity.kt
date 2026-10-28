package com.example.nonelauncher

import android.os.Bundle
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Block back button — stay on home screen
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { }
        })

        enableEdgeToEdge()

        // The wallpaper is drawn inside the composition (WallpaperBackground) so glass can sample
        // it. Reading the static wallpaper needs photo access on Android 13+, though, so the window
        // stays transparent with the system wallpaper behind it as the un-granted fallback.
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)

        setContent {
            NoneLauncherTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    LauncherScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
