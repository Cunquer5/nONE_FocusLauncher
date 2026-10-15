package com.example.nonelauncher.ui.modern

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
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

private enum class Zone { None, Top, Left, Right }

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
    val focusModeEnabled by homeViewModel.focusModeEnabled.collectAsState()
    val isSearching by homeViewModel.isSearching.collectAsState()
    val contactResults by homeViewModel.contactResults.collectAsState()
    val fileResults by homeViewModel.fileResults.collectAsState()
    val frequentPackages by homeViewModel.frequentPackages.collectAsState()
    val frequentApps = remember(frequentPackages, uiState.apps) {
        frequentPackages.mapNotNull { pkg -> uiState.apps.firstOrNull { it.packageName == pkg } }
    }
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
    // Sheets anchor above the dock; the structure sheet deliberately does NOT reveal the dock
    val dockShown = dockVisible || showSettings || showOneThing || showHistory

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

    // Left unused, the dock collapses itself back to the small handle; an open sheet keeps it anchored.
    // dockTouchTick re-arms the window on dock touches that change no other state (Phone).
    var dockTouchTick by remember { mutableStateOf(0) }
    LaunchedEffect(dockVisible, anySheet, dockTouchTick) {
        if (dockVisible && !anySheet) {
            kotlinx.coroutines.delay(6_000)
            dockVisible = false
        }
    }

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
            // Swipe up near the bottom reveals the hidden dock — bottom band only, not anywhere
            .pointerInput(showSearch, anySheet, dockShown) {
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
                        if (nearBottom && up > 150f && !showSearch && !dockShown) {
                            dockVisible = true
                            up = 0f
                        }
                    }
                )
            }
            // Edge-local horizontal swipes: right band = open search, left band = open structure,
            // top band (the step chip's zone) = next/previous step. Middle of the screen does nothing.
            .pointerInput(showSearch, anySheet, structureSteps.size) {
                var acc = 0f
                var zone = Zone.None
                detectHorizontalDragGestures(
                    onDragStart = { start ->
                        acc = 0f
                        zone = when {
                            start.x > size.width * 0.75f -> Zone.Right
                            start.x < size.width * 0.25f -> Zone.Left
                            start.y < size.height * 0.3f -> Zone.Top
                            else -> Zone.None
                        }
                    },
                    onDragEnd = { acc = 0f },
                    onDragCancel = { acc = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        acc += dragAmount
                        when {
                            zone == Zone.Right && acc < -150f && !showSearch && !anySheet -> {
                                showSearch = true
                                acc = 0f
                            }
                            zone == Zone.Left && acc > 150f && !showSearch && !anySheet -> {
                                showStructure = true
                                showSettings = false
                                showHistory = false
                                showOneThing = false
                                homeViewModel.onSearchQueryChanged("")
                                acc = 0f
                            }
                            zone == Zone.Top && structureSteps.isNotEmpty() && !showSearch && !anySheet -> {
                                if (acc < -150f) {
                                    homeViewModel.moveStructureStep(1)
                                    acc = 0f
                                } else if (acc > 150f) {
                                    homeViewModel.moveStructureStep(-1)
                                    acc = 0f
                                }
                            }
                        }
                    }
                )
            }
    ) {

        // Right-edge handle — teaches/taps the swipe-from-right search gesture.
        // 48dp-wide touch target wraps a 20dp visual pill so accessibility minimum is met.
        // Indication is null on the wrapper: an unclipped ripple would draw a rectangle
        // around the invisible touch box. The pill fades with surface state, like the left rail.
        AnimatedVisibility(
            visible = !showSearch && !anySheet,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(96.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showSearch = true }
                    .clearAndSetSemantics { contentDescription = "Search apps, contacts, files" }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .width(20.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
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
                    color = Color.White.copy(alpha = 0.75f),
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

        // Structure access rail — hidden while the structure section, 1 Thing, or search is open
        AnimatedVisibility(
            visible = !showOneThing && !showSearch && !showStructure,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier
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
        }

        // Mini 1 Thing island — rides up above the dock when it expands; hidden under sheets/search
        if (!showSearch && !showSettings && !showOneThing && !showHistory && !showStructure) {
            val islandBottom by animateDpAsState(
                if (dockVisible) 120.dp else 72.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "islandShift"
            )
            val task = oneThingTask
            val done = task?.subTasks?.count { it.isCompleted } ?: 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = islandBottom)
                    .clip(RoundedCornerShape(18.dp))
                    .hazeChild(state = hazeState, blurRadius = 12.dp)
                    .background(Color(0xFF0F0F0F).copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
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
                            text = " $done/${task.subTasks.size}",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }

        // Bottom circle — small collapsed-dock handle (48 dp touch target, 20 dp visual)
        AnimatedVisibility(
            visible = !dockShown && !showSearch,
            enter = fadeIn(tween(180, delayMillis = 120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .size(48.dp)
                    .clickable { dockVisible = true }
                    .clearAndSetSemantics { contentDescription = "Show dock" },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.45f))
                )
            }
        }

        // Dock — hidden until swipe-up, bottom handle, or an open sheet reveals it.
        // Spring arrival decelerates naturally; exit is a stiffer, faster retract.
        AnimatedVisibility(
            visible = dockShown,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                initialOffsetY = { it }
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                animationSpec = spring(dampingRatio = 1f, stiffness = 700f),
                targetOffsetY = { it }
            ) + fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassDock(
            hazeState = hazeState,
            activeItem = activeTab,
            modifier = Modifier,
            onPhoneClick = {
                dockTouchTick++
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "No phone dialer found", android.widget.Toast.LENGTH_SHORT).show()
                }
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
            searchQueryFlow = homeViewModel.searchQuery,
            onSearchQueryChange = { homeViewModel.onSearchQueryChanged(it) },
            apps = uiState.apps,
            contacts = contactResults,
            files = fileResults,
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
            onTogglePinned = { homeViewModel.togglePinnedApp(it) },
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
            isSearching = isSearching,
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
