package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
import com.example.nonelauncher.data.model.serializeOneThingTask
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardActionButton
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardCloseButton
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardTextField
import com.example.nonelauncher.ui.theme.FlapWordmark
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun OneThingInputCard(
    onTaskSet: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mainTask by remember { mutableStateOf("") }
    var actionStep by remember { mutableStateOf("") }

    val submit: () -> Unit = {
        if (mainTask.isNotBlank()) {
            val subTasks = actionStep.split("\n").filter { it.isNotBlank() }
                .map { SubTask(text = it, isCompleted = false) }
            onTaskSet(serializeOneThingTask(OneThingTask(mainTask, subTasks)))
            mainTask = ""
            actionStep = ""
        }
    }

    BoardPanel(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlapWordmark(text = "1 Thing", cellHeight = 24.dp, fontSize = 12.sp)
                BoardCloseButton(onClick = onDismiss)
            }

            Spacer(modifier = Modifier.height(6.dp))
            BoardLabel(text = "What must move today?", color = Board.Steel)
            Spacer(modifier = Modifier.height(14.dp))

            BoardTextField(
                value = mainTask,
                onValueChange = { mainTask = it },
                placeholder = "Your main focus",
                textStyle = BoardBodyStyle.copy(fontSize = 16.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            BoardTextField(
                value = actionStep,
                onValueChange = { actionStep = it },
                placeholder = "First action step\nThen the next one",
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            BoardLabel(text = "One step per line · optional", color = Board.Steel)

            Spacer(modifier = Modifier.height(14.dp))

            BoardActionButton(
                text = "Set Focus Task",
                enabled = mainTask.isNotBlank(),
                onClick = submit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
