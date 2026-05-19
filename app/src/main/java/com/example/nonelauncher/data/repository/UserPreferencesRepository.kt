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

    private val _oneThingTask = MutableStateFlow(prefs.getString(KEY_ONE_THING_TASK, null))
    val oneThingTask: StateFlow<String?> = _oneThingTask.asStateFlow()

    private val _oneThingTaskWithState = MutableStateFlow(
        parseOneThingTaskWithState(prefs.getString(KEY_ONE_THING_TASK_STATE, null))
    )
    val oneThingTaskWithState: StateFlow<OneThingTask?> = _oneThingTaskWithState.asStateFlow()

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

    data class AppResumePrompts(
        val showFirstLaunchWelcome: Boolean = false,
        val showFeedback: Boolean = false,
    )

    /**
     * Tracks each app open. Shows a one-time welcome on the first launch after install.
     * Feedback prompt is evaluated only when the launcher is default and welcome is not shown.
     */
    fun onAppResume(isDefaultLauncher: Boolean): AppResumePrompts {
        val appLaunchCount = prefs.getInt(KEY_APP_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_APP_LAUNCH_COUNT, appLaunchCount).apply()

        val showWelcome = appLaunchCount >= FIRST_LAUNCH_WELCOME_AT &&
            !prefs.getBoolean(KEY_FIRST_LAUNCH_WELCOME_SHOWN, false)

        val showFeedback = if (
            !FEEDBACK_TEST_TRIGGER_ON_ONE_THING &&
            isDefaultLauncher &&
            !showWelcome
        ) {
            onLauncherResume()
        } else {
            false
        }

        return AppResumePrompts(
            showFirstLaunchWelcome = showWelcome,
            showFeedback = showFeedback,
        )
    }

    fun markFirstLaunchWelcomeShown() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH_WELCOME_SHOWN, true).apply()
    }

    /** Increments home-screen resume count and returns whether the feedback prompt should show. */
    private fun onLauncherResume(): Boolean {
        val now = System.currentTimeMillis()
        val launchCount = prefs.getInt(KEY_FEEDBACK_LAUNCH_COUNT, 0) + 1
        prefs.edit().apply {
            putInt(KEY_FEEDBACK_LAUNCH_COUNT, launchCount)
            if (!prefs.contains(KEY_FEEDBACK_FIRST_USE_TIME)) {
                putLong(KEY_FEEDBACK_FIRST_USE_TIME, now)
            }
        }.apply()
        return shouldShowFeedbackPrompt(launchCount, now)
    }

    fun markFeedbackPromptShown() {
        prefs.edit().putLong(KEY_FEEDBACK_LAST_PROMPT_TIME, System.currentTimeMillis()).apply()
    }

    fun snoozeFeedbackPrompt() {
        markFeedbackPromptShown()
    }

    fun permanentlyDisableFeedbackPrompt() {
        prefs.edit().putBoolean(KEY_FEEDBACK_PERMANENTLY_DISABLED, true).apply()
    }

    private fun shouldShowFeedbackPrompt(launchCount: Int, now: Long): Boolean {
        if (prefs.getBoolean(KEY_FEEDBACK_PERMANENTLY_DISABLED, false)) return false
        val lastPrompt = prefs.getLong(KEY_FEEDBACK_LAST_PROMPT_TIME, 0L)
        if (lastPrompt > 0L && now - lastPrompt < THIRTY_DAYS_MS) return false
        val firstUse = prefs.getLong(KEY_FEEDBACK_FIRST_USE_TIME, now)
        val sevenDaysPassed = now - firstUse >= SEVEN_DAYS_MS
        return launchCount >= LAUNCH_THRESHOLD || sevenDaysPassed
    }

    companion object {
        private const val PREFS_NAME = "focus_prefs"
        private const val KEY_ONE_THING_TASK = "one_thing_task"
        private const val KEY_ONE_THING_TASK_STATE = "one_thing_task_with_state"

        private const val KEY_APP_LAUNCH_COUNT = "app_launch_count"
        private const val KEY_FIRST_LAUNCH_WELCOME_SHOWN = "first_launch_welcome_shown"
        private const val FIRST_LAUNCH_WELCOME_AT = 1

        private const val KEY_FEEDBACK_LAUNCH_COUNT = "feedback_launch_count"
        private const val KEY_FEEDBACK_FIRST_USE_TIME = "feedback_first_use_time"
        private const val KEY_FEEDBACK_LAST_PROMPT_TIME = "feedback_last_prompt_time"
        private const val KEY_FEEDBACK_PERMANENTLY_DISABLED = "feedback_permanently_disabled"

        private const val LAUNCH_THRESHOLD = 5
        private const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000
        private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000

        const val FEEDBACK_WEBSITE_URL = "https://none-launcher.vercel.app/dashboard"

        /** Set to false before release — feedback shows on 1 Thing dock tap instead of resume rules. */
        const val FEEDBACK_TEST_TRIGGER_ON_ONE_THING = true
    }
}
