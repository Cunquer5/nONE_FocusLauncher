package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardActionButton
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardCloseButton
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardTextField
import com.example.nonelauncher.ui.theme.FlapWordmark

/**
 * Structure editor sheet: the day as one ordered step per line.
 * Saving restarts the flow at step 1; the current step advances from the board rows.
 */
@Composable
fun StructureSheet(
    steps: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(steps.joinToString("\n")) }

    BoardPanel(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlapWordmark(text = "Structure", cellHeight = 26.dp, fontSize = 13.sp)
                BoardCloseButton(onClick = onDismiss)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "ONE STEP PER LINE. TAP A ROW ON THE BOARD TO SET THE CURRENT STEP.",
                style = BoardBodyStyle.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = Board.Steel
            )
            Spacer(modifier = Modifier.height(16.dp))

            BoardTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Wakeup\nSunlight exposure\nWorkout\nCleanse\nEat\nFree slot\nLunch\n...",
                textStyle = BoardBodyStyle,
                minLines = 8,
                maxLines = 14,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.height(20.dp))

            BoardLabel(
                text = "${text.lines().count { it.isNotBlank() }} steps",
                color = Board.Steel
            )
            Spacer(modifier = Modifier.height(10.dp))
            BoardActionButton(
                text = "Save Structure",
                onClick = { onSave(text.lines().filter { it.isNotBlank() }) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
