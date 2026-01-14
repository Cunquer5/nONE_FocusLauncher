package com.example.focuslauncher.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)

    private val _isFocusModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_FOCUS_ENABLED, false))
    val isFocusModeEnabled: StateFlow<Boolean> = _isFocusModeEnabled.asStateFlow()

    private val _isBlurEnabled = MutableStateFlow(prefs.getBoolean(KEY_BLUR_ENABLED, false))
    val isBlurEnabled: StateFlow<Boolean> = _isBlurEnabled.asStateFlow()

    private val _gridColumns = MutableStateFlow(prefs.getInt(KEY_GRID_COLUMNS, 4))
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _focusAppPackages = MutableStateFlow(prefs.getStringSet(KEY_FOCUS_APPS, emptySet()) ?: emptySet())
    val focusAppPackages: StateFlow<Set<String>> = _focusAppPackages.asStateFlow()

    private val _appOrder = MutableStateFlow(parseAppOrder(prefs.getString(KEY_APP_ORDER, null)))
    val appOrder: StateFlow<List<String>> = _appOrder.asStateFlow()

    fun setFocusModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOCUS_ENABLED, enabled).apply()
        _isFocusModeEnabled.value = enabled
    }

    fun setBlurEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLUR_ENABLED, enabled).apply()
        _isBlurEnabled.value = enabled
    }

    fun setGridColumns(columns: Int) {
        prefs.edit().putInt(KEY_GRID_COLUMNS, columns).apply()
        _gridColumns.value = columns
    }

    fun setFocusApps(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_FOCUS_APPS, packages).apply()
        _focusAppPackages.value = packages
    }

    fun addFocusApp(packageName: String) {
        val current = _focusAppPackages.value.toMutableSet()
        current.add(packageName)
        setFocusApps(current)
    }

    fun removeFocusApp(packageName: String) {
        val current = _focusAppPackages.value.toMutableSet()
        current.remove(packageName)
        setFocusApps(current)
    }

    fun setAppOrder(order: List<String>) {
        val serialized = order.joinToString(",")
        prefs.edit().putString(KEY_APP_ORDER, serialized).apply()
        _appOrder.value = order
    }

    private fun parseAppOrder(serialized: String?): List<String> {
        if (serialized.isNullOrEmpty()) return emptyList()
        return serialized.split(",").filter { it.isNotEmpty() }
    }

    companion object {
        private const val KEY_FOCUS_ENABLED = "focus_mode_enabled"
        private const val KEY_BLUR_ENABLED = "blur_enabled"
        private const val KEY_GRID_COLUMNS = "grid_columns"
        private const val KEY_FOCUS_APPS = "focus_app_packages"
        private const val KEY_APP_ORDER = "app_sort_order"
    }
}
