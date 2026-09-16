package com.example.ui.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CharbonThemeMode
import com.example.model.KeyboardMode
import com.example.model.UnicodeBlock
import com.example.model.UnicodeCharacter
import com.example.repository.CharbonPreferences
import com.example.repository.UnicodeRepository
import com.example.ui.theme.CharbonTheme
import com.example.ui.theme.LocalCharbonColors

private const val TAG = "CharbonKeyboard"

@Composable
fun CharbonKeyboardContent(
    onInsertText: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onSendKeyEvent: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onSwitchKeyboard: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { CharbonPreferences(context) }

    var currentTheme by remember { mutableStateOf(prefs.themeMode) }
    var currentMode by remember { mutableStateOf(KeyboardMode.CHARMAP) }
    var activeBlock by remember {
        mutableStateOf<UnicodeBlock?>(
            UnicodeRepository.BLOCKS.firstOrNull { it.id == prefs.lastBlockId } ?: UnicodeRepository.BLOCKS.first()
        )
    }
    var isFavoritesActive by remember { mutableStateOf(false) }
    var isRecentsActive by remember { mutableStateOf(false) }
    var activeCustomCollection by remember { mutableStateOf<String?>(null) }
    var customCollections by remember { mutableStateOf(prefs.getCustomCollections()) }
    var favoritesList by remember { mutableStateOf(prefs.getFavorites()) }
    var recentsList by remember { mutableStateOf(prefs.getRecents()) }
    val keyboardHeightDp = prefs.keyboardHeightDp

    var selectedCharacter by remember {
        mutableStateOf<UnicodeCharacter?>(
            UnicodeRepository.getCharacter(0x25AD) // Default to U+25AD White Rectangle
        )
    }
    var inspectorCharacter by remember { mutableStateOf<UnicodeCharacter?>(null) }

    // Haptic feedback function
    fun performHaptic() {
        if (!prefs.hapticFeedbackEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                Log.d(TAG, "Haptic feedback triggered (API 31+)")
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
                Log.d(TAG, "Haptic feedback triggered (legacy)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Haptic feedback error: ${e.message}", e)
        }
    }

    CharbonTheme(mode = currentTheme) {
        val colors = LocalCharbonColors.current

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(colors.keyboardBackground)
                .testTag("charbon_keyboard_container")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
            // --- TOP TOOLBAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(colors.toolbarBackground)
                    .border(width = 0.8.dp, color = colors.divider)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CharMap Tab
                    ToolbarPill(
                        label = "Unicode",
                        icon = Icons.Default.FormatShapes,
                        isSelected = currentMode == KeyboardMode.CHARMAP,
                        onClick = {
                            currentMode = KeyboardMode.CHARMAP
                            performHaptic()
                        },
                        tag = "tab_unicode"
                    )

                    // Alphanumeric Tab
                    ToolbarPill(
                        label = "ABC",
                        icon = Icons.Default.TextFields,
                        isSelected = currentMode == KeyboardMode.ALPHANUMERIC,
                        onClick = {
                            currentMode = KeyboardMode.ALPHANUMERIC
                            performHaptic()
                        },
                        tag = "tab_abc"
                    )

                    // Cursor Tab
                    ToolbarPill(
                        label = "Cursor",
                        icon = Icons.Default.OpenWith,
                        isSelected = currentMode == KeyboardMode.ARROW_PAD,
                        onClick = {
                            currentMode = KeyboardMode.ARROW_PAD
                            performHaptic()
                        },
                        tag = "tab_cursor"
                    )

                    // Kaomoji Tab
                    ToolbarPill(
                        label = "Kaomoji",
                        icon = Icons.Default.Mood,
                        isSelected = currentMode == KeyboardMode.KAOMOJI,
                        onClick = {
                            currentMode = KeyboardMode.KAOMOJI
                            performHaptic()
                        },
                        tag = "tab_kaomoji"
                    )
                }

                // Quick Tool Actions (Theme toggle, Switch IME, Settings)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cycle Theme button
                    Box(
                        modifier = Modifier
                            .size(31.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(colors.keySpecialBackground)
                            .border(0.8.dp, colors.keyBorder, RoundedCornerShape(9.dp))
                            .clickable {
                                val next = when (currentTheme) {
                                    CharbonThemeMode.DARK -> CharbonThemeMode.LIGHT
                                    CharbonThemeMode.LIGHT -> CharbonThemeMode.AMOLED
                                    CharbonThemeMode.AMOLED -> CharbonThemeMode.DARK
                                    CharbonThemeMode.SYSTEM -> CharbonThemeMode.DARK
                                }
                                currentTheme = next
                                prefs.themeMode = next
                                performHaptic()
                            }
                            .testTag("toolbar_theme_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = "Switch Theme",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Switch Keyboard (IME)
                    Box(
                        modifier = Modifier
                            .size(31.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(colors.keySpecialBackground)
                            .border(0.8.dp, colors.keyBorder, RoundedCornerShape(9.dp))
                            .clickable {
                                onSwitchKeyboard()
                                performHaptic()
                            }
                            .testTag("toolbar_switch_keyboard"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Input Method",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Open Settings
                    Box(
                        modifier = Modifier
                            .size(31.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(colors.keySpecialBackground)
                            .border(0.8.dp, colors.keyBorder, RoundedCornerShape(9.dp))
                            .clickable {
                                onOpenSettings()
                                performHaptic()
                            }
                            .testTag("toolbar_settings"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // --- MAIN KEYBOARD BODY (Dynamic height based on user preference) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyboardHeightDp.dp)
            ) {
                when (currentMode) {
                    KeyboardMode.CHARMAP -> {
                        UnicodeGridView(
                            activeBlock = activeBlock,
                            isFavoritesActive = isFavoritesActive,
                            isRecentsActive = isRecentsActive,
                            activeCustomCollection = activeCustomCollection,
                            customCollections = customCollections,
                            favoritesList = favoritesList,
                            recentsList = recentsList,
                            selectedCharacter = selectedCharacter,
                            onSelectBlock = { block ->
                                activeBlock = block
                                isFavoritesActive = false
                                isRecentsActive = false
                                activeCustomCollection = null
                                prefs.lastBlockId = block.id
                                performHaptic()
                            },
                            onSelectFavorites = {
                                isFavoritesActive = true
                                isRecentsActive = false
                                activeCustomCollection = null
                                favoritesList = prefs.getFavorites()
                                performHaptic()
                            },
                            onSelectRecents = {
                                isRecentsActive = true
                                isFavoritesActive = false
                                activeCustomCollection = null
                                recentsList = prefs.getRecents()
                                performHaptic()
                            },
                            onSelectCustomCollection = { colName ->
                                activeCustomCollection = colName
                                isFavoritesActive = false
                                isRecentsActive = false
                                performHaptic()
                            },
                            onCharacterClick = { charItem ->
                                selectedCharacter = charItem
                                onInsertText(charItem.char)
                                prefs.addRecent(charItem.codePoint)
                                recentsList = prefs.getRecents()
                                performHaptic()
                            },
                            onCharacterLongClick = { charItem ->
                                selectedCharacter = charItem
                                inspectorCharacter = charItem
                                performHaptic()
                            },
                            onOpenInspector = { charItem ->
                                inspectorCharacter = charItem
                                performHaptic()
                            },
                            onToggleFavorite = { cp ->
                                prefs.toggleFavorite(cp)
                                favoritesList = prefs.getFavorites()
                                performHaptic()
                            }
                        )
                    }

                    KeyboardMode.ALPHANUMERIC -> {
                        AlphanumericKeyboardView(
                            onInsertText = { text ->
                                onInsertText(text)
                                performHaptic()
                            },
                            onBackspace = {
                                onBackspace()
                                performHaptic()
                            },
                            showQuickSymbols = prefs.showQuickSymbolRow
                        )
                    }

                    KeyboardMode.ARROW_PAD -> {
                        CursorPadView(
                            onSendKeyEvent = { keyEvent ->
                                onSendKeyEvent(keyEvent)
                                performHaptic()
                            },
                            onSelectAll = {
                                onSelectAll()
                                performHaptic()
                            }
                        )
                    }

                    KeyboardMode.KAOMOJI -> {
                        KaomojiGridView(
                            onInsertText = { text ->
                                onInsertText(text)
                                performHaptic()
                            }
                        )
                    }

                    KeyboardMode.CLIPBOARD -> {
                        // Fallback to charmap
                        currentMode = KeyboardMode.CHARMAP
                    }
                }
            }

            // --- BOTTOM CONTROL ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(colors.toolbarBackground)
                    .border(width = 0.8.dp, color = colors.divider)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Layout Toggle Button (ABC / 🔣)
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.keySpecialBackground)
                        .border(1.dp, colors.keyBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            currentMode = if (currentMode == KeyboardMode.CHARMAP) KeyboardMode.ALPHANUMERIC else KeyboardMode.CHARMAP
                            performHaptic()
                        }
                        .testTag("bottom_toggle_mode"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentMode == KeyboardMode.CHARMAP) "ABC" else "🔣 Map",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                }

                // Spacebar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.keyBackground)
                        .border(1.dp, colors.keyBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            onInsertText(" ")
                            performHaptic()
                        }
                        .testTag("key_spacebar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentMode == KeyboardMode.CHARMAP) (activeBlock?.name ?: "Space") else "Charbon Space",
                        color = colors.textSecondary.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                // Continuous Repeating Backspace Button
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.keySpecialBackground)
                        .border(1.dp, colors.keyBorder, RoundedCornerShape(10.dp))
                        .repeatingClickable {
                            onBackspace()
                            performHaptic()
                        }
                        .testTag("bottom_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Enter / Action Button
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.accent)
                        .clickable {
                            onEnter()
                            performHaptic()
                        }
                        .testTag("bottom_enter"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = "Enter",
                        tint = colors.accentText,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Inspector In-Surface Overlay
        inspectorCharacter?.let { inspected ->
            CharacterInspectorDialog(
                character = inspected,
                isFavorite = prefs.isFavorite(inspected.codePoint),
                onToggleFavorite = { cp ->
                    prefs.toggleFavorite(cp)
                    favoritesList = prefs.getFavorites()
                    performHaptic()
                },
                onInsert = { text ->
                    onInsertText(text)
                    prefs.addRecent(inspected.codePoint)
                    recentsList = prefs.getRecents()
                    performHaptic()
                },
                onDismiss = {
                    inspectorCharacter = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
}

@Composable
private fun ToolbarPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val colors = LocalCharbonColors.current
    Row(
        modifier = Modifier
            .height(31.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) colors.accent else colors.keySpecialBackground)
            .border(
                width = if (isSelected) 1.dp else 0.8.dp,
                color = if (isSelected) colors.accent else colors.keyBorder,
                shape = RoundedCornerShape(9.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) colors.accentText else colors.textSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (isSelected) colors.accentText else colors.textPrimary,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.25.sp
        )
    }
}
