package com.example.nonelauncher.ui.modern

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.parseOneThingTaskWithState
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import com.example.nonelauncher.ui.home.HomeViewModel
import com.example.nonelauncher.ui.modern.components.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun ModernHomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onOpenSettings: () -> Unit = {},
    onRequestFeedbackPrompt: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    val allIntentions by homeViewModel.allIntentions.collectAsState(initial = emptyList())
    val structureSteps by homeViewModel.structureSteps.collectAsState()
    val structureIndex by homeViewModel.structureIndex.collectAsState()
    val hazeState = remember { HazeState() }

    // Wake-start: first home resume of a new day resets the structure flow to step 1
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) homeViewModel.rollStructureDay()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showSearch by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showOneThing by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showStructure by remember { mutableStateOf(false) }
    var appToLaunch by remember { mutableStateOf<AppModel?>(null) }

    var showInputCard by remember { mutableStateOf(false) }
    var showTaskDisplay by remember { mutableStateOf(false) }
    var dockVisible by remember { mutableStateOf(false) }
    val anySheet = showSettings || showOneThing || showHistory || showStructure
    // Sheets anchor above the dock, so an open sheet forces the dock shown
    val dockShown = dockVisible || anySheet

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
        // 5s disposal lives in the ViewModel: survives navigation, single source, re-armed on re-complete
        homeViewModel.scheduleOneThingReset()
    }

    val oneThingTask = uiState.oneThingTaskWithState

    // Back closes the open surface (launcher convention: bare home still swallows back)
    BackHandler(enabled = showSearch || showOneThing || showSettings || showHistory || showStructure || dockVisible) {
        when {
            showSearch -> {
                showSearch = false
                homeViewModel.onSearchQueryChanged("")
            }
            showHistory -> {
                showHistory = false
                showSettings = true
            }
            showStructure -> showStructure = false
            showOneThing -> {
                showOneThing = false
                showInputCard = false
                showTaskDisplay = false
            }
            else -> {
                showSettings = false
                dockVisible = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            // Tap empty home hides the dock again
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { dockVisible = false }
            // Swipe up anywhere reveals the hidden dock
            .pointerInput(showSearch, anySheet, dockShown) {
                var up = 0f
                detectVerticalDragGestures(
                    onDragStart = { up = 0f },
                    onDragEnd = { up = 0f },
                    onDragCancel = { up = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        // dragAmount < 0 = finger moving upward; require a deliberate cumulative pull
                        if (dragAmount < 0) up += -dragAmount
                        if (up > 150f && !showSearch && !dockShown) {
                            dockVisible = true
                            up = 0f
                        }
                    }
                )
            }
            // Swipe from the right side opens search, which slides in from the right
            .pointerInput(showSearch, anySheet) {
                var left = 0f
                detectHorizontalDragGestures(
                    onDragStart = { left = 0f },
                    onDragEnd = { left = 0f },
                    onDragCancel = { left = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        // dragAmount < 0 = finger moving leftward
                        if (dragAmount < 0) left += -dragAmount
                        if (left > 150f && !showSearch && !anySheet) {
                            showSearch = true
                            left = 0f
                        }
                    }
                )
            }
    ) {

        // Right-edge handle — teaches/taps the swipe-from-right search gesture
        if (!showSearch && !anySheet) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .width(20.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .clickable { showSearch = true }
                    .clearAndSetSemantics { contentDescription = "Search apps, contacts, files" }
            )
        }

        // Current structure step — tap to advance through the day's flow
        if (structureSteps.isNotEmpty() &&
            !showSearch && !showSettings && !showHistory && !showOneThing && !showStructure
        ) {
            val idx = structureIndex.coerceIn(0, structureSteps.lastIndex)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .hazeChild(state = hazeState, blurRadius = 14.dp)
                    .background(Color(0xFF0F0F0F).copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .clickable { homeViewModel.advanceStructureStep() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clearAndSetSemantics {
                        contentDescription = "Structure step ${idx + 1} of ${structureSteps.size}: ${structureSteps[idx]}. Tap to advance."
                    }
            ) {
                Text(
                    text = structureSteps[idx],
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 240.dp)
                )
                Text(
                    text = " ${idx + 1}/${structureSteps.size}",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

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
                    .statusBarsPadding()
                    .padding(top = 12.dp, bottom = 120.dp)
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
                onWallpaperClick = {
                    showWallpaperDialog = true
                    showSettings = false
                },
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

        // Structure editor sheet
        if (showStructure) {
            StructureSheet(
                steps = structureSteps,
                onDismiss = { showStructure = false },
                onSave = { steps ->
                    homeViewModel.setStructureSteps(steps)
                    showStructure = false
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(0.8f)
            )
        }

        // Structure access rail — semi-transparent minimal button on the left edge
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .width(20.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(Color.White.copy(alpha = 0.3f))
                .clickable {
                    if (showStructure) {
                        showStructure = false
                    } else {
                        showStructure = true
                        showSettings = false
                        showHistory = false
                        showOneThing = false
                        showSearch = false
                        homeViewModel.onSearchQueryChanged("")
                    }
                }
                .clearAndSetSemantics { contentDescription = "Structure" }
        )

        // Bottom handle — tap to reveal the dock (swipe up anywhere also works)
        if (!dockShown && !showSearch) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.45f))
                    .clickable { dockVisible = true }
                    .clearAndSetSemantics { contentDescription = "Show dock" }
            )
        }

        // Dock — hidden until swipe-up, bottom handle, or an open sheet reveals it
        AnimatedVisibility(
            visible = dockShown,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassDock(
            hazeState = hazeState,
            activeItem = activeTab,
            modifier = Modifier,
            onPhoneClick = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    context.startActivity(intent)
                } catch (e: Exception) { }
            },
            onOneThingClick = {
                if (UserPreferencesRepository.FEEDBACK_TEST_TRIGGER_ON_ONE_THING) {
                    onRequestFeedbackPrompt()
                }
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
        }

        // Search overlay — slides in from the right on swipe-from-right
        UniversalSearchOverlay(
            visible = showSearch,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { homeViewModel.onSearchQueryChanged(it) },
            apps = uiState.apps,
            contacts = uiState.contactResults,
            files = uiState.fileResults,
            onDismiss = { 
                showSearch = false
                homeViewModel.onSearchQueryChanged("")
            },
            onAppClick = { app ->
                showSearch = false
                homeViewModel.onSearchQueryChanged("")
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
            onContactClick = { contact ->
                showSearch = false
                homeViewModel.onSearchQueryChanged("")
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.withAppendedPath(
                            android.provider.ContactsContract.Contacts.CONTENT_URI,
                            contact.id
                        )
                    }
                    context.startActivity(intent)
                } catch (e: Exception) { }
            },
            onFileClick = { file ->
                showSearch = false
                homeViewModel.onSearchQueryChanged("")
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(file.uri, context.contentResolver.getType(file.uri))
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) { }
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
                onDismiss = {
                    showWallpaperDialog = false
                    showSettings = true
                },
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
