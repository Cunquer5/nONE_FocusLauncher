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
import com.example.nonelauncher.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

@Composable
fun OneThingTaskDisplay(
    task: String,
    onMainTaskCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks = task.split("\n").filter { it.isNotBlank() }
    val mainTask = tasks.firstOrNull() ?: ""
    val subTasks = tasks.drop(1)
    
    var isMainTaskCompleted by remember { mutableStateOf(false) }
    var completedSubtasks by remember { mutableStateOf(setOf<Int>()) }
    
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
                        .clickable { isMainTaskCompleted = !isMainTaskCompleted }
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
                val isSubtaskCompleted = completedSubtasks.contains(index)
                
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
                                completedSubtasks = if (isSubtaskCompleted) {
                                    completedSubtasks - index
                                } else {
                                    completedSubtasks + index
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSubtaskCompleted)
                                        OrangePrimary
                                    else
                                        Color.White.copy(alpha = 0.3f)
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
                            text = subTask,
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