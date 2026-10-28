package com.example.nonelauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

/**
 * The one Haze source for the window, provided above every surface. Glass children sample the
 * wallpaper layer through it; a surface that carries its own private HazeState has no backdrop
 * and degrades to a plain tint.
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/**
 * Liquid glass: blurred ground, a low dark tint, a light sheen from above and a specular rim that
 * fades toward the lower-right. Values are tuned for white type over a user's own wallpaper.
 */
@Immutable
data class LiquidGlassStyle(
    val blurRadius: Dp = 24.dp,
    val tint: Color = Ink.copy(alpha = 0.40f),
    val rimAlpha: Float = 0.46f,
    val sheenAlpha: Float = 0.07f,
    val noise: Float = 0.02f,
    val shadowElevation: Dp = 0.dp,
) {
    fun withTintAlpha(alpha: Float) = copy(tint = tint.copy(alpha = alpha))

    companion object {
        private val Ink = Color(0xFF0A0A0B)

        /** Chips and the 1 Thing island: small, close to the eye. */
        val Thin = LiquidGlassStyle(
            blurRadius = 16.dp,
            tint = Ink.copy(alpha = 0.30f),
            rimAlpha = 0.38f,
            sheenAlpha = 0.05f,
            noise = 0.015f,
            shadowElevation = 8.dp,
        )

        /** Sheets, the search card, dialog panels. */
        val Regular = LiquidGlassStyle(
            blurRadius = 24.dp,
            tint = Ink.copy(alpha = 0.40f),
            rimAlpha = 0.46f,
            sheenAlpha = 0.07f,
            noise = 0.02f,
            shadowElevation = 16.dp,
        )

        /** The tallest surface: search over the whole height. */
        val Heavy = LiquidGlassStyle(
            blurRadius = 30.dp,
            tint = Ink.copy(alpha = 0.48f),
            rimAlpha = 0.50f,
            sheenAlpha = 0.07f,
            noise = 0.025f,
            shadowElevation = 24.dp,
        )

        /** A small control resting on an existing glass surface; no second backdrop pass. */
        val Control = LiquidGlassStyle(
            blurRadius = 0.dp,
            tint = Ink.copy(alpha = 0.46f),
            rimAlpha = 0.52f,
            sheenAlpha = 0.12f,
            noise = 0f,
            shadowElevation = 0.dp,
        )

        /**
         * A seat on the dial. Denser than a panel: white icons and glyphs have to stay legible
         * while a bright wallpaper passes underneath, and the disc is only 46–64dp wide.
         */
        val Seat = LiquidGlassStyle(
            blurRadius = 12.dp,
            tint = Ink.copy(alpha = 0.52f),
            rimAlpha = 0.38f,
            sheenAlpha = 0.10f,
            noise = 0f,
            shadowElevation = 0.dp,
        )

        /** The seat at the apex: held forward, so its rim and density step up. */
        val SeatApex = Seat.copy(
            tint = Ink.copy(alpha = 0.64f),
            rimAlpha = 0.54f,
        )
    }
}

/**
 * Paints a glass surface. Pass `hazeState` (normally `LocalHazeState.current`) to sample the
 * wallpaper; pass null and `sampling = false` for surfaces that cannot reach that source.
 */
fun Modifier.liquidGlass(
    hazeState: HazeState?,
    shape: Shape,
    style: LiquidGlassStyle = LiquidGlassStyle.Regular,
    sampling: Boolean = true,
): Modifier {
    val source = hazeState
    val canSample = sampling && source != null
    // Below Android 12 Haze has no blur to offer; deepen the tint so type keeps its contrast.
    val tint = if (canSample && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        style.tint.copy(alpha = (style.tint.alpha + 0.12f).coerceAtMost(0.9f))
    } else {
        style.tint
    }
    val material = if (canSample) {
        Modifier.hazeChild(
            state = source,
            shape = shape,
            style = HazeStyle(
                tint = tint,
                blurRadius = style.blurRadius,
                noiseFactor = style.noise,
            )
        )
    } else {
        Modifier.background(tint)
    }
    val rim = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = style.rimAlpha),
            Color.White.copy(alpha = style.rimAlpha * 0.38f),
            Color.White.copy(alpha = style.rimAlpha * 0.10f),
        )
    )

    return this
        .then(
            if (style.shadowElevation > 0.dp) {
                Modifier.shadow(
                    elevation = style.shadowElevation,
                    shape = shape,
                    clip = false,
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                )
            } else {
                Modifier
            }
        )
        .clip(shape)
        .then(material)
        .drawBehind {
            if (style.sheenAlpha > 0f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = style.sheenAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, -size.height * 0.12f),
                        radius = size.maxDimension * 0.95f
                    )
                )
            }
        }
        .border(width = 1.dp, brush = rim, shape = shape)
}

@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    style: LiquidGlassStyle = LiquidGlassStyle.Regular,
    hazeState: HazeState? = LocalHazeState.current,
    sampling: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.liquidGlass(hazeState, shape, style, sampling),
        content = content
    )
}

/**
 * The circular close/clear control used on every sheet: dense glass over the panel's glass,
 * light enough not to need a second backdrop pass.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .liquidGlass(
                hazeState = null,
                shape = CircleShape,
                style = LiquidGlassStyle.Control,
                sampling = false,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .glassPress(interaction, CircleShape),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}
