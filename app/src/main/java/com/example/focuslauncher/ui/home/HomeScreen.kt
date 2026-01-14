package com.example.focuslauncher.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.PowerManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focuslauncher.data.local.entity.Intention
import com.example.focuslauncher.data.model.AppModel
import com.example.focuslauncher.ui.components.IntentionDialog
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val allIntentions by homeViewModel.allIntentions.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var showSettingsSheet by remember { mutableStateOf(false) }
    var appToLaunch by remember { mutableStateOf<AppModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val density = LocalDensity.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    
    val gridState = rememberLazyGridState()
    var isSearchVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var searchTextFieldValue by remember { 
        mutableStateOf(TextFieldValue(uiState.searchQuery, TextRange(uiState.searchQuery.length))) 
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 15 && gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0) {
                    if (!isSearchVisible) {
                        isSearchVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
            homeViewModel.onSearchQueryChanged("")
            searchTextFieldValue = TextFieldValue("")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (!isSearchVisible) {
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier
                        .padding(bottom = navigationBarPadding)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isSearchVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = statusBarPadding + 16.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchTextFieldValue,
                            onValueChange = { 
                                searchTextFieldValue = it
                                homeViewModel.onSearchQueryChanged(it.text) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = { Text("Search apps...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isSearchVisible = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(uiState.gridColumns),
                    state = gridState,
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    modifier = modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        top = if (isSearchVisible) 16.dp else statusBarPadding + 24.dp,
                        bottom = navigationBarPadding + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.apps, key = { it.packageName }) { app ->
                        val isAffectedByFocus = uiState.isFocusModeEnabled && !app.isFocusApp
                        val isSystemOrEssential = app.isEssential || app.packageName == "com.android.settings" || app.packageName == context.packageName

                        AppIcon(
                            app = app,
                            isGrayscale = isAffectedByFocus,
                            isBlurred = isAffectedByFocus && uiState.isBlurEnabled,
                            gridColumns = uiState.gridColumns,
                            onAppClick = {
                                if (isSystemOrEssential) {
                                     val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                     if (launchIntent != null) context.startActivity(launchIntent)
                                } else {
                                    appToLaunch = app
                                }
                            },
                            onUninstall = {
                                try {
                                    val intent = Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.fromParts("package", app.packageName, null)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            onAppInfo = {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", app.packageName, null)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            isUninstallable = !isSystemOrEssential
                        )
                    }
                }
            }
        }

        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                scrimColor = Color.Black.copy(alpha = 0.32f),
                containerColor = Color.Transparent, 
                tonalElevation = 0.dp,
                dragHandle = null, 
                windowInsets = WindowInsets(0, 0, 0, 0) 
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(statusBarPadding + 48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showSettingsSheet = false }
                    )
                    
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp, 4.dp)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                                )
                            }
                            
                            SettingsContent(
                                isFocusModeEnabled = uiState.isFocusModeEnabled,
                                onToggleFocusMode = { homeViewModel.toggleFocusMode() },
                                isBlurEnabled = uiState.isBlurEnabled,
                                onToggleBlur = { homeViewModel.toggleBlur() },
                                currentGridColumns = uiState.gridColumns,
                                onUpdateGridColumns = { homeViewModel.updateGridColumns(it) },
                                apps = uiState.apps,
                                onToggleAppFocus = { homeViewModel.toggleAppFocusStatus(it.packageName) },
                                onChangeWallpaper = {
                                    val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                                    context.startActivity(Intent.createChooser(intent, "Select Wallpaper"))
                                },
                                recentIntentions = uiState.recentIntentions,
                                allIntentions = allIntentions
                            )
                        }
                    }
                }
            }
        }
        
        val currentApp = appToLaunch
        if (currentApp != null) {
            IntentionDialog(
                app = currentApp,
                onDismiss = { appToLaunch = null },
                onConfirm = { intention ->
                    homeViewModel.onIntentionSubmitted(currentApp, intention) {
                        try {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(currentApp.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        } catch (_: Exception) {
                            // Ignored
                        }
                        appToLaunch = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    app: AppModel,
    isGrayscale: Boolean,
    isBlurred: Boolean,
    gridColumns: Int,
    onAppClick: (AppModel) -> Unit,
    onUninstall: () -> Unit,
    onAppInfo: () -> Unit,
    isUninstallable: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    val iconSize = when (gridColumns) {
        4 -> 56.dp
        5 -> 48.dp
        6 -> 40.dp
        else -> 56.dp
    }
    
    val blurRadius = when (gridColumns) {
        4 -> 8.dp
        5 -> 6.dp
        6 -> 4.dp
        else -> 8.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = Color.Gray),
                onClick = { onAppClick(app) },
                onLongClick = { showMenu = true }
            )
            .padding(4.dp)
    ) {
        Box {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("App Info") },
                    onClick = { 
                        showMenu = false
                        onAppInfo() 
                    }
                )
                if (isUninstallable) {
                    DropdownMenuItem(
                        text = { Text("Uninstall") },
                        onClick = { 
                            showMenu = false
                            onUninstall() 
                        }
                    )
                }
            }
        }
        val colorFilter = remember(isGrayscale) {
            if (isGrayscale) {
                ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            } else {
                null
            }
        }

        Box(
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    shape = CircleShape
                    clip = true
                }
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isBlurred) Modifier.blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded) 
                        else Modifier
                    ),
                colorFilter = colorFilter,
                alpha = if (isGrayscale) 0.5f else 1.0f 
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = app.label,
            fontSize = if (gridColumns > 5) 10.sp else 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (isGrayscale) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsContent(
    isFocusModeEnabled: Boolean,
    onToggleFocusMode: () -> Unit,
    isBlurEnabled: Boolean,
    onToggleBlur: () -> Unit,
    currentGridColumns: Int,
    onUpdateGridColumns: (Int) -> Unit,
    apps: List<AppModel>,
    onToggleAppFocus: (AppModel) -> Unit,
    onChangeWallpaper: () -> Unit,
    recentIntentions: List<Intention>,
    allIntentions: List<Intention>
) {
    var showFullLog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .animateContentSize()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = navigationBarPadding + 16.dp)
    ) {
        @Suppress("DEPRECATION")
        Text("Launcher Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onChangeWallpaper,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Wallpaper")
        }

        Spacer(modifier = Modifier.height(8.dp))

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(fallbackIntent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isIgnoringOptimizations
        ) {
            Text(if (isIgnoringOptimizations) "✓ Battery Unrestricted" else "Set Battery to Unrestricted")
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            @Suppress("DEPRECATION")
            Text("Focus Mode")
            Switch(checked = isFocusModeEnabled, onCheckedChange = { onToggleFocusMode() })
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            @Suppress("DEPRECATION")
            Text("Blur Non-Focus Apps")
            Switch(checked = isBlurEnabled, onCheckedChange = { onToggleBlur() })
        }

        Spacer(modifier = Modifier.height(16.dp))
        @Suppress("DEPRECATION")
        Text("App Grid Size", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(4, 5, 6).forEach { size ->
                OutlinedButton(
                    onClick = { onUpdateGridColumns(size) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = if (currentGridColumns == size) 
                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
                        else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("$size per row")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        @Suppress("DEPRECATION")
        Text("Manage Focus Apps", style = MaterialTheme.typography.titleMedium)
        @Suppress("DEPRECATION")
        Text("Select apps that remain colorful in Focus Mode", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            apps.forEach { app ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (app.isFocusApp) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onToggleAppFocus(app) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = app.icon, 
                        contentDescription = null, 
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                         colorFilter = if (!app.isFocusApp) {
                             remember(app.isFocusApp) { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
                         } else null
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Suppress("DEPRECATION")
            Text("Recent Intentions", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showFullLog = true }) {
                Text("See All")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recentIntentions.take(5).forEach { intention ->
                IntentionItem(intention)
            }
            if (recentIntentions.isEmpty()) {
                @Suppress("DEPRECATION")
                Text(
                    "No intentions logged yet.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (showFullLog) {
        FullIntentionLogDialog(
            intentions = allIntentions,
            onDismiss = { showFullLog = false }
        )
    }
}

@Composable
fun IntentionItem(intention: Intention) {
    @Suppress("DEPRECATION")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = intention.appLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            val date = remember(intention.timestamp) {
                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(intention.timestamp)
            }
            @Suppress("DEPRECATION")
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        @Suppress("DEPRECATION")
        Text(
            text = "\"${intention.intentionText}\"",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun FullIntentionLogDialog(
    intentions: List<Intention>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        @Suppress("DEPRECATION")
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    @Suppress("DEPRECATION")
                    Text(
                        text = "Full Intention Log",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                @Suppress("DEPRECATION")
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(intentions, key = { it.id }) { intention ->
                        IntentionItem(intention)
                    }
                }
            }
        }
    }
}
