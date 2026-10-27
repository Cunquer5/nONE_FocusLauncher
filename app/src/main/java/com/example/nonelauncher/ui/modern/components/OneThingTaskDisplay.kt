package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardActionButton
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardCloseButton
import com.example.nonelauncher.ui.theme.BoardDisplayStyle
import com.example.nonelauncher.ui.theme.BoardDivider
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardLabelStyle
import com.example.nonelauncher.ui.theme.BoardMonoStyle
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.FlapWordmark

/**
 * 1 Thing sheet — the focus as the board's white departure band, steps listed beneath it.
 * The check toggles completion; swipe right on the band closes the task.
 */
@Composable
fun OneThingTaskDisplay(
    task: OneThingTask,
    onSetMainCompleted: (Boolean) -> Unit,
    onNewTask: () -> Unit,
    onDismiss: () -> Unit = {},
    onUpdateSubtask: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val subTasks = task.subTasks
    val isCompleted = task.isMainCompleted
    val doneCount = subTasks.count { it.isCompleted }
    val view = LocalView.current

    // Completing the last step finishes the task; the sheet does not have to be told twice.
    LaunchedEffect(doneCount, subTasks.size, isCompleted) {
        if (!isCompleted && subTasks.isNotEmpty() && doneCount == subTasks.size) {
            onSetMainCompleted(true)
        }
    }

    val complete: () -> Unit = {
        if (!isCompleted) {
            subTasks.forEach { if (!it.isCompleted) onUpdateSubtask(it.id, true) }
            onSetMainCompleted(true)
        }
    }

    val reopen: () -> Unit = {
        if (isCompleted) {
            subTasks.forEach { if (it.isCompleted) onUpdateSubtask(it.id, false) }
            onSetMainCompleted(false)
        }
    }

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
                FlapWordmark(text = "1 Thing", cellHeight = 26.dp, fontSize = 13.sp)
                BoardCloseButton(onClick = onDismiss)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // White paint band — the focus reads from across the room
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Board.cell)
                    .background(Board.Paint)
                    .pointerInput(isCompleted) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = { totalDrag = 0f },
                            onDragCancel = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                if (totalDrag > 100f) {
                                    complete()
                                    totalDrag = 0f
                                } else if (totalDrag < -100f) {
                                    reopen()
                                    totalDrag = 0f
                                }
                            }
                        )
                    }
                    .padding(start = 12.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BoardLabel(
                        text = if (isCompleted) "Done today" else "The One Thing",
                        color = Board.Ink.copy(alpha = 0.62f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.mainTask.uppercase(),
                        style = BoardDisplayStyle.copy(fontSize = 18.sp, lineHeight = 21.sp),
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isCompleted) Board.Ink.copy(alpha = 0.6f) else Board.Ink
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 48dp target around the 30dp flap cell
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                            if (isCompleted) reopen() else complete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(Board.cell)
                            .border(2.dp, if (isCompleted) Board.Action else Board.Ink, Board.cell)
                            .background(if (isCompleted) Board.Action else Color.Transparent, Board.cell),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Reopen task",
                                tint = Board.Ink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            BoardLabel(
                text = if (isCompleted) "Tap the check or swipe left to reopen"
                else "Tap the check or swipe right to close",
                color = Board.Steel
            )

            if (subTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardLabel(text = "Steps", color = Board.Steel)
                    Text(
                        text = "%02d/%02d".format(doneCount, subTasks.size),
                        style = BoardMonoStyle.copy(fontSize = 12.sp),
                        color = if (isCompleted) Board.Action else Board.PaintDim
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                BoardDivider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(subTasks) { index, subTask ->
                        val isSubtaskCompleted = subTask.isCompleted
                        val isNext = !isSubtaskCompleted &&
                            subTasks.take(index).all { it.isCompleted }
                        if (index > 0) {
                            BoardDivider(modifier = Modifier.padding(start = 34.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(Board.cell)
                                .clickable { onUpdateSubtask(subTask.id, !isSubtaskCompleted) }
                                .pointerInput(isSubtaskCompleted) {
                                    var totalDrag = 0f
                                    detectHorizontalDragGestures(
                                        onDragEnd = { totalDrag = 0f },
                                        onDragCancel = { totalDrag = 0f },
                                        onHorizontalDrag = { _, dragAmount ->
                                            totalDrag += dragAmount
                                            if (totalDrag > 100f && !isSubtaskCompleted) {
                                                onUpdateSubtask(subTask.id, true)
                                                totalDrag = 0f
                                            } else if (totalDrag < -100f && isSubtaskCompleted) {
                                                onUpdateSubtask(subTask.id, false)
                                                totalDrag = 0f
                                            }
                                        }
                                    )
                                }
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "%02d".format(index + 1),
                                color = if (isNext) Board.Action else Board.PaintDim,
                                style = BoardMonoStyle.copy(fontSize = 12.sp),
                                modifier = Modifier.width(26.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(Board.cell)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSubtaskCompleted) Board.Action else Board.Hair,
                                        shape = Board.cell
                                    )
                                    .background(
                                        if (isSubtaskCompleted) Board.Action else Color.Transparent,
                                        Board.cell
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSubtaskCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Board.Ink,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = subTask.text.uppercase(),
                                style = BoardBodyStyle.copy(
                                    fontSize = 13.sp,
                                    fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Medium,
                                    textDecoration = if (isSubtaskCompleted)
                                        TextDecoration.LineThrough
                                        else TextDecoration.None
                                ),
                                color = when {
                                    isSubtaskCompleted -> Board.PaintDim
                                    isNext -> Board.Paint
                                    else -> Board.Paint.copy(alpha = 0.8f)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = when {
                                    isSubtaskCompleted -> "DONE"
                                    isNext -> "NEXT"
                                    else -> "—"
                                },
                                color = when {
                                    isSubtaskCompleted -> Board.Steel
                                    isNext -> Board.Action
                                    else -> Board.Steel
                                },
                                style = BoardLabelStyle.copy(fontSize = 10.sp, letterSpacing = 1.4.sp),
                                textAlign = TextAlign.End,
                                modifier = Modifier.widthIn(min = 40.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))
            BoardActionButton(
                text = "New task",
                onClick = onNewTask,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
