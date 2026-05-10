package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

@Composable
fun GlassDock(
    hazeState: HazeState,
    activeItem: DockItemType = DockItemType.NONE,
    onPhoneClick: () -> Unit,
    onOneThingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .height(80.dp)
    ) {
        // LAYER 1: Background with Haze blur
        Box(
            modifier = Modifier
                .matchParentSize()
                .haze(
                    state = hazeState,
                    style = HazeStyle(tint = Color.Transparent)
                )
        )

        // LAYER 2: Glass surface + content
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(40.dp))
                .hazeChild(
                    state = hazeState,
                    style = HazeStyle(blurRadius = 20.dp)
                )
                .background(Color(0xFF0F0F0F).copy(alpha = 0.55f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(40.dp)
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
                    showLabel = false
                )

                // 1 Thing — centre focal item
                Box(
                    modifier = Modifier
                        .height(63.dp)
                        .clip(RoundedCornerShape(33.dp))
                        //.background(Color.Black.copy(alpha = 0.5f))
                        .clickable(onClick = onOneThingClick)
                        .padding(horizontal = if (activeItem == DockItemType.ONE_THING) 16.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp),
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
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            Text(
                                text = "Thing",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                modifier = Modifier.padding(start = 1.dp)
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
