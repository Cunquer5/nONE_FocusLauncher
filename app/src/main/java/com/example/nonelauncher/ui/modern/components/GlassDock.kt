package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.liquidGlass
import dev.chrisbanes.haze.HazeState

@Composable
fun GlassDock(
    hazeState: HazeState,
    activeItem: DockItemType = DockItemType.NONE,
    onPhoneClick: () -> Unit,
    onOneThingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // One glass surface. A second `haze()` source used to live in this box, which made the
    // dock sample its own empty layer instead of the wallpaper and offset the blur.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .height(80.dp)
            .liquidGlass(
                hazeState = hazeState,
                shape = RoundedCornerShape(40.dp),
                style = LiquidGlassStyle.Regular.copy(
                    blurRadius = 24.dp,
                    tint = Color(0xFF0A0A0B).copy(alpha = 0.42f),
                    innerShade = 0.16f,
                ),
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Phone
            DockItem(
                icon = Icons.Default.Call,
                label = "Phone",
                isActive = activeItem == DockItemType.PHONE,
                onClick = onPhoneClick,
                showLabel = true
            )

            // 1 Thing — centre focal item. Active plate, label rhythm and expand
            // direction match the sibling DockItems exactly.
            Box(
                modifier = Modifier
                    .height(63.dp)
                    .clip(RoundedCornerShape(33.dp))
                    .then(
                        if (activeItem == DockItemType.ONE_THING)
                            Modifier.background(Color.Black.copy(alpha = 0.55f))
                        else Modifier
                    )
                    .clickable(onClick = onOneThingClick)
                    .clearAndSetSemantics { contentDescription = "1 Thing" }
                    .padding(horizontal = if (activeItem == DockItemType.ONE_THING) 12.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            color = Color(0xFFFF4D00),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    AnimatedVisibility(
                        visible = activeItem == DockItemType.ONE_THING,
                        enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                    ) {
                        Text(
                            text = "Thing",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Settings
            DockItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isActive = activeItem == DockItemType.SETTINGS,
                onClick = onSettingsClick,
                showLabel = true
            )
        }
    }
}

@Composable
fun DockItem(
    icon: ImageVector?,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    showLabel: Boolean = true,
    customIcon: @Composable ((Boolean) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .height(63.dp)
            .clip(RoundedCornerShape(33.dp))
            .then(if (isActive) Modifier.background(Color.Black.copy(alpha = 0.55f)) else Modifier)
            .clickable(onClick = onClick)
            .then(
                if (isActive && showLabel)
                    Modifier.widthIn(min = 48.dp)
                else
                    Modifier.width(48.dp)
            )
            .padding(horizontal = if (isActive && showLabel) 12.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.wrapContentWidth()
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (customIcon != null) {
                    customIcon(isActive)
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) Color(0xFFFF4D00) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isActive && showLabel,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
            }
        }
    }
}
