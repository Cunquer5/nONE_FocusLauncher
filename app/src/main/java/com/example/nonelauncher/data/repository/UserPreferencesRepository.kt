package com.example.nonelauncher.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.nonelauncher.data.model.OneThingTask
import com.example.nonelauncher.data.model.SubTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isFocusModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_FOCUS_ENABLED, false))
    val isFocusModeEnabled: StateFlow<Boolean> = _isFocusModeEnabled.asStateFlow()

    private val _isBlurEnabled = MutableStateFlow(prefs.getBoolean(KEY_BLUR_ENABLED, false))
    val isBlurEnabled: StateFlow<Boolean> = _isBlurEnabled.asStateFlow()

    private val _focusAppPackages = MutableStateFlow(
        prefs.getStringSet(KEY_FOCUS_APPS, emptySet()) ?: emptySet()
    )
    val focusAppPackages: StateFlow<Set<String>> = _focusAppPackages.asStateFlow()

    private val _oneThingTask = MutableStateFlow(prefs.getString(KEY_ONE_THING_TASK, null))
    val oneThingTask: StateFlow<String?> = _oneThingTask.asStateFlow()

    private val _oneThingTaskWithState = MutableStateFlow(
        parseOneThingTaskWithState(prefs.getString(KEY_ONE_THING_TASK_STATE, null))
    )
    val oneThingTaskWithState: StateFlow<OneThingTask?> = _oneThingTaskWithState.asStateFlow()

    fun setFocusModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOCUS_ENABLED, enabled).apply()
        _isFocusModeEnabled.value = enabled
    }

    fun setBlurEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLUR_ENABLED, enabled).apply()
        _isBlurEnabled.value = enabled
    }

    fun setFocusApps(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_FOCUS_APPS, packages).apply()
        _focusAppPackages.value = packages
    }

    fun setOneThingTask(task: String?) {
        prefs.edit().putString(KEY_ONE_THING_TASK, task).apply()
        _oneThingTask.value = task
    }

    fun setOneThingTaskWithState(task: OneThingTask?) {
        val serialized = task?.let { serializeOneThingTask(it) }
        prefs.edit().putString(KEY_ONE_THING_TASK_STATE, serialized).apply()
        _oneThingTaskWithState.value = task
    }

    private fun serializeOneThingTask(task: OneThingTask): String {
        return "${task.mainTask}|${
            task.subTasks.joinToString(";") {
                "${it.id},${
                    it.text
                        .replace("|", "\\|")
                        .replace(";", "\\;")
                        .replace(",", "\\,")
                },${it.isCompleted}"
            }
        }"
    }

    private fun parseOneThingTaskWithState(serialized: String?): OneThingTask? {
        if (serialized.isNullOrEmpty()) return null
        val parts = serialized.split("|")
        if (parts.size < 2) return null
        val mainTask = parts[0]
        val subTasks = parts[1].split(";").filter { it.isNotEmpty() }.mapNotNull { subStr ->
            val subParts = subStr.split(",")
            if (subParts.size >= 3) {
                SubTask(
                    id = subParts[0],
                    text = subParts[1]
                        .replace("\\|", "|")
                        .replace("\\;", ";")
                        .replace("\\,", ","),
                    isCompleted = subParts[2].toBoolean()
                )
            } else null
        }
        return OneThingTask(mainTask, subTasks)
    }

    companion object {
        private const val PREFS_NAME = "focus_prefs"
        private const val KEY_FOCUS_ENABLED = "focus_mode_enabled"
        private const val KEY_BLUR_ENABLED = "blur_enabled"
        private const val KEY_FOCUS_APPS = "focus_app_packages"
        private const val KEY_ONE_THING_TASK = "one_thing_task"
        private const val KEY_ONE_THING_TASK_STATE = "one_thing_task_with_state"
    }
}
