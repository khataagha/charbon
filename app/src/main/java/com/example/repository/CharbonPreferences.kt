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

    var keyboardHeightDp: Int
        get() = prefs.getInt(KEY_HEIGHT, 220)
        set(value) = prefs.edit().putInt(KEY_HEIGHT, value.coerceIn(180, 320)).apply()

    var showQuickSymbolRow: Boolean
        get() = prefs.getBoolean(KEY_QUICK_SYMBOLS, true)
        set(value) = prefs.edit().putBoolean(KEY_QUICK_SYMBOLS, value).apply()

    var lastBlockId: String
        get() = prefs.getString(KEY_LAST_BLOCK, "GEOMETRIC") ?: "GEOMETRIC"
        set(value) = prefs.edit().putString(KEY_LAST_BLOCK, value).apply()

    fun getFavorites(): List<Int> {
        val raw = prefs.getString(KEY_FAVORITES, "9645,9644,9632,9633,9650,9679,8721,8734,8594,8377,9733,10024") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.distinct()
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
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.distinct()
    }

    fun addRecent(codePoint: Int) {
        val list = getRecents().toMutableList()
        list.remove(codePoint)
        list.add(0, codePoint)
        prefs.edit().putString(KEY_RECENTS, list.take(40).joinToString(",")).apply()
    }

    /**
     * Custom user-defined symbol collections (tag name to list of code points).
     * Stored as "TagName:cp1,cp2,cp3;OtherTag:cp4,cp5"
     */
    fun getCustomCollections(): Map<String, List<Int>> {
        val raw = prefs.getString(KEY_CUSTOM_COLLECTIONS, "") ?: ""
        if (raw.isBlank()) {
            // Default sample collections
            return mapOf(
                "Math & Logic" to listOf(8721, 8734, 8730, 8800, 8804, 8805, 8747, 8745, 8746),
                "Arrows & Flow" to listOf(8592, 8594, 8593, 8595, 8656, 8658, 10140, 10145),
                "Game & Chess" to listOf(9812, 9813, 9814, 9815, 9816, 9817, 9824, 9829, 9830, 9827)
            )
        }
        val map = mutableMapOf<String, List<Int>>()
        raw.split(";").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val name = parts[0].trim()
                val items = parts[1].split(",").mapNotNull { it.trim().toIntOrNull() }
                if (name.isNotEmpty()) {
                    map[name] = items
                }
            }
        }
        return map
    }

    fun saveCustomCollection(name: String, codePoints: List<Int>) {
        val current = getCustomCollections().toMutableMap()
        current[name.trim()] = codePoints.distinct()
        val serialized = current.entries.joinToString(";") { "${it.key}:${it.value.joinToString(",")}" }
        prefs.edit().putString(KEY_CUSTOM_COLLECTIONS, serialized).apply()
    }

    fun deleteCustomCollection(name: String) {
        val current = getCustomCollections().toMutableMap()
        current.remove(name.trim())
        val serialized = current.entries.joinToString(";") { "${it.key}:${it.value.joinToString(",")}" }
        prefs.edit().putString(KEY_CUSTOM_COLLECTIONS, serialized).apply()
    }

    companion object {
        private const val KEY_THEME = "charbon_theme"
        private const val KEY_HAPTIC = "charbon_haptic"
        private const val KEY_HEIGHT = "charbon_height_dp"
        private const val KEY_QUICK_SYMBOLS = "charbon_quick_symbols"
        private const val KEY_LAST_BLOCK = "charbon_last_block"
        private const val KEY_FAVORITES = "charbon_favorites"
        private const val KEY_RECENTS = "charbon_recents"
        private const val KEY_CUSTOM_COLLECTIONS = "charbon_custom_collections"
    }
}
