package com.example.nonelauncher.ui.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
import com.example.nonelauncher.data.model.parseOneThingTaskWithState
import com.example.nonelauncher.data.model.serializeOneThingTask
import com.example.nonelauncher.ui.MainViewModel
import com.example.nonelauncher.ui.components.IntentionDialog
import com.example.nonelauncher.ui.home.HomeViewModel
import com.example.nonelauncher.ui.modes.LauncherUiMode
import com.example.nonelauncher.ui.modern.components.*
import com.example.nonelauncher.ui.theme.OrangePrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze


@Composable
fun ModernHomeScreen(
    appWidgetHost: android.appwidget.AppWidgetHost? = null,
    appWidgetManager: android.appwidget.AppWidgetManager? = null,
    mainViewModel: MainViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    val allIntentions by homeViewModel.allIntentions.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }

    var showAppDrawer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showOneThing by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var appToLaunch by remember { mutableStateOf<AppModel?>(null) }
    
    // State for 1 Thing card visibility
    var showInputCard by remember { mutableStateOf(false) }
    var showTaskDisplay by remember { mutableStateOf(false) }

    val activeTab = remember(showAppDrawer, showSettings, showOneThing) {
        when {
            showSettings -> DockItemType.SETTINGS
            showAppDrawer -> DockItemType.APPS
            showOneThing -> DockItemType.ONE_THING
            else -> DockItemType.NONE
        }
    }

    // Task completion handlers
    val onTaskSet: (String) -> Unit = { taskStr ->
        // Parse the serialized task string back to OneThingTask object
        val parsedTask = parseOneThingTaskWithState(taskStr)
        if (parsedTask != null) {
            homeViewModel.setOneThingTaskWithState(parsedTask)
        }
        showInputCard = false
        showTaskDisplay = true
    }
    
    val onMainTaskCompleted: () -> Unit = {
        // Mark all subtasks as completed when main task is done
        uiState.oneThingTaskWithState?.subTasks?.forEach { subTask ->
            if (!subTask.isCompleted) {
                homeViewModel.updateSubtaskState(subTask.id, true)
            }
        }
        
        // Schedule cleanup after delay
        scope.launch {
            kotlinx.coroutines.delay(5000L)
            homeViewModel.resetOneThingTask()
        }
    }

    val oneThingTask = uiState.oneThingTaskWithState
    val checkedSubtaskIds = remember(oneThingTask) {
        oneThingTask?.subTasks?.filter { it.isCompleted }?.map { it.id }?.toSet() ?: emptySet()
    }
    
    // Old local state that we're replacing:
    // var checkedSubtasks by remember(oneThingTask) { mutableStateOf(setOf<Int>()) }
    // var isMainTaskChecked by remember(oneThingTask) { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {


        // Main Content: 1 Thing Display (Only visible when task exists and showTaskDisplay is true)
        if (showOneThing && showTaskDisplay && uiState.oneThingTaskWithState != null) {
            OneThingTaskDisplay(
                task = uiState.oneThingTaskWithState!!,
                onMainTaskCompleted = onMainTaskCompleted,
                onUpdateSubtask = { subtaskId, isCompleted ->
                    homeViewModel.updateSubtaskState(subtaskId, isCompleted)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp, bottom = 120.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        // 1 Thing Input Overlay (Only visible when showInputCard is true)
        if (showOneThing && showInputCard) {
            OneThingInputCard(
                onTaskSet = onTaskSet,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        if (showSettings) {
            SettingsSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(0.85f),
                recentIntentions = uiState.recentIntentions,
                focusApps = uiState.apps,
                onLegacyMode = {
                    mainViewModel.setUiMode(LauncherUiMode.LEGACY)
                    showSettings = false
                },
                onWallpaperClick = { showWallpaperDialog = true },
                onHistoryClick = { 
                    showSettings = false
                    showHistory = true 
                },
                onToggleFocusApp = { homeViewModel.toggleAppFocusStatus(it.packageName) },
                onToggleFocusMode = { homeViewModel.toggleFocusMode() },
                onToggleBlur = { homeViewModel.toggleBlur() },
                isFocusMode = uiState.isFocusModeEnabled,
                isBlur = uiState.isBlurEnabled
            )
        }

        if (showHistory) {
            IntentionHistorySheet(
                intentions = allIntentions,
                onDismiss = { 
                    showHistory = false
                    showSettings = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp) // Leave space for dock
                    .padding(horizontal = 8.dp) // Little margin - same as SettingsSheet
                    .fillMaxHeight(0.85f)
            )
        }

        // Dock
        GlassDock(
            activeItem = activeTab,
            modifier = Modifier.align(Alignment.BottomCenter),
            onPhoneClick = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    context.startActivity(intent)
                } catch (e: Exception) {
                }
            },
            onOneThingClick = {
                if (showOneThing) {
                    showOneThing = false
                    showInputCard = false
                    showTaskDisplay = false
                } else {
                    showOneThing = true
                    showSettings = false
                    showAppDrawer = false
                    if (oneThingTask != null) {
                        showTaskDisplay = true
                        showInputCard = false
                    } else {
                        showInputCard = true
                        showTaskDisplay = false
                    }
                }
            },
            onAppsClick = {
                if (showAppDrawer) showAppDrawer = false else {
                    showAppDrawer = true
                    showSettings = false
                    showOneThing = false
                }
            },
            onSettingsClick = {
                if (showSettings) showSettings = false else {
                    showSettings = true
                    showAppDrawer = false
                    showOneThing = false
                }
            }
        )

        if (showAppDrawer) {
            AppDrawer(
                apps = uiState.apps,
                gridColumns = uiState.gridColumns,
                appWidgetHost = appWidgetHost,
                appWidgetManager = appWidgetManager,
                widgetIds = uiState.widgetIds,
                onWidgetAdded = { id -> homeViewModel.updateWidgetIds(uiState.widgetIds + id) },
                onWidgetRemoved = { id -> homeViewModel.updateWidgetIds(uiState.widgetIds - id) },
                onDismiss = { showAppDrawer = false },
                onAppClick = { app ->
                    showAppDrawer = false
                    val isSystemOrEssential =
                        app.isEssential || app.packageName == "com.android.settings" || app.packageName == context.packageName
                    if (isSystemOrEssential) {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                        if (launchIntent != null) context.startActivity(launchIntent)
                    } else {
                        appToLaunch = app
                    }
                },
                isFocusModeEnabled = uiState.isFocusModeEnabled,
                isBlurEnabled = uiState.isBlurEnabled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp) // Leave space for dock
                    .padding(horizontal = 8.dp) // Little margin
                    .fillMaxHeight(0.85f)
            )
        }

        // Intention Dialog
        if (appToLaunch != null) {
            ModernIntentionDialog(
                app = appToLaunch!!,
                onDismiss = { appToLaunch = null },
                onConfirm = { intention ->
                    homeViewModel.onIntentionSubmitted(appToLaunch!!, intention) {
                        try {
                            val launchIntent =
                                context.packageManager.getLaunchIntentForPackage(appToLaunch!!.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        } catch (_: Exception) {
                        }
                        appToLaunch = null
                    }
                }
            )
        }

        // Wallpaper Selection Dialog
        if (showWallpaperDialog) {
            WallpaperSelectionDialog(
                onDismiss = { showWallpaperDialog = false },
                onOrangeColorSelected = {
                    // Set solid orange wallpaper using WallpaperManager
                    try {
                        val wallpaperManager = android.app.WallpaperManager.getInstance(context)
                        val orangeBitmap = android.graphics.Bitmap.createBitmap(
                            1920, 1080, android.graphics.Bitmap.Config.ARGB_8888
                        )
                        val canvas = android.graphics.Canvas(orangeBitmap)
                        canvas.drawColor(android.graphics.Color.parseColor("#FF4D00"))
                        wallpaperManager.setBitmap(orangeBitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onCustomWallpaperClick = {
                    // Open system wallpaper picker
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SET_WALLPAPER)
                        context.startActivity(
                            android.content.Intent.createChooser(intent, "Select Wallpaper")
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }
    }
}
