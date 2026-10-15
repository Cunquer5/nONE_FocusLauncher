package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.glassPress
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.ContactModel
import com.example.nonelauncher.data.model.FileModel
import com.example.nonelauncher.ui.components.AppIcon
import com.example.nonelauncher.ui.theme.HazeGlassMorphism
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.StateFlow

/**
 * Full-screen search overlay, shown when the user swipes in from the right edge.
 * Automatically focuses the search field when visible.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UniversalSearchOverlay(
    visible: Boolean,
    searchQueryFlow: StateFlow<String>,
    onSearchQueryChange: (String) -> Unit,
    apps: List<AppModel>,
    contacts: List<ContactModel>,
    files: List<FileModel>,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onAppClick: (AppModel) -> Unit,
    onTogglePinned: (AppModel) -> Unit,
    onContactClick: (ContactModel) -> Unit,
    onFileClick: (FileModel) -> Unit,
    focusModeEnabled: Boolean = false,
    frequentApps: List<AppModel> = emptyList(),
    isSearching: Boolean = false,
    onRefreshResults: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hazeState = HazeGlassMorphism.rememberHazeState()
    val focusRequester = remember { FocusRequester() }

    // Permission state is re-read from the PackageManager after every dialog and every Settings
    // visit — grants can also happen outside the app.
    val activity = context as? android.app.Activity
    val isGranted: (String) -> Boolean = {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    val filePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var hasContactPermission by remember { mutableStateOf(isGranted(android.Manifest.permission.READ_CONTACTS)) }
    var hasFilePermission by remember { mutableStateOf(isGranted(filePermission)) }
    val refreshPermissionState: () -> Unit = {
        hasContactPermission = isGranted(android.Manifest.permission.READ_CONTACTS)
        hasFilePermission = isGranted(filePermission)
    }

    var refocusTick by remember { mutableStateOf(0) }

    // Once the user picks "Don't ask again", requestPermissions() auto-denies with no dialog at
    // all and shouldShowRequestPermissionRationale() goes false for that permission — App Settings
    // is the only route left. Contacts and files are different permission groups, so one request
    // can end with a dialog for one and a silent auto-deny for the other.
    val canAskAgain: (String) -> Boolean = { permission ->
        activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    val openAppSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissionState()
        refocusTick++
        onRefreshResults()
    }

    val openAppSettings: () -> Unit = {
        openAppSettingsLauncher.launch(
            android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)
            )
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshPermissionState()
        val unreachable =
            (!hasContactPermission && !canAskAgain(android.Manifest.permission.READ_CONTACTS)) ||
            (!hasFilePermission && !canAskAgain(filePermission))
        if (unreachable) {
            openAppSettings()
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

    // Permission requests are user-initiated from the results affordance — never ambushed mid-keystroke
    val requestMissingPermissions: () -> Unit = {
        val missing = mutableListOf<String>()
        if (!hasContactPermission) missing.add(android.Manifest.permission.READ_CONTACTS)
        if (!hasFilePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            missing.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            missing.add(android.Manifest.permission.READ_MEDIA_VIDEO)
            missing.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else if (!hasFilePermission) {
            missing.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (missing.isNotEmpty()) permissionsLauncher.launch(missing.toTypedArray())
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

    // (permission auto-request on first keystroke removed — see requestMissingPermissions,
    // triggered by an explicit affordance in the results list)

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f),
            initialOffsetX = { it }
        ) + fadeIn(tween(180)),
        exit = slideOutHorizontally(
            animationSpec = spring(dampingRatio = 1f, stiffness = 600f),
            targetOffsetX = { it }
        ) + fadeOut(tween(140)),
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
            // Blur background layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState, backgroundColor = Color.Transparent)
            )

            // Glass surface — taps on empty area dismiss (indication=null: a full-panel ripple bloom is noise)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeChild(state = hazeState, blurRadius = 28.dp)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .semantics { paneTitle = "Search apps, contacts, files" }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
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
                                Text("Search apps, contacts, files...", color = Color.White.copy(0.75f))
                            },
                            leadingIcon = {
                                // Decorative — the placeholder already labels the field for TalkBack.
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(0.7f)
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
                                            tint = Color.White.copy(0.7f)
                                        )
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = OrangePrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { launchTopResult() }),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Explicit close clears the query; tap-glass dismiss (onDismiss) does not.
                        // TextButton, not IconButton-with-Close — visually distinct from the inline clear.
                        TextButton(onClick = onCancel) {
                            Text("Cancel", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Fetching contacts/files for the current query — thin orange hairline
                    if (isSearching && query.isNotBlank()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = OrangePrimary,
                            trackColor = Color.White.copy(alpha = 0.08f)
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
                                        focusModeEnabled = focusModeEnabled,
                                        hazeState = hazeState
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                if (pinnedApps.isNotEmpty()) {
                                    SearchSectionHeader("Essentials")
                                    AppsGrid(
                                        apps = pinnedApps,
                                        onAppClick = onAppClick,
                                        onTogglePinned = onTogglePinned,
                                        focusModeEnabled = focusModeEnabled,
                                        hazeState = hazeState
                                    )
                                }
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
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                // Live announcements only — a container contentDescription here
                                // would override the readable children for TalkBack
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            // Apps Section
                            if (filteredApps.isNotEmpty()) {
                                stickyHeader {
                                    SearchSectionHeader("Apps")
                                }
                                item {
                                    AppsGrid(
                                        apps = filteredApps,
                                        onAppClick = onAppClick,
                                        onTogglePinned = onTogglePinned,
                                        hazeState = hazeState
                                    )
                                }
                            }

                            // Contacts Section
                            if (contacts.isNotEmpty()) {
                                stickyHeader {
                                    SearchSectionHeader("Contacts")
                                }
                                items(contacts) { contact ->
                                    ContactItem(contact, onContactClick)
                                }
                            }

                            // Files Section
                            if (files.isNotEmpty()) {
                                stickyHeader {
                                    SearchSectionHeader("Files")
                                }
                                items(files) { file ->
                                    FileItem(file, onFileClick)
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
                                            color = Color.White.copy(alpha = 0.85f),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (missingPermissions)
                                                "Try fewer letters, or add contacts and files to results."
                                            else
                                                "Try fewer letters, or check the spelling.",
                                            color = Color.White.copy(alpha = 0.55f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (missingPermissions) {
                                            TextButton(onClick = requestMissingPermissions) {
                                                Text("Add contacts and files to results", color = OrangePrimary)
                                            }
                                        }
                                    }
                                }
                            } else if (missingPermissions) {
                                item {
                                    TextButton(onClick = requestMissingPermissions) {
                                        Text(
                                            "Add contacts and files to results",
                                            color = OrangePrimary
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
}

@Composable
private fun SearchSectionHeader(title: String) {
    // No backdrop — the label floats on the glass panel; a band here always read as an artifact
    Text(
        text = title,
        color = OrangePrimary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun AppsGrid(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    onTogglePinned: (AppModel) -> Unit,
    focusModeEnabled: Boolean = false,
    hazeState: dev.chrisbanes.haze.HazeState
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
                                },
                                hazeState = hazeState
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

@Composable
private fun ContactItem(contact: ContactModel, onClick: (ContactModel) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick(contact) }
            .glassPress(interactionSource, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (contact.photo != null) {
                Image(
                    bitmap = contact.photo,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = contact.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (contact.phoneNumber != null) {
                Text(
                    text = contact.phoneNumber,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun FileItem(file: FileModel, onClick: (FileModel) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick(file) }
            .glassPress(interactionSource, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Description,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = file.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${(file.size / 1024)} KB",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
