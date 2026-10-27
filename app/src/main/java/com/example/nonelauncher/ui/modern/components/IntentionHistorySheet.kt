package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardCloseButton
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardLabelStyle
import com.example.nonelauncher.ui.theme.BoardMonoStyle
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.FlapWordmark
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
    val groupedIntentions = remember(intentions) {
        // Sort groups by their newest entry, descending: comparator-equal day labels can no longer
        // collide in a SortedMap and silently drop older days (the old toSortedMap bug).
        intentions.groupBy { intention ->
            getRelativeDate(Date(intention.timestamp))
        }.toList().sortedByDescending { (_, dayIntentions) ->
            dayIntentions.maxOf { it.timestamp }
        }
    }

    BoardPanel(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlapWordmark(text = "History", cellHeight = 26.dp, fontSize = 13.sp)
                BoardCloseButton(onClick = onDismiss)
            }

            Spacer(modifier = Modifier.height(8.dp))
            BoardLabel(text = "Logged intentions, newest first", color = Board.Steel)

            Spacer(modifier = Modifier.height(18.dp))

            if (groupedIntentions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BoardLabel(text = "Log empty", color = Board.Steel)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Open a distracting app and the gate will ask why. Answers collect here, by day.",
                        style = BoardBodyStyle.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = Board.PaintDim,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groupedIntentions.forEach { (dateLabel, intentionsForDate) ->
                        item {
                            Text(
                                text = dateLabel.uppercase(),
                                color = Board.Steel,
                                style = BoardLabelStyle.copy(letterSpacing = 2.sp),
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }

                        items(intentionsForDate) { intention ->
                            HistoryItem(intention)
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(intention: Intention) {
    val time = remember(intention.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(intention.timestamp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Board.cell)
            .background(Board.InkSoft)
            .border(1.dp, Board.HairFaint, Board.cell)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            style = BoardMonoStyle.copy(fontSize = 12.sp),
            color = Board.Steel,
            modifier = Modifier.width(46.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Board.HairFaint)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = intention.appLabel.uppercase(),
                style = BoardLabelStyle,
                color = Board.PaintDim
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = intention.intentionText,
                style = BoardBodyStyle.copy(fontSize = 13.sp),
                color = Board.Paint,
                maxLines = 2
            )
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
