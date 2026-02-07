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
import com.example.nonelauncher.ui.theme.apiAwareBlur
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
    onAppsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .height(80.dp)
    ) {
        // LAYER 1: Background with Haze blur (BEHIND content)
        Box(
            modifier = Modifier
                .matchParentSize()
                .haze(
                    state = hazeState,
                    style = HazeStyle(
                        tint = Color.Transparent
                    )
                )
        )
        
        // LAYER 2: Glass surface + Content (NO BLUR ON CONTENT)
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
            // Content layer (stays sharp)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(
                    icon = Icons.Default.Call,
                    label = "Phone",
                    isActive = activeItem == DockItemType.PHONE,
                    onClick = onPhoneClick,
                    showLabel = false
                )

                Box(
                    modifier = Modifier
                        .height(63.dp)
                        .clip(RoundedCornerShape(33.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
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
                            Row {
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
                }

                DockItem(
                    icon = null,
                    label = "App Grid",
                    isActive = activeItem == DockItemType.APPS,
                    onClick = onAppsClick,
                    showLabel = true,
                    customIcon = { isActive -> RoundedGridIcon(isActive) }
                )

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
fun RoundedGridIcon(isActive: Boolean = false) {
    val iconColor = if (isActive) Color(0xFFFF4D00) else Color(0xFFE0E0E0)
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(26.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(2.dp)))
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(2.dp)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(2.dp)))
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(2.dp)))
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
                    Modifier.widthIn(min = 48.dp) // Allow natural width when active
                else 
                    Modifier.width(48.dp) // Fixed width when inactive
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
                    color = /*if (isActive) Color(0xFFFF4D00) else */Color.White,
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