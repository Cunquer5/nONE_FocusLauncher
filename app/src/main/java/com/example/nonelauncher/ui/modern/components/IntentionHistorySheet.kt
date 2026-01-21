package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.GlassMorphism.cardGlassBackground
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun IntentionHistorySheet(
    intentions: List<Intention>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha=0.89f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // LAYER 1: Background with blur (BEHIND content)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .cardGlassBackground()
            )
            
            // LAYER 2: Content on top (NO blur)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Intention History",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Review your focus moments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.Black, CircleShape)
                        .size(32.dp)
                ) {
                    //Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Simplified Grouping: Just show list for now, ideally group by date
            Text(
                "TODAY",
                style = MaterialTheme.typography.labelSmall,
                color = OrangePrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(intentions) { intention ->
                    HistoryItem(intention)
                }
            }
        }
        }
    }
}

@Composable
fun HistoryItem(intention: Intention) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(0.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
             //Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White.copy(0.7f))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
             Text(
                 text = "${intention.appLabel}: ${intention.intentionText}",
                 style = MaterialTheme.typography.bodyMedium,
                 color = Color.White.copy(0.9f)
             )
             Spacer(modifier = Modifier.height(4.dp))
             val time = remember(intention.timestamp) {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(intention.timestamp)
             }
             Text(
                 text = time,
                 style = MaterialTheme.typography.labelSmall,
                 color = Color.White.copy(0.4f)
             )
        }
    }
}

