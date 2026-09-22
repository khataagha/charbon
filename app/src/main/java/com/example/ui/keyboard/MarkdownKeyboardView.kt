package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCharbonColors

/**
 * Dedicated Markdown layout view for Charbon keyboard.
 * Provides instant Markdown formatting, structural blocks, and symbols.
 */
@Composable
fun MarkdownKeyboardView(
    onInsertText: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onPerformHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCharbonColors.current
    var selectedHeadingLevel by remember { mutableStateOf(1) }

    val markdownSnippets = listOf(
        Pair("Task", "- [ ] "),
        Pair("Divider", "\n---\n"),
        Pair("Table", "| Col 1 | Col 2 |\n|---|---|\n| Item 1 | Item 2 |\n"),
        Pair("Footnote", "[^1]\n\n[^1]: "),
        Pair("Math", "$$  $$"),
        Pair("Details", "<details>\n<summary>Title</summary>\nContent\n</details>"),
        Pair("Highlight", "==highlight=="),
        Pair("Superscript", "^super^")
    )

    val markdownSymbols = listOf(
        "#", "*", "_", "~", "`", ">", "-", "+",
        "[", "]", "(", ")", "{", "}", "|", "\\",
        "!", "@", "$", "%", "^", "&", "=", ":",
        ";", "\"", "'", "<", ">", "/", "?", "."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // --- 1. Markdown Quick Formatting Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.toolbarBackground)
                .border(0.8.dp, colors.keyBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            MarkdownKeyButton(
                icon = Icons.Default.Title,
                label = "H$selectedHeadingLevel",
                tooltip = "Heading H$selectedHeadingLevel",
                onClick = {
                    onInsertText("#".repeat(selectedHeadingLevel) + " ")
                    selectedHeadingLevel = if (selectedHeadingLevel >= 3) 1 else selectedHeadingLevel + 1
                    onPerformHaptic()
                },
                testTag = "kb_md_heading"
            )

            MarkdownKeyButton(
                icon = Icons.Default.FormatBold,
                tooltip = "Bold (**text**)",
                onClick = {
                    onInsertText("****")
                    onPerformHaptic()
                },
                testTag = "kb_md_bold"
            )

            MarkdownKeyButton(
                icon = Icons.Default.FormatItalic,
                tooltip = "Italic (*text*)",
                onClick = {
                    onInsertText("**")
                    onPerformHaptic()
                },
                testTag = "kb_md_italic"
            )

            MarkdownKeyButton(
                icon = Icons.Default.FormatUnderlined,
                tooltip = "Underline (<u>text</u>)",
                onClick = {
                    onInsertText("<u></u>")
                    onPerformHaptic()
                },
                testTag = "kb_md_underline"
            )

            MarkdownKeyButton(
                icon = Icons.Default.FormatStrikethrough,
                tooltip = "Strikethrough (~~text~~)",
                onClick = {
                    onInsertText("~~~~")
                    onPerformHaptic()
                },
                testTag = "kb_md_strikethrough"
            )

            MarkdownKeyButton(
                icon = Icons.Default.Code,
                tooltip = "Inline Code (`code`)",
                onClick = {
                    onInsertText("``")
                    onPerformHaptic()
                },
                testTag = "kb_md_inline_code"
            )

            MarkdownKeyButton(
                icon = Icons.Default.IntegrationInstructions,
                tooltip = "Code Block (```)",
                onClick = {
                    onInsertText("```\n\n```")
                    onPerformHaptic()
                },
                testTag = "kb_md_code_block"
            )

            MarkdownKeyButton(
                icon = Icons.Default.FormatQuote,
                tooltip = "Blockquote (> quote)",
                onClick = {
                    onInsertText("> ")
                    onPerformHaptic()
                },
                testTag = "kb_md_blockquote"
            )

            MarkdownKeyButton(
                icon = Icons.Default.FormatListBulleted,
                tooltip = "Bulleted List (- item)",
                onClick = {
                    onInsertText("- ")
                    onPerformHaptic()
                },
                testTag = "kb_md_bullet_list"
            )

            MarkdownKeyButton(
                icon = Icons.Default.FormatListNumbered,
                tooltip = "Numbered List (1. item)",
                onClick = {
                    onInsertText("1. ")
                    onPerformHaptic()
                },
                testTag = "kb_md_numbered_list"
            )

            MarkdownKeyButton(
                icon = Icons.Default.Link,
                tooltip = "Link ([text](url))",
                onClick = {
                    onInsertText("[](https://)")
                    onPerformHaptic()
                },
                testTag = "kb_md_link"
            )
        }

        // --- 2. Markdown Snippets Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            markdownSnippets.forEach { (name, snippet) ->
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.keySpecialBackground)
                        .border(0.8.dp, colors.keyBorder, RoundedCornerShape(8.dp))
                        .clickable(
                            indication = ripple(bounded = true, color = colors.accent),
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = {
                                onInsertText(snippet)
                                onPerformHaptic()
                            }
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- 3. Markdown Symbol Grid ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(markdownSymbols) { sym ->
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.keyBackground)
                        .border(0.8.dp, colors.keyBorder, RoundedCornerShape(8.dp))
                        .clickable(
                            indication = ripple(bounded = true, color = colors.accent),
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = {
                                onInsertText(sym)
                                onPerformHaptic()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sym,
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- 4. Bottom Action Bar (Space, Backspace, Enter) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Space key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.keyBackground)
                    .border(0.8.dp, colors.keyBorder, RoundedCornerShape(9.dp))
                    .clickable(
                        indication = ripple(bounded = true, color = colors.accent),
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = {
                            onInsertText(" ")
                            onPerformHaptic()
                        }
                    )
                    .testTag("kb_md_space"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Space",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Backspace key
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 40.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.keySpecialBackground)
                    .border(0.8.dp, colors.keyBorder, RoundedCornerShape(9.dp))
                    .clickable(
                        indication = ripple(bounded = true, color = colors.accent),
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = {
                            onBackspace()
                            onPerformHaptic()
                        }
                    )
                    .testTag("kb_md_backspace"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Enter key
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 40.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.accent)
                    .clickable(
                        indication = ripple(bounded = true, color = Color.White),
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = {
                            onEnter()
                            onPerformHaptic()
                        }
                    )
                    .testTag("kb_md_enter"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = Color.Black,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun MarkdownKeyButton(
    icon: ImageVector,
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    testTag: String = ""
) {
    val colors = LocalCharbonColors.current

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.keySpecialBackground)
            .border(0.8.dp, colors.keyBorder, RoundedCornerShape(8.dp))
            .clickable(
                indication = ripple(bounded = true, color = colors.accent),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick
            )
            .semantics {
                contentDescription = tooltip
                role = Role.Button
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            Text(
                text = label,
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = colors.textPrimary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
