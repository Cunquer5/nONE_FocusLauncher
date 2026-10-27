@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.example.nonelauncher.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.R

/**
 * Departure-board material: matte flap black, steel hairlines, warm paint lettering.
 * Orange stays reserved for the one action on a surface and the current row.
 */
object Board {
    val Ink = Color(0xFF0C0C0C)
    val InkSoft = Color(0xFF151719)
    val Flap = Color(0xFF1A1D1F)
    val Well = Color(0xFF050505)
    val Paint = Color(0xFFF4F1EA)
    val PaintDim = Paint.copy(alpha = 0.55f)
    val Steel = Color(0xFF83868A)
    val Hair = Color(0xFF2B2F32)
    val HairFaint = Color(0xFF1B1E21)
    val Action = OrangePrimary

    val cell = RoundedCornerShape(2.dp)
    val panel = RoundedCornerShape(2.dp)
}

// Self-hosted faces: the board's voice is Oswald condensing, Roboto Mono for figures,
// Inter for labels. Variable files, so each weight is an axis setting, not another asset.
val BoardDisplayFamily = FontFamily(
    Font(R.font.oswald, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.oswald, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.oswald, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)

val BoardMonoFamily = FontFamily(
    Font(R.font.roboto_mono, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.roboto_mono, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
)

val BoardSansFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
)

val BoardLabelStyle = TextStyle(
    fontFamily = BoardSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.5.sp
)

val BoardDisplayStyle = TextStyle(
    fontFamily = BoardDisplayFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 19.sp,
    letterSpacing = 0.5.sp
)

val BoardMonoStyle = TextStyle(
    fontFamily = BoardMonoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.3.sp
)

val BoardBodyStyle = TextStyle(
    fontFamily = BoardSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 19.sp,
    letterSpacing = 0.2.sp
)

enum class BoardRowState { DONE, NOW, NEXT, LATER }

/** Vertical pinstripes 24dp apart — the flap rail texture, painted not imaged. */
fun Modifier.pinstripes(
    color: Color = Color.White.copy(alpha = 0.045f),
    spacing: Dp = 24.dp
): Modifier = drawBehind {
    val step = spacing.toPx().coerceAtLeast(1f)
    var x = step
    while (x < size.width) {
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
}

@Composable
fun BoardPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            // Flat console: one matte slab dropped on the wallpaper, no gradient, no highlight.
            .shadow(elevation = 26.dp, shape = Board.panel, clip = false, spotColor = Color.Black, ambientColor = Color.Black)
            .clip(Board.panel)
            .background(Board.Ink)
            .border(1.dp, Board.Hair, Board.panel)
    ) {
        content()
    }
}

/** Press feedback on the board: the row steps up one flap shade, no ripple, no glow. */
@Composable
fun Modifier.boardPress(
    interactionSource: MutableInteractionSource,
    shape: Shape = Board.cell,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val wash by animateColorAsState(
        targetValue = if (pressed) Board.Flap else Color.Transparent,
        label = "boardPress"
    )
    return this.background(wash, shape)
}

/** Split-flap wordmark: one hinged cell per letter, flipped in sequence on first show. */
@Composable
fun FlapWordmark(
    text: String,
    modifier: Modifier = Modifier,
    cellHeight: Dp = 30.dp,
    fontSize: TextUnit = 16.sp
) {
    val chars = text.uppercase()
    var shown by remember(chars) { mutableStateOf(false) }
    LaunchedEffect(chars) { shown = true }
    val density = LocalDensity.current
    val camera = with(density) { 14f * this.density }
    // Glyphs are capped to the flap: at 2x font scale an uncapped 16sp letter is taller than
    // the 30dp cell and gets clipped. The wordmark is a mark, body copy still follows the scale.
    val glyphSize = minOf(fontSize.value, with(density) { (cellHeight * 0.58f).toSp().value }).sp

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = chars },
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        chars.forEachIndexed { index, char ->
            if (char == ' ') {
                Spacer(modifier = Modifier.width(cellHeight * 0.35f))
            } else {
                val turn by animateFloatAsState(
                    targetValue = if (shown) 0f else -90f,
                    animationSpec = tween(durationMillis = 240, delayMillis = index * 26),
                    label = "flap$index"
                )
                Box(
                    modifier = Modifier
                        .size(width = cellHeight * 0.74f, height = cellHeight)
                        .graphicsLayer {
                            rotationX = turn
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                            this.cameraDistance = camera
                        }
                        .clip(Board.cell)
                        .background(Board.Flap)
                        .border(1.dp, Board.Hair, Board.cell),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.55f))
                    )
                    Text(
                        text = char.toString(),
                        color = Board.Paint,
                        fontFamily = BoardMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = glyphSize,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun BoardLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Board.PaintDim,
    maxLines: Int = 1
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        style = BoardLabelStyle,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun BoardDivider(modifier: Modifier = Modifier, color: Color = Board.HairFaint) {
    Box(modifier = modifier.fillMaxWidth().height(1.dp).background(color))
}

/** The single action cell on a surface — orange, square-cornered, paint lettering. */
@Composable
fun BoardActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(Board.cell)
            .background(if (enabled) Board.Action else Board.Action.copy(alpha = 0.3f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            // Ink on orange clears AA at this size; white on orange only reaches 3.3:1
            color = Board.Ink,
            style = BoardLabelStyle.copy(fontSize = 11.sp, letterSpacing = 1.8.sp),
            maxLines = 1
        )
    }
}

@Composable
fun BoardQuietButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(Board.cell)
            .border(1.dp, Board.Hair, Board.cell)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = Board.Paint,
            style = BoardLabelStyle.copy(fontSize = 11.sp, letterSpacing = 1.8.sp),
            maxLines = 1
        )
    }
}

@Composable
fun BoardCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        // 48dp touch target around the 34dp visual cell
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(Board.cell)
                .border(1.dp, Board.Hair, Board.cell),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Board.Paint.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun BoardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
    singleLine: Boolean = false,
    textStyle: TextStyle = BoardBodyStyle,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Board.Steel,
                style = textStyle
            )
        },
        textStyle = textStyle.copy(color = Board.Paint),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Board.Well,
            unfocusedContainerColor = Board.Well,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Board.Paint,
            unfocusedTextColor = Board.Paint,
            cursorColor = Board.Action
        ),
        shape = Board.cell,
        minLines = minLines,
        maxLines = maxLines,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier
    )
}
