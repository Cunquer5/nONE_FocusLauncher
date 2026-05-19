package com.example.nonelauncher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import com.example.nonelauncher.ui.components.FeedbackPromptDialog
import com.example.nonelauncher.ui.components.WelcomeFirstLaunchDialog
import com.example.nonelauncher.ui.components.openFeedbackWebsite
import com.example.nonelauncher.ui.modern.ModernHomeScreen

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    var isDefaultLauncher by remember { mutableStateOf(isDefaultLauncher(context)) }
    var showFirstLaunchWelcome by remember { mutableStateOf(false) }
    var showFeedbackPrompt by remember { mutableStateOf(false) }
    val prefsRepository = remember { UserPreferencesRepository(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isDefaultLauncher = isDefaultLauncher(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    isDefaultLauncher = isDefaultLauncher(context)
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

    if (showFirstLaunchWelcome) {
        WelcomeFirstLaunchDialog(
            isDefaultLauncher = isDefaultLauncher,
            onGetStarted = {
                prefsRepository.markFirstLaunchWelcomeShown()
                showFirstLaunchWelcome = false
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

    if (!isDefaultLauncher) {
        Surface(color = androidx.compose.ui.graphics.Color.Transparent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "nONE Launcher needs to be set as your default Home app to work correctly.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
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
                }) {
                    Text(text = "Set as Default")
                }
            }
        }
    } else {
        ModernHomeScreen(
            onRequestFeedbackPrompt = { showFeedbackPrompt = true },
        )
    }
}

fun isDefaultLauncher(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
    val resolveInfo = context.packageManager.resolveActivity(intent, 0)
    return resolveInfo?.activityInfo?.packageName == context.packageName
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
