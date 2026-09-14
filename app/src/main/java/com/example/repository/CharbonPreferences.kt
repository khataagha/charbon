package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CharbonThemeMode

class CharbonPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("charbon_keyboard_prefs", Context.MODE_PRIVATE)

    var themeMode: CharbonThemeMode
        get() {
            val raw = prefs.getString(KEY_THEME, CharbonThemeMode.DARK.name) ?: CharbonThemeMode.DARK.name
            return try { CharbonThemeMode.valueOf(raw) } catch (_: Exception) { CharbonThemeMode.DARK }
        }
        set(value) {
            prefs.edit().putString(KEY_THEME, value.name).apply()
        }

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var lastBlockId: String
        get() = prefs.getString(KEY_LAST_BLOCK, "GEOMETRIC") ?: "GEOMETRIC"
        set(value) = prefs.edit().putString(KEY_LAST_BLOCK, value).apply()

    fun getFavorites(): List<Int> {
        val raw = prefs.getString(KEY_FAVORITES, "9645,9644,9632,9633,9650,9679,8721,8734,8594,8377,9733,10024") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    fun toggleFavorite(codePoint: Int): Boolean {
        val current = getFavorites().toMutableList()
        val isFav: Boolean
        if (current.contains(codePoint)) {
            current.remove(codePoint)
            isFav = false
        } else {
            current.add(0, codePoint)
            isFav = true
        }
        prefs.edit().putString(KEY_FAVORITES, current.take(100).joinToString(",")).apply()
        return isFav
    }

    fun isFavorite(codePoint: Int): Boolean {
        return getFavorites().contains(codePoint)
    }

    fun getRecents(): List<Int> {
        val raw = prefs.getString(KEY_RECENTS, "9645,9644,9632,8721,8594,9733") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    fun addRecent(codePoint: Int) {
        val list = getRecents().toMutableList()
        list.remove(codePoint)
        list.add(0, codePoint)
        prefs.edit().putString(KEY_RECENTS, list.take(40).joinToString(",")).apply()
    }

    companion object {
        private const val KEY_THEME = "charbon_theme"
        private const val KEY_HAPTIC = "charbon_haptic"
        private const val KEY_LAST_BLOCK = "charbon_last_block"
        private const val KEY_FAVORITES = "charbon_favorites"
        private const val KEY_RECENTS = "charbon_recents"
    }
}
