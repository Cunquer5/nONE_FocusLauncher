package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.data.model.AppModel
import android.app.WallpaperColors // Changed import
import com.example.nonelauncher.ui.theme.OrangePrimary

@Composable
fun ModernIntentionDialog(
    app: AppModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    wallpaperColors: WallpaperColors? = null // Updated parameter type
) {
    var intentionText by remember { mutableStateOf("") }
    
    // Using WallpaperColors.primaryColor and converting to Compose Color
    val primaryColor = wallpaperColors?.primaryColor?.let { androidx.compose.ui.graphics.Color(it.toArgb()) } ?: OrangePrimary
    val surfaceColor = if (wallpaperColors != null) Color.White else Color.White
    val inputColor = if (wallpaperColors != null) Color(0xFFF5F6F8) else Color(0xFFF5F6F8)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(32.dp),
                color = surfaceColor,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon,
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFEFEFEF))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = """Intention for 
opening 
${app.label}?""",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            ),
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = intentionText,
                        onValueChange = { intentionText = it },
                        placeholder = { 
                            Text(
                                "Why are you opening this app?", 
                                color = Color(0xFFAAAAAA),
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputColor,
                            unfocusedContainerColor = inputColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = primaryColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Button(
                            onClick = { 
                                if (intentionText.isNotBlank()) onConfirm(intentionText)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.shadow(4.dp, RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "Proceed →",
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Maybe later",
                    color = Color.White.copy(0.8f),
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(8.dp),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "Set limit",
                    color = Color.White.copy(0.8f),
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}