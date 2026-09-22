package com.example.ui.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveFormatState
import com.example.model.InlineFormat
import com.example.ui.theme.LocalCharbonColors

/**
 * A horizontal, responsive, icon-only formatting toolbar for Markdown.
 * Includes Bold, Italic, Underline, Strikethrough, Inline Code, Code Block,
 * Blockquote, Bulleted List, Numbered List, Link, Heading selector, and Undo/Redo.
 */
@Composable
fun MarkdownToolbar(
    activeState: ActiveFormatState,
    canUndo: Boolean,
    canRedo: Boolean,
    onToggleInline: (InlineFormat) -> Unit,
    onToggleHeading: () -> Unit,
    onToggleBlockquote: () -> Unit,
    onToggleBulletList: () -> Unit,
    onToggleNumberedList: () -> Unit,
    onToggleCodeBlock: () -> Unit,
    onInsertLink: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCharbonColors.current
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.toolbarBackground)
            .border(0.8.dp, colors.divider, RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // --- Undo / Redo ---
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            tooltip = "Undo",
            isActive = false,
            isEnabled = canUndo,
            onClick = onUndo,
            testTag = "markdown_btn_undo"
        )
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Filled.Redo,
            tooltip = "Redo",
            isActive = false,
            isEnabled = canRedo,
            onClick = onRedo,
            testTag = "markdown_btn_redo"
        )

        ToolbarDivider()

        // --- Heading Selector ---
        ToolbarIconButton(
            icon = Icons.Default.Title,
            tooltip = if (activeState.headingLevel > 0) "Heading H${activeState.headingLevel} (Click to change)" else "Heading",
            isActive = activeState.headingLevel > 0,
            badge = if (activeState.headingLevel > 0) "H${activeState.headingLevel}" else null,
            onClick = onToggleHeading,
            testTag = "markdown_btn_heading"
        )

        ToolbarDivider()

        // --- Inline Formats ---
        ToolbarIconButton(
            icon = Icons.Default.FormatBold,
            tooltip = "Bold (**text**)",
            isActive = activeState.isBold,
            onClick = { onToggleInline(InlineFormat.BOLD) },
            testTag = "markdown_btn_bold"
        )

        ToolbarIconButton(
            icon = Icons.Default.FormatItalic,
            tooltip = "Italic (*text*)",
            isActive = activeState.isItalic,
            onClick = { onToggleInline(InlineFormat.ITALIC) },
            testTag = "markdown_btn_italic"
        )

        ToolbarIconButton(
            icon = Icons.Default.FormatUnderlined,
            tooltip = "Underline (<u>text</u>)",
            isActive = activeState.isUnderline,
            onClick = { onToggleInline(InlineFormat.UNDERLINE) },
            testTag = "markdown_btn_underline"
        )

        ToolbarIconButton(
            icon = Icons.Default.FormatStrikethrough,
            tooltip = "Strikethrough (~~text~~)",
            isActive = activeState.isStrikethrough,
            onClick = { onToggleInline(InlineFormat.STRIKETHROUGH) },
            testTag = "markdown_btn_strikethrough"
        )

        ToolbarIconButton(
            icon = Icons.Default.Code,
            tooltip = "Inline Code (`code`)",
            isActive = activeState.isInlineCode,
            onClick = { onToggleInline(InlineFormat.INLINE_CODE) },
            testTag = "markdown_btn_inline_code"
        )

        ToolbarDivider()

        // --- Block Formats ---
        ToolbarIconButton(
            icon = Icons.Default.FormatQuote,
            tooltip = "Blockquote (> quote)",
            isActive = activeState.isBlockquote,
            onClick = onToggleBlockquote,
            testTag = "markdown_btn_blockquote"
        )

        ToolbarIconButton(
            icon = Icons.Default.FormatListBulleted,
            tooltip = "Bulleted List (- item)",
            isActive = activeState.isBulletList,
            onClick = onToggleBulletList,
            testTag = "markdown_btn_bullet_list"
        )

        ToolbarIconButton(
            icon = Icons.Default.FormatListNumbered,
            tooltip = "Numbered List (1. item)",
            isActive = activeState.isNumberedList,
            onClick = onToggleNumberedList,
            testTag = "markdown_btn_numbered_list"
        )

        ToolbarIconButton(
            icon = Icons.Default.IntegrationInstructions,
            tooltip = "Code Block (```)",
            isActive = activeState.isCodeBlock,
            onClick = onToggleCodeBlock,
            testTag = "markdown_btn_code_block"
        )

        ToolbarDivider()

        // --- Link ---
        ToolbarIconButton(
            icon = Icons.Default.Link,
            tooltip = "Insert Link ([text](url))",
            isActive = false,
            onClick = onInsertLink,
            testTag = "markdown_btn_link"
        )
    }
}

/**
 * Single icon-only formatting button matching Charbon's squircle styling.
 */
@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    tooltip: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    badge: String? = null,
    testTag: String = ""
) {
    val colors = LocalCharbonColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor = when {
        !isEnabled -> Color.Transparent
        isActive -> colors.accent.copy(alpha = 0.18f)
        else -> colors.keySpecialBackground
    }

    val borderColor = when {
        !isEnabled -> Color.Transparent
        isActive -> colors.accent
        else -> colors.keyBorder
    }

    val tintColor = when {
        !isEnabled -> colors.textSecondary.copy(alpha = 0.35f)
        isActive -> colors.accent
        else -> colors.textPrimary
    }

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .border(if (isActive) 1.2.dp else 0.8.dp, borderColor, RoundedCornerShape(9.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = colors.accent),
                enabled = isEnabled,
                onClick = onClick
            )
            .semantics {
                contentDescription = tooltip
                role = Role.Button
                selected = isActive
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = tintColor,
            modifier = Modifier.size(17.dp)
        )

        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.accent)
                    .padding(horizontal = 2.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.Black,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ToolbarDivider() {
    val colors = LocalCharbonColors.current
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .width(1.dp)
            .height(20.dp)
            .background(colors.divider)
    )
}

/**
 * Formatted Markdown live preview parser and renderer.
 */
@Composable
fun MarkdownPreviewView(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalCharbonColors.current

    if (markdownText.isBlank()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Live preview will appear here as you type Markdown...",
                color = colors.textSecondary.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val lines = markdownText.split("\n")
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()

        for (line in lines) {
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    // Render accumulated code block
                    val codeContent = codeBlockLines.joinToString("\n")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.keySpecialBackground)
                            .border(1.dp, colors.keyBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = codeContent,
                            color = colors.accent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBlockLines.add(line)
                continue
            }

            val trimmed = line.trimStart()

            // Headings
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("### "), colors.accent),
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("## "), colors.accent),
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    Column {
                        Text(
                            text = parseInlineMarkdown(trimmed.removePrefix("# "), colors.accent),
                            color = colors.textPrimary,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 4.dp),
                            thickness = 0.8.dp,
                            color = colors.divider
                        )
                    }
                }
                trimmed.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.accent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.removePrefix("> "), colors.accent),
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = "• ",
                            color = colors.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(2), colors.accent),
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
                Regex("""^\d+\.\s""").containsMatchIn(trimmed) -> {
                    val match = Regex("""^(\d+\.)\s(.*)""").find(trimmed)
                    val prefix = match?.groupValues?.get(1) ?: "1."
                    val content = match?.groupValues?.get(2) ?: trimmed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = "$prefix ",
                            color = colors.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = parseInlineMarkdown(content, colors.accent),
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(line, colors.accent),
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            val codeContent = codeBlockLines.joinToString("\n")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.keySpecialBackground)
                    .border(1.dp, colors.keyBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = codeContent,
                    color = colors.accent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Parses inline markdown: **bold**, *italic*, <u>underline</u>, ~~strikethrough~~, `code`, and [link](url).
 */
private fun parseInlineMarkdown(text: String, linkColor: Color): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var idx = 0
        while (idx < text.length) {
            // Check Link [text](url)
            if (text[idx] == '[') {
                val endBracket = text.indexOf(']', idx)
                if (endBracket != -1 && endBracket + 1 < text.length && text[endBracket + 1] == '(') {
                    val endParen = text.indexOf(')', endBracket + 1)
                    if (endParen != -1) {
                        val linkText = text.substring(idx + 1, endBracket)
                        pushStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
                        append(linkText)
                        pop()
                        idx = endParen + 1
                        continue
                    }
                }
            }

            // Bold **text**
            if (text.startsWith("**", idx)) {
                val endIdx = text.indexOf("**", idx + 2)
                if (endIdx != -1) {
                    val inner = text.substring(idx + 2, endIdx)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(inner)
                    pop()
                    idx = endIdx + 2
                    continue
                }
            }

            // Strikethrough ~~text~~
            if (text.startsWith("~~", idx)) {
                val endIdx = text.indexOf("~~", idx + 2)
                if (endIdx != -1) {
                    val inner = text.substring(idx + 2, endIdx)
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(inner)
                    pop()
                    idx = endIdx + 2
                    continue
                }
            }

            // Underline <u>text</u>
            if (text.startsWith("<u>", idx, ignoreCase = true)) {
                val endIdx = text.indexOf("</u>", idx + 3, ignoreCase = true)
                if (endIdx != -1) {
                    val inner = text.substring(idx + 3, endIdx)
                    pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    append(inner)
                    pop()
                    idx = endIdx + 4
                    continue
                }
            }

            // Inline Code `text`
            if (text[idx] == '`') {
                val endIdx = text.indexOf('`', idx + 1)
                if (endIdx != -1) {
                    val inner = text.substring(idx + 1, endIdx)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.Gray.copy(alpha = 0.2f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    append(" $inner ")
                    pop()
                    idx = endIdx + 1
                    continue
                }
            }

            // Italic *text*
            if (text[idx] == '*') {
                val endIdx = text.indexOf('*', idx + 1)
                if (endIdx != -1) {
                    val inner = text.substring(idx + 1, endIdx)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(inner)
                    pop()
                    idx = endIdx + 1
                    continue
                }
            }

            append(text[idx])
            idx++
        }
    }
}
