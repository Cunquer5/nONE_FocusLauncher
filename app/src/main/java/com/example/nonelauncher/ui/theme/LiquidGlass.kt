package com.example.nonelauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * The one Haze source for the window, provided above every surface. Glass children sample the
 * wallpaper layer through it; a surface that carries its own private HazeState has no backdrop
 * and degrades to a plain tint.
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/**
 * Liquid glass: a blurred, tinted sample of the ground under a lensing edge.
 *
 * The optic is built from the same [Shape] as the clip — backdrop, sheen, bottom thickness and
 * specular rim all share one outline, so nothing can drift from the surface it belongs to.
 *
 * No platform elevation shadow: Compose draws a shadow's silhouette behind the node, and behind a
 * translucent tint that silhouette reads as a flat darkening of the glass itself (and on some
 * devices is sliced away in a rectangle). Depth comes from the tint, the rim and the inner shade.
 */
@Immutable
data class LiquidGlassStyle(
    val blurRadius: Dp = 24.dp,
    val tint: Color = Ink.copy(alpha = 0.40f),
    val rimAlpha: Float = 0.52f,
    val sheenAlpha: Float = 0.07f,
    val noise: Float = 0.02f,
    /** Light gathering along the top edge, the lens reading. */
    val edgeLight: Float = 0.12f,
    /** Black thickness gathered along the bottom inner edge. */
    val innerShade: Float = 0.12f,
) {
    fun withTintAlpha(alpha: Float) = copy(tint = tint.copy(alpha = alpha))

    companion object {
        private val Ink = Color(0xFF0A0A0B)

        /** Chips and the 1 Thing island: small, close to the eye. */
        val Thin = LiquidGlassStyle(
            blurRadius = 18.dp,
            tint = Ink.copy(alpha = 0.28f),
            rimAlpha = 0.50f,
            sheenAlpha = 0.06f,
            noise = 0.012f,
            edgeLight = 0.10f,
            innerShade = 0.08f,
        )

        /** Sheets, the search card, dialog panels. */
        val Regular = LiquidGlassStyle(
            blurRadius = 26.dp,
            tint = Ink.copy(alpha = 0.38f),
            rimAlpha = 0.55f,
            sheenAlpha = 0.07f,
            noise = 0.02f,
            edgeLight = 0.12f,
            innerShade = 0.12f,
        )

        /** The tallest surface: search over the whole height. */
        val Heavy = LiquidGlassStyle(
            blurRadius = 32.dp,
            tint = Ink.copy(alpha = 0.46f),
            rimAlpha = 0.58f,
            sheenAlpha = 0.08f,
            noise = 0.025f,
            edgeLight = 0.12f,
            innerShade = 0.14f,
        )

        /** A small control resting on an existing glass surface; no second backdrop pass. */
        val Control = LiquidGlassStyle(
            blurRadius = 0.dp,
            tint = Ink.copy(alpha = 0.46f),
            rimAlpha = 0.55f,
            sheenAlpha = 0.10f,
            noise = 0f,
            edgeLight = 0.08f,
            innerShade = 0.10f,
        )

        /**
         * A seat on the dial. A disc this small has to show its backdrop or it reads as a smoked
         * puck, so the tint stays thin and a long blur does the legibility work instead; the rim
         * and the inner shade carry the lensing edge against a bright wallpaper.
         */
        val Seat = LiquidGlassStyle(
            blurRadius = 22.dp,
            tint = Ink.copy(alpha = 0.30f),
            rimAlpha = 0.50f,
            sheenAlpha = 0.10f,
            noise = 0f,
            edgeLight = 0.12f,
            innerShade = 0.14f,
        )

        /** The seat at the apex: held forward, so its rim and density step up. */
        val SeatApex = Seat.copy(
            tint = Ink.copy(alpha = 0.40f),
            rimAlpha = 0.62f,
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
        Modifier.hazeEffect(
            state = source,
            style = HazeStyle(
                blurRadius = style.blurRadius,
                tint = HazeTint(tint),
                noiseFactor = style.noise,
                fallbackTint = HazeTint(tint),
            ),
        )
    } else {
        Modifier.background(tint)
    }

    return this
        .clip(shape)
        .then(material)
        .drawWithCache {
            // One outline for the clip, the rim and the edge shading: they cannot misalign.
            val outline = shape.createOutline(size, layoutDirection, this)

            val topSheen = Brush.radialGradient(
                0f to Color.White.copy(alpha = style.sheenAlpha),
                0.55f to Color.White.copy(alpha = style.sheenAlpha * 0.22f),
                1f to Color.Transparent,
                center = Offset(size.width * 0.5f, -size.height * 0.12f),
                radius = size.maxDimension * 0.95f,
            )
            val topEdge = Brush.verticalGradient(
                0f to Color.White.copy(alpha = style.edgeLight),
                0.3f to Color.White.copy(alpha = style.edgeLight * 0.18f),
                1f to Color.Transparent,
                startY = 0f,
                endY = size.height * 0.62f,
            )
            val bottomShade = Brush.verticalGradient(
                0f to Color.Transparent,
                1f to Color.Black.copy(alpha = style.innerShade),
                startY = size.height * 0.45f,
                endY = size.height,
            )
            // Bright at the top, thin down the sides, a soft bounce at the bottom.
            val rim = Brush.linearGradient(
                0f to Color.White.copy(alpha = style.rimAlpha),
                0.28f to Color.White.copy(alpha = style.rimAlpha * 0.42f),
                0.62f to Color.White.copy(alpha = style.rimAlpha * 0.12f),
                0.86f to Color.White.copy(alpha = style.rimAlpha * 0.32f),
                1f to Color.White.copy(alpha = style.rimAlpha * 0.50f),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.85f, size.height),
            )
            val rimWidth = 1.5.dp.toPx()

            onDrawBehind {
                drawRect(topSheen)
                drawRect(topEdge)
                drawRect(bottomShade)
            }
            onDrawWithContent {
                drawContent()
                drawOutline(outline, rim, style = Stroke(rimWidth))
            }
        }
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
    // The material is its own child, drawn before the content: a focused text field inside the
    // panel re-composites its own node, and sharing that node with the backdrop left a bare
    // rectangle where the blur stopped being painted.
    Box(modifier = modifier) {
        Box(
            Modifier
                .matchParentSize()
                .liquidGlass(hazeState, shape, style, sampling)
        )
        content()
    }
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
