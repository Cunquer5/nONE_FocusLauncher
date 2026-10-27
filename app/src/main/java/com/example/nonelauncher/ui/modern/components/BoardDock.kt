package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardMonoFamily

/**
 * Dock as the board's control strip: three labelled cells split by steel hairlines.
 * The active cell lights its label orange; nothing expands, the row keeps its grid.
 */
@Composable
fun BoardDock(
    activeItem: DockItemType = DockItemType.NONE,
    onPhoneClick: () -> Unit,
    onOneThingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(58.dp)
            .clip(Board.cell)
            .background(Board.Ink)
            .border(1.dp, Board.Hair, Board.cell)
    ) {
        DockCell(
            label = "Phone",
            isActive = activeItem == DockItemType.PHONE,
            onClick = onPhoneClick,
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Call
        )
        DockSeam()
        DockCell(
            label = "1 Thing",
            isActive = activeItem == DockItemType.ONE_THING,
            onClick = onOneThingClick,
            modifier = Modifier.weight(1.15f),
            figure = "1"
        )
        DockSeam()
        DockCell(
            label = "Settings",
            isActive = activeItem == DockItemType.SETTINGS,
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Settings
        )
    }
}

@Composable
private fun DockSeam() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(Board.HairFaint)
    )
}

@Composable
private fun DockCell(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    figure: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = label
                selected = isActive
            }
            .background(if (isActive) Board.Flap else Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) Board.Action else Board.Paint.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            } else if (figure != null) {
                Text(
                    text = figure,
                    color = if (isActive) Board.Action else Board.Paint.copy(alpha = 0.85f),
                    fontFamily = BoardMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            BoardLabel(
                text = label,
                color = if (isActive) Board.Action else Board.Paint.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
        // The active cell marks itself on the edge, like a lit berth on the board.
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Board.Action)
            )
        }
    }
}
