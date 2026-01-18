package com.example.nonelauncher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nonelauncher.ui.modes.LauncherUiMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class UiModeManager(private val context: Context) {
    private val UI_MODE_KEY = stringPreferencesKey("ui_mode")

    val uiModeFlow: Flow<LauncherUiMode> = context.dataStore.data
        .map { preferences ->
            val modeName = preferences[UI_MODE_KEY] ?: LauncherUiMode.LEGACY.name
            try {
                LauncherUiMode.valueOf(modeName)
            } catch (e: IllegalArgumentException) {
                LauncherUiMode.LEGACY
            }
        }

    suspend fun setUiMode(mode: LauncherUiMode) {
        context.dataStore.edit { it[UI_MODE_KEY] = mode.name }
    }
}
