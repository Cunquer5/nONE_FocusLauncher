package com.example.nonelauncher.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import com.example.nonelauncher.ui.components.FeedbackPromptDialog
import com.example.nonelauncher.ui.components.WelcomeFirstLaunchDialog
import com.example.nonelauncher.ui.components.openFeedbackWebsite
import com.example.nonelauncher.ui.modern.ModernHomeScreen
import com.example.nonelauncher.ui.theme.LiquidGlassPanel
import com.example.nonelauncher.ui.theme.LiquidGlassStyle
import com.example.nonelauncher.ui.theme.LocalHazeState
import com.example.nonelauncher.ui.theme.OrangePrimary
import com.example.nonelauncher.ui.theme.WallpaperBackground
import dev.chrisbanes.haze.HazeState

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    var isDefaultLauncher by remember { mutableStateOf(isDefaultLauncher(context)) }
    var bypassSetup by rememberSaveable { mutableStateOf(false) }
    var showFirstLaunchWelcome by remember { mutableStateOf(false) }
    var showFeedbackPrompt by remember { mutableStateOf(false) }
    var wallpaperRefresh by remember { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }
    var groundLoaded by remember { mutableStateOf<Boolean?>(null) }
    var groundPromptDismissed by rememberSaveable { mutableStateOf(false) }
    var wallpaperAccess by remember { mutableStateOf(canReadWallpaper(context)) }
    val prefsRepository = remember { UserPreferencesRepository(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isDefaultLauncher = isDefaultLauncher(context)
    }

    // Reading the static wallpaper is gated by storage access: all-files on Android 11+, the
    // legacy read permission below it. Without either, the system wallpaper shows through the
    // window but glass has nothing in-tree to sample.
    val legacyStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Granting does not change the wallpaper id, so ask the ground to re-read explicitly.
        wallpaperRefresh++
    }
    val requestWallpaperAccess: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            openAllFilesAccessSettings(context)
        } else {
            runCatching {
                legacyStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    isDefaultLauncher = isDefaultLauncher(context)
                    // Returning from the access settings screen: the wallpaper only becomes
                    // readable now, so re-read the ground.
                    val nowAccess = canReadWallpaper(context)
                    if (nowAccess != wallpaperAccess) {
                        wallpaperAccess = nowAccess
                        wallpaperRefresh++
                    }
                    val prompts = prefsRepository.onAppResume(isDefaultLauncher(context))
                    when {
                        prompts.showFirstLaunchWelcome -> showFirstLaunchWelcome = true
                        prompts.showFeedback -> showFeedbackPrompt = true
                    }
                }
                Lifecycle.Event.ON_START -> isDefaultLauncher = isDefaultLauncher(context)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // The ground every glass surface samples. Drawn by the app rather than by the window
        // manager, so blur, dimming and wallpaper changes stay under our control.
        WallpaperBackground(
            modifier = Modifier.fillMaxSize(),
            hazeState = hazeState,
            refreshKey = wallpaperRefresh,
            onLoadResult = { groundLoaded = it },
        )

        CompositionLocalProvider(LocalHazeState provides hazeState) {
            if (!isDefaultLauncher && !bypassSetup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        style = LiquidGlassStyle.Regular
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "nONE Launcher needs to be set as your default Home app to work correctly.",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        val roleManager = context.getSystemService(RoleManager::class.java)
                                        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                                            roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                                        } else {
                                            openFallbackHomeSettings(context)
                                        }
                                    } else {
                                        openFallbackHomeSettings(context)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text(text = "Set as Default")
                            }
                            // Detection can disagree with the system's own Home picker on some ROMs.
                            // The home screen is never held hostage to that disagreement.
                            TextButton(onClick = { bypassSetup = true }) {
                                Text(text = "Continue anyway", color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            } else {
                ModernHomeScreen(
                    hazeState = hazeState,
                    onRequestFeedbackPrompt = { showFeedbackPrompt = true },
                )
            }

            // Without a readable wallpaper there is nothing for glass to blur: surfaces fall
            // back to a flat tint over the system wallpaper. All-files access is the only door
            // the wallpaper service opens to a target-34 app on Android 11+.
            if (groundLoaded == false && !groundPromptDismissed && !showFirstLaunchWelcome) {
                WallpaperAccessPrompt(
                    onOpenSettings = requestWallpaperAccess,
                    onDismiss = { groundPromptDismissed = true },
                )
            }

            if (showFirstLaunchWelcome) {
                WelcomeFirstLaunchDialog(
                    isDefaultLauncher = isDefaultLauncher,
                    needsWallpaperAccess = !wallpaperAccess,
                    onGetStarted = {
                        prefsRepository.markFirstLaunchWelcomeShown()
                        showFirstLaunchWelcome = false
                        if (!wallpaperAccess) requestWallpaperAccess()
                    },
                )
            }

            if (showFeedbackPrompt) {
                LaunchedEffect(Unit) {
                    prefsRepository.markFeedbackPromptShown()
                }
                FeedbackPromptDialog(
                    onVisitWebsite = {
                        openFeedbackWebsite(context)
                        showFeedbackPrompt = false
                    },
                    onLater = {
                        prefsRepository.snoozeFeedbackPrompt()
                        showFeedbackPrompt = false
                    },
                    onNoThanks = {
                        prefsRepository.permanentlyDisableFeedbackPrompt()
                        showFeedbackPrompt = false
                    },
                )
            }
        }
    }
}

/**
 * A wallpaper surface is what glass samples. All-files access is the only way a target-34 app
 * can read the system wallpaper on Android 11+ — the media permission is not enough there.
 */
private fun canReadWallpaper(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

private fun openAllFilesAccessSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
    val appPage = Intent(
        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
        Uri.parse("package:${context.packageName}"),
    )
    runCatching { context.startActivity(appPage) }.onFailure {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }
}

@Composable
private fun WallpaperAccessPrompt(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            style = LiquidGlassStyle.Regular,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Glass needs wallpaper access",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = "Allow nONE to read your wallpaper so the blur samples the real thing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                        Text("Open settings")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Later", color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

fun isDefaultLauncher(context: Context): Boolean {
    // The home role is what Settings > Default apps reports, so it is the only check that agrees
    // with the user. resolveActivity() answers "No activity found" on this device while nONE holds
    // the role, which trapped the setup screen behind a button that could not change anything.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return true
        }
    }
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
    val resolveInfo = context.packageManager.resolveActivity(intent, 0)
    if (resolveInfo?.activityInfo?.packageName == context.packageName) return true
    // Some ROMs leave the role unassigned while still resolving Home to the only launcher
    // installed; Settings then shows nONE as the Home app even though isRoleHeld() is false.
    val homeApps = context.packageManager.queryIntentActivities(intent, 0)
    return homeApps.size == 1 && homeApps.first().activityInfo.packageName == context.packageName
}

fun openFallbackHomeSettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_HOME_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
    } catch (e: Exception) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
            Toast.makeText(context, "Please set nONE Launcher as Default Home App in Settings", Toast.LENGTH_LONG).show()
        } catch (e2: Exception) {
            Toast.makeText(context, "Unable to open Settings", Toast.LENGTH_SHORT).show()
        }
    }
}
