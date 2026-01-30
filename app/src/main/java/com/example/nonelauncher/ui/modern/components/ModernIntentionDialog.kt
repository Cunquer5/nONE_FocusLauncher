package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.data.model.AppModel
import android.graphics.Color as AndroidColor
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.theme.HazeGlassMorphism.glassBackground
import com.example.nonelauncher.ui.theme.HazeGlassMorphism.hazeBackground

@Composable
fun ModernIntentionDialog(
    app: AppModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    wallpaperColors: android.app.WallpaperColors? = null
) {
    var intentionText by remember { mutableStateOf("") }
    val hazeState = HazeGlassMorphism.rememberHazeState()

    // Using WallpaperColors.primaryColor and converting to Compose Color
    val primaryColor = wallpaperColors?.primaryColor?.toArgb()?.let { 
        androidx.compose.ui.graphics.Color(it)
    } ?: OrangePrimary

    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                // LAYER 1: Background with Haze blur (BEHIND content)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .hazeBackground(hazeState, backgroundColor = Color.Transparent)
                )

                // LAYER 2: Glass surface + content (NO blur)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassBackground(
                            hazeState = hazeState,
                            intensity = HazeGlassMorphism.BlurIntensity.MEDIUM,
                            color = Color.Black.copy(alpha = 0.3f),
                            cornerRadius = 32.dp
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFEFEFEF))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = """Intention for 
opening 
${app.label}?""",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        TextField(
                            value = intentionText,
                            onValueChange = { intentionText = it },
                            placeholder = {
                                Text(
                                    "Why are you opening this app?",
                                    color = Color.White.copy(0.6f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = primaryColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cancel Button
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Proceed Button
                            Button(
                                onClick = {
                                    if (intentionText.isNotBlank()) onConfirm(intentionText)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Proceed →",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}