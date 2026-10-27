package com.example.nonelauncher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardActionButton
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardLabelStyle
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardQuietButton
import com.example.nonelauncher.ui.theme.FlapWordmark

@Composable
fun FeedbackPromptDialog(
    onVisitWebsite: () -> Unit,
    onLater: () -> Unit,
    onNoThanks: () -> Unit,
) {
    Dialog(onDismissRequest = onLater) {
        BoardPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlapWordmark(text = "Feedback", cellHeight = 24.dp, fontSize = 12.sp)
                    BoardLabel(text = "2 min", color = Board.Steel)
                }

                Text(
                    text = "Tell us what works and what doesn't. Every note shapes the next board.",
                    style = BoardBodyStyle.copy(fontSize = 13.sp),
                    color = Board.Paint.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardQuietButton(
                        text = "Later",
                        onClick = onLater,
                        modifier = Modifier.weight(1f)
                    )
                    BoardActionButton(
                        text = "Visit Website",
                        onClick = onVisitWebsite,
                        modifier = Modifier.weight(1.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .heightIn(min = 48.dp)
                        .clickable(onClick = onNoThanks)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO THANKS",
                        color = Board.Steel,
                        style = BoardLabelStyle.copy(fontSize = 9.sp, letterSpacing = 1.4.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun openFeedbackWebsite(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(UserPreferencesRepository.FEEDBACK_WEBSITE_URL),
    )
    context.startActivity(intent)
}
