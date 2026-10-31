package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.components.GlassContainer
import com.example.nonelauncher.ui.components.GradientGlassMorphismButton
import com.example.nonelauncher.ui.theme.GlassIconButton

/**
 * Structure editor sheet: the day as one ordered step per line.
 * Saving restarts the flow at step 1; the current step advances from the home-screen chip.
 */
@Composable
fun StructureSheet(
    steps: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(steps.joinToString("\n")) }

    GlassContainer(
        modifier = modifier,
        cornerRadius = 32.dp,
        blurRadius = 22.dp,
        tintAlpha = 0.55f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = 36.dp, end = 24.dp, bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Structure",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                GlassIconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your day as an ordered flow — one step per line.\nTap the chip at the top of home to move to the next step.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        "Wakeup\nSunlight exposure\nWorkout\nCleanse\nEat\nFree slot\nLunch\n...",
                        color = Color.White.copy(0.4f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.35f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFFF4D00)
                ),
                shape = RoundedCornerShape(16.dp),
                minLines = 8,
                maxLines = 14,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.height(24.dp))

            GradientGlassMorphismButton(
                onClick = { onSave(text.lines().filter { it.isNotBlank() }) },
                text = "Save Structure",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}
