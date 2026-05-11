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
    private val _recentIntentions = intentionRepository.getAllIntentions().map { it }
    
    private val _searchQuery = MutableStateFlow("")
    private val _contactResults = MutableStateFlow<List<ContactModel>>(emptyList())
    private val _fileResults = MutableStateFlow<List<FileModel>>(emptyList())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<HomeUiState> = combine(
        _installedApps,
        _recentIntentions,
        prefsRepository.oneThingTask,
        prefsRepository.oneThingTaskWithState,
        _searchQuery,
        _contactResults,
        _fileResults
    ) { args ->
        val apps = args[0] as List<AppModel>
        val intentions = args[1] as List<Intention>
        val oneThing = args[2] as String?
        val oneThingWithState = args[3] as OneThingTask?
        val query = args[4] as String
        val contacts = args[5] as List<ContactModel>
        val files = args[6] as List<FileModel>

        HomeUiState(
            apps = apps.sortedBy { it.label.lowercase() },
            recentIntentions = intentions,
            oneThingTask = oneThing,
            oneThingTaskWithState = oneThingWithState,
            searchQuery = query,
            contactResults = contacts,
            fileResults = files
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
                    if (query.isBlank()) {
                        _contactResults.value = emptyList()
                        _fileResults.value = emptyList()
                    } else {
                        // Use separate launches with try-catch to prevent one failure from stopping the observer
                        launch {
                            try {
                                _contactResults.value = contactRepository.searchContacts(query)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                _contactResults.value = emptyList()
                            }
                        }
                        launch {
                            try {
                                _fileResults.value = fileRepository.searchFiles(query)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                _fileResults.value = emptyList()
                            }
                        }
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
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

    fun resetOneThingTask() {
        prefsRepository.setOneThingTaskWithState(null)
    }

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
    val oneThingTaskWithState: OneThingTask? = null,
    val searchQuery: String = "",
    val contactResults: List<ContactModel> = emptyList(),
    val fileResults: List<FileModel> = emptyList()
)
