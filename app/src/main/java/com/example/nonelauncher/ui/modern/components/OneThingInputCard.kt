package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.OrangePrimary

@Composable
fun OneThingInputCard(
    onTaskSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mainTask by remember { mutableStateOf("") }
    var actionStep by remember { mutableStateOf("") }
    
    // Custom colors from image
    val inputFieldColor = Color(0xFFD6694D) // Darker orange/red for input bg
    val buttonTextColor = Color(0xFFE05A34) // Deep orange

    // Glass/Translucent Card Overlay
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.2f)) // Slightly more visible glass
            .padding(24.dp)
    ) {
        Column {
            Text(
                "NEW 1 THING", 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            TextField(
                value = mainTask,
                onValueChange = { mainTask = it },
                placeholder = { Text("What's the main task?", color = Color.White.copy(0.6f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputFieldColor,
                    unfocusedContainerColor = inputFieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "SPECIFIC STEP", 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            TextField(
                value = actionStep,
                onValueChange = { actionStep = it },
                placeholder = { Text("One articulate action step...", color = Color.White.copy(0.6f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputFieldColor,
                    unfocusedContainerColor = inputFieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (mainTask.isNotBlank()) {
                        onTaskSet(if (actionStep.isNotBlank()) "$mainTask\n$actionStep" else mainTask)
                        // Clear previous input text
                        mainTask = ""
                        actionStep = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Set Task", color = buttonTextColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
