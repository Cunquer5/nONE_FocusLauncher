package com.example.nonelauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.components.GlassMorphismButton

@Composable
fun IntentionDialog(
    app: AppModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var intentionText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val hazeState = HazeGlassMorphism.rememberHazeState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Opening ${app.label}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "What is your intention?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = intentionText,
                    onValueChange = { 
                        intentionText = it 
                        isError = false
                    },
                    label = { Text("Intention (min 5 chars)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    minLines = 2,
                    maxLines = 4
                )
                
                if (isError) {
                    Text(
                        text = "Please enter at least 5 characters.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Changed to SpaceBetween for better layout
                ) {
                    GlassMorphismButton(
                        onClick = onDismiss,
                        text = "Cancel",
                        hazeState = hazeState,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.height(48.dp)
                    )
                    
                    GlassMorphismButton(
                        onClick = {
                            if (intentionText.trim().length >= 5) {
                                onConfirm(intentionText.trim())
                            } else {
                                isError = true
                            }
                        },
                        text = "Open App",
                        hazeState = hazeState,
                        modifier = Modifier.height(48.dp)
                    )
                }
            }
        }
    }
}