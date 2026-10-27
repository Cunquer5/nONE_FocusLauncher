package com.example.nonelauncher.ui.modern

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
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
import com.example.nonelauncher.ui.theme.OrangePrimary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import kotlin.math.abs

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

    val openStructure: () -> Unit = {
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

    // Left unused, the dock collapses itself back to the small handle; an open sheet keeps it anchored.
    // dockTouchTick re-arms the window on dock touches that change no other state (Phone).
    var dockTouchTick by remember { mutableStateOf(0) }
    LaunchedEffect(dockVisible, anySheet, dockTouchTick) {
        if (dockVisible && !anySheet) {
            kotlinx.coroutines.delay(6_000)
            dockVisible = false
        }
    }

    // The keyboard replaces the structure sheet's 120dp dock gap when it appears. Keeping both
    // would ride the root imePadding and fling the tall editor off the top edge — the pad
    // shrinks while the IME is up, so the card settles a reasonable way up, not all the way.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    // Shared by every card that owns a text field (structure editor, search)
    val sheetPad by animateDpAsState(
        if (imeVisible) 16.dp else 120.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sheetPad"
    )

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
            // The step chip's top band is the only horizontal screen gesture left: swipe it
            // right for the next step, left for the previous one. Search and Structure are
            // deliberately not edge gestures anymore — the bottom handle owns both.
            .pointerInput(showSearch, anySheet, structureSteps.size) {
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
                        if (onChip && structureSteps.isNotEmpty() && !showSearch && !anySheet) {
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

        // 1 Thing task display — single glass sheet, rises like the structure editor
        AnimatedVisibility(
            visible = showOneThing && showTaskDisplay && oneThingTask != null,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val task = taskForSheet
            if (task != null) {
                OneThingTaskDisplay(
                    task = task,
                    onMainTaskCompleted = onMainTaskCompleted,
                    onUpdateSubtask = { subtaskId, isCompleted ->
                        homeViewModel.updateSubtaskState(subtaskId, isCompleted)
                    },
                    onDismiss = {
                        showOneThing = false
                        showTaskDisplay = false
                        showInputCard = false
                    },
                    modifier = Modifier
                        .padding(bottom = 120.dp)
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight(0.85f)
                )
            }
        }

        // 1 Thing input card
        AnimatedVisibility(
            visible = showOneThing && showInputCard,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            OneThingInputCard(
                onTaskSet = onTaskSet,
                modifier = Modifier
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 16.dp)
            )
        }

        // Settings sheet
        AnimatedVisibility(
            visible = showSettings,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SettingsSheet(
                modifier = Modifier
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
        AnimatedVisibility(
            visible = showHistory,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            IntentionHistorySheet(
                intentions = allIntentions,
                onDismiss = {
                    showHistory = false
                    showSettings = true
                },
                modifier = Modifier
                    .padding(bottom = 120.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(0.85f)
            )
        }

        // Structure editor sheet
        AnimatedVisibility(
            visible = showStructure,
            enter = bottomEnter,
            exit = bottomExit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            StructureSheet(
                steps = structureSteps,
                onDismiss = { showStructure = false },
                onSave = { steps ->
                    homeViewModel.setStructureSteps(steps)
                    showStructure = false
                },
                modifier = Modifier
                    .padding(bottom = sheetPad)
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(0.8f)
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

        // Bottom dot — the collapsed dock's only handle. Tap reveals the dock; a horizontal
        // drag pulls the dot toward the surface it opens (left Structure, right search) and
        // names that destination as it travels. Past 40dp it goes orange, ticks once and
        // commits on release; short of that, it springs home.
        AnimatedVisibility(
            visible = !dockShown && !showSearch,
            enter = fadeIn(tween(180, delayMillis = 120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val density = LocalDensity.current
            val haptic = LocalHapticFeedback.current
            var dragX by remember { mutableFloatStateOf(0f) } // dp; negative = left
            var isDragging by remember { mutableStateOf(false) }
            var armedSide by remember { mutableIntStateOf(0) }
            val threshold = 40f
            val progress = (abs(dragX) / threshold).coerceIn(0f, 1f)
            val eased = progress * progress * (3f - 2f * progress)

            // Released short of the threshold, the dot falls back to rest under its own spring.
            LaunchedEffect(isDragging) {
                if (!isDragging && dragX != 0f) {
                    animate(
                        initialValue = dragX,
                        targetValue = 0f,
                        // Critically damped: an overshoot would flip the dot's stretch and
                        // flash the opposite direction's chip on the way home.
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 550f)
                    ) { value, _ ->
                        dragX = value
                    }
                }
            }

            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // Only the active direction exists while the finger is down, sliding out
                // from behind the dot; it clears on release instead of riding the spring back.
                if (isDragging && dragX < -2f) {
                    HandleDestinationChip(
                        label = "Structure",
                        side = -1,
                        progress = eased,
                        armed = armedSide == -1
                    )
                } else if (isDragging && dragX > 2f) {
                    HandleDestinationChip(
                        label = "Search",
                        side = 1,
                        progress = eased,
                        armed = armedSide == 1
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        // No ripple: the default (dark) indication filled the square touch
                        // bounds behind the round dot. The stretch is the press feedback.
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { dockVisible = true }
                        // After clickable so the drag detector sees the Main pass first: a swipe is
                        // consumed and cancels the tap, while a still finger still opens the dock.
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    armedSide = 0
                                    dragX = 0f
                                    isDragging = true
                                },
                                onDragCancel = {
                                    armedSide = 0
                                    isDragging = false
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val deltaDp = with(density) { dragAmount.toDp().value }
                                    dragX = (dragX + deltaDp).coerceIn(-72f, 72f)
                                    val nowArmed = when {
                                        dragX <= -threshold -> -1
                                        dragX >= threshold -> 1
                                        else -> 0
                                    }
                                    if (nowArmed != 0 && nowArmed != armedSide) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    armedSide = nowArmed
                                },
                                onDragEnd = {
                                    val target = armedSide
                                    armedSide = 0
                                    isDragging = false
                                    when (target) {
                                        -1 -> openStructure()
                                        1 -> openSearch()
                                    }
                                }
                            )
                        }
                        // Swipes have no TalkBack equivalent, so both destinations are also
                        // exposed as accessibility actions on the same node.
                        .semantics {
                            contentDescription =
                                "Dock. Tap to open. Swipe left for Structure, swipe right for search."
                            customActions = listOf(
                                CustomAccessibilityAction("Open Structure") {
                                    openStructure()
                                    true
                                },
                                CustomAccessibilityAction("Open Search") {
                                    openSearch()
                                    true
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Direction reads from the dot's own body: it leans and stretches toward
                    // the surface the finger is pulling it to, anchored on the far edge.
                    val direction = if (dragX < 0f) -1f else 1f
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                translationX = (dragX * 0.55f).coerceIn(-24f, 24f).dp.toPx()
                                scaleX = 1f + 0.5f * eased
                                scaleY = 1f - 0.12f * eased
                                transformOrigin = TransformOrigin(
                                    if (direction < 0f) 1f else 0f,
                                    0.5f
                                )
                            }
                            .clip(CircleShape)
                            .background(
                                if (armedSide != 0) OrangePrimary
                                else Color.White.copy(alpha = 0.45f + 0.4f * eased)
                            )
                    )
                }
            }
        }

        // Dock — hidden until swipe-up, bottom handle, or an open sheet reveals it.
        // Spring arrival decelerates naturally; exit is a stiffer, faster retract.
        AnimatedVisibility(
            visible = dockShown,
            enter = bottomEnter,
            exit = bottomExit,
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

        // Search overlay — a floating glass card that rises from the bottom; the bottom
        // handle's rightward swipe is its only trigger now that the edge artifact is gone.
        UniversalSearchOverlay(
            visible = showSearch,
            bottomPad = sheetPad,
            searchQueryFlow = homeViewModel.searchQuery,
            onSearchQueryChange = { homeViewModel.onSearchQueryChanged(it) },
            apps = uiState.apps,
            contactsFlow = homeViewModel.contactResults,
            filesFlow = homeViewModel.fileResults,
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

/**
 * The destination that a handle swipe is pulling toward. It rides out from behind the dot as
 * the drag grows and turns orange once the gesture is armed, so the target is named before the
 * finger lifts rather than discovered after the sheet opens.
 */
@Composable
private fun HandleDestinationChip(
    label: String,
    side: Int,
    progress: Float,
    armed: Boolean,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .offset(x = (side * 86f * progress).dp)
            .alpha(progress)
            .clip(shape)
            .background(Color(0xFF0F0F0F).copy(alpha = 0.62f))
            .border(
                width = 1.dp,
                color = if (armed) OrangePrimary else Color.White.copy(alpha = 0.18f),
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            // Transient gesture feedback; the handle itself carries the accessibility actions.
            .clearAndSetSemantics { }
    ) {
        Text(
            text = label,
            color = if (armed) OrangePrimary else Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
