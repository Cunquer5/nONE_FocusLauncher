package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
import com.example.nonelauncher.data.model.serializeOneThingTask
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.apiAwareBlur
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import com.example.nonelauncher.ui.components.GlassMorphismButton
import com.example.nonelauncher.ui.components.GradientGlassMorphismButton

@Composable
fun OneThingInputCard(
    onTaskSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mainTask by remember { mutableStateOf("") }
    var actionStep by remember { mutableStateOf("") }
    val hazeState = HazeGlassMorphism.rememberHazeState()

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Background with blur effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                //.apiAwareBlur(15.dp, fallbackAlpha = 0.3f)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(24.dp)
                )
        )
        
        // Content layer (stays sharp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Simplified header
            Text(
                "ADD 1 THING",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Single input field for main task
            TextField(
                value = mainTask,
                onValueChange = { mainTask = it },
                placeholder = { 
                    Text("What's your main focus today?", color = Color.White.copy(0.5f)) 
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = OrangePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Optional action step
            TextField(
                value = actionStep,
                onValueChange = { actionStep = it },
                placeholder = {
                    Text("Optional: Specific action step...", color = Color.White.copy(0.5f))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = OrangePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Button dims until a main task is typed; guard still blocks submit
            GradientGlassMorphismButton(
                onClick = {
                    if (mainTask.isNotBlank()) {
                        // Create OneThingTask with main task and subtasks
                        val subTasks = actionStep.split("\n").filter { it.isNotBlank() }
                            .map { SubTask(text = it, isCompleted = false) }
                        val oneThingTask = OneThingTask(mainTask, subTasks)
                        onTaskSet(serializeOneThingTask(oneThingTask))
                        mainTask = ""
                        actionStep = ""
                    }
                },
                text = "Set Focus Task",
                hazeState = hazeState,
                glassAlpha = 1.0f,  // fully opaque glass
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .graphicsLayer {
                        alpha = if (mainTask.isBlank()) 0.35f else 1f
                    }

            )
        }
    }
}