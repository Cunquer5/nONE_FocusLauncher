package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.theme.HazeGlassMorphism.glassBackground
import com.example.nonelauncher.ui.theme.HazeGlassMorphism.hazeBackground
import com.example.nonelauncher.ui.theme.SquircleShapes

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

    // Gentle arrival only — the layout below is the incumbent one, untouched
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(if (shown) 1f else 0f, tween(200), label = "gateFade")
    val risePx = with(LocalDensity.current) { 24.dp.toPx() }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .graphicsLayer {
                        alpha = appear
                        translationY = (1f - appear) * risePx
                    }
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
                            // Squircle well like the search rows — the icon sits on a product
                            // plate, not on a raw light-gray square
                            Box(
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(SquircleShapes.squircle(85.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.08f),
                                        SquircleShapes.squircle(85.dp)
                                    )
                            ) {
                                Image(
                                    bitmap = app.icon,
                                    contentDescription = app.label,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(SquircleShapes.squircle(85.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Intention for opening ${app.label}?",
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
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { if (intentionText.isNotBlank()) onConfirm(intentionText) }
                            ),
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
                            com.example.nonelauncher.ui.components.GlassMorphismButton(
                                onClick = onDismiss,
                                text = "Cancel",
                                hazeState = hazeState,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            )

                            // Proceed Button — dims while the note is empty; guard still blocks submit
                            com.example.nonelauncher.ui.components.GlassMorphismButton(
                                onClick = {
                                    if (intentionText.isNotBlank()) onConfirm(intentionText)
                                },
                                text = "Proceed",
                                hazeState = hazeState,
                                color = primaryColor,
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(56.dp)
                                    .graphicsLayer {
                                        alpha = if (intentionText.isBlank()) 0.35f else 1f
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
