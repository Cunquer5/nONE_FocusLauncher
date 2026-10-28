package com.example.nonelauncher.ui.modern.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.components.GlassMorphismButton
import com.example.nonelauncher.ui.components.GradientGlassMorphismButton
import com.example.nonelauncher.ui.theme.LiquidGlassPanel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.SquircleShapes

/**
 * The intention gate, drawn inside the home window rather than in a Dialog. A dialog owns a
 * separate window and cannot sample the wallpaper's Haze source, so its glass would be a flat tint.
 */
@Composable
fun ModernIntentionDialog(
    app: AppModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    wallpaperColors: android.app.WallpaperColors? = null
) {
    var intentionText by remember { mutableStateOf("") }

    val primaryColor = wallpaperColors?.primaryColor?.toArgb()?.let { Color(it) } ?: OrangePrimary

    BackHandler { onDismiss() }

    // Gentle arrival only — the panel settles 24dp up into place.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(if (shown) 1f else 0f, tween(200), label = "gateFade")
    val risePx = with(LocalDensity.current) { 24.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim: a press outside the panel dismisses without submitting.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 14.dp)
                // A panel is opaque to taps: without this, a press on the title would fall
                // through to the dismissing scrim behind it.
                .pointerInput(Unit) { detectTapGestures { } }
                .graphicsLayer {
                    alpha = appear
                    translationY = (1f - appear) * risePx
                }
        ) {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                style = LiquidGlassStyle.Heavy.copy(
                    blurRadius = 26.dp,
                    tint = Color(0xFF0A0A0B).copy(alpha = 0.52f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Squircle well like the search rows — the icon sits on a product
                        // plate, not on a raw light-gray square
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(SquircleShapes.squircle(72.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.08f),
                                    SquircleShapes.squircle(72.dp)
                                )
                        ) {
                            Image(
                                bitmap = app.icon,
                                contentDescription = app.label,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(SquircleShapes.squircle(72.dp))
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

                    Spacer(modifier = Modifier.height(20.dp))

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
                            .height(110.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassMorphismButton(
                            onClick = onDismiss,
                            text = "Cancel",
                            modifier = Modifier.weight(1f),
                        )

                        // Dims while the note is empty; the guard still blocks submit
                        GradientGlassMorphismButton(
                            onClick = {
                                if (intentionText.isNotBlank()) onConfirm(intentionText)
                            },
                            text = "Proceed",
                            color = primaryColor,
                            modifier = Modifier
                                .weight(1.4f)
                                .graphicsLayer {
                                    alpha = if (intentionText.isBlank()) 0.35f else 1f
                                },
                        )
                    }
                }
            }
        }
    }
}
