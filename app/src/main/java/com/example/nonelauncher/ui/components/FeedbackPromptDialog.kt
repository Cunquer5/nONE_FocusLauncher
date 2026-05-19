package com.example.nonelauncher.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.data.repository.UserPreferencesRepository

@Composable
fun FeedbackPromptDialog(
    onVisitWebsite: () -> Unit,
    onLater: () -> Unit,
    onNoThanks: () -> Unit,
) {
    Dialog(onDismissRequest = onLater) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Share your feedback",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "We'd love to hear your thoughts. Please visit our feedback page.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    TextButton(onClick = onVisitWebsite) {
                        Text("Visit Website")
                    }
                    TextButton(onClick = onLater) {
                        Text("Later")
                    }
                    TextButton(onClick = onNoThanks) {
                        Text("No Thanks")
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
