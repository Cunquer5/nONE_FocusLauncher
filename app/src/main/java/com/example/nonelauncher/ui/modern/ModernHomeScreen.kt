package com.example.nonelauncher.ui.modern

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.parseOneThingTaskWithState
import com.example.nonelauncher.ui.MainViewModel
import com.example.nonelauncher.ui.home.HomeViewModel
import com.example.nonelauncher.ui.modes.LauncherUiMode
import com.example.nonelauncher.ui.modern.components.*
import kotlinx.coroutines.launch
import dev.chrisbanes.haze.HazeState

@Composable
fun ModernHomeScreen(
    mainViewModel: MainViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    val allIntentions by homeViewModel.allIntentions.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }

    var showSearch by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showOneThing by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var appToLaunch by remember { mutableStateOf<AppModel?>(null) }

    var showInputCard by remember { mutableStateOf(false) }
    var showTaskDisplay by remember { mutableStateOf(false) }

    val activeTab = remember(showSettings, showOneThing) {
        when {
            showSettings -> DockItemType.SETTINGS
            showOneThing -> DockItemType.ONE_THING
            else -> DockItemType.NONE
        }
    }

    val onTaskSet: (String) -> Unit = { taskStr ->
        val parsedTask = parseOneThingTaskWithState(taskStr)
        if (parsedTask != null) {
            homeViewModel.setOneThingTaskWithState(parsedTask)
        }
        showInputCard = false
        showTaskDisplay = true
    }

    val onMainTaskCompleted: () -> Unit = {
        uiState.oneThingTaskWithState?.subTasks?.forEach { subTask ->
            if (!subTask.isCompleted) {
                homeViewModel.updateSubtaskState(subTask.id, true)
            }
        }
        scope.launch {
            kotlinx.coroutines.delay(5000L)
            homeViewModel.resetOneThingTask()
        }
    }

    val oneThingTask = uiState.oneThingTaskWithState

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Swipe down anywhere on the home screen to open search
            .pointerInput(showSearch, showSettings, showOneThing) {
                detectVerticalDragGestures { _, dragAmount ->
                    // dragAmount > 0 means finger moving downward
                    if (dragAmount > 20f && !showSearch && !showSettings && !showOneThing) {
                        showSearch = true
                    }
                }
            }
    ) {

        // 1 Thing task display
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

        // 1 Thing input card
        if (showOneThing && showInputCard) {
            OneThingInputCard(
                onTaskSet = onTaskSet,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        // Settings sheet
        if (showSettings) {
            SettingsSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(0.85f),
                recentIntentions = uiState.recentIntentions,
                onLegacyMode = {
                    mainViewModel.setUiMode(LauncherUiMode.LEGACY)
                    showSettings = false
                },
                onWallpaperClick = { showWallpaperDialog = true },
                onHistoryClick = {
                    showHistory = true
                    showSettings = false
                    showOneThing = false
                }
            )
        }

        // Intention history sheet
        if (showHistory) {
            IntentionHistorySheet(
                intentions = allIntentions,
                onDismiss = {
                    showHistory = false
                    showSettings = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(0.85f)
            )
        }

        // Dock — 3 items only
        GlassDock(
            hazeState = hazeState,
            activeItem = activeTab,
            modifier = Modifier.align(Alignment.BottomCenter),
            onPhoneClick = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    context.startActivity(intent)
                } catch (e: Exception) { }
            },
            onOneThingClick = {
                if (showOneThing) {
                    showOneThing = false
                    showInputCard = false
                    showTaskDisplay = false
                } else {
                    showOneThing = true
                    showSettings = false
                    showHistory = false
                    if (oneThingTask != null) {
                        showTaskDisplay = true
                        showInputCard = false
                    } else {
                        showInputCard = true
                        showTaskDisplay = false
                    }
                }
            },
            onSettingsClick = {
                if (showSettings) {
                    showSettings = false
                } else {
                    showSettings = true
                    showHistory = false
                    showOneThing = false
                }
            }
        )

        // Search overlay — slides in from top on swipe-down
        AppSearchOverlay(
            visible = showSearch,
            apps = uiState.apps,
            onDismiss = { showSearch = false },
            onAppClick = { app ->
                showSearch = false
                val isSystemOrEssential = app.isEssential ||
                    app.packageName == "com.android.settings" ||
                    app.packageName == context.packageName
                if (isSystemOrEssential) {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) context.startActivity(launchIntent)
                } else {
                    appToLaunch = app
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Intention dialog
        if (appToLaunch != null) {
            ModernIntentionDialog(
                app = appToLaunch!!,
                onDismiss = { appToLaunch = null },
                onConfirm = { intention ->
                    homeViewModel.onIntentionSubmitted(appToLaunch!!, intention) {
                        try {
                            val launchIntent = context.packageManager
                                .getLaunchIntentForPackage(appToLaunch!!.packageName)
                            if (launchIntent != null) context.startActivity(launchIntent)
                        } catch (_: Exception) { }
                        appToLaunch = null
                    }
                }
            )
        }

        // Wallpaper dialog
        if (showWallpaperDialog) {
            WallpaperSelectionDialog(
                onDismiss = { showWallpaperDialog = false },
                onOrangeColorSelected = {
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
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SET_WALLPAPER)
                        context.startActivity(android.content.Intent.createChooser(intent, "Select Wallpaper"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }
    }
}
