package com.example.nonelauncher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.LocalHazeState
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.glassPress
import com.example.nonelauncher.ui.theme.liquidGlass

/**
 * The quiet action on a glass surface: blurred wallpaper under a dark tint, white label.
 * Press feedback is the 14% light wash from [glassPress] plus a 3% settle, never a ripple —
 * ripples disappear on black glass over a bright photo.
 */
@Composable
fun GlassMorphismButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Color.White,
    cornerRadius: Dp = 16.dp,
    height: Dp = 56.dp,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .height(height)
            .liquidGlass(
                hazeState = LocalHazeState.current,
                shape = shape,
                style = LiquidGlassStyle.Control.copy(blurRadius = 16.dp),
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .glassPress(interaction, shape)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * The one accent action of a surface. A tinted plate, not blurred wallpaper: the brand orange
 * has to read as the same orange over any wallpaper, so it carries only the glass rim and the
 * top-left sheen.
 */
@Composable
fun GradientGlassMorphismButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = OrangePrimary,
    textColor: Color = Color.White,
    cornerRadius: Dp = 16.dp,
    height: Dp = 56.dp,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .height(height)
            .liquidGlass(
                hazeState = null,
                shape = shape,
                style = LiquidGlassStyle(
                    tint = color,
                    rimAlpha = 0.42f,
                    sheenAlpha = 0.18f,
                    noise = 0f,
                ),
                sampling = false,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .glassPress(interaction, shape)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.invoke()
            Text(
                text = text,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
