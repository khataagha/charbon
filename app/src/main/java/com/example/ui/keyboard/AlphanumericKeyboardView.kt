package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCharbonColors

@Composable
fun AlphanumericKeyboardView(
    onInsertText: (String) -> Unit,
    onBackspace: () -> Unit,
    showQuickSymbols: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = LocalCharbonColors.current
    var isShifted by remember { mutableStateOf(false) }

    val quickSymbols = listOf("±", "≠", "°", "×", "÷", "≈", "∞", "√", "π", "→")
    val row0 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.keyboardBackground)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Optional Quick Symbol strip
        if (showQuickSymbols) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (sym in quickSymbols) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.keySpecialBackground)
                            .border(0.6.dp, colors.keyBorder, RoundedCornerShape(4.dp))
                            .clickable { onInsertText(sym) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sym,
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Number row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (num in row0) {
                AlphaKey(
                    label = num,
                    modifier = Modifier.weight(1f),
                    onClick = { onInsertText(num) }
                )
            }
        }

        // QWERTY row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (char in row1) {
                val display = if (isShifted) char.uppercase() else char
                AlphaKey(
                    label = display,
                    modifier = Modifier.weight(1f),
                    onClick = { onInsertText(display) }
                )
            }
        }

        // ASDF row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (char in row2) {
                val display = if (isShifted) char.uppercase() else char
                AlphaKey(
                    label = display,
                    modifier = Modifier.weight(1f),
                    onClick = { onInsertText(display) }
                )
            }
        }

        // ZXCV row with Shift and Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift key
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isShifted) colors.accent else colors.keySpecialBackground)
                    .border(1.dp, colors.keyBorder, RoundedCornerShape(6.dp))
                    .clickable { isShifted = !isShifted }
                    .testTag("alpha_shift_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Shift",
                    tint = if (isShifted) colors.accentText else colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            for (char in row3) {
                val display = if (isShifted) char.uppercase() else char
                AlphaKey(
                    label = display,
                    modifier = Modifier.weight(1f),
                    onClick = { onInsertText(display) }
                )
            }

            // Continuous repeating Backspace key
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.keySpecialBackground)
                    .border(1.dp, colors.keyBorder, RoundedCornerShape(6.dp))
                    .repeatingClickable { onBackspace() }
                    .testTag("alpha_backspace_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AlphaKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalCharbonColors.current
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.keyBackground)
            .border(0.8.dp, colors.keyBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
