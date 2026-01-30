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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.apiAwareBlur

@Composable
fun OneThingInputCard(
    onTaskSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mainTask by remember { mutableStateOf("") }
    var actionStep by remember { mutableStateOf("") }

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
                .apiAwareBlur(15.dp, fallbackAlpha = 0.7f)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
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

            // Simplified button
            Button(
                onClick = {
                    if (mainTask.isNotBlank()) {
                        onTaskSet(if (actionStep.isNotBlank()) "$mainTask\n$actionStep" else mainTask)
                        mainTask = ""
                        actionStep = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    "Set Focus Task", 
                    color = Color.White, 
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}