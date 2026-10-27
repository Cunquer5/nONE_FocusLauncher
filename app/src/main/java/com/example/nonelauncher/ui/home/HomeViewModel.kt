package com.example.nonelauncher.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nonelauncher.data.local.AppDatabase
import com.example.nonelauncher.data.local.entity.Intention
import com.example.nonelauncher.data.model.AppModel
import com.example.nonelauncher.data.model.ContactModel
import com.example.nonelauncher.data.model.FileModel
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.repository.AppRepository
import com.example.nonelauncher.data.repository.ContactRepository
import com.example.nonelauncher.data.repository.FileRepository
import com.example.nonelauncher.data.repository.IntentionRepository
import com.example.nonelauncher.data.repository.UserPreferencesRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)
    private val contactRepository = ContactRepository(application)
    private val fileRepository = FileRepository(application)
    private val prefsRepository = UserPreferencesRepository(application)
    private val intentionRepository = IntentionRepository(
        AppDatabase.getDatabase(application).intentionDao()
    )

    private val _installedApps = MutableStateFlow<List<AppModel>>(emptyList())
    // Single shared subscription to the intention table: uiState and the history sheet read this
    // same StateFlow rather than each launching the Room query separately.
    val allIntentions: StateFlow<List<Intention>> = intentionRepository.getAllIntentions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _searchQuery = MutableStateFlow("")
    private val _contactResults = MutableStateFlow<List<ContactModel>>(emptyList())
    private val _fileResults = MutableStateFlow<List<FileModel>>(emptyList())
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    // Search state stays off uiState: a keystroke that republishes the whole HomeUiState
    // recomposes the entire home screen underneath the search sheet.
    val searchQuery: StateFlow<String> = _searchQuery
    val contactResults: StateFlow<List<ContactModel>> = _contactResults
    val fileResults: StateFlow<List<FileModel>> = _fileResults

    // Top launched packages, most-first — powers the pre-query Frequent grid
    val frequentPackages: StateFlow<List<String>> = prefsRepository.appLaunchCounts
        .map { counts -> counts.entries.sortedByDescending { it.value }.take(10).map { it.key } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun recordAppLaunch(packageName: String) = prefsRepository.recordAppLaunch(packageName)

    /** First toggle adopts the auto-detected shelf as the starting point, so removals work from then on. */
    fun togglePinnedApp(app: AppModel) {
        val shelf = prefsRepository.pinnedAppPackages.value
            ?: _installedApps.value.filter { it.isEssential }.map { it.packageName }.toSet()
        prefsRepository.setPinnedAppPackages(
            if (app.packageName in shelf) shelf - app.packageName else shelf + app.packageName
        )
    }

    // Essentials shelf = the user's picks, or the auto-detected essentials until they pick.
    // Separate from isEssential, which still drives grayscale and the intention prompt.
    private val appsWithPins = combine(_installedApps, prefsRepository.pinnedAppPackages) { apps, picked ->
        val shelf = picked ?: apps.filter { it.isEssential }.map { it.packageName }.toSet()
        apps.map { it.copy(isPinned = it.packageName in shelf) }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        appsWithPins,
        allIntentions,
        prefsRepository.oneThingTask,
        prefsRepository.oneThingTaskWithState
    ) { apps, intentions, oneThing, oneThingWithState ->
        HomeUiState(
            apps = apps,
            recentIntentions = intentions,
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

    init {
        loadApps()
        observeSearchQuery()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _installedApps.value = appRepository.getInstalledApps()
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    val q = query.trim()
                    if (q.isEmpty()) {
                        _contactResults.value = emptyList()
                        _fileResults.value = emptyList()
                        _isSearching.value = false
                    } else {
                        runRemoteSearch(q)
                    }
                }
        }
    }

    // coroutineScope joins both so isSearching flips only when the query is done;
    // the stale guard drops results whose query changed while they were running
    private suspend fun runRemoteSearch(q: String) {
        _isSearching.value = true
        try {
            coroutineScope {
                launch {
                    try {
                        val results = contactRepository.searchContacts(q)
                        if (_searchQuery.value.trim() == q) _contactResults.value = results
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (_searchQuery.value.trim() == q) _contactResults.value = emptyList()
                    }
                }
                launch {
                    try {
                        val results = fileRepository.searchFiles(q)
                        if (_searchQuery.value.trim() == q) _fileResults.value = results
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (_searchQuery.value.trim() == q) _fileResults.value = emptyList()
                    }
                }
            }
        } finally {
            _isSearching.value = false
        }
    }

    /** Re-run the current query — a permission grant changes what is searchable mid-session. */
    fun refreshSearch() {
        val q = _searchQuery.value.trim()
        if (q.isNotEmpty()) viewModelScope.launch { runRemoteSearch(q) }
    }

    fun onSearchQueryChanged(query: String) {
        // Clear immediately so the previous query's results never linger under the new one
        if (query != _searchQuery.value) {
            _contactResults.value = emptyList()
            _fileResults.value = emptyList()
        }
        _searchQuery.value = query
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

    /** Completing the task is a state the day can hold, not a deletion. Only "New task" clears it. */
    fun setMainTaskCompleted(isCompleted: Boolean) {
        val currentTask = prefsRepository.oneThingTaskWithState.value ?: return
        prefsRepository.setOneThingTaskWithState(currentTask.copy(isMainCompleted = isCompleted))
    }

    fun resetOneThingTask() {
        prefsRepository.setOneThingTaskWithState(null)
    }

    // --- Structure passthroughs ---
    val structureSteps = prefsRepository.structureSteps
    val structureIndex = prefsRepository.structureIndex

    // --- Focus mode passthrough ---
    val focusModeEnabled = prefsRepository.focusModeEnabled
    fun setFocusModeEnabled(enabled: Boolean) = prefsRepository.setFocusModeEnabled(enabled)

    fun setStructureSteps(steps: List<String>) = prefsRepository.setStructureSteps(steps)
    fun moveStructureStep(delta: Int) = prefsRepository.moveStructureStep(delta)
    fun advanceStructureStep() = prefsRepository.advanceStructureStep()
    fun rollStructureDay() = prefsRepository.rollStructureDay()

    fun onIntentionSubmitted(app: AppModel, intention: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            intentionRepository.logIntention(app.packageName, app.label, intention)
            onComplete()
        }
    }
}

data class HomeUiState(
    val apps: List<AppModel> = emptyList(),
    val recentIntentions: List<Intention> = emptyList(),
    val oneThingTask: String? = null,
    val oneThingTaskWithState: OneThingTask? = null
)
