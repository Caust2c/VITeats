package com.viteats.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences("viteats_theme_prefs", Context.MODE_PRIVATE)
    )

    private val _isDarkMode = MutableStateFlow(loadDarkModePreference())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private fun loadDarkModePreference(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    fun toggleDarkMode(): Boolean {
        val next = !_isDarkMode.value
        setDarkMode(next)
        return next
    }

    companion object {
        private const val KEY_DARK_MODE = "key_is_dark_mode"
    }
}
