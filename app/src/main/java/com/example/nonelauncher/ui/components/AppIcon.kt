package com.example.nonelauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardBodyStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppModel,
    isGrayscale: Boolean = false,
    gridColumns: Int = 4,
    onAppClick: (AppModel) -> Unit,
    onUninstall: () -> Unit = {},
    onAppInfo: () -> Unit = {},
    isUninstallable: Boolean = false,
    onTogglePinned: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    val iconSize = remember(gridColumns) {
        when (gridColumns) {
            4 -> 54.dp
            5 -> 46.dp
            6 -> 38.dp
            else -> 54.dp
        }
    }

    val colorFilter = remember(isGrayscale) {
        if (isGrayscale) {
            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        } else {
            null
        }
    }

    val (fontSize, lineHeight) = remember(gridColumns) {
        if (gridColumns > 5) 9.sp to 11.sp else 10.sp to 12.sp
    }

    val wellColor by animateColorAsState(
        targetValue = if (pressed) Board.Flap else Board.Well,
        label = "appWell"
    )
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "appPress")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(Board.cell)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = "Open ${app.label}",
                onLongClickLabel = "Options for ${app.label}",
                onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    onAppClick(app)
                },
                onLongClick = { showMenu = true }
            )
            .padding(4.dp)
    ) {
        Box {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Board.Flap)
            ) {
                DropdownMenuItem(
                    text = { Text("App Info", color = Board.Paint, style = BoardBodyStyle) },
                    onClick = {
                        showMenu = false
                        onAppInfo()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            if (app.isPinned) "Remove from Essentials" else "Add to Essentials",
                            color = Board.Paint,
                            style = BoardBodyStyle
                        )
                    },
                    onClick = {
                        showMenu = false
                        onTogglePinned()
                    }
                )
                if (isUninstallable) {
                    DropdownMenuItem(
                        text = { Text("Uninstall", color = Board.Paint, style = BoardBodyStyle) },
                        onClick = {
                            showMenu = false
                            onUninstall()
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(Board.cell)
                .background(wellColor)
                .border(1.dp, Board.Hair, Board.cell)
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = null,
                colorFilter = colorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(Board.cell)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = app.label,
            fontSize = fontSize,
            lineHeight = lineHeight,
            letterSpacing = 0.3.sp,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = Board.Paint.copy(alpha = if (isGrayscale) 0.45f else 0.92f)
        )
    }
}
