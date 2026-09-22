package com.example.model

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Supported inline markdown formatting styles.
 */
enum class InlineFormat(
    val openDelimiter: String,
    val closeDelimiter: String,
    val label: String
) {
    BOLD("**", "**", "Bold"),
    ITALIC("*", "*", "Italic"),
    UNDERLINE("<u>", "</u>", "Underline"),
    STRIKETHROUGH("~~", "~~", "Strikethrough"),
    INLINE_CODE("`", "`", "Inline Code")
}

/**
 * Current active state of all markdown formats at the cursor or current selection.
 */
data class ActiveFormatState(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val isInlineCode: Boolean = false,
    val isCodeBlock: Boolean = false,
    val isBlockquote: Boolean = false,
    val isBulletList: Boolean = false,
    val isNumberedList: Boolean = false,
    val headingLevel: Int = 0 // 0 = none, 1 = H1, 2 = H2, 3 = H3
) {
    fun isFormatActive(format: InlineFormat): Boolean = when (format) {
        InlineFormat.BOLD -> isBold
        InlineFormat.ITALIC -> isItalic
        InlineFormat.UNDERLINE -> isUnderline
        InlineFormat.STRIKETHROUGH -> isStrikethrough
        InlineFormat.INLINE_CODE -> isInlineCode
    }
}

/**
 * Core Markdown formatting engine providing smart selection wrapping,
 * format detection, typing mode, and toggle unwrapping.
 */
object MarkdownFormattingEngine {

    /**
     * Inspects the current text and selection/cursor position to determine
     * which formatting styles are currently active.
     */
    fun detectActiveFormats(
        value: TextFieldValue,
        pendingInlineStyles: Set<InlineFormat> = emptySet()
    ): ActiveFormatState {
        val text = value.text
        val selection = value.selection

        // Check line-level block formats
        val currentLine = getLineAtCursor(text, selection.start)
        val trimmedLine = currentLine.trimStart()

        val isBlockquote = trimmedLine.startsWith("> ")
        val isBulletList = trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")
        val isNumberedList = Regex("""^\d+\.\s""").containsMatchIn(trimmedLine)
        val headingLevel = when {
            trimmedLine.startsWith("### ") -> 3
            trimmedLine.startsWith("## ") -> 2
            trimmedLine.startsWith("# ") -> 1
            else -> 0
        }

        val isCodeBlock = isInsideCodeBlock(text, selection.start)

        // Check inline formats
        val isBold = pendingInlineStyles.contains(InlineFormat.BOLD) ||
                isStyleActiveAtRange(text, selection, InlineFormat.BOLD)
        val isItalic = pendingInlineStyles.contains(InlineFormat.ITALIC) ||
                isStyleActiveAtRange(text, selection, InlineFormat.ITALIC)
        val isUnderline = pendingInlineStyles.contains(InlineFormat.UNDERLINE) ||
                isStyleActiveAtRange(text, selection, InlineFormat.UNDERLINE)
        val isStrikethrough = pendingInlineStyles.contains(InlineFormat.STRIKETHROUGH) ||
                isStyleActiveAtRange(text, selection, InlineFormat.STRIKETHROUGH)
        val isInlineCode = pendingInlineStyles.contains(InlineFormat.INLINE_CODE) ||
                isStyleActiveAtRange(text, selection, InlineFormat.INLINE_CODE)

        return ActiveFormatState(
            isBold = isBold,
            isItalic = isItalic,
            isUnderline = isUnderline,
            isStrikethrough = isStrikethrough,
            isInlineCode = isInlineCode,
            isCodeBlock = isCodeBlock,
            isBlockquote = isBlockquote,
            isBulletList = isBulletList,
            isNumberedList = isNumberedList,
            headingLevel = headingLevel
        )
    }

    /**
     * Toggles an inline formatting style.
     * If text is selected:
     *   - If already formatted with this style, removes delimiters.
     *   - Otherwise, wraps selected text in delimiters.
     * If no text is selected:
     *   - If inside formatted text, unwraps it.
     *   - Otherwise toggles pre-selection typing mode with delimiter insertion.
     */
    fun toggleInlineFormat(
        value: TextFieldValue,
        format: InlineFormat,
        pendingStyles: Set<InlineFormat> = emptySet()
    ): Pair<TextFieldValue, Set<InlineFormat>> {
        val text = value.text
        val selection = value.selection
        val open = format.openDelimiter
        val close = format.closeDelimiter

        // Case 1: Text is selected
        if (!selection.collapsed) {
            val start = minOf(selection.start, selection.end)
            val end = maxOf(selection.start, selection.end)
            val selectedText = text.substring(start, end)

            // Check if selected text is explicitly wrapped in delimiters
            if (selectedText.startsWith(open) && selectedText.endsWith(close) &&
                selectedText.length >= open.length + close.length) {
                // Unwrap
                val unwrapped = selectedText.substring(open.length, selectedText.length - close.length)
                val newText = text.substring(0, start) + unwrapped + text.substring(end)
                val newSelection = TextRange(start, start + unwrapped.length)
                return Pair(TextFieldValue(text = newText, selection = newSelection), pendingStyles - format)
            }

            // Check if surrounding text outside selection wraps this selection
            if (start >= open.length && end + close.length <= text.length) {
                val surroundingOpen = text.substring(start - open.length, start)
                val surroundingClose = text.substring(end, end + close.length)
                if (surroundingOpen == open && surroundingClose == close) {
                    // Remove surrounding delimiters
                    val newText = text.substring(0, start - open.length) + selectedText + text.substring(end + close.length)
                    val newSelection = TextRange(start - open.length, start - open.length + selectedText.length)
                    return Pair(TextFieldValue(text = newText, selection = newSelection), pendingStyles - format)
                }
            }

            // Otherwise, wrap selection with delimiters
            val wrapped = open + selectedText + close
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val newSelection = TextRange(start + open.length, start + open.length + selectedText.length)
            return Pair(TextFieldValue(text = newText, selection = newSelection), pendingStyles)
        }

        // Case 2: No text is selected (Cursor only)
        val cursor = selection.start

        // Check if cursor is directly inside matching empty delimiters e.g. **|**
        if (cursor >= open.length && cursor + close.length <= text.length) {
            val before = text.substring(cursor - open.length, cursor)
            val after = text.substring(cursor, cursor + close.length)
            if (before == open && after == close) {
                // Remove empty delimiters
                val newText = text.substring(0, cursor - open.length) + text.substring(cursor + close.length)
                return Pair(
                    TextFieldValue(text = newText, selection = TextRange(cursor - open.length)),
                    pendingStyles - format
                )
            }
        }

        // Check if cursor is inside non-empty formatted text e.g. **he|llo**
        val enclosing = findEnclosingDelimiters(text, cursor, format)
        if (enclosing != null) {
            val (openIdx, closeIdx) = enclosing
            // Unwrap formatted word
            val inner = text.substring(openIdx + open.length, closeIdx)
            val newText = text.substring(0, openIdx) + inner + text.substring(closeIdx + close.length)
            val newCursor = (cursor - open.length).coerceIn(openIdx, openIdx + inner.length)
            return Pair(
                TextFieldValue(text = newText, selection = TextRange(newCursor)),
                pendingStyles - format
            )
        }

        // Toggle pre-selection pending typing mode
        if (pendingStyles.contains(format)) {
            // Deactivate
            if (cursor + close.length <= text.length && text.substring(cursor, cursor + close.length) == close) {
                return Pair(
                    TextFieldValue(text = text, selection = TextRange(cursor + close.length)),
                    pendingStyles - format
                )
            }
            return Pair(value, pendingStyles - format)
        } else {
            // Activate and insert paired delimiters
            val newText = text.substring(0, cursor) + open + close + text.substring(cursor)
            return Pair(
                TextFieldValue(text = newText, selection = TextRange(cursor + open.length)),
                pendingStyles + format
            )
        }
    }

    /**
     * Toggles blockquote (> ) for all selected lines or line at cursor.
     */
    fun toggleBlockquote(value: TextFieldValue): TextFieldValue =
        toggleLinePrefix(value, prefix = "> ")

    /**
     * Toggles bullet list (- ) for all selected lines or line at cursor.
     */
    fun toggleBulletList(value: TextFieldValue): TextFieldValue =
        toggleLinePrefix(value, prefix = "- ")

    /**
     * Toggles numbered list (1. , 2. , etc.) for all selected lines or line at cursor.
     */
    fun toggleNumberedList(value: TextFieldValue): TextFieldValue =
        toggleNumberedLinePrefix(value)

    /**
     * Cycles or toggles heading level: # -> ## -> ### -> none.
     */
    fun toggleHeading(value: TextFieldValue, targetLevel: Int? = null): TextFieldValue {
        val text = value.text
        val selection = value.selection
        val cursor = selection.start

        val lineInfo = getLineRange(text, cursor)
        val line = text.substring(lineInfo.first, lineInfo.second)
        val trimmed = line.trimStart()
        val leadingSpaces = line.length - trimmed.length
        val indent = line.substring(0, leadingSpaces)

        val currentLevel = when {
            trimmed.startsWith("### ") -> 3
            trimmed.startsWith("## ") -> 2
            trimmed.startsWith("# ") -> 1
            else -> 0
        }

        val nextLevel = targetLevel ?: when (currentLevel) {
            0 -> 1
            1 -> 2
            2 -> 3
            else -> 0
        }

        // Strip existing heading hashes and spaces
        val cleanContent = trimmed.replace(Regex("""^#{1,6}\s*"""), "")
        val newLine = if (nextLevel > 0) {
            indent + "#".repeat(nextLevel) + " " + cleanContent
        } else {
            indent + cleanContent
        }

        val newText = text.substring(0, lineInfo.first) + newLine + text.substring(lineInfo.second)
        val delta = newLine.length - line.length
        val newCursor = (cursor + delta).coerceIn(lineInfo.first, lineInfo.first + newLine.length)

        return TextFieldValue(text = newText, selection = TextRange(newCursor))
    }

    /**
     * Toggles code block (```) around selected lines or inserts empty block.
     */
    fun toggleCodeBlock(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val selection = value.selection
        val start = minOf(selection.start, selection.end)
        val end = maxOf(selection.start, selection.end)

        if (selection.collapsed) {
            val codeBlock = "```\n\n```"
            val newText = text.substring(0, start) + codeBlock + text.substring(end)
            return TextFieldValue(text = newText, selection = TextRange(start + 4))
        } else {
            val selected = text.substring(start, end)
            if (selected.startsWith("```") && selected.endsWith("```")) {
                // Unwrap code block
                var inner = selected.removePrefix("```").removeSuffix("```")
                if (inner.startsWith("\n")) inner = inner.substring(1)
                if (inner.endsWith("\n")) inner = inner.substring(0, inner.length - 1)
                val newText = text.substring(0, start) + inner + text.substring(end)
                return TextFieldValue(text = newText, selection = TextRange(start, start + inner.length))
            } else {
                val wrapped = "```\n$selected\n```"
                val newText = text.substring(0, start) + wrapped + text.substring(end)
                return TextFieldValue(text = newText, selection = TextRange(start + 4, start + 4 + selected.length))
            }
        }
    }

    /**
     * Inserts or wraps a link [text](url).
     */
    fun insertLink(value: TextFieldValue, defaultUrl: String = "https://"): TextFieldValue {
        val text = value.text
        val selection = value.selection
        val start = minOf(selection.start, selection.end)
        val end = maxOf(selection.start, selection.end)

        if (selection.collapsed) {
            val linkTemplate = "[link]($defaultUrl)"
            val newText = text.substring(0, start) + linkTemplate + text.substring(end)
            val urlStart = start + 7
            return TextFieldValue(
                text = newText,
                selection = TextRange(urlStart, urlStart + defaultUrl.length)
            )
        } else {
            val selected = text.substring(start, end)
            val linkString = "[$selected]($defaultUrl)"
            val newText = text.substring(0, start) + linkString + text.substring(end)
            val urlStart = start + selected.length + 3
            return TextFieldValue(
                text = newText,
                selection = TextRange(urlStart, urlStart + defaultUrl.length)
            )
        }
    }

    /**
     * Handles typing in pre-selection typing mode.
     * If user types text when pendingStyles are active, wraps the inserted text.
     */
    fun handleTextCommit(
        current: TextFieldValue,
        textToInsert: String,
        pendingStyles: Set<InlineFormat>
    ): TextFieldValue {
        if (pendingStyles.isEmpty()) {
            val start = minOf(current.selection.start, current.selection.end)
            val end = maxOf(current.selection.start, current.selection.end)
            val newText = current.text.substring(0, start) + textToInsert + current.text.substring(end)
            return TextFieldValue(text = newText, selection = TextRange(start + textToInsert.length))
        }

        // Apply all pending styles in order
        var wrapped = textToInsert
        for (style in pendingStyles) {
            wrapped = style.openDelimiter + wrapped + style.closeDelimiter
        }

        val start = minOf(current.selection.start, current.selection.end)
        val end = maxOf(current.selection.start, current.selection.end)
        val newText = current.text.substring(0, start) + wrapped + current.text.substring(end)
        val closeOffset = pendingStyles.sumOf { it.closeDelimiter.length }
        val cursorPosition = start + wrapped.length - closeOffset

        return TextFieldValue(text = newText, selection = TextRange(cursorPosition))
    }

    // --- Private Helper Utilities ---

    private fun getLineRange(text: String, index: Int): Pair<Int, Int> {
        val safeIndex = index.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', safeIndex - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', safeIndex).let { if (it == -1) text.length else it }
        return Pair(lineStart, lineEnd)
    }

    private fun getLineAtCursor(text: String, cursor: Int): String {
        val (start, end) = getLineRange(text, cursor)
        return text.substring(start, end)
    }

    private fun toggleLinePrefix(value: TextFieldValue, prefix: String): TextFieldValue {
        val text = value.text
        val selection = value.selection
        val start = minOf(selection.start, selection.end)
        val end = maxOf(selection.start, selection.end)

        val firstLineRange = getLineRange(text, start)
        val lastLineRange = getLineRange(text, end)

        val affectedText = text.substring(firstLineRange.first, lastLineRange.second)
        val lines = affectedText.split('\n')

        val allHavePrefix = lines.all { it.trimStart().startsWith(prefix) }

        val newLines = lines.map { line ->
            if (allHavePrefix) {
                // Remove prefix
                val trimmed = line.trimStart()
                val leading = line.substring(0, line.length - trimmed.length)
                leading + trimmed.removePrefix(prefix)
            } else {
                // Add prefix
                val trimmed = line.trimStart()
                val leading = line.substring(0, line.length - trimmed.length)
                leading + prefix + trimmed
            }
        }

        val replacement = newLines.joinToString("\n")
        val newText = text.substring(0, firstLineRange.first) + replacement + text.substring(lastLineRange.second)
        return TextFieldValue(
            text = newText,
            selection = TextRange(firstLineRange.first, firstLineRange.first + replacement.length)
        )
    }

    private fun toggleNumberedLinePrefix(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val selection = value.selection
        val start = minOf(selection.start, selection.end)
        val end = maxOf(selection.start, selection.end)

        val firstLineRange = getLineRange(text, start)
        val lastLineRange = getLineRange(text, end)

        val affectedText = text.substring(firstLineRange.first, lastLineRange.second)
        val lines = affectedText.split('\n')

        val numberRegex = Regex("""^\d+\.\s""")
        val allHaveNumber = lines.all { numberRegex.containsMatchIn(it.trimStart()) }

        val newLines = lines.mapIndexed { index, line ->
            val trimmed = line.trimStart()
            val leading = line.substring(0, line.length - trimmed.length)
            if (allHaveNumber) {
                leading + trimmed.replaceFirst(numberRegex, "")
            } else {
                leading + "${index + 1}. " + trimmed
            }
        }

        val replacement = newLines.joinToString("\n")
        val newText = text.substring(0, firstLineRange.first) + replacement + text.substring(lastLineRange.second)
        return TextFieldValue(
            text = newText,
            selection = TextRange(firstLineRange.first, firstLineRange.first + replacement.length)
        )
    }

    private fun findEnclosingDelimiters(text: String, cursor: Int, format: InlineFormat): Pair<Int, Int>? {
        val open = format.openDelimiter
        val close = format.closeDelimiter
        val (lineStart, lineEnd) = getLineRange(text, cursor)
        val line = text.substring(lineStart, lineEnd)
        val cursorInLine = cursor - lineStart

        var searchIndex = 0
        while (searchIndex < line.length) {
            val openIdx = line.indexOf(open, searchIndex)
            if (openIdx == -1) break
            val closeIdx = line.indexOf(close, openIdx + open.length)
            if (closeIdx == -1) break

            // If cursor is within openIdx and closeIdx + close.length
            if (cursorInLine in (openIdx + open.length)..closeIdx) {
                return Pair(lineStart + openIdx, lineStart + closeIdx)
            }
            searchIndex = closeIdx + close.length
        }
        return null
    }

    private fun isStyleActiveAtRange(text: String, selection: TextRange, format: InlineFormat): Boolean {
        val open = format.openDelimiter
        val close = format.closeDelimiter
        val cursor = selection.start

        if (!selection.collapsed) {
            val start = minOf(selection.start, selection.end)
            val end = maxOf(selection.start, selection.end)
            val selected = text.substring(start, end)
            if (selected.startsWith(open) && selected.endsWith(close) && selected.length >= open.length + close.length) {
                return true
            }
            if (start >= open.length && end + close.length <= text.length) {
                val b = text.substring(start - open.length, start)
                val a = text.substring(end, end + close.length)
                if (b == open && a == close) return true
            }
        }

        val (lineStart, lineEnd) = getLineRange(text, cursor)
        val line = text.substring(lineStart, lineEnd)
        val cursorInLine = cursor - lineStart

        var searchIndex = 0
        while (searchIndex < line.length) {
            val openIdx = line.indexOf(open, searchIndex)
            if (openIdx == -1) break
            val closeIdx = line.indexOf(close, openIdx + open.length)
            if (closeIdx == -1) break

            if (cursorInLine in openIdx..(closeIdx + close.length)) {
                return true
            }
            searchIndex = closeIdx + close.length
        }

        return false
    }

    private fun isInsideCodeBlock(text: String, cursor: Int): Boolean {
        var count = 0
        var pos = 0
        while (pos < cursor) {
            val idx = text.indexOf("```", pos)
            if (idx == -1 || idx >= cursor) break
            count++
            pos = idx + 3
        }
        return count % 2 == 1
    }
}
