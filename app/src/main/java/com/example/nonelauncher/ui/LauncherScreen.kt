package com.example.nonelauncher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nonelauncher.ui.home.HomeScreen
import com.example.nonelauncher.ui.modes.LauncherUiMode
import com.example.nonelauncher.ui.modern.ModernHomeScreen

@Composable
fun LauncherScreen(
    appWidgetHost: android.appwidget.AppWidgetHost? = null,
    appWidgetManager: android.appwidget.AppWidgetManager? = null,
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    var isDefaultLauncher by remember { mutableStateOf(isDefaultLauncher(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiMode by mainViewModel.uiMode.collectAsState(initial = LauncherUiMode.LEGACY)

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        isDefaultLauncher = isDefaultLauncher(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefaultLauncher = isDefaultLauncher(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                            roleLauncher.launch(intent)
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
        Crossfade(targetState = uiMode, label = "UiTransition") { mode ->
            when (mode) {
                LauncherUiMode.LEGACY -> HomeScreen(
                    onOpenModernSettings = {
                        // This can be used if we need to deep link or trigger something
                    }
                )
                LauncherUiMode.MODERN -> ModernHomeScreen(
                    appWidgetHost = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    onOpenSettings = {
                        // In Modern mode, we might want a way to switch back
                        mainViewModel.setUiMode(LauncherUiMode.LEGACY)
                    }
                )
            }
        }
    }
}

fun isDefaultLauncher(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = context.packageManager.resolveActivity(intent, 0)
    return resolveInfo?.activityInfo?.packageName == context.packageName
}

fun openFallbackHomeSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "Please set nONE Launcher as Default Home App in Settings", Toast.LENGTH_LONG).show()
        } catch (e2: Exception) {
            Toast.makeText(context, "Unable to open Settings", Toast.LENGTH_SHORT).show()
        }
    }
}
