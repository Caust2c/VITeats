package com.viteats.app.data

import android.content.Context
import com.viteats.app.data.security.CryptoManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SessionEvent {
    object SessionExpired : SessionEvent()
    object LoggedOut : SessionEvent()
}

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("viteats_secure_prefs", Context.MODE_PRIVATE)

    private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    var registrationNumber: String?
        get() = CryptoManager.decrypt(prefs.getString("enc_reg_no", null))
        set(value) {
            val encrypted = CryptoManager.encrypt(value)
            prefs.edit().putString("enc_reg_no", encrypted).apply()
        }

    var userIdentifier: String?
        get() = CryptoManager.decrypt(prefs.getString("enc_user_id", null))
        set(value) {
            val encrypted = CryptoManager.encrypt(value)
            prefs.edit().putString("enc_user_id", encrypted).apply()
        }

    var cachedPin: String?
        get() = CryptoManager.decrypt(prefs.getString("enc_pin", null))
        set(value) {
            val encrypted = CryptoManager.encrypt(value)
            prefs.edit().putString("enc_pin", encrypted).apply()
        }

    fun hasValidSession(): Boolean {
        return !registrationNumber.isNullOrBlank() && !userIdentifier.isNullOrBlank()
    }

    fun notifySessionExpired() {
        clear()
        _sessionEvents.tryEmit(SessionEvent.SessionExpired)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _sessionEvents.tryEmit(SessionEvent.LoggedOut)
    }
}

