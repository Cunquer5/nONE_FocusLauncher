package com.example.nonelauncher.ui.modern

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.parseOneThingTaskWithState
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import com.example.nonelauncher.ui.home.HomeViewModel
import com.example.nonelauncher.ui.modern.components.*
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardDisplayStyle
import com.example.nonelauncher.ui.theme.BoardDivider
import com.example.nonelauncher.ui.theme.BoardLabelStyle
import com.example.nonelauncher.ui.theme.BoardMonoStyle
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardRowState
import com.example.nonelauncher.ui.theme.bottomEnter
import com.example.nonelauncher.ui.theme.bottomExit
import com.example.nonelauncher.ui.theme.pinstripes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ModernHomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onOpenSettings: () -> Unit = {},
    onRequestFeedbackPrompt: () -> Unit = {},
) {
    val context = LocalContext.current
    // Board follows the device language and 12/24-hour choice; a hardcoded "HH:mm" read as
    // 21:04 to users who keep a 12-hour phone.
    val locale = Locale.getDefault()
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val boardDate = remember(locale) { DateTimeFormatter.ofPattern("EEE dd MMM", locale) }
    val boardClock = remember(locale, is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm", locale)
    }
    val uiState by homeViewModel.uiState.collectAsState()
    val allIntentions by homeViewModel.allIntentions.collectAsState(initial = emptyList())
    val structureSteps by homeViewModel.structureSteps.collectAsState()
    val structureIndex by homeViewModel.structureIndex.collectAsState()
    val focusModeEnabled by homeViewModel.focusModeEnabled.collectAsState()
    val frequentPackages by homeViewModel.frequentPackages.collectAsState()
    val frequentApps = remember(frequentPackages, uiState.apps) {
        frequentPackages.mapNotNull { pkg -> uiState.apps.firstOrNull { it.packageName == pkg } }
    }
    val oneThingTask = uiState.oneThingTaskWithState

    // Board clock: a minute is the finest unit the surface shows, so a slow tick is enough.
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            kotlinx.coroutines.delay(15_000)
        }
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

    // The board's now band and the dial's 1 Thing seat both land here.
    val openOneThing: () -> Unit = {
        showOneThing = true
        showSettings = false
        showHistory = false
        showStructure = false
        showSearch = false
        if (oneThingTask != null) {
            showTaskDisplay = true
            showInputCard = false
        } else {
            showInputCard = true
            showTaskDisplay = false
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

    // The dial's seats: the fixed board actions first, then whatever the user pinned.
    val dialPinnedApps = remember(uiState.apps) { uiState.apps.filter { it.isPinned } }
    val dialEntries = remember(dialPinnedApps) {
        buildList {
            add(DialEntry.Action(DialAction.ONE_THING, "1 Thing"))
            add(DialEntry.Action(DialAction.STRUCTURE, "Structure"))
            add(DialEntry.Action(DialAction.SEARCH, "Search"))
            add(DialEntry.Action(DialAction.SETTINGS, "Settings"))
            add(DialEntry.Action(DialAction.PHONE, "Phone"))
            add(DialEntry.Action(DialAction.ADD_APPS, "Add apps"))
            dialPinnedApps.forEach { add(DialEntry.App(it)) }
        }
    }

    // Backdrop behind every raised surface. Without it the board disappeared and the sheet
    // floated over bare wallpaper, which is most of why the screens read as unfinished.
    val closeTopSurface: () -> Unit = {
        when {
            showHistory -> showHistory = false
            showSettings -> showSettings = false
            showStructure -> showStructure = false
            showOneThing -> {
                showOneThing = false
                showInputCard = false
                showTaskDisplay = false
            }
            else -> dialVisible = false
        }
    }

    val scrimVisible = anySheet || showSearch

    // AnimatedVisibility keeps its content composed while it slides out, and the disposal timer
    // nulls the live task mid-exit, so the sheet keeps drawing the task it was already showing.
    var retainedTask by remember { mutableStateOf<OneThingTask?>(null) }
    LaunchedEffect(oneThingTask) {
        if (oneThingTask != null) retainedTask = oneThingTask
    }
    val taskForSheet = oneThingTask ?: retainedTask

    // The keyboard replaces the structure sheet's 120dp gap when it appears. Keeping both
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
            .pointerInput(showSearch, anySheet, dialVisible) {
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
                        if (nearBottom && up > 150f && !showSearch && !anySheet && !dialVisible) {
                            dialVisible = true
                            up = 0f
                        }
                    }
                )
            }
    ) {

        // Dims the wallpaper behind the status bar so system icons stay legible on a bright photo
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(96.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )

        // Departure board — the day's log at a glance. Tap a row to make it the current step;
        // the current row keeps the orange berth, everything past it dims as history.
        // It stays composed under every sheet: a board that vanishes and replays its flap
        // animation on each open was the loudest "unfinished" tell.
        run {
            val lastIndex = structureSteps.lastIndex.coerceAtLeast(0)
            val currentIndex = structureIndex.coerceIn(0, lastIndex)
            val totalSteps = structureSteps.size
            val remaining = (totalSteps - currentIndex - 1).coerceAtLeast(0)
            val oneThingDone = oneThingTask?.isMainCompleted == true

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoardPanel(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        // Header: brand left, date · clock · remaining right — one 44dp rail.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NONE",
                                color = Board.Paint.copy(alpha = 0.8f),
                                style = BoardLabelStyle.copy(fontSize = 10.sp, letterSpacing = 3.sp)
                            )
                            Text(
                                text = buildString {
                                    append(now.format(boardDate).uppercase(locale))
                                    append(" · ")
                                    append(now.format(boardClock))
                                    if (totalSteps > 0) {
                                        append(" · ")
                                        append(remaining)
                                        append(" remaining")
                                    }
                                },
                                color = Board.Steel,
                                style = BoardMonoStyle.copy(fontSize = 10.sp, lineHeight = 12.sp)
                            )
                        }
                        BoardDivider(color = Board.HairFaint)

                        // White paint band: the 1 Thing is the board's current departure, with
                        // the action berth running its full height on the right.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                // The white band floats inside the slab: breathing room above and
                                // below lets its corners actually round instead of meeting the
                                // header and rail as a hard stripe.
                                .padding(vertical = 10.dp)
                                // Intrinsic pass keeps the action berth full height while the
                                // band still grows if the task runs to two lines at large scale.
                                .height(IntrinsicSize.Min)
                                .clip(Board.cell)
                                .background(Board.Paint)
                                .pinstripes(color = Board.Ink.copy(alpha = 0.05f))
                                .clickable { openOneThing() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 70.dp)
                                    .padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 10.dp)
                            ) {
                                Text(
                                    text = (if (oneThingDone) "Done today" else "The One Thing")
                                        .uppercase(locale),
                                    color = Board.Ink.copy(alpha = 0.55f),
                                    style = BoardLabelStyle.copy(
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp,
                                        letterSpacing = 1.9.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = (oneThingTask?.mainTask ?: "Set today's one thing")
                                        .uppercase(locale),
                                    color = if (oneThingTask == null) Board.Ink.copy(alpha = 0.6f)
                                    else Board.Ink,
                                    style = BoardDisplayStyle,
                                    textDecoration = if (oneThingDone) TextDecoration.LineThrough
                                    else TextDecoration.None,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(94.dp)
                                    .fillMaxHeight()
                                    // Inset so the action keeps a real corner radius: a berth
                                    // flush to the band edge could only ever be square.
                                    .padding(end = 12.dp, top = 12.dp, bottom = 12.dp)
                                    .clip(Board.cell)
                                    .background(Board.Action)
                                    .clickable { openOneThing() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (oneThingTask == null) "SET" else "OPEN",
                                    // Ink on orange clears AA at this size; white on orange only
                                    // reaches 3.3:1 and this cell is below large-text size.
                                    color = Board.Ink,
                                    style = BoardLabelStyle.copy(fontSize = 10.sp, letterSpacing = 2.sp)
                                )
                            }
                        }

                        // Column rail: fixed figure and status widths keep the data on a grid.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NO.",
                                color = Board.Steel,
                                style = BoardLabelStyle.copy(fontSize = 8.sp, lineHeight = 10.sp, letterSpacing = 1.9.sp),
                                modifier = Modifier.width(74.dp)
                            )
                            Text(
                                text = "STEP",
                                color = Board.Steel,
                                style = BoardLabelStyle.copy(fontSize = 8.sp, lineHeight = 10.sp, letterSpacing = 1.9.sp),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "STATUS",
                                color = Board.Steel,
                                style = BoardLabelStyle.copy(fontSize = 8.sp, lineHeight = 10.sp, letterSpacing = 1.9.sp),
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(92.dp)
                            )
                        }
                        BoardDivider(color = Board.HairFaint)

                        if (totalSteps == 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 62.dp)
                                    .pinstripes()
                                    .clickable { openStructure() }
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+",
                                    color = Board.Action,
                                    style = BoardDisplayStyle.copy(fontSize = 18.sp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Set today's structure",
                                    color = Board.Paint.copy(alpha = 0.85f),
                                    style = BoardBodyStyle.copy(fontSize = 13.sp)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // Four 62dp rows plus their seams: the reference shows four
                                    // departures, and the crop leaves room for the lamp on short
                                    // screens. Longer structures scroll inside the same frame.
                                    .heightIn(max = 251.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                structureSteps.forEachIndexed { index, step ->
                                    val state = when {
                                        index < currentIndex -> BoardRowState.DONE
                                        index == currentIndex -> BoardRowState.NOW
                                        index == currentIndex + 1 -> BoardRowState.NEXT
                                        else -> BoardRowState.LATER
                                    }
                                    BoardStepRow(
                                        index = index,
                                        text = step,
                                        state = state,
                                        onClick = {
                                            homeViewModel.moveStructureStep(index - structureIndex)
                                        }
                                    )
                                    if (index < structureSteps.lastIndex) {
                                        BoardDivider(color = Board.HairFaint)
                                    }
                                }
                            }
                        }

                        if (totalSteps > 0) {
                            BoardDivider(color = Board.HairFaint)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 34.dp)
                                    .background(Board.Well)
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$totalSteps steps · $currentIndex done".uppercase(locale),
                                    color = Board.Steel,
                                    style = BoardMonoStyle.copy(
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp,
                                        letterSpacing = 1.2.sp
                                    )
                                )
                                Text(
                                    text = "TAP A ROW",
                                    color = Board.Steel,
                                    style = BoardLabelStyle.copy(
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp,
                                        letterSpacing = 1.6.sp
                                    )
                                )
                            }
                        }
                    }
                }

            }
        }

        // Scrim under every raised surface: keeps the board as context, and a tap outside
        // the sheet closes it instead of silently hiding the dial.
        AnimatedVisibility(
            visible = scrimVisible,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(140)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { closeTopSurface() }
                    .clearAndSetSemantics { }
            )
        }

        // 1 Thing task display — single board sheet, rises like the structure editor
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
                    onSetMainCompleted = { homeViewModel.setMainTaskCompleted(it) },
                    onNewTask = {
                        showTaskDisplay = false
                        showInputCard = true
                    },
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
                        .statusBarsPadding()
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
                onDismiss = {
                    showOneThing = false
                    showInputCard = false
                    showTaskDisplay = false
                },
                modifier = Modifier
                    .padding(bottom = sheetPad)
                    .padding(horizontal = 8.dp)
                    .statusBarsPadding()
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
                    .fillMaxHeight(0.85f)
                    .statusBarsPadding(),
                recentIntentions = uiState.recentIntentions,
                focusModeEnabled = focusModeEnabled,
                onToggleFocusMode = { homeViewModel.setFocusModeEnabled(it) },
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
                    .statusBarsPadding()
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
                    .statusBarsPadding()
            )
        }

        // Bottom lamp — the dial's only handle. Tap it (or swipe up from the bottom band) to
        // reveal the dial; Structure and Search live on that dial, not on separate gestures.
        AnimatedVisibility(
            visible = !dialVisible && !showSearch && !anySheet,
            enter = fadeIn(tween(180, delayMillis = 120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Caption on its own scrim so it reads over any wallpaper. One line: the lamp
                // has one gesture now.
                Text(
                    text = "TAP · DIAL",
                    color = Board.Paint.copy(alpha = 0.9f),
                    style = BoardLabelStyle.copy(fontSize = 8.sp, lineHeight = 11.sp, letterSpacing = 1.9.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(Board.capsule)
                        .background(Board.Ink.copy(alpha = 0.82f))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .clearAndSetSemantics { }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        // No ripple: the default (dark) indication filled the square touch
                        // bounds behind the dot.
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { dialVisible = true }
                        .semantics {
                            contentDescription = "Board dial. Tap to open."
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Soft bloom around the lamp, not a flat colored ring.
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            // A dark rim keeps the lamp readable on a white or pale wallpaper.
                            .background(Color.White.copy(alpha = 0.82f))
                            .border(1.dp, Board.Ink.copy(alpha = 0.35f), CircleShape)
                    )
                }
            }
        }

        // The dial — the old dock re-seated on an arc. Tap the lamp or swipe up from the bottom
        // band to reveal it; it retracts by itself after 3s of no touch.
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
                        openOneThing()
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
                    DialAction.PHONE -> {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "No phone dialer found", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    DialAction.ADD_APPS -> {
                        openSearch()
                        android.widget.Toast.makeText(
                            context,
                            "Long-press an app in search, then Add to Essentials.",
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )

        // Search overlay — a floating board card that rises from the bottom; opened from the
        // dial's Search seat.
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
                // Query is preserved; Pixel-launcher behavior — a mis-tap on empty board
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
 * One departure row: figure, step, status cell. The status restyles the row in place —
 * the grid never moves as the day advances.
 */
@Composable
private fun BoardStepRow(
    index: Int,
    text: String,
    state: BoardRowState,
    onClick: () -> Unit
) {
    val statusText = when (state) {
        BoardRowState.DONE -> "DONE"
        BoardRowState.NOW -> "NOW"
        BoardRowState.NEXT -> "NEXT"
        BoardRowState.LATER -> "SCHEDULED"
    }
    // Restyle in place, not in one frame: the berth tint and status cell cross over as the
    // current step moves, which is the board's whole signature move.
    val rowBg by animateColorAsState(
        if (state == BoardRowState.NOW) Board.Action.copy(alpha = 0.05f) else Color.Transparent,
        tween(220),
        label = "rowBg"
    )
    val isDone = state == BoardRowState.DONE
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .background(rowBg)
            .pinstripes()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 14.dp)
            .semantics {
                contentDescription = "Step ${index + 1}, $text, ${statusText.lowercase()}. Tap to set current."
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%02d".format(index + 1),
            color = when {
                state == BoardRowState.NOW -> Board.Action
                isDone -> Board.Paint.copy(alpha = 0.28f)
                else -> Board.Paint
            },
            style = BoardMonoStyle,
            modifier = Modifier.width(74.dp)
        )
        Text(
            text = text.uppercase(Locale.getDefault()),
            color = if (isDone) Board.Paint.copy(alpha = 0.28f) else Board.Paint,
            style = BoardDisplayStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                letterSpacing = 0.7.sp
            ),
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier.width(92.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Only the current and next departures take a border; the rest read as plain
            // columns, which is what keeps the list scanable at a glance.
            val boxed = state == BoardRowState.NOW || state == BoardRowState.NEXT
            Text(
                text = statusText,
                color = when (state) {
                    BoardRowState.NOW -> Board.Action
                    BoardRowState.NEXT -> Board.Paint
                    else -> Board.Steel
                },
                style = BoardLabelStyle.copy(
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    letterSpacing = 1.6.sp
                ),
                textAlign = TextAlign.End,
                modifier = if (boxed) {
                    Modifier
                        .border(
                            width = 1.dp,
                            color = if (state == BoardRowState.NOW) Board.Action
                            else Board.Paint.copy(alpha = 0.24f),
                            shape = Board.capsule
                        )
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                } else {
                    Modifier
                }
            )
        }
    }
}
