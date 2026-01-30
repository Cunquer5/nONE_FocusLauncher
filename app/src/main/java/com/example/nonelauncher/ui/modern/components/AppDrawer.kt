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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.ui.components.AppIcon
import com.example.nonelauncher.ui.theme.Black
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import com.example.nonelauncher.ui.theme.AppGridSpacing

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
    val hazeState = HazeGlassMorphism.rememberHazeState()
    var searchQuery by remember { mutableStateOf("") }
    var showWidgetPicker by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val (columnSpacing, rowSpacing) = AppGridSpacing.getSpacingForColumns(4)

    // Layout Constants
    val widgetCardWidth = 110.dp

    // Snapping behavior for the carousel
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val snapFlingBehavior =
        androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = lazyListState)

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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // LAYER 1: Background with Haze blur (BEHIND content)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(
                        state = hazeState,
                        backgroundColor = Color.Transparent
                    )
            )
            
            // LAYER 2: Glass surface + Content (NO BLUR ON CONTENT)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .hazeChild(state = hazeState, blurRadius = 24.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Content goes here - stays sharp
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

                    val pagerState = rememberPagerState(pageCount = { 2 })

                    if (showWidgetPicker) {
                        // Widget Picker with Previews
                        WidgetPickerCarousel(
                            appWidgetManager = appWidgetManager,
                            onWidgetSelected = { widgetInfo ->
                                if (appWidgetHost != null) {
                                    val newId = appWidgetHost.allocateAppWidgetId()
                                    val bindIntent = android.content.Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_BIND)
                                        .putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, newId)
                                        .putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, widgetInfo.provider)
                                    
                                    if (appWidgetManager?.bindAppWidgetIdIfAllowed(newId, widgetInfo.provider) == true) {
                                        onWidgetAdded(newId)
                                        showWidgetPicker = false
                                    } else {
                                        pickWidgetLauncher.launch(bindIntent)
                                    }
                                }
                            },
                            onDismiss = { showWidgetPicker = false },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.weight(1f),
                                beyondBoundsPageCount = 1,
                                verticalAlignment = Alignment.Top
                            ) { page ->
                                if (page == 0) {
                                    // Page 1: App Grid - Only apps, no widget carousel
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            columnSpacing
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(rowSpacing),
                                        contentPadding = PaddingValues(bottom = 32.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredApps, key = { it.packageName }) { app ->
                                            val isAffectedByFocus =
                                                isFocusModeEnabled && !app.isFocusApp
                                            AppIcon(
                                                app = app,
                                                onAppClick = onAppClick,
                                                gridColumns = 4,
                                                isGrayscale = isAffectedByFocus,
                                                isBlurred = isAffectedByFocus && isBlurEnabled,
                                                isUninstallable = true,
                                                hazeState = hazeState
                                            )
                                        }
                                    }
                                } else {
                                    // Page 2: Full Widget Grid with Dynamic Fitting
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(bottom = 32.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            items = widgetIds,
                                            key = { it },
                                            span = { id ->
                                                val info = appWidgetManager?.getAppWidgetInfo(id)
                                                val span = if (info != null) {
                                                    when {
                                                        info.minWidth > 300 -> 4
                                                        info.minWidth > 200 -> 3
                                                        info.minWidth > 150 -> 2
                                                        else -> 1
                                                    }
                                                } else 1
                                                GridItemSpan(span)
                                            }
                                        ) { appWidgetId ->
                                            if (appWidgetHost != null && appWidgetManager != null) {
                                                val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                                                if (appWidgetInfo != null) {
                                                    var showMenu by remember { mutableStateOf(false) }
                                                    
                                                    val widgetHeight = remember(appWidgetInfo) {
                                                        appWidgetInfo.minHeight.dp.coerceAtLeast(widgetCardWidth)
                                                    }

                                                    Box {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(widgetHeight)
                                                                .clip(RoundedCornerShape(24.dp))
                                                                .background(Color.White.copy(alpha = 0.05f))
                                                                .pointerInput(Unit) {
                                                                    detectTapGestures(onLongPress = { showMenu = true })
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

                                        item(span = { GridItemSpan(1) }) {
                                            Box(
                                                modifier = Modifier
                                                    .size(widgetCardWidth)
                                                    .clip(RoundedCornerShape(24.dp))
                                                    .background(Color.Black.copy(alpha = 0.7f))
                                                    .clickable { showWidgetPicker = true },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add Widget",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Page Indicators
                            Row(
                                modifier = Modifier
                                    .height(32.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(2) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.3f)
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(6.dp)
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

@Composable
fun WidgetPickerCarousel(
    appWidgetManager: android.appwidget.AppWidgetManager?,
    onWidgetSelected: (android.appwidget.AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val availableWidgets = remember {
        appWidgetManager?.getInstalledProviders() ?: emptyList()
    }

    Column(modifier = modifier) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Widget",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic grid based on available widgets
        val columns = minOf(3, availableWidgets.size)
        if (availableWidgets.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(availableWidgets) { widgetInfo ->
                    WidgetPreviewCard(
                        widgetInfo = widgetInfo,
                        onClick = { onWidgetSelected(widgetInfo) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No widgets available",
                    color = Color.White.copy(0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun WidgetPreviewCard(
    widgetInfo: android.appwidget.AppWidgetProviderInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val packageManager = context.packageManager
    
    val widgetLabel = remember(widgetInfo) {
        try {
            widgetInfo.loadLabel(packageManager)
        } catch (e: Exception) {
            "Widget"
        }
    }
    
    val widgetPreview = remember(widgetInfo) {
        try {
            widgetInfo.loadPreviewImage(context, android.util.DisplayMetrics.DENSITY_DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (widgetPreview != null) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.widget.ImageView(ctx).apply {
                            setImageDrawable(widgetPreview)
                            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White.copy(0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White.copy(0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = widgetLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 9.sp
            )
        }
    }
}