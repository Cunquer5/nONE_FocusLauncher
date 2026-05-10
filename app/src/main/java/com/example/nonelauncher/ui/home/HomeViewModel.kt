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
    private val intentionRepository = IntentionRepository(
        AppDatabase.getDatabase(application).intentionDao()
    )

    private val _installedApps = MutableStateFlow<List<AppModel>>(emptyList())
    private val _recentIntentions = intentionRepository.getAllIntentions().map { it }

    val uiState: StateFlow<HomeUiState> = combine(
        _installedApps,
        _recentIntentions,
        prefsRepository.oneThingTask,
        prefsRepository.oneThingTaskWithState
    ) { apps, intentions, oneThing, oneThingWithState ->
        HomeUiState(
            apps = apps.sortedBy { it.label.lowercase() },
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

    val allIntentions = intentionRepository.getAllIntentions()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _installedApps.value = appRepository.getInstalledApps()
        }
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
    val oneThingTaskWithState: OneThingTask? = null
)
