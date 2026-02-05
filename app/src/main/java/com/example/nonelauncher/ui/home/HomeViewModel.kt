package com.example.nonelauncher.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nonelauncher.data.local.AppDatabase
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
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
        combine(
            prefsRepository.isFocusModeEnabled,
            prefsRepository.isBlurEnabled,
            prefsRepository.focusAppPackages
        ) { isFocusMode, isBlur, focusPackages ->
            Triple(isFocusMode, isBlur, focusPackages)
        },
        combine(
            prefsRepository.gridColumns,
            prefsRepository.appOrder,
            prefsRepository.widgetIds
        ) { columns, order, widgetIds ->
            Triple(columns, order, widgetIds)
        }
    ) { first, second ->
        Prefs(
            isFocusMode = first.first,
            isBlur = first.second,
            focusPackages = first.third,
            columns = second.first,
            appOrder = second.second,
            widgetIds = second.third
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        listOf(_installedApps, _userPrefsFlow, _recentIntentions, _searchQuery, prefsRepository.oneThingTask, prefsRepository.oneThingTaskWithState)
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
        
        val filteredApps = apps.map { app ->
            app.copy(isFocusMode = prefs.isFocusMode, isFocusApp = prefs.focusPackages.contains(app.packageName))
        }.filter { 
            it.label.contains(query, ignoreCase = true)
        }

        val sortedApps = if (query.isEmpty()) {
            if (prefs.appOrder.isNotEmpty()) {
                // Create a map of package name to index for O(1) lookup
                val orderMap = prefs.appOrder.withIndex().associate { it.value to it.index }
                
                filteredApps.sortedWith(
                    compareBy<AppModel> { orderMap[it.packageName] ?: Int.MAX_VALUE }
                        .thenByDescending { it.isFocusApp }
                        .thenBy { it.label.lowercase() }
                )
            } else {
                // Default Sort
                filteredApps.sortedWith(
                    compareByDescending<AppModel> { it.isFocusApp }
                        .thenBy { it.label.lowercase() }
                )
            }
        } else {
            filteredApps.sortedBy { it.label.lowercase() }
        }

        HomeUiState(
            apps = sortedApps,
            isFocusModeEnabled = prefs.isFocusMode,
            isBlurEnabled = prefs.isBlur,
            recentIntentions = intentions,
            searchQuery = query,
            gridColumns = prefs.columns,
            oneThingTask = oneThing,
            oneThingTaskWithState = oneThingWithState,
            widgetIds = prefs.widgetIds
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
        val current = uiState.value.isFocusModeEnabled
        prefsRepository.setFocusModeEnabled(!current)
    }

    fun toggleBlur() {
        val current = uiState.value.isBlurEnabled
        prefsRepository.setBlurEnabled(!current)
    }

    fun updateGridColumns(columns: Int) {
        prefsRepository.setGridColumns(columns)
    }
    
    fun updateWidgetIds(ids: List<Int>) {
        prefsRepository.setWidgetIds(ids)
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
        val currentTask = prefsRepository.oneThingTaskWithState.value
        if (currentTask != null) {
            val updatedSubTasks = currentTask.subTasks.map { subTask ->
                if (subTask.id == subtaskId) subTask.copy(isCompleted = isCompleted) else subTask
            }
            val updatedTask = currentTask.copy(subTasks = updatedSubTasks)
            prefsRepository.setOneThingTaskWithState(updatedTask)
        }
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
    
    fun saveAppOrder(newOrder: List<AppModel>) {
        val packageOrder = newOrder.map { it.packageName }
        prefsRepository.setAppOrder(packageOrder)
    }

    private data class Prefs(
        val isFocusMode: Boolean,
        val isBlur: Boolean,
        val focusPackages: Set<String>,
        val columns: Int,
        val appOrder: List<String>,
        val widgetIds: List<Int>
    )
}

data class HomeUiState(
    val apps: List<AppModel> = emptyList(),
    val isFocusModeEnabled: Boolean = false,
    val isBlurEnabled: Boolean = false,
    val recentIntentions: List<Intention> = emptyList(),
    val searchQuery: String = "",
    val gridColumns: Int = 4,
    val oneThingTask: String? = null,
    val oneThingTaskWithState: OneThingTask? = null,
    val widgetIds: List<Int> = emptyList()
)
