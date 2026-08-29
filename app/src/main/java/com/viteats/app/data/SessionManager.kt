package com.viteats.app.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("viteats_prefs", Context.MODE_PRIVATE)

    var registrationNumber: String?
        get() = prefs.getString("reg_no", null)
        set(value) = prefs.edit().putString("reg_no", value).apply()

    var userIdentifier: String?
        get() = prefs.getString("user_id", null)
        set(value) = prefs.edit().putString("user_id", value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
