package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.nonelauncher.ui.components.GlassContainer
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun IntentionHistorySheet(
    intentions: List<Intention>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = HazeGlassMorphism.rememberHazeState()
    val groupedIntentions = remember(intentions) {
        // Sort groups by their newest entry, descending: comparator-equal day labels can no longer
        // collide in a SortedMap and silently drop older days (the old toSortedMap bug).
        intentions.groupBy { intention ->
            getRelativeDate(Date(intention.timestamp))
        }.toList().sortedByDescending { (_, dayIntentions) ->
            dayIntentions.maxOf { it.timestamp }
        }
    }

    GlassContainer(
        modifier = modifier.fillMaxSize(),
        cornerRadius = 32.dp,
        blurRadius = 22.dp,
        tintAlpha = 0.55f
    ) {
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .hazeChild(state = hazeState, blurRadius = 8.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.335f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedIntentions.forEach { (dateLabel, intentionsForDate) ->
                    item {
                        Text(
                            dateLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(intentionsForDate) { intention ->
                        HistoryItem(intention, hazeState)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(intention: Intention, hazeState: HazeState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .hazeChild(state = hazeState, blurRadius = 12.dp)
            .background(Color.Black.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
                val dateTime = remember(intention.timestamp) {
                    getFormattedDateTime(Date(intention.timestamp))
                }
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.4f)
                )
            }
        }
    }
}

fun getRelativeDate(timestamp: Date): String {
    val intentionCal = Calendar.getInstance().apply { time = timestamp }
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    
    return when {
        isSameDay(intentionCal, todayCal) -> "Today"
        isSameDay(intentionCal, yesterdayCal) -> "Yesterday"
        else -> SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(timestamp)
    }
}

fun getFormattedDateTime(timestamp: Date): String {
    val intentionCal = Calendar.getInstance().apply { time = timestamp }
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    
    return when {
        isSameDay(intentionCal, todayCal) -> 
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp)
        isSameDay(intentionCal, yesterdayCal) -> 
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp)
        else -> 
            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(timestamp)
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}