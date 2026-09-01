package com.viteats.app

import android.content.SharedPreferences
import com.viteats.app.data.ThemeManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class ThemeManagerTest {

    private fun createFakeSharedPreferences(initialValues: Map<String, Any?> = emptyMap()): SharedPreferences {
        val map = HashMap<String, Any?>(initialValues)

        val editorProxy = Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java)
        ) { _, method, args ->
            when (method.name) {
                "putBoolean" -> {
                    map[args[0] as String] = args[1] as Boolean
                    Proxy.newProxyInstance(
                        SharedPreferences.Editor::class.java.classLoader,
                        arrayOf(SharedPreferences.Editor::class.java)
                    ) { _, _, _ -> null }
                }
                "apply", "commit" -> null
                else -> null
            }
        } as SharedPreferences.Editor

        return Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getBoolean" -> {
                    val key = args[0] as String
                    val defValue = args[1] as Boolean
                    (map[key] as? Boolean) ?: defValue
                }
                "edit" -> editorProxy
                else -> null
            }
        } as SharedPreferences
    }

    @Test
    fun testDefaultDarkModeIsFalse() {
        val prefs = createFakeSharedPreferences()
        val themeManager = ThemeManager(prefs)

        assertFalse(themeManager.isDarkMode.value)
    }

    @Test
    fun testSetDarkModePersistsPreference() {
        val prefs = createFakeSharedPreferences()
        val themeManager = ThemeManager(prefs)

        themeManager.setDarkMode(true)
        assertTrue(themeManager.isDarkMode.value)
    }

    @Test
    fun testToggleDarkMode() {
        val prefs = createFakeSharedPreferences()
        val themeManager = ThemeManager(prefs)

        val next = themeManager.toggleDarkMode()
        assertTrue(next)
        assertTrue(themeManager.isDarkMode.value)

        val nextAgain = themeManager.toggleDarkMode()
        assertFalse(nextAgain)
        assertFalse(themeManager.isDarkMode.value)
    }
}
