package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
import com.example.nonelauncher.data.model.serializeOneThingTask
import com.example.nonelauncher.ui.components.GradientGlassMorphismButton
import com.example.nonelauncher.ui.theme.LiquidGlassPanel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.OrangePrimary

@Composable
fun OneThingInputCard(
    onTaskSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mainTask by remember { mutableStateOf("") }
    var actionStep by remember { mutableStateOf("") }

    LiquidGlassPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        style = LiquidGlassStyle.Regular.copy(
            blurRadius = 20.dp,
            tint = Color(0xFF0A0A0B).copy(alpha = 0.42f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 32.dp, end = 20.dp, bottom = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "ADD 1 THING",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassTaskField(
                value = mainTask,
                onValueChange = { mainTask = it },
                placeholder = "What's your main focus today?"
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassTaskField(
                value = actionStep,
                onValueChange = { actionStep = it },
                placeholder = "Optional: Specific action step..."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Button dims until a main task is typed; guard still blocks submit
            GradientGlassMorphismButton(
                onClick = {
                    if (mainTask.isNotBlank()) {
                        val subTasks = actionStep.split("\n").filter { it.isNotBlank() }
                            .map { SubTask(text = it, isCompleted = false) }
                        val oneThingTask = OneThingTask(mainTask, subTasks)
                        onTaskSet(serializeOneThingTask(oneThingTask))
                        mainTask = ""
                        actionStep = ""
                    }
                },
                text = "Set Focus Task",
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

/**
 * Container-less input for glass surfaces: BasicTextField draws text, cursor and selection only.
 * Material's TextField paints its own container, which showed as a hard-edged rectangle over
 * the card's blurred backdrop.
 */
@Composable
private fun GlassTaskField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodyLarge
    Box(modifier = modifier.fillMaxWidth().heightIn(min = 32.dp)) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = style,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = style.copy(color = Color.White),
            cursorBrush = SolidColor(OrangePrimary),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
