package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
import com.example.nonelauncher.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

@Composable
fun OneThingTaskDisplay(
    task: OneThingTask,
    onMainTaskCompleted: () -> Unit,
    onUpdateSubtask: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val mainTask = task.mainTask
    val subTasks = task.subTasks
    
    // Use external state instead of local state
    // Calculate if all subtasks are completed
    val allSubtasksCompleted by remember(subTasks) {
        derivedStateOf {
            subTasks.isNotEmpty() && subTasks.all { it.isCompleted }
        }
    }
    
    // Calculate if main task should be considered completed
    val isMainTaskCompleted by remember(subTasks, allSubtasksCompleted) {
        derivedStateOf {
            allSubtasksCompleted || (subTasks.isEmpty() && false) // Main task completed if all subtasks done
        }
    }
    
    LaunchedEffect(isMainTaskCompleted) {
        if (isMainTaskCompleted) {
            delay(300)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .clickable { 
                            // For main task, complete all subtasks
                            if (!allSubtasksCompleted && subTasks.isNotEmpty()) {
                                // Complete all subtasks
                                subTasks.forEach { subTask ->
                                    if (!subTask.isCompleted) {
                                        onUpdateSubtask(subTask.id, true)
                                    }
                                }
                            }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { 
                                onUpdateSubtask(subTask.id, !isSubtaskCompleted)
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