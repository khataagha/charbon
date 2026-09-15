package com.example.model

/**
 * Represents a single Unicode character with complete metadata
 * for typing, search, and inspector details.
 */
data class UnicodeCharacter(
    val codePoint: Int,
    val char: String,
    val hex: String,
    val dec: Int,
    val utf8Bytes: String,
    val name: String,
    val blockName: String,
    val htmlEntity: String
)

/**
 * Represents a logical block/category in the Unicode Character Map.
 */
data class UnicodeBlock(
    val id: String,
    val name: String,
    val icon: String,
    val startCode: Int,
    val endCode: Int
) {
    val count: Int get() = endCode - startCode + 1
}

enum class KeyboardMode {
    CHARMAP,
    ALPHANUMERIC,
    ARROW_PAD,
    KAOMOJI,
    CLIPBOARD
}

enum class CharbonThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}
