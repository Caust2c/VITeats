package com.viteats.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viteats.app.data.ThemeManager
import com.viteats.app.data.remote.BalanceResponse
import com.viteats.app.data.repository.AuthRepository
import com.viteats.app.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeManager: ThemeManager,
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = themeManager.isDarkMode

    private val _studentProfile = MutableStateFlow<BalanceResponse?>(null)
    val studentProfile: StateFlow<BalanceResponse?> = _studentProfile.asStateFlow()

    init {
        fetchStudentProfile()
    }

    fun fetchStudentProfile() {
        viewModelScope.launch {
            try {
                val response = studentRepository.getBalance()
                if (response.isSuccessful) {
                    _studentProfile.value = response.body()?.firstOrNull()
                }
            } catch (e: Exception) {
                // Keep default or null
            }
        }
    }

    fun toggleDarkMode() {
        themeManager.toggleDarkMode()
    }

    fun setDarkMode(enabled: Boolean) {
        themeManager.setDarkMode(enabled)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
