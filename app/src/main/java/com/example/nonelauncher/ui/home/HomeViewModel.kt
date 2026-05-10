package com.example.nonelauncher.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nonelauncher.data.local.AppDatabase
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.repository.AppRepository
import com.example.nonelauncher.data.repository.IntentionRepository
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)
    private val prefsRepository = UserPreferencesRepository(application)
    private val intentionRepository = IntentionRepository(AppDatabase.getDatabase(application).intentionDao())

    private val _installedApps = MutableStateFlow<List<AppModel>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    private val _recentIntentions = intentionRepository.getAllIntentions().map { it }

    private val _userPrefsFlow = combine(
        prefsRepository.isFocusModeEnabled,
        prefsRepository.isBlurEnabled,
        prefsRepository.focusAppPackages
    ) { isFocusMode, isBlur, focusPackages ->
        Prefs(
            isFocusMode = isFocusMode,
            isBlur = isBlur,
            focusPackages = focusPackages
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        listOf(
            _installedApps,
            _userPrefsFlow,
            _recentIntentions,
            _searchQuery,
            prefsRepository.oneThingTask,
            prefsRepository.oneThingTaskWithState
        )
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val apps = values[0] as List<AppModel>
        @Suppress("UNCHECKED_CAST")
        val prefs = values[1] as Prefs
        @Suppress("UNCHECKED_CAST")
        val intentions = values[2] as List<Intention>
        @Suppress("UNCHECKED_CAST")
        val query = values[3] as String
        @Suppress("UNCHECKED_CAST")
        val oneThing = values[4] as String?
        @Suppress("UNCHECKED_CAST")
        val oneThingWithState = values[5] as OneThingTask?

        val processedApps = apps.map { app ->
            app.copy(
                isFocusMode = prefs.isFocusMode,
                isFocusApp = prefs.focusPackages.contains(app.packageName)
            )
        }.sortedWith(
            compareByDescending<AppModel> { it.isFocusApp }
                .thenBy { it.label.lowercase() }
        )

        HomeUiState(
            apps = processedApps,
            isFocusModeEnabled = prefs.isFocusMode,
            isBlurEnabled = prefs.isBlur,
            recentIntentions = intentions,
            searchQuery = query,
            oneThingTask = oneThing,
            oneThingTaskWithState = oneThingWithState
        )
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    val allIntentions = intentionRepository.getAllIntentions()

    init {
        loadApps()
    }

    fun reloadApps() {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _installedApps.value = appRepository.getInstalledApps()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFocusMode() {
        prefsRepository.setFocusModeEnabled(!uiState.value.isFocusModeEnabled)
    }

    fun toggleBlur() {
        prefsRepository.setBlurEnabled(!uiState.value.isBlurEnabled)
    }

    fun toggleAppFocusStatus(packageName: String) {
        val currentPackages = prefsRepository.focusAppPackages.value
        val newPackages = if (currentPackages.contains(packageName)) {
            currentPackages - packageName
        } else {
            currentPackages + packageName
        }
        prefsRepository.setFocusApps(newPackages)
    }

    fun setOneThingTaskWithState(task: OneThingTask?) {
        prefsRepository.setOneThingTaskWithState(task)
    }

    fun updateSubtaskState(subtaskId: String, isCompleted: Boolean) {
        val currentTask = prefsRepository.oneThingTaskWithState.value ?: return
        val updatedTask = currentTask.copy(
            subTasks = currentTask.subTasks.map { subTask ->
                if (subTask.id == subtaskId) subTask.copy(isCompleted = isCompleted) else subTask
            }
        )
        prefsRepository.setOneThingTaskWithState(updatedTask)
    }

    fun resetOneThingTask() {
        prefsRepository.setOneThingTaskWithState(null)
    }

    fun setOneThingTask(task: String?) {
        prefsRepository.setOneThingTask(task)
    }

    fun onIntentionSubmitted(app: AppModel, intention: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            intentionRepository.logIntention(app.packageName, app.label, intention)
            onComplete()
        }
    }

    private data class Prefs(
        val isFocusMode: Boolean,
        val isBlur: Boolean,
        val focusPackages: Set<String>
    )
}

data class HomeUiState(
    val apps: List<AppModel> = emptyList(),
    val isFocusModeEnabled: Boolean = false,
    val isBlurEnabled: Boolean = false,
    val recentIntentions: List<Intention> = emptyList(),
    val searchQuery: String = "",
    val oneThingTask: String? = null,
    val oneThingTaskWithState: OneThingTask? = null
)
