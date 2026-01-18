package com.example.nonelauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nonelauncher.data.UiModeManager
import com.example.nonelauncher.ui.modes.LauncherUiMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val uiModeManager = UiModeManager(application)

    val uiMode: Flow<LauncherUiMode> = uiModeManager.uiModeFlow

    fun setUiMode(mode: LauncherUiMode) {
        viewModelScope.launch {
            uiModeManager.setUiMode(mode)
        }
    }
}
