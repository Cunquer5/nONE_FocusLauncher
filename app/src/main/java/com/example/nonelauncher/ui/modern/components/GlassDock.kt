package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
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
import com.example.nonelauncher.ui.theme.GlassMorphism.dockGlassBackground
import com.example.nonelauncher.ui.theme.Black
import com.example.nonelauncher.ui.theme.OrangePrimary


enum class DockItemType {
    PHONE, ONE_THING, APPS, SETTINGS, NONE
}

@Composable
fun GlassDock(
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
            .dockGlassBackground()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
            
            // "1 Thing" Item
            Box(
                modifier = Modifier
                    .height(63.dp)
                    .clip(RoundedCornerShape(33.dp))
                    .then(if (activeItem == DockItemType.ONE_THING) Modifier.background(Black.copy(alpha = 0.5f)) else Modifier)
                    .clickable(onClick = onOneThingClick)
                    .padding(horizontal = if (activeItem == DockItemType.ONE_THING) 16.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            color = if (activeItem == DockItemType.ONE_THING) OrangePrimary else Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = activeItem == DockItemType.ONE_THING,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        Row {
                            // Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Thing", // Explicit request: "only the 'Thing' text"
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            DockItem(
                icon = null, // Custom icon used
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

@Composable
fun RoundedGridIcon(isActive: Boolean = false) {
    val iconColor = if (isActive) OrangePrimary else Color(0xFFE0E0E0)
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(26.dp) // Adjusted to fit nicely in 28dp area
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(3.dp)))
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(3.dp)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(3.dp)))
            Box(Modifier.size(10.dp).background(iconColor, RoundedCornerShape(3.dp)))
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
            .then(if (isActive) Modifier.background(Color.Black.copy(alpha = 0.65f)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = if (isActive && showLabel) 16.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                        tint = if (isActive) OrangePrimary else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isActive && showLabel,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = if (isActive) OrangePrimary else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
