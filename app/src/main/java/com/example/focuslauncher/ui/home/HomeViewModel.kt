package com.example.focuslauncher.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focuslauncher.data.local.AppDatabase
import com.example.focuslauncher.data.local.entity.Intention
import com.example.focuslauncher.data.model.AppModel
import com.example.focuslauncher.data.repository.AppRepository
import com.example.focuslauncher.data.repository.IntentionRepository
import com.example.focuslauncher.data.repository.UserPreferencesRepository
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

    private val _recentIntentions = intentionRepository.getAllIntentions().map { it.take(5) }

    private val _userPrefsFlow = combine(
        prefsRepository.isFocusModeEnabled,
        prefsRepository.isBlurEnabled,
        prefsRepository.focusAppPackages,
        prefsRepository.gridColumns,
        prefsRepository.appOrder
    ) { isFocusMode, isBlur, focusPackages, columns, order ->
        Prefs(isFocusMode, isBlur, focusPackages, columns, order)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        _installedApps,
        _userPrefsFlow,
        _recentIntentions,
        _searchQuery
    ) { apps, prefs, intentions, query ->
        
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
                        .thenByDescending { it.isFocusApp } // Tie-breaker if needed, or keeping focus apps logic secondary to order?
                        // Actually, if user sets order, that should win. But new apps go to end.
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
            gridColumns = prefs.columns
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

    fun toggleAppFocusStatus(packageName: String) {
        val currentPackages = prefsRepository.focusAppPackages.value
        val newPackages = if (currentPackages.contains(packageName)) {
            currentPackages - packageName
        } else {
            currentPackages + packageName
        }
        prefsRepository.setFocusApps(newPackages)
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
        val appOrder: List<String>
    )
}

data class HomeUiState(
    val apps: List<AppModel> = emptyList(),
    val isFocusModeEnabled: Boolean = false,
    val isBlurEnabled: Boolean = false,
    val recentIntentions: List<Intention> = emptyList(),
    val searchQuery: String = "",
    val gridColumns: Int = 4
)
