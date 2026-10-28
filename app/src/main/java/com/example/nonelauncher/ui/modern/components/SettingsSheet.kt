package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.ui.components.GradientGlassMorphismButton
import com.example.nonelauncher.ui.theme.LiquidGlassPanel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SettingsSheet(
    recentIntentions: List<Intention>,
    onWallpaperClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassPanel(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        style = LiquidGlassStyle.Regular.copy(
            blurRadius = 22.dp,
            tint = Color(0xFF0A0A0B).copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Launcher Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Customize your minimalist experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Wallpaper button
            GradientGlassMorphismButton(
                onClick = onWallpaperClick,
                text = "Wallpaper Selection",
                leadingIcon = {
                    Icon(
                        Icons.Default.Wallpaper,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            // Intentions log
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Intentions Log",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "HISTORY",
                        color = OrangePrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onHistoryClick() }
                    )
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentIntentions) { intention ->
                        IntentionCard(intention)
                    }
                }
            }
        }
    }
}

@Composable
fun IntentionCard(intention: Intention) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // A row inside the sheet: already over glass, so a flat tint instead of a second
            // backdrop pass.
            .liquidGlass(
                hazeState = null,
                shape = RoundedCornerShape(16.dp),
                style = LiquidGlassStyle.Thin.copy(
                    tint = Color(0xFF0A0A0B).copy(alpha = 0.42f),
                    rimAlpha = 0.12f,
                    sheenAlpha = 0.03f,
                    shadowElevation = 0.dp,
                ),
                sampling = false,
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "${intention.appLabel}: ${intention.intentionText}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.9f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            val date = remember(intention.timestamp) {
                SimpleDateFormat("'Today', h:mm a", Locale.getDefault()).format(intention.timestamp)
            }
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.4f)
            )
        }
    }
}
