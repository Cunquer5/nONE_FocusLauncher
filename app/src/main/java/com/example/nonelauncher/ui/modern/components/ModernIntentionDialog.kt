package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardActionButton
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardQuietButton
import com.example.nonelauncher.ui.theme.BoardTextField
import com.example.nonelauncher.ui.theme.FlapWordmark

@Composable
fun ModernIntentionDialog(
    app: AppModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var intentionText by remember { mutableStateOf("") }

    // Gentle arrival only — the board itself is still
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(if (shown) 1f else 0f, tween(200), label = "gateFade")
    val risePx = with(LocalDensity.current) { 24.dp.toPx() }

    Dialog(onDismissRequest = onDismiss) {
        BoardPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .graphicsLayer {
                    alpha = appear
                    translationY = (1f - appear) * risePx
                }
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlapWordmark(text = "Intention", cellHeight = 24.dp, fontSize = 12.sp)
                    BoardLabel(text = "Gate", color = Board.Steel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(Board.cell)
                            .background(Board.Well)
                            .border(1.dp, Board.Hair, Board.cell)
                    ) {
                        Image(
                            bitmap = app.icon,
                            contentDescription = app.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(Board.cell)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        BoardLabel(text = "Opening", color = Board.Steel)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = app.label.uppercase(),
                            color = Board.Paint,
                            style = BoardBodyStyle.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                BoardLabel(text = "Why are you opening this?", color = Board.Steel)
                Spacer(modifier = Modifier.height(8.dp))

                BoardTextField(
                    value = intentionText,
                    onValueChange = { intentionText = it },
                    placeholder = "One line is enough",
                    textStyle = BoardBodyStyle,
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (intentionText.isNotBlank()) onConfirm(intentionText) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardQuietButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    BoardActionButton(
                        text = "Proceed",
                        enabled = intentionText.isNotBlank(),
                        onClick = {
                            if (intentionText.isNotBlank()) onConfirm(intentionText)
                        },
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}
