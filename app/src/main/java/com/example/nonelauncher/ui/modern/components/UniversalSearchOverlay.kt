package com.example.nonelauncher.ui.modern.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
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
import android.content.pm.PackageManager
import android.os.Build

/**
 * Full-screen search overlay, shown when the user swipes in from the right edge.
 * Automatically focuses the search field when visible.
 */
@Composable
fun UniversalSearchOverlay(
    visible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    apps: List<AppModel>,
    contacts: List<ContactModel>,
    files: List<FileModel>,
    onDismiss: () -> Unit,
    onAppClick: (AppModel) -> Unit,
    onContactClick: (ContactModel) -> Unit,
    onFileClick: (FileModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hazeState = HazeGlassMorphism.rememberHazeState()
    val focusRequester = remember { FocusRequester() }

    // Permission state
    var hasContactPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasFilePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
    }

    val filePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasFilePermission = permissions.values.any { it }
    }

    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) emptyList()
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    // Auto-focus the text field whenever the overlay becomes visible
    LaunchedEffect(visible) {
        if (visible) {
            kotlinx.coroutines.delay(100) // brief delay for animation to start
            focusRequester.requestFocus()
        }
    }

    // Request permissions when user starts searching
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            if (!hasContactPermission) {
                contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            }
            if (!hasFilePermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    filePermissionsLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO,
                            android.Manifest.permission.READ_MEDIA_AUDIO
                        )
                    )
                } else {
                    filePermissionsLauncher.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
                }
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Blur background layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState, backgroundColor = Color.Transparent)
            )

            // Glass surface — taps on empty area dismiss (also block taps reaching the dock underneath)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeChild(state = hazeState, blurRadius = 28.dp)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(onClick = onDismiss)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 120.dp) // leave room for dock
                ) {
                    // Search bar row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text("Search apps, contacts, files...", color = Color.White.copy(0.6f))
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White.copy(0.7f)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
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
                                cursorColor = Color(0xFFFF4D00)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .focusRequester(focusRequester),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (searchQuery.isBlank()) {
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
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            // Apps Section
                            if (filteredApps.isNotEmpty()) {
                                item {
                                    SearchSectionHeader("Apps")
                                }
                                item {
                                    AppsGrid(
                                        apps = filteredApps,
                                        onAppClick = onAppClick,
                                        hazeState = hazeState
                                    )
                                }
                            }

                            // Contacts Section
                            if (contacts.isNotEmpty()) {
                                item {
                                    SearchSectionHeader("Contacts")
                                }
                                items(contacts) { contact ->
                                    ContactItem(contact, onContactClick)
                                }
                            }

                            // Files Section
                            if (files.isNotEmpty()) {
                                item {
                                    SearchSectionHeader("Files")
                                }
                                items(files) { file ->
                                    FileItem(file, onFileClick)
                                }
                            }

                            if (filteredApps.isEmpty() && contacts.isEmpty() && files.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 64.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No results found",
                                            color = Color.White.copy(alpha = 0.65f),
                                            style = MaterialTheme.typography.bodyLarge
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
fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFFF4D00),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AppsGrid(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    hazeState: dev.chrisbanes.haze.HazeState
) {
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
                                isGrayscale = false,
                                isBlurred = false,
                                isUninstallable = true,
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
fun ContactItem(contact: ContactModel, onClick: (ContactModel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick(contact) }
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
                fontWeight = FontWeight.Medium
            )
            if (contact.phoneNumber != null) {
                Text(
                    text = contact.phoneNumber,
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun FileItem(file: FileModel, onClick: (FileModel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick(file) }
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
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
