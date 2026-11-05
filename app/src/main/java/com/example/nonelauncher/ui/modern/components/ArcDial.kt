package com.example.nonelauncher.ui.modern.components

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardLabelStyle
import com.example.nonelauncher.ui.theme.BoardMonoFamily
import com.example.nonelauncher.ui.theme.BoardMonoStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** The fixed seats of the board dial. User apps follow them on the same arc. */
enum class DialAction { ONE_THING, STRUCTURE, SEARCH, SETTINGS, PHONE, ADD_APPS }

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
// the outer seats is R(1 - cos θ), so a shallow pitch looks like a straight shelf at any size.
// 30° and a 0.56-of-width radius put the next seat either side off the display: a row is three
// seats, and the dial reads as turning rather than as a strip of six.
private const val DEGREES_PER_ITEM = 30f

// The two cell sizes of a row: the apex is held forward, the seats either side sit back.
private const val SEAT_APEX_DP = 80f
private const val SEAT_SIDE_DP = 68f

/**
 * The home app surface, re-seated on an arc in board material: matte flap cells on a hairline
 * track. One finger turns the wheel with momentum and a snap; whatever sits at the apex is the
 * selection and its name is the only text on the dial. The accent marks that selection once:
 * the apex cell's rim and the index tick beneath it. Idle for three seconds and the dial sinks
 * back into the edge.
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
    // The name tag belongs to the stop, not to the motion: it clears while the wheel turns so
    // labels cannot strobe as they pass the apex, then fades in once the wheel settles.
    var moving by remember { mutableStateOf(false) }
    var idleEpoch by remember { mutableIntStateOf(0) }

    LaunchedEffect(visible) {
        if (visible) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            reveal.animateTo(1f, spring(dampingRatio = 0.82f, stiffness = 650f))
        } else {
            // A cancelled spin leaves this latched: the next open must start at rest.
            moving = false
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
        moving = true
        scope.launch {
            rotation.animateTo(
                targetValue = rotation.value + steps,
                animationSpec = spring(dampingRatio = 0.86f, stiffness = 340f)
            )
            rotation.snapTo(normalize(rotation.value, countF))
            moving = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(DIAL_HEIGHT)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        // Three seats to a row: 0.56 of the width as the radius keeps the cells either side of the
        // apex fully on screen while the next one starts past the edge.
        val pitchRad = Math.toRadians((DEGREES_PER_ITEM * 2f).toDouble()).toFloat()
        val radiusPx = min(
            with(density) { ARC_MAX_RADIUS.toPx() },
            (widthPx * 0.56f) / sin(pitchRad)
        )
        val apexY = heightPx - with(density) { APEX_LIFT.toPx() }
        val cx = widthPx / 2f
        val itemArcPx = radiusPx * (DEGREES_PER_ITEM * PI.toFloat() / 180f)
        val labelLiftPx = with(density) { 56.dp.toPx() }
        val labelAlpha = ((reveal.value - 0.45f) / 0.55f).coerceIn(0f, 1f)
        val labelOpacity by animateFloatAsState(
            targetValue = if (moving) 0f else labelAlpha,
            animationSpec = tween(durationMillis = 120),
            label = "dialLabelOpacity"
        )

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
                    // dot when it is closed.
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
                                moving = true
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
                                    moving = false
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
                                "Board dial. ${entries[selected].label} selected. Previous and next turn the dial; open the centre seat to launch it."
                            customActions = listOf(
                                CustomAccessibilityAction("Previous stop") {
                                    spinBy(-1f)
                                    true
                                },
                                CustomAccessibilityAction("Next stop") {
                                    spinBy(1f)
                                    true
                                },
                                CustomAccessibilityAction("Close dial") {
                                    onRequestHide()
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
                    // The track only bridges the gaps between seats. A continuous arc would run
                    // under every cell and read as a scratch across the glyphs.
                    val apexHalfDeg = seatHalfAngle(SEAT_APEX_DP, radiusPx, density)
                    val sideHalfDeg = seatHalfAngle(SEAT_SIDE_DP, radiusPx, density)
                    val gapSweep = (DEGREES_PER_ITEM - sideHalfDeg) - apexHalfDeg
                    val trackTopLeft = Offset(cx - radiusPx, centreY - radiusPx)
                    val trackSize = Size(radiusPx * 2f, radiusPx * 2f)
                    for (start in floatArrayOf(
                        270f - (DEGREES_PER_ITEM - sideHalfDeg),
                        270f + apexHalfDeg
                    )) {
                        // The hairline floats on the wallpaper, so it carries its own dark bed:
                        // steel alone disappears over a pale photo.
                        drawArc(
                            color = Board.Ink.copy(alpha = 0.42f * fade),
                            startAngle = start,
                            sweepAngle = gapSweep,
                            useCenter = false,
                            topLeft = trackTopLeft,
                            size = trackSize,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                        drawArc(
                            color = Board.Steel.copy(alpha = 0.7f * fade),
                            startAngle = start,
                            sweepAngle = gapSweep,
                            useCenter = false,
                            topLeft = trackTopLeft,
                            size = trackSize,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // The one accent on the surface: the index mark under the selected seat.
                    val indexStart = Offset(cx, centreY - (radiusPx - 46.dp.toPx()))
                    val indexEnd = Offset(cx, centreY - (radiusPx - 58.dp.toPx()))
                    drawLine(
                        color = Board.Ink.copy(alpha = 0.55f * fade),
                        start = indexStart,
                        end = indexEnd,
                        strokeWidth = 7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Board.Action.copy(alpha = 0.95f * fade),
                        start = indexStart,
                        end = indexEnd,
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            entries.forEachIndexed { index, entry ->
                val steps = signedSteps(index, rotation.value, countF)
                val distance = abs(steps)
                // Only the seat either side of the apex is part of the row; the next one fades in
                // from the edge as it turns toward the apex.
                val baseAlpha = if (distance <= 1f) {
                    1f - 0.18f * distance
                } else {
                    0.82f * (2f - distance).coerceIn(0f, 1f)
                }
                val nodeAlpha = (baseAlpha * reveal.value).coerceIn(0f, 1f)
                if (nodeAlpha <= 0.02f) return@forEachIndexed

                val radians = Math.toRadians((steps * DEGREES_PER_ITEM).toDouble())
                val r = radiusPx * reveal.value
                val x = cx + r * sin(radians).toFloat()
                val y = apexY + r * (1f - cos(radians).toFloat())

                val nodeDp = lerpDp(SEAT_APEX_DP, SEAT_SIDE_DP, distance.coerceIn(0f, 1f))
                val nodePx = with(density) { nodeDp.toPx() }
                val seatScale = (0.72f + 0.28f * reveal.value) * (1f - 0.04f * min(distance, 1f))
                val isApex = index == selected

                key(entry.key) {
                    DialSeat(
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

            // The selected seat's name, tagged like a board label above the apex.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset {
                        IntOffset(
                            0,
                            -(with(density) { APEX_LIFT.toPx() } + labelLiftPx).roundToInt()
                        )
                    }
                    .alpha(labelOpacity)
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
                    val entry = entries.getOrNull(index)
                    if (entry != null) {
                        Row(
                            modifier = Modifier
                                .widthIn(max = 240.dp)
                                // The tag floats over wallpaper on tall screens and over the
                                // board on short ones; the shadow separates it from both.
                                .shadow(6.dp, Board.capsule, clip = false)
                                .clip(Board.capsule)
                                .background(Board.Ink.copy(alpha = 0.94f))
                                .border(1.dp, Board.Hair, Board.capsule)
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "%02d".format(index + 1),
                                color = Board.Steel,
                                style = BoardMonoStyle.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = entry.label.uppercase(),
                                color = Board.Paint,
                                style = BoardLabelStyle.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    letterSpacing = 1.4.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialSeat(
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
    val appIconSize = size * 0.64f
    val pressScale = if (pressed) 0.94f else 1f

    val fill by animateColorAsState(
        targetValue = when {
            pressed -> Board.Well
            isApex -> Board.Flap
            else -> Board.Ink
        },
        label = "seatFill"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            // A soft flap seat dropped on the wallpaper: one rounded cell, one quiet shadow.
            .shadow(
                elevation = 8.dp,
                shape = Board.seat,
                clip = false,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(Board.seat)
            .background(fill)
            .border(
                width = 1.dp,
                color = if (isApex) Board.Action else Board.Hair,
                shape = Board.seat
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
            // The icon sits directly on the seat: the old inner well added a second outline
            // around an icon that already carries its own shape.
            is DialEntry.App -> Image(
                bitmap = entry.app.icon,
                contentDescription = null,
                // Fit, not FillBounds: a non-square icon is letterboxed on the seat
                // instead of being stretched.
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(appIconSize)
                    // Legacy square icons soften to the same cell radius as the seat.
                    .clip(Board.cell)
            )

            is DialEntry.Action -> when (entry.action) {
                DialAction.ONE_THING -> Text(
                    text = "1",
                    // Orange marks the selection; a side seat wearing it would outrank the apex.
                    color = if (isApex) Board.Action else Board.Paint,
                    fontFamily = BoardMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = with(density) { (size.toPx() * 0.44f).toSp() },
                    lineHeight = with(density) { (size.toPx() * 0.5f).toSp() }
                )

                DialAction.STRUCTURE -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Board.Paint,
                    modifier = Modifier.size(size * 0.38f)
                )

                DialAction.SEARCH -> Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Board.Paint,
                    modifier = Modifier.size(size * 0.38f)
                )

                DialAction.SETTINGS -> Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Board.Paint,
                    modifier = Modifier.size(size * 0.38f)
                )

                DialAction.PHONE -> Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Board.Paint,
                    modifier = Modifier.size(size * 0.38f)
                )

                DialAction.ADD_APPS -> Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    // Same weight as every other seat: the plus is a destination, not a
                    // disabled control.
                    tint = Board.Paint,
                    modifier = Modifier.size(size * 0.4f)
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

/** Half the angle a seat of [seatDp] covers where its centre sits on the arc. */
private fun seatHalfAngle(seatDp: Float, radiusPx: Float, density: Density): Float {
    val ratio = (with(density) { seatDp.dp.toPx() } / 2f / radiusPx).coerceIn(0f, 1f)
    return Math.toDegrees(asin(ratio.toDouble())).toFloat()
}
