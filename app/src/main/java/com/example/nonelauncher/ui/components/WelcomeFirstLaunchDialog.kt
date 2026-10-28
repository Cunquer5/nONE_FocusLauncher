package com.example.nonelauncher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.LiquidGlassPanel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle

@Composable
fun WelcomeFirstLaunchDialog(
    isDefaultLauncher: Boolean,
    needsWallpaperAccess: Boolean,
    onGetStarted: () -> Unit,
) {
    BackHandler { onGetStarted() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Holds focus: the first-run card is the only thing to answer, so taps outside do nothing.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
        )

        LiquidGlassPanel(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            style = LiquidGlassStyle.Heavy.copy(
                blurRadius = 26.dp,
                tint = Color(0xFF0A0A0B).copy(alpha = 0.52f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Welcome to nONE Launcher",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                Text(
                    text = if (isDefaultLauncher) {
                        "A minimalist home focused on calm and intention. Swipe up from the bottom edge, or tap the dot, to open the dial of apps and surfaces."
                    } else {
                        "Thanks for installing. Set nONE as your default Home app to use the launcher, then swipe up from the bottom edge to open the dial."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
                if (needsWallpaperAccess) {
                    Text(
                        text = "nONE draws your wallpaper itself so the glass can blur it. Allow photo access when your device asks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    TextButton(onClick = onGetStarted) {
                        Text("Get Started", color = Color.White)
                    }
                }
            }
        }
    }
}
