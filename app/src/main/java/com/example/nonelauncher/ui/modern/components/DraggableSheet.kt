package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * A sheet welded to the bottom edge, with a grabber that pulls it down.
 *
 * The detector lives on the strip, not on the card: the sheets scroll, and a vertical drag on
 * their content has to keep belonging to the content. A drag past the threshold animates the
 * sheet the rest of the way off and dismisses; anything shorter springs back.
 */
@Composable
fun DraggableSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var dragY by remember { mutableFloatStateOf(0f) }
    var sheetHeight by remember { mutableFloatStateOf(0f) }
    var settling by remember { mutableStateOf(false) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val threshold = with(density) { 120.dp.toPx() }

    fun settleBack() {
        settleJob?.cancel()
        settleJob = scope.launch {
            animate(
                initialValue = dragY,
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f),
            ) { value, _ -> dragY = max(0f, value) }
        }
    }

    fun dismiss() {
        if (settling) return
        settling = true
        settleJob?.cancel()
        scope.launch {
            animate(
                initialValue = dragY,
                targetValue = if (sheetHeight > 0f) sheetHeight else 800f,
                animationSpec = tween(180, easing = FastOutLinearInEasing),
            ) { value, _ -> dragY = value }
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { sheetHeight = it.height.toFloat() }
            .graphicsLayer { translationY = dragY },
    ) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(32.dp)
                .semantics {
                    contentDescription = "Drag down to close"
                    customActions = listOf(
                        CustomAccessibilityAction("Close") {
                            onDismiss()
                            true
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { settleJob?.cancel() },
                        onDragCancel = { settleBack() },
                        // Upward lifts nothing: the sheet is already at its height.
                        onVerticalDrag = { _, dragAmount ->
                            dragY = max(0f, dragY + dragAmount)
                        },
                        onDragEnd = {
                            if (dragY > max(threshold, sheetHeight * 0.18f)) dismiss()
                            else settleBack()
                        },
                    )
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (dragY > 0f) 0.72f else 0.42f))
            )
        }
    }
}
