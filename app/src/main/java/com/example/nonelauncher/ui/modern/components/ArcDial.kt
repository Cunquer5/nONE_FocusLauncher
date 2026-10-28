package com.example.nonelauncher.ui.modern.components

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.SquircleShapes
import com.example.nonelauncher.ui.theme.liquidGlass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** The five fixed seats of the dial. User apps follow them on the same arc. */
enum class DialAction { ONE_THING, STRUCTURE, SEARCH, SETTINGS, ADD_APPS }

sealed interface DialEntry {
    val key: String
    val label: String

    data class Action(val action: DialAction, override val label: String) : DialEntry {
        override val key: String get() = "action:${action.name}"
    }

    data class App(val app: AppModel) : DialEntry {
        override val key: String get() = "app:${app.packageName}"
        override val label: String get() = app.label
    }
}

private val DIAL_HEIGHT = 250.dp
private val ARC_MAX_RADIUS = 320.dp
private val APEX_LIFT = 160.dp

// Angular pitch, not radius, is what makes an arc read as an arc: the sag between the apex and
// the outer seats is R(1 - cos 2θ), so a shallow pitch looks like a straight shelf at any size.
private const val DEGREES_PER_ITEM = 20f
private const val ARC_HALF_SPAN_DEG = 50f

/**
 * The home app surface: glass seats on a hairline arc above the bottom edge.
 *
 * One finger spins the wheel with momentum and a snap; whatever sits at the apex is the selection and
 * its name is the only text on screen. The accent appears once — the index mark under the apex seat.
 * Idle for three seconds and the wheel sinks back into the edge.
 */
@Composable
fun ArcDial(
    visible: Boolean,
    entries: List<DialEntry>,
    onAction: (DialAction) -> Unit,
    onApp: (AppModel) -> Unit,
    onRequestHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val count = entries.size
    val countF = count.toFloat()
    val reveal = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    // A finger resting on a seat is neither a drag nor a tap; it must still hold the dial open.
    var holding by remember { mutableStateOf(false) }
    var idleEpoch by remember { mutableIntStateOf(0) }

    LaunchedEffect(visible) {
        if (visible) {
            reveal.animateTo(1f, spring(dampingRatio = 0.82f, stiffness = 650f))
        } else {
            reveal.animateTo(0f, spring(dampingRatio = 1f, stiffness = 1000f))
        }
    }

    // Three seconds is the product rule; TalkBack navigation moves at its own pace and would be
    // erased mid-read, so the window stretches when touch exploration is on. Both are re-read on
    // arming so a TalkBack toggle mid-session takes effect.
    val accessibility = remember(context) {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    }
    LaunchedEffect(visible, dragging, holding, idleEpoch) {
        if (visible && !dragging && !holding) {
            val idleMs = if (accessibility?.isTouchExplorationEnabled == true) 15_000L else 3_000L
            delay(idleMs)
            onRequestHide()
        }
    }

    val selected = ((rotation.value.roundToInt() % count) + count) % count
    var lastTick by remember { mutableIntStateOf(selected) }
    LaunchedEffect(selected, visible) {
        if (visible && reveal.value > 0.9f && selected != lastTick) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        lastTick = selected
    }

    val spinBy: (Float) -> Unit = { steps ->
        idleEpoch++
        scope.launch {
            rotation.animateTo(
                targetValue = rotation.value + steps,
                animationSpec = spring(dampingRatio = 0.86f, stiffness = 340f)
            )
            rotation.snapTo(normalize(rotation.value, countF))
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(DIAL_HEIGHT)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        // Fit two seats either side of the apex inside the screen: 0.44 of the width is the
        // outer seat's centre, which leaves its whole disc on screen at every density.
        val pitchRad = Math.toRadians((DEGREES_PER_ITEM * 2f).toDouble()).toFloat()
        val radiusPx = min(
            with(density) { ARC_MAX_RADIUS.toPx() },
            (widthPx * 0.44f) / sin(pitchRad)
        )
        val apexY = heightPx - with(density) { APEX_LIFT.toPx() }
        val cx = widthPx / 2f
        val itemArcPx = radiusPx * (DEGREES_PER_ITEM * PI.toFloat() / 180f)
        val labelLiftPx = with(density) { 40.dp.toPx() }
        val labelAlpha = ((reveal.value - 0.45f) / 0.55f).coerceIn(0f, 1f)

        val dragState = rememberDraggableState { delta ->
            scope.launch { rotation.snapTo(rotation.value - delta / itemArcPx) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .then(
                    // Only a live dial may claim this band: a pointer-input node is a hit-test
                    // target even when it consumes nothing, and this box covers the collapsed
                    // dot and the 1 Thing island when it is closed.
                    if (visible) {
                        // Observes without consuming, on the pass that runs before the seats'
                        // clickables: any finger down suspends the idle retract.
                        Modifier.pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitPointerEvent(PointerEventPass.Initial)
                                        .changes.any { it.pressed }
                                    if (down != holding) holding = down
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (visible) {
                        Modifier.draggable(
                            state = dragState,
                            orientation = Orientation.Horizontal,
                            onDragStarted = {
                                dragging = true
                                idleEpoch++
                            },
                            onDragStopped = { velocity ->
                                dragging = false
                                idleEpoch++
                                // Launched, not awaited: the drag's last snapTo is already queued on the
                                // same scope and would otherwise cancel this settle animation.
                                val target = (rotation.value - (velocity / itemArcPx) * 0.16f)
                                    .roundToInt().toFloat()
                                scope.launch {
                                    rotation.animateTo(
                                        targetValue = target,
                                        animationSpec = spring(dampingRatio = 0.84f, stiffness = 330f)
                                    )
                                    rotation.snapTo(normalize(rotation.value, countF))
                                }
                            }
                        )
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (visible) {
                        Modifier.semantics {
                            contentDescription =
                                "App dial. ${entries[selected].label} selected. Swipe to spin, tap the centre item to open."
                            customActions = listOf(
                                CustomAccessibilityAction("Previous item") {
                                    spinBy(-1f)
                                    true
                                },
                                CustomAccessibilityAction("Next item") {
                                    spinBy(1f)
                                    true
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            if (reveal.value > 0.01f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val fade = reveal.value.toFloat()
                    // The wheel's centre sits a radius below the apex, off the bottom of the box.
                    val centreY = apexY + radiusPx
                    drawArc(
                        color = Color.White.copy(alpha = 0.14f * fade),
                        startAngle = 270f - ARC_HALF_SPAN_DEG,
                        sweepAngle = ARC_HALF_SPAN_DEG * 2f,
                        useCenter = false,
                        topLeft = Offset(cx - radiusPx, centreY - radiusPx),
                        size = Size(radiusPx * 2f, radiusPx * 2f),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Scale ticks sit in the gaps between seats, never under them, so the track
                    // does not read as a scratch across the icons.
                    val inner = radiusPx - 2.dp.toPx()
                    val outer = radiusPx - 11.dp.toPx()
                    for (half in -3..2) {
                        val radians = Math.toRadians(((half + 0.5f) * DEGREES_PER_ITEM).toDouble())
                        drawLine(
                            color = Color.White.copy(alpha = 0.2f * fade),
                            start = Offset(
                                cx + inner * sin(radians).toFloat(),
                                centreY - inner * cos(radians).toFloat()
                            ),
                            end = Offset(
                                cx + outer * sin(radians).toFloat(),
                                centreY - outer * cos(radians).toFloat()
                            ),
                            strokeWidth = 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // The one accent on the surface: the index mark under the selected seat.
                    drawLine(
                        color = OrangePrimary.copy(alpha = 0.92f * fade),
                        start = Offset(cx, centreY - (radiusPx - 38.dp.toPx())),
                        end = Offset(cx, centreY - (radiusPx - 50.dp.toPx())),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            entries.forEachIndexed { index, entry ->
                val steps = signedSteps(index, rotation.value, countF)
                val distance = abs(steps)
                val baseAlpha = if (distance <= 2f) {
                    1f - 0.22f * distance
                } else {
                    ((2.6f - distance) / 0.6f) * 0.56f
                }
                val nodeAlpha = (baseAlpha * reveal.value).coerceIn(0f, 1f)
                if (nodeAlpha <= 0.02f) return@forEachIndexed

                val radians = Math.toRadians((steps * DEGREES_PER_ITEM).toDouble())
                val r = radiusPx * reveal.value
                val x = cx + r * sin(radians).toFloat()
                val y = apexY + r * (1f - cos(radians).toFloat())

                val nodeDp = lerpDp(64f, 46f, (distance / 2f).coerceIn(0f, 1f))
                val nodePx = with(density) { nodeDp.toPx() }
                val seatScale = (0.72f + 0.28f * reveal.value) * (1f - 0.04f * min(distance, 1f))
                val isApex = index == selected

                key(entry.key) {
                    DialNode(
                        entry = entry,
                        size = nodeDp,
                        isApex = isApex,
                        contentDescription = if (isApex) {
                            "Open ${entry.label}"
                        } else {
                            "Select ${entry.label}"
                        },
                        onTap = {
                            if (visible) {
                                idleEpoch++
                                haptic.performHapticFeedback(
                                    if (isApex) HapticFeedbackType.LongPress
                                    else HapticFeedbackType.TextHandleMove
                                )
                                if (isApex) {
                                    when (entry) {
                                        is DialEntry.Action -> onAction(entry.action)
                                        is DialEntry.App -> onApp(entry.app)
                                    }
                                } else {
                                    spinBy(steps)
                                }
                            }
                        },
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (x - nodePx / 2f).roundToInt(),
                                    (y - nodePx / 2f).roundToInt()
                                )
                            }
                            .zIndex(10f - distance)
                            .alpha(nodeAlpha)
                            .graphicsLayer {
                                scaleX = seatScale
                                scaleY = seatScale
                            }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset {
                        IntOffset(
                            0,
                            -(with(density) { APEX_LIFT.toPx() } + labelLiftPx).roundToInt()
                        )
                    }
                    .alpha(labelAlpha)
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = selected,
                    animationSpec = tween(durationMillis = 140),
                    label = "dialLabel"
                ) { index ->
                    // The outgoing seat is composed for one more crossfade frame; entries can
                    // shrink under it when a pinned app is uninstalled or unpinned.
                    val label = entries.getOrNull(index)?.label
                    if (label != null) {
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp,
                            // The label floats on the wallpaper, not on glass; a soft shadow
                            // holds it against a bright photo.
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 10f
                                )
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 220.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialNode(
    entry: DialEntry,
    size: Dp,
    isApex: Boolean,
    contentDescription: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val density = LocalDensity.current
    val appIconSize = size * 0.66f
    val pressScale = if (pressed) 0.93f else 1f

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            // Dense, non-sampling glass. The wheel redraws every dragged frame, and a blurred
            // child per seat both costs a backdrop pass per disc and leaves its layer behind on
            // some GPUs when the dial retracts. The tint still carries the wallpaper's colour.
            .liquidGlass(
                hazeState = null,
                shape = CircleShape,
                style = if (isApex) LiquidGlassStyle.SeatApex else LiquidGlassStyle.Seat,
                sampling = false,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onTap
            )
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Button
                onClick(label = if (isApex) "Open" else "Select") {
                    onTap()
                    true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (entry) {
            is DialEntry.App -> Image(
                bitmap = entry.app.icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(appIconSize)
                    .clip(SquircleShapes.squircle(appIconSize))
            )

            is DialEntry.Action -> when (entry.action) {
                DialAction.ONE_THING -> Text(
                    text = "1",
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = with(density) { (size.toPx() * 0.5f).toSp() },
                    lineHeight = with(density) { (size.toPx() * 0.56f).toSp() }
                )

                DialAction.STRUCTURE -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.42f)
                )

                DialAction.SEARCH -> Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.42f)
                )

                DialAction.SETTINGS -> Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.42f)
                )

                DialAction.ADD_APPS -> Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(size * 0.44f)
                )
            }
        }
    }
}

/** Signed shortest number of seats between an entry and the apex, in (-count/2, count/2]. */
private fun signedSteps(index: Int, rotation: Float, count: Float): Float {
    var delta = (index - rotation) % count
    if (delta < 0f) delta += count
    if (delta > count / 2f) delta -= count
    return delta
}

private fun normalize(value: Float, count: Float): Float {
    var wrapped = value % count
    if (wrapped < 0f) wrapped += count
    return wrapped
}

private fun lerpDp(start: Float, end: Float, fraction: Float): Dp =
    (start + (end - start) * fraction).dp
