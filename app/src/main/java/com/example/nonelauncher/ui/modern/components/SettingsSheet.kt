package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.OrangePrimary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SettingsSheet(
    recentIntentions: List<Intention>,
    focusApps: List<AppModel>,
    onLegacyMode: () -> Unit,
    onWallpaperClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onToggleFocusApp: (AppModel) -> Unit,
    onToggleFocusMode: () -> Unit,
    onToggleBlur: () -> Unit,
    isFocusMode: Boolean,
    isBlur: Boolean,
    modifier: Modifier = Modifier
) {
    // Custom Background Card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(), // Occupy available space passed by parent
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF221510)) // Deep brown match
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

            // Mode Toggle
            ModeToggle(isModern = true, onLegacyClick = onLegacyMode)

            // Wallpaper Button
            Button(
                onClick = onWallpaperClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                 Icon(Icons.Default.Wallpaper, contentDescription = null, tint = Color.Black)
                 Spacer(modifier = Modifier.width(8.dp))
                 Text("Wallpaper Selection", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            // Intentions Log - Scrollable
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Intentions Log", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "HISTORY", 
                        color = OrangePrimary, 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onHistoryClick() }
                    )
                }
                
                // Scrollable intentions list
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentIntentions) { intention ->
                        IntentionCard(intention)
                    }
                }
            }

            // Focus App Management
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Focus App Management", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(focusApps) { app ->
                        FocusAppItem(app, onToggle = { onToggleFocusApp(app) })
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            // Bottom Mode Selector
            BottomModeSelector(
                isFocus = isFocusMode,
                isBlur = isBlur,
                onNormal = { 
                    if(isFocusMode) onToggleFocusMode()
                    if(isBlur) onToggleBlur()
                },
                onFocus = {
                    if(!isFocusMode) onToggleFocusMode()
                    if(isBlur) onToggleBlur() // Normal focus implies simple focus
                },
                onBlur = {
                     if(!isFocusMode) onToggleFocusMode()
                     if(!isBlur) onToggleBlur()
                }
            )
        }
    }
}

@Composable
fun ModeToggle(isModern: Boolean, onLegacyClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF382922)), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isModern) Color(0xFF4E3D35) else Color.Transparent), 
            contentAlignment = Alignment.Center
        ) {
            Text("Modern", color = if (isModern) Color.White else Color.White.copy(0.4f), fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onLegacyClick() }
                .background(if (!isModern) Color(0xFF4E3D35) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text("Legacy", color = if (!isModern) Color.White else Color.White.copy(0.4f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun IntentionCard(intention: Intention) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF382922))
            .padding(16.dp)
    ) {
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

@Composable
fun FocusAppItem(app: AppModel, onToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onToggle() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (app.isFocusApp) OrangePrimary else Color(0xFF382922)),
            contentAlignment = Alignment.Center
        ) {
             Image(
                bitmap = app.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                colorFilter = if (!app.isFocusApp) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(app.label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f), maxLines = 1)
    }
}

@Composable
fun BottomModeSelector(isFocus: Boolean, isBlur: Boolean, onNormal: () -> Unit, onFocus: () -> Unit, onBlur: () -> Unit) {
    // Logic to visually select: 
    // Normal: !Focus
    // Focus: Focus && !Blur
    // Blur: Focus && Blur
    
    val selection = when {
        isFocus && isBlur -> 2
        isFocus -> 1
        else -> 0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black), // Very dark background for the pill
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Normal
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if(selection == 0) OrangePrimary else Color.Transparent)
                .clickable(onClick = onNormal), 
            contentAlignment = Alignment.Center
        ) {
            Text("Normal", color = if (selection == 0) Color.Black else Color.White.copy(0.4f), fontWeight = if(selection==0) FontWeight.Bold else FontWeight.Normal)
        }
        
        // Focus
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if(selection == 1) OrangePrimary else Color.Transparent)
                .clickable(onClick = onFocus), 
            contentAlignment = Alignment.Center
        ) {
            Text("Focus", color = if (selection == 1) Color.Black else Color.White.copy(0.4f), fontWeight = if(selection==1) FontWeight.Bold else FontWeight.Normal)
        }

        // Blur
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if(selection == 2) OrangePrimary else Color.Transparent)
                .clickable(onClick = onBlur), 
            contentAlignment = Alignment.Center
        ) {
            Text("Blur", color = if (selection == 2) Color.Black else Color.White.copy(0.4f), fontWeight = if(selection==2) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
