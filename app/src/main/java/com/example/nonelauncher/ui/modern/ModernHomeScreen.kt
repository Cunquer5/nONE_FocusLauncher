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
import com.example.nonelauncher.ui.MainViewModel
import com.example.nonelauncher.ui.components.IntentionDialog
import com.example.nonelauncher.ui.home.HomeViewModel
import com.example.nonelauncher.ui.modes.LauncherUiMode
import com.example.nonelauncher.ui.modern.components.*
import com.example.nonelauncher.ui.theme.OrangePrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ModernHomeScreen(
    appWidgetHost: android.appwidget.AppWidgetHost? = null,
    appWidgetManager: android.appwidget.AppWidgetManager? = null,
    mainViewModel: MainViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    onOpenSettings: () -> Unit // Kept for compatibility
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope() // ...

    // Sheets state
    var showAppDrawer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showOneThing by remember { mutableStateOf(false) }

    // Active Tab Logic for Dock
    val activeTab = remember(showAppDrawer, showSettings, showOneThing) {
        when {
            showSettings -> DockItemType.SETTINGS
            showAppDrawer -> DockItemType.APPS
            showOneThing -> DockItemType.ONE_THING
            else -> DockItemType.NONE
        }
    }

    // Intention Logic
    var appToLaunch by remember { mutableStateOf<AppModel?>(null) }

    // Persistent 1 Thing
    val oneThingTask = uiState.oneThingTask
    
    // Local state for visually tracking checked subtasks
    var checkedSubtasks by remember(oneThingTask) { mutableStateOf(setOf<Int>()) }
    var isMainTaskChecked by remember(oneThingTask) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangePrimary)
    ) {
        // Main Content: 1 Thing Display (Only visible when 1 Thing tab is active)
        if (showOneThing && oneThingTask != null && oneThingTask.isNotEmpty()) {
            val tasks = oneThingTask.split("\n").filter { it.isNotBlank() }
            val mainTask = tasks.firstOrNull() ?: ""
            val subTasks = tasks.drop(1)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 200.dp, top = 80.dp), // Push up for input card, add top padding
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                // Main Task
                Row(verticalAlignment = Alignment.Top) {
                    // Large Circle Outline - Checking this off clears everything
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 8.dp)
                            .border(3.dp, Color.White, CircleShape)
                            .clickable { 
                                if (!isMainTaskChecked) {
                                    isMainTaskChecked = true
                                    scope.launch {
                                        delay(5000L)
                                        homeViewModel.setOneThingTask(null)
                                        checkedSubtasks = emptySet()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isMainTaskChecked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = mainTask,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp,
                            textDecoration = if (isMainTaskChecked) TextDecoration.LineThrough else null
                        ),
                        color = if (isMainTaskChecked) Color.White.copy(alpha = 0.5f) else Color.White,
                        textAlign = TextAlign.Start
                    )
                }

                if (subTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    subTasks.forEachIndexed { index, subTask ->
                        val isChecked = checkedSubtasks.contains(index)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .clickable { 
                                    checkedSubtasks = if (isChecked) {
                                        checkedSubtasks - index
                                    } else {
                                        checkedSubtasks + index
                                    }
                                }
                        ) {
                            // Small Solid Dot or Checkmark
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (isChecked) Color.White.copy(alpha = 0.3f) 
                                        else Color.White.copy(alpha = 0.8f), 
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            
                            Text(
                                text = subTask,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else null
                                ),
                                color = if (isChecked) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }

        // 1 Thing Input Overlay (Only visible when 1 Thing tab is active)
        if (showOneThing) {
            // Background Box - Now non-clickable so user must use navbar to close
            Box(
                modifier = Modifier.fillMaxSize()
            )

            OneThingInputCard(
                onTaskSet = { task -> homeViewModel.setOneThingTask(task) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp) // Gap for dock
                    .padding(horizontal = 16.dp)
            )
        }

        if (showSettings) {
            SettingsSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp) // Leave space for dock
                    .padding(horizontal = 8.dp) // Little margin
                    .fillMaxHeight(0.85f),
                recentIntentions = uiState.recentIntentions,
                focusApps = uiState.apps,
                onLegacyMode = {
                    mainViewModel.setUiMode(LauncherUiMode.LEGACY)
                    showSettings = false
                },
                onWallpaperClick = {
                    try {
                        val intent =
                            android.content.Intent(android.content.Intent.ACTION_SET_WALLPAPER)
                        context.startActivity(
                            android.content.Intent.createChooser(
                                intent,
                                "Select Wallpaper"
                            )
                        )
                    } catch (e: Exception) {
                    }
                },
                onHistoryClick = {
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
                intentions = uiState.recentIntentions,
                onDismiss = { showHistory = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp) // Leave space for dock
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
                if (showOneThing) showOneThing = false else {
                    showOneThing = true
                    showSettings = false
                    showAppDrawer = false
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
                    .padding(bottom = 100.dp) // Leave space for dock
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
    }
}
