package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.data.model.Changelog
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardActionButton
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardDivider
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardLabelStyle
import com.example.nonelauncher.ui.theme.BoardMonoStyle
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardQuietButton
import com.example.nonelauncher.ui.theme.FlapWordmark
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SettingsSheet(
    recentIntentions: List<Intention>,
    onWallpaperClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusModeEnabled: Boolean = false,
    onToggleFocusMode: (Boolean) -> Unit = {}
) {
    BoardPanel(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                FlapWordmark(text = "Settings", cellHeight = 26.dp, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                BoardLabel(text = "Launcher control", color = Board.Steel)
            }

            // Focus mode is the app's core switch and was missing from settings entirely.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(Board.cell)
                    .background(Board.InkSoft)
                    .border(1.dp, Board.HairFaint, Board.cell)
                    .clickable { onToggleFocusMode(!focusModeEnabled) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "nONE FOCUS MODE",
                        color = Board.Paint,
                        style = BoardLabelStyle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grayscale everything that is not essential",
                        color = Board.Steel,
                        style = BoardBodyStyle.copy(fontSize = 12.sp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                TrackSwitch(on = focusModeEnabled)
            }

            BoardActionButton(
                text = "Wallpaper Selection",
                onClick = onWallpaperClick,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardLabel(text = "Intentions Log", color = Board.Paint)
                    BoardQuietButton(
                        text = "History",
                        onClick = onHistoryClick,
                        modifier = Modifier.widthIn(min = 104.dp)
                    )
                }
                BoardDivider()
                if (recentIntentions.isEmpty()) {
                    Text(
                        text = "No intentions logged yet. When you open a distracting app, the gate asks why — the answer lands here.",
                        color = Board.PaintDim,
                        style = BoardBodyStyle.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(recentIntentions) { intention ->
                            IntentionCard(intention)
                        }
                    }
                }
            }

            BoardDivider()
            BoardLabel(
                text = "nONE Focus Launcher · v${Changelog.first().versionName}",
                color = Board.Steel
            )
        }
    }
}

/** Board-language switch: a squared track, no Material thumb shadow. */
@Composable
private fun TrackSwitch(on: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(Board.cell)
            .background(if (on) Board.Action else Board.Well)
            .border(1.dp, if (on) Board.Action else Board.Hair, Board.cell),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(18.dp)
                .clip(Board.cell)
                .background(if (on) Board.Ink else Board.Steel)
        )
    }
}

@Composable
fun IntentionCard(intention: Intention) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Board.cell)
            .background(Board.InkSoft)
            .border(1.dp, Board.HairFaint, Board.cell)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${intention.appLabel.uppercase()} — ${intention.intentionText}",
                style = BoardBodyStyle.copy(fontSize = 13.sp),
                color = Board.Paint,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            val date = remember(intention.timestamp) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(intention.timestamp)
            }
            Text(
                text = date,
                style = BoardMonoStyle.copy(fontSize = 11.sp),
                color = Board.Steel
            )
        }
    }
}
