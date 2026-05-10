package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.components.AppIcon
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

/**
 * Full-screen search overlay, shown when the user swipes down on the home screen.
 * Automatically focuses the search field when visible.
 */
@Composable
fun AppSearchOverlay(
    visible: Boolean,
    apps: List<AppModel>,
    onDismiss: () -> Unit,
    onAppClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = HazeGlassMorphism.rememberHazeState()
    var searchQuery by remember(visible) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) emptyList()
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    // Auto-focus the text field whenever the overlay becomes visible
    LaunchedEffect(visible) {
        if (visible) {
            kotlinx.coroutines.delay(100) // brief delay for animation to start
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Blur background layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState, backgroundColor = Color.Transparent)
            )

            // Glass surface
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeChild(state = hazeState, blurRadius = 28.dp)
                    .background(Color.Black.copy(alpha = 0.75f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 120.dp) // leave room for dock
                ) {
                    // Search bar row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text("Search apps...", color = Color.White.copy(0.6f))
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White.copy(0.7f)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(0.7f)
                                        )
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFFF4D00)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .focusRequester(focusRequester),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    when {
                        searchQuery.isBlank() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Type to search apps",
                                    color = Color.White.copy(alpha = 0.35f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        filteredApps.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No apps found",
                                    color = Color.White.copy(alpha = 0.45f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredApps, key = { it.packageName }) { app ->
                                    AppIcon(
                                        app = app,
                                        onAppClick = onAppClick,
                                        gridColumns = 4,
                                        isGrayscale = false,
                                        isBlurred = false,
                                        isUninstallable = true,
                                        hazeState = hazeState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
