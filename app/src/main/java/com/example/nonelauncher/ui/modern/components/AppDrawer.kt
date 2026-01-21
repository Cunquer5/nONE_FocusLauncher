package com.example.nonelauncher.ui.modern.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.components.AppIcon
import com.example.nonelauncher.ui.theme.Black
import com.example.nonelauncher.ui.theme.GlassMorphism.cardGlassBackground

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    apps: List<AppModel>,
    gridColumns: Int,
    appWidgetHost: android.appwidget.AppWidgetHost?,
    appWidgetManager: android.appwidget.AppWidgetManager?,
    widgetIds: List<Int>,
    onWidgetAdded: (Int) -> Unit,
    onWidgetRemoved: (Int) -> Unit,
    onDismiss: () -> Unit,
    onAppClick: (AppModel) -> Unit,
    isFocusModeEnabled: Boolean = false,
    isBlurEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Layout Constants
    val widgetCardWidth = 110.dp

    // Snapping behavior for the carousel
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val snapFlingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = lazyListState)

    // Result launcher for widget picking
    val pickWidgetLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val appWidgetId = data?.getIntExtra(
                android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
                -1
            ) ?: -1
            if (appWidgetId != -1) {
                onWidgetAdded(appWidgetId)
            }
        } else if (result.resultCode == android.app.Activity.RESULT_CANCELED) {
            val appWidgetId = result.data?.getIntExtra(
                android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
                -1
            ) ?: -1
            if (appWidgetId != -1 && appWidgetHost != null) {
                appWidgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps 
        else apps.filter { 
            it.label.contains(searchQuery, ignoreCase = true) 
        }
    }

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha=0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // LAYER 1: Background with blur (BEHIND content)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .cardGlassBackground()
            )
            
            // LAYER 2: Content on top (NO blur)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { 
                    Text(
                        stringResource(com.example.nonelauncher.R.string.search_apps_widgets), 
                        color = Color.White.copy(0.8f)
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = stringResource(com.example.nonelauncher.R.string.action_search), 
                        tint = Color.White.copy(0.8f)
                    ) 
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable Widget Carousel
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = lazyListState,
                flingBehavior = snapFlingBehavior,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actual System Widgets
                items(widgetIds) { appWidgetId ->
                    var showMenu by remember { mutableStateOf(false) }

                    if (appWidgetHost != null && appWidgetManager != null) {
                        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                        if (appWidgetInfo != null) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .width(widgetCardWidth)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = { showMenu = true }
                                            )
                                        }
                                ) {
                                    androidx.compose.ui.viewinterop.AndroidView(
                                        factory = { ctx ->
                                            appWidgetHost.createView(ctx, appWidgetId, appWidgetInfo).apply {
                                                setAppWidget(appWidgetId, appWidgetInfo)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.7f))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Remove Widget", color = Color.White) },
                                        onClick = {
                                            onWidgetRemoved(appWidgetId)
                                            appWidgetHost.deleteAppWidgetId(appWidgetId)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Add Widget Button
                item {
                    val strAdd = stringResource(com.example.nonelauncher.R.string.action_add_widget)
                    Box(
                        modifier = Modifier
                            .size(widgetCardWidth)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Black.copy(alpha = 0.7f))
                            .clickable {
                                if (appWidgetHost != null) {
                                    val newId = appWidgetHost.allocateAppWidgetId()
                                    val pickIntent = android.content.Intent(
                                        android.appwidget.AppWidgetManager.ACTION_APPWIDGET_PICK
                                    ).apply {
                                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
                                    }
                                    pickWidgetLauncher.launch(pickIntent)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strAdd,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Grid - Fixed to 4 columns and scrollable
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isAffectedByFocus = isFocusModeEnabled && !app.isFocusApp
                    AppIcon(
                        app = app,
                        onAppClick = onAppClick,
                        gridColumns = 4,
                        isGrayscale = isAffectedByFocus,
                        isBlurred = isAffectedByFocus && isBlurEnabled,
                        isUninstallable = true
                    )
                }
            }
            }
        }
    }
}
