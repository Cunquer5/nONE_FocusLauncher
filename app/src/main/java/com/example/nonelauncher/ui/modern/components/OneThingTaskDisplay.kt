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
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay

@Composable
fun OneThingTaskDisplay(
    task: OneThingTask,
    onMainTaskCompleted: () -> Unit,
    onUpdateSubtask: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val hazeState = HazeGlassMorphism.rememberHazeState()
    val mainTask = task.mainTask
    val subTasks = task.subTasks
    
    // Local state to track direct main task completion (when no subtasks)
    var isMainTaskDirectlyCompleted by remember { mutableStateOf(false) }
    
    // Use external state instead of local state
    // Calculate if all subtasks are completed
    val allSubtasksCompleted by remember(subTasks) {
        derivedStateOf {
            subTasks.isNotEmpty() && subTasks.all { it.isCompleted }
        }
    }
    
    // Calculate if main task should be considered completed
    val isMainTaskCompleted by remember(subTasks, allSubtasksCompleted, isMainTaskDirectlyCompleted) {
        derivedStateOf {
            allSubtasksCompleted || (subTasks.isEmpty() && isMainTaskDirectlyCompleted)
        }
    }
    
    LaunchedEffect(isMainTaskCompleted) {
        if (isMainTaskCompleted) {
            delay(150)
            onMainTaskCompleted()
        }
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main task
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .hazeChild(state = hazeState, blurRadius = 12.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .clickable { 
                            // Allow main task to be checked even without subtasks
                            // For main task, complete all subtasks OR mark as completed directly if no subtasks
                            if (!isMainTaskCompleted) {
                                if (subTasks.isNotEmpty()) {
                                    // Complete all subtasks
                                    subTasks.forEach { subTask ->
                                        if (!subTask.isCompleted) {
                                            onUpdateSubtask(subTask.id, true)
                                        }
                                    }
                                } else {
                                    // No subtasks - mark as directly completed and trigger completion
                                    isMainTaskDirectlyCompleted = true
                                    onMainTaskCompleted()
                                }
                            }
                        }
                        .pointerInput(isMainTaskCompleted) {
                            var totalDrag = 0f
                            detectHorizontalDragGestures(
                                onDragEnd = { totalDrag = 0f },
                                onDragCancel = { totalDrag = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    totalDrag += dragAmount
                                    if (totalDrag > 100f && !isMainTaskCompleted) {
                                        if (subTasks.isNotEmpty()) {
                                            // Complete all subtasks
                                            subTasks.forEach { subTask ->
                                                if (!subTask.isCompleted) {
                                                    onUpdateSubtask(subTask.id, true)
                                                }
                                            }
                                        } else {
                                            // No subtasks - mark as directly completed and trigger completion
                                            isMainTaskDirectlyCompleted = true
                                            onMainTaskCompleted()
                                        }
                                        totalDrag = 0f
                                    }
                                }
                            )
                        }
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
                        style = MaterialTheme.typography.headlineMedium.copy(
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
            }
        }

        // Subtasks
        if (subTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Steps:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            itemsIndexed(subTasks) { index, subTask ->
                val isSubtaskCompleted = subTask.isCompleted
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .hazeChild(state = hazeState, blurRadius = 12.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { 
                                val newState = !isSubtaskCompleted
                                onUpdateSubtask(subTask.id, newState)
                            }
                            .pointerInput(isSubtaskCompleted) {
                                var totalDrag = 0f
                                detectHorizontalDragGestures(
                                    onDragEnd = { totalDrag = 0f },
                                    onDragCancel = { totalDrag = 0f },
                                    onHorizontalDrag = { change, dragAmount ->
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

                        Spacer(modifier = Modifier.width(16.dp))

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