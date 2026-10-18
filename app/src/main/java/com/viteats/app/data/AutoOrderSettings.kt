package com.viteats.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AutoOrderConfig(
    val enabled: Boolean = false,
    val hour: Int = 19,
    val minute: Int = 0,
    val allowPartial: Boolean = false,
    val hardLimit: Double = 0.0,
    val generation: Long = 0,
    val lastResult: String = "No automatic orders yet."
)

class AutoOrderSettings(context: Context, private val session: SessionManager) {
    private val prefs = context.getSharedPreferences("ordering_settings", Context.MODE_PRIVATE)
    private fun key(owner: String) = "config_$owner"
    private val gson = com.google.gson.Gson()
    private val _config = MutableStateFlow(read())
    val config = _config.asStateFlow()

    fun read(owner: String = session.userIdentifier.orEmpty()): AutoOrderConfig =
        runCatching { gson.fromJson(prefs.getString(key(owner), null), AutoOrderConfig::class.java) }
            .getOrNull() ?: AutoOrderConfig()

    fun refresh() { _config.value = read() }

    fun save(config: AutoOrderConfig) {
        require(config.hour in 0..23 && config.minute in 0..59)
        require(config.hardLimit.isFinite() && config.hardLimit >= 0)
        val owner = session.userIdentifier ?: return
        val updated = config.copy(generation = System.currentTimeMillis())
        prefs.edit().putString(key(owner), gson.toJson(updated)).commit()
        _config.value = updated
    }

    fun report(owner: String, message: String) {
        val updated = read(owner).copy(lastResult = message)
        prefs.edit().putString(key(owner), gson.toJson(updated)).commit()
        if (owner == session.userIdentifier) _config.value = updated
    }
}
