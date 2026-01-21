package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.OrangePrimaryDark
import com.example.nonelauncher.ui.theme.GlassMorphism.cardGlassBackground
import com.example.nonelauncher.ui.theme.BlackGlass

@Composable
fun OneThingInputCard(
    onTaskSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mainTask by remember { mutableStateOf("") }
    var actionStep by remember { mutableStateOf("") }

    // Glass/Translucent Card Overlay - Black glass morphism effect
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(BlackGlass.copy(alpha = 0.6f))
            .padding(24.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Text(
                "ADD 1 THING",
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
                    focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = OrangePrimary
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
                placeholder = { Text("Articulate action steps...", color = Color.White.copy(0.6f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.4f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = OrangePrimary
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
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Set Task", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
