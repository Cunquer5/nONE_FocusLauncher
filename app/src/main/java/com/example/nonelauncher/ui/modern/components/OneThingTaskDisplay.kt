package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.ui.components.GlassContainer
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.theme.OrangePrimary
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.delay

/**
 * 1 Thing display — one glass sheet, same family as the structure editor.
 * Rows ride the single surface bare (nested cards inside a card read as noise);
 * tap or swipe right completes, swipe left on a done step re-opens it.
 */
@Composable
fun OneThingTaskDisplay(
    task: OneThingTask,
    onMainTaskCompleted: () -> Unit,
    onDismiss: () -> Unit = {},
    onUpdateSubtask: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val hazeState = HazeGlassMorphism.rememberHazeState()
    val mainTask = task.mainTask
    val subTasks = task.subTasks

    // Local state to track direct main task completion (when no subtasks)
    var isMainTaskDirectlyCompleted by remember { mutableStateOf(false) }

    val allSubtasksCompleted by remember(subTasks) {
        derivedStateOf {
            subTasks.isNotEmpty() && subTasks.all { it.isCompleted }
        }
    }

    val isMainTaskCompleted by remember(subTasks, allSubtasksCompleted, isMainTaskDirectlyCompleted) {
        derivedStateOf {
            allSubtasksCompleted || (subTasks.isEmpty() && isMainTaskDirectlyCompleted)
        }
    }

    // Opening a sheet that already shows a completed task is not a new completion. Only a
    // false -> true transition made while the sheet is open reports upward and arms the reset,
    // so re-opening yesterday's finished task does not delete it a few seconds later.
    var hasReportedCompletion by remember { mutableStateOf(isMainTaskCompleted) }
    LaunchedEffect(isMainTaskCompleted) {
        if (isMainTaskCompleted) {
            if (!hasReportedCompletion) {
                hasReportedCompletion = true
                delay(150)
                onMainTaskCompleted()
            }
        } else {
            hasReportedCompletion = false
        }
    }

    val completeMainTask: () -> Unit = {
        if (!isMainTaskCompleted) {
            if (subTasks.isNotEmpty()) {
                subTasks.forEach { subTask ->
                    if (!subTask.isCompleted) onUpdateSubtask(subTask.id, true)
                }
            } else {
                // No subtasks — LaunchedEffect on isMainTaskCompleted fires the single completion callback
                isMainTaskDirectlyCompleted = true
            }
        }
    }

    val doneCount = subTasks.count { it.isCompleted }

    GlassContainer(
        modifier = modifier,
        cornerRadius = 32.dp,
        blurRadius = 22.dp,
        tintAlpha = 0.55f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1 Thing",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .hazeChild(state = hazeState, blurRadius = 8.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.335f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main task — the sheet's one focal line; tap or swipe right completes it
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { completeMainTask() }
                    .pointerInput(isMainTaskCompleted) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = { totalDrag = 0f },
                            onDragCancel = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                if (totalDrag > 100f) {
                                    completeMainTask()
                                    totalDrag = 0f
                                }
                            }
                        )
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(
                            width = 3.dp,
                            color = if (isMainTaskCompleted) OrangePrimary else Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isMainTaskCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = mainTask,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isMainTaskCompleted)
                            TextDecoration.LineThrough
                            else TextDecoration.None
                    ),
                    color = if (isMainTaskCompleted)
                        Color.White.copy(alpha = 0.5f)
                        else Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            if (subTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.titleSmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "$doneCount/${subTasks.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(subTasks) { index, subTask ->
                        val isSubtaskCompleted = subTask.isCompleted
                        if (index > 0) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.06f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
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
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSubtaskCompleted)
                                            OrangePrimary
                                        else
                                            Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(
                                        if (isSubtaskCompleted) OrangePrimary else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSubtaskCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = subTask.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = if (isSubtaskCompleted)
                                        TextDecoration.LineThrough
                                        else TextDecoration.None
                                ),
                                color = if (isSubtaskCompleted)
                                    Color.White.copy(alpha = 0.5f)
                                    else Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
