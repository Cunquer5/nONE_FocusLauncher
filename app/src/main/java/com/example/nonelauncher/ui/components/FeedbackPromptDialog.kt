package com.example.nonelauncher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import com.example.nonelauncher.ui.theme.LiquidGlassPanel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle

@Composable
fun FeedbackPromptDialog(
    onVisitWebsite: () -> Unit,
    onLater: () -> Unit,
    onNoThanks: () -> Unit,
) {
    BackHandler { onLater() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLater,
                )
        )

        LiquidGlassPanel(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                // Keep taps on the panel from reaching the dismissing scrim behind it.
                .pointerInput(Unit) { detectTapGestures { } },
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
                    text = "Share your feedback",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                Text(
                    text = "We'd love to hear your thoughts. Please visit our feedback page.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    TextButton(onClick = onVisitWebsite) {
                        Text("Visit Website", color = Color.White)
                    }
                    TextButton(onClick = onLater) {
                        Text("Later", color = Color.White.copy(alpha = 0.7f))
                    }
                    TextButton(onClick = onNoThanks) {
                        Text("No Thanks", color = Color.White.copy(alpha = 0.7f))
                    }
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
