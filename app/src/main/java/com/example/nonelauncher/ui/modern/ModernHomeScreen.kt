package com.example.nonelauncher.ui.modern

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import com.example.nonelauncher.ui.theme.bottomEnter
import com.example.nonelauncher.ui.theme.bottomExit
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.parseOneThingTaskWithState
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import com.example.nonelauncher.ui.home.HomeViewModel
import com.example.nonelauncher.ui.modern.components.*
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.liquidGlass
import dev.chrisbanes.haze.HazeState

@Composable
fun ModernHomeScreen(
    hazeState: HazeState,
    homeViewModel: HomeViewModel = viewModel(),
    onOpenSettings: () -> Unit = {},
    onRequestFeedbackPrompt: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    val allIntentions by homeViewModel.allIntentions.collectAsState(initial = emptyList())
    val structureSteps by homeViewModel.structureSteps.collectAsState()
    val structureIndex by homeViewModel.structureIndex.collectAsState()
    val focusModeEnabled by homeViewModel.focusModeEnabled.collectAsState()
    val frequentPackages by homeViewModel.frequentPackages.collectAsState()
    val frequentApps = remember(frequentPackages, uiState.apps) {
        frequentPackages.mapNotNull { pkg -> uiState.apps.firstOrNull { it.packageName == pkg } }
    }

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
    var dialVisible by remember { mutableStateOf(false) }
    val anySheet = showSettings || showOneThing || showHistory || showStructure

    val openStructure: () -> Unit = {
        dialVisible = false
        showStructure = true
        showSettings = false
        showHistory = false
        showOneThing = false
        showInputCard = false
        showTaskDisplay = false
        showSearch = false
        homeViewModel.onSearchQueryChanged("")
    }

    // Search and Structure share the bottom handle, and either can be opened while the
    // other is up, so each entry point must close the rest first.
    val openSearch: () -> Unit = {
        dialVisible = false
        showSearch = true
        showStructure = false
        showSettings = false
        showHistory = false
        showOneThing = false
        showInputCard = false
        showTaskDisplay = false
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
    // AnimatedVisibility keeps its content composed while it slides out, and the disposal timer
    // nulls the live task mid-exit, so the sheet keeps drawing the task it was already showing.
    var retainedTask by remember { mutableStateOf<OneThingTask?>(null) }
    LaunchedEffect(oneThingTask) {
        if (oneThingTask != null) retainedTask = oneThingTask
    }
    val taskForSheet = oneThingTask ?: retainedTask

    val density = LocalDensity.current
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
    // Sheets rise from the bottom and stop short of the top, so the wallpaper keeps a band of
    // daylight above the glass and a card reads as laid over the page, not as the page itself.
    val sheetTopGap = LocalConfiguration.current.screenHeightDp.dp * 0.14f

    // Any app outside the essential set is gated by the intention dialog. The dial and the
    // search overlay share this so the same app behaves the same way from either entry.
    val launchApp: (AppModel) -> Unit = { app ->
        val isSystemOrEssential = app.isEssential ||
            app.packageName == "com.android.settings" ||
            app.packageName == context.packageName
        if (isSystemOrEssential) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) context.startActivity(launchIntent)
        } else {
            appToLaunch = app
        }
    }

    // The dial's seats: the four surfaces first, then whatever the user pinned.
    val dialPinnedApps = remember(uiState.apps) { uiState.apps.filter { it.isPinned } }
    val dialEntries = remember(dialPinnedApps) {
        buildList {
            add(DialEntry.Action(DialAction.ONE_THING, "1 Thing"))
            add(DialEntry.Action(DialAction.STRUCTURE, "Structure"))
            add(DialEntry.Action(DialAction.SEARCH, "Search"))
            add(DialEntry.Action(DialAction.SETTINGS, "Settings"))
            add(DialEntry.Action(DialAction.ADD_APPS, "Add apps"))
            dialPinnedApps.forEach { add(DialEntry.App(it)) }
        }
    }

    // The sheets ride the root's imePadding: when the keyboard opens they settle on its top edge
    // rather than at the bottom of the window, so a focused field is never under the keys.
    val closeOneThing: () -> Unit = {
        showOneThing = false
        showTaskDisplay = false
        showInputCard = false
    }

    // Back closes the open surface (launcher convention: bare home still swallows back)
    BackHandler(enabled = showSearch || showOneThing || showSettings || showHistory || showStructure || dialVisible) {
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
                dialVisible = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            // Tap empty home hides the dial again
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { dialVisible = false }
            // Swipe up near the bottom reveals the hidden dial — bottom band only, not anywhere
            .pointerInput(showSearch, anySheet, dialVisible, appToLaunch, showWallpaperDialog) {
                var up = 0f
                var nearBottom = false
                detectVerticalDragGestures(
                    onDragStart = { start ->
                        up = 0f
                        nearBottom = start.y > size.height * 0.7f
                    },
                    onDragEnd = { up = 0f },
                    onDragCancel = { up = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        // dragAmount < 0 = finger moving upward; require a deliberate cumulative pull
                        if (dragAmount < 0) up += -dragAmount
                        if (nearBottom && up > 150f && !showSearch && !anySheet && !dialVisible &&
                            appToLaunch == null && !showWallpaperDialog
                        ) {
                            dialVisible = true
                            up = 0f
                        }
                    }
                )
            }
            // The step chip's top band is the only horizontal screen gesture left: swipe it
            // right for the next step, left for the previous one. Search and Structure are
            // deliberately not edge gestures anymore — the bottom handle owns both.
            .pointerInput(showSearch, anySheet, structureSteps.size, appToLaunch, showWallpaperDialog) {
                var acc = 0f
                var onChip = false
                detectHorizontalDragGestures(
                    onDragStart = { start ->
                        acc = 0f
                        onChip = start.y < size.height * 0.3f
                    },
                    onDragEnd = { acc = 0f },
                    onDragCancel = { acc = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        acc += dragAmount
                        if (onChip && structureSteps.isNotEmpty() && !showSearch && !anySheet &&
                            appToLaunch == null && !showWallpaperDialog
                        ) {
                            if (acc < -150f) {
                                homeViewModel.moveStructureStep(1)
                                acc = 0f
                            } else if (acc > 150f) {
                                homeViewModel.moveStructureStep(-1)
                                acc = 0f
                            }
                        }
                    }
                )
            }
    ) {

        // Current structure step — tap to advance through the day's flow
        if (structureSteps.isNotEmpty() &&
            !showSearch && !showSettings && !showHistory && !showOneThing && !showStructure
        ) {
            val idx = structureIndex.coerceIn(0, structureSteps.lastIndex)
            val chipShape = RoundedCornerShape(20.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = statusBarHeight + 12.dp)
                    .liquidGlass(hazeState, chipShape, LiquidGlassStyle.Thin)
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
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // 1 Thing task display — single glass sheet, rises like the structure editor
        AnimatedVisibility(
            visible = showOneThing && showTaskDisplay && oneThingTask != null,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val task = taskForSheet
            if (task != null) {
                DraggableSheet(
                    onDismiss = closeOneThing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = sheetTopGap)
                        .fillMaxHeight()
                ) {
                    OneThingTaskDisplay(
                        task = task,
                        onMainTaskCompleted = onMainTaskCompleted,
                        onUpdateSubtask = { subtaskId, isCompleted ->
                            homeViewModel.updateSubtaskState(subtaskId, isCompleted)
                        },
                        onDismiss = closeOneThing,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 1 Thing input card
        AnimatedVisibility(
            visible = showOneThing && showInputCard,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DraggableSheet(
                onDismiss = closeOneThing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = sheetTopGap)
            ) {
                OneThingInputCard(
                    onTaskSet = onTaskSet,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Settings sheet
        AnimatedVisibility(
            visible = showSettings,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DraggableSheet(
                onDismiss = { showSettings = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = sheetTopGap)
                    .fillMaxHeight()
            ) {
                SettingsSheet(
                    modifier = Modifier.fillMaxSize(),
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
        }

        // Intention history sheet
        AnimatedVisibility(
            visible = showHistory,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DraggableSheet(
                onDismiss = {
                    showHistory = false
                    showSettings = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = sheetTopGap)
                    .fillMaxHeight()
            ) {
                IntentionHistorySheet(
                    intentions = allIntentions,
                    onDismiss = {
                        showHistory = false
                        showSettings = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Structure editor sheet
        AnimatedVisibility(
            visible = showStructure,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DraggableSheet(
                onDismiss = { showStructure = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = sheetTopGap)
                    .fillMaxHeight()
            ) {
                StructureSheet(
                    steps = structureSteps,
                    onDismiss = { showStructure = false },
                    onSave = { steps ->
                        homeViewModel.setStructureSteps(steps)
                        showStructure = false
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Mini 1 Thing island — hidden while the dial is out and under any sheet or search
        if (!showSearch && !showSettings && !showOneThing && !showHistory && !showStructure && !dialVisible) {
            val task = oneThingTask
            val done = task?.subTasks?.count { it.isCompleted } ?: 0
            val islandShape = RoundedCornerShape(18.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 72.dp)
                    .liquidGlass(hazeState, islandShape, LiquidGlassStyle.Thin)
                    .clickable {
                        showOneThing = true
                        showSettings = false
                        showHistory = false
                        if (task != null) {
                            showTaskDisplay = true
                            showInputCard = false
                        } else {
                            showInputCard = true
                            showTaskDisplay = false
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .clearAndSetSemantics {
                        contentDescription = if (task == null) {
                            "1 Thing not set. Tap to set your focus."
                        } else {
                            "1 Thing: ${task.mainTask}, $done of ${task.subTasks.size} steps done. Tap to open."
                        }
                    }
            ) {
                Text(
                    text = "1",
                    color = Color(0xFFFF4D00),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (task == null) {
                    Text(
                        text = "Set your 1 Thing",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = task.mainTask,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 220.dp)
                    )
                    if (task.subTasks.isNotEmpty()) {
                        Text(
                            text = "$done/${task.subTasks.size}",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }

        // Bottom dot — the collapsed dial's handle, and it stays put. Tap reveals the dial; the
        // arc is the navigation, so the dot no longer slides toward a destination.
        AnimatedVisibility(
            visible = !dialVisible && !showSearch && !anySheet,
            enter = fadeIn(tween(180, delayMillis = 120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .size(56.dp)
                    // No ripple: the default (dark) indication filled the square touch bounds
                    // behind the round dot.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { dialVisible = true }
                    .semantics { contentDescription = "App dial. Tap to open." },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        // A drop shadow, not a denser fill: the only resting control has to
                        // read on a bright photo without turning into a button.
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.40f),
                            spotColor = Color.Black.copy(alpha = 0.40f)
                        )
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f))
                )
            }
        }

        // The dial — the dock, re-seated on an arc. Swipe up from the bottom band, or tap the
        // dot; it retracts by itself after 3s of no touch.
        ArcDial(
            visible = dialVisible,
            entries = dialEntries,
            onAction = { action ->
                dialVisible = false
                when (action) {
                    DialAction.ONE_THING -> {
                        if (UserPreferencesRepository.FEEDBACK_TEST_TRIGGER_ON_ONE_THING) {
                            onRequestFeedbackPrompt()
                        }
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
                    DialAction.STRUCTURE -> openStructure()
                    DialAction.SEARCH -> openSearch()
                    DialAction.SETTINGS -> {
                        showSettings = true
                        showHistory = false
                        showOneThing = false
                        showInputCard = false
                        showTaskDisplay = false
                    }
                    DialAction.ADD_APPS -> {
                        openSearch()
                        android.widget.Toast.makeText(
                            context,
                            "Long-press an app or file in search, then Add to Essentials.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onApp = { app ->
                dialVisible = false
                launchApp(app)
            },
            onRequestHide = { dialVisible = false },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Search overlay — a floating glass card that rises from the bottom; the bottom
        // handle's rightward swipe is its only trigger now that the edge artifact is gone.
        UniversalSearchOverlay(
            visible = showSearch,
            topPad = sheetTopGap,
            searchQueryFlow = homeViewModel.searchQuery,
            onSearchQueryChange = { homeViewModel.onSearchQueryChanged(it) },
            apps = uiState.apps,
            contactsFlow = homeViewModel.contactResults,
            filesFlow = homeViewModel.fileResults,
            pinnedFilesFlow = homeViewModel.pinnedFiles,
            onDismiss = {
                showSearch = false
                // Query is preserved; Pixel-launcher behavior — a mis-tap on empty glass
                // should not destroy multi-character input. Clear happens on explicit
                // Cancel (below), Back (BackHandler), or after launching a result.
            },
            onCancel = {
                showSearch = false
                homeViewModel.onSearchQueryChanged("")
            },
            onAppClick = { app ->
                showSearch = false
                homeViewModel.onSearchQueryChanged("")
                launchApp(app)
            },
            onTogglePinned = { homeViewModel.togglePinnedApp(it) },
            onTogglePinnedFile = { homeViewModel.togglePinnedFile(it) },
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
            onRefreshResults = { homeViewModel.refreshSearch() },
            focusModeEnabled = focusModeEnabled,
            frequentApps = frequentApps,
            isSearchingFlow = homeViewModel.isSearching,
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

