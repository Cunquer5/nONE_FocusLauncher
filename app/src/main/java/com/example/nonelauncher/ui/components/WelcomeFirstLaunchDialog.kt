package com.example.nonelauncher.ui.components

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

@Composable
fun WelcomeFirstLaunchDialog(
    isDefaultLauncher: Boolean,
    onGetStarted: () -> Unit,
) {
    Dialog(onDismissRequest = onGetStarted) {
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
                    text = "Welcome to nONE Launcher",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = if (isDefaultLauncher) {
                        "A minimalist home focused on calm and intention. Swipe down to search apps, contacts, and files."
                    } else {
                        "Thanks for installing. Set nONE as your default Home app to use the launcher, then swipe down to search apps, contacts, and files."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    TextButton(onClick = onGetStarted) {
                        Text("Get Started")
                    }
                }
            }
        }
    }
}
