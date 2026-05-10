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
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SettingsSheet(
    recentIntentions: List<Intention>,
    onLegacyMode: () -> Unit,
    onWallpaperClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = HazeGlassMorphism.rememberHazeState()

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background blur layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState, backgroundColor = Color.Transparent)
            )

            // Glass surface + content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .hazeChild(state = hazeState, blurRadius = 16.dp)
                    .background(Color.Black.copy(alpha = 0.46f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(32.dp)
                    )
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

                    // Modern / Legacy mode toggle
                    ModeToggle(isModern = true, onLegacyClick = onLegacyMode, hazeState)

                    // Wallpaper button
                    Button(
                        onClick = onWallpaperClick,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Wallpaper Selection",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                                IntentionCard(intention, hazeState)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeToggle(isModern: Boolean, onLegacyClick: () -> Unit, hazeState: HazeState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .hazeChild(state = hazeState, blurRadius = 12.dp)
            .background(Color.Black.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isModern) OrangePrimary else Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Modern", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLegacyClick() }
                    .background(if (!isModern) OrangePrimary else Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Legacy", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun IntentionCard(intention: Intention, hazeState: HazeState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .hazeChild(state = hazeState, blurRadius = 12.dp)
            .background(Color.Black.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
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
