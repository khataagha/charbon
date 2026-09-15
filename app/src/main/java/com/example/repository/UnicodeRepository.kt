package com.example.repository

import com.example.model.UnicodeBlock
import com.example.model.UnicodeCharacter
import java.util.Locale

object UnicodeRepository {

    val BLOCKS: List<UnicodeBlock> = listOf(
        UnicodeBlock("GEOMETRIC", "Geometric Shapes", "▭", 0x25A0, 0x25FF),
        UnicodeBlock("BOX", "Box Drawing", "╔", 0x2500, 0x257F),
        UnicodeBlock("BLOCKS", "Block Elements", "█", 0x2580, 0x259F),
        UnicodeBlock("MATH", "Math Operators", "∑", 0x2200, 0x22FF),
        UnicodeBlock("SUPP_MATH", "Supp. Math", "⨁", 0x2A00, 0x2A70),
        UnicodeBlock("ARROWS", "Arrows", "➔", 0x2190, 0x21FF),
        UnicodeBlock("CURRENCY", "Currency", "€", 0x20A0, 0x20CF),
        UnicodeBlock("SUB_SUPER", "Sub/Super", "x²", 0x2070, 0x209F),
        UnicodeBlock("GREEK", "Greek & Coptic", "Ω", 0x0370, 0x03FF),
        UnicodeBlock("SYMBOLS", "Misc Symbols", "★", 0x2600, 0x26FF),
        UnicodeBlock("DINGBATS", "Dingbats", "✦", 0x2700, 0x27BF),
        UnicodeBlock("RUNIC", "Runic", "ᚱ", 0x16A0, 0x16EA),
        UnicodeBlock("BRAILLE", "Braille Patterns", "⠿", 0x2800, 0x28FF),
        UnicodeBlock("ENCLOSED", "Enclosed Alpha", "①", 0x2460, 0x24EA),
        UnicodeBlock("PHONETIC", "IPA Extensions", "ə", 0x0250, 0x02AF)
    )

    private val KNOWN_NAMES = mapOf(
        0x25AD to "WHITE RECTANGLE",
        0x25AC to "BLACK RECTANGLE",
        0x25A0 to "BLACK SQUARE",
        0x25A1 to "WHITE SQUARE",
        0x25B2 to "BLACK UP-POINTING TRIANGLE",
        0x25B3 to "WHITE UP-POINTING TRIANGLE",
        0x25BC to "BLACK DOWN-POINTING TRIANGLE",
        0x25BD to "WHITE DOWN-POINTING TRIANGLE",
        0x25CF to "BLACK CIRCLE",
        0x25CB to "WHITE CIRCLE",
        0x25C6 to "BLACK DIAMOND",
        0x25C7 to "WHITE DIAMOND",
        0x25CE to "BULLSEYE",
        0x25E2 to "BLACK LOWER RIGHT TRIANGLE",
        0x25EF to "LARGE CIRCLE",
        0x2211 to "N-ARY SUMMATION",
        0x220F to "N-ARY PRODUCT",
        0x221A to "SQUARE ROOT",
        0x221E to "INFINITY",
        0x222B to "INTEGRAL",
        0x2248 to "ALMOST EQUAL TO",
        0x2260 to "NOT EQUAL TO",
        0x2264 to "LESS-THAN OR EQUAL TO",
        0x2265 to "GREATER-THAN OR EQUAL TO",
        0x20AC to "EURO SIGN",
        0x20BD to "RUSSIAN RUBLE SIGN",
        0x20B9 to "INDIAN RUPEE SIGN",
        0x20BF to "BITCOIN SIGN",
        0x2190 to "LEFTWARDS ARROW",
        0x2192 to "RIGHTWARDS ARROW",
        0x2191 to "UPWARDS ARROW",
        0x2193 to "DOWNWARDS ARROW",
        0x2605 to "BLACK STAR",
        0x2606 to "WHITE STAR",
        0x2665 to "BLACK HEART SUIT",
        0x2660 to "BLACK SPADE SUIT",
        0x2500 to "BOX DRAWINGS LIGHT HORIZONTAL",
        0x2502 to "BOX DRAWINGS LIGHT VERTICAL",
        0x250C to "BOX DRAWINGS LIGHT DOWN AND RIGHT",
        0x2510 to "BOX DRAWINGS LIGHT DOWN AND LEFT",
        0x2514 to "BOX DRAWINGS LIGHT UP AND RIGHT",
        0x2518 to "BOX DRAWINGS LIGHT UP AND LEFT",
        0x251C to "BOX DRAWINGS LIGHT VERTICAL AND RIGHT",
        0x2524 to "BOX DRAWINGS LIGHT VERTICAL AND LEFT",
        0x252C to "BOX DRAWINGS LIGHT DOWN AND HORIZONTAL",
        0x2534 to "BOX DRAWINGS LIGHT UP AND HORIZONTAL",
        0x253C to "BOX DRAWINGS LIGHT VERTICAL AND HORIZONTAL",
        0x2550 to "BOX DRAWINGS DOUBLE HORIZONTAL",
        0x2551 to "BOX DRAWINGS DOUBLE VERTICAL",
        0x2554 to "BOX DRAWINGS DOUBLE DOWN AND RIGHT",
        0x2557 to "BOX DRAWINGS DOUBLE DOWN AND LEFT",
        0x255A to "BOX DRAWINGS DOUBLE UP AND RIGHT",
        0x255D to "BOX DRAWINGS DOUBLE UP AND LEFT",
        0x2588 to "FULL BLOCK",
        0x2580 to "UPPER HALF BLOCK",
        0x2584 to "LOWER HALF BLOCK"
    )

    private val blockCache = mutableMapOf<String, List<UnicodeCharacter>>()

    fun getCharactersForBlock(block: UnicodeBlock): List<UnicodeCharacter> {
        return blockCache.getOrPut(block.id) {
            val list = ArrayList<UnicodeCharacter>(block.count)
            for (cp in block.startCode..block.endCode) {
                if (Character.isValidCodePoint(cp)) {
                    list.add(createCharacter(cp, block.name))
                }
            }
            list
        }
    }

    fun getCharacter(codePoint: Int, blockName: String? = null): UnicodeCharacter {
        val resolvedBlock = blockName ?: (BLOCKS.firstOrNull { codePoint in it.startCode..it.endCode }?.name ?: "Unicode")
        return createCharacter(codePoint, resolvedBlock)
    }

    fun search(query: String): List<UnicodeCharacter> {
        val clean = query.trim()
        if (clean.isEmpty()) return emptyList()

        val hexTarget = clean.removePrefix("U+").removePrefix("u+").removePrefix("0x").trim().uppercase(Locale.US)
        val queryLower = clean.lowercase(Locale.ROOT)

        val results = mutableListOf<UnicodeCharacter>()
        for (b in BLOCKS) {
            val chars = getCharactersForBlock(b)
            for (c in chars) {
                val matchesChar = c.char == clean
                val matchesHex = hexTarget.isNotEmpty() && c.hex.contains(hexTarget)
                val matchesName = c.name.lowercase(Locale.ROOT).contains(queryLower)
                if (matchesChar || matchesHex || matchesName) {
                    results.add(c)
                    if (results.size >= 160) return results
                }
            }
        }
        return results
    }

    private fun createCharacter(codePoint: Int, blockName: String): UnicodeCharacter {
        val charStr = String(Character.toChars(codePoint))
        val hexStr = Integer.toHexString(codePoint).uppercase(Locale.US).padStart(4, '0')
        val officialName = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Character.getName(codePoint)
            } else null
        } catch (_: Exception) {
            null
        }
        val name = KNOWN_NAMES[codePoint] ?: officialName ?: "$blockName (U+$hexStr)"
        val utf8Bytes = getUtf8Bytes(charStr)
        val htmlEntity = "&#$codePoint;"

        return UnicodeCharacter(
            codePoint = codePoint,
            char = charStr,
            hex = hexStr,
            dec = codePoint,
            utf8Bytes = utf8Bytes,
            name = name,
            blockName = blockName,
            htmlEntity = htmlEntity
        )
    }

    private fun getUtf8Bytes(str: String): String {
        return try {
            val bytes = str.toByteArray(Charsets.UTF_8)
            bytes.joinToString(" ") { String.format(Locale.US, "%02X", it) }
        } catch (_: Exception) {
            ""
        }
    }
}
