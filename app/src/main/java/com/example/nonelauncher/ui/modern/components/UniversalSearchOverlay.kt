package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.bottomEnter
import com.example.nonelauncher.ui.theme.bottomExit
import com.example.nonelauncher.ui.theme.boardPress
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.ContactModel
import com.example.nonelauncher.data.model.FileModel
import com.example.nonelauncher.data.repository.FileRepository
import com.example.nonelauncher.ui.components.AppIcon
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardCloseButton
import com.example.nonelauncher.ui.theme.BoardDivider
import com.example.nonelauncher.ui.theme.BoardLabel
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.BoardQuietButton

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.StateFlow

/**
 * Search as a floating board card above the wallpaper, the same material as the structure editor.
 * Shown by the right-edge artifact, a rightward swipe on the collapsed-dock handle, or Back.
 * Automatically focuses the search field when visible.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UniversalSearchOverlay(
    visible: Boolean,
    searchQueryFlow: StateFlow<String>,
    onSearchQueryChange: (String) -> Unit,
    apps: List<AppModel>,
    contactsFlow: StateFlow<List<ContactModel>>,
    filesFlow: StateFlow<List<FileModel>>,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onAppClick: (AppModel) -> Unit,
    onTogglePinned: (AppModel) -> Unit,
    onContactClick: (ContactModel) -> Unit,
    onFileClick: (FileModel) -> Unit,
    focusModeEnabled: Boolean = false,
    frequentApps: List<AppModel> = emptyList(),
    isSearchingFlow: StateFlow<Boolean>,
    onRefreshResults: () -> Unit = {},
    bottomPad: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    // Collected here, not in the home screen's scope: a streaming contact/file result then
    // recomposes only the overlay subtree, not the whole home behind the blur.
    val contacts by contactsFlow.collectAsState()
    val files by filesFlow.collectAsState()
    val isSearching by isSearchingFlow.collectAsState()

    // Permission state is re-read from the PackageManager after every dialog and every Settings
    // visit — grants can also happen outside the app.
    val activity = context as? android.app.Activity
    val isGranted: (String) -> Boolean = {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    var hasContactPermission by remember { mutableStateOf(isGranted(android.Manifest.permission.READ_CONTACTS)) }
    var hasFilePermission by remember { mutableStateOf(FileRepository.canReadFiles(context)) }
    val refreshPermissionState: () -> Unit = {
        hasContactPermission = isGranted(android.Manifest.permission.READ_CONTACTS)
        hasFilePermission = FileRepository.canReadFiles(context)
    }

    var refocusTick by remember { mutableStateOf(0) }

    // Once the user picks "Don't ask again", requestPermissions() auto-denies with no dialog at
    // all and shouldShowRequestPermissionRationale() goes false for that permission — App Settings
    // is the only route left. Each permission group gets its own check so a dead-end is detected
    // per source (contacts vs. legacy storage), not lumped together.
    val canAskAgain: (String) -> Boolean = { permission ->
        activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    val systemPageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissionState()
        refocusTick++
        onRefreshResults()
    }

    val openSystemPage: (android.content.Intent) -> Unit = { intent -> systemPageLauncher.launch(intent) }

    // App Settings — the only route left once a runtime permission is permanently denied.
    val appSettingsIntent = android.content.Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        android.net.Uri.fromParts("package", context.packageName, null)
    )

    // Contacts and (legacy) files are separate permission groups, each with its own permanently-denied
    // check — a dead-end in one group shouldn't send the user to App Settings while asking about the other.
    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissionState()
        if (!hasContactPermission && !canAskAgain(android.Manifest.permission.READ_CONTACTS)) {
            openSystemPage(appSettingsIntent)
        } else {
            refocusTick++
            onRefreshResults()
        }
    }

    val storageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissionState()
        if (!hasFilePermission && !canAskAgain(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            openSystemPage(appSettingsIntent)
        } else {
            refocusTick++
            onRefreshResults()
        }
    }

    // Return the keyboard after a system permission dialog steals it.
    // The delay is OS-dialog timing (no lifecycle signal fires when the dialog actually
    // releases input focus), not Compose timing -- a withFrameNanos wait would not help here.
    LaunchedEffect(refocusTick) {
        if (refocusTick > 0 && visible) {
            kotlinx.coroutines.delay(150)
            runCatching { focusRequester.requestFocus() }
        }
    }

    // Permission requests are user-initiated from the results affordance — never ambushed mid-keystroke.
    val requestContacts: () -> Unit = {
        contactsLauncher.launch(android.Manifest.permission.READ_CONTACTS)
    }

    val requestFiles: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // All files access is an app-op with no runtime dialog — its Settings page is the grant UI.
            val appScoped = android.content.Intent(
                android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                android.net.Uri.fromParts("package", context.packageName, null)
            )
            try {
                openSystemPage(appScoped)
            } catch (e: android.content.ActivityNotFoundException) {
                // Some OEM ROMs omit the app-specific page; fall back to the general one.
                openSystemPage(
                    android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                )
            }
        } else {
            storageLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Tiered ranking, best-target-on-Enter and the editor text itself live in the overlay
    // content below — see the `query` state there.

    // Auto-focus the text field whenever the overlay becomes visible.
    // Two frames is the Compose-idiomatic wait for composition + layout to settle,
    // so the focusRequester modifier is attached before we call requestFocus().
    LaunchedEffect(visible) {
        if (visible) {
            withFrameNanos { }
            withFrameNanos { }
            runCatching { focusRequester.requestFocus() }
        }
    }

    // (permission auto-request on first keystroke removed — see requestContacts/requestFiles,
    // triggered by explicit affordances in the results list)

    // The overlay enters and retracts with the same shared spring as the sheets and the dock —
    // everything in this launcher rises from the bottom, whichever edge taught you the gesture.
    AnimatedVisibility(
        visible = visible,
        enter = bottomEnter,
        exit = bottomExit,
        modifier = modifier
    ) {
        // Editor text is owned here rather than hoisted: the ViewModel echoes it back a frame
        // later through the StateFlow, and feeding that into the field reverts in-progress text
        // and pins the caret. Every ViewModel-side reset hides the overlay first, so seeding on
        // entry is enough — no sync back down.
        var query by remember { mutableStateOf(searchQueryFlow.value) }

        // Tiered ranking: prefix > word-prefix > substring, alphabetical within tier, capped for scanability
        val filteredApps = remember(query, apps) {
            val q = query.trim()
            if (q.isBlank()) emptyList()
            else {
                val ql = q.lowercase()
                apps.asSequence()
                    .filter { it.label.contains(q, ignoreCase = true) }
                    .map { app ->
                        val al = app.label.lowercase()
                        val tier = when {
                            al.startsWith(ql) -> 0
                            app.label.split(Regex("[^\\p{L}\\p{N}]+")).any { it.lowercase().startsWith(ql) } -> 1
                            else -> 2
                        }
                        tier to app
                    }
                    .sortedWith(compareBy({ it.first }, { it.second.label.lowercase() }))
                    .map { it.second }
                    .take(16)
                    .toList()
            }
        }

        // Enter/Search launches the single best target: top app, else top contact, else top file
        val launchTopResult: () -> Unit = {
            when {
                filteredApps.isNotEmpty() -> onAppClick(filteredApps.first())
                contacts.isNotEmpty() -> onContactClick(contacts.first())
                files.isNotEmpty() -> onFileClick(files.first())
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Outside the card: tap or swipe left to put search away. The card registers its own
            // pointer node, so presses on its sheet never reach this layer.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .pointerInput(Unit) {
                        var total = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { total = 0f },
                            onDragCancel = { total = 0f },
                            onHorizontalDrag = { _, dragAmount -> total += dragAmount },
                            onDragEnd = {
                                if (total < -80.dp.toPx()) onDismiss()
                                total = 0f
                            }
                        )
                    }
            )

            // Floating board card — square corners, steel hairline, parked a dock-height above
            // the bottom edge (the gap collapses when the keyboard is up so the field stays in reach).
            BoardPanel(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomPad)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .statusBarsPadding()
                    .semantics { paneTitle = "Search apps, contacts, files" }
                    // Register a hit target over the sheet so presses on the card do not fall
                    // through to the dismiss layer behind it.
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
                    .pointerInput(Unit) {
                        var total = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { total = 0f },
                            onDragCancel = { total = 0f },
                            onHorizontalDrag = { _, dragAmount -> total += dragAmount },
                            onDragEnd = {
                                if (total < -80.dp.toPx()) onDismiss()
                                total = 0f
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 12.dp)
                ) {
                    // Search bar row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = query,
                            onValueChange = {
                                query = it
                                onSearchQueryChange(it)
                            },
                            placeholder = {
                                Text("Search apps, contacts, files...", color = Board.Steel, style = BoardBodyStyle)
                            },
                            leadingIcon = {
                                // Decorative — the placeholder already labels the field for TalkBack.
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Board.Steel
                                )
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = {
                                        query = ""
                                        onSearchQueryChange("")
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Board.Paint
                                        )
                                    }
                                }
                            },
                            textStyle = BoardBodyStyle.copy(color = Board.Paint),
                            colors = TextFieldDefaults.colors(
                                // A recessed flap well on the board, not a floating pill
                                focusedContainerColor = Board.Well,
                                unfocusedContainerColor = Board.Well,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Board.Paint,
                                unfocusedTextColor = Board.Paint,
                                cursorColor = OrangePrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(Board.cell)
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { launchTopResult() }),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Same square close cell as every board sheet. It clears the query; a press
                        // outside the card only hides search and keeps the typed text.
                        BoardCloseButton(onClick = onCancel)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fetching contacts/files for the current query — thin orange hairline
                    if (isSearching && query.isNotBlank()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = OrangePrimary,
                            trackColor = Board.HairFaint
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (query.isBlank()) {
                        // Pre-query: launch what you actually launch, instead of a blank recall test
                        val pinnedApps = remember(apps) { apps.filter { it.isPinned } }
                        if (frequentApps.isNotEmpty() || pinnedApps.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 16.dp)
                            ) {
                                if (frequentApps.isNotEmpty()) {
                                    SearchSectionHeader("Frequent")
                                    AppsGrid(
                                        apps = frequentApps.take(8),
                                        onAppClick = onAppClick,
                                        onTogglePinned = onTogglePinned,
                                        focusModeEnabled = focusModeEnabled
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                if (pinnedApps.isNotEmpty()) {
                                    SearchSectionHeader("Essentials")
                                    AppsGrid(
                                        apps = pinnedApps,
                                        onAppClick = onAppClick,
                                        onTogglePinned = onTogglePinned,
                                        focusModeEnabled = focusModeEnabled
                                    )
                                }
                            }
                        } else if (apps.isNotEmpty()) {
                            // Nothing pinned yet: the card still opens onto something tappable
                            // instead of a bare instruction line.
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 16.dp)
                            ) {
                                SearchSectionHeader("All apps")
                                AppsGrid(
                                    apps = apps.take(16),
                                    onAppClick = onAppClick,
                                    onTogglePinned = onTogglePinned,
                                    focusModeEnabled = focusModeEnabled
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Type to search",
                                    color = Board.PaintDim,
                                    style = BoardBodyStyle
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                // Live announcements only — a container contentDescription here
                                // would override the readable children for TalkBack
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            // Results flow as cardless rows grouped by floating labels —
                            // headers scroll with the list; nothing occludes on the sheet

                            // Apps Section
                            if (filteredApps.isNotEmpty()) {
                                item {
                                    SearchSectionHeader("Apps")
                                }
                                items(filteredApps) { app ->
                                    AppResultRow(
                                        app = app,
                                        query = query,
                                        onAppClick = onAppClick,
                                        onTogglePinned = onTogglePinned,
                                    )
                                }
                            }

                            // Contacts Section
                            if (contacts.isNotEmpty()) {
                                item {
                                    SearchSectionHeader("Contacts")
                                }
                                items(contacts) { contact ->
                                    ContactItem(contact, query, onContactClick)
                                }
                            }

                            // Files Section
                            if (files.isNotEmpty()) {
                                item {
                                    SearchSectionHeader("Files")
                                }
                                items(files) { file ->
                                    FileItem(file, query, onFileClick)
                                }
                            }

                            // Recovery card: one message that names what happened and what to do next.
                            // When sources are missing but partial results exist, keep the standalone CTA.
                            val noResults = filteredApps.isEmpty() && contacts.isEmpty() && files.isEmpty()
                            val missingPermissions = !hasContactPermission || !hasFilePermission
                            if (noResults) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp)
                                            .padding(top = 48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "No matches for \"$query\"",
                                            color = Board.Paint,
                                            style = BoardBodyStyle,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (missingPermissions)
                                                "Try fewer letters, or add contacts and files to results."
                                            else
                                                "Try fewer letters, or check the spelling.",
                                            color = Board.PaintDim,
                                            style = BoardBodyStyle.copy(fontSize = 13.sp, lineHeight = 18.sp)
                                        )
                                        if (missingPermissions) {
                                            AddSourcesRow(
                                                missingContacts = !hasContactPermission,
                                                missingFiles = !hasFilePermission,
                                                onAddContacts = requestContacts,
                                                onAddFiles = requestFiles,
                                            )
                                        }
                                    }
                                }
                            } else if (missingPermissions) {
                                item {
                                    AddSourcesRow(
                                        missingContacts = !hasContactPermission,
                                        missingFiles = !hasFilePermission,
                                        onAddContacts = requestContacts,
                                        onAddFiles = requestFiles,
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
private fun SearchSectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp)) {
        BoardLabel(
            text = title,
            color = Board.Steel,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        BoardDivider(color = Board.HairFaint)
    }
}

@Composable
private fun AddSourcesRow(
    missingContacts: Boolean,
    missingFiles: Boolean,
    onAddContacts: () -> Unit,
    onAddFiles: () -> Unit,
) {
    if (!missingContacts && !missingFiles) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (missingContacts) {
            BoardQuietButton(
                text = "Add contacts",
                onClick = onAddContacts,
                modifier = Modifier.weight(1f)
            )
        }
        if (missingFiles) {
            BoardQuietButton(
                text = "Add files",
                onClick = onAddFiles,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AppsGrid(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    onTogglePinned: (AppModel) -> Unit,
    focusModeEnabled: Boolean = false
) {
    val context = LocalContext.current
    val rows = (apps.size + 3) / 4
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (j in 0 until 4) {
                    val index = i * 4 + j
                    if (index < apps.size) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppIcon(
                                app = apps[index],
                                onAppClick = onAppClick,
                                gridColumns = 4,
                                isGrayscale = focusModeEnabled && !apps[index].isEssential,
                                onTogglePinned = { onTogglePinned(apps[index]) },
                                isUninstallable = apps[index].isUninstallable,
                                onUninstall = {
                                    try {
                                        context.startActivity(
                                            android.content.Intent(
                                                android.content.Intent.ACTION_DELETE,
                                                android.net.Uri.fromParts("package", apps[index].packageName, null)
                                            )
                                        )
                                    } catch (e: Exception) {
                                        Log.w("UniversalSearchOverlay", "Uninstall launch failed for ${apps[index].packageName}", e)
                                    }
                                },
                                onAppInfo = {
                                    try {
                                        val intent = android.content.Intent(
                                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            android.net.Uri.fromParts("package", apps[index].packageName, null)
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.w("UniversalSearchOverlay", "App Info launch failed for ${apps[index].packageName}", e)
                                    }
                                }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppResultRow(
    app: AppModel,
    query: String,
    onAppClick: (AppModel) -> Unit,
    onTogglePinned: (AppModel) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val rowShape = Board.cell
    val iconShape = Board.cell

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    onAppClick(app)
                },
                onLongClick = { showMenu = true }
            )
            .boardPress(interactionSource, rowShape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Icon well sits on the board like every other cell: opaque, hairline, no blur
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(iconShape)
                .background(Board.Well)
                .border(1.dp, Board.Hair, iconShape)
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(iconShape)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = highlightMatch(app.label, query),
            color = Board.Paint,
            style = BoardBodyStyle,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Board.Flap)
        ) {
            DropdownMenuItem(
                text = { Text("App Info", color = Board.Paint, style = BoardBodyStyle) },
                onClick = {
                    showMenu = false
                    try {
                        context.startActivity(
                            android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.fromParts("package", app.packageName, null)
                            )
                        )
                    } catch (e: Exception) {
                        Log.w("UniversalSearchOverlay", "App Info launch failed for ${app.packageName}", e)
                    }
                }
            )
            DropdownMenuItem(
                text = {
                        Text(
                            if (app.isPinned) "Remove from Essentials" else "Add to Essentials",
                            color = Board.Paint,
                            style = BoardBodyStyle
                        )
                },
                onClick = {
                    showMenu = false
                    onTogglePinned(app)
                }
            )
            if (app.isUninstallable) {
                DropdownMenuItem(
                    text = { Text("Uninstall", color = Board.Paint, style = BoardBodyStyle) },
                    onClick = {
                        showMenu = false
                        try {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_DELETE,
                                    android.net.Uri.fromParts("package", app.packageName, null)
                                )
                            )
                        } catch (e: Exception) {
                            Log.w("UniversalSearchOverlay", "Uninstall launch failed for ${app.packageName}", e)
                        }
                    }
                )
            }
        }
    }
}

// Emphasises what the user actually typed: matched span in brand orange, rest in white.
private fun highlightMatch(text: String, query: String): AnnotatedString {
    val q = query.trim()
    val idx = if (q.isEmpty()) -1 else text.indexOf(q, ignoreCase = true)
    if (idx < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        withStyle(SpanStyle(color = Board.Paint.copy(alpha = 0.85f))) { append(text.substring(0, idx)) }
        withStyle(SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold)) { append(text.substring(idx, idx + q.length)) }
        withStyle(SpanStyle(color = Board.Paint.copy(alpha = 0.85f))) { append(text.substring(idx + q.length)) }
    }
}

@Composable
private fun ContactItem(contact: ContactModel, query: String, onClick: (ContactModel) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val rowShape = Board.cell
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .clickable(interactionSource = interactionSource, indication = null) { onClick(contact) }
            .boardPress(interactionSource, rowShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(Board.cell)
                .background(Board.Well)
                .border(1.dp, Board.Hair, Board.cell),
            contentAlignment = Alignment.Center
        ) {
            if (contact.photo != null) {
                Image(
                    bitmap = contact.photo,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(Board.cell),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Board.Steel,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = highlightMatch(contact.name, query),
                color = Board.Paint,
                style = BoardBodyStyle,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (contact.phoneNumber != null) {
                Text(
                    text = contact.phoneNumber,
                    color = Board.Steel,
                    style = BoardBodyStyle.copy(fontSize = 12.sp)
                )
            }
        }
    }
}

@Composable
private fun FileItem(file: FileModel, query: String, onClick: (FileModel) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val rowShape = Board.cell
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .clickable(interactionSource = interactionSource, indication = null) { onClick(file) }
            .boardPress(interactionSource, rowShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bare line glyph on the board — a tinted well behind a line icon reads as a drop
        // shadow square. Same 44dp slot keeps the column rhythm with app and contact rows.
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            DocumentGlyph(modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = highlightMatch(file.name, query),
                color = Board.Paint,
                style = BoardBodyStyle,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val ext = file.name.substringAfterLast('.', "").uppercase()
            val kb = file.size / 1024
            val sizeText = if (kb >= 1024) "${kb / 1024} MB" else "$kb KB"
            Text(
                text = if (ext.isNotEmpty()) "$ext · $sizeText" else sizeText,
                color = Board.Steel,
                style = BoardBodyStyle.copy(fontSize = 12.sp)
            )
        }
    }
}

// Hand-drawn line glyph instead of Icons.Default.Description — the stock filled document reads
// generic next to the app wells; a single rounded stroke with a folded corner matches the
// launcher's line weight. Two alphas: confident body, quiet fold.
@Composable
private fun DocumentGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.075f
        val fold = w * 0.3f
        val r = w * 0.16f
        val left = strokeW / 2f
        val top = strokeW / 2f
        val right = w - strokeW / 2f
        val bottom = h - strokeW / 2f

        val body = Path().apply {
            moveTo(left + r, top)
            lineTo(right - fold, top)
            lineTo(right, top + fold)
            lineTo(right, bottom - r)
            quadraticBezierTo(right, bottom, right - r, bottom)
            lineTo(left + r, bottom)
            quadraticBezierTo(left, bottom, left, bottom - r)
            lineTo(left, top + r)
            quadraticBezierTo(left, top, left + r, top)
            close()
        }
        drawPath(
            body,
            Board.Paint.copy(alpha = 0.8f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val corner = Path().apply {
            moveTo(right - fold, top)
            lineTo(right - fold, top + fold)
            lineTo(right, top + fold)
        }
        drawPath(
            corner,
            Board.Paint.copy(alpha = 0.45f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
